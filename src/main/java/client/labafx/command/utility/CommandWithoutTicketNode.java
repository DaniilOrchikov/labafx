package client.labafx.command.utility;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.ResourceBundle;

public class CommandWithoutTicketNode extends CommandNode {

    @Override
    public NodeWithOpenAndCloseTransition getRootNode() throws IOException {
        stackPane = createStackPane();
        stackPane.setAlignment(Pos.TOP_LEFT);
        openTransition = createOpenTransition(stackPane, stackPane.getChildren());
        closeTransition = createCloseTransition(stackPane, stackPane.getChildren());

        stackPane.setTranslateX(TRANSLATE_X);
        stackPane.setTranslateY(TRANSLATE_Y);
        return new NodeWithOpenAndCloseTransition(stackPane, openTransition, closeTransition);
    }

    @Override
    StackPane createStackPane() throws IOException {
        StackPane stackPane = new StackPane();
        stackPane.getChildren().add(createVbox("command-buttons.fxml"));
        return stackPane;
    }

    @Override
    public void changingId(String commandName) {
        super.changingId(commandName);
        stackPane.lookup("#commandNameLabel").setId(commandName + "commandNameLabel");
    }

    @Override
    public void clearNode(String name) {
    }

    @Override
    public void changeLocale(ResourceBundle bundle, String commandName) {
        super.changeLocale(bundle, commandName);
        ((Label) stackPane.lookup("#" + commandName + "commandNameLabel")).setText(bundle.getString("button." + commandName));
    }
}
