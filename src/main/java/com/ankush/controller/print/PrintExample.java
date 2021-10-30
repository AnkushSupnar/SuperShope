package com.ankush.controller.print;

import ch.qos.logback.classic.BasicConfigurator;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

class PrintingExample
{
    PrintingExample(String filePath)
    {
        try
        {
           // Properties prop = CommonMethods.loadPropertiesFile();
           // changeWindowsDefaultPrinter(prop.getProperty("Bill.printer"));
            PDDocument document = PDDocument.load(new File(filePath));

            //PrintService myPrintService = findPrintService(prop.getProperty("Bill.printer"));
            //PrintService myPrintService = findPrintService("EPSON TM-T82X Receipt");
            //PrintService myPrintService = findPrintService("Star BSC10");



            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPageable(new PDFPageable(document));
            //job.setPrintService(myPrintService);
            job.setPrintService(PrintServiceLookup.lookupDefaultPrintService());
            //job.setPrintService(myPrintService);
            job.print();
            document.close();
        }catch(Exception e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String args[])
    {
        //BasicConfigurator.configure();
        new PrintingExample("D:\\Shopee\\bill.pdf");

    }
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