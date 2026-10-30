package com.ankush.controller.settings;

import com.ankush.config.SpringFXMLLoader;
import com.ankush.controller.print.PrintExample;
import com.ankush.data.service.AppSettingsService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

@Component
public class AppSettingsController implements Initializable {

    @Autowired
    SpringFXMLLoader loader;

    @Autowired
    AppSettingsService appSettingsService;

    @FXML private Button btnBack;
    @FXML private ToggleButton toggleStockCheck;
    @FXML private Label lblStatus;
    @FXML private ToggleButton toggleQuickAdd;
    @FXML private ComboBox<String> cmbPrintMode;

    private static final String PRINT_ASK_LABEL = "Ask every time";
    private static final String PRINT_ALWAYS_LABEL = "Always print";
    private static final String PRINT_NEVER_LABEL = "Never print";

    @FXML private ComboBox<String> cmbBillPrinter;
    @FXML private ComboBox<String> cmbPrinterType;

    private static final String DEFAULT_PRINTER_LABEL = "Windows default printer";
    // label -> dot width saved in settings (see PrintExample.resolveDots)
    private static final Map<String, Integer> PRINTER_TYPES = new LinkedHashMap<>();
    static {
        PRINTER_TYPES.put("Auto (from printer name)", PrintExample.AUTO_WIDTH);
        PRINTER_TYPES.put("Thermal 80mm, 180 dpi (TM-T88)", 512);
        PRINTER_TYPES.put("Thermal 80mm, 203 dpi (TM-T82/T83/T20)", 576);
        PRINTER_TYPES.put("Thermal 58mm, 203 dpi", 384);
        PRINTER_TYPES.put("Normal printer (A4 / PDF)", PrintExample.DRIVER_PRINTING);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Load current setting
        boolean stockCheck = appSettingsService.isStockCheckEnabled();
        toggleStockCheck.setSelected(stockCheck);
        updateToggleStyle(toggleStockCheck, stockCheck);

        // Toggle listener
        toggleStockCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            appSettingsService.setStockCheckEnabled(newVal);
            updateToggleStyle(toggleStockCheck, newVal);
            lblStatus.setText("Setting saved successfully.");
        });

        boolean quickAdd = appSettingsService.isQuickAddEnabled();
        toggleQuickAdd.setSelected(quickAdd);
        updateToggleStyle(toggleQuickAdd, quickAdd);
        toggleQuickAdd.selectedProperty().addListener((obs, oldVal, newVal) -> {
            appSettingsService.setQuickAddEnabled(newVal);
            updateToggleStyle(toggleQuickAdd, newVal);
            lblStatus.setText("Setting saved successfully.");
        });

        cmbPrintMode.getItems().setAll(PRINT_ASK_LABEL, PRINT_ALWAYS_LABEL, PRINT_NEVER_LABEL);
        String mode = appSettingsService.getPrintMode();
        cmbPrintMode.setValue(AppSettingsService.PRINT_ALWAYS.equals(mode) ? PRINT_ALWAYS_LABEL
                : AppSettingsService.PRINT_NEVER.equals(mode) ? PRINT_NEVER_LABEL : PRINT_ASK_LABEL);
        cmbPrintMode.valueProperty().addListener((obs, oldVal, newVal) -> {
            String value = PRINT_ALWAYS_LABEL.equals(newVal) ? AppSettingsService.PRINT_ALWAYS
                    : PRINT_NEVER_LABEL.equals(newVal) ? AppSettingsService.PRINT_NEVER : AppSettingsService.PRINT_ASK;
            appSettingsService.setPrintMode(value);
            lblStatus.setText("Setting saved successfully.");
        });

        cmbBillPrinter.getItems().add(DEFAULT_PRINTER_LABEL);
        for (PrintService service : PrintServiceLookup.lookupPrintServices(null, null)) {
            cmbBillPrinter.getItems().add(service.getName());
        }
        String printer = appSettingsService.getBillPrinter();
        if (!printer.isEmpty() && !cmbBillPrinter.getItems().contains(printer)) {
            cmbBillPrinter.getItems().add(printer); // saved printer that is not connected right now
        }
        cmbBillPrinter.setValue(printer.isEmpty() ? DEFAULT_PRINTER_LABEL : printer);
        cmbBillPrinter.valueProperty().addListener((obs, oldVal, newVal) -> {
            appSettingsService.setBillPrinter(DEFAULT_PRINTER_LABEL.equals(newVal) ? "" : newVal);
            lblStatus.setText("Setting saved successfully.");
        });

        cmbPrinterType.getItems().setAll(PRINTER_TYPES.keySet());
        int dots = appSettingsService.getPrinterDots();
        PRINTER_TYPES.forEach((label, value) -> {
            if (value == dots) cmbPrinterType.setValue(label);
        });
        if (cmbPrinterType.getValue() == null) cmbPrinterType.setValue(PRINTER_TYPES.keySet().iterator().next());
        cmbPrinterType.valueProperty().addListener((obs, oldVal, newVal) -> {
            appSettingsService.setPrinterDots(PRINTER_TYPES.get(newVal));
            lblStatus.setText("Setting saved successfully.");
        });

        // Back button
        btnBack.setOnAction(e -> {
            BorderPane mainPane = (BorderPane) btnBack.getScene().lookup("#mainPane");
            if (mainPane != null) {
                Pane pane = loader.getPage("/fxml/settings/SettingsMenu.fxml");
                mainPane.setCenter(pane);
            }
        });
    }

    private void updateToggleStyle(ToggleButton toggle, boolean enabled) {
        if (enabled) {
            toggle.setText("ON");
            toggle.setStyle("-fx-background-color: linear-gradient(to right, #00796B, #004D40); -fx-background-radius: 18; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        } else {
            toggle.setText("OFF");
            toggle.setStyle("-fx-background-color: #BDBDBD; -fx-background-radius: 18; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        }
    }
}
