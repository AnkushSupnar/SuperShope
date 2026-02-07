package com.ankush.controller.print;

import com.ankush.data.entities.Bill;
import com.ankush.data.entities.ShopeeInfo;
import com.ankush.data.entities.Transaction;
import com.ankush.data.service.BillService;
import com.ankush.data.service.ShopeeInfoService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

//@Component
public class PrintBill {
    @Autowired
    BillService billservice;

    private ShopeeInfo shopeeInfo;

    Document doc;
    String fileName = "D:\\Shopee\\bill.pdf";
    public static final String fontname = "D:\\Shopee\\kiran.ttf";
    //public static java.awt.Font fontname = new java.awt.Font("/fxm/font/kiran.ttf");
    Font f1 = FontFactory.getFont(fontname, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 30f, Font.BOLD);//, BaseColor.BLACK);
    Font f2 = FontFactory.getFont(fontname, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 14f, Font.NORMAL, BaseColor.BLUE);
    Font f3 = FontFactory.getFont(fontname, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 16f, Font.BOLD, BaseColor.BLACK);
    Font f4 = FontFactory.getFont(fontname, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED, 12f, Font.BOLD, BaseColor.BLACK);
    Font f5 = FontFactory.getFont(fontname, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED, 18f, Font.BOLD, BaseColor.BLACK);
    private static Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL);
    private static Font small = new Font(Font.FontFamily.COURIER, 8, Font.BOLD);
    private static Font smallMedium = new Font(Font.FontFamily.COURIER, 10, Font.BOLD);
    private static Font extrasmall = new Font(Font.FontFamily.COURIER, 3, Font.BOLD);

    Bill bill;

    public void setBill(Bill bill) {
        this.bill = bill;
        createDoc();
   //  new PrintingExample("D:\\Shopee\\bill.pdf");
    }

    public PrintBill() {

    }

    public void setShopeeInfo(ShopeeInfo shopeeInfo) {
        this.shopeeInfo = shopeeInfo;
    }

    public void createDoc() {
        //this.bill=bill;
        float left = 0;
        float right = 0;
        float top = 20;
        float bottom = 0;
        try {
            createDocument(addData());
            doc.close();
            System.out.println("Print Done");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void createDocument(PdfPTable table) {
        try {
            //Rectangle pagesize = new Rectangle(216f, 400f+table.getTotalHeight());
            System.out.println("table Height==" + table.getTotalHeight());
            Rectangle pagesize = new Rectangle(230f, 10000f + table.getTotalHeight());
            doc = new Document(pagesize, -1f, 0f, 15f, 180f);
            OutputStream out = new FileOutputStream(new File(fileName));
            PdfWriter.getInstance(doc, out);
            doc.open();

            addHeader();
            Paragraph p = new Paragraph();
            //p.setLeading(25);
           // p.setIndentationLeft(1);
            p.add(table);

            //doc.add(new Paragraph("..................."));
             doc.add(p);
             p = new Paragraph("         kRpayaa Aapalyaa vastau ibalaa pa`maaNao maaojauna Gyaavyaata.",f4);
             p.setLeading(10);
            doc.add(p);
            p = new Paragraph("         AapaNa Aalaata AanaMd vaaTlaa paunha yaa!",f4);
            p.setLeading(10);
            doc.add(p);

            p = new Paragraph("     SoftwareBy Ankush Supnar(8329394603)",small);
            p.setLeading(10);
            doc.add(p);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addHeader() throws DocumentException {
        String shopName = "      laXmaI saupar maako-T";
        String shopContact = "               maao.naM 9822420872,9970192697 ";

        if (shopeeInfo != null) {
            if (shopeeInfo.getShopeeName() != null && !shopeeInfo.getShopeeName().isEmpty()) {
                shopName = "      " + shopeeInfo.getShopeeName();
            }
            if (shopeeInfo.getShopeeContact() != null && !shopeeInfo.getShopeeContact().isEmpty()) {
                shopContact = "               maao.naM " + shopeeInfo.getShopeeContact() + " ";
            }
        }

        Paragraph p = new Paragraph(shopName, f1);
        p.setLeading(4);
        doc.add(p);

        if (shopeeInfo != null && shopeeInfo.getAddress() != null && !shopeeInfo.getAddress().isEmpty()) {
            p = new Paragraph("             " + shopeeInfo.getAddress(), f2);
            p.setLeading(10);
            doc.add(p);
        }

        p = new Paragraph(shopContact, f2);
        p.setLeading(10);
        p.setLeading(18);
        doc.add(p);
        PdfPTable table = new PdfPTable(2);
        table.setTotalWidth(new float[]{150, 100});

        String cash = "Cash Bill";
        if (bill.getPaid() <= 0.0f) cash = "Credit Bill";

        PdfPCell c1 = new PdfPCell(new Paragraph(cash, smallBold));
        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
        //c1.setBorder(0);
        c1.setBorder(PdfPCell.TOP);
        table.addCell(c1);

        c1 = new PdfPCell(new Paragraph(String.valueOf("Bill No: "+bill.getBillno()), smallBold));
        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
        c1.setBorder(PdfPCell.TOP);
        table.addCell(c1);

        c1 = new PdfPCell(new Paragraph(String.valueOf("Date: "+bill.getDate()), smallBold));
        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
        //c1.setBorder(0);
        c1.setBorder(PdfPCell.TOP);
        table.addCell(c1);

        c1 = new PdfPCell(new Paragraph(String.valueOf("Time: "+ LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm"))), smallBold));
        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
        //c1.setBorder(0);
        c1.setBorder(PdfPCell.TOP);
        table.addCell(c1);

        if (bill.getCustomer() != null && bill.getCustomer().getName() != null
                && !bill.getCustomer().getName().isEmpty() && bill.getPaid() <= 0.0f) {
            c1 = new PdfPCell(new Paragraph("Name: " + bill.getCustomer().getName(), smallBold));
            c1.setBorder(0);
            c1.setColspan(2);
            table.addCell(c1);

            if (bill.getCustomer().getContact() != null && !bill.getCustomer().getContact().isEmpty()) {
                c1 = new PdfPCell(new Paragraph("Mo: " + bill.getCustomer().getContact(), smallBold));
                c1.setBorder(PdfPCell.BOTTOM);
                c1.setColspan(2);
                table.addCell(c1);
            }
        } else {
            c1 = new PdfPCell(new Paragraph(" ", extrasmall));
            c1.setBorder(PdfPCell.BOTTOM);
            c1.setColspan(2);
            table.addCell(c1);
        }

        p = new Paragraph("        ",extrasmall);
        doc.add(p);
        doc.add(table);

    }

    private PdfPTable addData() throws DocumentException {

        PdfPTable table = new PdfPTable(5);
        table.setTotalWidth(new float[]{35, 140, 60,  70, 60});
        PdfPCell c1 = new PdfPCell(new Paragraph("k` ", f4));
        c1.setHorizontalAlignment(Element.ALIGN_LEFT);

        //c1.setBorder(0);
        c1.setBorder(PdfPCell.BOTTOM);
        table.addCell(c1);

        c1 = new PdfPCell(new Paragraph("maalaacao naava", f4));
        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
        c1.setBorder(PdfPCell.BOTTOM);
        table.addCell(c1);

        c1 = new PdfPCell(new Paragraph("naga", f4));
        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
        c1.setBorder(PdfPCell.BOTTOM);
        table.addCell(c1);

        c1 = new PdfPCell(new Paragraph("MRP", smallMedium));
        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
        c1.setBorder(PdfPCell.BOTTOM);
       // table.addCell(c1);

        c1 = new PdfPCell(new Paragraph("dr", f4));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        c1.setBorder(PdfPCell.BOTTOM);
        table.addCell(c1);

        c1 = new PdfPCell(new Paragraph("ekuNa", f4));
        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
        c1.setBorder(PdfPCell.BOTTOM);
        table.addCell(c1);
        //transaction data
        int sr = 0;
        float mrp=0.0f,totalmrp=0.0f;
        if (bill.getTransactions() == null) {
            bill.setTransactions(new java.util.ArrayList<>());
        }
        for (Transaction tr : bill.getTransactions()) {
            mrp += tr.getRate();
            totalmrp+=(tr.getQuantity()*tr.getRate());
            c1 = new PdfPCell(new Paragraph(String.valueOf((++sr)), f4));
            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
            c1.setBorder(PdfPCell.NO_BORDER);
            table.addCell(c1);

            c1 = new PdfPCell(new Paragraph(tr.getItemname(), f4));
            c1.setVerticalAlignment(Element.ALIGN_LEFT);
            c1.setBorder(PdfPCell.NO_BORDER);
            table.addCell(c1);

            c1 = new PdfPCell(new Paragraph(String.valueOf(tr.getQuantity()), f4));
            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
            c1.setBorder(PdfPCell.NO_BORDER);
            table.addCell(c1);

            c1 = new PdfPCell(new Paragraph(String.valueOf(tr.getRate()), f4));
            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
            c1.setBorder(PdfPCell.NO_BORDER);
           // table.addCell(c1);

            c1 = new PdfPCell(new Paragraph(String.valueOf(tr.getSailingprice()), f4));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            c1.setBorder(PdfPCell.NO_BORDER);
            table.addCell(c1);

            c1 = new PdfPCell(new Paragraph(String.valueOf(tr.getAmount()), f4));
            c1.setHorizontalAlignment(Element.ALIGN_LEFT);
            c1.setBorder(PdfPCell.NO_BORDER);
            table.addCell(c1);
        }
        //addFooter
        //PdfPTable footer = new PdfPTable(3);
        PdfPTable footer = new PdfPTable(1);
        //footer.setTotalWidth(new float[]{500, 500, 500});
        footer.setTotalWidth(new float[]{500});

        c1 = new PdfPCell(new Paragraph("ikMmata ", f4));
        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
        c1.setBorder(PdfPCell.NO_BORDER);
      //  footer.addCell(c1);

        c1 = new PdfPCell(new Paragraph("ekUNa bacata", f4));
        c1.setBorder(PdfPCell.NO_BORDER);
        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
        //footer.addCell(c1);

        c1 = new PdfPCell(new Paragraph("ekuNa r@kma", f4));
        c1.setBorder(PdfPCell.NO_BORDER);
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        footer.addCell(c1);

        c1 = new PdfPCell(new Paragraph(""+totalmrp, f5));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        c1.setBorder(PdfPCell.BOX);
       // footer.addCell(c1);

        if(bill.getDiscount()<0)
            bill.setDiscount(0.0f);
        c1 = new PdfPCell(new Paragraph(""+(bill.getDiscount()), f5));
        c1.setBorder(PdfPCell.BOX);
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
       // footer.addCell(c1);

        c1 = new PdfPCell(new Paragraph(""+bill.getGrandtotal(), f5));
        c1.setBorder(PdfPCell.BOX);
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        footer.addCell(c1);

        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
        c1.setBorder(PdfPCell.TOP);
        c1.setColspan(6);
        c1.addElement(footer);
        table.addCell(c1);
        return table;


    }

}
