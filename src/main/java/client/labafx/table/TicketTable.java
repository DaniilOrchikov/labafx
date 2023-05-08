package client.labafx.table;

import client.labafx.ClientLogic;
import client.labafx.ErrorWindow;
import client.labafx.ExplanationPopup;
import client.labafx.MainWindow;
import client.labafx.command.utility.RandomTextGenerator;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import ticket.Ticket;
import ticket.TicketBuilder;
import ticket.TicketType;
import ticket.VenueType;
import utility.Command;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static client.labafx.command.utility.CommandNode.*;

public class TicketTable {
    private final Tab mainNode;
    private final TableView<TicketBuilder> tableView;
    private final ObservableList<TicketBuilder> tickets = FXCollections.observableArrayList();
    private final ObservableList<TicketBuilder> TICKETS = FXCollections.observableArrayList();
    private final ExecutorService pool = Executors.newFixedThreadPool(3);
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
            if (((RadioButton) filterHBox.lookup("#filterAutoupdateRadioButton")).isSelected()) {
                refresh(clientLogic.getTickets());
                tableView.setEditable(false);
            } else tableView.setEditable(true);
        }, 0, 400, TimeUnit.MILLISECONDS);
    }

    private HBox getFilterView() throws IOException {
        filterHBox = new FXMLLoader(MainWindow.class.getResource("filter-view.fxml")).load();
        ChoiceBox<String> filterTypeChoiceBox = (ChoiceBox<String>) filterHBox.lookup("#filterTypeChoiceBox");
        for (int i = 1; i < TicketType.values().length; i++)
            filterTypeChoiceBox.getItems().add(TicketType.values()[i].toString());
        filterTypeChoiceBox.setValue("USUAL");
        ChoiceBox<String> choiceBox = (ChoiceBox<String>) filterHBox.lookup("#filterChoiceBox");
        choiceBox.getItems().addAll("filter_contains_name", "filter_less_than_price", "filter_by_price", "filter_greater_than_type", "filter_by_user_name", "min_by_venue");
        choiceBox.setValue("filter_contains_name");
        choiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue) {
                case "filter_contains_name", "filter_by_user_name" -> {
                    filterHBox.lookup("#filterTypeChoiceBox").setVisible(false);
                    filterHBox.lookup("#filterPriceSpinner").setVisible(false);
                    filterHBox.lookup("#filterNameTextField").setVisible(true);
                }
                case "filter_greater_than_type" -> {
                    filterHBox.lookup("#filterNameTextField").setVisible(false);
                    filterHBox.lookup("#filterPriceSpinner").setVisible(false);
                    filterHBox.lookup("#filterTypeChoiceBox").setVisible(true);
                }
                case "min_by_venue" -> {
                    filterHBox.lookup("#filterNameTextField").setVisible(false);
                    filterHBox.lookup("#filterPriceSpinner").setVisible(false);
                    filterHBox.lookup("#filterTypeChoiceBox").setVisible(false);
                }
                default -> {
                    filterHBox.lookup("#filterTypeChoiceBox").setVisible(false);
                    filterHBox.lookup("#filterNameTextField").setVisible(false);
                    filterHBox.lookup("#filterPriceSpinner").setVisible(true);
                }
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
        switch (filterType) {
            case "filter_contains_name" -> {
                String name = ((TextField) filterHBox.lookup("#filterNameTextField")).getText();
                tickets.addAll(FXCollections.observableArrayList(TICKETS.stream().filter(t -> t.getName().contains(name)).toList()));
            }
            case "filter_by_user_name" -> {
                String userName = ((TextField) filterHBox.lookup("#filterNameTextField")).getText();
                tickets.addAll(FXCollections.observableArrayList(TICKETS.stream().filter(t -> t.getUserName().equals(userName)).toList()));
            }
            case "filter_greater_than_type" -> {
                String type = ((ChoiceBox<String>) filterHBox.lookup("#filterTypeChoiceBox")).getValue();
                tickets.addAll(FXCollections.observableArrayList(TICKETS.stream().filter(t -> TicketType.valueOf(type).compareTo(t.getType()) > 0).toList()));
            }
            case "min_by_venue" -> {
                TICKETS.stream().min(Comparator.comparing(tb -> tb.getTicket().getVenue())).ifPresent(tickets::add);
            }
            default -> {
                Integer price = ((Spinner<Integer>) filterHBox.lookup("#filterPriceSpinner")).getValue();
                if (filterType.equals("filter_less_than_price")) {
                    tickets.addAll(FXCollections.observableArrayList(TICKETS.stream().filter(t -> t.getPrice() < price).toList()));
                } else {
                    tickets.addAll(FXCollections.observableArrayList(TICKETS.stream().filter(t -> Objects.equals(t.getPrice(), price)).toList()));
                }
            }
        }
        tableView.sort();
    }

    public Tab getMainNode() {
        return mainNode;
    }

    public void refresh(ArrayList<TicketBuilder> tArr) {
        Platform.runLater(() -> {
            tickets.clear();
            tickets.addAll(FXCollections.observableArrayList(tArr));
            TICKETS.clear();
            TICKETS.addAll(tickets);
            filter();
        });
    }

    private void createColumns() {
        TableColumn<TicketBuilder, String> userNameColumn = new TableColumn<>("userName");
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        TableColumn<TicketBuilder, Long> idColumn = new TableColumn<>("id");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<TicketBuilder, String> nameColumn = new TableColumn<>("name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setEditable(true);
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setOnEditCommit(event -> {
            TicketBuilder ticketBuilder = event.getTableView().getItems().get(event.getTablePosition().getRow());
            pool.execute(() -> {
                if (accessAllowed(ticketBuilder)) {
                    ticketBuilder.setName(event.getNewValue());
                    update(ticketBuilder);
                    tableView.sort();
                }
            });
        });

        TableColumn<TicketBuilder, Integer> xColumn = new TableColumn<>("x");
        xColumn.setCellValueFactory(new PropertyValueFactory<>("x"));
        xColumn.setEditable(true);
        setIntegerValueFactory(xColumn);
        xColumn.setOnEditCommit(event -> {
            TicketBuilder ticketBuilder = event.getTableView().getItems().get(event.getTablePosition().getRow());
            pool.execute(() -> {
                if (accessAllowed(ticketBuilder)) {
                    ticketBuilder.setX(event.getNewValue().toString());
                    update(ticketBuilder);
                    tableView.sort();
                }
            });
        });

        TableColumn<TicketBuilder, Integer> yColumn = new TableColumn<>("y");
        yColumn.setCellValueFactory(new PropertyValueFactory<>("y"));
        yColumn.setEditable(true);
        setIntegerValueFactory(yColumn);
        yColumn.setOnEditCommit(event -> {
            TicketBuilder ticketBuilder = event.getTableView().getItems().get(event.getTablePosition().getRow());
            pool.execute(() -> {
                if (accessAllowed(ticketBuilder)) {
                    ticketBuilder.setY(event.getNewValue().toString());
                    update(ticketBuilder);
                    tableView.sort();
                }
            });
        });

        TableColumn<TicketBuilder, Integer> priceColumn = new TableColumn<>("price");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceColumn.setEditable(true);
        setIntegerValueFactory(priceColumn);
        priceColumn.setOnEditCommit(event -> {
            TicketBuilder ticketBuilder = event.getTableView().getItems().get(event.getTablePosition().getRow());
            pool.execute(() -> {
                if (accessAllowed(ticketBuilder)) {
                    ticketBuilder.setPrice(event.getNewValue().toString());
                    update(ticketBuilder);
                    tableView.sort();
                }
            });
        });

        TableColumn<TicketBuilder, TicketType> typeColumn = new TableColumn<>("type");
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeColumn.setEditable(true);
        ObservableList<TicketType> ticketTypes = FXCollections.observableArrayList(TicketType.VIP, TicketType.USUAL, TicketType.BUDGETARY, TicketType.CHEAP);
        typeColumn.setCellFactory(ComboBoxTableCell.forTableColumn(ticketTypes));
        typeColumn.setOnEditCommit(event -> {
            TicketBuilder ticketBuilder = event.getTableView().getItems().get(event.getTablePosition().getRow());
            pool.execute(() -> {
                if (accessAllowed(ticketBuilder)) {
                    ticketBuilder.setType(event.getNewValue().toString());
                    update(ticketBuilder);
                    tableView.sort();
                }
            });
        });

        TableColumn<TicketBuilder, Long> capacityColumn = new TableColumn<>("capacity");
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("venueCapacity"));
        capacityColumn.setEditable(true);
        setLongValueFactory(capacityColumn);
        capacityColumn.setOnEditCommit(event -> {
            TicketBuilder ticketBuilder = event.getTableView().getItems().get(event.getTablePosition().getRow());
            pool.execute(() -> {
                if (accessAllowed(ticketBuilder)) {
                    ticketBuilder.setVenueCapacity(event.getNewValue().toString());
                    update(ticketBuilder);
                    tableView.sort();
                }
            });
        });

        TableColumn<TicketBuilder, VenueType> venueTypeColumn = new TableColumn<>("venueType");
        venueTypeColumn.setCellValueFactory(new PropertyValueFactory<>("venueType"));
        venueTypeColumn.setEditable(true);
        ObservableList<VenueType> venueTypes = FXCollections.observableArrayList(VenueType.BAR, VenueType.PUB, VenueType.OPEN_AREA);
        venueTypeColumn.setCellFactory(ComboBoxTableCell.forTableColumn(venueTypes));
        venueTypeColumn.setOnEditCommit(event -> {
            TicketBuilder ticketBuilder = event.getTableView().getItems().get(event.getTablePosition().getRow());
            pool.execute(() -> {
                if (accessAllowed(ticketBuilder)) {
                    ticketBuilder.setVenueType(event.getNewValue().toString());
                    update(ticketBuilder);
                    tableView.sort();
                }
            });
        });

        TableColumn<TicketBuilder, String> streetColumn = new TableColumn<>("street");
        streetColumn.setCellValueFactory(new PropertyValueFactory<>("addressStreet"));
        streetColumn.setEditable(true);
        streetColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        streetColumn.setOnEditCommit(event -> {
            TicketBuilder ticketBuilder = event.getTableView().getItems().get(event.getTablePosition().getRow());
            pool.execute(() -> {
                if (accessAllowed(ticketBuilder)) {
                    ticketBuilder.setAddressStreet(event.getNewValue());
                    update(ticketBuilder);
                    tableView.sort();
                }
            });
        });

        TableColumn<TicketBuilder, String> zipCodeColumn = new TableColumn<>("zipCode");
        zipCodeColumn.setCellValueFactory(new PropertyValueFactory<>("addressZipCode"));
        zipCodeColumn.setEditable(true);
        zipCodeColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        zipCodeColumn.setOnEditCommit(event -> {
            TicketBuilder ticketBuilder = event.getTableView().getItems().get(event.getTablePosition().getRow());
            pool.execute(() -> {
                if (accessAllowed(ticketBuilder)) {
                    ticketBuilder.setAddressZipCode(event.getNewValue());
                    update(ticketBuilder);
                    tableView.sort();
                }
            });
        });

        TableColumn<TicketBuilder, LocalDateTime> creationDateColumn = new TableColumn<>("creationDate");
        creationDateColumn.setCellValueFactory(new PropertyValueFactory<>("creationDate"));
        tableView.getColumns().addAll(userNameColumn, idColumn, nameColumn, xColumn, yColumn, priceColumn, typeColumn, capacityColumn, venueTypeColumn, streetColumn, zipCodeColumn, creationDateColumn);
    }

    private void update(TicketBuilder ticketBuilder) {
        String req = clientLogic.communicatingWithServer(new Command(new String[]{"update", ticketBuilder.getId().toString()}, ticketBuilder, clientLogic.userName, clientLogic.userPassword)).text();
        Platform.runLater(() -> {
            if (!req.equals("OK")) {
                try {
                    ErrorWindow.show(req, "Ошибка при выполнении команды update");
                } catch (IOException ignored) {
                }
            }
        });
    }

    private void setIntegerValueFactory(TableColumn<TicketBuilder, Integer> tableColumn) {
        tableColumn.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<>() {
            @Override
            public String toString(Integer integer) {
                try {
                    return integer.toString();
                } catch (NullPointerException e) {
                    return "null";
                }
            }

            @Override
            public Integer fromString(String string) {
                if (isInteger(string) && Integer.parseInt(string) > 0)
                    return Integer.parseInt(string);
                else if (isLong(string))
                    return Integer.MAX_VALUE;
                else
                    return 1;
            }
        }));
    }

    private void setLongValueFactory(TableColumn<TicketBuilder, Long> tableColumn) {
        tableColumn.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<>() {
            @Override
            public String toString(Long l) {
                return l.toString();
            }

            @Override
            public Long fromString(String string) {
                if (isLong(string) && Long.parseLong(string) > 0)
                    return Long.parseLong(string);
                else
                    return 1L;
            }
        }));
    }

    private boolean accessAllowed(TicketBuilder ticketBuilder) {
        if (!ticketBuilder.getUserName().equals(clientLogic.userName)) {
            tableView.refresh();
            Platform.runLater(() -> {
                try {
                    ExplanationPopup.show("Не удалось получить доступ к объекту", "update", new Stage());
                } catch (IOException ignored) {
                }
            });

        }
        return ticketBuilder.getUserName().equals(clientLogic.userName);
    }
}
