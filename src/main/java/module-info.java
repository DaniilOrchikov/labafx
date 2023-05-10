module client.labafx {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires ticketLib;
    requires com.google.gson;
    requires lombok;

    opens client.labafx to javafx.fxml;
    opens client.labafx.command to javafx.fxml;
    exports client.labafx;
    opens client.labafx.command.utility to javafx.fxml;
    exports client.labafx.draw;
    opens client.labafx.draw to javafx.fxml;
}