module client.labafx {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires com.almasb.fxgl.all;
    requires ticketLib;
    requires com.google.gson;


    opens client.labafx to javafx.fxml;
    opens client.labafx.command to javafx.fxml;
    exports client.labafx;
}