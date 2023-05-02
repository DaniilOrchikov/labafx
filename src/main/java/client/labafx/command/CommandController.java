package client.labafx.command;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

public class CommandController {
    @FXML
    private void setColorBlack(KeyEvent event) {
        (((TextField) event.getSource()).getScene().lookup("#" + ((TextField) event.getSource()).getId().replace("Field", "Label"))).setStyle("-fx-text-fill: black;");
    }
}
