package com.ankush.data.service;

import com.ankush.data.entities.AppSettings;
import com.ankush.data.repository.AppSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
