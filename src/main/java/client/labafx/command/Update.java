package client.labafx.command;

import client.labafx.ClientLogic;
import client.labafx.ErrorWindow;
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

public class Update extends GUICommand {
    public Update(ClientLogic clientLogic) {
        super("update", clientLogic);
    }

    @Override
    public StackPane createRootNode(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        ticketNode = new TicketNode();
        ticketNode.getRootNode();

        VBox vBox = new FXMLLoader(MainWindow.class.getResource("ticket-layer-update.fxml")).load();
        NodeWithOpenAndCloseTransition node = ticketNode.addNode(1, vBox);

        stackPane = (StackPane) node.node();
        stackPane.setAlignment(Pos.TOP_LEFT);
        openTransition = node.openTransition();
        closeTransition = node.closeTransition();

        changingId();
        stackPane.lookup("#idChoiceBox").setId(getName() + "idChoiceBox");

        setButtonsActions();
        button.addEventHandler(ActionEvent.ACTION, event -> pushButton(vBox));
        Button okButton = ((Button) stackPane.lookup("#" + getName() + "OKButton"));
        okButton.setOnAction(this::pushOkButton);

        return stackPane;
    }

    private void pushButton(VBox vBox) {
        Runnable task = () -> {
            String answer = clientLogic.communicatingWithServer(new Command(new String[]{"get_all_valid_id"}, clientLogic.userName, clientLogic.userPassword)).text();
            if (answer.matches("^\\[\\d+(,\\s*\\d+)*]$")) {
                answer = answer.replaceAll("\\[|]", "");
                String[] strIdArr = answer.split(", ");
                Platform.runLater(() -> {
                    ChoiceBox<Long> choiceBox = (ChoiceBox<Long>) vBox.lookup("#" + getName() + "idChoiceBox");
                    choiceBox.getItems().clear();
                    for (String s : strIdArr) {
                        choiceBox.getItems().add(Long.parseLong(s));
                    }
                });
            } else if (!answer.equals("[]")) {
                String finalAnswer = answer;
                Platform.runLater(() -> {
                    ((ChoiceBox<Long>) vBox.lookup("#idChoiceBox")).getItems().clear();
                    try {
                        ErrorWindow.show(finalAnswer, "Ошибка при выполнении команды " + getName());
                    } catch (IOException ignored) {
                    }
                });
            }
        };
        new Thread(task).start();
    }
    @Override
    public void pushOkButton(ActionEvent event) {
        TicketBuilder ticketBuilder = ticketNode.getTicketBuilder(getName());
        if (ticketBuilder.readyTCreate()) {
            closeThisView();
            threadPool.execute(() -> {
                String req = clientLogic.communicatingWithServer(new Command(new String[]{getName(), ((ChoiceBox<Long>) stackPane.lookup("#" + getName() + "idChoiceBox")).getValue().toString()}, ticketBuilder, clientLogic.userName, clientLogic.userPassword)).text();
                Platform.runLater(() -> {
                    if (!req.equals("OK")) {
                        try {
                            ErrorWindow.show(req, "Ошибка при выполнении команды " + getName());
                        } catch (IOException ignored) {
                        }
                    }
                });
            });
        }
    }
}
