package com.ankush.controller.print;

import com.ankush.data.entities.Bill;
import com.ankush.data.entities.SalesReturn;
import com.ankush.data.entities.SalesReturnTransaction;
import com.ankush.data.entities.ShopeeInfo;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Generates a printable sales-return receipt PDF.  Layout mirrors {@link PrintBill}
 * so receipts look familiar, with a clearly highlighted "SALES RETURN" banner and
 * the original bill number for reference.
 */
@Component
public class PrintSalesReturn {

    public static final String DEFAULT_FILE_NAME = "D:\\Shopee\\salesreturn.pdf";
    private static final String fontname = getFontPath();

    private final Font f1 = FontFactory.getFont(fontname, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 30f, Font.BOLD);
    private final Font f2 = FontFactory.getFont(fontname, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 14f, Font.NORMAL, BaseColor.BLUE);
    private final Font f4 = FontFactory.getFont(fontname, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 12f, Font.BOLD, BaseColor.BLACK);
    private final Font f5 = FontFactory.getFont(fontname, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 18f, Font.BOLD, BaseColor.BLACK);

    private static final Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL);
    private static final Font small = new Font(Font.FontFamily.COURIER, 8, Font.BOLD);
    private static final Font extrasmall = new Font(Font.FontFamily.COURIER, 3, Font.BOLD);
    private static final Font returnBanner = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.RED);
    private static final Font refundFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.RED);

    private Document doc;

    private static String getFontPath() {
        try {
            java.net.URL fontUrl = PrintSalesReturn.class.getResource("/fonts/kiran.ttf");
            if (fontUrl != null) return fontUrl.toExternalForm();
        } catch (Exception ignored) {}
        return "D:\\Shopee\\kiran.ttf";
    }

    /**
     * Generates the PDF for the given SalesReturn at the default path and returns it.
     */
    public String generate(SalesReturn ret, ShopeeInfo shopee) throws DocumentException, java.io.IOException {
        return generate(ret, shopee, DEFAULT_FILE_NAME);
    }

    // Thermal-printer page width (~80mm in PDF points).
    private static final float PAGE_WIDTH = 230f;
    private static final float LEFT_MARGIN = -1f;
    private static final float RIGHT_MARGIN = 0f;
    private static final float TOP_MARGIN = 15f;
    private static final float BOTTOM_MARGIN = 25f;

    // Deterministic page-height model:
    //   pageHeight = TOP + HEADER (fixed) + ITEMS (variable) + FOOTER (fixed) + BOTTOM
    // Header includes shop name/address/contact + "SALES RETURN" banner + meta
    // block (return-no, against-bill, customer).  Footer includes refund total
    // table, reason line, and the two trailing notes.
    private static final float HEADER_HEIGHT = 290f;       // a bit taller than bill due to red banner + extra meta row
    private static final float ITEMS_TABLE_CHROME = 80f;   // header row + nested total row
    private static final float ROW_HEIGHT = 24f;           // per item, with slack for Marathi wrap
    private static final float FOOTER_HEIGHT = 110f;       // refund table + reason line + 2 closing paragraphs
    private static final float SAFETY_PAD = 20f;

    public String generate(SalesReturn ret, ShopeeInfo shopee, String filePath) throws DocumentException, java.io.IOException {
        File outFile = new File(filePath);
        File parent = outFile.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();

        int itemCount = (ret == null || ret.getTransactions() == null) ? 0 : ret.getTransactions().size();
        float itemsHeight = ITEMS_TABLE_CHROME + (itemCount * ROW_HEIGHT);
        float pageHeight = TOP_MARGIN + HEADER_HEIGHT + itemsHeight + FOOTER_HEIGHT + BOTTOM_MARGIN + SAFETY_PAD;

        Rectangle pagesize = new Rectangle(PAGE_WIDTH, pageHeight);
        doc = new Document(pagesize, LEFT_MARGIN, RIGHT_MARGIN, TOP_MARGIN, BOTTOM_MARGIN);
        OutputStream out = new FileOutputStream(outFile);
        PdfWriter.getInstance(doc, out);
        doc.open();
        renderContent(ret, shopee);
        doc.close();
        System.out.println("SalesReturn print: items=" + itemCount + " pageHeight=" + pageHeight);
        return filePath;
    }

    private void renderContent(SalesReturn ret, ShopeeInfo shopee) throws DocumentException {
        addHeader(ret, shopee);

        Paragraph p = new Paragraph();
        p.add(buildItemsTable(ret));
        doc.add(p);

        // refund total
        PdfPTable refundTable = new PdfPTable(2);
        refundTable.setTotalWidth(new float[]{180, 100});
        PdfPCell c1 = new PdfPCell(new Paragraph("TOTAL REFUND", refundFont));
        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
        c1.setBorder(PdfPCell.TOP);
        refundTable.addCell(c1);
        c1 = new PdfPCell(new Paragraph(String.valueOf(ret.getRefundamount() == null ? 0f : ret.getRefundamount()), refundFont));
        c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c1.setBorder(PdfPCell.TOP);
        refundTable.addCell(c1);
        doc.add(refundTable);

        if (ret.getReason() != null && !ret.getReason().isBlank()) {
            p = new Paragraph("Reason: " + ret.getReason(), smallBold);
            p.setLeading(12);
            doc.add(p);
        }

        p = new Paragraph("         This is a sales-return receipt. Please keep for your records.", smallBold);
        p.setLeading(12);
        doc.add(p);
        p = new Paragraph("     SoftwareBy Ankush Supnar(8329394603)", small);
        p.setLeading(10);
        doc.add(p);
    }

    private void addHeader(SalesReturn ret, ShopeeInfo shopee) throws DocumentException {
        String shopName = "      laXmaI saupar maako-T";
        String shopContact = "               maao.naM 9822420872,9970192697 ";
        if (shopee != null) {
            if (shopee.getShopeeName() != null && !shopee.getShopeeName().isEmpty()) {
                shopName = "      " + shopee.getShopeeName();
            }
            if (shopee.getShopeeContact() != null && !shopee.getShopeeContact().isEmpty()) {
                shopContact = "               maao.naM " + shopee.getShopeeContact() + " ";
            }
        }

        Paragraph p = new Paragraph(shopName, f1);
        p.setLeading(4);
        doc.add(p);
        if (shopee != null && shopee.getAddress() != null && !shopee.getAddress().isEmpty()) {
            p = new Paragraph("             " + shopee.getAddress(), f2);
            p.setLeading(10);
            doc.add(p);
        }
        p = new Paragraph(shopContact, f2);
        p.setLeading(18);
        doc.add(p);

        // big red banner
        p = new Paragraph("            *** SALES RETURN ***", returnBanner);
        p.setLeading(20);
        doc.add(p);

        PdfPTable meta = new PdfPTable(2);
        meta.setTotalWidth(new float[]{140, 110});

        Bill orig = ret.getBill();
        Long origBillNo = orig == null ? null : orig.getBillno();

        PdfPCell c1 = new PdfPCell(new Paragraph("Return No: " + (ret.getId() == null ? "-" : ret.getId()), smallBold));
        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
        c1.setBorder(PdfPCell.TOP);
        meta.addCell(c1);

        c1 = new PdfPCell(new Paragraph("Date: " + ret.getDate(), smallBold));
        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
        c1.setBorder(PdfPCell.TOP);
        meta.addCell(c1);

        c1 = new PdfPCell(new Paragraph("Against Bill: " + (origBillNo == null ? "-" : origBillNo), smallBold));
        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
        c1.setBorder(0);
        meta.addCell(c1);

        c1 = new PdfPCell(new Paragraph("Time: " + LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm")), smallBold));
        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
        c1.setBorder(0);
        meta.addCell(c1);

        boolean cashCustomer = ret.getCustomer() != null
                && ret.getCustomer().getId() != null
                && ret.getCustomer().getId() == 1;
        if (!cashCustomer && ret.getCustomer() != null && ret.getCustomer().getName() != null && !ret.getCustomer().getName().isEmpty()) {
            c1 = new PdfPCell(new Paragraph("Name: " + ret.getCustomer().getName(), smallBold));
            c1.setBorder(0);
            c1.setColspan(2);
            meta.addCell(c1);

            if (ret.getCustomer().getContact() != null && !ret.getCustomer().getContact().isEmpty()) {
                c1 = new PdfPCell(new Paragraph("Mo: " + ret.getCustomer().getContact(), smallBold));
                c1.setBorder(PdfPCell.BOTTOM);
                c1.setColspan(2);
                meta.addCell(c1);
            } else {
                c1 = new PdfPCell(new Paragraph(" ", extrasmall));
                c1.setBorder(PdfPCell.BOTTOM);
                c1.setColspan(2);
                meta.addCell(c1);
            }
        } else {
            c1 = new PdfPCell(new Paragraph(" ", extrasmall));
            c1.setBorder(PdfPCell.BOTTOM);
            c1.setColspan(2);
            meta.addCell(c1);
        }

        p = new Paragraph("        ", extrasmall);
        doc.add(p);
        doc.add(meta);
    }

    private PdfPTable buildItemsTable(SalesReturn ret) throws DocumentException {
        PdfPTable table = new PdfPTable(5);
        table.setTotalWidth(new float[]{35, 140, 60, 70, 60});

        PdfPCell c1 = new PdfPCell(new Paragraph("k`", f4));
        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
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

        c1 = new PdfPCell(new Paragraph("dr", f4));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        c1.setBorder(PdfPCell.BOTTOM);
        table.addCell(c1);

        c1 = new PdfPCell(new Paragraph("ekuNa", f4));
        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
        c1.setBorder(PdfPCell.BOTTOM);
        table.addCell(c1);

        int sr = 0;
        if (ret.getTransactions() != null) {
            for (SalesReturnTransaction t : ret.getTransactions()) {
                c1 = new PdfPCell(new Paragraph(String.valueOf(++sr), f4));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setBorder(PdfPCell.NO_BORDER);
                table.addCell(c1);

                c1 = new PdfPCell(new Paragraph(t.getItemname() == null ? "" : t.getItemname(), f4));
                c1.setBorder(PdfPCell.NO_BORDER);
                table.addCell(c1);

                c1 = new PdfPCell(new Paragraph(String.valueOf(t.getQuantity()), f4));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setBorder(PdfPCell.NO_BORDER);
                table.addCell(c1);

                c1 = new PdfPCell(new Paragraph(String.valueOf(t.getRate()), f4));
                c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                c1.setBorder(PdfPCell.NO_BORDER);
                table.addCell(c1);

                c1 = new PdfPCell(new Paragraph(String.valueOf(t.getAmount()), f4));
                c1.setHorizontalAlignment(Element.ALIGN_LEFT);
                c1.setBorder(PdfPCell.NO_BORDER);
                table.addCell(c1);
            }
        }

        // sub-total inside the items table footer
        PdfPTable footer = new PdfPTable(1);
        footer.setTotalWidth(new float[]{500});
        c1 = new PdfPCell(new Paragraph("ekuNa r@kma", f4));
        c1.setBorder(PdfPCell.NO_BORDER);
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        footer.addCell(c1);

        c1 = new PdfPCell(new Paragraph(String.valueOf(ret.getNettotal() == null ? 0f : ret.getNettotal()), f5));
        c1.setBorder(PdfPCell.BOX);
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        footer.addCell(c1);

        c1.setBorder(PdfPCell.TOP);
        c1.setColspan(5);
        c1.addElement(footer);
        table.addCell(c1);

        return table;
    }
}
