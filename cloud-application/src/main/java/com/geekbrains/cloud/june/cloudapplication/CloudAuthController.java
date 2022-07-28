package com.geekbrains.cloud.june.cloudapplication;

import com.geekbrains.cloud.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;


public class CloudAuthController implements Initializable {

    @FXML
    private PasswordField passIn, passReg;
    @FXML
    private TextField logIn, logReg;

    @FXML
    private TabPane tab;
    private Network network;
    public Stage theStage;
    private String homeDir;


    private void readLoop() throws IOException {
        try {
            while (true) {
                CloudMessage message = network.read();
                if (message instanceof ResponseRegistr responseRegistr) {
                    if (responseRegistr.isRegistr()) {
                        Path.of("client_files").resolve(homeDir).toFile().mkdir();
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("РЕГИСТРАЦИЯ ЗАВЕРШЕНА");
                                alert.setHeaderText("Пользователь успешно зарегистрирован");
                                alert.setContentText("Для входа в облако перейдите на вкладку вход");
                                alert.showAndWait();
                            }
                        });
                    } else {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("ОШИБКА");
                                alert.setHeaderText("Пользователь с таким именем уже зарегистрирован");
                                alert.setContentText("Используйте другой логин");
                                alert.showAndWait();
                            }
                        });
                    }
                }
                if (message instanceof ResponseServer responseServer) {
                    if (responseServer.getNameUser().startsWith("UserOnline")) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Error");
                                alert.setHeaderText("User online");
                                alert.setContentText("Please, try later");
                                alert.showAndWait();
                            }
                        });
                    } else {

                        if (!(responseServer.getNameUser().startsWith("No Auth"))) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        theStage.close();
                                        new CloudApplication(homeDir).start(new Stage());

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } else {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    Alert alert = new Alert(Alert.AlertType.ERROR);
                                    alert.setTitle("Error");
                                    alert.setHeaderText("Wrong login or password!");
                                    alert.setContentText("Please, try again");
                                    alert.showAndWait();
                                }
                            });

                        }
                    }
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            addEventFilterLogIn(logIn);
            addEventFilterLogIn(passIn);
            addEventFilterRegIn(logReg);
            addEventFilterRegIn(passReg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void regOn(ActionEvent actionEvent) throws IOException {
        startRead();
        homeDir = logReg.getText();
        network.write(new SignIn(logReg.getText(), passReg.getText(), Action.registr));
    }

    public void addEventFilterLogIn(TextField textfield) throws IOException {
        textfield.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    try {
                        input(new ActionEvent());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    keyEvent.consume();
                }
            }
        });
    }

    public void addEventFilterRegIn(TextField textfield) throws IOException {
        textfield.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    try {
                        regOn(new ActionEvent());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    keyEvent.consume();
                }
            }
        });
    }


    @FXML
    public void input(ActionEvent actionEvent) throws IOException {
        startRead();
        homeDir = logIn.getText();
        network.write(new SignIn(logIn.getText(), passIn.getText(), Action.signIn));
    }

    public void startRead() {
        try {
            network = new Network(8189);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Thread readThread = new Thread(() -> {
            try {
                readLoop();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        readThread.setDaemon(true);
        readThread.start();
    }
}
