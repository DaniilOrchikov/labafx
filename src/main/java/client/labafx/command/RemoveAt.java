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
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import utility.Command;

import java.io.IOException;
import java.util.ResourceBundle;

public class RemoveAt extends GUICommand {
    CommandWithoutTicketNode mainNode;

    public RemoveAt(ClientLogic clientLogic) {
        super("remove_at", "RemoveAt", clientLogic);
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
        NodeWithOpenAndCloseTransition node = mainNode.addNode(1, new FXMLLoader(MainWindow.class.getResource("command-remove_at-view.fxml")).load());

        stackPane = (StackPane) node.node();
        openTransition = node.openTransition();
        closeTransition = node.closeTransition();
        stackPane.setAlignment(Pos.TOP_LEFT);

        ((Spinner<Integer>) stackPane.lookup("#remove_atSpinner")).setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0, 0));

        mainNode.changingId(getCommandName());

        setButtonsActions();
        button.addEventHandler(ActionEvent.ACTION, event -> pushButton());
        getOkButton().setOnAction(this::pushOkButton);

        return stackPane;
    }

    @Override
    public void pushOkButton(ActionEvent event) {
        Integer index = ((Spinner<Integer>) stackPane.lookup("#remove_atSpinner")).getValue();
        closeThisView();
        threadPool.execute(() -> {
            String req = clientLogic.communicatingWithServer(new Command(new String[]{getCommandName(), index.toString()}, clientLogic.userName, clientLogic.userPassword)).text();
            Platform.runLater(() -> {
                if (!req.equals("OK")) {
                    try {
                        if (req.equals("access error"))
                            ExplanationPopup.show("Не удалось получить доступ к объекту", getCommandName(), primaryStage);
                        else ErrorWindow.show(req, "Ошибка при выполнении команды " + getCommandName());
                    } catch (IOException ignored) {
                    }
                }
            });
        });

    }

    private void pushButton() {
        if (clientLogic.getTicketArraySize() == 0) {
            try {
                ExplanationPopup.show("В коллекции отсутствуют объекты", getCommandName(), primaryStage);
            } catch (IOException ignored) {
            }
        } else {
            openTransition.play();
            Spinner<Integer> spinner = (Spinner<Integer>) stackPane.lookup("#remove_atSpinner");
            ((SpinnerValueFactory.IntegerSpinnerValueFactory) spinner.getValueFactory()).setMax(clientLogic.getTicketArraySize() - 1);
            spinner.getValueFactory().setValue(0);
            CommandNode.setRangeValidator(spinner, 0, clientLogic.getTicketArraySize());
        }
    }
    @Override
    public void changeLocale(ResourceBundle bundle) {
        super.changeLocale(bundle);
        ((Label)stackPane.lookup("#indexLabel")).setText(bundle.getString("label.indexLabel"));
    }
}
