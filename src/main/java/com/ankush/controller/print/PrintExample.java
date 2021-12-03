package com.ankush.controller.print;

import ch.qos.logback.classic.BasicConfigurator;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.apache.pdfbox.printing.PDFPrintable;
import org.apache.pdfbox.printing.Scaling;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.PageRanges;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

public class PrintExample
{
    public PrintExample(String filePath)
    {
        try
        {

           // Properties prop = CommonMethods.loadPropertiesFile();
           // changeWindowsDefaultPrinter(prop.getProperty("Bill.printer"));
            PDDocument document = PDDocument.load(new File(filePath));
            //PDFPrintable printable = new PDFPrintable(document,Scaling.ACTUAL_SIZE);
            //PrintService myPrintService = findPrintService(prop.getProperty("Bill.printer"));
            //PrintService myPrintService = findPrintService("EPSON TM-T82X Receipt");
            //PrintService myPrintService = findPrintService("Star BSC10");



            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPageable(new PDFPageable(document));
            PrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();
            attr.add(new PageRanges(1, 2)); // pages 1 to 1
            //job.setPrintService(myPrintService);
            job.setPrintService(PrintServiceLookup.lookupDefaultPrintService());
            //job.setPrintService(myPrintService);
            job.print(attr);
            document.close();

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
    // @SuppressWarnings("unused")
    private static PrintService findPrintService(String printerName) {
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