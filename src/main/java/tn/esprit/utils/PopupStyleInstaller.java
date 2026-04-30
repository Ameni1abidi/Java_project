package tn.esprit.utils;

import javafx.collections.ListChangeListener;
import javafx.scene.Scene;
import javafx.stage.Window;

public final class PopupStyleInstaller {

    private static final String APP_STYLESHEET = PopupStyleInstaller.class
            .getResource("/style.css")
            .toExternalForm();

    private PopupStyleInstaller() {
    }

    public static void install() {
        Window.getWindows().forEach(PopupStyleInstaller::applyStylesheetIfMissing);
        Window.getWindows().addListener((ListChangeListener<Window>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Window window : change.getAddedSubList()) {
                        applyStylesheetIfMissing(window);
                    }
                }
            }
        });
    }

    private static void applyStylesheetIfMissing(Window window) {
        Scene scene = window.getScene();
        if (scene == null) {
            return;
        }
        if (!scene.getStylesheets().contains(APP_STYLESHEET)) {
            scene.getStylesheets().add(APP_STYLESHEET);
        }
    }
}
