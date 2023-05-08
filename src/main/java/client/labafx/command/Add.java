package client.labafx.command;

import client.labafx.ClientLogic;
import client.labafx.ErrorWindow;
import client.labafx.ExplanationPopup;
import client.labafx.MainWindow;
import client.labafx.command.utility.CommandNode;
import client.labafx.command.utility.CommandWithTicketNode;
import client.labafx.command.utility.NodeWithOpenAndCloseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ticket.TicketBuilder;
import utility.Command;

import java.io.IOException;

public class Add extends GUICommand {

    CommandWithTicketNode mainNode;

    public Add(ClientLogic clientLogic) {
        super("add", "Add", clientLogic);
    }

    @Override
    public CommandNode getMainNode() {
        return mainNode;
    }

    @Override
    public StackPane createRootNode(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        mainNode = new CommandWithTicketNode();
        mainNode.getRootNode();

        VBox vBox = new FXMLLoader(MainWindow.class.getResource("command-selecting-add-mode.fxml")).load();
        ChoiceBox<String> choiceBox = (ChoiceBox<String>) vBox.lookup("#modeChoiceBox");
        choiceBox.getItems().addAll("normal", "if_max", "if_min");
        choiceBox.setValue("normal");
        NodeWithOpenAndCloseTransition node = mainNode.addNode(1, vBox);

        stackPane = (StackPane) node.node();
        openTransition = node.openTransition();
        closeTransition = node.closeTransition();
        stackPane.setAlignment(Pos.TOP_LEFT);

        mainNode.changingId(getCommandName());
        stackPane.lookup("#modeChoiceBox").setId(getCommandName() + "modeChoiceBox");

        setButtonsActions();

        getOkButton().setOnAction(this::pushOkButton);

        return stackPane;
    }

    @Override
    public void pushOkButton(ActionEvent event) {
        TicketBuilder ticketBuilder = mainNode.getTicketBuilder(getCommandName());
        if (ticketBuilder.readyTCreate()) {
            closeThisView();
            ticketBuilder.setUserName(clientLogic.userName);
            mainNode.clearNode(getCommandName());
            threadPool.execute(() -> {
                String mode = ((ChoiceBox<String>) stackPane.lookup("#" + getCommandName() + "modeChoiceBox")).getValue();
                String req = clientLogic.communicatingWithServer(new Command(new String[]{getCommandName() + (mode.equals("normal") ? "" : "_" + mode)}, ticketBuilder, clientLogic.userName, clientLogic.userPassword)).text();
                Platform.runLater(() -> {
                    if (!req.equals("OK")) {
                        try {
                            if (req.equals("Объект не добавлен"))
                                ExplanationPopup.show("Объект не добавлен", getCommandName() + "_" + mode, primaryStage);
                            else ErrorWindow.show(req, "Ошибка при выполнении команды " + getCommandName());
                        } catch (IOException ignored) {
                        }
                    }
                });
            });
        }
    }
}
