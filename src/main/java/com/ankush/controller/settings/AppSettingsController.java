package com.ankush.controller.settings;

import com.ankush.config.SpringFXMLLoader;
import com.ankush.data.service.AppSettingsService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class AppSettingsController implements Initializable {

    @Autowired
    SpringFXMLLoader loader;

    @Autowired
    AppSettingsService appSettingsService;

    @FXML private Button btnBack;
    @FXML private ToggleButton toggleStockCheck;
    @FXML private Label lblStatus;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Load current setting
        boolean stockCheck = appSettingsService.isStockCheckEnabled();
        toggleStockCheck.setSelected(stockCheck);
        updateToggleStyle(stockCheck);

        // Toggle listener
        toggleStockCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            appSettingsService.setStockCheckEnabled(newVal);
            updateToggleStyle(newVal);
            lblStatus.setText("Setting saved successfully.");
        });

        // Back button
        btnBack.setOnAction(e -> {
            BorderPane mainPane = (BorderPane) btnBack.getScene().lookup("#mainPane");
            if (mainPane != null) {
                Pane pane = loader.getPage("/fxml/settings/SettingsMenu.fxml");
                mainPane.setCenter(pane);
            }
        });
    }

    private void updateToggleStyle(boolean enabled) {
        if (enabled) {
            toggleStockCheck.setText("ON");
            toggleStockCheck.setStyle("-fx-background-color: linear-gradient(to right, #00796B, #004D40); -fx-background-radius: 18; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        } else {
            toggleStockCheck.setText("OFF");
            toggleStockCheck.setStyle("-fx-background-color: #BDBDBD; -fx-background-radius: 18; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        }
    }
}
