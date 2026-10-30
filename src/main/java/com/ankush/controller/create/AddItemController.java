package com.ankush.controller.create;

import com.ankush.config.SpringFXMLLoader;
import com.ankush.customUI.AutoCompleteTextField;
import com.ankush.data.entities.Item;
import com.ankush.data.service.ItemService;
import com.ankush.view.AlertNotification;
import com.ankush.view.StageManager;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
    @FXML private TextField txtShortCode;
    @FXML private ComboBox<String> cmbCategory;
    @FXML private TextField txtSearchName;
    @FXML private CheckBox chkActive;
    @FXML private TableColumn<Item,String> colShortCode;
    @FXML private TableColumn<Item,String> colCategory;
    @FXML private TableColumn<Item,String> colActive;
    @FXML private Button btnAutoCodes;
    @FXML private Button btnHideUnused;
    @FXML private Button btnMerge;
    private static final int UNUSED_DAYS = 180;
    //for item name Search
    @FXML private AnchorPane sidePane;

    @Autowired ItemService itemService;
    @Autowired private SpringFXMLLoader loader;

    private ToggleGroup  group;
    @Autowired
    ItemService service;
    @Autowired
    private AlertNotification alert;
    private ObservableList<Item>list = FXCollections.observableArrayList();
    private AutoCompleteTextField autoCompleteSearch;
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
    colShortCode.setCellValueFactory(new PropertyValueFactory<>("shortCode"));
    colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
    colActive.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().isActiveItem() ? "Yes" : "No"));
    txtShortCode.textProperty().addListener((obs, oldValue, newValue) -> {
        if (!newValue.matches("\\d{0,6}")) {
            txtShortCode.setText(oldValue);
        }
    });
    cmbCategory.getItems().setAll(itemService.getCategories());
        autoCompleteSearch = new AutoCompleteTextField(txtSearch, itemService.getAllItemNames(), Font.font("Kiran", 20));
        autoCompleteSearch.setUseContainsFilter(true);
        autoCompleteSearch.setSearchKeys(itemService.getSearchKeys());
        autoCompleteSearch.setOnSelectionCallback(selectedName -> show());
        btnAutoCodes.setOnAction(e -> autoAssignCodes());
        btnHideUnused.setOnAction(e -> hideUnusedItems());
        btnMerge.setOnAction(e -> mergeItems());


        btnSave.setOnAction(e->save());
    btnUpdate.setOnAction(e->update());
    btnClear.setOnAction(e->clear());
    btnExit.setOnAction(e -> {
        BorderPane root = (BorderPane) mainPane.getParent();
        root.setCenter(loader.getPage("/fxml/create/CreateMenu.fxml"));
    });
    btnShowAll.setOnAction(e->{
        list.clear();
        list.addAll(service.getAllItems());
        table.refresh();
    });
    btnShow.setOnAction(e->show());
    }

    private void show() {
        Item found = itemService.getItemByName(txtSearch.getText());
        if (found == null) found = itemService.getItemByShortCode(txtSearch.getText());
        list.clear();
        if (found != null) list.add(found);
    }

    private void autoAssignCodes() {
        if (!alert.showConfirmation("Auto Short Codes",
                "Give a short code (101, 102, ...) to every active item that has none?\n" +
                "Best-selling items get the smallest codes. Existing codes are not changed.")) {
            return;
        }
        int count = itemService.assignMissingShortCodes();
        alert.showSuccess(count + " items got a short code");
        refreshAfterBulkChange();
    }

    private void hideUnusedItems() {
        ChoiceDialog<Integer> period = new ChoiceDialog<>(UNUSED_DAYS, 60, 90, UNUSED_DAYS, 365);
        period.setTitle("Hide Unused Items");
        period.setHeaderText("Hide items that were neither sold nor purchased in the last ... days");
        period.setContentText("Days:");
        Integer days = period.showAndWait().orElse(null);
        if (days == null) return;
        List<Item> unused = itemService.findUnusedItems(days);
        if (unused.isEmpty()) {
            alert.showSuccess("No unused items found");
            return;
        }
        if (!alert.showConfirmation("Hide Unused Items",
                unused.size() + " items were neither sold nor purchased in the last " + days + " days.\n" +
                "Mark them inactive so they no longer appear in billing and purchase search?\n" +
                "(Nothing is deleted. Tick 'Active' again in this screen, or purchase the item, to bring it back.)")) {
            return;
        }
        itemService.deactivate(unused);
        alert.showSuccess(unused.size() + " items hidden from billing");
        refreshAfterBulkChange();
    }

    /**
     * MERGE: the item selected in the table is kept; ticked duplicates (e.g. "10 gram" and "9 gram" of the same
     * product) are joined into it and made inactive. Their old names still find it, their barcode / short code
     * bring it up, and their stock is added to it. Old bills are not changed.
     */
    private void mergeItems() {
        Item keep = table.getSelectionModel().getSelectedItem();
        if (keep == null) {
            alert.showError("Search the item to KEEP, click its row in the table, then click MERGE");
            return;
        }
        VBox choices = new VBox(6);
        for (String name : itemService.findSimilarNames(keep.getItemname())) {
            if (!name.equals(keep.getItemname())) choices.getChildren().add(mergeChoice(name));
        }
        TextField other = new TextField();
        other.setFont(Font.font("Kiran", 18));
        other.setPromptText("Other item name");
        AutoCompleteTextField otherSearch = new AutoCompleteTextField(other, itemService.getAllItemNames(), Font.font("Kiran", 20));
        otherSearch.setUseContainsFilter(true);
        Button addOther = new Button("Add to list");
        addOther.setOnAction(e -> {
            String name = other.getText().trim();
            if (itemService.getItemByName(name) == null || name.equals(keep.getItemname())) return;
            choices.getChildren().add(mergeChoice(name));
            other.clear();
        });
        Label keepName = new Label(keep.getItemname());
        keepName.setFont(Font.font("Kiran", 22));
        ScrollPane scroll = new ScrollPane(choices);
        scroll.setPrefSize(520, 260);
        scroll.setFitToWidth(true);
        VBox content = new VBox(10, new Label("Keep this item:"), keepName,
                new Label("Tick the duplicates to join into it:"), scroll, new HBox(8, other, addOther),
                new Label("Ticked items become inactive. Their old name, barcode and short code will bring up the kept item.\n"
                        + "Their stock is added to it. Old bills are not changed."));

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Merge Items");
        dialog.getDialogPane().setContent(content);
        ButtonType merge = new ButtonType("Merge", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(merge, ButtonType.CANCEL);
        if (dialog.showAndWait().orElse(ButtonType.CANCEL) != merge) return;

        List<Item> duplicates = choices.getChildren().stream()
                .map(n -> (CheckBox) n)
                .filter(CheckBox::isSelected)
                .map(c -> itemService.getItemByName((String) c.getUserData()))
                .filter(i -> i != null && !i.getId().equals(keep.getId()))
                .collect(Collectors.toList());
        if (duplicates.isEmpty()) {
            alert.showError("No item ticked");
            return;
        }
        int count = itemService.mergeItems(keep, duplicates);
        alert.showSuccess(count + " items merged");
        refreshAfterBulkChange();
    }

    private CheckBox mergeChoice(String name) {
        CheckBox box = new CheckBox(name);
        box.setFont(Font.font("Kiran", 18));
        box.setUserData(name);
        return box;
    }

    private void refreshAfterBulkChange() {
        autoCompleteSearch.setSearchKeys(itemService.getSearchKeys());
        list.clear();
        list.addAll(service.getAllItems());
        table.refresh();
    }

    private void clear() {
        txtItemName.setText("");
        txtPrice.setText("");
        txtRate.setText("");
        txtBarCode.setText("");
        rdbtnKg.setSelected(false);
        rdbtnNos.setSelected(false);
        txtSailingPrice.setText("");
        txtShortCode.setText("");
        cmbCategory.setValue(null);
        cmbCategory.getEditor().setText("");
        txtSearchName.setText("");
        chkActive.setSelected(true);
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
        txtShortCode.setText(item.getShortCode() == null ? "" : item.getShortCode());
        cmbCategory.setValue(item.getCategory());
        txtSearchName.setText(item.getSearchName() == null ? "" : item.getSearchName());
        chkActive.setSelected(item.isActiveItem());

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
            String shortCode = txtShortCode.getText().trim();
            item.setShortCode(shortCode.isEmpty() ? null : shortCode);
            item.setCategory(categoryText().isEmpty() ? null : categoryText());
            item.setSearchName(txtSearchName.getText().trim().isEmpty() ? null : txtSearchName.getText().trim());
            item.setActive(chkActive.isSelected());
            int flag=service.saveItem(item);
            if(item.getCategory()!=null && !cmbCategory.getItems().contains(item.getCategory()))
                cmbCategory.getItems().add(item.getCategory());
            autoCompleteSearch.setSearchKeys(itemService.getSearchKeys());
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
            String shortCode = txtShortCode.getText().trim();
            if(!shortCode.isEmpty())
            {
                Long currentId = id==0 ? null : id;
                if(service.isShortCodeTaken(shortCode, currentId))
                {
                    alert.showError("Short Code "+shortCode+" is already used by another item");
                    txtShortCode.requestFocus();
                    return false;
                }
                Item sameBarcode = service.getItemByBarcode(shortCode);
                if(sameBarcode!=null && !sameBarcode.getId().equals(currentId))
                {
                    alert.showError("Short Code "+shortCode+" is the barcode of another item, choose another");
                    txtShortCode.requestFocus();
                    return false;
                }
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
    // editable ComboBox: the typed text is in the editor until focus leaves it
    private String categoryText() {
        String typed = cmbCategory.getEditor().getText();
        if (typed != null && !typed.isBlank()) return typed.trim();
        return cmbCategory.getValue() == null ? "" : cmbCategory.getValue().trim();
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
