package com.ankush.controller.settings;

import com.ankush.config.SpringFXMLLoader;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

@Component
public class BackupController implements Initializable {
    private static final Logger LOG = LoggerFactory.getLogger(BackupController.class);
    private static final String PREF_BACKUP_PATH = "backup_folder_path";
    private static final String DEFAULT_BACKUP_FOLDER = System.getProperty("user.home") + File.separator + "SuperShopBackup";

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Autowired
    private SpringFXMLLoader loader;

    @FXML private TextField txtBackupPath;
    @FXML private Button btnChangePath;
    @FXML private Button btnBackupNow;
    @FXML private Button btnShareBackup;
    @FXML private Button btnBack;
    @FXML private ProgressBar progressBar;
    @FXML private Label lblStatus;

    private Preferences prefs;
    private String backupFolderPath;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        prefs = Preferences.userNodeForPackage(BackupController.class);
        backupFolderPath = prefs.get(PREF_BACKUP_PATH, DEFAULT_BACKUP_FOLDER);
        txtBackupPath.setText(backupFolderPath);

        // Ensure default folder exists
        ensureBackupFolderExists(backupFolderPath);

        btnChangePath.setOnAction(e -> changeBackupFolder());
        btnBackupNow.setOnAction(e -> performBackup());
        btnShareBackup.setOnAction(e -> shareBackup());
        btnBack.setOnAction(e -> {
            BorderPane mainPane = (BorderPane) btnBack.getScene().lookup("#mainPane");
            if (mainPane != null) {
                Pane pane = loader.getPage("/fxml/settings/SettingsMenu.fxml");
                mainPane.setCenter(pane);
            }
        });
    }

    private void changeBackupFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Backup Folder");

        File currentDir = new File(backupFolderPath);
        if (currentDir.exists()) {
            directoryChooser.setInitialDirectory(currentDir);
        }

        File selectedDir = directoryChooser.showDialog(txtBackupPath.getScene().getWindow());
        if (selectedDir != null) {
            backupFolderPath = selectedDir.getAbsolutePath();
            txtBackupPath.setText(backupFolderPath);
            prefs.put(PREF_BACKUP_PATH, backupFolderPath);
            lblStatus.setText("Backup folder changed successfully.");
            lblStatus.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 13px;");
        }
    }

    private void performBackup() {
        String mysqldumpPath = findMysqldumpPath();
        if (mysqldumpPath == null) {
            lblStatus.setText("Error: mysqldump not found. Please install MySQL.");
            lblStatus.setStyle("-fx-text-fill: #EF5350; -fx-font-size: 13px;");
            showAlert(Alert.AlertType.ERROR, "mysqldump Not Found",
                    "Could not find mysqldump.exe.\n\n" +
                    "Please ensure MySQL is installed. Checked locations:\n" +
                    "  - System PATH\n" +
                    "  - C:\\Program Files\\MySQL\\MySQL Server *\\bin\\\n" +
                    "  - C:\\Program Files (x86)\\MySQL\\MySQL Server *\\bin\\\n" +
                    "  - C:\\xampp\\mysql\\bin\\\n" +
                    "  - C:\\wamp64\\bin\\mysql\\*\\bin\\");
            return;
        }

        LOG.info("Using mysqldump at: {}", mysqldumpPath);
        ensureBackupFolderExists(backupFolderPath);

        String dbName = extractDatabaseName(dbUrl);
        String host = extractHost(dbUrl);
        String port = extractPort(dbUrl);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String fileName = "backup_" + timestamp + ".sql";
        String filePath = backupFolderPath + File.separator + fileName;

        btnBackupNow.setDisable(true);
        btnShareBackup.setDisable(true);
        progressBar.setVisible(true);
        progressBar.setProgress(-1); // indeterminate
        lblStatus.setText("Creating backup...");
        lblStatus.setStyle("-fx-text-fill: #3949AB; -fx-font-size: 13px;");

        Thread backupThread = new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(
                        mysqldumpPath,
                        "--host=" + host,
                        "--port=" + port,
                        "--user=" + dbUsername,
                        "--password=" + dbPassword,
                        "--result-file=" + filePath,
                        dbName
                );
                pb.redirectErrorStream(true);
                Process process = pb.start();
                int exitCode = process.waitFor();

                Platform.runLater(() -> {
                    progressBar.setProgress(1);
                    btnBackupNow.setDisable(false);
                    btnShareBackup.setDisable(false);

                    if (exitCode == 0) {
                        lblStatus.setText("Backup created successfully: " + fileName);
                        lblStatus.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 13px;");
                        showAlert(Alert.AlertType.INFORMATION, "Backup Successful",
                                "Database backup created successfully!\n\nFile: " + fileName + "\nLocation: " + backupFolderPath);
                    } else {
                        lblStatus.setText("Backup failed. Please check MySQL credentials or database name.");
                        lblStatus.setStyle("-fx-text-fill: #EF5350; -fx-font-size: 13px;");
                        showAlert(Alert.AlertType.ERROR, "Backup Failed",
                                "mysqldump exited with error code: " + exitCode +
                                "\nPlease verify database credentials and that the database exists.");
                    }
                    progressBar.setVisible(false);
                });
            } catch (IOException e) {
                LOG.error("Backup failed", e);
                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    btnBackupNow.setDisable(false);
                    btnShareBackup.setDisable(false);
                    lblStatus.setText("Error running mysqldump: " + e.getMessage());
                    lblStatus.setStyle("-fx-text-fill: #EF5350; -fx-font-size: 13px;");
                    showAlert(Alert.AlertType.ERROR, "Backup Error",
                            "Error running mysqldump:\n" + e.getMessage());
                });
            } catch (InterruptedException e) {
                LOG.error("Backup interrupted", e);
                Thread.currentThread().interrupt();
                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    btnBackupNow.setDisable(false);
                    btnShareBackup.setDisable(false);
                    lblStatus.setText("Backup was interrupted.");
                    lblStatus.setStyle("-fx-text-fill: #EF5350; -fx-font-size: 13px;");
                });
            }
        });
        backupThread.setDaemon(true);
        backupThread.start();
    }

    /**
     * Finds the full path to mysqldump.exe by checking:
     * 1. System PATH (just "mysqldump")
     * 2. Common MySQL installation directories on Windows
     */
    private String findMysqldumpPath() {
        // 1. Check if mysqldump is available in system PATH
        if (isExecutableAvailable("mysqldump")) {
            return "mysqldump";
        }

        // 2. Check common MySQL installation directories on Windows
        String[] searchDirs = {
                "C:\\Program Files\\MySQL",
                "C:\\Program Files (x86)\\MySQL",
                "C:\\xampp\\mysql\\bin",
                "C:\\wamp64\\bin\\mysql",
                "C:\\wamp\\bin\\mysql",
                "C:\\laragon\\bin\\mysql"
        };

        // Check xampp and direct bin paths first
        String[] directPaths = {
                "C:\\xampp\\mysql\\bin\\mysqldump.exe",
                "C:\\wamp64\\bin\\mysql\\mysql8.0.31\\bin\\mysqldump.exe",
                "C:\\laragon\\bin\\mysql\\mysql-8.0.30-winx64\\bin\\mysqldump.exe"
        };
        for (String path : directPaths) {
            if (new File(path).exists()) {
                return path;
            }
        }

        // Search inside "Program Files\MySQL\MySQL Server X.X\bin"
        String[] programFilesDirs = {
                "C:\\Program Files\\MySQL",
                "C:\\Program Files (x86)\\MySQL"
        };
        for (String baseDir : programFilesDirs) {
            File dir = new File(baseDir);
            if (dir.exists() && dir.isDirectory()) {
                File[] subDirs = dir.listFiles();
                if (subDirs != null) {
                    for (File subDir : subDirs) {
                        File mysqldump = new File(subDir, "bin" + File.separator + "mysqldump.exe");
                        if (mysqldump.exists()) {
                            return mysqldump.getAbsolutePath();
                        }
                    }
                }
            }
        }

        // Search inside wamp directories (versioned folders)
        String[] wampBaseDirs = {
                "C:\\wamp64\\bin\\mysql",
                "C:\\wamp\\bin\\mysql"
        };
        for (String baseDir : wampBaseDirs) {
            File dir = new File(baseDir);
            if (dir.exists() && dir.isDirectory()) {
                File[] subDirs = dir.listFiles();
                if (subDirs != null) {
                    for (File subDir : subDirs) {
                        File mysqldump = new File(subDir, "bin" + File.separator + "mysqldump.exe");
                        if (mysqldump.exists()) {
                            return mysqldump.getAbsolutePath();
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Tests if an executable can be run from the system PATH.
     */
    private boolean isExecutableAvailable(String command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command, "--version");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            process.waitFor();
            return true;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    private void shareBackup() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Backup File to Share");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQL Files", "*.sql"));

        File backupDir = new File(backupFolderPath);
        if (backupDir.exists()) {
            fileChooser.setInitialDirectory(backupDir);
        }

        File selectedFile = fileChooser.showOpenDialog(txtBackupPath.getScene().getWindow());
        if (selectedFile == null) {
            return;
        }

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Destination to Share Backup");
        File destDir = directoryChooser.showDialog(txtBackupPath.getScene().getWindow());

        if (destDir != null) {
            try {
                Path source = selectedFile.toPath();
                Path destination = Paths.get(destDir.getAbsolutePath(), selectedFile.getName());
                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                lblStatus.setText("Backup shared successfully to: " + destDir.getAbsolutePath());
                lblStatus.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 13px;");
                showAlert(Alert.AlertType.INFORMATION, "Share Successful",
                        "Backup file copied successfully!\n\nDestination: " + destination.toString());
            } catch (IOException e) {
                LOG.error("Failed to share backup", e);
                lblStatus.setText("Failed to share backup file.");
                lblStatus.setStyle("-fx-text-fill: #EF5350; -fx-font-size: 13px;");
                showAlert(Alert.AlertType.ERROR, "Share Failed",
                        "Failed to copy backup file.\n" + e.getMessage());
            }
        }
    }

    private void ensureBackupFolderExists(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private String extractDatabaseName(String url) {
        // jdbc:mysql://localhost:3306/shopee?useSSL=false
        String withoutParams = url.contains("?") ? url.substring(0, url.indexOf("?")) : url;
        return withoutParams.substring(withoutParams.lastIndexOf("/") + 1);
    }

    private String extractHost(String url) {
        // jdbc:mysql://localhost:3306/shopee
        String afterProtocol = url.substring(url.indexOf("//") + 2);
        return afterProtocol.substring(0, afterProtocol.indexOf(":"));
    }

    private String extractPort(String url) {
        // jdbc:mysql://localhost:3306/shopee
        String afterProtocol = url.substring(url.indexOf("//") + 2);
        String afterHost = afterProtocol.substring(afterProtocol.indexOf(":") + 1);
        return afterHost.substring(0, afterHost.indexOf("/"));
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
