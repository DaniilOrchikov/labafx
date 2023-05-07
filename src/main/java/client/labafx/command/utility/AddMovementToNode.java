package client.labafx.command.utility;

import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Parent;
import javafx.util.Duration;

public class AddMovementToNode {
    public static ParallelTransition getOpenTransition(Parent parentElement, int shiftWidth) {
        TranslateTransition openTransition = new TranslateTransition(Duration.millis(500), parentElement);
        openTransition.setToX(shiftWidth);
        return new ParallelTransition(openTransition);
    }

    public static ParallelTransition getOpenTransition(Parent parentElement) {
        TranslateTransition openTransition = new TranslateTransition(Duration.millis(500), parentElement);
        openTransition.setToX(230);
        return new ParallelTransition(openTransition);
    }

    public static ParallelTransition getCloseTransition(Parent parentElement) {
        TranslateTransition openTransition = new TranslateTransition(Duration.millis(500), parentElement);
        openTransition.setToX(-80);
        return new ParallelTransition(openTransition);
    }
}
