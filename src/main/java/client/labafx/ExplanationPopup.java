package client.labafx;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.io.IOException;

public class ExplanationPopup {

    public static void show(String text, String nameText, Stage primaryStage) throws IOException {
        Popup popup = new Popup();
        VBox vBox = new FXMLLoader(MainWindow.class.getResource("popup-view.fxml")).load();
        popup.getContent().add(vBox);
        popup.setAutoHide(true);
        ((Label)vBox.lookup("#nameLabel")).setText(nameText);
        ((Label)vBox.lookup("#textLabel")).setText(text);
        popup.show(primaryStage);
    }
}

