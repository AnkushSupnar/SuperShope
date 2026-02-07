package com.ankush.controller.report.customer;

import com.ankush.config.SpringFXMLLoader;
import com.ankush.data.dto.CustomerLedgerDTO;
import com.ankush.data.entities.Bill;
import com.ankush.data.entities.Customer;
import com.ankush.data.service.BillService;
import com.ankush.data.service.CustomerService;
import com.ankush.view.AlertNotification;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.ResourceBundle;

@Controller
public class CustomerLedgerController implements Initializable {

    @FXML private AnchorPane rootPane;
    @FXML private TextField txtCustomerName;
    @FXML private DatePicker dateFrom;
    @FXML private DatePicker dateTo;
    @FXML private Button btnShow;
    @FXML private Button btnHome;

    @FXML private HBox customerInfoBar;
    @FXML private Label lblCustomerName;
    @FXML private Label lblCustomerContact;
    @FXML private Label lblCustomerAddress;

    @FXML private TableView<CustomerLedgerDTO> table;
    @FXML private TableColumn<CustomerLedgerDTO, Integer> colSr;
    @FXML private TableColumn<CustomerLedgerDTO, LocalDate> colDate;
    @FXML private TableColumn<CustomerLedgerDTO, Long> colBillNo;
    @FXML private TableColumn<CustomerLedgerDTO, Integer> colItems;
    @FXML private TableColumn<CustomerLedgerDTO, Float> colGrandTotal;
    @FXML private TableColumn<CustomerLedgerDTO, Float> colPaid;
    @FXML private TableColumn<CustomerLedgerDTO, Float> colDue;

    @FXML private Label lblTotalPurchases;
    @FXML private Label lblTotalPaid;
    @FXML private Label lblBalanceDue;

    @Autowired
    private BillService billService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private AlertNotification alert;
    @Autowired
    private SpringFXMLLoader loader;

    private ObservableList<CustomerLedgerDTO> reportList = FXCollections.observableArrayList();
    private ListView<String> listView;
    private ObservableList<String> customerNames = FXCollections.observableArrayList();

    private static final String SYSTEM_FONT_STYLE = "-fx-font-family: 'System'; -fx-font-size: 14px;";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dateFrom.setValue(LocalDate.now().with(TemporalAdjusters.firstDayOfYear()));
        dateTo.setValue(LocalDate.now());

        colSr.setCellValueFactory(new PropertyValueFactory<>("sr"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colBillNo.setCellValueFactory(new PropertyValueFactory<>("billNo"));
        colItems.setCellValueFactory(new PropertyValueFactory<>("items"));
        colGrandTotal.setCellValueFactory(new PropertyValueFactory<>("grandTotal"));
        colPaid.setCellValueFactory(new PropertyValueFactory<>("paid"));
        colDue.setCellValueFactory(new PropertyValueFactory<>("due"));
        table.setItems(reportList);

        // Override Kiran font from report-table.css: use English font for all columns
        applySystemFontFactory(colSr);
        applySystemFontFactory(colBillNo);
        applySystemFontFactory(colItems);
        applySystemFontFactory(colGrandTotal);
        applySystemFontFactory(colPaid);
        applySystemFontFactory(colDue);

        // Date column with English font and formatted date
        colDate.setCellFactory(col -> new TableCell<CustomerLedgerDTO, LocalDate>() {
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(fmt));
                }
                setStyle(SYSTEM_FONT_STYLE);
            }
        });

        addCustomerNameSearch();

        btnShow.setOnAction(e -> loadData());
        btnHome.setOnAction(e -> {
            BorderPane root = (BorderPane) rootPane.getParent();
            Pane menuPane = loader.getPage("/fxml/report/ReportMenu.fxml");
            root.setCenter(menuPane);
        });
    }

    private void loadData() {
        reportList.clear();
        customerInfoBar.setVisible(false);

        String customerName = txtCustomerName.getText().trim();
        if (customerName.isEmpty()) {
            alert.showError("Please select a customer");
            txtCustomerName.requestFocus();
            return;
        }

        Customer customer = customerService.getCustomerByName(customerName);
        if (customer == null) {
            alert.showError("Customer not found: " + customerName);
            return;
        }

        // Show customer info
        customerInfoBar.setVisible(true);
        lblCustomerName.setText(customer.getName() != null ? customer.getName() : "");
        lblCustomerContact.setText(customer.getContact() != null ? customer.getContact() : "");
        lblCustomerAddress.setText(customer.getAddress() != null ? customer.getAddress() : "");

        LocalDate from = dateFrom.getValue();
        LocalDate to = dateTo.getValue();

        List<Bill> bills = billService.findByCustomerIdAndDateBetween(customer.getId(), from, to);

        int sr = 1;
        float totalPurchases = 0, totalPaid = 0, totalDue = 0;

        for (Bill bill : bills) {
            int itemCount = (bill.getTransactions() != null) ? bill.getTransactions().size() : 0;

            float grandTotal = bill.getGrandtotal() != null ? bill.getGrandtotal() : 0;
            float paid = bill.getPaid() != null ? bill.getPaid() : 0;
            float due = grandTotal - paid;

            reportList.add(CustomerLedgerDTO.builder()
                    .sr(sr++)
                    .date(bill.getDate())
                    .billNo(bill.getBillno())
                    .items(itemCount)
                    .grandTotal(grandTotal)
                    .paid(paid)
                    .due(due)
                    .build());

            totalPurchases += grandTotal;
            totalPaid += paid;
            totalDue += due;
        }

        lblTotalPurchases.setText(String.format("%.2f", totalPurchases));
        lblTotalPaid.setText(String.format("%.2f", totalPaid));
        lblBalanceDue.setText(String.format("%.2f", totalDue));
    }

    private void addCustomerNameSearch() {
        customerNames.addAll(customerService.getAllCustomerNames());
        listView = new ListView<>();
        listView.setStyle("-fx-font-size: 14px;");
        listView.setPrefHeight(150);
        listView.setVisible(false);
        listView.setManaged(false);

        rootPane.getChildren().add(listView);

        txtCustomerName.textProperty().addListener((obs, oldVal, newVal) -> {
            findCustomer(newVal);
            if (!listView.getItems().isEmpty()) {
                showListView();
                listView.getSelectionModel().select(0);
            } else {
                listView.setVisible(false);
            }
        });

        txtCustomerName.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.DOWN) {
                if (listView.isVisible() && !listView.getItems().isEmpty()) {
                    listView.requestFocus();
                    listView.getSelectionModel().select(0);
                }
            }
        });

        txtCustomerName.setOnMouseClicked(e -> {
            findCustomer(txtCustomerName.getText());
            showListView();
        });

        txtCustomerName.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                findCustomer(txtCustomerName.getText());
                showListView();
            } else {
                // Delay hiding so listView click can register
                Platform.runLater(() -> {
                    if (!listView.isFocused()) listView.setVisible(false);
                });
            }
        });

        listView.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                selectFromListView();
            }
        });

        listView.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                selectFromListView();
            }
        });

        listView.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !txtCustomerName.isFocused()) listView.setVisible(false);
        });
    }

    private void showListView() {
        Platform.runLater(() -> {
            positionListView();
            listView.setVisible(true);
            listView.toFront();
        });
    }

    private void selectFromListView() {
        String selected = listView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            txtCustomerName.setText(selected);
            listView.setVisible(false);
            txtCustomerName.requestFocus();
            txtCustomerName.positionCaret(selected.length());
        }
    }

    private void positionListView() {
        javafx.geometry.Bounds bounds = txtCustomerName.localToScene(txtCustomerName.getBoundsInLocal());
        javafx.geometry.Bounds rootBounds = rootPane.localToScene(rootPane.getBoundsInLocal());
        if (bounds == null || rootBounds == null) return;
        double x = bounds.getMinX() - rootBounds.getMinX();
        double y = bounds.getMaxY() - rootBounds.getMinY();
        listView.setLayoutX(x);
        listView.setLayoutY(y);
        listView.setPrefWidth(txtCustomerName.getWidth());
    }

    private void findCustomer(String find) {
        listView.getItems().clear();
        if (find == null || find.trim().isEmpty()) {
            listView.getItems().addAll(customerNames);
            return;
        }
        String lower = find.toLowerCase();
        for (String name : customerNames) {
            if (name.toLowerCase().contains(lower)) {
                listView.getItems().add(name);
            }
        }
    }

    // Helper: apply system font cell factory to override Kiran from CSS
    private <T> void applySystemFontFactory(TableColumn<CustomerLedgerDTO, T> column) {
        column.setCellFactory(col -> new TableCell<CustomerLedgerDTO, T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
                setStyle(SYSTEM_FONT_STYLE);
            }
        });
    }
}
