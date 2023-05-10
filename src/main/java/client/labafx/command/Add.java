package client.labafx.command;

import client.labafx.*;
import client.labafx.command.utility.CommandNode;
import client.labafx.command.utility.CommandWithTicketNode;
import client.labafx.command.utility.NodeWithOpenAndCloseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ticket.TicketBuilder;
import utility.Command;

import java.io.IOException;
import java.util.ResourceBundle;

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
        ChoiceBox<LocalString> choiceBox = (ChoiceBox<LocalString>) vBox.lookup("#modeChoiceBox");
        choiceBox.getItems().addAll(new LocalString("normal", "normal"), new LocalString("if_max", "if_max"), new LocalString("if_min", "if_min"));
        choiceBox.setValue(new LocalString("normal", "normal"));
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
                String mode = ((ChoiceBox<LocalString>) stackPane.lookup("#" + getCommandName() + "modeChoiceBox")).getValue().getName();
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

    @Override
    public void changeLocale(ResourceBundle bundle) {
        super.changeLocale(bundle);
        ((Label) stackPane.lookup("#modeLabel")).setText(bundle.getString("label.modeLabel"));
        ((Label) stackPane.lookup("#titleModeLabel")).setText(bundle.getString("label.titleModeLabel"));
        ChoiceBox<LocalString> choiceBox = (ChoiceBox<LocalString>) stackPane.lookup("#" + getCommandName() + "modeChoiceBox");
        choiceBox.getItems().clear();
        choiceBox.getItems().addAll(new LocalString("normal", bundle.getString("normal")), new LocalString("if_max", bundle.getString("if_max")), new LocalString("if_min", bundle.getString("if_min")));
        choiceBox.setValue(new LocalString("normal", bundle.getString("normal")));
    }
}
