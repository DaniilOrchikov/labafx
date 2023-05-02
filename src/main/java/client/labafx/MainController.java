package client.labafx;

import client.labafx.command.GUICommand;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MainController {
    @FXML
    private BorderPane mainPane;
    @FXML
    private StackPane leftStackPane;
    private GUICommand[] commands;
    @FXML
    private VBox buttonsVBox;
    @FXML
    private Label nameLabel;

    public void setName(String name){
        nameLabel.setText(name);
    }

    @FXML
    private void pressCommandButton(ActionEvent event) {
        Button button = (Button) event.getSource();
        String buttonId = button.getId();
        System.out.println(buttonId);
    }

    public StackPane getLeftStackPane() {
        return leftStackPane;
    }

    public BorderPane getMainPane() {
        return mainPane;
    }

    public void setCommands(GUICommand[] commands) {
        this.commands = commands;
        for (int i = 0; i < commands.length; i ++) {
            Button button = new Button(commands[i].getName());
            button.setId(commands[i].getName());
            button.setOnAction(this::pressCommandButton);
            commands[i].setButton(button);
            buttonsVBox.getChildren().add(button);
        }
    }
}
