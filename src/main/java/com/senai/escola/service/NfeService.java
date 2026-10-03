package com.senai.escola.service;



import com.senai.escola.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;

import javax.xml.crypto.*;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.*;
import javax.xml.crypto.dsig.spec.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NfeService {
    @Value("${app.nfe.uf}") private String uf;
    @Value("${app.nfe.ambiente}") private int ambiente;
    @Value("${app.nfe.certificado-path}") private String certPath;
    @Value("${app.nfe.certificado-senha}") private String certSenha;

    public String gerarXml(Empresa emp, NotaFiscal nf) {
        String cnpjEmp = emp.getCnpj().replaceAll("\\D","");
        String ie = emp.getInscricaoEstadual() != null ? emp.getInscricaoEstadual() : "";
        String chave = nf.getChaveAcesso() != null ? nf.getChaveAcesso() : gerarChave(cnpjEmp, nf);

        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <NFe xmlns="http://www.portalfiscal.inf.br/nfe">
              <infNFe versao="4.00" Id="NFe%s">
                <ide>
                  <cUF>%s</cUF><natOp>VENDA</natOp><mod>55</mod><serie>1</serie>
                  <nNF>%d</nNF><dhEmi>%sT12:00:00-03:00</dhEmi>
                  <tpNF>1</tpNF><idDest>1</idDest><cMunFG>3550308</cMunFG>
                  <tpImp>1</tpImp><tpEmis>1</tpEmis><cDV>0</cDV>
                  <tpAmb>%d</tpAmb><procEmi>0</procEmi><verProc>ERP-FISCAL-3.0</verProc>
                </ide>
                <emit>
                  <CNPJ>%s</CNPJ><xNome>%s</xNome><IE>%s</IE><CRT>3</CRT>
                </emit>
                <dest>
                  <CNPJ>%s</CNPJ><xNome>%s</xNome><indIEDest>1</indIEDest>
                </dest>
                <det nItem="1">
                  <prod>
                    <cProd>001</cProd><cEAN>SEM GTIN</cEAN>
                    <xProd>MERCADORIA</xProd><NCM>84714900</NCM>
                    <CFOP>%s</CFOP><uCom>UN</uCom><qCom>1</qCom>
                    <vUnCom>%s</vUnCom><vProd>%s</vProd>
                    <cEANTrib>SEM GTIN</cEANTrib>
                    <uTrib>UN</uTrib><qTrib>1</qTrib>
                    <vUnTrib>%s</vUnTrib><indTot>1</indTot>
                  </prod>
                  <imposto>
                    <ICMS><ICMS00><orig>0</orig><CST>00</CST><modBC>0</modBC>
                      <vBC>%s</vBC><pICMS>18.00</pICMS><vICMS>%s</vICMS>
                    </ICMS00></ICMS>
                    <PIS><PISAliq><CST>01</CST><vBC>%s</vBC><pPIS>1.65</pPIS><vPIS>0</vPIS></PISAliq></PIS>
                    <COFINS><COFINSAliq><CST>01</CST><vBC>%s</vBC><pCOFINS>7.60</pCOFINS><vCOFINS>0</vCOFINS></COFINSAliq></COFINS>
                  </imposto>
                </det>
                <total>
                  <ICMSTot>
                    <vBC>%s</vBC><vICMS>%s</vICMS><vProd>%s</vProd><vNF>%s</vNF>
                  </ICMSTot>
                </total>
                <transp><modFrete>9</modFrete></transp>
              </infNFe>
            </NFe>
            """.formatted(
                chave, uf, nf.getNumero(), nf.getDataEmissao(), ambiente,
                cnpjEmp, emp.getRazaoSocial(), ie,
                nf.getCnpjDestinatario() != null ? nf.getCnpjDestinatario() : "00000000000000",
                nf.getDestinatario() != null ? nf.getDestinatario() : "CONSUMIDOR FINAL",
                nf.getCfop() != null ? nf.getCfop() : "5102",
                nf.getValorProdutos().toPlainString(), nf.getValorProdutos().toPlainString(),
                nf.getValorProdutos().toPlainString(),
                nf.getBaseIcms() != null ? nf.getBaseIcms().toPlainString() : "0",
                nf.getValorIcms() != null ? nf.getValorIcms().toPlainString() : "0",
                nf.getValorProdutos().toPlainString(), nf.getValorProdutos().toPlainString(),
                nf.getBaseIcms() != null ? nf.getBaseIcms().toPlainString() : "0",
                nf.getValorIcms() != null ? nf.getValorIcms().toPlainString() : "0",
                nf.getValorProdutos().toPlainString(), nf.getValorProdutos().toPlainString()
        );
    }

    private String gerarChave(String cnpj, NotaFiscal nf) {
        return String.format("%s%02d%02d55001%09d%d%09d",
                cnpj, nf.getDataEmissao().getYear() % 100, nf.getDataEmissao().getMonthValue(),
                nf.getNumero(), 1, 1);
    }

    public String assinar(String xml) throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        File f = new File(certPath);
        if (!f.exists()) {
            // Certificado de teste em desenvolvimento
            return xml.replace("</infNFe>", "<Signature/>" + "</infNFe>");
        }
        try (InputStream in = new FileInputStream(f)) {
            ks.load(in, certSenha.toCharArray());
        }
        String alias = ks.aliases().nextElement();
        PrivateKey pk = (PrivateKey) ks.getKey(alias, certSenha.toCharArray());
        X509Certificate cert = (X509Certificate) ks.getCertificate(alias);

        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes()));

        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
        Reference ref = fac.newReference("#infNFe",
                fac.newDigestMethod(DigestMethod.SHA1, null),
                List.of(
                    fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null),
                    fac.newTransform("http://www.w3.org/TR/2001/REC-xml-c14n-20010315", (TransformParameterSpec) null)
                ), null, null);

        KeyInfoFactory kif = fac.getKeyInfoFactory();
        KeyInfo ki = kif.newKeyInfo(List.of(kif.newX509Data(List.of(cert))));

        XMLSignature sig = fac.newXMLSignature(
                fac.newSignedInfo(fac.newCanonicalizationMethod(
                        CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
                        fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
                        List.of(ref)), ki);

        NodeList nodes = doc.getElementsByTagName("infNFe");
        if (nodes.getLength() == 0) return xml;
        Node alvo = nodes.item(0);

        DOMSignContext ctx = new DOMSignContext(pk, alvo.getParentNode());
        ctx.putNamespacePrefix(XMLSignature.XMLNS, "ds");
        sig.sign(ctx);

        java.io.StringWriter sw = new java.io.StringWriter();
        TransformerFactory.newInstance().newTransformer()
                .transform(new DOMSource(doc), new StreamResult(sw));
        return sw.toString();
    }
}