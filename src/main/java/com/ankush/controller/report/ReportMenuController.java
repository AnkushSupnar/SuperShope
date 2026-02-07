package com.ankush.controller.report;

import com.ankush.config.SpringFXMLLoader;
import com.ankush.view.StageManager;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
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

    @FXML private AnchorPane mainPane;

    // Chips
    @FXML private HBox chipSales;
    @FXML private HBox chipPurchase;
    @FXML private HBox chipCustomer;
    @FXML private FontAwesomeIcon chipSalesIcon;
    @FXML private FontAwesomeIcon chipPurchaseIcon;
    @FXML private FontAwesomeIcon chipCustomerIcon;
    @FXML private Label chipSalesLabel;
    @FXML private Label chipPurchaseLabel;
    @FXML private Label chipCustomerLabel;

    // Panels
    @FXML private StackPane contentArea;
    @FXML private VBox panelSales;
    @FXML private VBox panelPurchase;
    @FXML private VBox panelCustomer;

    // Buttons
    @FXML private Button btnDailySales;
    @FXML private Button btnVIewStock;
    @FXML private Button btnPurchaseReport;
    @FXML private Button btnMonthlyPurchase;
    @FXML private Button btnCustomerLedger;
    @FXML private Button btnOutstandingDues;

    private Pane menuPane;

    private static final String CHIP_ACTIVE_STYLE =
            "-fx-background-color: WHITE; -fx-background-radius: 20; -fx-padding: 8 22; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 1);";
    private static final String CHIP_INACTIVE_STYLE =
            "-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 20; -fx-padding: 8 22; -fx-cursor: hand;";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Chip click handlers
        chipSales.setOnMouseClicked(e -> selectChip("sales"));
        chipPurchase.setOnMouseClicked(e -> selectChip("purchase"));
        chipCustomer.setOnMouseClicked(e -> selectChip("customer"));

        // Button handlers
        btnVIewStock.setOnAction(e -> {
            BorderPane root = (BorderPane) mainPane.getParent();
            menuPane = loader.getPage("/fxml/report/item/ItemStock.fxml");
            root.setCenter(menuPane);
        });

        btnDailySales.setOnAction(e -> {
            BorderPane root = (BorderPane) mainPane.getParent();
            menuPane = loader.getPage("/fxml/report/salesreport/SalesReport.fxml");
            root.setCenter(menuPane);
        });

        btnPurchaseReport.setOnAction(e -> {
            BorderPane root = (BorderPane) mainPane.getParent();
            menuPane = loader.getPage("/fxml/report/purchase/PurchaseReport.fxml");
            root.setCenter(menuPane);
        });

        btnMonthlyPurchase.setOnAction(e -> {
            BorderPane root = (BorderPane) mainPane.getParent();
            menuPane = loader.getPage("/fxml/report/purchase/MonthlyPurchase.fxml");
            root.setCenter(menuPane);
        });

        btnCustomerLedger.setOnAction(e -> {
            BorderPane root = (BorderPane) mainPane.getParent();
            menuPane = loader.getPage("/fxml/report/customer/CustomerLedger.fxml");
            root.setCenter(menuPane);
        });

        btnOutstandingDues.setOnAction(e -> {
            BorderPane root = (BorderPane) mainPane.getParent();
            menuPane = loader.getPage("/fxml/report/customer/OutstandingDues.fxml");
            root.setCenter(menuPane);
        });
    }

    private void selectChip(String chip) {
        // Reset all chips to inactive
        chipSales.setStyle(CHIP_INACTIVE_STYLE);
        chipPurchase.setStyle(CHIP_INACTIVE_STYLE);
        chipCustomer.setStyle(CHIP_INACTIVE_STYLE);
        chipSalesIcon.setFill(Color.WHITE);
        chipPurchaseIcon.setFill(Color.WHITE);
        chipCustomerIcon.setFill(Color.WHITE);
        chipSalesLabel.setStyle("-fx-text-fill: WHITE; -fx-font-size: 13px; -fx-font-weight: bold;");
        chipPurchaseLabel.setStyle("-fx-text-fill: WHITE; -fx-font-size: 13px; -fx-font-weight: bold;");
        chipCustomerLabel.setStyle("-fx-text-fill: WHITE; -fx-font-size: 13px; -fx-font-weight: bold;");

        // Hide all panels
        panelSales.setVisible(false);
        panelPurchase.setVisible(false);
        panelCustomer.setVisible(false);

        // Activate selected chip and show its panel
        switch (chip) {
            case "sales":
                chipSales.setStyle(CHIP_ACTIVE_STYLE);
                chipSalesIcon.setFill(Color.web("#1A237E"));
                chipSalesLabel.setStyle("-fx-text-fill: #1A237E; -fx-font-size: 13px; -fx-font-weight: bold;");
                panelSales.setVisible(true);
                break;
            case "purchase":
                chipPurchase.setStyle(CHIP_ACTIVE_STYLE);
                chipPurchaseIcon.setFill(Color.web("#1A237E"));
                chipPurchaseLabel.setStyle("-fx-text-fill: #1A237E; -fx-font-size: 13px; -fx-font-weight: bold;");
                panelPurchase.setVisible(true);
                break;
            case "customer":
                chipCustomer.setStyle(CHIP_ACTIVE_STYLE);
                chipCustomerIcon.setFill(Color.web("#1A237E"));
                chipCustomerLabel.setStyle("-fx-text-fill: #1A237E; -fx-font-size: 13px; -fx-font-weight: bold;");
                panelCustomer.setVisible(true);
                break;
        }
    }
}
