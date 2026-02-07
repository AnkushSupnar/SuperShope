package com.ankush.controller.settings;

import com.ankush.config.SpringFXMLLoader;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class SettingsMenuController implements Initializable {
    @Autowired
    SpringFXMLLoader loader;

    @FXML
    private Button btnBackup;

    @FXML
    private Button btnShopeeInfo;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        btnBackup.setOnAction(e -> {
            BorderPane mainPane = (BorderPane) btnBackup.getScene().lookup("#mainPane");
            if (mainPane != null) {
                Pane pane = loader.getPage("/fxml/settings/Backup.fxml");
                mainPane.setCenter(pane);
            }
        });
        btnShopeeInfo.setOnAction(e -> {
            BorderPane mainPane = (BorderPane) btnShopeeInfo.getScene().lookup("#mainPane");
            if (mainPane != null) {
                Pane pane = loader.getPage("/fxml/settings/ShopeeInfoSettings.fxml");
                mainPane.setCenter(pane);
            }
        });
    }
}
