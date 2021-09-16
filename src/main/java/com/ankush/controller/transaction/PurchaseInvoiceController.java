package com.ankush.controller.transaction;

import com.ankush.view.StageManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class PurchaseInvoiceController implements Initializable {
    @Autowired @Lazy
    StageManager stageManager;
    @FXML private SplitPane mainPane;
    @FXML private DatePicker date;
    @FXML private TextField txtInvoiceNo;
    @FXML private TextField txtParty;
    @FXML private TextField txtBarcode;
    @FXML private TextField txtItemName;
    @FXML private TextField txtQty;
    @FXML private ComboBox<?> cmbUnit;
    @FXML private TextField txtRate;
    @FXML private TextField txtAmount;
    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnRemove;
    @FXML private Button btnClear;
    @FXML private TableView<?> table;
    @FXML private TableColumn<?, ?> coSr;
    @FXML private TableColumn<?, ?> colBarcode;
    @FXML private TableColumn<?, ?> colItemName;
    @FXML private TableColumn<?, ?> colQty;
    @FXML private TableColumn<?, ?> colUnit;
    @FXML private TableColumn<?, ?> colRate;
    @FXML private TableColumn<?, ?> colAmount;
    @FXML private TextField txtTransporting;
    @FXML private TextField txtWages;
    @FXML private TextField txtOther;
    @FXML private TextField txtNetTotal;
    @FXML private TextField txtGrandTotal;
    @FXML private TextField txtDicount;
    @FXML private TextField txtPaid;
    @FXML private TextField txtBank;
    @FXML private Button btnSave;
    @FXML private Button btnUpdate2;
    @FXML private Button btnClear2;
    @FXML private Button btnPrint;
    @FXML private Button btnExit;
    @FXML private TableView<?> tableold;
    @FXML private TableColumn<?, ?> colSrNo;
    @FXML private TableColumn<?, ?> colDate;
    @FXML private TableColumn<?, ?> colInvoiceNo;
    @FXML private TableColumn<?, ?> colParty;
    @FXML private TableColumn<?, ?> colTotal;
    @FXML private TableColumn<?, ?> colPaid;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
