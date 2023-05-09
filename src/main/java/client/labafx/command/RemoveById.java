package client.labafx.command;

import client.labafx.ClientLogic;
import client.labafx.ErrorWindow;
import client.labafx.ExplanationPopup;
import client.labafx.MainWindow;
import client.labafx.command.utility.CommandNode;
import client.labafx.command.utility.CommandWithoutTicketNode;
import client.labafx.command.utility.NodeWithOpenAndCloseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utility.Command;

import java.io.IOException;
import java.util.ResourceBundle;

public class RemoveById extends GUICommand {
    CommandWithoutTicketNode mainNode;

    public RemoveById(ClientLogic clientLogic) {
        super("remove_by_id", "RemoveById", clientLogic);
    }

    @Override
    public CommandNode getMainNode() {
        return mainNode;
    }

    @Override
    public StackPane createRootNode(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        mainNode = new CommandWithoutTicketNode();
        mainNode.getRootNode();
        NodeWithOpenAndCloseTransition node = mainNode.addNode(1, new FXMLLoader(MainWindow.class.getResource("command-remove_by_id-view.fxml")).load());

        stackPane = (StackPane) node.node();
        openTransition = node.openTransition();
        closeTransition = node.closeTransition();
        stackPane.setAlignment(Pos.TOP_LEFT);

        mainNode.changingId(getCommandName());

        setButtonsActions();
        button.addEventHandler(ActionEvent.ACTION, event -> pushButton((VBox) stackPane.lookup("#vBox")));
        getOkButton().setOnAction(this::pushOkButton);
        getCancelButton().addEventHandler(ActionEvent.ACTION, event -> stackPane.lookup("#remove_by_ididLabel").setStyle("-fx-text-fill: black;"));

        return stackPane;
    }

    @Override
    public void pushOkButton(ActionEvent event) {
        Long id = ((ChoiceBox<Long>) stackPane.lookup("#" + getCommandName() + "idChoiceBox")).getValue();
        if (id == null) {
            stackPane.lookup("#remove_by_ididLabel").setStyle("-fx-text-fill: red;");
            return;
        }
        closeThisView();
        threadPool.execute(() -> {
            String req = clientLogic.communicatingWithServer(new Command(new String[]{getCommandName(), id.toString()}, clientLogic.userName, clientLogic.userPassword)).text();
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
                    ((ChoiceBox<Long>) vBox.lookup("#" + getCommandName() + "idChoiceBox")).getItems().clear();
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
    public void changeLocale(ResourceBundle bundle) {
        super.changeLocale(bundle);
        ((Label)stackPane.lookup("#remove_by_ididLabel")).setText(bundle.getString("label.idLabel"));
    }
}
