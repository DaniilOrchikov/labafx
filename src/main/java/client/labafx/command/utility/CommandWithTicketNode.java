package client.labafx.command.utility;

import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import ticket.TicketBuilder;
import ticket.TicketType;
import ticket.VenueType;

import java.io.IOException;

public class CommandWithTicketNode extends CommandNode {
    @Override
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

    @Override
    StackPane createStackPane() throws IOException {
        StackPane stackPane = new StackPane();
        stackPane.getChildren().add(createVbox("command-buttons.fxml"));
        stackPane.getChildren().add(createVbox("ticket-layer-3.fxml"));
        stackPane.getChildren().add(createVbox("ticket-layer-2.fxml"));
        stackPane.getChildren().add(createVbox("ticket-layer-1.fxml"));
        return stackPane;
    }

    @Override
    public void changingId(String commandName) {
        {
            super.changingId(commandName);
            stackPane.lookup("#nameField").setId(commandName + "nameField");
            stackPane.lookup("#zipCodeField").setId(commandName + "zipCodeField");
            stackPane.lookup("#streetField").setId(commandName + "streetField");
            stackPane.lookup("#xSpinner").setId(commandName + "xSpinner");
            stackPane.lookup("#ySpinner").setId(commandName + "ySpinner");
            stackPane.lookup("#priceSpinner").setId(commandName + "priceSpinner");
            stackPane.lookup("#capacitySpinner").setId(commandName + "capacitySpinner");
            stackPane.lookup("#ticketTypeChoiceBox").setId(commandName + "ticketTypeChoiceBox");
            stackPane.lookup("#venueTypeChoiceBox").setId(commandName + "venueTypeChoiceBox");
            stackPane.lookup("#nameLabel").setId(commandName + "nameLabel");
            stackPane.lookup("#zipCodeLabel").setId(commandName + "zipCodeLabel");
            stackPane.lookup("#streetLabel").setId(commandName + "streetLabel");
        }
    }

    public static Long getLongValueFromStringDouble(String value) {
        if (value.matches(".*E.*")) {
            double d = Double.parseDouble(value.split("E")[0]);
            long digit = Integer.parseInt(value.split("E")[1]);
            return (long) (d * Math.pow(10, digit));
        } else {
            return Long.parseLong(value.replace(".0", ""));
        }
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

    @Override
    public void clearNode(String commandName) {
        ((TextField) stackPane.lookup("#" + commandName + "nameField")).clear();
        ((Spinner<Integer>) stackPane.lookup("#" + commandName + "xSpinner")).getValueFactory().setValue(1);
        ((Spinner<Integer>) stackPane.lookup("#" + commandName + "ySpinner")).getValueFactory().setValue(1);
        ((TextField) stackPane.lookup("#" + commandName + "zipCodeField")).clear();
        ((TextField) stackPane.lookup("#" + commandName + "streetField")).clear();
        ((Spinner<Integer>) stackPane.lookup("#" + commandName + "priceSpinner")).getValueFactory().setValue(1);
        ((Spinner<Double>) stackPane.lookup("#" + commandName + "capacitySpinner")).getValueFactory().setValue(1d);
        stackPane.lookup("#" + commandName + "streetLabel").setStyle("-fx-text-fill: black;");
        stackPane.lookup("#" + commandName + "zipCodeLabel").setStyle("-fx-text-fill: black;");
        stackPane.lookup("#" + commandName + "nameLabel").setStyle("-fx-text-fill: black;");
    }
}
