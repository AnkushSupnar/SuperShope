package com.ankush.controller.print;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.standard.JobName;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Prints the bill on an ESC/POS thermal roll printer (EPSON TM series and compatibles) as raw printer commands.
 * <p>
 * Printing through the Windows driver forces the bill onto the driver's paper forms (80 x 297 mm), so long
 * bills were cut off or split into pages. Here the bill is rendered to one black-and-white image exactly as
 * tall as its content and sent as a single raw job: the printer feeds exactly the bill's length and cuts once
 * at the end. Windows passes RAW jobs straight to the printer (winprint print processor).
 */
public class EscPosPrinter {
    private static final int BAND_ROWS = 128;     // image rows per GS v 0 command; small bands suit every TM model
    private static final int INK_THRESHOLD = 160; // grey level below which a dot is printed
    private static final int FEED_LINES = 4;      // blank lines after the bill so the cut is below the footer

    /** Sends the bill PDF to {@code service}, or writes the printer data to {@code toFile} when that is not null. */
    public static void print(String pdfPath, PrintService service, int printerDots, File toFile)
            throws IOException, PrintException {
        byte[] job;
        try (PDDocument document = PDDocument.load(new File(pdfPath))) {
            job = buildJob(document, printerDots);
        }
        if (toFile != null) {
            try (OutputStream out = new FileOutputStream(toFile)) {
                out.write(job);
            }
            return;
        }
        DocPrintJob printJob = service.createPrintJob();
        HashPrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
        attributes.add(new JobName("Bill", null));
        printJob.print(new SimpleDoc(job, DocFlavor.BYTE_ARRAY.AUTOSENSE, null), attributes);
        System.out.println("Bill sent to " + service.getName() + " as ESC/POS: " + job.length + " bytes");
    }

    /** ESC @, the bill as raster image bands (GS v 0), a short feed, then feed-to-cutter and cut (GS V B). */
    static byte[] buildJob(PDDocument document, int printerDots) throws IOException {
        float pageWidth = document.getPage(0).getMediaBox().getWidth();
        float dpi = printerDots / pageWidth * 72f; // the page width maps onto the printer's full dot width
        BufferedImage image = new PDFRenderer(document).renderImageWithDPI(0, dpi, ImageType.GRAY);

        int width = Math.min(printerDots, image.getWidth());
        int bytesPerRow = (printerDots + 7) / 8;
        int height = lastInkedRow(image, width) + 1;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(new byte[]{0x1B, 0x40}); // ESC @ initialise
        for (int top = 0; top < height; top += BAND_ROWS) {
            int rows = Math.min(BAND_ROWS, height - top);
            out.write(new byte[]{0x1D, 0x76, 0x30, 0x00,
                    (byte) (bytesPerRow & 0xFF), (byte) (bytesPerRow >> 8),
                    (byte) (rows & 0xFF), (byte) (rows >> 8)});
            for (int y = top; y < top + rows; y++) {
                for (int b = 0; b < bytesPerRow; b++) {
                    int bits = 0;
                    for (int bit = 0; bit < 8; bit++) {
                        int x = b * 8 + bit;
                        if (x < width && isInk(image, x, y)) bits |= 0x80 >> bit;
                    }
                    out.write(bits);
                }
            }
        }
        out.write(new byte[]{0x1B, 0x64, (byte) FEED_LINES}); // ESC d n feed
        out.write(new byte[]{0x1D, 0x56, 0x42, 0x00});        // GS V B 0: feed to the cutter and cut
        return out.toByteArray();
    }

    private static boolean isInk(BufferedImage image, int x, int y) {
        return (image.getRGB(x, y) & 0xFF) < INK_THRESHOLD;
    }

    /** Index of the last row with any printed dot, so no blank paper is fed after the footer. */
    private static int lastInkedRow(BufferedImage image, int width) {
        for (int y = image.getHeight() - 1; y >= 0; y--) {
            for (int x = 0; x < width; x++) {
                if (isInk(image, x, y)) return y;
            }
        }
        return 0;
    }
}
