package com.ankush.controller.home;

import com.ankush.common.CommonData;
import com.ankush.config.SpringFXMLLoader;
import com.ankush.view.FxmlView;
import com.ankush.view.StageManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
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
    @FXML private HBox menuDashboard;
    @FXML private HBox menuTransaction;
    @FXML private HBox menuCreate;
    @FXML private HBox menuInventary;
    @FXML private HBox menuMaster;
    @FXML private HBox menuReport;
    @FXML private Text txtUserName;
    @FXML private HBox menuExit;

    private Pane pane;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        if(CommonData.getLoginUser()!=null)
        {
            txtUserName.setText(CommonData.getLoginUser().getUsername());
        }
        menuDashboard.setOnMouseClicked(e->{
            pane =loader.getPage("/fxml/dashboard/Dashboard.fxml");
            mainPane.setCenter(pane);
        });
        menuTransaction.setOnMouseClicked(e -> {
                pane =loader.getPage("/fxml/transaction/TransactionMenu.fxml");
                mainPane.setCenter(pane);
        });
//        menuCreate.setOnMouseClicked(e->{
//            pane =loader.getPage("/fxml/create/CreateMenu.fxml");
//            mainPane.setCenter(pane);
//        });

        menuReport.setOnMouseClicked(e->{
            pane =loader.getPage("/fxml/report/ReportMenu.fxml");
            mainPane.setCenter(pane);
        });
        menuExit.setOnMouseClicked(e->System.exit(0));
        menuMaster.setOnMouseClicked(e->{
            pane =loader.getPage("/fxml/create/CreateMenu.fxml");
            mainPane.setCenter(pane);
        });
    }
}