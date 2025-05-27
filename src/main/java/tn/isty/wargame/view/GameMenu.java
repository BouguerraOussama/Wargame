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
import tn.isty.wargame.controller.GameController;
import tn.isty.wargame.model.*;
import tn.isty.wargame.util.SaveManager;
import tn.isty.wargame.util.UIUtils;

import java.net.URL;

public class GameMenu {

    private static AudioClip ambiance;

    public static Scene createMenuScene(Stage stage) {
        // 🎧 Musique d’ambiance
        if (ambiance == null) {
            try {
                URL audioUrl = GameMenu.class.getResource("/audio/front_ww1.wav");
                if (audioUrl != null) {
                    ambiance = new AudioClip(audioUrl.toExternalForm());
                    ambiance.setCycleCount(AudioClip.INDEFINITE);
                    ambiance.play();
                    System.out.println("🎵 Musique d’ambiance lancée !");
                } else {
                    System.err.println("❌ Fichier audio introuvable : /audio/front_ww1.wav");
                }
            } catch (Exception e) {
                System.err.println("❌ Erreur chargement audio : " + e.getMessage());
            }
        }

        Button soundToggle = UIUtils.createSoundToggle(ambiance);
        StackPane.setAlignment(soundToggle, Pos.TOP_RIGHT);
        soundToggle.setTranslateX(-20);
        soundToggle.setTranslateY(20);

        // 🧱 Titre
        Label title = new Label("WARGAME");
        title.setTextFill(Color.web("#F0EAD6"));
        title.setFont(Font.font("Georgia", FontWeight.EXTRA_BOLD, 72));
        title.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 4, 0.5, 1, 1);");

        // 🕹️ Boutons
        Button startButton = UIUtils.createMenuButton("Nouvelle Partie");
        Button loadButton = UIUtils.createMenuButton("Charger Partie");
        Button helpButton = UIUtils.createMenuButton("Aide / Tutoriel");
        Button quitButton = UIUtils.createMenuButton("Quitter");

        // ▶️ Actions
        startButton.setOnAction(e -> UIUtils.showScene(stage, GameSetup.createSetupScene(stage)));

        helpButton.setOnAction(e -> TutorialScreen.show(stage));

        quitButton.setOnAction(e -> {
            if (ambiance != null) ambiance.stop();
            stage.close();
        });

        loadButton.setOnAction(e -> {
            GameState loaded = SaveManager.charger("savegame.ser");
            if (loaded == null) {
                System.err.println("❌ Échec du chargement de la partie.");
                return;
            }

            Plateau old = loaded.getBoard();
            Plateau newBoard = new Plateau(old.getRows(), old.getCols());

            for (Player p : loaded.getAllPlayers()) {
                for (Unit u : p.getUnits()) {
                    HexagonTile tile = newBoard.getCase(u.getPosition().getRow(), u.getPosition().getCol());
                    tile.setUnit(u);
                    u.setPosition(tile);
                }
            }

            loaded.setBoard(newBoard);
            HexagonTile.setSharedGameState(loaded);
            newBoard.refreshVisibility();

            GameController controller = new GameController(loaded);

            Button endTurn = UIUtils.createMenuButton("Fin de tour");
            endTurn.setLayoutX(20);
            endTurn.setLayoutY(20);
            endTurn.setOnAction(ev -> controller.endTurn());

            Button save = UIUtils.createMenuButton("Sauvegarder");
            save.setLayoutX(140);
            save.setLayoutY(20);
            save.setOnAction(ev -> SaveManager.sauvegarder(loaded, "savegame.ser"));

            Button retour = UIUtils.createMenuButton("Retour Menu");
            retour.setLayoutX(280);
            retour.setLayoutY(20);
            retour.setOnAction(ev -> UIUtils.showScene(stage, createMenuScene(stage)));

            newBoard.getChildren().addAll(endTurn, save, retour);

            VBox panel = new VBox(10);
            panel.setStyle("-fx-background-color: #222; -fx-padding: 10;");
            panel.setPrefWidth(200);
            Label current = new Label("Joueur courant : " + loaded.getCurrentPlayer().getName());
            current.setStyle("-fx-text-fill: white;");
            panel.getChildren().add(current);

            HBox layout = new HBox(panel, newBoard);
            Scene gameScene = new Scene(layout);
            UIUtils.showScene(stage, gameScene);
            stage.setTitle("Wargame - Partie chargée");
            controller.playTurn();
        });

        // 📦 Organisation
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
        UIUtils.showScene(stage, scene);
        return scene;
    }
}
