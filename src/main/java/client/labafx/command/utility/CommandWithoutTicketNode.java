package client.labafx.command.utility;

import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;

import java.io.IOException;

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
    public void clearNode(String name) {}
}
