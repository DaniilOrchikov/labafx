package client.labafx.command;

import client.labafx.ClientLogic;
import client.labafx.table.TicketTable;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.TabPane;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Show extends Command {

    public Show(ClientLogic clientLogic) {
        super("show", "Show", clientLogic);
    }

    @Override
    public void pushButton(ActionEvent event) {
        threadPool.execute(() -> {
            TicketTable table = new TicketTable(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), clientLogic);
            table.refresh(clientLogic.getTickets());
            Platform.runLater(() -> {
                ((TabPane) button.getScene().lookup("#tableTabPane")).getTabs().add(table.getMainNode());
                ((TabPane) button.getScene().lookup("#tableTabPane")).getSelectionModel().select(table.getMainNode());
            });
        });
    }

    @Override
    public void setButtonsActions() {
        button.addEventHandler(ActionEvent.ACTION, this::pushButton);
    }
}
