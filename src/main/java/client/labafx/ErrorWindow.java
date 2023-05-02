package client.labafx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class ErrorWindow {
    public static void show(String text, String nameText) throws IOException {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        VBox vBox = new FXMLLoader(MainWindow.class.getResource("error-window.fxml")).load();
        ((Label)vBox.lookup("#errorLabel")).setText(text);
        ((Label)vBox.lookup("#errorLabelName")).setText(nameText);
        stage.setScene(new Scene(vBox));
        stage.showAndWait();
    }
}
