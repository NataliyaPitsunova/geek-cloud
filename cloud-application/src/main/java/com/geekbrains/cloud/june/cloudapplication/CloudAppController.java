package com.geekbrains.cloud.june.cloudapplication;

import com.geekbrains.cloud.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CloudAppController implements Initializable {
    private String homeDir;
    private Path pathToDir;
    @FXML
    public TableView<FileInfo> clientList;
    @FXML
    public TableView<FileInfo> serverList;
    @FXML
    public TextField nameNewDir;
    @FXML
    public Pane createNewFolderPane;
    @FXML
    private HBox buttons;
    private FileInfo next;
    private boolean focus;

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
                        try {
                            updateList(pathToDir.toFile());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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

            tableColumns();
            createNewFolderPane.setVisible(false);
            createNewFolderPane.setManaged(false);

            pathToDir = Path.of(homeDir);
            updateList(pathToDir.toFile());
            network = new Network(8189);
            Thread readThread = new Thread(this::readLoop);
            readThread.setDaemon(true);
            readThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tableColumns() {
        TableColumn<FileInfo, String> fileNameColumn = new TableColumn<>("Имя");
        fileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
        fileNameColumn.setPrefWidth(150);

        TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>("Тип");
        fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        fileTypeColumn.setPrefWidth(15);

        TableColumn<FileInfo, Long> fileSizeColumn = new TableColumn<>("Размер");
        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        fileSizeColumn.setPrefWidth(80);

        fileSizeColumn.setCellFactory(column -> {
            return new TableCell<FileInfo, Long>() {

                @Override
                protected void updateItem(Long item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        String text = String.format("%,d bytes", item);
                        if (item == -1L) {
                            text = "[DIR]";
                        }
                        setText(text);
                    }
                }
            };
        });

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        TableColumn<FileInfo, String> fileLastModColumn = new TableColumn<>("Последние изменения");
        fileLastModColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(dtf)));
        fileLastModColumn.setPrefWidth(80);


        clientList.getColumns().addAll(fileTypeColumn, fileNameColumn, fileSizeColumn, fileLastModColumn);
        clientList.getSortOrder().add(fileTypeColumn);

        TableColumn<FileInfo, String> fileNameClmn = new TableColumn<>("Имя");
        fileNameClmn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
        fileNameClmn.setPrefWidth(150);

        TableColumn<FileInfo, String> fileTypeClmn = new TableColumn<>("Тип");
        fileTypeClmn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        fileTypeClmn.setPrefWidth(15);

        TableColumn<FileInfo, Long> fileSizeClmn = new TableColumn<>("Размер");
        fileSizeClmn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        fileSizeClmn.setPrefWidth(80);

        fileSizeClmn.setCellFactory(column -> {
            return new TableCell<FileInfo, Long>() {

                @Override
                protected void updateItem(Long item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        String text = String.format("%,d bytes", item);
                        if (item == -1L) {
                            text = "[DIR]";
                        }
                        setText(text);
                    }
                }
            };
        });

        TableColumn<FileInfo, String> fileLastModClmn = new TableColumn<>("Последние изменения");

        fileLastModClmn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(dtf)));
        fileLastModClmn.setPrefWidth(80);
        serverList.getColumns().addAll(fileTypeClmn, fileNameClmn, fileSizeClmn, fileLastModClmn);
        serverList.getSortOrder().add(fileTypeClmn);
    }


    public void upload(ActionEvent actionEvent) throws IOException {

        String file = clientList.getSelectionModel().getSelectedItem().getFileName();
        network.write(new FileMessage(pathToDir.resolve(file)));
    }

    public void download(ActionEvent actionEvent) throws IOException {  //нажатие на кнопку download
        String file = serverList.getSelectionModel().getSelectedItem().getFileName();
        network.write(new FileRequest(file));
    }

    public void updateList(File curDirect) throws IOException {
        next = new FileInfo(curDirect.toPath());
        next.setFileName("[ ... ]");
        clientList.getItems().clear();
        if (!(pathToDir.endsWith("client_files"))) { //если это "верхняя папка, просто отображаем объекты
            clientList.getItems().add(0, next);
        }

        clientList.getItems().addAll(Files.list(curDirect.toPath()).map(FileInfo::new).collect(Collectors.toList()));
    }

    public void goToDir(MouseEvent mouseEvent) throws IOException {        //при клике мышкой на объект клиентского листа
        if (mouseEvent.getClickCount() == 2) {
            Path path;
            File curDirect;
            String nameDir = clientList.getSelectionModel().getSelectedItem().getFileName(); //выбранный файл
            if (nameDir.startsWith("[ ... ]")) {        //проверяем не был ли нажат переход на папку выше
                pathToDir = pathToDir.getParent();      //если да, поднимаемся на папку выше
                curDirect = new File(pathToDir.toString());
                updateList(curDirect);
            } else {        //если выделен объект не ...
                path = pathToDir.resolve(nameDir).toAbsolutePath();     //делаем путь к объекту
                curDirect = new File(path.toString());           //  оъект Файла с таким путем
                if ((curDirect.isDirectory())) {                    //проверяем этот объект директория?
                    pathToDir = path;
                    updateList(pathToDir.toFile());
                }
            }
        }
    }

    //переход в папку на стороне сервера
    public void goToServerDir(MouseEvent mouseEvent) throws IOException {
        if (mouseEvent.getClickCount() == 2) {

            String nameDir = serverList.getSelectionModel().getSelectedItem().getFileName();
            network.write(new GoToDirServer(nameDir, Action.goTo));//передаем имя директории в которую хотим зайти
        }
    }

    public void deleteFile(ActionEvent actionEvent) throws IOException {
        if (clientList.isFocused()) {
            String nameDir = clientList.getSelectionModel().getSelectedItem().getFileName(); //выбранный файл
            Path path = pathToDir.resolve(nameDir);
            if (path.toFile().exists()) {
                if (path.toFile().isDirectory()) {
                    for (File f : path.toFile().listFiles()) {
                        if (f.exists()) {
                            f.delete();
                        }
                    }
                    path.toFile().delete();
                }
                updateList(pathToDir.toFile());
            }
        } else {
            String nameDir = serverList.getSelectionModel().getSelectedItem().getFileName();
            network.write(new GoToDirServer(nameDir, Action.delete));
        }

    }

    public void create(ActionEvent actionEvent) {
        createNewFolderPane.setVisible(true);
        createNewFolderPane.setManaged(true);
        buttons.setDisable(true);
        focus = clientList.isFocused();
    }


    public void setNameNewFolder(ActionEvent actionEvent) throws IOException {
        buttons.setDisable(false);
        createNewFolderPane.setVisible(false);
        createNewFolderPane.setManaged(false);
        if (focus) {
            File f = new File(pathToDir.resolve(nameNewDir.getText()).toString());
            f.mkdir();
            updateList(pathToDir.toFile());
        } else {
            network.write(new GoToDirServer(nameNewDir.getText(), Action.create));
        }
    }
}
