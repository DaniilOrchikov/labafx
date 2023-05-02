package client.labafx.command;

import client.labafx.ClientLogic;
import client.labafx.ErrorWindow;
import client.labafx.ExplanationPopup;
import client.labafx.MainWindow;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ticket.TicketBuilder;
import utility.Command;

import java.io.IOException;

public class RemoveLower extends GUICommand {
    public RemoveLower(ClientLogic clientLogic) {
        super("remove_lower", clientLogic);
    }

    @Override
    public StackPane createRootNode(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        ticketNode = new TicketNode();
        NodeWithOpenAndCloseTransition node = ticketNode.getRootNode();

        stackPane = (StackPane) node.node();
        openTransition = node.openTransition();
        closeTransition = node.closeTransition();
        stackPane.setAlignment(Pos.TOP_LEFT);

        changingId();

        setButtonsActions();
        Button okButton = ((Button) stackPane.lookup("#" + getName() + "OKButton"));
        okButton.setOnAction(this::pushOkButton);

        return stackPane;
    }

    @Override
    public void pushOkButton(ActionEvent event) {
        TicketBuilder ticketBuilder = ticketNode.getTicketBuilder(getName());
        if (ticketBuilder.readyTCreate()) {
            closeThisView();
            threadPool.execute(() -> {
                String req = clientLogic.communicatingWithServer(new Command(new String[]{getName()}, ticketBuilder, clientLogic.userName, clientLogic.userPassword)).text();
                Platform.runLater(() -> {
                    try {
                        if (!req.matches("^[0-9]+$")) {
                            ErrorWindow.show(req, "Ошибка при выполнении команды " + getName());
                        } else ExplanationPopup.show("Удалено объектов - " + req, getName(), primaryStage);
                    } catch (IOException ignored) {
                    }
                });
            });
        }
    }
}
