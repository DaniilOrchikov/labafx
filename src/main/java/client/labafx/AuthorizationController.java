package client.labafx;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class AuthorizationController {
    @FXML
    private Label passwordErrorLabel;
    @FXML
    private Label loginErrorLabel;
    @FXML
    private Button loginButton;
    @FXML
    private Button registrationButton;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    private Client client;

    public void setClientLogic(Client client) {
        this.client = client;
    }
    @FXML
    private void pressLoginButton() throws IOException {
        String req = client.getClientLogic().authorization(new String[]{loginField.getText(), passwordField.getText()});
        switch (req.split("/")[0]) {
            case "OK" -> client.openMainWindow();
            case "password" -> {
                passwordErrorLabel.setText("Неверный пароль");
                passwordField.clear();
            }
            case "login" -> {
                loginErrorLabel.setText("Неверное имя пользователя");
                passwordField.clear();
            }
        }
    }
    @FXML
    private void pressRegistrationButton() throws IOException {
        String req = client.getClientLogic().registration(new String[]{loginField.getText(), passwordField.getText()});
        switch (req) {
            case "OK" -> client.openMainWindow();
            case "already exists" -> {
                loginErrorLabel.setText("Пользователь с таким именем уже существует");
                passwordField.clear();
            }
        }
    }
    @FXML
    private void clearErrorLabels(){
        loginErrorLabel.setText("");
        passwordErrorLabel.setText("");
    }
}