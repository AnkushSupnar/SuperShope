package com.ankush.controller.home;

import com.ankush.config.SpringFXMLLoader;
import com.ankush.view.FxmlView;
import com.ankush.view.StageManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
@Component
public class HomeController implements Initializable {
    private static final Logger LOG = LoggerFactory.getLogger(HomeController.class);
    @Autowired @Lazy
    StageManager stageManager;
    @Autowired
    SpringFXMLLoader loader;
    @FXML private BorderPane mainPane;
    @FXML private HBox menuTransaction;
    @FXML private HBox menuCreate;
    @FXML private HBox menuInventary;
    @FXML private HBox menuMaster;
    @FXML private HBox menuReport;
    private Pane pane;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        menuTransaction.setOnMouseClicked(e -> {
                pane =loader.getPage("/fxml/transaction/TransactionMenu.fxml");
                mainPane.setCenter(pane);
        });
    }
}