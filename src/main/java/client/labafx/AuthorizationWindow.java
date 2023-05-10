package client.labafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class AuthorizationWindow extends Application {
    private AuthorizationController controller;
    private Scene authorizationScene;
    ResourceBundle bundle = ResourceBundle.getBundle("client.labafx.localization", new Locale("ru"));

    private void changeLocale(Locale locale) {
        Locale.setDefault(locale);
        bundle = ResourceBundle.getBundle("client.labafx.localization", locale);
        controller.setBundle(bundle);
        ((Label)authorizationScene.lookup("#authorizationLabel")).setText(bundle.getString("label.authorizationLabel"));
        ((Label)authorizationScene.lookup("#loginLabel")).setText(bundle.getString("label.loginLabel"));
        ((Label)authorizationScene.lookup("#passwordLabel")).setText(bundle.getString("label.passwordLabel"));
        ((Button)authorizationScene.lookup("#loginButton")).setText(bundle.getString("button.loginButton"));
        ((Button)authorizationScene.lookup("#registrationButton")).setText(bundle.getString("button.registrationButton"));
    }
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlAuthorizationLoader = new FXMLLoader(getClass().getResource("authorization-view.fxml"));
        authorizationScene = new Scene(fxmlAuthorizationLoader.load());
        controller = fxmlAuthorizationLoader.getController();
        controller.setBundle(bundle);
        ((Button) authorizationScene.lookup("#russianButton")).setOnAction(e -> changeLocale(new Locale("ru")));
        ((Button) authorizationScene.lookup("#russianButton")).setTooltip(new Tooltip("Русский"));
        ((Button) authorizationScene.lookup("#russianButton")).setGraphic(new ImageView(new Image("E:\\IdeaProjects\\labafx\\src\\main\\resources\\client\\labafx\\graphic\\flag_ru.png")));
        ((Button) authorizationScene.lookup("#danishButton")).setOnAction(e -> changeLocale(new Locale("da")));
        ((Button) authorizationScene.lookup("#danishButton")).setTooltip(new Tooltip("Dansk"));
        ((Button) authorizationScene.lookup("#danishButton")).setGraphic(new ImageView(new Image("E:\\IdeaProjects\\labafx\\src\\main\\resources\\client\\labafx\\graphic\\flag_da.png")));
        ((Button) authorizationScene.lookup("#islandButton")).setOnAction(e -> changeLocale(new Locale("is")));
        ((Button) authorizationScene.lookup("#islandButton")).setTooltip(new Tooltip("Íslenska english"));
        ((Button) authorizationScene.lookup("#islandButton")).setGraphic(new ImageView(new Image("E:\\IdeaProjects\\labafx\\src\\main\\resources\\client\\labafx\\graphic\\flag_is.png")));
        ((Button) authorizationScene.lookup("#ecuadorianButton")).setOnAction(e -> changeLocale(new Locale("es", "EC")));
        ((Button) authorizationScene.lookup("#ecuadorianButton")).setTooltip(new Tooltip("Español (Ecuador)"));
        ((Button) authorizationScene.lookup("#ecuadorianButton")).setGraphic(new ImageView(new Image("E:\\IdeaProjects\\labafx\\src\\main\\resources\\client\\labafx\\graphic\\flag_es.png")));

        stage.setScene(authorizationScene);
        stage.show();
        changeLocale(new Locale("ru"));
    }
    public AuthorizationController getController(){
        return controller;
    }
}
