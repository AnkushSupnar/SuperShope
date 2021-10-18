package com.ankush.controller.print;

import com.ankush.data.entities.Bill;
import com.ankush.data.entities.Transaction;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
@Component
public class PrintBill {

    Document doc;
    String fileName= "D:\\Shopee\\bill.pdf";
    public static final String fontname = "D:\\Shopee\\kiran.ttf";
    //public static java.awt.Font fontname = new java.awt.Font("/fxm/font/kiran.ttf");
    Font f1 = FontFactory.getFont(fontname, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 30f, Font.BOLD, BaseColor.BLACK);
    Font f2 = FontFactory.getFont(fontname, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 16f, Font.BOLD, BaseColor.BLACK);
    Font f3 = FontFactory.getFont(fontname, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 16f, Font.BOLD, BaseColor.BLACK);
    private static Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL);
    Bill bill;

    public void setBill(Bill bill) {
        this.bill = bill;
    }

    public PrintBill()
    {

    }
    public void createDoc()
    {
        //this.bill=bill;
        float left = 30;
        float right = 0;
        float top = 20;
        float bottom = 0;
        try {
            createDocument(addData());
            doc.close();
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    void createDocument(PdfPTable table)
    {
        try {
        //Rectangle pagesize = new Rectangle(216f, 400f+table.getTotalHeight());
            System.out.println("table Height=="+table.getTotalHeight());
            Rectangle pagesize = new Rectangle(216f, 400f+table.getTotalHeight());
         doc = new Document(pagesize, 3f, 3f, 20f, 180f);
        OutputStream out = new FileOutputStream(new File(fileName));
        PdfWriter.getInstance(doc,out);
        doc.open();

        addHeader();
            Paragraph p = new Paragraph();
            p.setLeading(25);
            p.setIndentationLeft(1);
            p.add(table);

          doc.add(new Paragraph("..................."));
            doc.add(p);

        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void addHeader() throws DocumentException {
        Paragraph p = new Paragraph("      laXmaI saupar maako-T",f1);
        p.setLeading(5);
        doc.add(p);
        p = new Paragraph("     mau.paao :AMmaLnaor, taa.naovaasaa,ija.Ahmadnagar",f2);
        //p.setLeading(10);
        doc.add(p);

        p = new Paragraph("         maao.naM 8328394603,9960855742 ",f2);
        p.setLeading(12);
        doc.add(p);
    }

    private PdfPTable addData() throws DocumentException {
        PdfPTable table = new PdfPTable(6);
        table.setTotalWidth(new float[]{40,150,50,50,50,50 });
        PdfPCell c1 = new PdfPCell(new Paragraph("A.k` ",f3));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        //c1.setBorder(0);
        //c1.setBorder(PdfPCell.NO_BORDER);
        table.addCell(c1);

        c1 = new PdfPCell(new Paragraph("maalaacao naava",f3));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        //c1.setBorder(PdfPCell.NO_BORDER);
        table.addCell(c1);

        c1 = new PdfPCell(new Paragraph("naga",f3));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);

        table.addCell(c1);

        c1 = new PdfPCell(new Paragraph("Baava",f3));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);

        c1 = new PdfPCell(new Paragraph("dr",f3));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);

        c1 = new PdfPCell(new Paragraph("r@kma",f3));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);
        //transaction data
        int sr=0;
        for(Transaction tr:bill.getTransactions())
        {
            c1 = new PdfPCell(new Paragraph(String.valueOf((++sr)),f3));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            c1.setBorder(PdfPCell.NO_BORDER);
            table.addCell(c1);

            c1 = new PdfPCell(new Paragraph(tr.getItemname(),f3));
            c1.setVerticalAlignment(Element.ALIGN_LEFT);
            c1.setBorder(PdfPCell.NO_BORDER);
            table.addCell(c1);

            c1 = new PdfPCell(new Paragraph(String.valueOf(tr.getQuantity()),f3));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            c1.setBorder(PdfPCell.NO_BORDER);
            table.addCell(c1);

            c1 = new PdfPCell(new Paragraph(String.valueOf(tr.getRate()),f3));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            c1.setBorder(PdfPCell.NO_BORDER);
            table.addCell(c1);

            c1 = new PdfPCell(new Paragraph(String.valueOf(tr.getSailingprice()),f3));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            c1.setBorder(PdfPCell.NO_BORDER);
            table.addCell(c1);

            c1 = new PdfPCell(new Paragraph(String.valueOf(tr.getAmount()),f3));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            c1.setBorder(PdfPCell.NO_BORDER);
            table.addCell(c1);
        }






        return table;



    }

//    public static void main(String[] args) {
//        new PrintBill(new Bill());
//    }
}
