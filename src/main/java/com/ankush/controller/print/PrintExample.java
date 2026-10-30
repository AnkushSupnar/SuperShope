package com.ankush.controller.print;

import ch.qos.logback.classic.BasicConfigurator;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.printing.PDFPageable;
import org.apache.pdfbox.printing.PDFPrintable;
import org.apache.pdfbox.printing.Scaling;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Destination;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.MediaTray;
import javax.print.attribute.standard.PageRanges;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PrintExample
{
    /** Printer width value meaning "print through the Windows driver" (A4 / PDF printers). */
    public static final int DRIVER_PRINTING = 0;
    /** Printer width value meaning "decide from the printer name". */
    public static final int AUTO_WIDTH = -1;

    // Set from App Settings (AppSettingsService); the bill screens create PrintExample without Spring
    private static volatile String billPrinterName = "";
    private static volatile int billPrinterDots = AUTO_WIDTH;

    public static void configure(String printerName, int printerDots) {
        billPrinterName = printerName == null ? "" : printerName.trim();
        billPrinterDots = printerDots;
    }

    public PrintExample(String filePath)
    {
        try
        {

           // Properties prop = CommonMethods.loadPropertiesFile();
           // changeWindowsDefaultPrinter(prop.getProperty("Bill.printer"));
            // The chosen bill printer, not the Windows default: Windows 10/11 moves the default to the
            // last printer used, which sent bills to the wrong printer
            PrintService service = billPrinterName.isEmpty() ? null : findPrintService(billPrinterName);
            if (service == null) service = PrintServiceLookup.lookupDefaultPrintService();
            int dots = resolveDots(service, billPrinterDots);
            if (dots > 0) {
                EscPosPrinter.print(filePath, service, dots, null);
            } else {
                print(filePath, service, null);
            }

//            PDDocument doc = PDDocument.load(new File(filePath));
//            PDFPrintable printable = new PDFPrintable(doc,Scaling.ACTUAL_SIZE);
//            PrinterJob job = PrinterJob.getPrinterJob();
//            job.setPrintable(printable);
//            job.print();

//            PDDocument doc = PDDocument.load(new File(filePath));
//            PrinterJob job = PrinterJob.getPrinterJob();
//
//// define custom paper
//            Paper paper = new Paper();
//            paper.setSize(1306, 1396); // 1/72 inch
//            paper.setImageableArea(0, 0, paper.getWidth(), paper.getHeight()); // no margins
//
//// custom page format
//            PageFormat pageFormat = new PageFormat();
//            pageFormat.setPaper(paper);
//
//// override the page format
//            Book book = new Book();
//// append all pages
//            book.append(new PDFPrintable(doc, Scaling.SHRINK_TO_FIT), pageFormat, doc.getNumberOfPages());
//            job.setPageable(book);
//
//            job.print();

        }catch(Exception e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Prints the one-page bill PDF on a thermal roll printer as a single job with one cut at the end.
     * The receipt drivers' default form is 80 x 297 mm, which cut long bills off. So:
     *  - pick the driver's long roll form (e.g. "Roll Paper 80 x 3276 mm") so the whole bill is one page;
     *  - if the bill is longer than any form, split it between lines and use the driver's
     *    "Document[Feed,Cut]" mode so the pages come out as one strip, cut only after the last one.
     * @param toFile when not null, the driver's output is written to this file instead of the printer
     */
    static void print(String filePath, PrintService service, File toFile) throws IOException, PrinterException {
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            PDPage firstPage = document.getPage(0);
            float pdfWidth = firstPage.getMediaBox().getWidth();
            float pdfHeight = firstPage.getMediaBox().getHeight();

            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPrintService(service);

            PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
            MediaSizeName roll = findRollForm(service, pdfWidth, pdfHeight);
            if (roll != null) attributes.add(roll);
            Media documentTray = findDocumentTray(service);
            if (documentTray != null) attributes.add(documentTray);
            if (toFile != null) attributes.add(new Destination(toFile.toURI()));

            PageFormat pageFormat = roll != null ? job.getPageFormat(attributes) : longestPage(job, pdfWidth, pdfHeight);
            BillSlices slices = new BillSlices(document, pdfWidth, pdfHeight, pageFormat);
            Book book = new Book();
            book.append(slices, pageFormat, slices.count);
            job.setPageable(book);

            System.out.println("Printing bill on " + service.getName() + ": height=" + pdfHeight + " form=" + roll
                    + " pages=" + slices.count + " tray=" + documentTray);
            job.print(attributes);
        }
    }

    /** A page as long as the bill, as far as the driver allows (its printable height is capped at its form). */
    private static PageFormat longestPage(PrinterJob job, float width, float height) {
        Paper paper = new Paper();
        paper.setSize(width, height);
        paper.setImageableArea(0, 0, width, height);
        PageFormat requested = new PageFormat();
        requested.setPaper(paper);
        return job.validatePage(requested);
    }

    /**
     * The roll-paper form of the bill's width that fits the whole bill: the shortest one long enough,
     * else the longest one. Null when the driver has no roll form longer than its default.
     */
    static MediaSizeName findRollForm(PrintService service, float pdfWidth, float pdfHeight) {
        if (service == null) return null;
        Object values = service.getSupportedAttributeValues(Media.class, null, null);
        if (!(values instanceof Media[])) return null;
        float widthMm = pdfWidth / 72f * 25.4f;
        float heightMm = pdfHeight / 72f * 25.4f;
        MediaSizeName best = null;
        float bestLength = 0;
        for (Media media : (Media[]) values) {
            if (!(media instanceof MediaSizeName) || !media.toString().toLowerCase().contains("roll")) continue;
            MediaSize size = MediaSize.getMediaSizeForName((MediaSizeName) media);
            if (size == null || Math.abs(size.getX(MediaSize.MM) - widthMm) > 5) continue;
            float length = size.getY(MediaSize.MM);
            boolean fits = length >= heightMm;
            boolean bestFits = bestLength >= heightMm;
            if (best == null || (fits && (!bestFits || length < bestLength)) || (!fits && !bestFits && length > bestLength)) {
                best = (MediaSizeName) media;
                bestLength = length;
            }
        }
        // only worth choosing when it is longer than the 297 mm default
        return best != null && bestLength > 300 ? best : null;
    }

//    public static void main(String args[])
//    {
//        //BasicConfigurator.configure();
//        new PrintingExample("D:\\Shopee\\bill.pdf");
//
//    }
    static void changeWindowsDefaultPrinter(String printerName) {
        String cmdLine  = String.format("RUNDLL32 PRINTUI.DLL,PrintUIEntry /y /n \"%s\"", printerName);
        ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", cmdLine );
        builder.redirectErrorStream(true);
        Process p = null;
        try { p = builder.start(); }
        catch (IOException e) { e.printStackTrace(); }

        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = new String();
        while (true) {
            try {
                line = r.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (line == null) { break; }
            System.out.println( "result  "  + line);
        }
    }
    /** The driver's "Document[Feed,Cut]" source (EPSON TM drivers): no cut between pages, one cut at the end. */
    static Media findDocumentTray(PrintService service) {
        if (service == null) return null;
        Object values = service.getSupportedAttributeValues(Media.class, null, null);
        if (!(values instanceof Media[])) return null;
        for (Media media : (Media[]) values) {
            if (media instanceof MediaTray && media.toString().replace(" ", "").equalsIgnoreCase("Document[Feed,Cut]")) {
                return media;
            }
        }
        return null;
    }

    /** Draws one tall PDF page as several printer pages, each as tall as the driver can print. */
    static class BillSlices implements Printable {
        private static final int MAX_SEAM_SHIFT = 60; // how far up (PDF units) to look for a gap between lines

        final PDFRenderer renderer;
        final double scale;        // shrink to the printable width, never enlarge
        final double sliceHeight;  // tallest slice, in PDF units
        final List<Integer> starts = new ArrayList<>();
        final float pdfHeight;
        final int count;

        BillSlices(PDDocument document, float pdfWidth, float pdfHeight, PageFormat format) throws IOException {
            renderer = new PDFRenderer(document);
            this.pdfHeight = pdfHeight;
            scale = Math.min(1.0, format.getImageableWidth() / pdfWidth);
            double printable = format.getImageableHeight() / scale;
            sliceHeight = printable > 0 ? Math.min(printable, pdfHeight) : pdfHeight;

            // End each slice on a blank row so no line of text is cut in half where the slices meet
            BufferedImage page = renderer.renderImage(0, 1f, ImageType.GRAY); // 1 pixel per PDF unit
            int start = 0;
            starts.add(start);
            while (pdfHeight - start > sliceHeight) {
                int end = (int) (start + sliceHeight);
                int seam = end;
                for (int row = end; row > end - MAX_SEAM_SHIFT && row > start; row--) {
                    if (isBlankRow(page, row)) { seam = row; break; }
                }
                starts.add(seam);
                start = seam;
            }
            count = starts.size();
        }

        private static boolean isBlankRow(BufferedImage image, int row) {
            if (row >= image.getHeight()) return true;
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, row) & 0xFF) < 250) return false;
            }
            return true;
        }

        @Override
        public int print(Graphics graphics, PageFormat format, int index) throws PrinterException {
            if (index >= count) return NO_SUCH_PAGE;
            int top = starts.get(index);
            double bottom = index + 1 < count ? starts.get(index + 1) : pdfHeight;
            Graphics2D g2 = (Graphics2D) graphics;
            g2.setBackground(java.awt.Color.WHITE); // the renderer clears to the background colour first
            g2.translate(format.getImageableX(), format.getImageableY());
            g2.scale(scale, scale);
            double width = format.getImageableWidth() / scale;
            g2.clip(new Rectangle2D.Double(0, 0, width, bottom - top));
            g2.translate(0, -top);
            // the renderer changes the clip and transform it is given, so draw on a copy
            Graphics2D pageGraphics = (Graphics2D) g2.create();
            try {
                renderer.renderPageToGraphics(0, pageGraphics);
            } catch (IOException e) {
                throw new PrinterException(e.getMessage());
            } finally {
                pageGraphics.dispose();
            }
            // blank out the part of the next slice that the renderer drew past this slice's end
            g2.setClip(null);
            g2.setColor(java.awt.Color.WHITE);
            g2.fill(new Rectangle2D.Double(0, bottom, width, pdfHeight));
            return PAGE_EXISTS;
        }
    }

    /**
     * Dot width for ESC/POS printing, or {@link #DRIVER_PRINTING}. Auto: receipt printers by name; the TM-T88
     * series is 180 dpi (512 dots on 80 mm paper), most other 80 mm receipt printers are 203 dpi (576 dots).
     */
    static int resolveDots(PrintService service, int configured) {
        if (configured != AUTO_WIDTH) return configured;
        String name = service == null ? "" : service.getName().toLowerCase();
        if (!name.matches(".*(tm-|receipt|thermal|pos|rp-|xp-).*")) return DRIVER_PRINTING;
        return name.contains("t88") ? 512 : 576;
    }

    // @SuppressWarnings("unused")
    static PrintService findPrintService(String printerName) {
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService printService : printServices) {
            if (printService.getName().trim().equals(printerName)) {
                return printService;
            }
            //return printService;
        }
        return null;
    }
}