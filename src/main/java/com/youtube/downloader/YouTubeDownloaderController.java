package com.youtube.downloader;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.zeroturnaround.exec.ProcessExecutor;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YouTubeDownloaderController {

    @FXML
    private TextField videoUrlField;

    @FXML
    private TextField outputPathField;

    @FXML
    private TextField cookiesPathField;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Button downloadButton;

    @FXML
    private Button browseOutputButton;

    @FXML
    private Button browseCookiesButton;

    @FXML
    private Label statusLabel;

    private String detectPlatform(String videoUrl) {
        if (videoUrl.contains("youtube.com") || videoUrl.contains("youtu.be")) {
            return "YouTube";
        } else if (videoUrl.contains("vk.com") || videoUrl.contains("vkvideo.ru")) {
            return "VK";
        } else {
            return "Unknown";
        }
    }
    @FXML
    public void initialize() {
        // Логика для кнопки "Download"
        downloadButton.setOnAction(event -> downloadVideo());

        // Логика для кнопки "Browse" (папка сохранения)
        browseOutputButton.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select Download Directory");
            File selectedDirectory = directoryChooser.showDialog(null);
            if (selectedDirectory != null) {
                outputPathField.setText(selectedDirectory.getAbsolutePath());
            }
        });

        // Логика для кнопки "Browse" (файл cookies.txt)
        browseCookiesButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Cookies File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
            File selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile != null) {
                cookiesPathField.setText(selectedFile.getAbsolutePath());
            }
        });
    }

    private void downloadVideo() {
        String videoUrl = videoUrlField.getText().trim();
        String outputPath = outputPathField.getText().trim();
        String cookiesPath = cookiesPathField.getText().trim();

        if (videoUrl.isEmpty() || outputPath.isEmpty()) {
            statusLabel.setText("Please provide video URL and output path.");
            return;
        }

        // Обработка ссылок vkvideo.ru и исправление лишних дефисов
        if (videoUrl.contains("vkvideo.ru")) {
            videoUrl = videoUrl.replace("vkvideo.ru", "vk.com");
        }
        if (videoUrl.contains("video--")) {
            videoUrl = videoUrl.replace("video--", "video-");
        }

        // Распознаём платформу
        String platform = detectPlatform(videoUrl);
        if (platform.equals("Unknown")) {
            statusLabel.setText("Unsupported platform. Please use YouTube or VK.");
            return;
        }

        // Устанавливаем начальный прогресс
        progressBar.setProgress(0);
        statusLabel.setText("Preparing download from " + platform + "...");

        // Путь к yt-dlp.exe
        String ytDlpPath = "C:\\tools\\yt-dlp.exe";

        // Команда для yt-dlp
        String command = ytDlpPath +
                " -f \"bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]\" " +
                " --merge-output-format mp4 " +
                (cookiesPath.isEmpty() ? "" : "--cookies \"" + cookiesPath.replace("\\", "\\\\") + "\" ") +
                " -o \"" + outputPath.replace("\\", "\\\\") + "\\\\%(title)s.%(ext)s\" " +
                "\"" + videoUrl + "\"";

        // Выводим команду для отладки
        System.out.println("Command: " + command);

        // Выполняем команду в фоновом потоке
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                new ProcessExecutor()
                        .command("cmd", "/c", command)
                        .redirectOutput(new org.zeroturnaround.exec.stream.LogOutputStream() {
                            @Override
                            protected void processLine(String line) {
                                System.out.println(line);
                            }
                        })
                        .redirectErrorStream(true)
                        .execute();

                Platform.runLater(() -> {
                    statusLabel.setText("Download from " + platform + " completed!");
                    progressBar.setProgress(1);
                });

            } catch (IOException | InterruptedException | TimeoutException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setText("Error during download: " + e.getMessage());
                    progressBar.setProgress(0);
                });
            }
        });
    }


}
