package com.ankush.controller.report.purchase;

import com.ankush.config.SpringFXMLLoader;
import com.ankush.data.dto.MonthlyPurchaseDTO;
import com.ankush.data.entities.PurchaseInvoice;
import com.ankush.data.service.PurchaseInvoiceService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

@Controller
public class MonthlyPurchaseController implements Initializable {

    @FXML private AnchorPane rootPane;
    @FXML private ComboBox<Integer> cmbYear;
    @FXML private Button btnShow;
    @FXML private Button btnHome;

    @FXML private TableView<MonthlyPurchaseDTO> table;
    @FXML private TableColumn<MonthlyPurchaseDTO, Integer> colSr;
    @FXML private TableColumn<MonthlyPurchaseDTO, String> colMonth;
    @FXML private TableColumn<MonthlyPurchaseDTO, Integer> colInvoices;
    @FXML private TableColumn<MonthlyPurchaseDTO, Float> colNetTotal;
    @FXML private TableColumn<MonthlyPurchaseDTO, Float> colOtherCharges;
    @FXML private TableColumn<MonthlyPurchaseDTO, Float> colDiscount;
    @FXML private TableColumn<MonthlyPurchaseDTO, Float> colGrandTotal;
    @FXML private TableColumn<MonthlyPurchaseDTO, Float> colPaid;
    @FXML private TableColumn<MonthlyPurchaseDTO, Float> colDue;

    @FXML private Label lblYearlyTotal;
    @FXML private Label lblYearlyPaid;
    @FXML private Label lblYearlyDue;

    @Autowired
    private PurchaseInvoiceService purchaseInvoiceService;
    @Autowired
    private SpringFXMLLoader loader;

    private ObservableList<MonthlyPurchaseDTO> reportList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        int currentYear = LocalDate.now().getYear();
        ObservableList<Integer> years = FXCollections.observableArrayList();
        for (int y = currentYear; y >= currentYear - 5; y--) {
            years.add(y);
        }
        cmbYear.setItems(years);
        cmbYear.setValue(currentYear);

        colSr.setCellValueFactory(new PropertyValueFactory<>("sr"));
        colMonth.setCellValueFactory(new PropertyValueFactory<>("month"));
        colInvoices.setCellValueFactory(new PropertyValueFactory<>("invoiceCount"));
        colNetTotal.setCellValueFactory(new PropertyValueFactory<>("netTotal"));
        colOtherCharges.setCellValueFactory(new PropertyValueFactory<>("otherCharges"));
        colDiscount.setCellValueFactory(new PropertyValueFactory<>("discount"));
        colGrandTotal.setCellValueFactory(new PropertyValueFactory<>("grandTotal"));
        colPaid.setCellValueFactory(new PropertyValueFactory<>("paid"));
        colDue.setCellValueFactory(new PropertyValueFactory<>("due"));
        table.setItems(reportList);

        loadData();

        btnShow.setOnAction(e -> loadData());
        btnHome.setOnAction(e -> {
            BorderPane root = (BorderPane) rootPane.getParent();
            Pane menuPane = loader.getPage("/fxml/report/ReportMenu.fxml");
            root.setCenter(menuPane);
        });
    }

    private void loadData() {
        reportList.clear();
        int year = cmbYear.getValue();

        float yearlyTotal = 0, yearlyPaid = 0, yearlyDue = 0;
        int sr = 1;

        for (Month month : Month.values()) {
            LocalDate start = LocalDate.of(year, month, 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

            List<PurchaseInvoice> invoices = purchaseInvoiceService.getDatePeriodWisePurchaseInvoice(start, end);

            if (invoices.isEmpty()) continue;

            float netTotal = 0, otherCharges = 0, discount = 0, grandTotal = 0, paid = 0;

            for (PurchaseInvoice inv : invoices) {
                netTotal += inv.getNettotal();
                otherCharges += inv.getTransporting() + inv.getWages() + inv.getOther();
                discount += inv.getDiscount();
                grandTotal += inv.getGrandtotal();
                paid += inv.getPaid();
            }

            float due = grandTotal - paid;

            reportList.add(MonthlyPurchaseDTO.builder()
                    .sr(sr++)
                    .month(month.getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                    .invoiceCount(invoices.size())
                    .netTotal(netTotal)
                    .otherCharges(otherCharges)
                    .discount(discount)
                    .grandTotal(grandTotal)
                    .paid(paid)
                    .due(due)
                    .build());

            yearlyTotal += grandTotal;
            yearlyPaid += paid;
            yearlyDue += due;
        }

        lblYearlyTotal.setText(String.format("%.2f", yearlyTotal));
        lblYearlyPaid.setText(String.format("%.2f", yearlyPaid));
        lblYearlyDue.setText(String.format("%.2f", yearlyDue));
    }
}
