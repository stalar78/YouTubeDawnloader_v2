package com.youtube.downloader;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import org.zeroturnaround.exec.ProcessExecutor;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

public class YouTubeDownloaderController {

    @FXML
    private TextField videoUrlField;

    @FXML
    private TextField outputPathField;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Button downloadButton;

    @FXML
    private Button browseOutputButton;

    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        // Логика для кнопки "Download"
        downloadButton.setOnAction(event -> downloadVideo());

        // Логика для кнопки "Browse" (выбор папки сохранения)
        browseOutputButton.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select Download Directory");
            File selectedDirectory = directoryChooser.showDialog(null);
            if (selectedDirectory != null) {
                outputPathField.setText(selectedDirectory.getAbsolutePath());
            }
        });
    }

    private void downloadVideo() {
        String videoUrl = videoUrlField.getText().trim();
        String outputPath = outputPathField.getText().trim();

        if (videoUrl.isEmpty() || outputPath.isEmpty()) {
            statusLabel.setText("Please provide video URL and output path.");
            return;
        }

        // Устанавливаем начальный прогресс
        progressBar.setProgress(0);
        statusLabel.setText("Preparing download...");

        // Путь к yt-dlp.exe
        String ytDlpPath = "C:\\tools\\yt-dlp.exe";

        // Команда для yt-dlp
        String command = ytDlpPath +
                " -f bestvideo+bestaudio " +
                " --merge-output-format mp4 " +
                " -o \"" + outputPath.replace("\\", "\\\\") + "\\\\%(title)s.%(ext)s\" " +
                "\"" + videoUrl + "\"";

        // Выполняем команду в фоновом потоке
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                try {
                    new ProcessExecutor()
                            .command("cmd", "/c", command)
                            .execute();
                } catch (TimeoutException e) {
                    throw new RuntimeException(e);
                }

                // По завершении обновляем интерфейс
                Platform.runLater(() -> {
                    statusLabel.setText("Download completed!");
                    progressBar.setProgress(1);
                });

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setText("Error during download: " + e.getMessage());
                    progressBar.setProgress(0);
                });
            }
        });
    }
}
