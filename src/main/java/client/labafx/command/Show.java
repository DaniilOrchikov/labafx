package client.labafx.command;

import client.labafx.ClientLogic;
import client.labafx.table.TicketTable;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Show extends Command {

    private ArrayList<TicketTable> ticketTables = new ArrayList<>();
    private ResourceBundle bundle;
    private Update update;

    public Show(ClientLogic clientLogic, Update update) {
        super("show", "Show", clientLogic);
        this.update = update;
    }

    @Override
    public void pushButton(ActionEvent event) {
        threadPool.execute(() -> {
            TicketTable table = new TicketTable(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), clientLogic, update);
            ticketTables.add(table);
            table.changeLocale(bundle);
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

    @Override
    public void changeLocale(ResourceBundle bundle) {
        this.bundle = bundle;
        super.changeLocale(bundle);
        for (TicketTable ticketTable : ticketTables) {
            ticketTable.changeLocale(bundle);
        }
    }
}
