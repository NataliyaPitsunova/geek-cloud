package com.geekbrains.cloud.june.cloudapplication;

import com.geekbrains.cloud.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class CloudAppController implements Initializable {
    private String homeDir;
    private Path pathToDir;
    @FXML
    public ListView<String> clientList;
    @FXML
    public ListView<String> serverList;


    private Network network;

    private void readLoop() {
        try {
            while (true) {
                CloudMessage message = network.read();
                if (message instanceof ListFiles listFiles) {
                    Platform.runLater(() -> {
                        serverList.getItems().clear();
                        serverList.getItems().addAll(listFiles.getFiles());

                    });
                } else if (message instanceof FileMessage fileMessage) {
                    Path current = pathToDir.resolve(fileMessage.getName());
                    Files.write(current, fileMessage.getData());
                    Platform.runLater(() -> {
                        clientList.getItems().clear();
                        if (!(pathToDir.endsWith("client_files"))) {
                            clientList.getItems().add(0, "[ ... ]");
                        }
                        clientList.getItems().addAll(pathToDir.toFile().list());
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            homeDir = "client_files";
            clientList.getItems().clear();
            clientList.getItems().addAll(getFiles(homeDir));
            pathToDir = Path.of(homeDir);
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

        String file = clientList.getSelectionModel().getSelectedItem();
        network.write(new FileMessage(pathToDir.resolve(file)));
    }

    public void download(ActionEvent actionEvent) throws IOException {  //нажатие на кнопку download
        String file = serverList.getSelectionModel().getSelectedItem();
        network.write(new FileRequest(file));
    }


    public void goToDir(MouseEvent mouseEvent) {        //при клике мышкой на объект клиентского листа
        Path path;
        File curDirect;
        String nameDir = clientList.getSelectionModel().getSelectedItem(); //выбранный файл
        if (nameDir.startsWith("[ ... ]")) {        //проверяем не был ли нажат переход на папку выше
            pathToDir = pathToDir.getParent();      //если да, поднимаемся на папку выше
            curDirect = new File(pathToDir.toString());
            if (pathToDir.endsWith("client_files")) { //если это "верхняя папка, просто отображаем объекты
                clientList.getItems().clear();
                clientList.getItems().addAll(curDirect.list());
            } else {            //если у папки еще есть куда подниматься , добавляем в листвив 0 индексем ...
                clientList.getItems().clear();
                clientList.getItems().add(0, "[ ... ]");
                clientList.getItems().addAll(curDirect.list());
            }
        } else {        //если выделен объект не ...
            path = pathToDir.resolve(nameDir).toAbsolutePath();     //делаем путь к объекту
            curDirect = new File(path.toString());           //  оъект Файла с таким путем
            if (!(curDirect.isFile())) {                    //проверяем этот объект директория?
                clientList.getItems().clear();          //если да выстраиваем список файлов +переход наверх
                clientList.getItems().add(0, "[ ... ]");
                clientList.getItems().addAll(curDirect.list());
                pathToDir = path;
            }
        }
    }
//переход в папку на стороне сервера
    public void goToServerDir(MouseEvent mouseEvent) throws IOException {
        String directory = serverList.getSelectionModel().getSelectedItem();
        network.write(new GoToDirServer(directory));//передаем имя директории в которую хотим зайти
    }
}
