package com.ankush.controller.create;

import com.ankush.config.SpringFXMLLoader;
import com.ankush.data.entities.Item;
import com.ankush.data.service.ItemService;
import com.ankush.view.AlertNotification;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Component
public class SetSellingRateController implements Initializable {

    @Autowired
    private ItemService itemService;
    @Autowired
    private AlertNotification alert;
    @Autowired
    private SpringFXMLLoader loader;

    // Root
    @FXML private AnchorPane rootPane;

    // Header
    @FXML private Label lblItemCount;

    // Search fields (Billing style)
    @FXML private TextField txtBarcode;
    @FXML private TextField txtItemName;
    @FXML private Button btnShowAll;

    // Table
    @FXML private TableView<Item> table;
    @FXML private TableColumn<Item, Long> colSr;
    @FXML private TableColumn<Item, String> colName;
    @FXML private TableColumn<Item, String> colUnit;
    @FXML private TableColumn<Item, Float> colRate;
    @FXML private TableColumn<Item, Float> colSellingPrice;

    // Edit panel
    @FXML private VBox editPanel;
    @FXML private Label lblItemName;
    @FXML private Label lblCurrentRate;
    @FXML private Label lblCurrentSellingPrice;
    @FXML private Label lblProfit;
    @FXML private VBox profitBox;
    @FXML private TextField txtNewSellingPrice;
    @FXML private Button btnSave;
    @FXML private Button btnNext;
    @FXML private Button btnClear;
    @FXML private Button btnBack;

    private ObservableList<Item> list = FXCollections.observableArrayList();
    private ObservableList<String> itemNameSearch = FXCollections.observableArrayList();
    private ListView<String> listView;
    private Item selectedItem;
    private Item item;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Setup table columns
        colSr.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("itemname"));
        colUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        colRate.setCellValueFactory(new PropertyValueFactory<>("rate"));
        colSellingPrice.setCellValueFactory(new PropertyValueFactory<>("sailingprice"));

        // Load all items on startup
        loadAllItems();
        table.setItems(list);

        // Numeric-only validation on selling price input
        txtNewSellingPrice.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d{0,100}([\\.]\\d{0,4})?")) {
                    txtNewSellingPrice.setText(oldValue);
                }
            }
        });

        // Live profit calculation as user types new price
        txtNewSellingPrice.textProperty().addListener((obs, oldVal, newVal) -> {
            updateProfitDisplay();
        });

        // Table row selection -> show edit panel
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectItem(newVal);
            }
        });

        // Double-click table row to select and focus price field
        table.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                Item clickedItem = table.getSelectionModel().getSelectedItem();
                if (clickedItem != null) {
                    selectItem(clickedItem);
                }
            }
        });

        // Enter key on price field -> save
        txtNewSellingPrice.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                save();
            }
        });

        // Button actions
        btnSave.setOnAction(e -> save());
        btnNext.setOnAction(e -> selectNextItem());
        btnClear.setOnAction(e -> closeEditPanel());
        btnBack.setOnAction(e -> {
            BorderPane root = (BorderPane) rootPane.getParent();
            root.setCenter(loader.getPage("/fxml/create/CreateMenu.fxml"));
        });
        btnShowAll.setOnAction(e -> {
            loadAllItems();
            txtItemName.setText("");
            txtBarcode.setText("");
        });

        // ===== Barcode search (real-time, same as Billing) =====
        txtBarcode.setOnAction(e -> barcodeAction());
        txtBarcode.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                item = null;
                if (!txtBarcode.getText().equals("")) {
                    item = itemService.getItemByBarcode(txtBarcode.getText());
                    if (item != null) {
                        setItemFromSearch(item);
                    }
                }
            }
        });

        // ===== Item name search with autocomplete (same as Billing) =====
        addItemNameSearch();
    }

    // ===== Barcode action (Enter key on barcode field) =====
    private void barcodeAction() {
        if (txtBarcode.getText().isEmpty()) {
            txtItemName.requestFocus();
            return;
        }
        item = itemService.getItemByBarcode(txtBarcode.getText());
        if (item != null) {
            setItemFromSearch(item);
        }
    }

    // ===== When item found via search, filter table and select it =====
    private void setItemFromSearch(Item foundItem) {
        if (foundItem == null) return;
        txtItemName.setText(foundItem.getItemname());
        txtBarcode.setText(foundItem.getBarcode());

        // Filter table to show this item and select it
        list.clear();
        list.add(foundItem);
        table.refresh();
        updateItemCount();
        table.getSelectionModel().select(0);
    }

    // ===== Item name autocomplete (Billing pattern) =====
    void addItemNameSearch() {
        itemNameSearch.addAll(itemService.getAllItemNames());
        listView = new ListView<>();
        listView.setStyle("-fx-font:18pt \"Kiran\"");
        listView.setLayoutX(225);
        listView.setLayoutY(165);
        rootPane.getChildren().add(listView);
        listView.setVisible(false);

        // Key released on item name field
        txtItemName.setOnKeyReleased(e -> {
            findItem(txtItemName.getText());
            if (listView.getItems().size() > 0) {
                listView.getSelectionModel().select(0);
                listView.setVisible(true);
            }
            // ENTER - select item
            if (e.getCode() == KeyCode.ENTER) {
                if (listView.getItems().size() > 0) {
                    listView.getSelectionModel().select(0);
                    listView.requestFocus();
                }
                if (txtItemName.getText().equals(listView.getSelectionModel().getSelectedItem())) {
                    item = itemService.getItemByName(txtItemName.getText());
                    listView.setVisible(false);
                    setItemFromSearch(item);
                }
            }
            // DOWN ARROW - navigate to dropdown
            if (e.getCode() == KeyCode.DOWN) {
                if (listView.getItems().size() > 0) {
                    listView.getSelectionModel().select(0);
                    listView.requestFocus();
                }
            }
        });

        // Mouse click on item name field - show dropdown
        txtItemName.setOnMouseClicked(e -> {
            findItem(txtItemName.getText());
            listView.setVisible(true);
        });

        // Enter key on dropdown list - select item name
        listView.setOnKeyReleased(e -> {
            String selectedName = String.valueOf(listView.getSelectionModel().getSelectedItems());
            if (e.getCode() == KeyCode.ENTER) {
                txtItemName.setText(selectedName.substring(1, selectedName.length() - 1));
                listView.setVisible(false);
                txtItemName.requestFocus();
            }
        });

        // Double-click on dropdown list - select and search
        listView.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                String selectedName = String.valueOf(listView.getSelectionModel().getSelectedItems());
                txtItemName.setText(selectedName.substring(1, selectedName.length() - 1));
                setItemFromSearch(itemService.getItemByName(txtItemName.getText()));
                listView.setVisible(false);
            }
        });

        // Focus listeners - show/hide dropdown
        txtItemName.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
                if (t1) {
                    findItem(txtItemName.getText());
                    listView.setVisible(true);
                } else {
                    if (listView.isFocused())
                        return;
                    else
                        listView.setVisible(false);
                }
            }
        });

        listView.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
                if (t1) {
                    // in focus
                } else {
                    if (txtItemName.isFocused())
                        return;
                    else
                        listView.setVisible(false);
                }
            }
        });
    }

    // ===== Filter item names (same as Billing findItem) =====
    void findItem(String find) {
        listView.getItems().clear();
        if (find == null || find.equals("") || find.trim().equals("")) {
            listView.getItems().clear();
            listView.getItems().addAll(itemNameSearch);
            return;
        } else {
            listView.getItems().clear();
        }

        try {
            for (int i = 0; i < itemNameSearch.size(); i++) {
                if (itemNameSearch.get(i).toLowerCase().startsWith(find.toLowerCase())) {
                    listView.getItems().add(itemNameSearch.get(i));
                }
            }
        } catch (Exception e) {
            System.out.println("Error in findItem " + e.getMessage());
        }
    }

    // ===== Load all items into table =====
    private void loadAllItems() {
        list.clear();
        list.addAll(itemService.getAllItems());
        table.refresh();
        updateItemCount();
    }

    private void updateItemCount() {
        lblItemCount.setText(list.size() + " items");
    }

    // ===== Select item in edit panel =====
    private void selectItem(Item item) {
        selectedItem = item;

        // Show edit panel
        editPanel.setVisible(true);
        editPanel.setManaged(true);

        // Fill item details
        lblItemName.setText(item.getItemname());
        lblCurrentRate.setText(String.valueOf(item.getRate()));
        lblCurrentSellingPrice.setText(String.valueOf(item.getSailingprice()));

        // Pre-fill current selling price so user can just modify it
        txtNewSellingPrice.setText(String.valueOf(item.getSailingprice()));
        txtNewSellingPrice.requestFocus();
        txtNewSellingPrice.selectAll();

        updateProfitDisplay();
    }

    private void updateProfitDisplay() {
        if (selectedItem == null) return;
        try {
            String priceText = txtNewSellingPrice.getText();
            if (priceText == null || priceText.isEmpty()) {
                lblProfit.setText("0.0");
                setProfitStyle(0);
                return;
            }
            float newPrice = Float.parseFloat(priceText);
            float profit = newPrice - selectedItem.getRate();
            lblProfit.setText(String.format("%.2f", profit));
            setProfitStyle(profit);
        } catch (NumberFormatException e) {
            lblProfit.setText("--");
        }
    }

    private void setProfitStyle(float profit) {
        if (profit > 0) {
            lblProfit.setStyle("-fx-text-fill: #2E7D32; -fx-font-size: 15px; -fx-font-weight: bold;");
            profitBox.setStyle("-fx-background-color: #E8F5E9; -fx-background-radius: 8; -fx-padding: 4 14; -fx-border-color: #66BB6A; -fx-border-radius: 8; -fx-border-width: 1;");
        } else if (profit < 0) {
            lblProfit.setStyle("-fx-text-fill: #C62828; -fx-font-size: 15px; -fx-font-weight: bold;");
            profitBox.setStyle("-fx-background-color: #FFEBEE; -fx-background-radius: 8; -fx-padding: 4 14; -fx-border-color: #EF5350; -fx-border-radius: 8; -fx-border-width: 1;");
        } else {
            lblProfit.setStyle("-fx-text-fill: #9E9E9E; -fx-font-size: 15px; -fx-font-weight: bold;");
            profitBox.setStyle("-fx-background-color: #F5F5F5; -fx-background-radius: 8; -fx-padding: 4 14; -fx-border-color: #BDBDBD; -fx-border-radius: 8; -fx-border-width: 1;");
        }
    }

    private void save() {
        if (selectedItem == null) {
            alert.showError("Please select an item from the table first");
            return;
        }
        if (txtNewSellingPrice.getText().isEmpty()) {
            alert.showError("Enter New Selling Price");
            txtNewSellingPrice.requestFocus();
            return;
        }
        try {
            float newPrice = Float.parseFloat(txtNewSellingPrice.getText().trim());
            selectedItem.setSailingprice(newPrice);
            int result = itemService.saveItem(selectedItem);
            if (result == 2) {
                alert.showSuccess("Selling price updated: " + selectedItem.getItemname() + " -> " + newPrice);
                int index = list.indexOf(selectedItem);
                if (index >= 0) {
                    list.set(index, selectedItem);
                }
                table.refresh();
                lblCurrentSellingPrice.setText(String.valueOf(newPrice));
            } else {
                alert.showError("Error updating selling price");
            }
        } catch (NumberFormatException ex) {
            alert.showError("Enter a valid numeric selling price");
            txtNewSellingPrice.requestFocus();
        }
    }

    private void selectNextItem() {
        int currentIndex = table.getSelectionModel().getSelectedIndex();
        if (currentIndex < list.size() - 1) {
            table.getSelectionModel().select(currentIndex + 1);
            table.scrollTo(currentIndex + 1);
        } else {
            alert.showSuccess("Reached last item in the list");
        }
    }

    private void closeEditPanel() {
        selectedItem = null;
        editPanel.setVisible(false);
        editPanel.setManaged(false);
        table.getSelectionModel().clearSelection();
        txtNewSellingPrice.setText("");
    }
}
