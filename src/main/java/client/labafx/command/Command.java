package client.labafx.command;

import client.labafx.ClientLogic;
import client.labafx.ErrorWindow;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Command {
    final ClientLogic clientLogic;
    private final String commandName;
    private final String name;
    Button button;
    ExecutorService threadPool = Executors.newFixedThreadPool(2);

    public Command(String commandName, String name, ClientLogic clientLogic) {
        this.name = name;
        this.clientLogic = clientLogic;
        this.commandName = commandName;
    }

    public String getCommandName() {
        return commandName;
    }

    public String getName() {
        return name;
    }

    public void setButton(Button button) {
        this.button = button;
    }

    public void setButtonsActions() {
        button.addEventHandler(ActionEvent.ACTION, this::pushButton);
    }

    public void pushButton(ActionEvent event) {
        threadPool.execute(() -> {
            String req = clientLogic.communicatingWithServer(new utility.Command(new String[]{getCommandName()}, clientLogic.userName, clientLogic.userPassword)).text();
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
    public void changeLocale(ResourceBundle bundle){
        button.setText(bundle.getString("button." + commandName));
    };
}
