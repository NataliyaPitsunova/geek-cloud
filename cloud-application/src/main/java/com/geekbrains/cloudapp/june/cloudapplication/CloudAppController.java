package com.geekbrains.cloudapp.june.cloudapplication;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class CloudAppController implements Initializable {
    private String homeDir;
    private byte[] buffer;
    @FXML
    public ListView<String> clientList;
    @FXML
    public ListView<String> serverList;

    public File fileFromSrv;

    private Network network;

    private void readLoop() {
        try {
            while (true) {
                String command = network.readString();
                if (command.equals("#list")) {
                    Platform.runLater(() -> {
                        serverList.getItems().clear();
                    });
                    int lenght = network.getInt();
                    for (int i = 0; i < lenght; i++) {
                        String file = network.readString();
                        Platform.runLater(() -> serverList.getItems().add(file));
                    }
                }
                if (command.equals("#received")) {      //обработчик получил от клиента команду received, то записывая файл в поток он сперва присылает команду на клиента
                    saveFileToClient(fileFromSrv);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            buffer = new byte[256];
            homeDir = System.getProperty("user.home");
            clientList.getItems().clear();
            clientList.getItems().addAll(getFiles(homeDir));
            network = new Network(8189);
            Thread readThread = new Thread(this::readLoop);
            readThread.setDaemon(true);
            readThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getFiles(String dir) {
        String[] list = new File(dir).list();
        assert list != null;
        return Arrays.asList(list);
    }

    public void upload(ActionEvent actionEvent) throws IOException {
        network.getOs().writeUTF("#file");
        String file = clientList.getSelectionModel().getSelectedItem();
        network.getOs().writeUTF(file);
        File toSend = Path.of(homeDir).resolve(file).toFile();
        network.getOs().writeLong(toSend.length());
        try (FileInputStream fis = new FileInputStream(toSend)) {
            while (fis.available() > 0) {
                int read = fis.read(buffer);
                network.getOs().write(buffer, 0, read);
            }
        }

        network.getOs().flush();

    }

    public void download(ActionEvent actionEvent) throws IOException {  //нажатие на кнопку download
        network.getOs().writeUTF("#received");
        String fileName = serverList.getSelectionModel().getSelectedItem();
        network.getOs().writeUTF(fileName);
        fileFromSrv = Path.of(homeDir).resolve(fileName).toFile();
    }

    public void saveFileToClient(File file) throws IOException {        //сохранение файла из потока на клиенте
        buffer = new byte[256];
        long len = network.getIs().readLong();//понадобится
        try (FileOutputStream fos = new FileOutputStream(file)) {
            int count;
            if ((count = network.getIs().read(buffer)) != -1) {
                fos.write(buffer, 0, count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                clientList.getItems().clear();
                clientList.getItems().addAll(getFiles(homeDir));
                ;
            }
        });
    }

}
