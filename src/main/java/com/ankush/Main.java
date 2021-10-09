package com.ankush;

import com.ankush.view.FxmlView;
import com.ankush.view.StageManager;
import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.io.IOException;

@SpringBootApplication
public class Main extends Application {
	private ConfigurableApplicationContext springContext;
	protected StageManager stageManager;

	public static void main(String[] args) {

		Application.launch(args);
		//SpringApplication.run(Main.class, args);
	}

	@Override
	public void init() throws IOException {
		springContext = bootstrapSpringApplicationContext();

	}
	@Override
	public void start(Stage stage) throws Exception {
	stageManager  = springContext.getBean(StageManager.class,stage);
	displayInitialScene();
	}
	@Override
	public void stop()
	{
		springContext.close();
	}
	protected void displayInitialScene() {

		//stageManager.switchScene(FxmlView.LOGIN);
		stageManager.switchScene(FxmlView.BILLING);
		//stageManager.switchScene(FxmlView.EMPLOYEE);
		//stageManager.switchScene(FxmlView.HOME);
		//stageManager.switchScene(FxmlView.PURCHASEINVOICE);
		//stageManager.switchScene(FxmlView.ITEMSTOCK);
	}
	private ConfigurableApplicationContext bootstrapSpringApplicationContext() {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(Main.class);
		String[] args = getParameters().getRaw().stream().toArray(String[]::new);
		builder.headless(false); //needed for TestFX integration testing or eles will get a java.awt.HeadlessException during tests
		return builder.run(args);
	}
}
