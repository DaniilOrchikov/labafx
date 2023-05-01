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

public class Update extends GUICommand {
    public Update(ClientLogic clientLogic) {
        super("update", clientLogic);
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
        VBox vBox = new FXMLLoader(MainWindow.class.getResource("ticket-layer-update.fxml")).load();
        NodeWithOpenAndCloseTransition node = ticketNode.addNode(1, vBox);
        stackPane = (StackPane) node.node();
        changingId();
        stackPane.lookup("#idChoiceBox").setId(getName() + "idChoiceBox");
        openTransition = node.openTransition();
        closeTransition = node.closeTransition();
        stackPane.setAlignment(Pos.TOP_LEFT);

        button.setOnAction(event -> {
            openTransition.play();
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
                }else if (!answer.equals("[]")){
                    String finalAnswer = answer;
                    Platform.runLater(() -> {
                        ((ChoiceBox<Long>) vBox.lookup("#idChoiceBox")).getItems().clear();
                        try {
                            ErrorView.show(finalAnswer, "Ошибка при выполнении команды " + getName());
                        } catch (IOException ignored) {
                        }
                    });
                }
            };
            new Thread(task).start();
        });
        button.addEventHandler(ActionEvent.ACTION, event -> closeAllCommandsView());
        ((Button)stackPane.lookup("#" + getName() + "cancelButton")).setOnAction(event -> closeThisView());
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
        ticketBuilder.setVenueCapacity(String.valueOf(((Spinner<Long>) stackPane.lookup("#" + getName() + "capacitySpinner")).getValue()));
        ticketBuilder.setType(((ChoiceBox<String>) stackPane.lookup("#" + getName() + "ticketTypeChoiceBox")).getValue());
        ticketBuilder.setVenueType(((ChoiceBox<String>) stackPane.lookup("#" + getName() + "venueTypeChoiceBox")).getValue());
        if (!errors) {
            closeThisView();
            closeTransition.play();
            Runnable task = () -> {
                String req = clientLogic.communicatingWithServer(new Command(new String[]{getName(), ((ChoiceBox<Long>) stackPane.lookup("#" + getName() + "idChoiceBox")).getValue().toString()}, ticketBuilder, clientLogic.userName, clientLogic.userPassword)).text();
                Platform.runLater(() -> {
                    System.out.println(req);
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
