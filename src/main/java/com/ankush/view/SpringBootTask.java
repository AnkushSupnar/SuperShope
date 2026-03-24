package com.ankush.view;

import com.ankush.Main;
import javafx.concurrent.Task;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class SpringBootTask extends Task<ConfigurableApplicationContext> {

    private final String[] args;

    public SpringBootTask(String[] args) {
        this.args = args;
    }

    @Override
    protected ConfigurableApplicationContext call() throws Exception {
        updateMessage("Preparing application...");
        updateProgress(10, 100);

        updateMessage("Initializing Spring Framework...");
        updateProgress(15, 100);

        updateMessage("Connecting to database...");
        updateProgress(20, 100);

        SpringApplicationBuilder builder = new SpringApplicationBuilder(Main.class);
        builder.headless(false);
        ConfigurableApplicationContext context = builder.run(args);

        updateMessage("Loading resources...");
        updateProgress(95, 100);

        return context;
    }
}
