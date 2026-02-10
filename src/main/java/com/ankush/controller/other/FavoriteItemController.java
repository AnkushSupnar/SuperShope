package com.ankush.controller.other;

import com.ankush.config.SpringFXMLLoader;
import com.ankush.customUI.AutoCompleteTextField;
import com.ankush.data.entities.FavoriteCategory;
import com.ankush.data.entities.Item;
import com.ankush.data.service.FavoriteCategoryService;
import com.ankush.data.service.ItemService;
import com.ankush.view.AlertNotification;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.scene.layout.BorderPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@Component
public class FavoriteItemController implements Initializable {

    @Autowired
    private SpringFXMLLoader loader;
    @Autowired
    private FavoriteCategoryService categoryService;
    @Autowired
    private ItemService itemService;

    private final AlertNotification alert = new AlertNotification();

    // FXML elements
    @FXML private AnchorPane rootPane;
    @FXML private Button btnBack;
    @FXML private TextField txtCategoryName;
    @FXML private Button btnSaveCategory;
    @FXML private ListView<String> listCategories;
    @FXML private Button btnEditCategory;
    @FXML private Button btnDeleteCategory;
    @FXML private Button btnClearCategory;
    @FXML private Label lblSelectedCategory;
    @FXML private TextField txtItemSearch;
    @FXML private Button btnAddItem;
    @FXML private Button btnRemoveItem;
    @FXML private ListView<String> listCategoryItems;

    // State
    private final List<FavoriteCategory> categories = new ArrayList<>();
    private FavoriteCategory selectedCategory = null;
    private Long editingCategoryId = null;

    // Item search autocomplete
    private AutoCompleteTextField autoCompleteItemSearch;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadCategories();
        autoCompleteItemSearch = new AutoCompleteTextField(txtItemSearch, itemService.getAllItemNames(), Font.font("Kiran", 20));
        autoCompleteItemSearch.setOnSelectionCallback(selectedName -> addItemToCategory());

        // Back button
        btnBack.setOnAction(e -> {
            BorderPane root = (BorderPane) rootPane.getParent();
            root.setCenter(loader.getPage("/fxml/other/OtherMenu.fxml"));
        });

        // Category actions
        btnSaveCategory.setOnAction(e -> saveCategory());
        btnEditCategory.setOnAction(e -> editCategory());
        btnDeleteCategory.setOnAction(e -> deleteCategory());
        btnClearCategory.setOnAction(e -> clearCategoryForm());

        // Category selection
        listCategories.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                selectCategory();
            }
        });

        // Item actions
        btnAddItem.setOnAction(e -> addItemToCategory());
        btnRemoveItem.setOnAction(e -> removeItemFromCategory());
    }

    // ======================== Category Management ========================

    private void loadCategories() {
        categories.clear();
        categories.addAll(categoryService.getAllCategories());
        listCategories.getItems().clear();
        for (FavoriteCategory cat : categories) {
            listCategories.getItems().add(cat.getName());
        }
    }

    private void saveCategory() {
        String name = txtCategoryName.getText().trim();
        if (name.isEmpty()) {
            alert.showError("Please enter a category name");
            return;
        }

        if (editingCategoryId != null) {
            // Update existing
            FavoriteCategory category = categoryService.getCategoryById(editingCategoryId);
            if (category != null) {
                category.setName(name);
                categoryService.saveCategory(category);
                alert.showSuccess("Category updated successfully");
            }
        } else {
            // Check duplicate
            FavoriteCategory existing = categoryService.getCategoryByName(name);
            if (existing != null) {
                alert.showError("Category '" + name + "' already exists");
                return;
            }
            // Create new
            FavoriteCategory category = FavoriteCategory.builder()
                    .name(name)
                    .displayOrder(categories.size() + 1)
                    .build();
            categoryService.saveCategory(category);
            alert.showSuccess("Category saved successfully");
        }

        clearCategoryForm();
        loadCategories();
    }

    private void editCategory() {
        int index = listCategories.getSelectionModel().getSelectedIndex();
        if (index < 0) {
            alert.showError("Please select a category to edit");
            return;
        }
        FavoriteCategory category = categories.get(index);
        editingCategoryId = category.getId();
        txtCategoryName.setText(category.getName());
        txtCategoryName.requestFocus();
    }

    private void deleteCategory() {
        int index = listCategories.getSelectionModel().getSelectedIndex();
        if (index < 0) {
            alert.showError("Please select a category to delete");
            return;
        }
        FavoriteCategory category = categories.get(index);
        boolean confirm = alert.showConfirmation("Delete Category",
                "Are you sure you want to delete '" + category.getName() + "' and all its items?");
        if (confirm) {
            categoryService.deleteCategory(category.getId());
            alert.showSuccess("Category deleted successfully");
            clearCategoryForm();
            loadCategories();
            // Clear right panel if deleted category was selected
            if (selectedCategory != null && selectedCategory.getId().equals(category.getId())) {
                selectedCategory = null;
                lblSelectedCategory.setText("Select a Category");
                listCategoryItems.getItems().clear();
            }
        }
    }

    private void clearCategoryForm() {
        txtCategoryName.clear();
        editingCategoryId = null;
        listCategories.getSelectionModel().clearSelection();
    }

    private void selectCategory() {
        int index = listCategories.getSelectionModel().getSelectedIndex();
        if (index < 0) return;
        selectedCategory = categories.get(index);
        lblSelectedCategory.setText("k^ToigarI: " + selectedCategory.getName());
        loadCategoryItems();
    }

    // ======================== Item Management ========================

    private void loadCategoryItems() {
        listCategoryItems.getItems().clear();
        if (selectedCategory == null) return;
        List<String> itemNames = categoryService.getItemNamesByCategory(selectedCategory.getId());
        listCategoryItems.getItems().addAll(itemNames);
    }

    private void addItemToCategory() {
        if (selectedCategory == null) {
            alert.showError("Please select a category first");
            return;
        }
        String itemName = txtItemSearch.getText().trim();
        if (itemName.isEmpty()) {
            alert.showError("Please search and select an item");
            return;
        }
        Item item = itemService.getItemByName(itemName);
        if (item == null) {
            alert.showError("Item '" + itemName + "' not found");
            return;
        }
        int result = categoryService.addItemToCategory(selectedCategory, item);
        if (result == 0) {
            alert.showError("Item already exists in this category");
        } else {
            alert.showSuccess("Item added to '" + selectedCategory.getName() + "'");
            loadCategoryItems();
        }
        autoCompleteItemSearch.clear();
    }

    private void removeItemFromCategory() {
        if (selectedCategory == null) {
            alert.showError("Please select a category first");
            return;
        }
        String selectedItem = listCategoryItems.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            alert.showError("Please select an item to remove");
            return;
        }
        boolean confirm = alert.showConfirmation("Remove Item",
                "Remove '" + selectedItem + "' from '" + selectedCategory.getName() + "'?");
        if (confirm) {
            Item item = itemService.getItemByName(selectedItem);
            if (item != null) {
                categoryService.removeItemFromCategory(selectedCategory.getId(), item.getId());
                alert.showSuccess("Item removed successfully");
                loadCategoryItems();
            }
        }
    }
}
