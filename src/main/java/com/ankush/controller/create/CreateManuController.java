package com.ankush.controller.create;

import com.ankush.config.SpringFXMLLoader;
import com.ankush.view.FxmlView;
import com.ankush.view.StageManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
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
public class CreateManuController implements Initializable {
    @Autowired @Lazy
    private StageManager stageManager;
    @Autowired
    SpringFXMLLoader loader;
      @FXML private AnchorPane pane;
      @FXML private BorderPane root;
      @FXML private Button btnAddEmployee;
      @FXML private Button btnViewEmployee;
      @FXML private Button btnAddBank;
      @FXML private Button btnVIewBank;
      @FXML private Button btnAddUser;
      @FXML private Button btnAddItem;
      @FXML private Button btnViewItem;
      @FXML private Button btnAddParty;
      @FXML private Button btnViewParty;
      private Pane menuPane;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        btnAddEmployee.setOnAction(e -> {
            root = (BorderPane) pane.getParent();
            menuPane = loader.getPage("/fxml/create/AddEmployee.fxml");
            root.setCenter(menuPane);
        });
        btnAddBank.setOnAction(e -> {
            root = (BorderPane) pane.getParent();
            menuPane = loader.getPage("/fxml/create/AddBank.fxml");
            root.setCenter(menuPane);
        });
        btnAddUser.setOnAction(e -> {
            root = (BorderPane) pane.getParent();
            menuPane = loader.getPage("/fxml/create/AddUser.fxml");
            root.setCenter(menuPane);
        });
        btnAddItem.setOnAction(e -> {
            root = (BorderPane) pane.getParent();
            menuPane = loader.getPage("/fxml/create/AddItem.fxml");
            root.setCenter(menuPane);
        });
        btnAddParty.setOnAction(e -> {
            root = (BorderPane) pane.getParent();
            menuPane = loader.getPage("/fxml/create/AddPurchaseParty.fxml");
            root.setCenter(menuPane);
        });
    }
}
