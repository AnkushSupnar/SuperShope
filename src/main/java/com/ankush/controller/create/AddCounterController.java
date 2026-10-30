package com.ankush.controller.create;

import com.ankush.config.SpringFXMLLoader;
import com.ankush.data.entities.BillingCounter;
import com.ankush.data.service.BillingCounterService;
import com.ankush.view.AlertNotification;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/** Master for billing counters: each active counter can hold one bill in progress on the billing screen. */
@Component
public class AddCounterController implements Initializable {
    @FXML private AnchorPane pane;
    @FXML private TextField txtCounterNo;
    @FXML private TextField txtName;
    @FXML private CheckBox chkActive;
    @FXML private Button btnSave;
    @FXML private Button btnUpdate;
    @FXML private Button btnClear;
    @FXML private Button btnHome;
    @FXML private TableView<BillingCounter> table;
    @FXML private TableColumn<BillingCounter, Integer> colCounterNo;
    @FXML private TableColumn<BillingCounter, String> colName;
    @FXML private TableColumn<BillingCounter, String> colActive;
    @FXML private TableColumn<BillingCounter, String> colPending;
    @Autowired private BillingCounterService service;
    @Autowired private AlertNotification alert;
    @Autowired private SpringFXMLLoader loader;
    /** id of the counter selected for UPDATE, null when adding */
    private Integer id;
    private final ObservableList<BillingCounter> list = FXCollections.observableArrayList();
    private Map<Integer, double[]> pending = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        id = null;
        colCounterNo.setCellValueFactory(new PropertyValueFactory<>("counterno"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colActive.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActiveCounter() ? "YES" : "NO"));
        colPending.setCellValueFactory(c -> {
            double[] p = pending.get(c.getValue().getCounterno());
            return new SimpleStringProperty(p == null ? "-" : String.format(Locale.ROOT, "%d items, Rs. %.2f", (int) p[0], p[1]));
        });
        table.setItems(list);
        loadCounters();
        txtCounterNo.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d{0,4}")) txtCounterNo.setText(oldValue);
        });
        table.setOnMouseClicked(e -> {
            BillingCounter c = table.getSelectionModel().getSelectedItem();
            if (c == null) return;
            id = c.getId();
            txtCounterNo.setText(String.valueOf(c.getCounterno()));
            txtName.setText(c.getName());
            chkActive.setSelected(c.isActiveCounter());
        });
        btnSave.setOnAction(e -> save(false));
        btnUpdate.setOnAction(e -> save(true));
        btnClear.setOnAction(e -> clear());
        btnHome.setOnAction(e -> {
            BorderPane root = (BorderPane) pane.getParent();
            root.setCenter(loader.getPage("/fxml/create/CreateMenu.fxml"));
        });
        if (list.isEmpty()) txtCounterNo.setText("1");
        else txtCounterNo.setText(String.valueOf(list.get(list.size() - 1).getCounterno() + 1));
        if (txtName.getText().isEmpty()) txtName.setText("Counter " + txtCounterNo.getText());
    }

    private void loadCounters() {
        service.ensureDefaultCounter();
        pending = service.getPendingSummary();
        list.setAll(service.getAllCounters());
        table.refresh();
    }

    private void save(boolean update) {
        if (update && id == null) {
            alert.showError("Select a counter in the table first");
            return;
        }
        if (txtCounterNo.getText().isEmpty()) {
            alert.showError("Enter counter number");
            txtCounterNo.requestFocus();
            return;
        }
        BillingCounter counter = BillingCounter.builder()
                .id(update ? id : null)
                .counterno(Integer.parseInt(txtCounterNo.getText()))
                .name(txtName.getText())
                .active(chkActive.isSelected())
                .build();
        try {
            service.saveCounter(counter);
            alert.showSuccess(update ? "Counter updated" : "Counter saved");
            loadCounters();
            clear();
        } catch (IllegalArgumentException ex) {
            alert.showError(ex.getMessage());
        }
    }

    private void clear() {
        id = null;
        table.getSelectionModel().clearSelection();
        int next = list.stream().mapToInt(BillingCounter::getCounterno).max().orElse(0) + 1;
        txtCounterNo.setText(String.valueOf(next));
        txtName.setText("Counter " + next);
        chkActive.setSelected(true);
    }
}
