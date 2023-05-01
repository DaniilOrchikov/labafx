package client.labafx.command;

import javafx.animation.ParallelTransition;
import javafx.scene.Node;

public record NodeWithOpenAndCloseTransition(Node node, ParallelTransition openTransition, ParallelTransition closeTransition) {
}
