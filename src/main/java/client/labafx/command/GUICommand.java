package client.labafx.command;

import client.labafx.ClientLogic;
import client.labafx.command.utility.CommandNode;
import javafx.animation.ParallelTransition;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public abstract class GUICommand extends Command{
    private CommandNode mainNode;
    ParallelTransition openTransition;
    ParallelTransition closeTransition;
    StackPane stackPane;
    Stage primaryStage;
    GUICommand[] commands;

    public GUICommand(String commandName, String name, ClientLogic clientLogic){
        super(commandName, name, clientLogic);
    }

    public void closeAllCommandsView() {
        for (GUICommand command : commands) {
            if (!command.getCommandName().equals(getCommandName())) {
                command.closeThisView();
                command.getMainNode().clearNode(command.getCommandName());
            }
        }
    }

    public abstract CommandNode getMainNode();

    public void closeThisView() {
        closeTransition.play();
        getMainNode().clearNode(getCommandName());
    }

    public void setCommands(GUICommand[] commands) {
        this.commands = commands;
    }

    public abstract StackPane createRootNode(Stage primaryStage) throws IOException;

    public void setButtonsActions() {
        button.setOnAction(event -> {
            openTransition.play();
            closeAllCommandsView();
        });
        ((Button) stackPane.lookup("#" + getCommandName() + "cancelButton")).setOnAction(event -> closeThisView());
    }

    public abstract void pushOkButton(ActionEvent event);

    public Button getCancelButton() {
        return (Button) stackPane.lookup("#" + getCommandName() + "cancelButton");
    }

    public Button getOkButton() {
        return (Button) stackPane.lookup("#" + getCommandName() + "OKButton");
    }
}
