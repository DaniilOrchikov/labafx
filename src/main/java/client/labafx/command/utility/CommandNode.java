package client.labafx.command.utility;

import client.labafx.MainWindow;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Spinner;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Arrays;

public abstract class CommandNode {
    final int TRANSLATE_X = -80;
    final int TRANSLATE_Y = 60;
    StackPane stackPane;
    ParallelTransition openTransition;
    ParallelTransition closeTransition;

    abstract NodeWithOpenAndCloseTransition getRootNode() throws IOException;

    public static VBox createVbox(String filename) throws IOException {
        FXMLLoader fxmlMainLoader = new FXMLLoader(MainWindow.class.getResource(filename));
        return fxmlMainLoader.load();
    }


    public ParallelTransition createOpenTransition(Parent parentElement, ObservableList<Node> childElements) {
        ParallelTransition parallelTransition = AddMovementToNode.getOpenTransition(parentElement);
        for (int i = 0; i < childElements.size(); i++) {
            TranslateTransition childTransition = new TranslateTransition(Duration.millis(600), childElements.get(i));
            childTransition.setToY(Arrays.stream(childElements.toArray(), i + 1, childElements.size()).mapToInt(ticket -> (int) ((VBox) ticket).getMinHeight()).sum() - 20 * (childElements.size() - i - 1));
            parallelTransition.getChildren().add(childTransition);
        }

        return parallelTransition;
    }

    public ParallelTransition createCloseTransition(Parent parentElement, ObservableList<Node> childElements) {
        ParallelTransition parallelTransition = AddMovementToNode.getCloseTransition(parentElement);
        for (Node childElement : childElements) {
            TranslateTransition childTransition = new TranslateTransition(Duration.millis(500), childElement);
            childTransition.setToY(0);
            parallelTransition.getChildren().add(childTransition);
        }

        return parallelTransition;
    }

    /**
     * Изначально 4 ноды
     */
    public NodeWithOpenAndCloseTransition addNode(int id, Node node) {
        stackPane.getChildren().add(id, node);
        openTransition = createOpenTransition(stackPane, stackPane.getChildren());
        closeTransition = createCloseTransition(stackPane, stackPane.getChildren());
        return new NodeWithOpenAndCloseTransition(stackPane, openTransition, closeTransition);
    }

    abstract StackPane createStackPane() throws IOException;

    public void changingId(String commandName) {
        stackPane.lookup("#cancelButton").setId(commandName + "cancelButton");
        stackPane.lookup("#OKButton").setId(commandName + "OKButton");
    }

    public abstract void clearNode(String name);

    private static boolean isInteger(String value) {
        if (!value.matches("\\d*")) return false;
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isLong(String value) {
        if (!value.matches("\\d*")) return false;
        return value.length() <= 17;
    }

    private static boolean integerInRange(String value, int a, int b) {
        if (!value.matches("\\d*")) return false;
        try {
            int n = Integer.parseInt(value);
            return (a <= n && n < b);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static void setRangeValidator(Spinner<?> spinner, int a, int b) {
        spinner.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(""))
                spinner.getEditor().setText(String.valueOf(a));
            else if (!integerInRange(newValue, a, b)) {
                spinner.getEditor().setText(oldValue);
            }
        });
    }

    public static void setIntegerValidator(Spinner<?> spinner) {
        spinner.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(""))
                spinner.getEditor().setText("1");
            else if (!isInteger(newValue)) {
                spinner.getEditor().setText(oldValue);
            }
        });
    }

    public static void setLongValidator(Spinner<?> spinner) {
        spinner.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(""))
                spinner.getEditor().setText("1");
            else if (!isLong(newValue)) {
                spinner.getEditor().setText(oldValue);
            }
        });
    }
}
