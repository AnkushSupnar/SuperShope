package com.ankush.controller.report.purchase;

import com.ankush.config.SpringFXMLLoader;
import com.ankush.data.dto.PurchaseReportDTO;
import com.ankush.data.entities.PurchaseInvoice;
import com.ankush.data.entities.ShopeeInfo;
import com.ankush.data.service.PurchaseInvoiceService;
import com.ankush.data.service.ShopeeInfoService;
import com.ankush.view.AlertNotification;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

@Controller
public class PurchaseReportController implements Initializable {

    // === FXML fields ===
    @FXML private AnchorPane rootPane;

    // Period chips
    @FXML private HBox chipDay;
    @FXML private HBox chipMonth;
    @FXML private HBox chipYear;
    @FXML private HBox chipCustom;
    @FXML private FontAwesomeIcon chipDayIcon;
    @FXML private FontAwesomeIcon chipMonthIcon;
    @FXML private FontAwesomeIcon chipYearIcon;
    @FXML private FontAwesomeIcon chipCustomIcon;
    @FXML private Label chipDayLabel;
    @FXML private Label chipMonthLabel;
    @FXML private Label chipYearLabel;
    @FXML private Label chipCustomLabel;

    // Date controls
    @FXML private StackPane dateControlsPane;
    @FXML private HBox dayControls;
    @FXML private HBox monthControls;
    @FXML private HBox yearControls;
    @FXML private HBox customControls;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> cmbMonth;
    @FXML private ComboBox<Integer> cmbMonthYear;
    @FXML private ComboBox<Integer> cmbYear;
    @FXML private DatePicker dateFrom;
    @FXML private DatePicker dateTo;

    // Party name search
    @FXML private TextField txtPartyName;

    // Buttons
    @FXML private Button btnShow;
    @FXML private Button btnExportPdf;
    @FXML private Button btnExportExcel;
    @FXML private Button btnBack;

    // Table
    @FXML private TableView<PurchaseReportDTO> table;
    @FXML private TableColumn<PurchaseReportDTO, Integer> colSr;
    @FXML private TableColumn<PurchaseReportDTO, LocalDate> colDate;
    @FXML private TableColumn<PurchaseReportDTO, String> colParty;
    @FXML private TableColumn<PurchaseReportDTO, String> colInvoiceNo;
    @FXML private TableColumn<PurchaseReportDTO, Integer> colItems;
    @FXML private TableColumn<PurchaseReportDTO, Float> colNetTotal;
    @FXML private TableColumn<PurchaseReportDTO, Float> colDiscount;
    @FXML private TableColumn<PurchaseReportDTO, Float> colGrandTotal;
    @FXML private TableColumn<PurchaseReportDTO, Float> colPaid;
    @FXML private TableColumn<PurchaseReportDTO, Float> colDue;

    // Summary labels
    @FXML private Label lblTotalInvoices;
    @FXML private Label lblTotalAmount;
    @FXML private Label lblTotalPaid;
    @FXML private Label lblTotalDue;

    // Services
    @Autowired private PurchaseInvoiceService purchaseInvoiceService;
    @Autowired private ShopeeInfoService shopeeInfoService;
    @Autowired private AlertNotification alert;
    @Autowired private SpringFXMLLoader loader;

    private ObservableList<PurchaseReportDTO> reportList = FXCollections.observableArrayList();
    private ObservableList<PurchaseReportDTO> allInvoicesList = FXCollections.observableArrayList();
    private String currentPeriod = "day";

    private static final String CHIP_ACTIVE =
            "-fx-background-color: WHITE; -fx-background-radius: 18; -fx-padding: 6 18; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 1);";
    private static final String CHIP_INACTIVE =
            "-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 18; -fx-padding: 6 18; -fx-cursor: hand;";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Table columns
        colSr.setCellValueFactory(new PropertyValueFactory<>("sr"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colParty.setCellValueFactory(new PropertyValueFactory<>("partyName"));
        colInvoiceNo.setCellValueFactory(new PropertyValueFactory<>("invoiceNo"));
        colItems.setCellValueFactory(new PropertyValueFactory<>("itemCount"));
        colNetTotal.setCellValueFactory(new PropertyValueFactory<>("netTotal"));
        colDiscount.setCellValueFactory(new PropertyValueFactory<>("discount"));
        colGrandTotal.setCellValueFactory(new PropertyValueFactory<>("grandTotal"));
        colPaid.setCellValueFactory(new PropertyValueFactory<>("paid"));
        colDue.setCellValueFactory(new PropertyValueFactory<>("due"));
        table.setItems(reportList);

        // Date defaults
        datePicker.setValue(LocalDate.now());
        dateFrom.setValue(LocalDate.now().withDayOfMonth(1));
        dateTo.setValue(LocalDate.now());

        // Populate month/year combos
        cmbMonth.setItems(FXCollections.observableArrayList(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        ));
        cmbMonth.setValue(LocalDate.now().getMonth().toString().substring(0, 1)
                + LocalDate.now().getMonth().toString().substring(1).toLowerCase());

        int currentYear = LocalDate.now().getYear();
        ObservableList<Integer> years = FXCollections.observableArrayList();
        for (int y = currentYear; y >= currentYear - 10; y--) years.add(y);
        cmbMonthYear.setItems(years);
        cmbMonthYear.setValue(currentYear);
        cmbYear.setItems(FXCollections.observableArrayList(years));
        cmbYear.setValue(currentYear);

        // Chip handlers
        chipDay.setOnMouseClicked(e -> selectPeriod("day"));
        chipMonth.setOnMouseClicked(e -> selectPeriod("month"));
        chipYear.setOnMouseClicked(e -> selectPeriod("year"));
        chipCustom.setOnMouseClicked(e -> selectPeriod("custom"));

        // Button handlers
        btnShow.setOnAction(e -> handleShow());
        btnExportPdf.setOnAction(e -> exportPdf());
        btnExportExcel.setOnAction(e -> exportExcel());
        btnBack.setOnAction(e -> goBack());

        // Auto-load on date change
        datePicker.setOnAction(e -> loadDataForCurrentPeriod());

        // Load initial data
        loadDataForCurrentPeriod();
    }

    // ===================== PERIOD SELECTION =====================

    private void selectPeriod(String period) {
        currentPeriod = period;

        // Reset all chips
        chipDay.setStyle(CHIP_INACTIVE);
        chipMonth.setStyle(CHIP_INACTIVE);
        chipYear.setStyle(CHIP_INACTIVE);
        chipCustom.setStyle(CHIP_INACTIVE);
        chipDayIcon.setFill(Color.WHITE);
        chipMonthIcon.setFill(Color.WHITE);
        chipYearIcon.setFill(Color.WHITE);
        chipCustomIcon.setFill(Color.WHITE);
        chipDayLabel.setStyle("-fx-text-fill: WHITE; -fx-font-size: 12px; -fx-font-weight: bold;");
        chipMonthLabel.setStyle("-fx-text-fill: WHITE; -fx-font-size: 12px; -fx-font-weight: bold;");
        chipYearLabel.setStyle("-fx-text-fill: WHITE; -fx-font-size: 12px; -fx-font-weight: bold;");
        chipCustomLabel.setStyle("-fx-text-fill: WHITE; -fx-font-size: 12px; -fx-font-weight: bold;");

        // Hide all date panels
        dayControls.setVisible(false);
        monthControls.setVisible(false);
        yearControls.setVisible(false);
        customControls.setVisible(false);

        // Activate selected
        switch (period) {
            case "day":
                chipDay.setStyle(CHIP_ACTIVE);
                chipDayIcon.setFill(Color.web("#1A237E"));
                chipDayLabel.setStyle("-fx-text-fill: #1A237E; -fx-font-size: 12px; -fx-font-weight: bold;");
                dayControls.setVisible(true);
                break;
            case "month":
                chipMonth.setStyle(CHIP_ACTIVE);
                chipMonthIcon.setFill(Color.web("#1A237E"));
                chipMonthLabel.setStyle("-fx-text-fill: #1A237E; -fx-font-size: 12px; -fx-font-weight: bold;");
                monthControls.setVisible(true);
                break;
            case "year":
                chipYear.setStyle(CHIP_ACTIVE);
                chipYearIcon.setFill(Color.web("#1A237E"));
                chipYearLabel.setStyle("-fx-text-fill: #1A237E; -fx-font-size: 12px; -fx-font-weight: bold;");
                yearControls.setVisible(true);
                break;
            case "custom":
                chipCustom.setStyle(CHIP_ACTIVE);
                chipCustomIcon.setFill(Color.web("#1A237E"));
                chipCustomLabel.setStyle("-fx-text-fill: #1A237E; -fx-font-size: 12px; -fx-font-weight: bold;");
                customControls.setVisible(true);
                break;
        }

        loadDataForCurrentPeriod();
    }

    // ===================== DATE RANGE CALCULATION =====================

    private LocalDate[] getDateRange() {
        switch (currentPeriod) {
            case "day":
                LocalDate d = datePicker.getValue();
                return new LocalDate[]{d, d};
            case "month": {
                int monthIndex = cmbMonth.getSelectionModel().getSelectedIndex() + 1;
                int year = cmbMonthYear.getValue();
                YearMonth ym = YearMonth.of(year, monthIndex);
                return new LocalDate[]{ym.atDay(1), ym.atEndOfMonth()};
            }
            case "year": {
                int year = cmbYear.getValue();
                return new LocalDate[]{LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31)};
            }
            case "custom":
                return new LocalDate[]{dateFrom.getValue(), dateTo.getValue()};
            default:
                return new LocalDate[]{LocalDate.now(), LocalDate.now()};
        }
    }

    // ===================== DATA LOADING =====================

    private void loadDataForCurrentPeriod() {
        LocalDate[] range = getDateRange();
        loadDataForRange(range[0], range[1]);
    }

    private void loadDataForRange(LocalDate from, LocalDate to) {
        reportList.clear();
        allInvoicesList.clear();
        table.refresh();

        List<PurchaseInvoice> invoices = purchaseInvoiceService.getDatePeriodWisePurchaseInvoice(from, to);

        int sr = 1;
        for (PurchaseInvoice inv : invoices) {
            String partyName = inv.getPurchaseParty() != null ? inv.getPurchaseParty().getName() : "N/A";
            float due = inv.getGrandtotal() - inv.getPaid();

            PurchaseReportDTO row = PurchaseReportDTO.builder()
                    .sr(sr++)
                    .date(inv.getDate())
                    .partyName(partyName)
                    .invoiceNo(inv.getPartyinvoiceno() != null ? inv.getPartyinvoiceno() : "")
                    .itemCount(inv.getTransactions() != null ? inv.getTransactions().size() : 0)
                    .netTotal(inv.getNettotal())
                    .discount(inv.getDiscount())
                    .grandTotal(inv.getGrandtotal())
                    .paid(inv.getPaid())
                    .due(due)
                    .build();

            allInvoicesList.add(row);
        }

        reportList.addAll(allInvoicesList);
        updateSummary();
    }

    private void updateSummary() {
        float totalAmount = 0f;
        float totalPaid = 0f;
        float totalDue = 0f;

        for (PurchaseReportDTO row : reportList) {
            totalAmount += row.getGrandTotal();
            totalPaid += row.getPaid();
            totalDue += row.getDue();
        }

        lblTotalInvoices.setText(String.valueOf(reportList.size()));
        lblTotalAmount.setText(String.format("%.2f", totalAmount));
        lblTotalPaid.setText(String.format("%.2f", totalPaid));
        lblTotalDue.setText(String.format("%.2f", totalDue));
    }

    // ===================== SHOW / FILTER =====================

    private void handleShow() {
        loadDataForCurrentPeriod();

        String partyFilter = txtPartyName.getText();
        if (partyFilter != null && !partyFilter.trim().isEmpty()) {
            reportList.clear();
            int sr = 1;
            for (PurchaseReportDTO row : allInvoicesList) {
                if (row.getPartyName().toLowerCase().contains(partyFilter.trim().toLowerCase())) {
                    row.setSr(sr++);
                    reportList.add(row);
                }
            }
            updateSummary();
        }
    }

    // ===================== BACK =====================

    private void goBack() {
        try {
            BorderPane root = (BorderPane) rootPane.getParent();
            root.setCenter(loader.getPage("/fxml/report/ReportMenu.fxml"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===================== PDF EXPORT =====================

    private void exportPdf() {
        if (reportList.isEmpty()) {
            alert.showError("No data to export!");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("PurchaseReport_" + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + ".pdf");
        File file = fileChooser.showSaveDialog(rootPane.getScene().getWindow());
        if (file == null) return;

        try {
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.DARK_GRAY);
            Font subHeaderFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.GRAY);
            Font dataFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.BLACK);
            Font colHeaderFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.WHITE);
            Font summaryFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.DARK_GRAY);

            Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
            OutputStream out = new FileOutputStream(file);
            PdfWriter.getInstance(doc, out);
            doc.open();

            // Shop header
            List<ShopeeInfo> shopList = shopeeInfoService.getAllShopeeInfo();
            if (!shopList.isEmpty()) {
                ShopeeInfo shop = shopList.get(0);
                if (shop.getShopeeName() != null && !shop.getShopeeName().isEmpty()) {
                    Paragraph shopName = new Paragraph(shop.getShopeeName(), headerFont);
                    shopName.setAlignment(Element.ALIGN_CENTER);
                    doc.add(shopName);
                }
                if (shop.getAddress() != null && !shop.getAddress().isEmpty()) {
                    Paragraph addr = new Paragraph(shop.getAddress(), subHeaderFont);
                    addr.setAlignment(Element.ALIGN_CENTER);
                    doc.add(addr);
                }
                if (shop.getShopeeContact() != null && !shop.getShopeeContact().isEmpty()) {
                    Paragraph contact = new Paragraph("Contact: " + shop.getShopeeContact(), subHeaderFont);
                    contact.setAlignment(Element.ALIGN_CENTER);
                    doc.add(contact);
                }
            }

            // Report title and period
            doc.add(new Paragraph(" "));
            LocalDate[] range = getDateRange();
            String periodText = "Purchase Report";
            if (range[0].equals(range[1])) {
                periodText += " - " + range[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } else {
                periodText += " - " + range[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        + " to " + range[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            Paragraph title = new Paragraph(periodText, new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, new BaseColor(26, 35, 126)));
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);
            doc.add(new Paragraph(" "));

            // Data table
            PdfPTable pdfTable = new PdfPTable(10);
            pdfTable.setWidthPercentage(100);
            pdfTable.setWidths(new float[]{4, 9, 16, 9, 5, 11, 8, 13, 11, 11});

            // Column headers
            BaseColor headerBg = new BaseColor(57, 73, 171);
            String[] headers = {"Sr", "Date", "Party Name", "Invoice No", "Items", "Net Total", "Discount", "Grand Total", "Paid", "Due"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(h, colHeaderFont));
                cell.setBackgroundColor(headerBg);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                cell.setBorderColor(BaseColor.WHITE);
                pdfTable.addCell(cell);
            }

            // Data rows
            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (PurchaseReportDTO row : reportList) {
                addPdfCell(pdfTable, String.valueOf(row.getSr()), dataFont, Element.ALIGN_CENTER);
                addPdfCell(pdfTable, row.getDate().format(dateFmt), dataFont, Element.ALIGN_CENTER);
                addPdfCell(pdfTable, row.getPartyName(), dataFont, Element.ALIGN_LEFT);
                addPdfCell(pdfTable, row.getInvoiceNo(), dataFont, Element.ALIGN_CENTER);
                addPdfCell(pdfTable, String.valueOf(row.getItemCount()), dataFont, Element.ALIGN_CENTER);
                addPdfCell(pdfTable, String.format("%.2f", row.getNetTotal()), dataFont, Element.ALIGN_RIGHT);
                addPdfCell(pdfTable, String.format("%.2f", row.getDiscount()), dataFont, Element.ALIGN_RIGHT);
                addPdfCell(pdfTable, String.format("%.2f", row.getGrandTotal()), dataFont, Element.ALIGN_RIGHT);
                addPdfCell(pdfTable, String.format("%.2f", row.getPaid()), dataFont, Element.ALIGN_RIGHT);
                addPdfCell(pdfTable, String.format("%.2f", row.getDue()), dataFont, Element.ALIGN_RIGHT);
            }

            doc.add(pdfTable);

            // Summary
            doc.add(new Paragraph(" "));
            PdfPTable summaryTable = new PdfPTable(4);
            summaryTable.setWidthPercentage(90);
            summaryTable.setHorizontalAlignment(Element.ALIGN_CENTER);

            addSummaryCell(summaryTable, "Invoices: " + lblTotalInvoices.getText(), summaryFont, new BaseColor(21, 101, 192));
            addSummaryCell(summaryTable, "Total Amount: " + lblTotalAmount.getText(), summaryFont, new BaseColor(230, 81, 0));
            addSummaryCell(summaryTable, "Total Paid: " + lblTotalPaid.getText(), summaryFont, new BaseColor(46, 125, 50));
            addSummaryCell(summaryTable, "Total Due: " + lblTotalDue.getText(), summaryFont, new BaseColor(198, 40, 40));

            doc.add(summaryTable);

            doc.close();
            out.close();
            alert.showSuccess("PDF exported successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            alert.showError("PDF export failed: " + e.getMessage());
        }
    }

    private void addPdfCell(PdfPTable table, String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(4);
        cell.setBorderColor(new BaseColor(200, 200, 230));
        table.addCell(cell);
    }

    private void addSummaryCell(PdfPTable table, String text, Font font, BaseColor bgColor) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setBackgroundColor(new BaseColor(
                Math.min(bgColor.getRed() + 180, 255),
                Math.min(bgColor.getGreen() + 180, 255),
                Math.min(bgColor.getBlue() + 180, 255)));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8);
        cell.setBorderColor(bgColor);
        table.addCell(cell);
    }

    // ===================== EXCEL EXPORT =====================

    private void exportExcel() {
        if (reportList.isEmpty()) {
            alert.showError("No data to export!");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Excel Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fileChooser.setInitialFileName("PurchaseReport_" + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + ".xlsx");
        File file = fileChooser.showSaveDialog(rootPane.getScene().getWindow());
        if (file == null) return;

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Purchase Report");

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Data style
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // Number style
            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.cloneStyleFrom(dataStyle);
            numberStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

            // Title row
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            LocalDate[] range = getDateRange();
            String periodLabel;
            if (range[0].equals(range[1])) {
                periodLabel = "Purchase Report - " + range[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } else {
                periodLabel = "Purchase Report - " + range[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        + " to " + range[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            titleCell.setCellValue(periodLabel);
            CellStyle titleStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);

            // Header row
            Row hRow = sheet.createRow(2);
            String[] headers = {"Sr", "Date", "Party Name", "Invoice No", "Items", "Net Total", "Discount", "Grand Total", "Paid", "Due"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = hRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            int rowNum = 3;
            for (PurchaseReportDTO row : reportList) {
                Row dataRow = sheet.createRow(rowNum++);
                Cell c0 = dataRow.createCell(0); c0.setCellValue(row.getSr()); c0.setCellStyle(dataStyle);
                Cell c1 = dataRow.createCell(1); c1.setCellValue(row.getDate().format(dateFmt)); c1.setCellStyle(dataStyle);
                Cell c2 = dataRow.createCell(2); c2.setCellValue(row.getPartyName()); c2.setCellStyle(dataStyle);
                Cell c3 = dataRow.createCell(3); c3.setCellValue(row.getInvoiceNo()); c3.setCellStyle(dataStyle);
                Cell c4 = dataRow.createCell(4); c4.setCellValue(row.getItemCount()); c4.setCellStyle(dataStyle);
                Cell c5 = dataRow.createCell(5); c5.setCellValue(row.getNetTotal()); c5.setCellStyle(numberStyle);
                Cell c6 = dataRow.createCell(6); c6.setCellValue(row.getDiscount()); c6.setCellStyle(numberStyle);
                Cell c7 = dataRow.createCell(7); c7.setCellValue(row.getGrandTotal()); c7.setCellStyle(numberStyle);
                Cell c8 = dataRow.createCell(8); c8.setCellValue(row.getPaid()); c8.setCellStyle(numberStyle);
                Cell c9 = dataRow.createCell(9); c9.setCellValue(row.getDue()); c9.setCellStyle(numberStyle);
            }

            // Summary row
            Row summaryRow = sheet.createRow(rowNum + 1);
            CellStyle summaryStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font sFont = workbook.createFont();
            sFont.setBold(true);
            sFont.setFontHeightInPoints((short) 11);
            summaryStyle.setFont(sFont);
            summaryStyle.setBorderTop(BorderStyle.DOUBLE);
            summaryStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

            CellStyle labelStyle = workbook.createCellStyle();
            labelStyle.cloneStyleFrom(summaryStyle);
            labelStyle.setDataFormat((short) 0);

            Cell sInvoices = summaryRow.createCell(0);
            sInvoices.setCellValue("Invoices: " + lblTotalInvoices.getText());
            sInvoices.setCellStyle(labelStyle);

            Cell sLabel = summaryRow.createCell(5);
            sLabel.setCellValue("Totals:");
            sLabel.setCellStyle(labelStyle);

            Cell sGrandTotal = summaryRow.createCell(7);
            sGrandTotal.setCellValue(Double.parseDouble(lblTotalAmount.getText()));
            sGrandTotal.setCellStyle(summaryStyle);

            Cell sPaid = summaryRow.createCell(8);
            sPaid.setCellValue(Double.parseDouble(lblTotalPaid.getText()));
            sPaid.setCellStyle(summaryStyle);

            Cell sDue = summaryRow.createCell(9);
            sDue.setCellValue(Double.parseDouble(lblTotalDue.getText()));
            sDue.setCellStyle(summaryStyle);

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write file
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
            alert.showSuccess("Excel exported successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            alert.showError("Excel export failed: " + e.getMessage());
        }
    }
}
