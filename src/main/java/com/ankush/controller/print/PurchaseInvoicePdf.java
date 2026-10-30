package com.ankush.controller.print;

import com.ankush.data.entities.PurchaseInvoice;
import com.ankush.data.entities.PurchaseParty;
import com.ankush.data.entities.PurchaseTransaction;
import com.ankush.data.entities.ShopeeInfo;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Purchase invoice as an A4 PDF: shop header, supplier and invoice details, all lines (the table header is
 * repeated on every page), charges and totals, and "Page x of y" on every page.
 * Item names, units and the shop name are in the Kiran font encoding used everywhere in the app.
 */
public class PurchaseInvoicePdf {
    private static final String KIRAN = PrintBill.fontname;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final Font shopFont = FontFactory.getFont(KIRAN, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 24f, Font.BOLD);
    private final Font shopSubFont = FontFactory.getFont(KIRAN, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 14f, Font.NORMAL);
    private final Font itemFont = FontFactory.getFont(KIRAN, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 14f, Font.NORMAL);
    private final Font titleFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
    private final Font labelFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.DARK_GRAY);
    private final Font textFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
    private final Font boldFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
    private final Font headFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.WHITE);
    private final Font grandFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private static final BaseColor HEAD_BG = new BaseColor(0x28, 0x35, 0x93);
    private static final BaseColor STRIPE = new BaseColor(0xF2, 0xF4, 0xFA);

    /** D:\Shopee\PurchaseInvoices where the shop keeps its bill PDFs, otherwise the user's home folder. */
    public static File defaultFolder() {
        File shopee = new File("D:\\Shopee");
        File base = shopee.isDirectory() ? shopee : new File(System.getProperty("user.home"), "SuperShope");
        return new File(base, "PurchaseInvoices");
    }

    /** Writes the invoice into {@code folder} and returns the file. */
    public File create(PurchaseInvoice invoice, ShopeeInfo shop, File folder) throws IOException, DocumentException {
        if (!folder.isDirectory() && !folder.mkdirs()) throw new IOException("Can't create folder " + folder);
        String no = invoice.getPartyinvoiceno() == null ? "" : invoice.getPartyinvoiceno().replaceAll("[^A-Za-z0-9_-]", "_");
        String base = "PurchaseInvoice_" + invoice.getId() + (no.isEmpty() ? "" : "_" + no);
        // one file per invoice, replaced on every print. If the old PDF is open in a viewer that locks it,
        // a single "_copy" file is used (also replaced), so printing never piles up files.
        File file = new File(folder, base + ".pdf");
        if (!replaceable(file)) {
            file = new File(folder, base + "_copy.pdf");
            if (!replaceable(file))
                throw new IOException("The invoice PDF is open in the PDF viewer. Close it and print again.");
        }
        Document doc = new Document(PageSize.A4, 36, 36, 36, 50);
        try (OutputStream out = new FileOutputStream(file)) {
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            writer.setPageEvent(new Footer());
            doc.addTitle("Purchase Invoice " + invoice.getPartyinvoiceno());
            doc.open();
            addHeader(doc, shop);
            addDetails(doc, invoice);
            addLines(doc, invoice.getTransactions());
            addTotals(doc, invoice);
            doc.close();
        }
        return file;
    }

    private void addHeader(Document doc, ShopeeInfo shop) throws DocumentException {
        if (shop != null && notBlank(shop.getShopeeName())) {
            Paragraph name = new Paragraph(shop.getShopeeName(), shopFont);
            name.setAlignment(Element.ALIGN_CENTER);
            doc.add(name);
            if (notBlank(shop.getAddress())) {
                Paragraph address = new Paragraph(shop.getAddress(), shopSubFont);
                address.setAlignment(Element.ALIGN_CENTER);
                doc.add(address);
            }
            if (notBlank(shop.getShopeeContact())) {
                Paragraph contact = new Paragraph("maao.naM " + shop.getShopeeContact(), shopSubFont);
                contact.setAlignment(Element.ALIGN_CENTER);
                doc.add(contact);
            }
        }
        Paragraph title = new Paragraph("PURCHASE INVOICE", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(6);
        title.setSpacingAfter(8);
        doc.add(title);
    }

    private void addDetails(Document doc, PurchaseInvoice invoice) throws DocumentException {
        PdfPTable t = new PdfPTable(new float[]{1.3f, 1f});
        t.setWidthPercentage(100);
        t.setSpacingAfter(10);

        PdfPCell supplier = box();
        supplier.addElement(new Paragraph("SUPPLIER", labelFont));
        PurchaseParty party = invoice.getPurchaseParty();
        supplier.addElement(new Paragraph(party == null ? "-" : party.getName(), boldFont));
        if (party != null && notBlank(party.getAddress())) supplier.addElement(new Paragraph(party.getAddress(), textFont));
        if (party != null && notBlank(party.getGst())) supplier.addElement(new Paragraph("GST: " + party.getGst(), textFont));
        if (party != null && notBlank(party.getPan())) supplier.addElement(new Paragraph("PAN: " + party.getPan(), textFont));
        t.addCell(supplier);

        PdfPCell info = box();
        info.addElement(pair("Invoice No", invoice.getPartyinvoiceno()));
        info.addElement(pair("Entry No", String.valueOf(invoice.getId())));
        info.addElement(pair("Date", invoice.getDate() == null ? "-" : invoice.getDate().format(DATE)));
        info.addElement(pair("Paid From", invoice.getBank() == null ? "-" : invoice.getBank().getBankname()));
        t.addCell(info);
        doc.add(t);
    }

    private void addLines(Document doc, List<PurchaseTransaction> lines) throws DocumentException {
        PdfPTable t = new PdfPTable(new float[]{0.5f, 1.6f, 3.6f, 0.8f, 0.8f, 1f, 1f, 1.3f});
        t.setWidthPercentage(100);
        t.setHeaderRows(1);
        for (String h : new String[]{"Sr", "Barcode", "Item Name", "Qty", "Unit", "Rate", "MRP", "Amount"}) {
            PdfPCell c = new PdfPCell(new Phrase(h, headFont));
            c.setBackgroundColor(HEAD_BG);
            c.setPadding(5);
            c.setHorizontalAlignment(h.equals("Item Name") || h.equals("Barcode") ? Element.ALIGN_LEFT : Element.ALIGN_CENTER);
            t.addCell(c);
        }
        int sr = 0;
        if (lines != null) {
            for (PurchaseTransaction tr : lines) {
                sr++;
                BaseColor bg = sr % 2 == 0 ? STRIPE : BaseColor.WHITE;
                t.addCell(cell(String.valueOf(sr), textFont, Element.ALIGN_CENTER, bg));
                t.addCell(cell(tr.getBarcode() == null ? "" : tr.getBarcode(), textFont, Element.ALIGN_LEFT, bg));
                t.addCell(cell(tr.getItemname(), itemFont, Element.ALIGN_LEFT, bg));
                t.addCell(cell(qty(tr.getQty()), textFont, Element.ALIGN_RIGHT, bg));
                t.addCell(cell(tr.getUnit() == null ? "" : tr.getUnit(), itemFont, Element.ALIGN_CENTER, bg));
                t.addCell(cell(money(tr.getPrice()), textFont, Element.ALIGN_RIGHT, bg));
                t.addCell(cell(money(tr.getMrp()), textFont, Element.ALIGN_RIGHT, bg));
                t.addCell(cell(money(tr.getAmount()), textFont, Element.ALIGN_RIGHT, bg));
            }
        }
        doc.add(t);
    }

    private void addTotals(Document doc, PurchaseInvoice invoice) throws DocumentException {
        PdfPTable outer = new PdfPTable(new float[]{1.2f, 1f});
        outer.setWidthPercentage(100);
        outer.setSpacingBefore(10);
        outer.setKeepTogether(true);

        int count = invoice.getTransactions() == null ? 0 : invoice.getTransactions().size();
        PdfPCell left = new PdfPCell();
        left.setBorder(Rectangle.NO_BORDER);
        left.addElement(new Paragraph("Total items: " + count, textFont));
        outer.addCell(left);

        PdfPTable t = new PdfPTable(new float[]{1.4f, 1f});
        t.setWidthPercentage(100);
        totalRow(t, "Net Total", money(invoice.getNettotal()), textFont);
        totalRow(t, "Transporting", money(invoice.getTransporting()), textFont);
        totalRow(t, "Wages", money(invoice.getWages()), textFont);
        totalRow(t, "Other Charges", money(invoice.getOther()), textFont);
        totalRow(t, "Discount", "- " + money(invoice.getDiscount()), textFont);
        totalRow(t, "Grand Total", money(invoice.getGrandtotal()), grandFont);
        totalRow(t, "Paid", money(invoice.getPaid()), textFont);
        totalRow(t, "Balance", money(invoice.getGrandtotal() - invoice.getPaid()), boldFont);
        PdfPCell right = new PdfPCell(t);
        right.setBorder(Rectangle.NO_BORDER);
        outer.addCell(right);
        doc.add(outer);
    }

    private void totalRow(PdfPTable t, String label, String value, Font font) {
        PdfPCell l = new PdfPCell(new Phrase(label, font));
        PdfPCell v = new PdfPCell(new Phrase(value, font));
        for (PdfPCell c : new PdfPCell[]{l, v}) {
            c.setPadding(4);
            c.setBorderColor(BaseColor.LIGHT_GRAY);
            if (font == grandFont) c.setBackgroundColor(STRIPE);
        }
        v.setHorizontalAlignment(Element.ALIGN_RIGHT);
        t.addCell(l);
        t.addCell(v);
    }

    private PdfPCell cell(String text, Font font, int align, BaseColor bg) {
        PdfPCell c = new PdfPCell(new Phrase(text == null ? "" : text, font));
        c.setHorizontalAlignment(align);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setPaddingTop(2);
        c.setPaddingBottom(4);
        c.setBackgroundColor(bg);
        c.setBorderColor(BaseColor.LIGHT_GRAY);
        return c;
    }

    private PdfPCell box() {
        PdfPCell c = new PdfPCell();
        c.setPadding(6);
        c.setBorderColor(BaseColor.LIGHT_GRAY);
        return c;
    }

    private Paragraph pair(String label, String value) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + ":  ", labelFont));
        p.add(new Chunk(value == null ? "-" : value, boldFont));
        return p;
    }

    /** True when the file doesn't exist or could be removed (a PDF open in a locking viewer can't be). */
    private static boolean replaceable(File file) {
        return !file.exists() || file.delete();
    }

    private static boolean notBlank(String s) { return s != null && !s.isBlank(); }

    private static String money(Float v) { return String.format(Locale.ROOT, "%.2f", v == null ? 0f : v); }

    private static String money(float v) { return String.format(Locale.ROOT, "%.2f", v); }

    private static String qty(Float v) {
        if (v == null) return "";
        return v == Math.floor(v) ? String.valueOf(v.longValue()) : String.format(Locale.ROOT, "%.3f", v).replaceAll("0+$", "");
    }

    /** "Page x of y", print time and software credit at the bottom of every page. */
    private static class Footer extends PdfPageEventHelper {
        private final Font font = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, BaseColor.GRAY);
        private PdfTemplate total;
        private final String printed = "Printed " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a"));

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            total = writer.getDirectContent().createTemplate(30, 12);
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            float y = document.bottom() - 20;
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase(printed, font), document.left(), y, 0);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    new Phrase("Software by Ankush Supnar (8329394603)", font), (document.left() + document.right()) / 2, y, 0);
            // "Page x of" right-aligned (a trailing space would be dropped), the total page count after a gap
            String page = "Page " + writer.getPageNumber() + " of";
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, new Phrase(page, font), document.right() - 20, y, 0);
            cb.addTemplate(total, document.right() - 17, y);
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            ColumnText.showTextAligned(total, Element.ALIGN_LEFT,
                    new Phrase(String.valueOf(writer.getPageNumber()), font), 0, 0, 0);
        }
    }
}
