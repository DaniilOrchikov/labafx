package client.labafx.draw;

import client.labafx.command.Update;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.util.Duration;
import ticket.TicketBuilder;
import ticket.TicketType;

public class TicketPicture {
    private final GraphicsContext graphicsContext;
    private TicketBuilder ticketBuilder;
    private final double width = 70;
    private final double height = 50;
    private final Color color;
    private boolean firstDraw = true;
    private double x = 0;
    private double y = 0;
    private EventHandler<MouseEvent> mouseMovedHandler;
    private EventHandler<MouseEvent> mousePressedHandler;
    private Canvas canvas;
    private boolean selected = false;
    private final Update updateCommand;

    public TicketPicture(TicketBuilder ticketBuilder, Canvas canvas, Update updateCommand, String userName) {
        this.updateCommand = updateCommand;
        this.canvas = canvas;
        this.graphicsContext = canvas.getGraphicsContext2D();
        this.ticketBuilder = ticketBuilder;
        color = Color.valueOf(ticketBuilder.getUserColor());
        mousePressedHandler = event -> {
            double mouseX = event.getX();
            double mouseY = event.getY();
            if (mouseX >= x - width / 2 && mouseX <= x + width / 2 && mouseY >= y - height / 2 && mouseY <= y + height / 2 && selected && ticketBuilder.getUserName().equals(userName)) {
                updateCommand.fromTicketId(ticketBuilder.getId());
            }
        };
        mouseMovedHandler = event -> {
            double mouseX = event.getX();
            double mouseY = event.getY();
            selected = mouseX >= x - width / 2 && mouseX <= x + width / 2 && mouseY >= y - height / 2 && mouseY <= y + height / 2;
        };
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        canvas.addEventHandler(MouseEvent.MOUSE_MOVED, mouseMovedHandler);
    }

    private void updateXY(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void draw(double x, double y) {
        if (x != this.x || y != this.y) updateXY(x, y);
        if (firstDraw) {
            firstDraw(x, y);
            firstDraw = false;
        } else {
            Platform.runLater(() -> {
                graphicsContext.setFill(color);
                setStrokeColor();
                graphicsContext.fillPolygon(new double[]{x - width / 3, x, x + width / 3}, new double[]{y + height / 2, y + height / 2 + height / 3, y + height / 2}, 3);
                graphicsContext.strokePolygon(new double[]{x - width / 3, x, x + width / 3}, new double[]{y + height / 2, y + height / 2 + height / 3, y + height / 2}, 3);
                graphicsContext.fillRoundRect(x - width / 2, y - height / 2, width, height, 25, 25);
                graphicsContext.strokeRoundRect(x - width / 2, y - height / 2, width, height, 25, 25);
                graphicsContext.setFill(Color.BLACK);
                String text = ticketBuilder.getName();
                if (text.length() > 7) text = text.substring(0, 7) + "..";
                graphicsContext.fillText(text, getTextPosX(x), y);
            });
        }
    }

    public void firstDraw(double x, double y) {
        DoubleProperty pWidth = new SimpleDoubleProperty();
        DoubleProperty pHeight = new SimpleDoubleProperty();
        DoubleProperty pX = new SimpleDoubleProperty();
        DoubleProperty pY = new SimpleDoubleProperty();
        IntegerProperty fontSize = new SimpleIntegerProperty();

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(0),
                        new KeyValue(pWidth, width / 3),
                        new KeyValue(pHeight, 0),
                        new KeyValue(pX, width / 3),
                        new KeyValue(pY, height / 2),
                        new KeyValue(fontSize, 0)
                ),
                new KeyFrame(Duration.millis(300),
                        new KeyValue(pY, -height / 4)
                ),
                new KeyFrame(Duration.millis(400),
                        new KeyValue(pWidth, width),
                        new KeyValue(pHeight, height),
                        new KeyValue(pX, 0),
                        new KeyValue(pY, 0),
                        new KeyValue(fontSize, 14)
                )
        );
        timeline.setAutoReverse(false);
        timeline.setCycleCount(1);

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                Platform.runLater(() -> {
                    setStrokeColor();
                    graphicsContext.setFill(color);
                    graphicsContext.fillRoundRect(x - width / 2 + pX.doubleValue(), y - height / 2 + pY.doubleValue(), pWidth.doubleValue(), pHeight.doubleValue(), 25, 25);
                    graphicsContext.strokeRoundRect(x - width / 2 + pX.doubleValue(), y - height / 2 + pY.doubleValue(), pWidth.doubleValue(), pHeight.doubleValue(), 25, 25);
                    graphicsContext.setFill(Color.BLACK);
                    Font font = new Font(fontSize.intValue());
                    graphicsContext.setFont(font);
                    String str = ticketBuilder.getName();
                    if (str.length() > 7) str = str.substring(0, 7) + "..";
                    graphicsContext.fillText(str, getTextPosX(x + pX.doubleValue()), y);
                });
            }
        };
        PauseTransition pauseTransition = new PauseTransition(Duration.millis(400));
        pauseTransition.setOnFinished(event -> {
            timer.stop();
            timeline.stop();
        });
        timer.start();
        timeline.play();
        pauseTransition.play();
    }

    private void setStrokeColor() {
        Color strokeColor = Color.GRAY;
        if (ticketBuilder.getType().equals(TicketType.VIP)) strokeColor = Color.GOLD;
        if (ticketBuilder.getType().equals(TicketType.USUAL)) strokeColor = Color.BLUE;
        if (ticketBuilder.getType().equals(TicketType.BUDGETARY)) strokeColor = Color.GREEN;
        graphicsContext.setStroke(strokeColor);
        graphicsContext.setLineWidth(2);
    }

    public TicketBuilder getTicketBuilder() {
        return ticketBuilder;
    }

    private double getTextPosX(double x) {
        return x - width / 2 + width / 6;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void remove() {
        canvas.removeEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        canvas.removeEventHandler(MouseEvent.MOUSE_MOVED, mouseMovedHandler);
    }
}
