package com.ankush.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SplashScreen {

    private final Stage stage;
    private final ProgressBar progressBar;
    private final Label statusLabel;

    public SplashScreen(Stage stage) {
        this.stage = stage;

        // App icon
        ImageView icon = new ImageView(
                new Image(getClass().getResourceAsStream("/fxml/icon/store.png")));
        icon.setFitWidth(80);
        icon.setFitHeight(80);
        icon.setPreserveRatio(true);

        // Title
        Label title = new Label("Super Shopee");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#1565C0"));

        // Subtitle
        Label subtitle = new Label("Billing & Inventory Management");
        subtitle.setFont(Font.font("System", FontWeight.NORMAL, 13));
        subtitle.setTextFill(Color.GRAY);

        // Progress bar
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(320);
        progressBar.setPrefHeight(6);
        progressBar.setStyle(
                "-fx-accent: #1976D2;");

        // Status label
        statusLabel = new Label("Starting application...");
        statusLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        statusLabel.setTextFill(Color.web("#616161"));

        // Layout
        VBox root = new VBox(12, icon, title, subtitle, progressBar, statusLabel);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40, 50, 30, 50));
        root.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 12;");
        root.setEffect(new DropShadow(20, Color.rgb(0, 0, 0, 0.3)));

        Scene scene = new Scene(root, 420, 300);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);

        // Set taskbar icon
        try {
            stage.getIcons().add(
                    new Image(getClass().getResourceAsStream("/fxml/icon/store.png")));
        } catch (Exception ignored) {
        }
    }

    public void show() {
        stage.centerOnScreen();
        stage.show();
    }

    public void hide() {
        stage.hide();
    }

    public void setProgress(double value) {
        progressBar.setProgress(value);
    }

    public void setStatus(String message) {
        statusLabel.setText(message);
    }

    public Label getStatusLabel() {
        return statusLabel;
    }
}
