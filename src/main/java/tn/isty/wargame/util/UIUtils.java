package tn.isty.wargame.util;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.stage.Stage;
import tn.isty.wargame.controller.GameController;
import tn.isty.wargame.model.GameState;
/**
 * Classe utilitaire pour la création d'éléments d'interface utilisateur réutilisables.
 * Contient des méthodes statiques pour créer des boutons stylisés, gérer les fonds d'écran,
 * le contrôle audio et faciliter l'affichage des scènes.
 */
public class UIUtils {
    /**
     * Crée un bouton de menu avec un style spécifique.
     */
    public static Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("""
            -fx-font-family: 'Georgia';
            -fx-font-size: 18px;
            -fx-background-color: #4E3B31;
            -fx-text-fill: white;
            -fx-background-radius: 6;
            -fx-border-color: #6B8E23;
            -fx-border-width: 2px;
            -fx-border-radius: 6;
            -fx-padding: 10 20;
        """);
        btn.setPrefWidth(260);
        return btn;
    }
    /**
     * Crée un bouton blanc avec un style simple.
     */
    public static Button createWhiteButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("""
            -fx-font-size: 16px;
            -fx-background-color: white;
            -fx-text-fill: black;
            -fx-border-radius: 5;
            -fx-padding: 10px;
        """);
        btn.setPrefWidth(160);
        return btn;
    }
    /**
     * Renvoie un panneau avec l'image de fond du jeu redimensionnable.

     */
    public static Pane getBackgroundImagePane() {
        Image img = new Image(UIUtils.class.getResource("/images/war_background.jpg").toExternalForm());
        ImageView imageView = new ImageView(img);
        imageView.setPreserveRatio(false);

        StackPane root = new StackPane(imageView);
        root.widthProperty().addListener((obs, oldVal, newVal) -> imageView.setFitWidth(newVal.doubleValue()));
        root.heightProperty().addListener((obs, oldVal, newVal) -> imageView.setFitHeight(newVal.doubleValue()));
        return root;
    }

    /**
     * Enveloppe tout le contenu dans un fond plein écran.
     */
    public static StackPane wrapWithBackground(Pane content) {
        Pane background = getBackgroundImagePane();
        StackPane root = new StackPane();
        root.getChildren().addAll(background, content);
        return root;
    }

    /**
     * Crée un bouton pour activer/désactiver le son d’ambiance.
     */
    public static Button createSoundToggle(AudioClip audio) {
        Button btn = new Button("🔊");
        btn.setStyle("""
            -fx-font-size: 20px;
            -fx-background-color: transparent;
            -fx-text-fill: white;
            -fx-padding: 4px 8px;
        """);
        btn.setOnAction(e -> {
            if (audio.isPlaying()) {
                audio.stop();
                btn.setText("🔇");
            } else {
                audio.play();
                btn.setText("🔊");
            }
        });
        return btn;
    }

    public static VBox createSidePanel(GameState gameState, GameController gameController) {
        VBox panel = new VBox();
        panel.getChildren().add(new Label("Panneau d’information"));
        return panel;
    }

    /**
     * Affiche la scène en plein écran sans animation ni transition.
     */
    public static void showScene(Stage stage, Scene scene) {
        stage.setScene(scene);
        stage.setFullScreen(true);
    }
}
