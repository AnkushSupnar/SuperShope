package com.ankush.controller.home;

import com.ankush.common.CommonData;
import com.ankush.config.SpringFXMLLoader;
import com.ankush.view.StageManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
@Component
public class HomeController implements Initializable {
    private static final Logger LOG = LoggerFactory.getLogger(HomeController.class);
    @Autowired @Lazy
    StageManager stageManager;
    @Autowired
    SpringFXMLLoader loader;
    @FXML private Label lblShopeeName;
    @FXML private BorderPane mainPane;
    @FXML private HBox menuDashboard;
    @FXML private HBox menuTransaction;
    @FXML private HBox menuCreate;
    @FXML private HBox menuInventary;
    @FXML private HBox menuMaster;
    @FXML private HBox menuReport;
    @FXML private HBox menuOther;
    @FXML private Text txtUserName;
    @FXML private HBox menuSettings;
    @FXML private HBox menuSupport;
    @FXML private HBox menuExit;

    private static final String MENU_SELECTED_STYLE =
            "-fx-background-color: rgba(255,255,255,0.25); -fx-background-radius: 10; " +
            "-fx-padding: 12 20; -fx-cursor: hand; " +
            "-fx-border-color: transparent transparent transparent #FFCA28; " +
            "-fx-border-width: 0 0 0 3; -fx-border-radius: 10;";

    private static final String MENU_DEFAULT_STYLE =
            "-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 10; " +
            "-fx-padding: 12 20; -fx-cursor: hand;";

    private Pane pane;
    private List<HBox> navMenuItems;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        if(CommonData.getShopeeInfo()!=null){
            lblShopeeName.setText(CommonData.getShopeeInfo().getShopeeName());
            lblShopeeName.setFont(Font.font("kiran", 28));
        }
        if(CommonData.getLoginUser()!=null)
        {
            txtUserName.setText(CommonData.getLoginUser().getUsername());
        }

        navMenuItems = Arrays.asList(menuDashboard, menuTransaction, menuMaster, menuReport, menuOther, menuSettings);

        menuDashboard.setOnMouseClicked(e->{
            setActiveMenu(menuDashboard);
            pane =loader.getPage("/fxml/dashboard/Dashboard.fxml");
            mainPane.setCenter(pane);
        });
        menuTransaction.setOnMouseClicked(e -> {
                setActiveMenu(menuTransaction);
                pane =loader.getPage("/fxml/transaction/TransactionMenu.fxml");
                mainPane.setCenter(pane);
        });

        menuReport.setOnMouseClicked(e->{
            setActiveMenu(menuReport);
            pane =loader.getPage("/fxml/report/ReportMenu.fxml");
            mainPane.setCenter(pane);
        });
        menuSettings.setOnMouseClicked(e->{
            setActiveMenu(menuSettings);
            pane = loader.getPage("/fxml/settings/SettingsMenu.fxml");
            mainPane.setCenter(pane);
        });
        menuSupport.setOnMouseClicked(e -> showSupportDialog());
        menuExit.setOnMouseClicked(e->System.exit(0));
        menuMaster.setOnMouseClicked(e->{
            setActiveMenu(menuMaster);
            pane =loader.getPage("/fxml/create/CreateMenu.fxml");
            mainPane.setCenter(pane);
        });
        menuOther.setOnMouseClicked(e->{
            setActiveMenu(menuOther);
            pane =loader.getPage("/fxml/other/OtherMenu.fxml");
            mainPane.setCenter(pane);
        });

        // Set Dashboard as the default selected menu
        setActiveMenu(menuDashboard);
    }

    private void setActiveMenu(HBox activeMenu) {
        for (HBox menu : navMenuItems) {
            menu.setStyle(MENU_DEFAULT_STYLE);
        }
        activeMenu.setStyle(MENU_SELECTED_STYLE);
    }

    private void showSupportDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.initOwner(mainPane.getScene().getWindow());

        VBox root = new VBox(20);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #FFFFFF, #E8EAF6); " +
                "-fx-background-radius: 16; -fx-border-color: #3949AB; -fx-border-width: 2; -fx-border-radius: 16;");

        // Title
        Label title = new Label("Developer Support");
        title.setStyle("-fx-text-fill: #1A237E; -fx-font-size: 22px; -fx-font-weight: bold;");

        // Info rows
        VBox infoBox = new VBox(12);
        infoBox.setPadding(new Insets(10, 0, 10, 0));

        infoBox.getChildren().addAll(
                createInfoRow("Name", "Ankush S. Supnar"),
                createInfoRow("Mobile No", "+91 8382394603"),
                createInfoRow("WhatsApp", "+91 8329394603, +91 9960855742"),
                createInfoRow("Email", "ankushsupnar@gmail.com"),
                createInfoRow("Address", "Amalner, Ahilyanagar")
        );

        // Close button
        Button btnClose = new Button("Close");
        btnClose.setStyle("-fx-background-color: linear-gradient(to right, #3949AB, #1A237E); " +
                "-fx-text-fill: white; -fx-background-radius: 10; -fx-font-size: 14px; " +
                "-fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 10 40; " +
                "-fx-effect: dropshadow(gaussian, rgba(26,35,126,0.3), 6, 0, 0, 2);");
        btnClose.setOnAction(e -> dialog.close());

        root.getChildren().addAll(title, infoBox, btnClose);

        Scene scene = new Scene(root, 420, 340);
        scene.setFill(null);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private HBox createInfoRow(String label, String value) {
        Label lblKey = new Label(label + ":");
        lblKey.setStyle("-fx-text-fill: #3949AB; -fx-font-size: 13px; -fx-font-weight: bold;");
        lblKey.setMinWidth(90);

        Label lblValue = new Label(value);
        lblValue.setStyle("-fx-text-fill: #1A237E; -fx-font-size: 13px;");
        lblValue.setWrapText(true);

        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(lblKey, lblValue);
        return row;
    }
}