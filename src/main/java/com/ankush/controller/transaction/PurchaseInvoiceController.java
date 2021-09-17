package com.ankush.controller.transaction;

import com.ankush.data.entities.Item;
import com.ankush.data.entities.PurchaseTransaction;
import com.ankush.data.service.ItemService;
import com.ankush.data.service.PurchaseInvoiceService;
import com.ankush.data.service.PurchasePartyService;
import com.ankush.view.StageManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Popup;
import org.controlsfx.control.textfield.TextFields;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Component
public class PurchaseInvoiceController implements Initializable {
    @Autowired @Lazy
    StageManager stageManager;
    @FXML private SplitPane mainPane;
    @FXML private AnchorPane billingPane;
    @FXML private DatePicker date;
    @FXML private TextField txtInvoiceNo;
    @FXML private TextField txtParty;
    @FXML private TextField txtBarcode;
    @FXML private TextField txtItemName;
    @FXML private TextField txtQty;
    @FXML private ComboBox<String> cmbUnit;
    @FXML private TextField txtRate;
    @FXML private TextField txtAmount;
    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnRemove;
    @FXML private Button btnClear;
    @FXML private TableView<PurchaseTransaction> table;
    @FXML private TableColumn<PurchaseTransaction,Long> coSr;
    @FXML private TableColumn<PurchaseTransaction,String> colBarcode;
    @FXML private TableColumn<PurchaseTransaction,String> colItemName;
    @FXML private TableColumn<PurchaseTransaction,Float> colQty;
    @FXML private TableColumn<PurchaseTransaction,String> colUnit;
    @FXML private TableColumn<PurchaseTransaction,Float> colRate;
    @FXML private TableColumn<PurchaseTransaction,Float> colAmount;
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

    @Autowired
    PurchaseInvoiceService invoiceService;
    @Autowired
    PurchasePartyService partyService;
    @Autowired
    ItemService itemService;

    private ListView listView;
    private ObservableList<String> itemNameList = FXCollections.observableArrayList();

    private Item item;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        item = null;
        cmbUnit.getItems().addAll("KG","NOS");
        TextFields.bindAutoCompletion(txtParty,partyService.getAllPurchasePartyNames());
        //TextFields.bindAutoCompletion(txtItemName,itemService.getAllItemNames());
        addItemNameSearch();

    }
    void setItem()
    {
        if(item!=null)
        {
            txtBarcode.setText(item.getBarcode());
            txtRate.setText(String.valueOf(item.getRate()));
            if(item.getUnit().equals("ik.ga`^."))
               cmbUnit.getSelectionModel().select(0);
            else
                cmbUnit.getSelectionModel().select(1);
        }
    }





    void addItemNameSearch()
    {
        itemNameList.addAll(itemService.getAllItemNames());
        listView = new ListView();
        listView.setStyle("-fx-font:18pt \"Kiran\"");
        listView.setLayoutX(140);
        listView.setLayoutY(142);
        billingPane.getChildren().addAll(listView);
        listView.setVisible(false);
        txtItemName.setOnKeyReleased(e->{
            findItem(txtItemName.getText());
            if(listView.getItems().size()>0)
            {
                listView.getSelectionModel().select(0);
                listView.setVisible(true);
            }
            if(e.getCode()==KeyCode.ENTER){
                if(listView.getItems().size()>0)
                {
                    listView.getSelectionModel().select(0);
                    listView.requestFocus();
                }
                if(txtItemName.getText().equals(listView.getSelectionModel().getSelectedItem()))
                {
                    listView.setVisible(false);
                    item = itemService.getItemByName(txtItemName.getText());
                    System.out.println(item);
                    setItem();
                    txtQty.requestFocus();
                }
            }
            if(e.getCode()==KeyCode.DOWN)
            {
                if(listView.getItems().size()>0)
                {
                    listView.getSelectionModel().select(0);
                    listView.requestFocus();
                }
            }

        });
        txtItemName.setOnMouseClicked(e->{
            findItem(txtItemName.getText());
            listView.setVisible(true);
        });
        listView.setOnKeyReleased(e->{
            String item = String.valueOf(listView.getSelectionModel().getSelectedItems());
            if(e.getCode()== KeyCode.ENTER)
            {
                txtItemName.setText(item.substring(1,item.length()-1));
                listView.setVisible(false);
                txtItemName.requestFocus();
            }
        });
        listView.setOnMouseClicked(e->{
            if(e.getButton()== MouseButton.PRIMARY && e.getClickCount()==2)
            {
                String itemName = String.valueOf(listView.getSelectionModel().getSelectedItems());
                txtItemName.setText(itemName.substring(1,itemName.length()-1));
                item = itemService.getItemByName(txtItemName.getText());
                System.out.println(item);
                setItem();
                listView.setVisible(false);
            }
        });
    }
    void findItem(String find) {
        //cmodel.removeAllElements();
        listView.getItems().clear();
        if(find.equals("")|| find.equals(" "))
        {
            listView.getItems().clear();
            listView.getItems().addAll(itemNameList);
            return;
        }
        try {
            for (int i = 0; i < itemNameList.size(); i++) {
                if (itemNameList.get(i).startsWith(find)) {
                    listView.getItems().add(itemNameList.get(i));
                }
            }
        } catch (Exception e) {
            System.out.println("Error in findItem " + e.getMessage());
            return;
        }
    }
}
