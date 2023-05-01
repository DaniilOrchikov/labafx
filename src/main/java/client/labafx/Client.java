package client.labafx;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

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
        try {
            clientLogic = new ClientLogic(new ConsoleWriter());
            mainWindow.setClientLogic(clientLogic);
            mainWindow.createCommands();
//            clientLogic.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Runnable isCollectionUpdated = () -> {
            while (true) {
                try {
                    if (clientLogic.isCollectionUpdated()) {
                        clientLogic.updateMyCollection();
                    }
                    sleep(400);
                } catch (IOException | InterruptedException | ClassNotFoundException | NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(isCollectionUpdated);
        thread.start();
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