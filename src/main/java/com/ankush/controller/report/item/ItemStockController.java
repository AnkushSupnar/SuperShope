package com.ankush.controller.report.item;

import com.ankush.Main;
import com.ankush.data.entities.ItemStock;
import com.ankush.data.service.ItemStockService;
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
    private ListView listView;
    private ObservableList<String> itemNameList = FXCollections.observableArrayList();
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
        addItemNameSearch();
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
    void addItemNameSearch()
    {
        itemNameList.addAll(service.getAllItemNames());
        listView = new ListView();
        listView.setStyle("-fx-font:18pt \"Kiran\"");
        listView.setLayoutX(txtItemName.getLayoutX());
        listView.setLayoutY(txtItemName.getLayoutY()+33);
        mainPane.getChildren().addAll(listView);
        listView.setVisible(false);
        txtItemName.setOnKeyReleased(e->{
            findItem(txtItemName.getText());
            if(listView.getItems().size()>0)
            {
                listView.getSelectionModel().select(0);
                listView.setVisible(true);
            }
            if(e.getCode()== KeyCode.ENTER){
                if(listView.getItems().size()>0)
                {
                    listView.getSelectionModel().select(0);
                    listView.requestFocus();
                }
                if(txtItemName.getText().equals(listView.getSelectionModel().getSelectedItem()))
                {
                    listView.setVisible(false);
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
