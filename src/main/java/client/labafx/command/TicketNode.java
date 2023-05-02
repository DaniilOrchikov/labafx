package client.labafx.command;

import client.labafx.MainWindow;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import ticket.TicketBuilder;
import ticket.TicketType;
import ticket.VenueType;

import java.io.IOException;
import java.util.Arrays;

public class TicketNode {
    private final int TRANSLATE_X = -80;
    private final int TRANSLATE_Y = 60;
    private StackPane stackPane;
    private ParallelTransition openTransition;
    private ParallelTransition closeTransition;

    /**
     * Изначально 4 ноды
     */
    public NodeWithOpenAndCloseTransition addNode(int id, Node node) {
        stackPane.getChildren().add(id, node);
        openTransition = createOpenTransition(stackPane, stackPane.getChildren());
        closeTransition = createCloseTransition(stackPane, stackPane.getChildren());
        return new NodeWithOpenAndCloseTransition(stackPane, openTransition, closeTransition);
    }

    private boolean isInteger(String value) {
        if (!value.matches("\\d*")) return false;
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isLong(String value) {
        if (!value.matches("\\d*")) return false;
        return value.length() <= 17;
    }

    public void setIntegerValidator(Spinner<?> spinner) {
        spinner.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(""))
                spinner.getEditor().setText("1");
            else if (!isInteger(newValue)) {
                spinner.getEditor().setText(oldValue);
            }
        });
    }

    public void setLongValidator(Spinner<?> spinner) {
        spinner.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(""))
                spinner.getEditor().setText("1");
            else if (!isLong(newValue)) {
                spinner.getEditor().setText(oldValue);
            }
        });
    }

    public NodeWithOpenAndCloseTransition getRootNode() throws IOException {
        stackPane = createStackPane();
        stackPane.setAlignment(Pos.TOP_LEFT);
        openTransition = createOpenTransition(stackPane, stackPane.getChildren());
        closeTransition = createCloseTransition(stackPane, stackPane.getChildren());
        ChoiceBox<String> ticketTypeChoiceBox = (ChoiceBox<String>) stackPane.lookup("#ticketTypeChoiceBox");
        for (TicketType t : TicketType.values()) {
            ticketTypeChoiceBox.getItems().add(t.toString());
        }
        ticketTypeChoiceBox.setValue(ticketTypeChoiceBox.getItems().get(0));

        ChoiceBox<String> venueTypeChoiceBox = (ChoiceBox<String>) stackPane.lookup("#venueTypeChoiceBox");
        for (VenueType t : VenueType.values()) {
            venueTypeChoiceBox.getItems().add(t.toString());
        }
        venueTypeChoiceBox.setValue(venueTypeChoiceBox.getItems().get(0));
        setIntegerValidator((Spinner<Integer>) stackPane.lookup("#xSpinner"));
        setIntegerValidator((Spinner<Integer>) stackPane.lookup("#ySpinner"));
        setIntegerValidator((Spinner<Integer>) stackPane.lookup("#priceSpinner"));
        setLongValidator((Spinner<Long>) stackPane.lookup("#capacitySpinner"));

        stackPane.setTranslateX(TRANSLATE_X);
        stackPane.setTranslateY(TRANSLATE_Y);
        return new NodeWithOpenAndCloseTransition(stackPane, openTransition, closeTransition);
    }

    private StackPane createStackPane() throws IOException {
        StackPane stackPane = new StackPane();
        stackPane.getChildren().add(createVbox("ticket-layer-buttons.fxml"));
        stackPane.getChildren().add(createVbox("ticket-layer-3.fxml"));
        stackPane.getChildren().add(createVbox("ticket-layer-2.fxml"));
        stackPane.getChildren().add(createVbox("ticket-layer-1.fxml"));
        return stackPane;
    }

    public static VBox createVbox(String filename) throws IOException {
        FXMLLoader fxmlMainLoader = new FXMLLoader(MainWindow.class.getResource(filename));
        return fxmlMainLoader.load();
    }


    private ParallelTransition createOpenTransition(Parent parentElement, ObservableList<Node> childElements) {
        ParallelTransition parallelTransition = AddMovementToNode.getOpenTransition(parentElement);
        for (int i = 0; i < childElements.size(); i++) {
            TranslateTransition childTransition = new TranslateTransition(Duration.millis(600), childElements.get(i));
            childTransition.setToY(Arrays.stream(childElements.toArray(), i + 1, childElements.size()).mapToInt(ticket -> (int) ((VBox) ticket).getMinHeight()).sum() - 20 * (childElements.size() - i - 1));
            parallelTransition.getChildren().add(childTransition);
        }

        return parallelTransition;
    }

    public static Long getLongValueFromStringDouble(String value) {
        if (value.matches(".*E.*")) {
            double d = Double.parseDouble(value.split("E")[0]);
            long digit = Integer.parseInt(value.split("E")[1]);
            return (long)(d * Math.pow(10, digit));
        } else {
            return Long.parseLong(value.replace(".0", ""));
        }
    }

    private ParallelTransition createCloseTransition(Parent parentElement, ObservableList<Node> childElements) {
        ParallelTransition parallelTransition = AddMovementToNode.getCloseTransition(parentElement);
        for (Node childElement : childElements) {
            TranslateTransition childTransition = new TranslateTransition(Duration.millis(500), childElement);
            childTransition.setToY(0);
            parallelTransition.getChildren().add(childTransition);
        }

        return parallelTransition;
    }

    public TicketBuilder getTicketBuilder(String commandName) {
        TicketBuilder ticketBuilder = new TicketBuilder();
        if (!ticketBuilder.setName(((TextField) stackPane.lookup("#" + commandName + "nameField")).getText()).equals("OK")) {
            stackPane.lookup("#" + commandName + "nameLabel").setStyle("-fx-text-fill: red;");
        }
        if (!ticketBuilder.setAddressZipCode(((TextField) stackPane.lookup("#" + commandName + "zipCodeField")).getText()).equals("OK")) {
            stackPane.lookup("#" + commandName + "zipCodeLabel").setStyle("-fx-text-fill: red;");
        }
        if (!ticketBuilder.setAddressStreet(((TextField) stackPane.lookup("#" + commandName + "streetField")).getText()).equals("OK")) {
            stackPane.lookup("#" + commandName + "streetLabel").setStyle("-fx-text-fill: red;");
        }
        ticketBuilder.setX(((Spinner<Integer>) stackPane.lookup("#" + commandName + "xSpinner")).getValue().toString());
        ticketBuilder.setY(((Spinner<Integer>) stackPane.lookup("#" + commandName + "ySpinner")).getValue().toString());
        ticketBuilder.setPrice(((Spinner<Integer>) stackPane.lookup("#" + commandName + "priceSpinner")).getValue().toString());
        ticketBuilder.setVenueCapacity(getLongValueFromStringDouble(String.valueOf(((Spinner<Long>) stackPane.lookup("#" + commandName + "capacitySpinner")).getValue())).toString());
        ticketBuilder.setType(((ChoiceBox<String>) stackPane.lookup("#" + commandName + "ticketTypeChoiceBox")).getValue());
        ticketBuilder.setVenueType(((ChoiceBox<String>) stackPane.lookup("#" + commandName + "venueTypeChoiceBox")).getValue());
        return ticketBuilder;
    }
}
