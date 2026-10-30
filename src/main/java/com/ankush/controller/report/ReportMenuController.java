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

    // chip looks are in css/app-theme.css: white outlined chips, the selected one solid indigo
    private static final String CHIP_SELECTED = "chip-selected";
    private static final String CHIP_TEXT = "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: ";
    private static final String INDIGO = "#1A237E";

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
        setChip(chipSales, chipSalesIcon, chipSalesLabel, panelSales, "sales".equals(chip));
        setChip(chipPurchase, chipPurchaseIcon, chipPurchaseLabel, panelPurchase, "purchase".equals(chip));
        setChip(chipCustomer, chipCustomerIcon, chipCustomerLabel, panelCustomer, "customer".equals(chip));
    }

    private void setChip(HBox box, FontAwesomeIcon icon, Label label, VBox panel, boolean selected) {
        box.getStyleClass().remove(CHIP_SELECTED);
        if (selected) box.getStyleClass().add(CHIP_SELECTED);
        icon.setFill(selected ? Color.WHITE : Color.web(INDIGO));
        label.setStyle(CHIP_TEXT + (selected ? "white;" : INDIGO + ";"));
        panel.setVisible(selected);
    }
}
