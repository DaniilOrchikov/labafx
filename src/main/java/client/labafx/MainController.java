package client.labafx;

import client.labafx.command.Command;
import client.labafx.command.GUICommand;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import ticket.TicketBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainController {
    @FXML
    private StackPane mainPane;
    @FXML
    private StackPane leftStackPane;
    @FXML
    private VBox buttonsVBox;
    @FXML
    private Label nameLabel;

    public void setName(String name) {
        nameLabel.setText(name);
    }

    public StackPane getLeftStackPane() {
        return leftStackPane;
    }

    public StackPane getMainPane() {
        return mainPane;
    }

    private GUICommand[] commands;

    public void setCommands(GUICommand[] guiCommands, Command[] commands) {
        this.commands = guiCommands;
        ArrayList<Command> commandArrayList = new ArrayList<>(List.of(guiCommands));
        Collections.addAll(commandArrayList, commands);
        for (Command command : commandArrayList) {
            Button button = new Button(command.getName());
            button.setMinWidth(130);
            button.setId(command.getCommandName());
            command.setButton(button);
            buttonsVBox.getChildren().add(button);
        }
    }
}
