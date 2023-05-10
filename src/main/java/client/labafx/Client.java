package client.labafx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

public class Client extends Application {
    private final AuthorizationWindow authorizationWindow;
    private AuthorizationController authorizationController;
    private final MainWindow mainWindow;
    private MainController mainController;
    private Stage primaryStage;

    private ClientLogic clientLogic;


    public Client() {
        authorizationWindow = new AuthorizationWindow();
        mainWindow = new MainWindow();
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        clientLogic = new ClientLogic();
        mainWindow.setClientLogic(clientLogic);
        mainWindow.createCommands();
        ScheduledExecutorService sPool = Executors.newScheduledThreadPool(2);
        sPool.scheduleWithFixedDelay(() -> {
            try {
                if (clientLogic.userName != null) {
                    if (clientLogic.isCollectionUpdated()) {
                        clientLogic.updateMyCollection();
                        Platform.runLater(() -> ((Label) mainController.getMainPane().lookup("#numberOfElementsLabel")).setText(clientLogic.getTicketArraySize().toString()));
                    }
                }
            } catch (IOException | InterruptedException | ClassNotFoundException | NumberFormatException e) {
                e.printStackTrace();
            }
        }, 0, 1200, TimeUnit.MILLISECONDS);
        this.primaryStage = stage;
        primaryStage.setOnCloseRequest(we -> clientLogic.exit());
        authorizationWindow.start(primaryStage);
        authorizationController = authorizationWindow.getController();

        authorizationController.setClientLogic(this);
    }

    public void openMainWindow() throws IOException {
        primaryStage.close();
        primaryStage = new Stage();
        primaryStage.setOnCloseRequest(we -> clientLogic.exit());
        mainWindow.start(primaryStage);
        mainController = mainWindow.getController();
    }

    public ClientLogic getClientLogic() {
        return clientLogic;
    }
}