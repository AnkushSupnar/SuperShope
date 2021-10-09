package com.ankush.controller.transaction;

import com.ankush.data.service.ItemStockService;
import com.ankush.view.StageManager;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class BillingController  implements Initializable {
    @Autowired @Lazy
    StageManager stageManager;
    @FXML private AnchorPane leftPane;
    @FXML private AnchorPane rootPane;

    @FXML private DatePicker date;
    @FXML private TextField txtCustomerName;
    @FXML private TextField txtCustomerMobile;
    @FXML private TextField txtBarcode;
    @FXML private TextField txtItemName;
    @FXML private ComboBox<String> cmbUnit;
    @FXML private TextField txtQty;
    @FXML private TextField txtRate;
    @FXML private TextField txtAmount;
    @FXML private Button btnAdd;
    @FXML private Button btnRemove;
    @FXML private Button btnUpdate;
    @FXML private Button btnClear;
    @FXML private TableView<?> table;
    @FXML private TableColumn<?, ?> colSrNo;
    @FXML private TableColumn<?, ?> colBarcode;
    @FXML private TableColumn<?, ?> colItemName;
    @FXML private TableColumn<?, ?> colUnit;
    @FXML private TableColumn<?, ?> colQty;
    @FXML private TableColumn<?, ?> colRate;
    @FXML private TableColumn<?, ?> colPrice;
    @FXML private TableColumn<?, ?> colAmount;
    @FXML private Button btnSave;
    @FXML private Button btnPrint;
    @FXML private Button btnHome;
    @FXML private TextField txtNetTotal;
    @FXML private TextField txtOther;
    @FXML private TextField txtGrandTotal;
    @FXML private TextField txtMrp;
    @FXML private TextField txtDiscount;
    @FXML private TextField txtPayble;
    @FXML private TextField txtRecived;
    @FXML private TextField txtRemaining;
    private ListView<String> listItem;
    @Autowired
    private ItemStockService stockService;
    private ObservableList<String>itemNameList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        listItem = new ListView();
        listItem.setStyle("-fx-font:18pt \"Kiran\"");
        listItem.getItems().addAll(stockService.getAllItemNames());
        listItem.setLayoutX(2);
        listItem.setLayoutY(40);
        listItem.setPrefWidth(240);
        listItem.prefHeightProperty().bind(Bindings.divide(leftPane.heightProperty(),1.0));
        leftPane.getChildren().addAll(listItem);


    }
}
