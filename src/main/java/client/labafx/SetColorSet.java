package client.labafx;

import javafx.scene.Node;
import javafx.scene.paint.Color;

public class SetColorSet {
    public static void set(Node node, String colorHex) {
        Color bg = Color.web(colorHex);
        if (bg.getBrightness() < 0.6)
            node.setStyle(
                    "-fx-background-color: " + "#" + bg.toString().substring(2) + ";" +
                            "-fx-border-color: " + "#" + bg.darker().toString().substring(2) + ";" +
                            "-fx-border-width: 1;" +
                            "-fx-text-fill: WHITE;");
        else {
            node.setStyle(
                    "-fx-background-color: " + "#" + bg.toString().substring(2) + ";" +
                            "-fx-border-color: " + "#" + bg.darker().toString().substring(2) + ";" +
                            "-fx-border-width: 1;");
        }
        node.setOnMouseEntered(e -> {
            if (bg.getBrightness() >= 0.6)
                node.setStyle(
                        "-fx-background-color: " + "#" + bg.brighter().toString().substring(2) + ";" +
                                "-fx-border-color: " + "#" + bg.darker().toString().substring(2) + ";" +
                                "-fx-border-width: 1;");
            else
                node.setStyle(
                        "-fx-background-color: " + "#" + bg.darker().toString().substring(2) + ";" +
                                "-fx-border-color: " + "#" + bg.darker().toString().substring(2) + ";" +
                                "-fx-border-width: 1;" +
                                "-fx-text-fill: WHITE;");
        });
        node.setOnMouseExited(e -> {
            if (bg.getBrightness() >= 0.6) node.setStyle(
                    "-fx-background-color: " + "#" + bg.toString().substring(2) + ";" +
                            "-fx-border-color: " + "#" + bg.darker().toString().substring(2) + ";" +
                            "-fx-border-width: 1;");
            else node.setStyle(
                    "-fx-background-color: " + "#" + bg.toString().substring(2) + ";" +
                            "-fx-border-color: " + "#" + bg.darker().toString().substring(2) + ";" +
                            "-fx-border-width: 1;" +
                            "-fx-text-fill: WHITE;");
        });
        node.setOnMousePressed(e -> {
            if (bg.getBrightness() >= 0.6)
                node.setStyle(
                        "-fx-background-color: " + "#" + bg.darker().toString().substring(2) + ";" +
                                "-fx-border-color: " + "#" + bg.darker().toString().substring(2) + ";" +
                                "-fx-border-width: 1;");
            else
                node.setStyle(
                        "-fx-background-color: " + "#" + bg.brighter().toString().substring(2) + ";" +
                                "-fx-border-color: " + "#" + bg.darker().toString().substring(2) + ";" +
                                "-fx-border-width: 1;" +
                                "-fx-text-fill: WHITE;");
        });
        node.setOnMouseReleased(e -> {
            if (bg.getBrightness() >= 0.6)
                node.setStyle(
                        "-fx-background-color: " + "#" + bg.toString().substring(2) + ";" +
                                "-fx-border-color: " + "#" + bg.darker().toString().substring(2) + ";" +
                                "-fx-border-width: 1;");
            else node.setStyle(
                    "-fx-background-color: " + "#" + bg.toString().substring(2) + ";" +
                            "-fx-border-color: " + "#" + bg.darker().toString().substring(2) + ";" +
                            "-fx-border-width: 1;" +
                            "-fx-text-fill: WHITE;");
        });
    }
}
