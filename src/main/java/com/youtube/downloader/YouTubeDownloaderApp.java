package com.youtube.downloader;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class YouTubeDownloaderApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Загрузка FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/YouTubeDownloader.fxml"));

        Parent root = loader.load();

        primaryStage.setTitle("YouTube Downloader");
        primaryStage.setScene(new Scene(root, 500, 400));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}