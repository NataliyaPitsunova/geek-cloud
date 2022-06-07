module com.geekbrains.cloudapp.june.cloudapplication {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;

    opens com.geekbrains.cloudapp.june.cloudapplication to javafx.fxml;
    exports com.geekbrains.cloudapp.june.cloudapplication;
}