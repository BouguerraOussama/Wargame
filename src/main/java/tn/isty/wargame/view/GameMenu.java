package tn.isty.wargame.view;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import tn.isty.wargame.model.GameState;
import tn.isty.wargame.model.HexagonTile;
import tn.isty.wargame.util.SaveManager;
import tn.isty.wargame.util.UIUtils;

import java.io.File;
import java.net.URL;

public class GameMenu {

    private static AudioClip ambiance;

    public static Scene createMenuScene(Stage stage) {
        if (ambiance == null) {
            try {
                URL audioUrl = GameMenu.class.getResource("/audio/front_ww1.wav");
                if (audioUrl != null) {
                    ambiance = new AudioClip(audioUrl.toExternalForm());
                    ambiance.setCycleCount(AudioClip.INDEFINITE);
                    ambiance.play();
                    System.out.println(" Musique d’ambiance lancée !");
                } else {
                    System.err.println(" Fichier audio introuvable");
                }
            } catch (Exception e) {
                System.err.println(" Erreur chargement audio : " + e.getMessage());
            }
        }

        Button soundToggle = UIUtils.createSoundToggle(ambiance);
        StackPane.setAlignment(soundToggle, Pos.TOP_RIGHT);
        soundToggle.setTranslateX(-20);
        soundToggle.setTranslateY(20);

        Label title = new Label("WARGAME");
        title.setTextFill(Color.web("#F0EAD6"));
        title.setFont(Font.font("Georgia", FontWeight.EXTRA_BOLD, 72));
        title.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 4, 0.5, 1, 1);");

        Button startButton = UIUtils.createMenuButton("Nouvelle Partie");
        Button loadButton = UIUtils.createMenuButton("Charger Partie");
        Button helpButton = UIUtils.createMenuButton("Aide / Tutoriel");
        Button quitButton = UIUtils.createMenuButton("Quitter");

        startButton.setOnAction(e -> stage.setScene(GameSetup.createSetupScene(stage)));
        helpButton.setOnAction(e -> TutorialScreen.show(stage));
        quitButton.setOnAction(e -> {
            if (ambiance != null) ambiance.stop();
            stage.close();
        });

        loadButton.setOnAction(e -> {
            String chemin = System.getProperty("user.dir") + "/savegame.ser";
            File file = new File(chemin);
            if (!file.exists()) {
                System.err.println(" Aucun fichier de sauvegarde trouvé.");
                return;
            }

            GameState loaded = SaveManager.charger(chemin);
            if (loaded == null) {
                System.err.println(" Erreur lors du chargement.");
                return;
            }

            HexagonTile.setSharedGameState(loaded);
            loaded.getBoard().refreshVisibility();

            Scene gameScene = GameView.createGameScene(stage, loaded);
            stage.setScene(gameScene);
            stage.setTitle("Wargame - Partie chargée");
        });

        VBox menuBox = new VBox(30, title, startButton, loadButton, helpButton, quitButton);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setStyle("-fx-background-color: rgba(0,40,0,0.4); -fx-padding: 30; -fx-background-radius: 10;");

        Rectangle overlay = new Rectangle();
        overlay.setFill(Color.rgb(0, 40, 0, 0.25));
        overlay.widthProperty().bind(stage.widthProperty());
        overlay.heightProperty().bind(stage.heightProperty());

        StackPane root = new StackPane(
                UIUtils.getBackgroundImagePane(),
                overlay,
                menuBox,
                soundToggle
        );

        Scene scene = new Scene(root);
        stage.setScene(scene);
        return scene;
    }
}
