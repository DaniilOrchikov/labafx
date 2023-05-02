package client.labafx.command;

import client.labafx.ClientLogic;
import javafx.animation.ParallelTransition;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class GUICommand {
    private final String name;
    final ClientLogic clientLogic;
    Button button;
    ParallelTransition openTransition;
    ParallelTransition closeTransition;
    TicketNode ticketNode;
    StackPane stackPane;
    Stage primaryStage;
    GUICommand[] commands;
    ExecutorService threadPool = Executors.newFixedThreadPool(2);

    public GUICommand(String name, ClientLogic clientLogic) {
        this.name = name;
        this.clientLogic = clientLogic;
    }

    public void closeAllCommandsView() {
        for (GUICommand command : commands) {
            if (!command.getName().equals(getName())) {
                command.closeThisView();
            }
        }
    }

    public void closeThisView() {
        ((TextField) stackPane.lookup("#" + getName() + "nameField")).clear();
        ((Spinner<Integer>) stackPane.lookup("#" + getName() + "xSpinner")).getValueFactory().setValue(1);
        ((Spinner<Integer>) stackPane.lookup("#" + getName() + "ySpinner")).getValueFactory().setValue(1);
        ((TextField) stackPane.lookup("#" + getName() + "zipCodeField")).clear();
        ((TextField) stackPane.lookup("#" + getName() + "streetField")).clear();
        ((Spinner<Integer>) stackPane.lookup("#" + getName() + "priceSpinner")).getValueFactory().setValue(1);
        ((Spinner<Double>) stackPane.lookup("#" + getName() + "capacitySpinner")).getValueFactory().setValue(1d);
        stackPane.lookup("#" + getName() + "streetLabel").setStyle("-fx-text-fill: black;");
        stackPane.lookup("#" + getName() + "zipCodeLabel").setStyle("-fx-text-fill: black;");
        stackPane.lookup("#" + getName() + "nameLabel").setStyle("-fx-text-fill: black;");
        closeTransition.play();
    }

    public void setCommands(GUICommand[] commands) {
        this.commands = commands;
    }

    public String getName() {
        return name;
    }

    public abstract StackPane createRootNode(Stage primaryStage) throws IOException;

    public void setButton(Button button) {
        this.button = button;
    }

    public void changingId() {
        stackPane.lookup("#nameField").setId(getName() + "nameField");
        stackPane.lookup("#zipCodeField").setId(getName() + "zipCodeField");
        stackPane.lookup("#streetField").setId(getName() + "streetField");
        stackPane.lookup("#xSpinner").setId(getName() + "xSpinner");
        stackPane.lookup("#ySpinner").setId(getName() + "ySpinner");
        stackPane.lookup("#priceSpinner").setId(getName() + "priceSpinner");
        stackPane.lookup("#capacitySpinner").setId(getName() + "capacitySpinner");
        stackPane.lookup("#ticketTypeChoiceBox").setId(getName() + "ticketTypeChoiceBox");
        stackPane.lookup("#venueTypeChoiceBox").setId(getName() + "venueTypeChoiceBox");
        stackPane.lookup("#nameLabel").setId(getName() + "nameLabel");
        stackPane.lookup("#zipCodeLabel").setId(getName() + "zipCodeLabel");
        stackPane.lookup("#streetLabel").setId(getName() + "streetLabel");
        stackPane.lookup("#cancelButton").setId(getName() + "cancelButton");
        stackPane.lookup("#OKButton").setId(getName() + "OKButton");
    }

    public void setButtonsActions(){
        button.setOnAction(event -> {
            openTransition.play();
            closeAllCommandsView();
        });
        ((Button) stackPane.lookup("#" + getName() + "cancelButton")).setOnAction(event -> closeThisView());
    }
    public abstract void pushOkButton(ActionEvent event);
}
