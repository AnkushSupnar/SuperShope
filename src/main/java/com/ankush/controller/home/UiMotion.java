package com.ankush.controller.home;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

/** Small, quick animations shared by the home shell: pages slide in, menu cards lift on hover. */
final class UiMotion {
    private static final Duration PAGE_IN = Duration.millis(220);
    private static final Duration LIFT = Duration.millis(120);

    private UiMotion() {
    }

    /** Fade the page in while it rises 12px into place. */
    static void enterPage(Node page) {
        page.setOpacity(0);
        page.setTranslateY(12);
        FadeTransition fade = new FadeTransition(PAGE_IN, page);
        fade.setToValue(1);
        TranslateTransition rise = new TranslateTransition(PAGE_IN, page);
        rise.setToY(0);
        rise.setInterpolator(Interpolator.EASE_OUT);
        new ParallelTransition(fade, rise).play();
    }

    /** Cards with the "menu-card" style rise 3px under the mouse (their shadow deepens in CSS). */
    static void liftCardsOnHover(Node page) {
        for (Node card : page.lookupAll(".menu-card")) {
            TranslateTransition up = new TranslateTransition(LIFT, card);
            up.setToY(-3);
            TranslateTransition down = new TranslateTransition(LIFT, card);
            down.setToY(0);
            card.setOnMouseEntered(e -> {
                down.stop();
                up.playFromStart();
            });
            card.setOnMouseExited(e -> {
                up.stop();
                down.playFromStart();
            });
        }
    }
}
