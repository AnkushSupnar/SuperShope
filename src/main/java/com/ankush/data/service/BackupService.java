package com.ankush.data.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

@Service
public class BackupService {
    private static final Logger LOG = LoggerFactory.getLogger(BackupService.class);
    private static final String PREF_BACKUP_PATH = "backup_folder_path";
    private static final String DEFAULT_BACKUP_FOLDER = System.getProperty("user.home") + File.separator + "SuperShopBackup";
    private static final String BACKUP_FILE_NAME = "backup_latest.sql";

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    /**
     * Performs a silent background backup to the configured backup folder.
     * Uses the same file (backup_latest.sql) every time, overwriting the previous one.
     * Only runs if the backup folder has been configured (exists in preferences).
     */
    public void performAutoBackup() {
        try {
            Preferences prefs = Preferences.userNodeForPackage(
                    com.ankush.controller.settings.BackupController.class);
            String backupPath = prefs.get(PREF_BACKUP_PATH, null);

            if (backupPath == null || backupPath.isEmpty()) {
                LOG.info("Auto-backup skipped: no backup folder configured");
                return;
            }

            File backupDir = new File(backupPath);
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            String mysqldumpPath = findMysqldumpPath();
            if (mysqldumpPath == null) {
                LOG.warn("Auto-backup skipped: mysqldump not found");
                return;
            }

            String dbName = extractDatabaseName(dbUrl);
            String host = extractHost(dbUrl);
            String port = extractPort(dbUrl);
            String filePath = backupPath + File.separator + BACKUP_FILE_NAME;

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

                    if (exitCode == 0) {
                        LOG.info("Auto-backup completed successfully: {}", filePath);
                    } else {
                        LOG.error("Auto-backup failed with exit code: {}", exitCode);
                    }
                } catch (IOException | InterruptedException e) {
                    LOG.error("Auto-backup error", e);
                    if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
            backupThread.setDaemon(true);
            backupThread.start();

        } catch (Exception e) {
            LOG.error("Auto-backup error", e);
        }
    }

    private String findMysqldumpPath() {
        if (isExecutableAvailable("mysqldump")) {
            return "mysqldump";
        }

        String[] directPaths = {
                "C:\\xampp\\mysql\\bin\\mysqldump.exe",
                "C:\\wamp64\\bin\\mysql\\mysql8.0.31\\bin\\mysqldump.exe",
                "C:\\laragon\\bin\\mysql\\mysql-8.0.30-winx64\\bin\\mysqldump.exe"
        };
        for (String path : directPaths) {
            if (new File(path).exists()) return path;
        }

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
                        if (mysqldump.exists()) return mysqldump.getAbsolutePath();
                    }
                }
            }
        }

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
                        if (mysqldump.exists()) return mysqldump.getAbsolutePath();
                    }
                }
            }
        }

        return null;
    }

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

    private String extractDatabaseName(String url) {
        String withoutParams = url.contains("?") ? url.substring(0, url.indexOf("?")) : url;
        return withoutParams.substring(withoutParams.lastIndexOf("/") + 1);
    }

    private String extractHost(String url) {
        String afterProtocol = url.substring(url.indexOf("//") + 2);
        return afterProtocol.substring(0, afterProtocol.indexOf(":"));
    }

    private String extractPort(String url) {
        String afterProtocol = url.substring(url.indexOf("//") + 2);
        String afterHost = afterProtocol.substring(afterProtocol.indexOf(":") + 1);
        return afterHost.substring(0, afterHost.indexOf("/"));
    }
}
