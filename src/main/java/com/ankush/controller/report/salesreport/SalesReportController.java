package com.ankush.controller.report.salesreport;

import com.ankush.config.SpringFXMLLoader;
import com.ankush.data.dto.BillSalesReport;
import com.ankush.data.entities.Bill;
import com.ankush.data.entities.ShopeeInfo;
import com.ankush.data.service.BillService;
import com.ankush.data.service.ShopeeInfoService;
import com.ankush.view.AlertNotification;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;
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
public class SalesReportController implements Initializable {

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

    // Bill number search
    @FXML private TextField txtBillNo;

    // Buttons
    @FXML private Button btnShow;
    @FXML private Button btnExportPdf;
    @FXML private Button btnExportExcel;
    @FXML private Button btnBack;

    // Table
    @FXML private TableView<BillSalesReport> table;
    @FXML private TableColumn<BillSalesReport, Integer> colSr;
    @FXML private TableColumn<BillSalesReport, Long> colBillNo;
    @FXML private TableColumn<BillSalesReport, LocalDate> colDate;
    @FXML private TableColumn<BillSalesReport, String> colCustomer;
    @FXML private TableColumn<BillSalesReport, Integer> colItems;
    @FXML private TableColumn<BillSalesReport, Float> colNetTotal;
    @FXML private TableColumn<BillSalesReport, Float> colDiscount;
    @FXML private TableColumn<BillSalesReport, Float> colGrandTotal;
    @FXML private TableColumn<BillSalesReport, Float> colPaid;

    // Summary labels
    @FXML private Label lblTotalBills;
    @FXML private Label lblTotalSale;
    @FXML private Label lblTotalPaid;

    // Services
    @Autowired private BillService billService;
    @Autowired private ShopeeInfoService shopeeInfoService;
    @Autowired private AlertNotification alert;
    @Autowired private SpringFXMLLoader loader;

    private ObservableList<BillSalesReport> saleList = FXCollections.observableArrayList();
    private ObservableList<BillSalesReport> allBillsList = FXCollections.observableArrayList();
    private String currentPeriod = "day";

    private static final String CHIP_ACTIVE =
            "-fx-background-color: WHITE; -fx-background-radius: 18; -fx-padding: 6 18; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 1);";
    private static final String CHIP_INACTIVE =
            "-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 18; -fx-padding: 6 18; -fx-cursor: hand;";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Table columns
        colSr.setCellValueFactory(new PropertyValueFactory<>("sr"));
        colBillNo.setCellValueFactory(new PropertyValueFactory<>("billNo"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colItems.setCellValueFactory(new PropertyValueFactory<>("totalItems"));
        colNetTotal.setCellValueFactory(new PropertyValueFactory<>("netTotal"));
        colDiscount.setCellValueFactory(new PropertyValueFactory<>("discount"));
        colGrandTotal.setCellValueFactory(new PropertyValueFactory<>("grandTotal"));
        colPaid.setCellValueFactory(new PropertyValueFactory<>("paid"));
        table.setItems(saleList);

        // Override report-table.css Kiran font: apply system font cell factory to all non-customer columns
        String systemStyle = "-fx-font-family: 'System'; -fx-font-size: 13px;";
        applySystemFontFactory(colSr, systemStyle);
        applySystemFontFactory(colBillNo, systemStyle);
        applySystemFontFactory(colItems, systemStyle);
        applySystemFontFactory(colNetTotal, systemStyle);
        applySystemFontFactory(colDiscount, systemStyle);
        applySystemFontFactory(colGrandTotal, systemStyle);
        applySystemFontFactory(colPaid, systemStyle);

        // Date column: system font with dd/MM/yyyy format
        colDate.setCellFactory(col -> new TableCell<BillSalesReport, LocalDate>() {
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(fmt));
                }
                setStyle("-fx-font-family: 'System'; -fx-font-size: 13px;");
            }
        });

        // Customer column: Kiran font for customer name, green "PAID" when no customer
        colCustomer.setCellFactory(col -> new TableCell<BillSalesReport, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                BillSalesReport row = getTableRow().getItem();
                if (row.isHasCustomer()) {
                    setText(row.getCustomerName());
                    setFont(javafx.scene.text.Font.font("kiran", 22.0));
                    setStyle("-fx-text-fill: #1A237E;");
                } else {
                    setText("PAID");
                    setFont(javafx.scene.text.Font.font("System", 13.0));
                    setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                }
            }
        });

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
        saleList.clear();
        allBillsList.clear();
        table.refresh();

        List<Bill> billList;
        if (from.equals(to)) {
            billList = billService.getBillByDate(from);
        } else {
            billList = billService.findByDateBetween(from, to);
        }

        int sr = 1;
        for (Bill bill : billList) {
            boolean hasCustomer = bill.getCustomer() != null
                    && bill.getCustomer().getName() != null
                    && !bill.getCustomer().getName().trim().isEmpty();
            String customerName = hasCustomer ? bill.getCustomer().getName() : "PAID";

            int itemCount = 0;
            if (bill.getTransactions() != null) {
                itemCount = bill.getTransactions().size();
            }

            float netTotal = bill.getNettotal() != null ? bill.getNettotal() : 0f;
            float discount = bill.getDiscount() != null ? bill.getDiscount() : 0f;
            float grandTotal = bill.getGrandtotal() != null ? bill.getGrandtotal() : 0f;
            float paid = bill.getPaid() != null ? bill.getPaid() : 0f;

            BillSalesReport row = BillSalesReport.builder()
                    .sr(sr++)
                    .billNo(bill.getBillno())
                    .date(bill.getDate())
                    .customerName(customerName)
                    .hasCustomer(hasCustomer)
                    .totalItems(itemCount)
                    .netTotal(netTotal)
                    .discount(discount)
                    .grandTotal(grandTotal)
                    .paid(paid)
                    .build();

            allBillsList.add(row);
        }

        saleList.addAll(allBillsList);
        updateSummary();
    }

    private void updateSummary() {
        float totalSale = 0f;
        float totalPaid = 0f;

        for (BillSalesReport row : saleList) {
            totalSale += row.getGrandTotal();
            totalPaid += row.getPaid();
        }

        lblTotalBills.setText(String.valueOf(saleList.size()));
        lblTotalSale.setText(String.format("%.2f", totalSale));
        lblTotalPaid.setText(String.format("%.2f", totalPaid));
    }

    // ===================== SHOW / FILTER =====================

    private void handleShow() {
        loadDataForCurrentPeriod();

        String billNoFilter = txtBillNo.getText();
        if (billNoFilter != null && !billNoFilter.trim().isEmpty()) {
            saleList.clear();
            int sr = 1;
            for (BillSalesReport row : allBillsList) {
                if (String.valueOf(row.getBillNo()).contains(billNoFilter.trim())) {
                    row.setSr(sr++);
                    saleList.add(row);
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
        if (saleList.isEmpty()) {
            alert.showError("No data to export!");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("SalesReport_" + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + ".pdf");
        File file = fileChooser.showSaveDialog(rootPane.getScene().getWindow());
        if (file == null) return;

        try {
            String fontPath = getClass().getResource("/fxml/font/kiran.ttf").toString();
            Font kiranHeader = FontFactory.getFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 18f, Font.BOLD);
            Font kiranSub = FontFactory.getFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 12f, Font.NORMAL, BaseColor.GRAY);
            Font kiranData = FontFactory.getFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 10f, Font.NORMAL, BaseColor.BLACK);
            Font colHeaderFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.WHITE);
            Font dataFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.BLACK);
            Font summaryFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.DARK_GRAY);

            Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
            OutputStream out = new FileOutputStream(file);
            PdfWriter.getInstance(doc, out);
            doc.open();

            // Shop header using table for proper centering with Kiran font
            List<ShopeeInfo> shopList = shopeeInfoService.getAllShopeeInfo();
            if (!shopList.isEmpty()) {
                ShopeeInfo shop = shopList.get(0);
                PdfPTable headerTable = new PdfPTable(1);
                headerTable.setWidthPercentage(100);

                if (shop.getShopeeName() != null && !shop.getShopeeName().isEmpty()) {
                    PdfPCell nameCell = new PdfPCell(new Paragraph(shop.getShopeeName(), kiranHeader));
                    nameCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    nameCell.setBorder(PdfPCell.NO_BORDER);
                    nameCell.setPaddingBottom(4);
                    headerTable.addCell(nameCell);
                }
                if (shop.getAddress() != null && !shop.getAddress().isEmpty()) {
                    PdfPCell addrCell = new PdfPCell(new Paragraph(shop.getAddress(), kiranSub));
                    addrCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    addrCell.setBorder(PdfPCell.NO_BORDER);
                    addrCell.setPaddingBottom(2);
                    headerTable.addCell(addrCell);
                }
                if (shop.getShopeeContact() != null && !shop.getShopeeContact().isEmpty()) {
                    PdfPCell contactCell = new PdfPCell(new Paragraph("Mo. " + shop.getShopeeContact(), kiranSub));
                    contactCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    contactCell.setBorder(PdfPCell.NO_BORDER);
                    contactCell.setPaddingBottom(4);
                    headerTable.addCell(contactCell);
                }
                doc.add(headerTable);
            }

            // Report title and period
            doc.add(new Paragraph(" "));
            LocalDate[] range = getDateRange();
            String periodText = "Sales Report";
            if (range[0].equals(range[1])) {
                periodText += " - " + range[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } else {
                periodText += " - " + range[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        + " to " + range[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            PdfPTable titleTable = new PdfPTable(1);
            titleTable.setWidthPercentage(100);
            PdfPCell titleCell = new PdfPCell(new Paragraph(periodText, new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, new BaseColor(26, 35, 126))));
            titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            titleCell.setBorder(PdfPCell.NO_BORDER);
            titleCell.setPaddingBottom(8);
            titleTable.addCell(titleCell);
            doc.add(titleTable);

            // Data table
            PdfPTable pdfTable = new PdfPTable(9);
            pdfTable.setWidthPercentage(100);
            pdfTable.setWidths(new float[]{5, 8, 10, 20, 6, 12, 10, 14, 12});

            // Column headers
            BaseColor headerBg = new BaseColor(57, 73, 171);
            String[] headers = {"Sr", "Bill No", "Date", "Customer", "Items", "Net Total", "Discount", "Grand Total", "Paid"};
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
            Font paidFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, new BaseColor(46, 125, 50));
            for (BillSalesReport row : saleList) {
                addPdfCell(pdfTable, String.valueOf(row.getSr()), dataFont, Element.ALIGN_CENTER);
                addPdfCell(pdfTable, String.valueOf(row.getBillNo()), dataFont, Element.ALIGN_CENTER);
                addPdfCell(pdfTable, row.getDate().format(dateFmt), dataFont, Element.ALIGN_CENTER);
                // Customer name in Kiran font, or "PAID" in green
                if (row.isHasCustomer()) {
                    addPdfCell(pdfTable, row.getCustomerName(), kiranData, Element.ALIGN_LEFT);
                } else {
                    addPdfCell(pdfTable, "PAID", paidFont, Element.ALIGN_CENTER);
                }
                addPdfCell(pdfTable, String.valueOf(row.getTotalItems()), dataFont, Element.ALIGN_CENTER);
                addPdfCell(pdfTable, String.format("%.2f", row.getNetTotal()), dataFont, Element.ALIGN_RIGHT);
                addPdfCell(pdfTable, String.format("%.2f", row.getDiscount()), dataFont, Element.ALIGN_RIGHT);
                addPdfCell(pdfTable, String.format("%.2f", row.getGrandTotal()), dataFont, Element.ALIGN_RIGHT);
                addPdfCell(pdfTable, String.format("%.2f", row.getPaid()), dataFont, Element.ALIGN_RIGHT);
            }

            doc.add(pdfTable);

            // Summary
            doc.add(new Paragraph(" "));
            PdfPTable summaryTable = new PdfPTable(3);
            summaryTable.setWidthPercentage(80);
            summaryTable.setHorizontalAlignment(Element.ALIGN_CENTER);

            addSummaryCell(summaryTable, "Total Bills: " + lblTotalBills.getText(), summaryFont, new BaseColor(21, 101, 192));
            addSummaryCell(summaryTable, "Total Sale: " + lblTotalSale.getText(), summaryFont, new BaseColor(46, 125, 50));
            addSummaryCell(summaryTable, "Total Paid: " + lblTotalPaid.getText(), summaryFont, new BaseColor(230, 81, 0));

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
        if (saleList.isEmpty()) {
            alert.showError("No data to export!");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Excel Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fileChooser.setInitialFileName("SalesReport_" + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + ".xlsx");
        File file = fileChooser.showSaveDialog(rootPane.getScene().getWindow());
        if (file == null) return;

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sales Report");

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
                periodLabel = "Sales Report - " + range[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } else {
                periodLabel = "Sales Report - " + range[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
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
            String[] headers = {"Sr", "Bill No", "Date", "Customer", "Items", "Net Total", "Discount", "Grand Total", "Paid"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = hRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            int rowNum = 3;
            for (BillSalesReport row : saleList) {
                Row dataRow = sheet.createRow(rowNum++);
                Cell c0 = dataRow.createCell(0); c0.setCellValue(row.getSr()); c0.setCellStyle(dataStyle);
                Cell c1 = dataRow.createCell(1); c1.setCellValue(row.getBillNo()); c1.setCellStyle(dataStyle);
                Cell c2 = dataRow.createCell(2); c2.setCellValue(row.getDate().format(dateFmt)); c2.setCellStyle(dataStyle);
                Cell c3 = dataRow.createCell(3); c3.setCellValue(row.getCustomerName()); c3.setCellStyle(dataStyle);
                Cell c4 = dataRow.createCell(4); c4.setCellValue(row.getTotalItems()); c4.setCellStyle(dataStyle);
                Cell c5 = dataRow.createCell(5); c5.setCellValue(row.getNetTotal()); c5.setCellStyle(numberStyle);
                Cell c6 = dataRow.createCell(6); c6.setCellValue(row.getDiscount()); c6.setCellStyle(numberStyle);
                Cell c7 = dataRow.createCell(7); c7.setCellValue(row.getGrandTotal()); c7.setCellStyle(numberStyle);
                Cell c8 = dataRow.createCell(8); c8.setCellValue(row.getPaid()); c8.setCellStyle(numberStyle);
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

            Cell sLabel = summaryRow.createCell(4);
            sLabel.setCellValue("Totals:");
            sLabel.setCellStyle(summaryStyle);

            Cell sBills = summaryRow.createCell(0);
            sBills.setCellValue("Bills: " + lblTotalBills.getText());
            CellStyle labelStyle = workbook.createCellStyle();
            labelStyle.cloneStyleFrom(summaryStyle);
            labelStyle.setDataFormat((short) 0);
            sBills.setCellStyle(labelStyle);

            Cell sGrandTotal = summaryRow.createCell(7);
            sGrandTotal.setCellValue(Double.parseDouble(lblTotalSale.getText()));
            sGrandTotal.setCellStyle(summaryStyle);

            Cell sPaid = summaryRow.createCell(8);
            sPaid.setCellValue(Double.parseDouble(lblTotalPaid.getText()));
            sPaid.setCellStyle(summaryStyle);

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

    // Helper: apply system font cell factory to a column (overrides Kiran from CSS)
    private <T> void applySystemFontFactory(TableColumn<BillSalesReport, T> column, String style) {
        column.setCellFactory(col -> new TableCell<BillSalesReport, T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
                setStyle(style);
            }
        });
    }
}
