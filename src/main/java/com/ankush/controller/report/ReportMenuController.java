package com.ankush.controller.report;

import com.ankush.config.SpringFXMLLoader;
import com.ankush.view.StageManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;
@Component
public class ReportMenuController implements Initializable {
    @Autowired @Lazy
    StageManager stageManager;
    @Autowired
    SpringFXMLLoader loader;
    @FXML private BorderPane root;
    @FXML private AnchorPane mainPane;
    @FXML private Button btnVIewStock;
    private Pane menuPane;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        btnVIewStock.setOnAction(e->{
            root = (BorderPane) mainPane.getParent();
            menuPane = loader.getPage("/fxml/report/item/ItemStock.fxml");
            root.setCenter(menuPane);
        });
    }
}
