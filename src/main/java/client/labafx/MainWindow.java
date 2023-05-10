package client.labafx;

import client.labafx.command.*;
import client.labafx.draw.TicketDrawer;
import client.labafx.table.TicketTable;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainWindow extends Application {
    private MainController controller;
    private Command[] commands;
    private GUICommand[] guiCommands;
    private ClientLogic clientLogic;
    private final ExecutorService threadPool = Executors.newSingleThreadExecutor();

    public static void main(String[] args) {
        launch(args);
    }

    public void setClientLogic(ClientLogic clientLogic) {
        this.clientLogic = clientLogic;
    }

    private Update updateCommand;
    private TicketTable mainTicketTable;
    private ResourceBundle bundle = ResourceBundle.getBundle("client.labafx.localization", new Locale("ru"));


    public void createCommands() {
        updateCommand = new Update(clientLogic);
        Show showCommand = new Show(clientLogic, updateCommand);
        guiCommands = new GUICommand[]{new Add(clientLogic), updateCommand, new RemoveLower(clientLogic), new RemoveById(clientLogic), new RemoveAt(clientLogic)};
        for (GUICommand command : guiCommands) {
            command.setCommands(guiCommands);
        }
        commands = new Command[]{showCommand, new Command("clear", "Clear", clientLogic), new Command("remove_first", "RemoveFirst", clientLogic), new ExecuteScript(clientLogic, showCommand)};
    }

    private void changeLocale(Locale locale) {
        Locale.setDefault(locale);
        bundle = ResourceBundle.getBundle("client.labafx.localization", locale);
        ZonedDateTime zonedDateTime = clientLogic.getCreationTime();
        ZonedDateTime outputZonedDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of(bundle.getString("date.timeZone")));
        String formattedDateTime = outputZonedDateTime.format(DateTimeFormatter.ofPattern(bundle.getString("date.format")));
        ((Label) controller.getMainPane().lookup("#initializationDateLabel")).setText(formattedDateTime);
        ((Label) controller.getMainPane().lookup("#collectionTypeNameLabel")).setText(bundle.getString("label.collectionTypeLabel"));
        ((Label) controller.getMainPane().lookup("#initializationDateNameLabel")).setText(bundle.getString("label.initializationDateLabel"));
        ((Label) controller.getMainPane().lookup("#numberOfElementsNameLabel")).setText(bundle.getString("label.numberOfElementsLabel"));
        for (GUICommand command : guiCommands)
            command.changeLocale(bundle);
        for (Command command : commands)
            command.changeLocale(bundle);
        mainTicketTable.changeLocale(bundle);
    }

    @Override
    public void start(Stage stage) throws IOException {
        Locale.setDefault(new Locale("ru", "RU"));
        FXMLLoader fxmlMainLoader = new FXMLLoader(MainWindow.class.getResource("main-view.fxml"));
        Scene mainScene = new Scene(fxmlMainLoader.load());
        controller = fxmlMainLoader.getController();
        controller.setCommands(guiCommands, commands);
        for (GUICommand command : guiCommands) {
            controller.getLeftStackPane().getChildren().add(0, command.createRootNode(stage));
        }
        controller.setName(clientLogic.userName);
        stage.setMinWidth(controller.getMainPane().getMinWidth());
        stage.setMinHeight(controller.getMainPane().getMinHeight());
        ((Label) mainScene.lookup("#collectionTypeLabel")).setText("Vector");
        ((Button) mainScene.lookup("#russianButton")).setOnAction(e -> changeLocale(new Locale("ru")));
        ((Button) mainScene.lookup("#russianButton")).setTooltip(new Tooltip("Русский"));
        ((Button) mainScene.lookup("#russianButton")).setGraphic(new ImageView(new Image("E:\\IdeaProjects\\labafx\\src\\main\\resources\\client\\labafx\\graphic\\flag_ru.png")));
        ((Button) mainScene.lookup("#danishButton")).setOnAction(e -> changeLocale(new Locale("da")));
        ((Button) mainScene.lookup("#danishButton")).setTooltip(new Tooltip("Dansk"));
        ((Button) mainScene.lookup("#danishButton")).setGraphic(new ImageView(new Image("E:\\IdeaProjects\\labafx\\src\\main\\resources\\client\\labafx\\graphic\\flag_da.png")));
        ((Button) mainScene.lookup("#islandButton")).setOnAction(e -> changeLocale(new Locale("is")));
        ((Button) mainScene.lookup("#islandButton")).setTooltip(new Tooltip("Íslenska english"));
        ((Button) mainScene.lookup("#islandButton")).setGraphic(new ImageView(new Image("E:\\IdeaProjects\\labafx\\src\\main\\resources\\client\\labafx\\graphic\\flag_is.png")));
        ((Button) mainScene.lookup("#ecuadorianButton")).setOnAction(e -> changeLocale(new Locale("es", "EC")));
        ((Button) mainScene.lookup("#ecuadorianButton")).setTooltip(new Tooltip("Español (Ecuador)"));
        ((Button) mainScene.lookup("#ecuadorianButton")).setGraphic(new ImageView(new Image("E:\\IdeaProjects\\labafx\\src\\main\\resources\\client\\labafx\\graphic\\flag_es.png")));

        for (Command command : commands)
            command.setButtonsActions();
        Platform.runLater(() -> {
            GraphicsContext gc = ((Canvas) (mainScene.lookup("#colorCanvas"))).getGraphicsContext2D();
            gc.setFill(Color.valueOf(clientLogic.getColor()));
            gc.fillOval(0, 0, 40, 40);
        });

        HBox canvasHBox = (HBox) mainScene.lookup("#canvasHbox");
        Canvas canvas = (Canvas) mainScene.lookup("#canvas");
        TicketDrawer ticketDrawer = new TicketDrawer(clientLogic.getTickets(), canvas, updateCommand, clientLogic.userName);

        Platform.runLater(() -> {
            ticketDrawer.setWidth(canvasHBox.getWidth() - 200);
            ticketDrawer.setHeight(canvasHBox.getHeight());
            canvas.setWidth(canvasHBox.getWidth() - 200);
            canvas.setHeight(canvasHBox.getHeight());
            canvasHBox.widthProperty().addListener((obs, oldVal, newVal) -> {
                canvas.setWidth(newVal.doubleValue() - 200);
                canvas.setHeight(canvasHBox.getHeight());
                ticketDrawer.setWidth(canvas.getWidth());
                ticketDrawer.setHeight(canvas.getHeight());
            });
            canvasHBox.heightProperty().addListener((obs, oldVal, newVal) -> {
                canvas.setHeight(newVal.doubleValue());
                canvas.setWidth(canvasHBox.getWidth() - 200);
                ticketDrawer.setWidth(canvas.getWidth());
                ticketDrawer.setHeight(canvas.getHeight());
            });
        });
        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(() -> Platform.runLater(() -> {
            ticketDrawer.update(clientLogic.getTickets());
            canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            ticketDrawer.draw();
        }), 1200, 400, TimeUnit.MILLISECONDS);
        threadPool.execute(() -> {
            ZonedDateTime zonedDateTime = ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("Europe/Moscow"));
            ZonedDateTime outputZonedDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of(bundle.getString("date.timeZone")));
            String formattedDateTime = outputZonedDateTime.format(DateTimeFormatter.ofPattern(bundle.getString("date.format")));
            mainTicketTable = new TicketTable(formattedDateTime, clientLogic, updateCommand, bundle);
            mainTicketTable.refresh(clientLogic.getTickets());
            mainTicketTable.setAutoupdate(true);
            Platform.runLater(() -> ((TabPane) mainScene.lookup("#tableTabPane")).getTabs().add(mainTicketTable.getMainNode()));
        });

        stage.setMaximized(true);
        stage.setScene(mainScene);
        stage.show();
        changeLocale(new Locale("ru"));
    }

    public MainController getController() {
        return controller;
    }
}