package com.ankush.controller.transaction;

import com.ankush.controller.home.HomeController;
import com.ankush.view.FxmlView;
import com.ankush.view.StageManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class TransactionMenuController implements Initializable {
    private static final Logger LOG = LoggerFactory.getLogger(HomeController.class);
    @Autowired
    @Lazy
    StageManager stageManager;
    @FXML
    private Button btnPurchaseInvoice;

    @FXML
    private Button btnBilling;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //btnPurchaseInvoice.setOnAction(e->stageManager.switchScene(FxmlView.PURCHASEINVOICE));   
        btnPurchaseInvoice.setOnAction(e->stageManager.switchScene(FxmlView.PURCHASEINVOICE2));
            btnBilling.setOnAction(e->stageManager.switchScene(FxmlView.DAILYBILLING));
    }
}
