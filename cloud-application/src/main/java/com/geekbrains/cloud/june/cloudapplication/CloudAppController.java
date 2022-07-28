package com.geekbrains.cloud.june.cloudapplication;

import com.geekbrains.cloud.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CloudAppController implements Initializable {

    public String homeDir;

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
    @FXML
    private Label labelClient;
    private boolean focus;

    private Path current;

    private Network network;


    private void readLoop() {
        try {
            while (true) {
                CloudMessage message = network.read();
                if (message instanceof CopyDir copyDir) {
                    current = pathToDir.resolve(copyDir.getName());
                    current.toFile().mkdir();
                    pathToDir = current;
                } else if (message instanceof ListFiles listFiles) {
                    Platform.runLater(() -> {
                        serverList.getItems().clear();
                        serverList.getItems().addAll(listFiles.getFiles());
                    });
                } else if (message instanceof FileMessage fileMessage) {
                    current = pathToDir.resolve(fileMessage.getName());
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
    }

    public void initialClientList() {
        try {
            labelClient.setText(homeDir);
            tableColumns();             //добавляем колонки таблицы
            createNewFolderPane.setVisible(false);  //строка для ввода имени  при создании новой папки скрыта
            createNewFolderPane.setManaged(false);
            pathToDir = Path.of("client_files").resolve(homeDir).toAbsolutePath().normalize();
            updateList(pathToDir.toFile());         //заполняем таблицу
            network = new Network(8189);
            Thread readThread = new Thread(this::readLoop);
            readThread.setDaemon(true);
            readThread.start();
            network.write(new GoToDirServer(homeDir, homeDir, Action.goTo));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tableColumns() {        //метод заполнения таблицы
        //таблица клиента
        TableColumn<FileInfo, String> fileNameColumn = new TableColumn<>("Имя");        //столбец с именем
        fileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));    //указываем что будем выбирать  из fileInfo на каждый файл
        fileNameColumn.setPrefWidth(150);       //задаем размер столбца

        TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>("Тип");
        fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        fileTypeColumn.setPrefWidth(30);

        TableColumn<FileInfo, Long> fileSizeColumn = new TableColumn<>("Размер");
        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        fileSizeColumn.setPrefWidth(80);

        fileSizeColumn.setCellFactory(column -> {
            return new TableCell<FileInfo, Long>() {

                @Override   //проверяется пустая ли ячейка или нет , и отрисовывается размер файла
                protected void updateItem(Long item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        String text = String.format("%,d bytes", item);
                        if (item == -1L) {              //если директория [DIR] вместо размера
                            text = "[DIR]";
                        }
                        setText(text);
                    }
                }
            };
        });

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");     //формат для даты и времени последниъ изменений
        TableColumn<FileInfo, String> fileLastModColumn = new TableColumn<>("Последние изменения");
        fileLastModColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(dtf)));   //отображаем черезх формат
        fileLastModColumn.setPrefWidth(160);


        clientList.getColumns().addAll(fileTypeColumn, fileNameColumn, fileSizeColumn, fileLastModColumn);  //заполняем таблицу столбцами
        clientList.getSortOrder().add(fileTypeColumn);          //сортируем по типу файла
        //аналогично для таблицы на сервере
        TableColumn<FileInfo, String> fileNameClmn = new TableColumn<>("Имя");
        fileNameClmn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
        fileNameClmn.setPrefWidth(150);

        TableColumn<FileInfo, String> fileTypeClmn = new TableColumn<>("Тип");
        fileTypeClmn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        fileTypeClmn.setPrefWidth(30);

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
        fileLastModClmn.setPrefWidth(160);
        serverList.getColumns().addAll(fileTypeClmn, fileNameClmn, fileSizeClmn, fileLastModClmn);
        serverList.getSortOrder().add(fileTypeClmn);
    }

    //метод загрузки на сервер
    public void upload(ActionEvent actionEvent) throws IOException {
        if (clientList.isFocused()) {
            String file = clientList.getSelectionModel().getSelectedItem().getFileName();
            String fileParent = file;
            if (pathToDir.resolve(file).toFile().isFile()) {
                network.write(new FileMessage(pathToDir.resolve(file)));
            } else {
                network.write(new GoToDirServer(file, homeDir, Action.create));
                network.write(new GoToDirServer(file, homeDir, Action.copy));
                copyDirectory(pathToDir.resolve(file));
            }
            network.write(new GoToDirServer(fileParent, homeDir, Action.goToRoot));
        }

    }


    //рекурсивное копирование папок
    public void recursiveCopy(Path path) throws IOException {
        if (path.toFile().isDirectory()) {
            network.write(new GoToDirServer(path.getFileName().toString(), homeDir, Action.create));
            network.write(new GoToDirServer(path.getFileName().toString(), homeDir, Action.copy));
            copyDirectory(path);
        } else {
            network.write(new FileMessage(path));
        }
    }

    private void copyDirectory(Path path) throws IOException {
        for (Path f : Files.list(path).collect(Collectors.toList())) {
            recursiveCopy(f);
        }
    }

    //метод загрузки с сервера
    public void download(ActionEvent actionEvent) throws IOException {  //нажатие на кнопку download
        if (serverList.isFocused()) {
            String file = serverList.getSelectionModel().getSelectedItem().getFileName();
            network.write(new FileRequest(file));
        }
    }

    //обновление содержания директории
    public void updateList(File curDirect) throws IOException {
        FileInfo next = new FileInfo(curDirect.toPath());
        next.setFileName("[ ... ]");
        clientList.getItems().clear();
        if (!(pathToDir.endsWith(labelClient.getText()))) { //если это "верхняя папка, просто отображаем объекты
            clientList.getItems().add(0, next);     //если папка в папке сверху добавляем [ ... ]
        }
        clientList.getItems().addAll(Files.list(curDirect.toPath()).map(FileInfo::new).collect(Collectors.toList()));
    }

    //переход в директорию на клиенте
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
            network.write(new GoToDirServer(nameDir, homeDir, Action.goTo));//передаем имя директории в которую хотим зайти
        }
    }

    //удаление файла
    public void deleteFile(ActionEvent actionEvent) throws IOException {
        if (clientList.isFocused()) {
            if (!(clientList.getSelectionModel().getSelectedItem() == null)) {
                String nameDir = clientList.getSelectionModel().getSelectedItem().getFileName(); //выбранный файл
                Path path = pathToDir.resolve(nameDir);
                if (path.toFile().exists()) {
                    if (path.toFile().isDirectory()) {
                        for (File f : path.toFile().listFiles()) {
                            recursiveDelete(f);
                        }
                    }
                    path.toFile().delete();
                    updateList(pathToDir.toFile());
                }
            }
        } else {
            if (!(serverList.getSelectionModel().getSelectedItem() == null)) {
                String nameDir = serverList.getSelectionModel().getSelectedItem().getFileName();
                network.write(new GoToDirServer(nameDir, homeDir, Action.delete));
            }
        }
    }


    //рекурсивное удаление папки
    public static void recursiveDelete(File file) {
        if (!file.exists())
            return;
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                recursiveDelete(f);
            }
        }
        file.delete();
    }

    // создание новой  папки
    public void create(ActionEvent actionEvent) {
        nameNewDir.setPromptText("Введите наименование новой папки");
        createNewFolderPane.setVisible(true);       //видна панель для ввода имени новой папки
        createNewFolderPane.setManaged(true);
        buttons.setDisable(true);//пока имя новой папки не введено недоступны кнопки действий с файлами
        focus = clientList.isFocused();     //проверяем на чем фокус на клиенте или на сервере, чтобы знать где создать папку
    }

    //жимем ок после ввода имени новой папки
    public void setNameNewFolder(ActionEvent actionEvent) throws IOException {
        buttons.setDisable(false);      //кнопки доступны
        createNewFolderPane.setVisible(false);
        createNewFolderPane.setManaged(false);
        String name = nameNewDir.getText().trim();
        if (nameNewDir.getPromptText().equals(("Введите наименование новой папки"))) {
            if (!(name.equals(""))) {//проверка на пустое имя
                if (focus) {
                    File f = new File(pathToDir.resolve(name).toString());
                    f.mkdir();
                    updateList(pathToDir.toFile());
                } else {
                    network.write(new GoToDirServer(name, homeDir, Action.create));
                }
            }
        } else {
            if (!(name.equals(""))) {
                if (focus) {
                    Path p = pathToDir.resolve(clientList.getSelectionModel().getSelectedItem().getFileName());
                    try {
                        if (p.toFile().exists()) {
                            if (p.toFile().renameTo(new File(pathToDir.resolve(name).toString()))) {
                                System.out.println("Rename complite");
                            } else {
                                System.out.println("Rename failed");
                            }
                        } else {
                            System.out.println("File No exist");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    updateList(pathToDir.toFile());
                }else{
                    network.write(new RenameFile(serverList.getSelectionModel().getSelectedItem().getFileName(), name));
                }

            }
        }
        nameNewDir.clear();
    }

    public void Rename(ActionEvent actionEvent) {
        nameNewDir.setPromptText("");
        createNewFolderPane.setVisible(true);       //видна панель для ввода имени новой папки
        createNewFolderPane.setManaged(true);
        buttons.setDisable(true);//пока имя новой папки не введено недоступны кнопки действий с файлами
        if (clientList.isFocused()) {
            nameNewDir.setText(clientList.getSelectionModel().getSelectedItem().getFileName());
            focus = clientList.isFocused();     //проверяем на чем фокус на клиенте или на сервере, чтобы знать где создать папку
        } else {
            nameNewDir.setText(serverList.getSelectionModel().getSelectedItem().getFileName());
        }
    }
}
