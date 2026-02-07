package com.ankush.controller.report.customer;

import com.ankush.config.SpringFXMLLoader;
import com.ankush.data.dto.OutstandingDuesDTO;
import com.ankush.data.entities.Bill;
import com.ankush.data.entities.Customer;
import com.ankush.data.service.BillService;
import com.ankush.data.service.CustomerService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

@Controller
public class OutstandingDuesController implements Initializable {

    @FXML private AnchorPane rootPane;
    @FXML private Button btnRefresh;
    @FXML private Button btnHome;

    @FXML private TableView<OutstandingDuesDTO> table;
    @FXML private TableColumn<OutstandingDuesDTO, Integer> colSr;
    @FXML private TableColumn<OutstandingDuesDTO, String> colCustomer;
    @FXML private TableColumn<OutstandingDuesDTO, String> colContact;
    @FXML private TableColumn<OutstandingDuesDTO, Integer> colTotalBills;
    @FXML private TableColumn<OutstandingDuesDTO, Float> colTotalAmount;
    @FXML private TableColumn<OutstandingDuesDTO, Float> colTotalPaid;
    @FXML private TableColumn<OutstandingDuesDTO, Float> colDueAmount;

    @FXML private Label lblCustomerCount;
    @FXML private Label lblTotalOutstanding;
    @FXML private Label lblTotalCollected;

    @Autowired
    private BillService billService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private SpringFXMLLoader loader;

    private ObservableList<OutstandingDuesDTO> reportList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colSr.setCellValueFactory(new PropertyValueFactory<>("sr"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colTotalBills.setCellValueFactory(new PropertyValueFactory<>("totalBills"));
        colTotalAmount.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colTotalPaid.setCellValueFactory(new PropertyValueFactory<>("totalPaid"));
        colDueAmount.setCellValueFactory(new PropertyValueFactory<>("dueAmount"));
        table.setItems(reportList);

        loadData();

        btnRefresh.setOnAction(e -> loadData());
        btnHome.setOnAction(e -> {
            BorderPane root = (BorderPane) rootPane.getParent();
            Pane menuPane = loader.getPage("/fxml/report/ReportMenu.fxml");
            root.setCenter(menuPane);
        });
    }

    private void loadData() {
        reportList.clear();

        List<Customer> customers = customerService.getAllCustomer();
        List<Bill> allBills = billService.getAllBills();

        // Group bills by customer ID
        Map<Integer, List<Bill>> billsByCustomer = new HashMap<>();
        for (Bill bill : allBills) {
            if (bill.getCustomer() != null) {
                int custId = bill.getCustomer().getId();
                billsByCustomer.computeIfAbsent(custId, k -> new java.util.ArrayList<>()).add(bill);
            }
        }

        int sr = 1;
        float totalOutstanding = 0, totalCollected = 0;

        for (Customer customer : customers) {
            // Skip Cash customer (ID=1 is typically the default walk-in customer)
            if (customer.getId() != null && customer.getId() == 1) continue;

            List<Bill> customerBills = billsByCustomer.get(customer.getId());
            if (customerBills == null || customerBills.isEmpty()) continue;

            float totalAmount = 0, totalPaid = 0;
            for (Bill bill : customerBills) {
                totalAmount += bill.getGrandtotal() != null ? bill.getGrandtotal() : 0;
                totalPaid += bill.getPaid() != null ? bill.getPaid() : 0;
            }

            float due = totalAmount - totalPaid;
            if (due <= 0) continue; // No outstanding dues

            reportList.add(OutstandingDuesDTO.builder()
                    .sr(sr++)
                    .customerName(customer.getName() != null ? customer.getName() : "N/A")
                    .contact(customer.getContact() != null ? customer.getContact() : "")
                    .totalBills(customerBills.size())
                    .totalAmount(totalAmount)
                    .totalPaid(totalPaid)
                    .dueAmount(due)
                    .build());

            totalOutstanding += due;
            totalCollected += totalPaid;
        }

        lblCustomerCount.setText(String.valueOf(reportList.size()));
        lblTotalOutstanding.setText(String.format("%.2f", totalOutstanding));
        lblTotalCollected.setText(String.format("%.2f", totalCollected));
    }
}
