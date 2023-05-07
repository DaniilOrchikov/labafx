package client.labafx.table;

import client.labafx.ClientLogic;
import client.labafx.MainWindow;
import client.labafx.command.utility.RandomTextGenerator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ticket.TicketBuilder;
import ticket.TicketType;
import ticket.VenueType;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static client.labafx.command.utility.CommandNode.setIntegerValidator;

public class TicketTable {
    private final Tab mainNode;
    private final TableView<TicketBuilder> tableView;
    private final ObservableList<TicketBuilder> tickets = FXCollections.observableArrayList();
    private final ObservableList<TicketBuilder> TICKETS = FXCollections.observableArrayList();
    private final ExecutorService pool = Executors.newSingleThreadExecutor();
    private HBox filterHBox;
    private final ClientLogic clientLogic;

    public TicketTable(String name, ClientLogic clientLogic) {
        this.clientLogic = clientLogic;
        mainNode = new Tab(name);
        String strId = RandomTextGenerator.generate(7);
        mainNode.setId(strId);
        tableView = new TableView<>(tickets);
        createColumns();
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.setId("anchorPane");
        VBox vBox = new VBox();
        try {
            vBox.getChildren().add(getFilterView());
        } catch (IOException ignored) {
        }
        vBox.getChildren().add(tableView);
        anchorPane.getChildren().add(vBox);
        AnchorPane.setTopAnchor(vBox, 0.0);
        AnchorPane.setBottomAnchor(vBox, 0.0);
        AnchorPane.setLeftAnchor(vBox, 0.0);
        AnchorPane.setRightAnchor(vBox, 0.0);
        mainNode.setContent(anchorPane);
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
            if (((RadioButton) filterHBox.lookup("#filterAutoupdateRadioButton")).isSelected())
                refresh(clientLogic.getTickets());
        }, 0, 400, TimeUnit.MILLISECONDS);
    }

    private HBox getFilterView() throws IOException {
        filterHBox = new FXMLLoader(MainWindow.class.getResource("filter-view.fxml")).load();
        ChoiceBox<String> filterTypeChoiceBox = (ChoiceBox<String>) filterHBox.lookup("#filterTypeChoiceBox");
        for (int i = 1; i < TicketType.values().length; i++)
            filterTypeChoiceBox.getItems().add(TicketType.values()[i].toString());
        filterTypeChoiceBox.setValue("USUAL");
        ChoiceBox<String> choiceBox = (ChoiceBox<String>) filterHBox.lookup("#filterChoiceBox");
        choiceBox.getItems().addAll("filter_contains_name", "filter_less_than_price", "filter_by_price", "filter_greater_than_type");
        choiceBox.setValue("filter_contains_name");
        choiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals("filter_contains_name")) {
                filterHBox.lookup("#filterTypeChoiceBox").setVisible(false);
                filterHBox.lookup("#filterPriceSpinner").setVisible(false);
                filterHBox.lookup("#filterNameTextField").setVisible(true);
            } else if (newValue.equals("filter_greater_than_type")) {
                filterHBox.lookup("#filterNameTextField").setVisible(false);
                filterHBox.lookup("#filterPriceSpinner").setVisible(false);
                filterHBox.lookup("#filterTypeChoiceBox").setVisible(true);
            } else {
                filterHBox.lookup("#filterTypeChoiceBox").setVisible(false);
                filterHBox.lookup("#filterNameTextField").setVisible(false);
                filterHBox.lookup("#filterPriceSpinner").setVisible(true);
            }
        });
        Spinner<Integer> spinner = (Spinner<Integer>) filterHBox.lookup("#filterPriceSpinner");
        setIntegerValidator(spinner);
        ((Button) filterHBox.lookup("#filterOKButton")).setOnAction(event -> filter());
        return filterHBox;
    }

    private void filter() {
        ChoiceBox<String> choiceBox = (ChoiceBox<String>) filterHBox.lookup("#filterChoiceBox");
        String filterType = choiceBox.getValue();
        if (filterType == null) return;
        tickets.clear();
        if (filterType.equals("filter_contains_name")) {
            String name = ((TextField) filterHBox.lookup("#filterNameTextField")).getText();
            tickets.addAll(FXCollections.observableArrayList(TICKETS.stream().filter(t -> t.getName().contains(name)).toList()));
        } else if (filterType.equals("filter_greater_than_type")) {
            String type = ((ChoiceBox<String>) filterHBox.lookup("#filterTypeChoiceBox")).getValue();
            tickets.addAll(FXCollections.observableArrayList(TICKETS.stream().filter(t -> TicketType.valueOf(type).compareTo(t.getType()) > 0).toList()));
        } else {
            Integer price = ((Spinner<Integer>) filterHBox.lookup("#filterPriceSpinner")).getValue();
            if (filterType.equals("filter_less_than_price")) {
                tickets.addAll(FXCollections.observableArrayList(TICKETS.stream().filter(t -> t.getPrice() < price).toList()));
            } else {
                tickets.addAll(FXCollections.observableArrayList(TICKETS.stream().filter(t -> Objects.equals(t.getPrice(), price)).toList()));
            }
        }
    }

    public Tab getMainNode() {
        return mainNode;
    }

    public void refresh(ArrayList<TicketBuilder> tArr) {
        pool.execute(() -> {
            tickets.clear();
            tickets.addAll(FXCollections.observableArrayList(tArr));
            TICKETS.clear();
            TICKETS.addAll(FXCollections.observableArrayList(tArr));
            filter();
        });
    }

    private void createColumns() {
        TableColumn<TicketBuilder, Long> idColumn = new TableColumn<>("id");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<TicketBuilder, String> nameColumn = new TableColumn<>("name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<TicketBuilder, Integer> xColumn = new TableColumn<>("x");
        xColumn.setCellValueFactory(new PropertyValueFactory<>("x"));
        TableColumn<TicketBuilder, Integer> yColumn = new TableColumn<>("y");
        yColumn.setCellValueFactory(new PropertyValueFactory<>("y"));
        TableColumn<TicketBuilder, Integer> priceColumn = new TableColumn<>("price");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        TableColumn<TicketBuilder, TicketType> typeColumn = new TableColumn<>("type");
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        TableColumn<TicketBuilder, Long> capacityColumn = new TableColumn<>("capacity");
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("venueCapacity"));
        TableColumn<TicketBuilder, VenueType> venueTypeColumn = new TableColumn<>("venueType");
        venueTypeColumn.setCellValueFactory(new PropertyValueFactory<>("venueType"));
        TableColumn<TicketBuilder, String> streetColumn = new TableColumn<>("street");
        streetColumn.setCellValueFactory(new PropertyValueFactory<>("addressStreet"));
        TableColumn<TicketBuilder, String> zipCodeColumn = new TableColumn<>("zipCode");
        zipCodeColumn.setCellValueFactory(new PropertyValueFactory<>("addressZipCode"));
        TableColumn<TicketBuilder, LocalDateTime> creationDateColumn = new TableColumn<>("creationDate");
        creationDateColumn.setCellValueFactory(new PropertyValueFactory<>("creationDate"));
        tableView.getColumns().addAll(idColumn, nameColumn, xColumn, yColumn, priceColumn, typeColumn, capacityColumn, venueTypeColumn, streetColumn, zipCodeColumn, creationDateColumn);
    }
}
