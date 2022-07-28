package com.geekbrains.cloud.june.cloudapplication;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class CloudApplication extends Application {

    public String label;
    static CloudAppController cloudAppController;

    public CloudApplication(String homeDir) {
        this.label = homeDir;
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("client-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        cloudAppController = fxmlLoader.getController();
        cloudAppController.homeDir =label;
        cloudAppController.initialClientList();
        stage.setTitle(label + " CLOUD");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                Platform.exit();
                System.exit(0);
            }
        });
    }


}