package client.labafx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class ErrorView {
    public static void show(String text, String nameText) throws IOException {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        VBox vBox = new FXMLLoader(MainWindow.class.getResource("error-view.fxml")).load();
        ((Label)vBox.lookup("#errorLabel")).setText(text);
        ((Label)vBox.lookup("#errorLabelName")).setText(nameText);
        popupStage.setScene(new Scene(vBox));
        popupStage.showAndWait();
    }
}
