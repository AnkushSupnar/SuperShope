package com.ankush.controller.transaction;

import com.ankush.data.entities.*;
import com.ankush.data.service.*;
import com.ankush.view.AlertNotification;
import com.ankush.view.FxmlView;
import com.ankush.view.StageManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.converter.FloatStringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.temporal.TemporalAdjusters.nextOrSame;
import static java.time.temporal.TemporalAdjusters.previousOrSame;

@Component
public class SalesReturnController implements Initializable {

    @Autowired @Lazy
    StageManager stageManager;
    @Autowired private AlertNotification alert;
    @Autowired private BillService billService;
    @Autowired private SalesReturnService salesReturnService;
    @Autowired private ItemStockService stockService;
    @Autowired private LoginService loginService;

    @FXML private DatePicker date;
    @FXML private TextField txtBillNo;
    @FXML private Button btnLoadBill;
    @FXML private Label lblCustomer;
    @FXML private TextField txtCustomer;
    @FXML private Label lblCustomerMobile;
    @FXML private TextField txtCustomerMobile;
    @FXML private DatePicker billDate;
    @FXML private TextField txtBillTotal;
    @FXML private TextArea txtReason;

    @FXML private TableView<ReturnRow> table;
    @FXML private TableColumn<ReturnRow, Number> colSr;
    @FXML private TableColumn<ReturnRow, String> colBarcode;
    @FXML private TableColumn<ReturnRow, String> colItemName;
    @FXML private TableColumn<ReturnRow, String> colUnit;
    @FXML private TableColumn<ReturnRow, Number> colSoldQty;
    @FXML private TableColumn<ReturnRow, Number> colAlreadyReturned;
    @FXML private TableColumn<ReturnRow, Number> colReturnableQty;
    @FXML private TableColumn<ReturnRow, Number> colRate;
    @FXML private TableColumn<ReturnRow, Float> colReturnQty;
    @FXML private TableColumn<ReturnRow, Number> colAmount;

    @FXML private TextField txtTotalReturnAmount;
    @FXML private Button btnSave;
    @FXML private Button btnClear;
    @FXML private Button btnHome;
    @FXML private Button btnReturnAll;
    @FXML private Button btnReturnItem;
    @FXML private Button btnPrint;
    @FXML private Button btnPreview;

    @FXML private TableView<SalesReturn> tableOld;
    @FXML private TableColumn<SalesReturn, Long> colReturnId;
    @FXML private TableColumn<SalesReturn, Long> colOldBillNo;
    @FXML private TableColumn<SalesReturn, LocalDate> colOldDate;
    @FXML private TableColumn<SalesReturn, String> colOldCustomer;
    @FXML private TableColumn<SalesReturn, Number> colOldAmount;

    @FXML private DatePicker dateSearch;
    @FXML private Button btnToday;
    @FXML private Button btnWeek;
    @FXML private Button btnMonth;
    @FXML private Button btnAll;

    private final ObservableList<ReturnRow> rowList = FXCollections.observableArrayList();
    private final ObservableList<SalesReturn> returnList = FXCollections.observableArrayList();
    private Bill loadedBill;
    private Bill pendingBill;
    private SalesReturn lastSaved;
    @Autowired private com.ankush.controller.print.PrintSalesReturn printer;
    @Autowired private com.ankush.data.service.ShopeeInfoService shopeeInfoService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        date.setValue(LocalDate.now());
        dateSearch.setValue(LocalDate.now());
        txtTotalReturnAmount.setText("0.0");

        setupTable();
        setupOldTable();

        btnLoadBill.setOnAction(e -> loadBill());
        txtBillNo.setOnAction(e -> loadBill());
        btnSave.setOnAction(e -> save());
        btnClear.setOnAction(e -> clear());
        btnHome.setOnAction(e -> {
            Stage win = (Stage) btnHome.getScene().getWindow();
            if (win == stageManager.getPrimaryStage()) {
                stageManager.switchScene(FxmlView.HOME);
            } else {
                win.close();
            }
        });

        dateSearch.setOnAction(e -> reloadOld(dateSearch.getValue()));
        btnToday.setOnAction(e -> reloadOld(LocalDate.now()));
        btnWeek.setOnAction(e -> {
            LocalDate d = dateSearch.getValue() == null ? LocalDate.now() : dateSearch.getValue();
            returnList.setAll(salesReturnService.getByDateBetween(
                    d.with(previousOrSame(MONDAY)), d.with(nextOrSame(SUNDAY))));
        });
        btnMonth.setOnAction(e -> {
            LocalDate d = dateSearch.getValue() == null ? LocalDate.now() : dateSearch.getValue();
            returnList.setAll(salesReturnService.getByDateBetween(
                    d.withDayOfMonth(1), d.withDayOfMonth(d.lengthOfMonth())));
        });
        btnAll.setOnAction(e -> returnList.setAll(salesReturnService.getAll()));

        if (btnReturnAll != null) btnReturnAll.setOnAction(e -> returnAll());
        if (btnReturnItem != null) btnReturnItem.setOnAction(e -> returnSelected());
        if (btnPrint != null) btnPrint.setOnAction(e -> printLastReturn(true));
        if (btnPreview != null) btnPreview.setOnAction(e -> printLastReturn(false));

        reloadOld(LocalDate.now());

        // if a bill was pre-selected from another screen, populate it now
        if (pendingBill != null) {
            Bill p = pendingBill;
            pendingBill = null;
            txtBillNo.setText(String.valueOf(p.getBillno()));
            loadedBill = p;
            populateFromBill(p);
        }
    }

    public void preselectBill(Bill bill) {
        this.pendingBill = bill;
    }

    private void returnAll() {
        if (rowList.isEmpty()) {
            alert.showError("Load a Bill first");
            return;
        }
        for (ReturnRow r : rowList) {
            float qty = r.getReturnableQty() == null ? 0f : r.getReturnableQty();
            r.setReturnQty(qty);
            r.setAmount(qty * (r.getRate() == null ? 0f : r.getRate()));
        }
        table.refresh();
        recalcTotal();
    }

    private void returnSelected() {
        ReturnRow r = table.getSelectionModel().getSelectedItem();
        if (r == null) {
            alert.showError("Select an item row first");
            return;
        }
        float qty = r.getReturnableQty() == null ? 0f : r.getReturnableQty();
        if (qty <= 0f) {
            alert.showError("Nothing returnable for " + r.getItemname());
            return;
        }
        r.setReturnQty(qty);
        r.setAmount(qty * (r.getRate() == null ? 0f : r.getRate()));
        table.refresh();
        recalcTotal();
    }

    private boolean isCashBill(Bill bill) {
        if (bill == null) return false;
        Customer c = bill.getCustomer();
        boolean cashCustomer = c != null && c.getId() != null && c.getId() == 1;
        boolean cashPaid = bill.getPaid() > 0f;
        return cashCustomer && cashPaid;
    }

    private void setCustomerFieldsVisible(boolean visible) {
        if (lblCustomer != null) { lblCustomer.setVisible(visible); lblCustomer.setManaged(visible); }
        if (txtCustomer != null) { txtCustomer.setVisible(visible); txtCustomer.setManaged(visible); }
        if (lblCustomerMobile != null) { lblCustomerMobile.setVisible(visible); lblCustomerMobile.setManaged(visible); }
        if (txtCustomerMobile != null) { txtCustomerMobile.setVisible(visible); txtCustomerMobile.setManaged(visible); }
    }

    private void printLastReturn(boolean sendToPrinter) {
        if (lastSaved == null) {
            alert.showError("No saved Sales Return to print. Save first.");
            return;
        }
        try {
            java.util.List<ShopeeInfo> infos = shopeeInfoService.getAllShopeeInfo();
            ShopeeInfo info = infos.isEmpty() ? null : infos.get(0);
            String path = printer.generate(lastSaved, info);
            if (sendToPrinter) {
                new com.ankush.controller.print.PrintExample(path);
            } else {
                java.awt.Desktop.getDesktop().open(new java.io.File(path));
            }
        } catch (Exception ex) {
            alert.showError("Failed to print: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void reloadOld(LocalDate d) {
        if (d == null) return;
        returnList.setAll(salesReturnService.getByDate(d));
    }

    private void setupTable() {
        Font kiranFont = Font.font("Kiran", 18);

        colSr.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getSr()));
        colSr.setCellFactory(numberCellFactory(kiranFont));

        colBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        colBarcode.setCellFactory(stringCellFactory(kiranFont));

        colItemName.setCellValueFactory(new PropertyValueFactory<>("itemname"));
        colItemName.setCellFactory(stringCellFactory(Font.font("Kiran", 22)));

        colUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        colUnit.setCellFactory(stringCellFactory(kiranFont));

        colSoldQty.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getSoldQty()));
        colSoldQty.setCellFactory(numberCellFactory(kiranFont));

        colAlreadyReturned.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getAlreadyReturned()));
        colAlreadyReturned.setCellFactory(numberCellFactory(kiranFont));

        colReturnableQty.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getReturnableQty()));
        colReturnableQty.setCellFactory(numberCellFactory(kiranFont));

        colRate.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getRate()));
        colRate.setCellFactory(numberCellFactory(kiranFont));

        colReturnQty.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getReturnQty()));

        colAmount.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getAmount()));
        colAmount.setCellFactory(numberCellFactory(kiranFont));

        // make return qty editable (Kiran-fonted text field for editing)
        Font kiranEditFont = Font.font("Kiran", 20);
        colReturnQty.setCellFactory(column -> new TextFieldTableCell<ReturnRow, Float>(new FloatStringConverter() {
            @Override
            public Float fromString(String value) {
                try {
                    if (value == null || value.isBlank()) return 0.0f;
                    return Float.parseFloat(value);
                } catch (NumberFormatException ex) {
                    return 0.0f;
                }
            }
        }) {
            @Override
            public void updateItem(Float item, boolean empty) {
                super.updateItem(item, empty);
                setFont(kiranEditFont);
                setStyle("-fx-text-fill: #B71C1C; -fx-font-weight: bold;");
            }
        });
        colReturnQty.setOnEditCommit(event -> {
            ReturnRow row = event.getRowValue();
            Float newVal = (Float) event.getNewValue();
            if (newVal == null || newVal < 0) newVal = 0.0f;
            float maxReturnable = row.getReturnableQty();
            if (newVal > maxReturnable) {
                alert.showError("Return qty cannot exceed returnable " + maxReturnable + " for " + row.getItemname());
                newVal = maxReturnable;
            }
            row.setReturnQty(newVal);
            row.setAmount(newVal * row.getRate());
            table.refresh();
            recalcTotal();
        });
        table.setEditable(true);
        table.setItems(rowList);
    }

    private void setupOldTable() {
        Font kiran = Font.font("Kiran", 14);
        colReturnId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colOldBillNo.setCellValueFactory(c -> {
            Bill b = c.getValue().getBill();
            return new SimpleObjectProperty<>(b == null ? null : b.getBillno());
        });
        colOldDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colOldCustomer.setCellValueFactory(c -> {
            Customer cust = c.getValue().getCustomer();
            return new SimpleStringProperty(cust == null ? "" : cust.getName());
        });
        colOldCustomer.setCellFactory(stringCellFactory(kiran));
        colOldAmount.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getRefundamount()));
        tableOld.setItems(returnList);
    }

    private <S> Callback<TableColumn<S, String>, TableCell<S, String>> stringCellFactory(Font font) {
        return col -> new TableCell<S, String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setFont(font);
                    setText(item);
                }
            }
        };
    }

    private <S> Callback<TableColumn<S, Number>, TableCell<S, Number>> numberCellFactory(Font font) {
        return col -> new TableCell<S, Number>() {
            @Override
            public void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setFont(font);
                    setText(String.valueOf(item));
                }
            }
        };
    }

    private void loadBill() {
        String txt = txtBillNo.getText();
        if (txt == null || txt.isBlank()) {
            alert.showError("Enter Bill No");
            txtBillNo.requestFocus();
            return;
        }
        long no;
        try {
            no = Long.parseLong(txt.trim());
        } catch (NumberFormatException ex) {
            alert.showError("Invalid Bill No");
            return;
        }
        Bill bill;
        try {
            bill = billService.getBillByBillNo(no);
        } catch (Exception ex) {
            alert.showError("Bill not found: " + no);
            return;
        }
        if (bill == null) {
            alert.showError("Bill not found: " + no);
            return;
        }
        populateFromBill(bill);
    }

    private void populateFromBill(Bill bill) {
        loadedBill = bill;
        billDate.setValue(bill.getDate());
        txtBillTotal.setText(String.valueOf(bill.getGrandtotal()));
        if (isCashBill(bill)) {
            txtCustomer.setText("");
            txtCustomerMobile.setText("");
            setCustomerFieldsVisible(false);
        } else {
            setCustomerFieldsVisible(true);
            if (bill.getCustomer() != null) {
                txtCustomer.setText(bill.getCustomer().getName() == null ? "" : bill.getCustomer().getName());
                txtCustomerMobile.setText(bill.getCustomer().getContact() == null ? "" : bill.getCustomer().getContact());
            } else {
                txtCustomer.setText("");
                txtCustomerMobile.setText("");
            }
        }
        rowList.clear();
        long sr = 0;
        for (Transaction tr : bill.getTransactions()) {
            float already = salesReturnService.getAlreadyReturnedQty(bill.getBillno(), tr.getItemname(), tr.getBarcode());
            float returnable = (tr.getQuantity() == null ? 0f : tr.getQuantity()) - already;
            if (returnable < 0) returnable = 0;
            ReturnRow row = ReturnRow.builder()
                    .sr(++sr)
                    .barcode(tr.getBarcode())
                    .itemname(tr.getItemname())
                    .unit(tr.getUnit())
                    .soldQty(tr.getQuantity() == null ? 0f : tr.getQuantity())
                    .alreadyReturned(already)
                    .returnableQty(returnable)
                    .rate(tr.getSailingprice() == null ? 0f : tr.getSailingprice())
                    .returnQty(0.0f)
                    .amount(0.0f)
                    .build();
            rowList.add(row);
        }
        recalcTotal();
    }

    private void recalcTotal() {
        float total = 0;
        for (ReturnRow r : rowList) {
            total += (r.getAmount() == null ? 0 : r.getAmount());
        }
        txtTotalReturnAmount.setText(String.valueOf(total));
    }

    private void save() {
        try {
            if (loadedBill == null) {
                alert.showError("Load a Bill first");
                return;
            }
            if (date.getValue() == null) {
                alert.showError("Select Return Date");
                date.requestFocus();
                return;
            }
            List<ReturnRow> toReturn = new ArrayList<>();
            for (ReturnRow r : rowList) {
                if (r.getReturnQty() != null && r.getReturnQty() > 0) toReturn.add(r);
            }
            if (toReturn.isEmpty()) {
                alert.showError("Enter Return Quantity for at least one item");
                return;
            }
            // safety re-check vs latest already-returned values
            for (ReturnRow r : toReturn) {
                float already = salesReturnService.getAlreadyReturnedQty(loadedBill.getBillno(), r.getItemname(), r.getBarcode());
                float maxReturnable = r.getSoldQty() - already;
                if (r.getReturnQty() > maxReturnable + 0.0001f) {
                    alert.showError("Return qty exceeds available for " + r.getItemname() + " (max " + maxReturnable + ")");
                    return;
                }
            }
            if (!alert.showConfirmation("Confirm Sales Return", "Save sales return for bill " + loadedBill.getBillno() + "?")) {
                return;
            }

            float total = 0;
            SalesReturn sr = SalesReturn.builder()
                    .bill(loadedBill)
                    .customer(loadedBill.getCustomer())
                    .date(date.getValue())
                    .reason(txtReason.getText() == null ? "" : txtReason.getText())
                    .login(loginService.getLoginByUserName("Admin"))
                    .transactions(new ArrayList<>())
                    .build();

            for (ReturnRow r : toReturn) {
                float amt = r.getReturnQty() * r.getRate();
                total += amt;
                SalesReturnTransaction t = SalesReturnTransaction.builder()
                        .barcode(r.getBarcode())
                        .itemname(r.getItemname())
                        .unit(r.getUnit())
                        .quantity(r.getReturnQty())
                        .rate(r.getRate())
                        .amount(amt)
                        .salesReturn(sr)
                        .build();
                sr.getTransactions().add(t);
            }
            sr.setNettotal(total);
            sr.setRefundamount(total);

            int result = salesReturnService.saveSalesReturn(sr);
            if (result == 1) {
                addBackStock(sr.getTransactions());
                lastSaved = sr;
                alert.showSuccess("Sales Return Saved. Refund Amount: " + total);
                if (alert.showConfirmation("Print Receipt", "Do you want to print the sales return receipt?")) {
                    printLastReturn(true);
                }
                reloadOld(date.getValue());
                clear();
            } else {
                alert.showError("Could not save Sales Return");
            }
        } catch (Exception e) {
            alert.showError("Error saving Sales Return: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addBackStock(List<SalesReturnTransaction> list) {
        for (SalesReturnTransaction t : list) {
            ItemStock stock = ItemStock.builder()
                    .itemname(t.getItemname())
                    .barcode(t.getBarcode())
                    .unit(t.getUnit())
                    .stock(t.getQuantity())
                    .build();
            stockService.saveItemStock(stock);
        }
    }

    private void clear() {
        loadedBill = null;
        txtBillNo.setText("");
        txtCustomer.setText("");
        txtCustomerMobile.setText("");
        setCustomerFieldsVisible(true);
        billDate.setValue(null);
        txtBillTotal.setText("");
        txtReason.setText("");
        rowList.clear();
        txtTotalReturnAmount.setText("0.0");
        date.setValue(LocalDate.now());
    }

    @lombok.Getter
    @lombok.Setter
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class ReturnRow {
        private Long sr;
        private String barcode;
        private String itemname;
        private String unit;
        private Float soldQty;
        private Float alreadyReturned;
        private Float returnableQty;
        private Float rate;
        private Float returnQty;
        private Float amount;
    }
}
