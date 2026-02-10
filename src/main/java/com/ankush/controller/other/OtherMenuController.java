package com.ankush.controller.other;

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
public class OtherMenuController implements Initializable {
    @Autowired @Lazy
    private StageManager stageManager;
    @Autowired
    SpringFXMLLoader loader;

    @FXML private AnchorPane pane;
    @FXML private Button btnOpenFavorite;

    private BorderPane root;
    private Pane menuPane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        btnOpenFavorite.setOnAction(e -> {
            root = (BorderPane) pane.getParent();
            menuPane = loader.getPage("/fxml/other/FavoriteItem.fxml");
            root.setCenter(menuPane);
        });
    }
}
