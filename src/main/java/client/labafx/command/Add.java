package client.labafx.command;

import client.labafx.ClientLogic;
import client.labafx.ErrorView;
import client.labafx.MainWindow;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ticket.TicketBuilder;
import utility.Command;

import java.io.IOException;

public class Add extends GUICommand {

    public Add(ClientLogic clientLogic) {
        super("add", clientLogic);
    }

    @Override
    public void setButton(Button button) {
        this.button = button;
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
        button.setOnAction(event -> openTransition.play());
        button.addEventHandler(ActionEvent.ACTION, event -> closeAllCommandsView());
        ((Button) stackPane.lookup("#" + getName() + "cancelButton")).setOnAction(event -> closeThisView());
        Button okButton = ((Button) stackPane.lookup("#" + getName() + "OKButton"));
        okButton.setOnAction(this::pushOkButton);
        return stackPane;
    }

    private void pushOkButton(ActionEvent event) {
        TicketBuilder ticketBuilder = new TicketBuilder();
        boolean errors = false;
        if (!ticketBuilder.setName(((TextField) stackPane.lookup("#" + getName() + "nameField")).getText()).equals("OK")) {
            stackPane.lookup("#" + getName() + "nameLabel").setStyle("-fx-text-fill: red;");
            errors = true;
        }
        if (!ticketBuilder.setAddressZipCode(((TextField) stackPane.lookup("#" + getName() + "zipCodeField")).getText()).equals("OK")) {
            stackPane.lookup("#" + getName() + "zipCodeLabel").setStyle("-fx-text-fill: red;");
            errors = true;
        }
        if (!ticketBuilder.setAddressStreet(((TextField) stackPane.lookup("#" + getName() + "streetField")).getText()).equals("OK")) {
            stackPane.lookup("#" + getName() + "streetLabel").setStyle("-fx-text-fill: red;");
            errors = true;
        }
        ticketBuilder.setX(((Spinner<Integer>) stackPane.lookup("#" + getName() + "xSpinner")).getValue().toString());
        ticketBuilder.setY(((Spinner<Integer>) stackPane.lookup("#" + getName() + "ySpinner")).getValue().toString());
        ticketBuilder.setPrice(((Spinner<Integer>) stackPane.lookup("#" + getName() + "priceSpinner")).getValue().toString());
        String capacity = String.valueOf(((Spinner<Double>) stackPane.lookup("#" + getName() + "capacitySpinner")).getValue());
        capacity = capacity.substring(0, capacity.length() - 2);
        ticketBuilder.setVenueCapacity(capacity);
        ticketBuilder.setType(((ChoiceBox<String>) stackPane.lookup("#" + getName() + "ticketTypeChoiceBox")).getValue());
        ticketBuilder.setVenueType(((ChoiceBox<String>) stackPane.lookup("#" + getName() + "venueTypeChoiceBox")).getValue());
        if (!errors) {
            closeThisView();
            closeTransition.play();
            Runnable task = () -> {
                String mode = ((ChoiceBox<String>) stackPane.lookup("#" + getName() + "modeChoiceBox")).getValue();
                String req = clientLogic.communicatingWithServer(new Command(new String[]{getName() + (mode.equals("normal") ? "" : "_" + mode)}, ticketBuilder, clientLogic.userName, clientLogic.userPassword)).text();
                Platform.runLater(() -> {
                    if (!req.equals("OK")) {
                        try {
                            ErrorView.show(req, "Ошибка при выполнении команды " + getName());
                        } catch (IOException ignored) {
                        }
                    }
                });
            };
            Thread thread = new Thread(task);
            thread.start();
        }
    }
}
