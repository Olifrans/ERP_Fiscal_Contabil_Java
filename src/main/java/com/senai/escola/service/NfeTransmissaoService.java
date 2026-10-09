package com.senai.escola.service;


import com.senai.escola.entity.NotaFiscal;
import com.senai.escola.repository.NotaFiscalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyStore;
import javax.net.ssl.*;

@Service
@RequiredArgsConstructor
public class NfeTransmissaoService {

    private final NotaFiscalRepository nfRepo;
    private final NfeService nfeService; // Seu serviço existente que gera e assina o XML

    @Value("${app.nfe.certificado-path}")
    private String certPath;
    @Value("${app.nfe.certificado-senha}")
    private String certSenha;

    // URL de Homologação da SEFAZ SP (SVRS)
    private static final String URL_AUTORIZACAO = "https://homologacao.nfe.fazenda.sp.gov.br/ws/nfeautorizacao4.asmx";

    @Transactional
    public String transmitirNfe(Long nfId) throws Exception {
        NotaFiscal nf = nfRepo.findById(nfId).orElseThrow(() -> new RuntimeException("NF não encontrada"));
        
        // 1. Gera e assina o XML (seu método existente)
        String xmlAssinado = nfeService.assinar(nfeService.gerarXml(nf.getEmpresa(), nf));

        // 2. Monta o Envelope SOAP da SEFAZ
        String soapEnvelope = """
            <soap12:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
                             xmlns:xsd="http://www.w3.org/2001/XMLSchema" 
                             xmlns:soap12="http://www.w3.org/2003/05/soap-envelope">
              <soap12:Header>
                <nfeCabecMsg xmlns="http://www.portalfiscal.inf.br/nfe/wsdl/NFeAutorizacao4">
                  <cUF>35</cUF>
                  <versaoDados>4.00</versaoDados>
                </nfeCabecMsg>
              </soap12:Header>
              <soap12:Body>
                <nfeDadosMsg xmlns="http://www.portalfiscal.inf.br/nfe/wsdl/NFeAutorizacao4">
                  <enviNFe versao="4.00" xmlns="http://www.portalfiscal.inf.br/nfe">
                    <idLote>1</idLote>
                    <indSinc>1</indSinc>
                    %s
                  </enviNFe>
                </nfeDadosMsg>
              </soap12:Body>
            </soap12:Envelope>
            """.formatted(xmlAssinado);

        // 3. Configura o SSL com o Certificado A1 (Mutual TLS)
        SSLContext sslContext = criarSSLContext();

        // 4. Envia via Java 21 HttpClient
        HttpClient client = HttpClient.newBuilder()
                .sslContext(sslContext)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_AUTORIZACAO))
                .header("Content-Type", "application/soap+xml; charset=utf-8")
                .header("SOAPAction", "http://www.portalfiscal.inf.br/nfe/wsdl/NFeAutorizacao4/nfeAutorizacaoLote")
                .POST(HttpRequest.BodyPublishers.ofString(soapEnvelope))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // 5. Atualiza status no banco
        if (response.body().contains("<cStat>103</cStat>") || response.body().contains("<cStat>104</cStat>")) {
            nf.setStatus("ENVIADA");
            // Aqui você extrairia o 'nRec' (número do recibo) do XML de resposta para consultar o processamento depois
        } else {
            nf.setStatus("REJEITADA");
        }
        nf.setProtocolo("Recibo SEFAZ: " + System.currentTimeMillis()); // Simplificado para MVP
        nf.setXml(response.body()); // Salva o retorno da SEFAZ
        nfRepo.save(nf);

        return response.body();
    }

    private SSLContext criarSSLContext() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(certPath)) {
            keyStore.load(fis, certSenha.toCharArray());
        }
        
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, certSenha.toCharArray());
        
        SSLContext sc = SSLContext.getInstance("TLSv1.2");
        sc.init(kmf.getKeyManagers(), null, null);
        return sc;
    }
}