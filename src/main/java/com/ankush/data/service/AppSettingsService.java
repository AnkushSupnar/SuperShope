package com.ankush.data.service;

import com.ankush.controller.print.PrintExample;
import com.ankush.data.entities.AppSettings;
import com.ankush.data.repository.AppSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Service
public class AppSettingsService {

    @Autowired
    private AppSettingsRepository repository;

    public String getValue(String key, String defaultValue) {
        Optional<AppSettings> setting = repository.findBySettingKey(key);
        return setting.map(AppSettings::getSettingValue).orElse(defaultValue);
    }

    public void setValue(String key, String value) {
        Optional<AppSettings> existing = repository.findBySettingKey(key);
        if (existing.isPresent()) {
            existing.get().setSettingValue(value);
            repository.save(existing.get());
        } else {
            repository.save(AppSettings.builder()
                    .settingKey(key)
                    .settingValue(value)
                    .build());
        }
    }

    public boolean isStockCheckEnabled() {
        return "true".equalsIgnoreCase(getValue("stock_check", "true"));
    }

    public void setStockCheckEnabled(boolean enabled) {
        setValue("stock_check", String.valueOf(enabled));
    }

    /** Billing: Enter on Quantity adds the line straight away instead of moving to Rate. */
    public boolean isQuickAddEnabled() {
        return "true".equalsIgnoreCase(getValue("quick_add", "true"));
    }

    public void setQuickAddEnabled(boolean enabled) {
        setValue("quick_add", String.valueOf(enabled));
    }

    public static final String PRINT_ASK = "ASK";
    public static final String PRINT_ALWAYS = "ALWAYS";
    public static final String PRINT_NEVER = "NEVER";

    /** What happens after a bill is saved: ASK, ALWAYS or NEVER print. */
    public String getPrintMode() {
        return getValue("print_after_save", PRINT_ASK);
    }

    public void setPrintMode(String mode) {
        setValue("print_after_save", mode);
    }

    /** Printer for bills; empty means the Windows default printer. */
    public String getBillPrinter() {
        return getValue("bill_printer", "");
    }

    public void setBillPrinter(String printerName) {
        setValue("bill_printer", printerName == null ? "" : printerName);
        publishPrinterSettings();
    }

    /** Thermal printer dot width (512 / 576 / 384), 0 = Windows driver, -1 = auto from the printer name. */
    public int getPrinterDots() {
        try {
            return Integer.parseInt(getValue("printer_dots", String.valueOf(PrintExample.AUTO_WIDTH)));
        } catch (NumberFormatException e) {
            return PrintExample.AUTO_WIDTH;
        }
    }

    public void setPrinterDots(int dots) {
        setValue("printer_dots", String.valueOf(dots));
        publishPrinterSettings();
    }

    @PostConstruct
    public void publishPrinterSettings() {
        PrintExample.configure(getBillPrinter(), getPrinterDots());
    }
}
