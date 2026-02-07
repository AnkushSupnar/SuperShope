package com.ankush.controller.settings;

import com.ankush.common.CommonData;
import com.ankush.config.SpringFXMLLoader;
import com.ankush.data.entities.ShopeeInfo;
import com.ankush.data.service.ShopeeInfoService;
import com.ankush.view.AlertNotification;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Component
public class ShopeeInfoSettingsController implements Initializable {
    private static final Logger LOG = LoggerFactory.getLogger(ShopeeInfoSettingsController.class);

    @Autowired
    private ShopeeInfoService shopeeInfoService;

    @Autowired
    private AlertNotification alert;

    @Autowired
    private SpringFXMLLoader loader;

    @FXML private TextField txtShopeeName;
    @FXML private TextField txtOwnerName;
    @FXML private TextField txtContact;
    @FXML private TextField txtAddress;
    @FXML private Button btnUpdate;
    @FXML private Button btnReset;
    @FXML private Button btnBack;
    @FXML private Label lblStatus;

    private ShopeeInfo currentShopeeInfo;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadShopeeInfo();
        btnUpdate.setOnAction(e -> updateShopeeInfo());
        btnReset.setOnAction(e -> resetFields());
        btnBack.setOnAction(e -> {
            BorderPane mainPane = (BorderPane) btnBack.getScene().lookup("#mainPane");
            if (mainPane != null) {
                Pane pane = loader.getPage("/fxml/settings/SettingsMenu.fxml");
                mainPane.setCenter(pane);
            }
        });
    }

    private void loadShopeeInfo() {
        List<ShopeeInfo> allInfo = shopeeInfoService.getAllShopeeInfo();
        if (allInfo != null && !allInfo.isEmpty()) {
            currentShopeeInfo = allInfo.get(0);
            populateFields();
        } else {
            btnUpdate.setDisable(true);
            lblStatus.setText("No Shopee Info record found. Please register your shop first.");
            lblStatus.setStyle("-fx-text-fill: #EF5350; -fx-font-size: 13px;");
        }
    }

    private void populateFields() {
        if (currentShopeeInfo != null) {
            txtShopeeName.setText(currentShopeeInfo.getShopeeName() != null ? currentShopeeInfo.getShopeeName() : "");
            txtOwnerName.setText(currentShopeeInfo.getShopeeOwnerName() != null ? currentShopeeInfo.getShopeeOwnerName() : "");
            txtContact.setText(currentShopeeInfo.getShopeeContact() != null ? currentShopeeInfo.getShopeeContact() : "");
            txtAddress.setText(currentShopeeInfo.getAddress() != null ? currentShopeeInfo.getAddress() : "");
        }
    }

    private void updateShopeeInfo() {
        if (currentShopeeInfo == null) {
            alert.showError("No Shopee Info record found to update.");
            return;
        }

        if (areFieldsEmpty()) {
            alert.showError("Please fill in all fields before updating.");
            return;
        }

        currentShopeeInfo.setShopeeName(txtShopeeName.getText().trim());
        currentShopeeInfo.setShopeeOwnerName(txtOwnerName.getText().trim());
        currentShopeeInfo.setShopeeContact(txtContact.getText().trim());
        currentShopeeInfo.setAddress(txtAddress.getText().trim());

        try {
            ShopeeInfo saved = shopeeInfoService.saveShopeeInfo(currentShopeeInfo);
            if (saved != null) {
                CommonData.setShopeeInfo(saved);
                alert.showSuccess("Shopee Info updated successfully!");
                lblStatus.setText("Shopee Info updated successfully.");
                lblStatus.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 13px;");
            } else {
                alert.showError("Failed to update Shopee Info.");
                lblStatus.setText("Failed to update Shopee Info.");
                lblStatus.setStyle("-fx-text-fill: #EF5350; -fx-font-size: 13px;");
            }
        } catch (Exception e) {
            LOG.error("Error updating Shopee Info", e);
            alert.showError("Error updating Shopee Info: " + e.getMessage());
            lblStatus.setText("Error updating Shopee Info.");
            lblStatus.setStyle("-fx-text-fill: #EF5350; -fx-font-size: 13px;");
        }
    }

    private void resetFields() {
        populateFields();
        lblStatus.setText("Fields reset to saved values.");
        lblStatus.setStyle("-fx-text-fill: #3949AB; -fx-font-size: 13px;");
    }

    private boolean areFieldsEmpty() {
        return txtShopeeName.getText().trim().isEmpty()
                || txtOwnerName.getText().trim().isEmpty()
                || txtContact.getText().trim().isEmpty()
                || txtAddress.getText().trim().isEmpty();
    }
}
