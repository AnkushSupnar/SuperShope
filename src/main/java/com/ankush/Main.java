package com.ankush;

import com.ankush.controller.print.PrintBill;
import com.ankush.view.FxmlView;
import com.ankush.view.StageManager;
import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

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


		stageManager.switchScene(FxmlView.LOGIN);
		//stageManager.switchScene(FxmlView.BILLING);
		//stageManager.switchScene(FxmlView.CUSTOMER);
		//stageManager.switchScene(FxmlView.EMPLOYEE);
		//stageManager.switchScene(FxmlView.HOME);
		//stageManager.switchScene(FxmlView.PURCHASEINVOICE);
		//stageManager.switchScene(FxmlView.ITEMSTOCK);
		//stageManager.switchScene(FxmlView.SALEREPORT);
		//stageManager.switchScene(FxmlView.ITEM);
	}
	private ConfigurableApplicationContext bootstrapSpringApplicationContext() {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(Main.class);
		String[] args = getParameters().getRaw().stream().toArray(String[]::new);
		builder.headless(false); //needed for TestFX integration testing or eles will get a java.awt.HeadlessException during tests
		return builder.run(args);
	}
}
