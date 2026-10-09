package com.senai.escola.service;



import com.senai.escola.service.RazaoBalanceteService.ItemBalancete;
import com.senai.escola.service.RazaoBalanceteService.ItemRazao;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RelatorioPdfService {

    public byte[] balancetePdf(List<ItemBalancete> itens, LocalDate ini, LocalDate fim, String empresa) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);
        PdfWriter.getInstance(doc, out);
        doc.open();

        Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font sub = FontFactory.getFont(FontFactory.HELVETICA, 10);
        
        Paragraph p = new Paragraph("BALANCETE ANALÍTICO", titulo);
        p.setAlignment(Element.ALIGN_CENTER);
        doc.add(p);
        doc.add(new Paragraph(empresa, sub));
        doc.add(new Paragraph("Período: " + ini + " a " + fim, sub));
        doc.add(new Paragraph(" "));

        PdfPTable tab = new PdfPTable(6);
        tab.setWidthPercentage(100);
        tab.setWidths(new float[]{2f, 5f, 2.5f, 2.5f, 2.5f, 2.5f});

        // ✅ CORREÇÃO: A cor branca é definida no Font, não na célula
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Font.NORMAL, Color.WHITE);
        Color headerBg = new Color(52, 73, 94);

        String[] headers = {"Código", "Descrição", "Débitos", "Créditos", "Saldo D", "Saldo C"};
        for (String h : headers) {
            PdfPCell c = new PdfPCell(new Phrase(h, headerFont));
            c.setBackgroundColor(headerBg);
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setPadding(5);
            tab.addCell(c);
        }

        BigDecimal totD = BigDecimal.ZERO, totC = BigDecimal.ZERO;
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
        
        for (var it : itens) {
            tab.addCell(new PdfPCell(new Phrase(it.codigo(), cellFont)));
            tab.addCell(new PdfPCell(new Phrase(it.descricao(), cellFont)));
            tab.addCell(new PdfPCell(new Phrase(fmt(it.totalDeb()), cellFont)));
            tab.addCell(new PdfPCell(new Phrase(fmt(it.totalCred()), cellFont)));
            tab.addCell(new PdfPCell(new Phrase(it.saldo().signum() >= 0 ? fmt(it.saldo()) : "", cellFont)));
            tab.addCell(new PdfPCell(new Phrase(it.saldo().signum() < 0 ? fmt(it.saldo().abs()) : "", cellFont)));
            
            totD = totD.add(it.totalDeb());
            totC = totC.add(it.totalCred());
        }

        // ✅ CORREÇÃO: Phrase passada no construtor, não via setPhrase()
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        Color totalBg = new Color(236, 240, 241);
        
        PdfPCell boldCell1 = new PdfPCell(new Phrase("TOTAIS", boldFont));
        boldCell1.setBackgroundColor(totalBg);
        
        PdfPCell boldCell2 = new PdfPCell(new Phrase("TOTAIS", boldFont));
        boldCell2.setBackgroundColor(totalBg);

        tab.addCell(boldCell1);
        tab.addCell(boldCell2);
        tab.addCell(new PdfPCell(new Phrase(fmt(totD), boldFont)));
        tab.addCell(new PdfPCell(new Phrase(fmt(totC), boldFont)));
        tab.addCell(new PdfPCell(new Phrase("", boldFont)));
        tab.addCell(new PdfPCell(new Phrase("", boldFont)));

        doc.add(tab);
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("Emitido em " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                + " — ERP Fiscal Contábil v3.0", sub));
        doc.close();
        return out.toByteArray();
    }

    public byte[] razaoPdf(List<ItemRazao> itens, String conta, LocalDate ini, LocalDate fim, String empresa) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);
        PdfWriter.getInstance(doc, out);
        doc.open();

        Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font sub = FontFactory.getFont(FontFactory.HELVETICA, 10);
        
        Paragraph p = new Paragraph("LIVRO RAZÃO", titulo);
        p.setAlignment(Element.ALIGN_CENTER);
        doc.add(p);
        doc.add(new Paragraph(empresa, sub));
        doc.add(new Paragraph("Conta: " + conta, sub));
        doc.add(new Paragraph("Período: " + ini + " a " + fim, sub));
        doc.add(new Paragraph(" "));

        PdfPTable tab = new PdfPTable(6);
        tab.setWidthPercentage(100);
        tab.setWidths(new float[]{2f, 5f, 2f, 2.5f, 2.5f, 2.5f});

        // ✅ CORREÇÃO: Cor branca no Font
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Font.NORMAL, Color.WHITE);
        Color headerBg = new Color(52, 73, 94);

        String[] headers = {"Data", "Histórico", "Doc", "Débito", "Crédito", "Saldo"};
        for (String h : headers) {
            PdfPCell c = new PdfPCell(new Phrase(h, headerFont));
            c.setBackgroundColor(headerBg);
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setPadding(5);
            tab.addCell(c);
        }

        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
        for (var it : itens) {
            tab.addCell(new PdfPCell(new Phrase(it.data().toString(), cellFont)));
            tab.addCell(new PdfPCell(new Phrase(it.historico(), cellFont)));
            tab.addCell(new PdfPCell(new Phrase(it.documento() != null ? it.documento() : "", cellFont)));
            tab.addCell(new PdfPCell(new Phrase(it.debito().signum() > 0 ? fmt(it.debito()) : "", cellFont)));
            tab.addCell(new PdfPCell(new Phrase(it.credito().signum() > 0 ? fmt(it.credito()) : "", cellFont)));
            tab.addCell(new PdfPCell(new Phrase(fmt(it.saldo()), cellFont)));
        }

        doc.add(tab);
        doc.close();
        return out.toByteArray();
    }

    private String fmt(BigDecimal v) {
        if (v == null) return "0,00";
        return String.format("%15,.2f", v).replace(',', 'X').replace('.', ',').replace('X', '.');
    }
}