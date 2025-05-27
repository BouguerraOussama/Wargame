package tn.isty.wargame.view;

import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
/**
 * Panneau de journalisation (log) affichant les messages dans une zone de texte.
 * Ce panneau est non éditable, affiche du texte avec retour à la ligne automatique,
 * et supporte l'affichage d'émoticônes grâce à la police Unicode Emoji.
 */
public class LogPanel extends VBox {
    private static final TextArea logArea = new TextArea();

    public LogPanel() {
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setStyle(
            "-fx-font-family: 'Noto Color Emoji', 'Segoe UI Emoji', 'Arial Unicode MS';" +
            "-fx-font-size: 14px;" +
            "-fx-text-fill: white;" +
            "-fx-control-inner-background: #000000;"
        );
        this.getChildren().add(logArea);
        VBox.setVgrow(logArea, Priority.ALWAYS);
    }
    /**
     * Ajoute un nouveau message dans le journal et fait défiler automatiquement vers le bas.
     *
     *  String text est Le texte à ajouter dans la zone de log.
     */
    public static void append(String text) {
        logArea.appendText(text + "\n");
        logArea.setScrollTop(Double.MAX_VALUE);
    }
}
