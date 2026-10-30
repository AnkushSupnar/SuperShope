package com.ankush;

import com.ankush.data.service.LoginService;
import com.ankush.view.FxmlView;
import com.ankush.view.SplashScreen;
import com.ankush.view.SpringBootTask;
import com.ankush.view.StageManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Main extends Application {
	private ConfigurableApplicationContext springContext;
	protected StageManager stageManager;
	LoginService loginService;

	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void init() {
		// Spring Boot initialization moved to background thread in start()
	}

	@Override
	public void start(Stage stage) {
		Platform.setImplicitExit(false);

		// Show splash screen immediately
		SplashScreen splash = new SplashScreen(stage);
		splash.setProgress(0.05);
		splash.show();

		// Create Spring Boot background task
		String[] args = getParameters().getRaw().toArray(new String[0]);
		SpringBootTask springTask = new SpringBootTask(args);

		// Bind status messages from task to splash label
		splash.getStatusLabel().textProperty().bind(springTask.messageProperty());

		// Animated progress: easing curve from 5% toward 85%, never reaching it
		final double[] progress = {0.05};
		Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), e -> {
			progress[0] += (0.85 - progress[0]) * 0.025;
			splash.setProgress(progress[0]);
		}));
		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.play();

		// On success: snap progress to 100% and show login
		springTask.setOnSucceeded(event -> {
			timeline.stop();
			springContext = springTask.getValue();

			splash.getStatusLabel().textProperty().unbind();
			splash.setProgress(0.95);
			splash.setStatus("Loading login screen...");

			// Brief delay to show 100% before transitioning
			Timeline finishTimeline = new Timeline(
					new KeyFrame(Duration.millis(300), e -> {
						splash.setProgress(1.0);
						splash.setStatus("Ready!");
					}),
					new KeyFrame(Duration.millis(1000), e -> {
						splash.hide();
						showLoginStage();
					})
			);
			finishTimeline.play();
		});

		// On failure: show error on splash
		springTask.setOnFailed(event -> {
			timeline.stop();
			splash.getStatusLabel().textProperty().unbind();
			splash.setProgress(0);

			Throwable ex = springTask.getException();
			String msg = ex != null ? ex.getMessage() : "Unknown error";
			splash.setStatus("Failed to start: " + msg);
		});

		// Launch Spring Boot on a daemon thread
		Thread springThread = new Thread(springTask);
		springThread.setDaemon(true);
		springThread.start();
	}

	private void showLoginStage() {
		Stage loginStage = new Stage();
		try {
			loginStage.getIcons().add(
					new Image(getClass().getResourceAsStream("/fxml/icon/store.png")));
		} catch (Exception ignored) {
		}

		Platform.setImplicitExit(true);

		// Register Stage as a singleton bean so Spring can inject it into StageManager factory method
		springContext.getBeanFactory().registerSingleton("primaryStage", loginStage);
		stageManager = springContext.getBean(StageManager.class);
		loginService = springContext.getBean(LoginService.class);
		displayInitialScene();
	}

	@Override
	public void stop() {
		if (springContext != null) {
			springContext.close();
		}
	}

	protected void displayInitialScene() {
		stageManager.switchScene(FxmlView.LOGIN);
	}
}
