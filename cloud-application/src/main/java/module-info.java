module com.geekbrains.cloudapp.june.cloudapplication {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.geekbrains.cloud.june.model;
    requires io.netty.codec;

    opens com.geekbrains.cloud.june.cloudapplication to javafx.fxml;
    exports com.geekbrains.cloud.june.cloudapplication;

}