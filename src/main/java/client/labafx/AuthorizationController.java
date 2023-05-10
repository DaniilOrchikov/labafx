package client.labafx;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.ResourceBundle;

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
    private ResourceBundle bundle;

    public void setClientLogic(Client client) {
        this.client = client;
    }
    @FXML
    private void pressLoginButton() throws IOException {
        String req = client.getClientLogic().authorization(new String[]{loginField.getText(), passwordField.getText()});
        switch (req.split("/")[0]) {
            case "OK" -> client.openMainWindow();
            case "password" -> {
                passwordErrorLabel.setText(bundle.getString("errorPassword"));
                passwordField.clear();
            }
            case "login" -> {
                loginErrorLabel.setText(bundle.getString("errorLogin"));
                passwordField.clear();
            }
        }
    }
    @FXML
    private void pressRegistrationButton() throws IOException {
        if (passwordField.getText().equals("") || passwordField.getText().isBlank()) {
            passwordErrorLabel.setText(bundle.getString("errorPassword"));
            return;
        }
        String req = client.getClientLogic().registration(new String[]{loginField.getText(), passwordField.getText()});
        switch (req.split("/")[0]) {
            case "OK" -> client.openMainWindow();
            case "already exists" -> {
                loginErrorLabel.setText(bundle.getString("alreadyExists"));
                passwordField.clear();
            }
        }
    }
    @FXML
    private void clearErrorLabels(){
        loginErrorLabel.setText("");
        passwordErrorLabel.setText("");
    }

    public void setBundle(ResourceBundle bundle) {
        this.bundle = bundle;
    }
}