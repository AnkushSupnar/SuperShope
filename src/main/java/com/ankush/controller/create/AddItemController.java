package com.ankush.controller.create;

import com.ankush.data.entities.Item;
import com.ankush.data.service.ItemService;
import com.ankush.view.AlertNotification;
import com.ankush.view.StageManager;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Callback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Component
public class AddItemController implements Initializable {
    @Autowired @Lazy
    StageManager stageManager;

    @FXML private AnchorPane mainPane;
    @FXML private TextField txtItemName;
    @FXML private TextField txtBarCode;
    @FXML private RadioButton rdbtnKg;
    @FXML private RadioButton rdbtnNos;
    @FXML private TextField txtPrice;
    @FXML private TextField txtSailingPrice;
    @FXML private TextField txtRate;
    @FXML private Button btnSave;
    @FXML private Button btnUpdate;
    @FXML private Button btnClear;
    @FXML private Button btnExit;
    @FXML private TableView<Item> table;
    @FXML private TableColumn<Item, Long> colSr;
    @FXML private TableColumn<Item,String> colName;
    @FXML private TableColumn<Item,String> colBarcode;
    @FXML private TableColumn<Item,String> colUnit;
    @FXML private TableColumn<Item,Float> colPrize;
    @FXML private TableColumn<Item,Float> colSailingPrice;
    @FXML private TableColumn<Item,Float> colRate;
    @FXML private Button btnShowAll;
    @FXML private TextField txtSearch;
    @FXML private Button btnShow;
    //for item name Search
    @FXML private AnchorPane sidePane;

    @Autowired ItemService itemService;

    private ToggleGroup  group;
    @Autowired
    ItemService service;
    @Autowired
    private AlertNotification alert;
    private ObservableList<Item>list = FXCollections.observableArrayList();
    private Long id;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    this.id= Long.valueOf(0);
    group = new ToggleGroup();
    rdbtnNos.setToggleGroup(group);
    rdbtnKg.setToggleGroup(group);
    colSr.setCellValueFactory(new PropertyValueFactory<>("id"));
    colName.setCellValueFactory(new PropertyValueFactory<>("itemname"));
    colBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
    colUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
    colPrize.setCellValueFactory(new PropertyValueFactory<>("price"));
    colRate.setCellValueFactory(new PropertyValueFactory<>("rate"));
    colSailingPrice.setCellValueFactory(new PropertyValueFactory<>("sailingprice"));
    //list.addAll(service.getAllItems());
    table.setItems(list);
    txtSailingPrice.textProperty().addListener(new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            if (!newValue.matches("\\d{0,100}([\\.]\\d{0,4})?")) {
                txtSailingPrice.setText(oldValue);
            }
        }
    });
    txtRate.textProperty().addListener(new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            if (!newValue.matches("\\d{0,100}([\\.]\\d{0,4})?")) {
                txtRate.setText(oldValue);
            }
        }
    });
    txtPrice.textProperty().addListener(new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            if (!newValue.matches("\\d{0,100}([\\.]\\d{0,4})?")) {
                txtPrice.setText(oldValue);
            }
        }
    });
    addItemSearch();


        btnSave.setOnAction(e->save());
    btnUpdate.setOnAction(e->update());
    btnClear.setOnAction(e->clear());
    btnExit.setOnAction(e->mainPane.setVisible(false));
    btnShowAll.setOnAction(e->{
        list.clear();
        list.addAll(service.getAllItems());
        table.refresh();
    });
    btnShow.setOnAction(e->show());
    }

    private void show() {
        list.clear();
        list.add(itemService.getItemByName(txtSearch.getText()));
    }

    private void addItemSearch() {
        ListView listView;
        ObservableList<String> itemNameSearch = FXCollections.observableArrayList();
        itemNameSearch.addAll(itemService.getAllItemNames());
        listView = new ListView();
        listView.setStyle("-fx-font:18pt \"Kiran\"");
        listView.setLayoutX(txtSearch.getLayoutX());
        listView.setLayoutY(txtSearch.getLayoutY()+40);
        listView.setPrefWidth(txtSearch.getPrefWidth());
        sidePane.getChildren().addAll(listView);
        listView.setVisible(false);
        txtSearch.setOnKeyReleased(e->{
            listView.setVisible(true);
            if(txtSearch.getText()==null &&txtSearch.getText().isEmpty() || txtSearch.getText().equals("") || txtSearch.getText().equals(" ")) {
                listView.getItems().clear();
                listView.getItems().addAll(itemNameSearch);
                return;
            }
                listView.getItems().clear();
                listView.getItems().addAll(searchList(txtSearch.getText(),itemNameSearch));
            if(e.getCode()== KeyCode.ENTER ||e.getCode()== KeyCode.DOWN) {
                System.out.println("Pressed "+e.getCode());
                if (listView.getItems().size() > 0) {
                    listView.getSelectionModel().select(0);
                    listView.requestFocus();
                }
            }
        });
        txtSearch.setOnMouseClicked(e->{
            if(txtSearch.getText()!=null)
            {
                if(txtSearch.getText()==null &&txtSearch.getText().isEmpty() || txtSearch.getText().equals("") || txtSearch.getText().equals(" ")) {
                    listView.getItems().clear();
                    listView.getItems().addAll(itemNameSearch);
                    listView.setVisible(true);
                    return;
                }
                listView.getItems().clear();
                listView.getItems().addAll(searchList(txtSearch.getText(),itemNameSearch));
                if(!listView.getItems().isEmpty()) listView.setVisible(true);
            }
        });
        listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue observableValue, String o, String t1) {
                if(listView.isFocused())
                txtSearch.setText(t1);
            }
        });
        listView.setOnKeyReleased(e->{
            if(e.getCode()==KeyCode.BACK_SPACE)
            {
                txtSearch.requestFocus();
            }
            if(e.getCode()==KeyCode.ENTER)
            {
                listView.setVisible(false);
                btnShow.requestFocus();
            }
        });
        listView.setOnMouseClicked(e->{
            if(e.getButton()== MouseButton.PRIMARY && e.getClickCount()==2)
                listView.setVisible(false);
        });
        listView.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
                if(t1) {
                    //in focus
                }
                else{
                    if(txtSearch.isFocused())
                        return;
                    else
                        listView.setVisible(false);
                }
            }
        });
    }


    private List<String> searchList(String text, ObservableList<String> itemNameSearch) {
        if(text==null)
            return itemNameSearch;


        List<String> searchWords = Arrays.asList(text.trim().split(" "));
        return itemNameSearch.stream().filter(input->{
            return searchWords.stream().allMatch(word->
                    input.toLowerCase().startsWith(word.toLowerCase()));
        }).collect(Collectors.toList());
    }

    private void clear() {
        txtItemName.setText("");
        txtPrice.setText("");
        txtRate.setText("");
        txtBarCode.setText("");
        rdbtnKg.setSelected(false);
        rdbtnNos.setSelected(false);
        txtSailingPrice.setText("");
        id= Long.valueOf(0);
    }

    private void update()
    {
        if(table.getSelectionModel().getSelectedItem()==null)
        {
            return;
        }
        Item item = table.getSelectionModel().getSelectedItem();
        txtItemName.setText(item.getItemname());
        txtRate.setText(""+item.getRate());
        txtPrice.setText(""+item.getPrice());
        txtBarCode.setText(item.getBarcode());
        txtSailingPrice.setText(String.valueOf(item.getSailingprice()));
        if(item.getUnit().equals("ik.ga`^."))rdbtnKg.setSelected(true);
        else rdbtnNos.setSelected(true);

        id=item.getId();
    }

    private void save() {
        try {

            if(!validate())
            {
                return;
            }
            Item item = new Item();
            if(id!=0)item.setId(id);
            item.setItemname(txtItemName.getText().trim());
            item.setBarcode(txtBarCode.getText().trim());
            item.setPrice(Float.parseFloat(txtPrice.getText().trim()));
            item.setRate(Float.parseFloat(txtRate.getText().trim()));
            item.setSailingprice(Float.parseFloat(txtSailingPrice.getText()));
            if(rdbtnKg.isSelected())
                item.setUnit("ik.ga`^.");
            else
                item.setUnit("naga");
            int flag=service.saveItem(item);
            if(flag==1)
            {
                alert.showSuccess("Item Save Success ");
                addInList(item);
            }else
            if(flag==2)
            {
                alert.showSuccess("Item Update Success");
                addInList(item);
            }
            else
                alert.showError("Error in Item Saving");
                clear();
        }catch(Exception e)
        {
            alert.showError("Error in Saving Item "+e.getMessage());
        }
    }

    private void addInList(Item item) {
        int index=-1;
        for(int i=0;i<list.size();i++)
        {
            if(list.get(i).getId()==item.getId())
            {
                index=i;
                break;
            }
        }
        if(index==-1)
        {
            list.add(item);
        }
        else {
            list.remove(index);
            list.add(index,item);
        }
    }

    private boolean validate() {
        try {
            if(txtItemName.getText().isEmpty())
            {
                alert.showError("Enter Item Name");
                txtItemName.requestFocus();
                return false;
            }
            if(id==0 && service.getItemByName(txtItemName.getText())!=null)
            {
                alert.showError("Item Name Already Exist Choose another One");
                txtItemName.requestFocus();
                return false;
            }
            if(txtBarCode.getText().isEmpty())
            {
                txtBarCode.setText("");
            }
            if(id==0 && service.getItemByItemnameAndBarcode(txtItemName.getText().trim(),txtBarCode.getText().trim())!=null)
            {
                alert.showError("Item Already Exist");
                txtItemName.requestFocus();
                return false;
            }
            if(id==0 && !txtBarCode.getText().equals("0") && service.getItemByBarcode(txtBarCode.getText().trim())!=null )
            {
                alert.showError("This Bar code Already Exist With another Item Name");
                txtBarCode.requestFocus();
                return false;
            }
            if(!rdbtnKg.isSelected() && !rdbtnNos.isSelected())
            {
                alert.showError("Select Item Unit");

                return false;
            }
            if(txtPrice.getText().isEmpty())
            {
                alert.showError("Enter Price");
                txtPrice.requestFocus();
                return false;
            }
            if(txtRate.getText().isEmpty())
            {
                alert.showError("Enter Sailing Rate");
                txtRate.requestFocus();
                return false;
            }
            if(!isNumber(txtPrice.getText()))
            {
                alert.showError("Enter Price In Digit");
                txtPrice.requestFocus();
                return false;
            }
            if(!isNumber(txtRate.getText()))
            {
                alert.showError("Enter Rate In Digit");
                txtRate.requestFocus();
                return false;
            }
            if(txtSailingPrice.getText().isEmpty())
            {
                alert.showError("Enter Item Sailing Price");
                txtSailingPrice.requestFocus();
                return false;
            }
            if(!isNumber(txtSailingPrice.getText()))
            {
                alert.showError("Enter Item Sailing Price in Digit");
                txtSailingPrice.requestFocus();
                return false;
            }
            return true;
        }catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }
    private boolean isNumber(String num) {
        if (num == null) {
            return false;
        }
        try {
            Float.parseFloat(num);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
