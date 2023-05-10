package client.labafx.table;

import client.labafx.ClientLogic;
import client.labafx.LocalString;
import client.labafx.LocalTicketBuilder;
import client.labafx.MainWindow;
import client.labafx.command.Update;
import client.labafx.command.utility.RandomTextGenerator;
import javafx.application.Platform;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static client.labafx.command.utility.CommandNode.setIntegerValidator;

public class TicketTable {
    private final Tab mainNode;
    private boolean autoRenewing = true;
    private final TableView<LocalTicketBuilder> tableView;
    private final ObservableList<LocalTicketBuilder> tickets = FXCollections.observableArrayList();
    private ObservableList<LocalTicketBuilder> TICKETS = FXCollections.observableArrayList();
    private HBox filterHBox;
    private final ClientLogic clientLogic;
    private ResourceBundle bundle;
    ArrayList<TableColumn<LocalTicketBuilder, ?>> columns = new ArrayList<>();

    public TicketTable(String name, ClientLogic clientLogic, Update updateCommand, ResourceBundle bundle) {
        this.bundle = bundle;
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
        tableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tableView.getSelectionModel().getSelectedItem() != null) {
                updateCommand.fromTicketId(tableView.getSelectionModel().getSelectedItem().getTicketBuilder().getId());
            }
        });
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
            if (((RadioButton) filterHBox.lookup("#filterAutoupdateRadioButton")).isSelected() && autoRenewing) {
                refresh(clientLogic.getTickets());
                tableView.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && tableView.getSelectionModel().getSelectedItem() != null) {
                        updateCommand.fromTicketId(tableView.getSelectionModel().getSelectedItem().getTicketBuilder().getId());
                    }
                });
            } else {
                tableView.setOnMouseClicked(event -> {
                });
            }
            if (!autoRenewing) {
                tableView.refresh();
            }
        }, 0, 400, TimeUnit.MILLISECONDS);
    }

    private HBox getFilterView() throws IOException {
        filterHBox = new FXMLLoader(MainWindow.class.getResource("filter-view.fxml")).load();
        ChoiceBox<String> filterTypeChoiceBox = (ChoiceBox<String>) filterHBox.lookup("#filterTypeChoiceBox");
        for (int i = 1; i < TicketType.values().length; i++)
            filterTypeChoiceBox.getItems().add(TicketType.values()[i].toString());
        filterTypeChoiceBox.setValue("USUAL");
        ChoiceBox<LocalString> choiceBox = (ChoiceBox<LocalString>) filterHBox.lookup("#filterChoiceBox");
        choiceBox.getItems().addAll(new LocalString("filter_contains_name", "filter_contains_name"),
                new LocalString("filter_less_than_price", "filter_less_than_price"),
                new LocalString("filter_by_price", "filter_by_price"),
                new LocalString("filter_greater_than_type", "filter_greater_than_type"),
                new LocalString("min_by_venue", "min_by_venue"),
                new LocalString("user_name", "user_name"),
                new LocalString("name", "name"),
                new LocalString("street", "street"),
                new LocalString("zipCode", "zipCode"));
        choiceBox.setValue(new LocalString("filter_contains_name", "filter_contains_name"));
        choiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue.getName()) {
                case "filter_contains_name", "user_name", "name", "street", "zipCode" -> {
                    filterHBox.lookup("#filterTypeChoiceBox").setVisible(false);
                    filterHBox.lookup("#filterSpinner").setVisible(false);
                    filterHBox.lookup("#filterTextField").setVisible(true);
                }
                case "filter_greater_than_type" -> {
                    filterHBox.lookup("#filterTextField").setVisible(false);
                    filterHBox.lookup("#filterSpinner").setVisible(false);
                    filterHBox.lookup("#filterTypeChoiceBox").setVisible(true);
                }
                case "min_by_venue" -> {
                    filterHBox.lookup("#filterTextField").setVisible(false);
                    filterHBox.lookup("#filterSpinner").setVisible(false);
                    filterHBox.lookup("#filterTypeChoiceBox").setVisible(false);
                }
                default -> {
                    filterHBox.lookup("#filterTypeChoiceBox").setVisible(false);
                    filterHBox.lookup("#filterTextField").setVisible(false);
                    filterHBox.lookup("#filterSpinner").setVisible(true);
                }
            }
        });
        Spinner<Integer> spinner = (Spinner<Integer>) filterHBox.lookup("#filterSpinner");
        setIntegerValidator(spinner);
        ((ChoiceBox<LocalString>) filterHBox.lookup("#sortChoiceBox")).getItems().setAll(
                new LocalString("none", "none"),
                new LocalString("userName", "userName"),
                new LocalString("id", "id"),
                new LocalString("name", "name"),
                new LocalString("x", "x"),
                new LocalString("y", "y"),
                new LocalString("price", "price"),
                new LocalString("type", "type"),
                new LocalString("capacity", "capacity"),
                new LocalString("venueType", "venueType"),
                new LocalString("street", "street"),
                new LocalString("zipCode", "zipCode"),
                new LocalString("creationDate", "creationDate"));
        ((ChoiceBox<LocalString>) filterHBox.lookup("#sortChoiceBox")).setValue(new LocalString("none", "none"));
        ((Button) filterHBox.lookup("#filterOKButton")).setOnAction(event -> {
            sort();
            filter();
        });
        return filterHBox;
    }

    private void filter() {
        ChoiceBox<LocalString> choiceBox = (ChoiceBox<LocalString>) filterHBox.lookup("#filterChoiceBox");
        String filterType = choiceBox.getValue().getName();
        if (filterType == null) return;
        tickets.clear();
        switch (filterType) {
            case "filter_contains_name" -> {
                String name = ((TextField) filterHBox.lookup("#filterTextField")).getText();
                if (name == null || name.isBlank()) tickets.addAll(TICKETS);
                else
                    tickets.addAll(FXCollections.observableArrayList(TICKETS.stream().filter(t -> t.getName().contains(name)).toList()));
            }
            case "user_name" -> {
                String userName = ((TextField) filterHBox.lookup("#filterTextField")).getText();
                if (userName == null || userName.isBlank()) tickets.addAll(TICKETS);
                else
                    tickets.addAll(FXCollections.observableArrayList(TICKETS.stream().filter(t -> t.getUserName().equals(userName)).toList()));
            }
            case "name" -> {
                String name = ((TextField) filterHBox.lookup("#filterTextField")).getText();
                if (name == null || name.isBlank()) tickets.addAll(TICKETS);
                else
                    tickets.addAll(FXCollections.observableArrayList(TICKETS.stream().filter(t -> t.getName().equals(name)).toList()));
            }
            case "street" -> {
                String street = ((TextField) filterHBox.lookup("#filterTextField")).getText();
                if (street == null || street.isBlank()) tickets.addAll(TICKETS);
                else
                    tickets.addAll(FXCollections.observableArrayList(TICKETS.stream().filter(t -> t.getAddressStreet().equals(street)).toList()));
            }
            case "zipCode" -> {
                String zipCode = ((TextField) filterHBox.lookup("#filterTextField")).getText();
                if (zipCode == null || zipCode.isBlank()) tickets.addAll(TICKETS);
                else
                    tickets.addAll(FXCollections.observableArrayList(TICKETS.stream().filter(t -> t.getAddressZipCode().equals(zipCode)).toList()));
            }
            case "filter_greater_than_type" -> {
                String type = ((ChoiceBox<String>) filterHBox.lookup("#filterTypeChoiceBox")).getValue();
                tickets.addAll(FXCollections.observableArrayList(TICKETS.stream().filter(t -> TicketType.valueOf(type).compareTo(t.getType()) > 0).toList()));
            }
            case "min_by_venue" ->
                    TICKETS.stream().min(Comparator.comparing(tb -> tb.getTicketBuilder().getTicket().getVenue())).ifPresent(tickets::add);
            default -> {
                Integer price = ((Spinner<Integer>) filterHBox.lookup("#filterSpinner")).getValue();
                if (filterType.equals("filter_less_than_price")) {
                    tickets.addAll(FXCollections.observableArrayList(TICKETS.stream().filter(t -> t.getTicketBuilder().getPrice() < price).toList()));
                } else {
                    tickets.addAll(FXCollections.observableArrayList(TICKETS.stream().filter(t -> Objects.equals(t.getTicketBuilder().getPrice(), price)).toList()));
                }
            }
        }
    }

    private void sort() {
        String sortType = ((ChoiceBox<LocalString>) filterHBox.lookup("#sortChoiceBox")).getValue().getName();
        if (sortType.equals("none")) return;
        tickets.clear();
        switch (sortType) {
            case "userName" ->
                    TICKETS = FXCollections.observableArrayList(TICKETS.stream().sorted(Comparator.comparing(LocalTicketBuilder::getUserName)).toList());
            case "id" ->
                    TICKETS = FXCollections.observableArrayList(TICKETS.stream().sorted(Comparator.comparingLong(t -> t.getTicketBuilder().getId())).toList());
            case "name" ->
                    TICKETS = FXCollections.observableArrayList(TICKETS.stream().sorted(Comparator.comparing(LocalTicketBuilder::getName)).toList());
            case "x" ->
                    TICKETS = FXCollections.observableArrayList(TICKETS.stream().sorted(Comparator.comparingInt(t -> t.getTicketBuilder().getX())).toList());
            case "y" ->
                    TICKETS = FXCollections.observableArrayList(TICKETS.stream().sorted(Comparator.comparingInt(t -> t.getTicketBuilder().getY())).toList());
            case "price" ->
                    TICKETS = FXCollections.observableArrayList(TICKETS.stream().sorted(Comparator.comparingInt(t -> t.getTicketBuilder().getPrice())).toList());
            case "type" ->
                    TICKETS = FXCollections.observableArrayList(TICKETS.stream().sorted(Comparator.comparing(LocalTicketBuilder::getType)).toList());
            case "capacity" ->
                    TICKETS = FXCollections.observableArrayList(TICKETS.stream().sorted(Comparator.comparingLong(t -> t.getTicketBuilder().getVenueCapacity())).toList());
            case "venueType" ->
                    TICKETS = FXCollections.observableArrayList(TICKETS.stream().sorted(Comparator.comparing(LocalTicketBuilder::getVenueType)).toList());
            case "street" ->
                    TICKETS = FXCollections.observableArrayList(TICKETS.stream().sorted(Comparator.comparing(LocalTicketBuilder::getAddressStreet)).toList());
            case "zipCode" ->
                    TICKETS = FXCollections.observableArrayList(TICKETS.stream().sorted(Comparator.comparing(LocalTicketBuilder::getAddressZipCode)).toList());
            case "creationDate" ->
                    TICKETS = FXCollections.observableArrayList(TICKETS.stream().sorted(Comparator.comparing(t -> t.getTicketBuilder().getCreationDate())).toList());
        }
        if (((RadioButton) filterHBox.lookup("#sortRadioButton")).isSelected())
            tickets.addAll(FXCollections.observableArrayList(IntStream.rangeClosed(1, TICKETS.size()).mapToObj(i -> TICKETS.get(TICKETS.size() - i)).toList()));
        else tickets.addAll(TICKETS);
    }

    public Tab getMainNode() {
        return mainNode;
    }

    public void refresh(ArrayList<TicketBuilder> tArr) {
        ArrayList<LocalTicketBuilder> ltArr = new ArrayList<>();
        for (TicketBuilder tb : tArr.stream().toList()) {
            ltArr.add(new LocalTicketBuilder(tb, bundle));
        }
        tickets.clear();
        tickets.addAll(FXCollections.observableArrayList(ltArr));
        TICKETS.clear();
        TICKETS.addAll(tickets);
        sort();
        filter();
    }

    private void createColumns() {
        TableColumn<LocalTicketBuilder, String> userNameColumn = new TableColumn<>("userName");
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        TableColumn<LocalTicketBuilder, String> idColumn = new TableColumn<>("id");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setSortable(false);

        TableColumn<LocalTicketBuilder, String> nameColumn = new TableColumn<>("name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setSortable(false);

        TableColumn<LocalTicketBuilder, String> xColumn = new TableColumn<>("x");
        xColumn.setCellValueFactory(new PropertyValueFactory<>("x"));
        xColumn.setSortable(false);

        TableColumn<LocalTicketBuilder, String> yColumn = new TableColumn<>("y");
        yColumn.setCellValueFactory(new PropertyValueFactory<>("y"));
        yColumn.setSortable(false);

        TableColumn<LocalTicketBuilder, String> priceColumn = new TableColumn<>("price");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceColumn.setSortable(false);

        TableColumn<LocalTicketBuilder, TicketType> typeColumn = new TableColumn<>("type");
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeColumn.setSortable(false);

        TableColumn<LocalTicketBuilder, String> capacityColumn = new TableColumn<>("capacity");
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("venueCapacity"));
        capacityColumn.setSortable(false);

        TableColumn<LocalTicketBuilder, VenueType> venueTypeColumn = new TableColumn<>("venueType");
        venueTypeColumn.setCellValueFactory(new PropertyValueFactory<>("venueType"));
        venueTypeColumn.setSortable(false);

        TableColumn<LocalTicketBuilder, String> streetColumn = new TableColumn<>("street");
        streetColumn.setCellValueFactory(new PropertyValueFactory<>("addressStreet"));
        streetColumn.setSortable(false);

        TableColumn<LocalTicketBuilder, String> zipCodeColumn = new TableColumn<>("zipCode");
        zipCodeColumn.setCellValueFactory(new PropertyValueFactory<>("addressZipCode"));
        zipCodeColumn.setSortable(false);

        TableColumn<LocalTicketBuilder, String> creationDateColumn = new TableColumn<>("creationDate");
        creationDateColumn.setCellValueFactory(new PropertyValueFactory<>("creationDate"));
        creationDateColumn.setSortable(false);
        tableView.getColumns().addAll(userNameColumn, idColumn, nameColumn, xColumn, yColumn, priceColumn, typeColumn, capacityColumn, venueTypeColumn, streetColumn, zipCodeColumn, creationDateColumn);
        columns.add(userNameColumn);
        columns.add(idColumn);
        columns.add(nameColumn);
        columns.add(xColumn);
        columns.add(yColumn);
        columns.add(priceColumn);
        columns.add(typeColumn);
        columns.add(capacityColumn);
        columns.add(venueTypeColumn);
        columns.add(streetColumn);
        columns.add(zipCodeColumn);
        columns.add(creationDateColumn);

    }

    public void changeLocale(ResourceBundle bundle) {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(LocalDateTime.parse(mainNode.getText(), DateTimeFormatter.ofPattern(this.bundle.getString("date.format"))), ZoneId.of(this.bundle.getString("date.timeZone")));
        ZonedDateTime outputZonedDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of(bundle.getString("date.timeZone")));
        String formattedDateTime = outputZonedDateTime.format(DateTimeFormatter.ofPattern(bundle.getString("date.format")));
        mainNode.setText(formattedDateTime);
        this.bundle = bundle;
        ((RadioButton) filterHBox.lookup("#filterAutoupdateRadioButton")).setText(bundle.getString("filterAutoupdateRadioButton"));
        ((RadioButton) filterHBox.lookup("#sortRadioButton")).setText(bundle.getString("sortRadioButton"));
        ((Label) filterHBox.lookup("#filterLabel")).setText(bundle.getString("label.filterLabel"));
        ((Label) filterHBox.lookup("#sortLabel")).setText(bundle.getString("label.sortLabel"));
        for (LocalTicketBuilder localTicketBuilder : tickets)
            localTicketBuilder.changeLocale(bundle);
        columns.get(0).setText(bundle.getString("label.userNameLabel"));
        columns.get(1).setText(bundle.getString("label.idLabel"));
        columns.get(2).setText(bundle.getString("label.nameLabel"));
        columns.get(5).setText(bundle.getString("label.priceLabel"));
        columns.get(6).setText(bundle.getString("label.typeLabel"));
        columns.get(7).setText(bundle.getString("label.capacityLabel"));
        columns.get(8).setText(bundle.getString("label.venueTypeLabel"));
        columns.get(9).setText(bundle.getString("label.streetLabel"));
        columns.get(10).setText(bundle.getString("label.zipCodeLabel"));
        columns.get(11).setText(bundle.getString("label.creationDateLabel"));
        ChoiceBox<LocalString> choiceBox = (ChoiceBox<LocalString>) filterHBox.lookup("#filterChoiceBox");
        choiceBox.getItems().clear();
        choiceBox.getItems().addAll(new LocalString("filter_contains_name", bundle.getString("filter_contains_name")),
                new LocalString("filter_less_than_price", bundle.getString("filter_contains_name")),
                new LocalString("filter_by_price", bundle.getString("filter_by_price")),
                new LocalString("filter_greater_than_type", bundle.getString("filter_greater_than_type")),
                new LocalString("min_by_venue", bundle.getString("min_by_venue")),
                new LocalString("user_name", bundle.getString("label.userNameLabel")),
                new LocalString("name", bundle.getString("label.nameLabel")),
                new LocalString("street", bundle.getString("label.streetLabel")),
                new LocalString("zipCode", bundle.getString("label.zipCodeLabel")));
        choiceBox.setValue(new LocalString("filter_contains_name", bundle.getString("filter_contains_name")));
        choiceBox = (ChoiceBox<LocalString>) filterHBox.lookup("#sortChoiceBox");
        choiceBox.getItems().clear();
        choiceBox.getItems().setAll(
                new LocalString("none", bundle.getString("none")),
                new LocalString("userName", bundle.getString("label.userNameLabel")),
                new LocalString("id", bundle.getString("label.idLabel")),
                new LocalString("name", bundle.getString("label.nameLabel")),
                new LocalString("x", "x"),
                new LocalString("y", "y"),
                new LocalString("price", bundle.getString("label.priceLabel")),
                new LocalString("type", bundle.getString("label.typeLabel")),
                new LocalString("capacity", bundle.getString("label.capacityLabel")),
                new LocalString("venueType", bundle.getString("label.venueTypeLabel")),
                new LocalString("street", bundle.getString("label.streetLabel")),
                new LocalString("zipCode", bundle.getString("label.zipCodeLabel")),
                new LocalString("creationDate", bundle.getString("label.creationDateLabel")));
        choiceBox.setValue(new LocalString("none", bundle.getString("none")));
        tableView.refresh();
    }

    public void setAutoRenewing(boolean autoRenewing) {
        this.autoRenewing = autoRenewing;
        if (!autoRenewing) {
            RadioButton radioButton = (RadioButton) filterHBox.lookup("#filterAutoupdateRadioButton");
            if (radioButton.isSelected()) radioButton.setSelected(false);
            radioButton.setVisible(false);
            tableView.setOnMouseClicked(event -> {
            });
        }
    }

    public void setAutoupdate(boolean f) {
        ((RadioButton) filterHBox.lookup("#filterAutoupdateRadioButton")).setSelected(f);
    }
}
