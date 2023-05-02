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

//TODO: нужно сделать корректный возврат при add_if_max/min
public class Add extends GUICommand {

    public Add(ClientLogic clientLogic) {
        super("add", clientLogic);
    }

    @Override
    public StackPane createRootNode(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        ticketNode = new TicketNode();
        ticketNode.getRootNode();

        VBox vBox = new FXMLLoader(MainWindow.class.getResource("ticket-layer-selecting-add-mode.fxml")).load();
        ChoiceBox<String> choiceBox = (ChoiceBox<String>) vBox.lookup("#modeChoiceBox");
        choiceBox.getItems().addAll("normal", "if_max", "if_min");
        choiceBox.setValue("normal");
        NodeWithOpenAndCloseTransition node = ticketNode.addNode(1, vBox);

        stackPane = (StackPane) node.node();
        openTransition = node.openTransition();
        closeTransition = node.closeTransition();
        stackPane.setAlignment(Pos.TOP_LEFT);

        changingId();
        stackPane.lookup("#modeChoiceBox").setId(getName() + "modeChoiceBox");

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
                String mode = ((ChoiceBox<String>) stackPane.lookup("#" + getName() + "modeChoiceBox")).getValue();
                String req = clientLogic.communicatingWithServer(new Command(new String[]{getName() + (mode.equals("normal") ? "" : "_" + mode)}, ticketBuilder, clientLogic.userName, clientLogic.userPassword)).text();
                Platform.runLater(() -> {
                    if (!req.equals("OK")) {
                        try {
                            if (req.equals("Объект не добавлен")) {
                                ExplanationPopup.show("Объект не добавлен", getName() + "_" + mode, primaryStage);
                            } else ErrorWindow.show(req, "Ошибка при выполнении команды " + getName());
                        } catch (IOException ignored) {
                        }
                    }
                });
            });
        }
    }
}
