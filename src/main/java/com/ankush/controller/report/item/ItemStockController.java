package com.ankush.controller.report.item;

import com.ankush.Main;
import com.ankush.data.entities.ItemStock;
import com.ankush.data.service.ItemStockService;
import com.ankush.customUI.AutoCompleteTextField;
import com.ankush.view.AlertNotification;
import com.ankush.view.StageManager;
import impl.org.controlsfx.skin.AutoCompletePopup;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import org.controlsfx.control.textfield.TextFields;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

@Component
public class ItemStockController implements Initializable {
    @Autowired @Lazy
    StageManager stageManager;
    @FXML private AnchorPane mainPane;
    @FXML private TextField txtItemName;
    @FXML private Button btnView;
    @FXML private Button btnViewAll;
    @FXML private Button btnHome;
    @FXML private TableView<ItemStock> table;
    @FXML private TableColumn<ItemStock,Long> colSrNo;
    @FXML private TableColumn<ItemStock,String> colItemName;
    @FXML private TableColumn<ItemStock,String> colUnit;
    @FXML private TableColumn<ItemStock,String> colBarcode;
    @FXML private TableColumn<ItemStock,Number> colStock;
    @Autowired
    private ItemStockService service;
    private ObservableList<ItemStock>list = FXCollections.observableArrayList();
    @Autowired
    private AlertNotification alert;
    private AutoCompleteTextField autoCompleteItemName;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Font.loadFont(Main.class.getResource("/fxml/font/kiran.ttf").toExternalForm(),10);
        colSrNo.setCellValueFactory(new PropertyValueFactory<>("id"));
        colItemName.setCellValueFactory(new PropertyValueFactory<>("itemname"));
        colUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        colBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        list.addAll(service.getAllItemStock());
        table.setItems(list);
        autoCompleteItemName = new AutoCompleteTextField(txtItemName, service.getAllItemNames(), Font.font("Kiran", 20));
        btnView.setOnAction(e->view());
        btnViewAll.setOnAction(e->viewAll());
        btnHome.setOnAction(e->mainPane.setVisible(false));

    }

    private void viewAll() {
        list.clear();
        list.addAll(service.getAllItemStock());
        table.refresh();
    }

    private void view() {
        if(txtItemName.getText().isEmpty()) {
            alert.showError("Enter Item Name");
            txtItemName.requestFocus();
            return;
        }
        list.clear();
        list.add(service.getItemStockByItemname(txtItemName.getText()));
        table.refresh();
    }
}
