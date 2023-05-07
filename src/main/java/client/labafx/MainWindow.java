package client.labafx;

import client.labafx.command.*;
import client.labafx.table.TicketTable;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainWindow extends Application {
    private MainController controller;
    private Command[] commands;
    private GUICommand[] guiCommands;
    private ClientLogic clientLogic;
    private ExecutorService threadPool = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        launch(args);
    }

    public void setClientLogic(ClientLogic clientLogic) {
        this.clientLogic = clientLogic;
    }

    public void createCommands() {
        guiCommands = new GUICommand[]{new Add(clientLogic), new Update(clientLogic), new RemoveLower(clientLogic), new RemoveById(clientLogic), new RemoveAt(clientLogic)};
        for (GUICommand command : guiCommands) {
            command.setCommands(guiCommands);
        }
        commands = new Command[]{new Show(clientLogic), new Command("clear", "Clear", clientLogic), new Command("remove_first", "RemoveFirst", clientLogic)};
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlMainLoader = new FXMLLoader(MainWindow.class.getResource("main-view.fxml"));
        Scene mainScene = new Scene(fxmlMainLoader.load());
        controller = fxmlMainLoader.getController();
        controller.setCommands(guiCommands, commands);
        for (GUICommand command : guiCommands) {
            controller.getLeftStackPane().getChildren().add(0, command.createRootNode(stage));
        }
        controller.setName(clientLogic.userName);
        stage.setMinWidth(controller.getMainPane().getMinWidth());
        stage.setMinHeight(controller.getMainPane().getMinHeight());

        threadPool.execute(() -> {
            TicketTable table = new TicketTable(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), clientLogic);
            table.refresh(clientLogic.getTickets());
            Platform.runLater(() -> ((TabPane) mainScene.lookup("#tableTabPane")).getTabs().add(table.getMainNode()));
        });

        for (Command command : commands)
            command.setButtonsActions();

        stage.setMaximized(true);
        stage.setScene(mainScene);
        stage.show();
    }

    public MainController getController() {
        return controller;
    }
}