package client.labafx.command;

import client.labafx.ClientLogic;
import client.labafx.ErrorWindow;
import client.labafx.ExplanationPopup;
import client.labafx.command.utility.CommandNode;
import client.labafx.command.utility.CommandWithTicketNode;
import client.labafx.command.utility.NodeWithOpenAndCloseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import ticket.TicketBuilder;
import utility.Command;

import java.io.IOException;

public class RemoveLower extends GUICommand {
    CommandWithTicketNode mainNode;
    public RemoveLower(ClientLogic clientLogic) {
        super("remove_lower", "RemoveLower", clientLogic);
    }

    @Override
    public CommandNode getMainNode() {
        return mainNode;
    }

    @Override
    public StackPane createRootNode(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        mainNode = new CommandWithTicketNode();
        NodeWithOpenAndCloseTransition node = mainNode.getRootNode();

        stackPane = (StackPane) node.node();
        openTransition = node.openTransition();
        closeTransition = node.closeTransition();
        stackPane.setAlignment(Pos.TOP_LEFT);

        mainNode.changingId(getCommandName());

        setButtonsActions();
        getOkButton().setOnAction(this::pushOkButton);

        return stackPane;
    }

    @Override
    public void pushOkButton(ActionEvent event) {
        TicketBuilder ticketBuilder = mainNode.getTicketBuilder(getCommandName());
        if (ticketBuilder.readyTCreate()) {
            closeThisView();
            threadPool.execute(() -> {
                String req = clientLogic.communicatingWithServer(new Command(new String[]{getCommandName()}, ticketBuilder, clientLogic.userName, clientLogic.userPassword)).text();
                Platform.runLater(() -> {
                    try {
                        if (!req.matches("^[0-9]+$"))
                            ErrorWindow.show(req, "Ошибка при выполнении команды " + getCommandName());
                         else ExplanationPopup.show("Удалено объектов - " + req, getCommandName(), primaryStage);
                    } catch (IOException ignored) {
                    }
                });
            });
        }
    }
}
