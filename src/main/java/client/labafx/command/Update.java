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
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ticket.TicketBuilder;
import utility.Command;

import java.io.IOException;

public class Update extends GUICommand {
    CommandWithTicketNode mainNode;

    public Update(ClientLogic clientLogic) {
        super("update", "Update", clientLogic);
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

        VBox vBox = new FXMLLoader(MainWindow.class.getResource("command-update.fxml")).load();
        NodeWithOpenAndCloseTransition node = mainNode.addNode(1, vBox);

        stackPane = (StackPane) node.node();
        stackPane.setAlignment(Pos.TOP_LEFT);
        openTransition = node.openTransition();
        closeTransition = node.closeTransition();

        mainNode.changingId(getCommandName());

        stackPane.lookup("#idChoiceBox").setId(getCommandName() + "idChoiceBox");

        setButtonsActions();
        button.addEventHandler(ActionEvent.ACTION, event -> pushButton(vBox));
        getOkButton().setOnAction(this::pushOkButton);
        getCancelButton().addEventHandler(ActionEvent.ACTION, event -> stackPane.lookup("#" + getCommandName() + "idChoiceBox").setStyle("-fx-text-fill: black;"));

        return stackPane;
    }

    private void pushButton(VBox vBox) {
        Runnable task = () -> {
            String answer = clientLogic.communicatingWithServer(new Command(new String[]{"get_all_valid_id"}, clientLogic.userName, clientLogic.userPassword)).text();
            if (answer.matches("^\\[\\d+(,\\s*\\d+)*]$")) {
                openTransition.play();
                answer = answer.replaceAll("\\[|]", "");
                String[] strIdArr = answer.split(", ");
                Platform.runLater(() -> {
                    ChoiceBox<Long> choiceBox = (ChoiceBox<Long>) vBox.lookup("#" + getCommandName() + "idChoiceBox");
                    choiceBox.getItems().clear();
                    for (String s : strIdArr) {
                        choiceBox.getItems().add(Long.parseLong(s));
                    }
                });
            } else if (!answer.equals("[]")) {
                String finalAnswer = answer;
                Platform.runLater(() -> {
                    ((ChoiceBox<Long>) vBox.lookup("#updateidChoiceBox")).getItems().clear();
                    try {
                        ErrorWindow.show(finalAnswer, "Ошибка при выполнении команды " + getCommandName());
                    } catch (IOException ignored) {
                    }
                });
            } else Platform.runLater(() -> {
                ((ChoiceBox<Long>) vBox.lookup("#" + getCommandName() + "idChoiceBox")).getItems().clear();
                try {
                    ExplanationPopup.show("В коллекции отсутствуют доступные вам объекты", getCommandName(), primaryStage);
                } catch (IOException ignored) {
                }
            });
        };
        new Thread(task).start();
    }

    @Override
    public void pushOkButton(ActionEvent event) {
        TicketBuilder ticketBuilder = mainNode.getTicketBuilder(getCommandName());
        Long id = ((ChoiceBox<Long>) stackPane.lookup("#" + getCommandName() + "idChoiceBox")).getValue();
        if (id == null) {
            stackPane.lookup("#updateidLabel").setStyle("-fx-text-fill: red;");
            return;
        }
        if (ticketBuilder.readyTCreate()) {
            ticketBuilder.setUserName(clientLogic.userName);
            closeThisView();
            mainNode.clearNode(getCommandName());
            threadPool.execute(() -> {
                String req = clientLogic.communicatingWithServer(new Command(new String[]{getCommandName(), id.toString()}, ticketBuilder, clientLogic.userName, clientLogic.userPassword)).text();
                Platform.runLater(() -> {
                    if (!req.equals("OK")) {
                        try {
                            ErrorWindow.show(req, "Ошибка при выполнении команды " + getCommandName());
                        } catch (IOException ignored) {
                        }
                    }
                });
            });
        }
    }
    public void fromTicketId(Long id){
        TicketBuilder ticketBuilder = clientLogic.getTBFromId(id);
        openTransition.play();
        closeAllCommandsView();
        ((TextField)stackPane.lookup("#updatenameField")).setText(ticketBuilder.getName());
        ((TextField)stackPane.lookup("#updatezipCodeField")).setText(ticketBuilder.getAddressZipCode());
        ((TextField)stackPane.lookup("#updatestreetField")).setText(ticketBuilder.getAddressStreet());
        ((Spinner<Integer>)stackPane.lookup("#updatexSpinner")).getValueFactory().setValue(ticketBuilder.getX());
        ((Spinner<Integer>)stackPane.lookup("#updateySpinner")).getValueFactory().setValue(ticketBuilder.getY());
        ((Spinner<Integer>)stackPane.lookup("#updatepriceSpinner")).getValueFactory().setValue(ticketBuilder.getPrice());
        ((Spinner<Double>)stackPane.lookup("#updatecapacitySpinner")).getValueFactory().setValue(ticketBuilder.getVenueCapacity().doubleValue());
        ((ChoiceBox<String>)stackPane.lookup("#updateticketTypeChoiceBox")).setValue(ticketBuilder.getType().toString());
        ((ChoiceBox<String>)stackPane.lookup("#updatevenueTypeChoiceBox")).setValue(ticketBuilder.getVenueType().toString());
        ((ChoiceBox<Long>)stackPane.lookup("#updateidChoiceBox")).setValue(id);
    }
}
