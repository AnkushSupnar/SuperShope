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
    public static final String fontname = getFontPath();
    Font f1 = FontFactory.getFont(fontname, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 30f, Font.BOLD);
    Font f2 = FontFactory.getFont(fontname, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 14f, Font.NORMAL, BaseColor.BLUE);
    Font f3 = FontFactory.getFont(fontname, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 16f, Font.BOLD, BaseColor.BLACK);
    Font f4 = FontFactory.getFont(fontname, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 12f, Font.BOLD, BaseColor.BLACK);
    Font f5 = FontFactory.getFont(fontname, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 18f, Font.BOLD, BaseColor.BLACK);

    private static String getFontPath() {
        try {
            java.net.URL fontUrl = PrintBill.class.getResource("/fonts/kiran.ttf");
            if (fontUrl != null) {
                return fontUrl.toExternalForm();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "D:\\Shopee\\kiran.ttf";
    }
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

    // Thermal-printer page width (~80mm in PDF points).
    private static final float PAGE_WIDTH = 230f;
    private static final float LEFT_MARGIN = -1f;
    private static final float RIGHT_MARGIN = 0f;
    private static final float TOP_MARGIN = 15f;
    private static final float BOTTOM_MARGIN = 25f;

    // Largest page PDF allows (200 inches); the measuring pass lays the bill out on a page this tall
    private static final float MAX_PAGE_HEIGHT = 14400f;

    /**
     * Writes the bill as one page exactly as tall as its content. The content is laid out once on a
     * very tall scratch page to measure the height it really uses (long Marathi names wrap, so a
     * per-row estimate is never right), then written again at that height.
     */
    public void createDoc() {
        try {
            int itemCount = (bill == null || bill.getTransactions() == null) ? 0 : bill.getTransactions().size();
            float contentHeight = measureContentHeight();
            float pageHeight = Math.min(MAX_PAGE_HEIGHT, TOP_MARGIN + contentHeight + BOTTOM_MARGIN);

            doc = new Document(new Rectangle(PAGE_WIDTH, pageHeight), LEFT_MARGIN, RIGHT_MARGIN, TOP_MARGIN, BOTTOM_MARGIN);
            try (OutputStream out = new FileOutputStream(new File(fileName))) {
                PdfWriter.getInstance(doc, out);
                doc.open();
                renderContent();
                doc.close();
            }
            System.out.println("Print Done; items=" + itemCount + " pageHeight=" + pageHeight);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private float measureContentHeight() throws DocumentException {
        doc = new Document(new Rectangle(PAGE_WIDTH, MAX_PAGE_HEIGHT), LEFT_MARGIN, RIGHT_MARGIN, TOP_MARGIN, BOTTOM_MARGIN);
        PdfWriter writer = PdfWriter.getInstance(doc, new java.io.ByteArrayOutputStream());
        doc.open();
        renderContent();
        float used = (MAX_PAGE_HEIGHT - TOP_MARGIN) - writer.getVerticalPosition(true);
        doc.close();
        return used;
    }

    private void renderContent() throws DocumentException {
        addHeader();
        Paragraph p = new Paragraph();
        p.add(addData());
        doc.add(p);
        p = new Paragraph("         kRpayaa Aapalyaa vastau ibalaa pa`maaNao maaojauna Gyaavyaata.", f4);
        p.setLeading(10);
        doc.add(p);
        p = new Paragraph("         AapaNa Aalaata AanaMd vaaTlaa paunha yaa!", f4);
        p.setLeading(10);
        doc.add(p);
        p = new Paragraph("     SoftwareBy Ankush Supnar(8329394603)", small);
        p.setLeading(10);
        doc.add(p);
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
