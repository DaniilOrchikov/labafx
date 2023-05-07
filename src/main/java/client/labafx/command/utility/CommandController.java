package client.labafx.command.utility;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.InputEvent;

public class CommandController {
    @FXML
    private void setColorBlack(InputEvent event) {
        (((Node) event.getSource()).getScene().lookup("#" + ((Node) event.getSource()).getId().replace("Field", "Label").replace("ChoiceBox", "Label"))).setStyle("-fx-text-fill: black;");
    }
}
