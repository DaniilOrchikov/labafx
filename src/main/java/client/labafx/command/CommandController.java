package client.labafx.command;

import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

public class CommandController {
    @FXML
    private void setColorBlack(KeyEvent event) {
        (((TextField) event.getSource()).getScene().lookup("#" + ((TextField) event.getSource()).getId().replace("Field", "Label"))).setStyle("-fx-text-fill: black;");
    }

    @FXML
    private void correctionEnteredIntegerValues(KeyEvent event) {
        Spinner<String> spinner = (Spinner<String>) event.getSource();
        String text = spinner.getValue();
        String key = String.valueOf(text.toCharArray()[text.length() - 1]);
        try {
            Integer.parseInt(key);
        } catch (NumberFormatException e) {
            spinner.getValueFactory().setValue(text.substring(0, text.length() - 2));
        }
    }
}
