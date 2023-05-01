package client.labafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AuthorizationWindow extends Application {
    private AuthorizationController controller;
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlAuthorizationLoader = new FXMLLoader(getClass().getResource("authorization-view.fxml"));
        Scene authorizationScene = new Scene(fxmlAuthorizationLoader.load());
        controller = fxmlAuthorizationLoader.getController();
        stage.setScene(authorizationScene);
        stage.show();
    }
    public AuthorizationController getController(){
        return controller;
    }
}
