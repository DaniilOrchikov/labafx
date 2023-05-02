package client.labafx;

import client.labafx.command.Add;
import client.labafx.command.GUICommand;
import client.labafx.command.RemoveLower;
import client.labafx.command.Update;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainWindow extends Application {
    private MainController controller;
    private GUICommand[] commands;
    private ClientLogic clientLogic;

    public static void main(String[] args) {
        launch(args);
    }

    public void setClientLogic(ClientLogic clientLogic) {
        this.clientLogic = clientLogic;
    }
    public void createCommands(){
        commands = new GUICommand[]{new Add(clientLogic), new Update(clientLogic), new RemoveLower(clientLogic)};
        for(GUICommand command:commands){
            command.setCommands(commands);
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlMainLoader = new FXMLLoader(MainWindow.class.getResource("main-view.fxml"));
        Scene mainScene = new Scene(fxmlMainLoader.load());
        controller = fxmlMainLoader.getController();
        controller.setCommands(commands);
        for (GUICommand guiCommand: commands) {
            controller.getLeftStackPane().getChildren().add(0, guiCommand.createRootNode(stage));
        }
        controller.setName(clientLogic.userName);
        stage.setMinWidth(controller.getMainPane().getMinWidth());
        stage.setMinHeight(controller.getMainPane().getMinHeight());
        stage.setMaximized(true);
        stage.setScene(mainScene);
        stage.show();
    }
    public MainController getController(){
        return controller;
    }
}