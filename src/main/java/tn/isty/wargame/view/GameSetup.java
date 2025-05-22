package tn.isty.wargame.view;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tn.isty.wargame.controller.GameController;
import tn.isty.wargame.model.*;
import tn.isty.wargame.util.SaveManager;

import java.util.ArrayList;
import java.util.List;

public class GameSetup {

    public static Scene createSetupScene(Stage stage) {
        Label label = new Label("Choisissez le nombre de joueurs");
        label.setStyle("-fx-font-size: 24px; -fx-text-fill: white;");

        VBox playerSelectionBox = new VBox(20);
        playerSelectionBox.setAlignment(Pos.CENTER);

        Button[] playerButtons = new Button[3];  // 2 à 4 joueurs

        for (int i = 0; i < 3; i++) {
            int playerCount = i + 2;
            Button btn = new Button(playerCount + " joueurs");
            btn.setStyle("-fx-font-size: 18px; -fx-background-color: #444; -fx-text-fill: white;");
            btn.setOnAction(e -> showPlayerChoice(stage, playerCount));
            playerButtons[i] = btn;
        }

        playerSelectionBox.getChildren().add(label);
        playerSelectionBox.getChildren().addAll(playerButtons);

        StackPane root = new StackPane();
        root.setBackground(getBackgroundImage());
        root.getChildren().add(playerSelectionBox);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setFullScreen(true);

        return scene;
    }

    private static void showPlayerChoice(Stage stage, int numPlayers) {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);

        Label label = new Label("Choisissez qui sera IA (max 1)");
        label.setStyle("-fx-font-size: 24px; -fx-text-fill: white;");

        List<Boolean> iaFlags = new ArrayList<>();
        for (int i = 0; i < numPlayers; i++) {
            iaFlags.add(false); // par défaut humain
        }

        VBox buttonBox = new VBox(10);
        for (int i = 0; i < numPlayers; i++) {
            int index = i;
            Button toggleBtn = new Button("Joueur " + (i + 1) + " : Humain");
            toggleBtn.setStyle("-fx-font-size: 18px; -fx-background-color: #444; -fx-text-fill: white;");
            toggleBtn.setOnAction(e -> {
                boolean isIA = !iaFlags.get(index);
                iaFlags.set(index, isIA);
                toggleBtn.setText("Joueur " + (index + 1) + " : " + (isIA ? "IA" : "Humain"));
            });
            buttonBox.getChildren().add(toggleBtn);
        }

        Button nextButton = new Button("Choisir le terrain");
        nextButton.setStyle("-fx-font-size: 18px; -fx-background-color: #222; -fx-text-fill: white;");
        nextButton.setOnAction(e -> showTerrainChoice(stage, numPlayers, iaFlags));

        layout.getChildren().addAll(label, buttonBox, nextButton);

        StackPane root = new StackPane();
        root.setBackground(getBackgroundImage());
        root.getChildren().add(layout);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setFullScreen(true);
    }

    private static void showTerrainChoice(Stage stage, int numPlayers, List<Boolean> iaFlags) {
        VBox terrainBox = new VBox(20);
        terrainBox.setAlignment(Pos.CENTER);

        Label label = new Label("Choisissez le terrain");
        label.setStyle("-fx-font-size: 24px; -fx-text-fill: white;");

        Button cityButton = createTerrainButton(stage, "Ville", "#2c3e50", plateau -> plateau.generateCityTerrain(10, 10), numPlayers, iaFlags);
        Button islandButton = createTerrainButton(stage, "Île", "#16a085", plateau -> plateau.generateIsland(10, 10), numPlayers, iaFlags);

        terrainBox.getChildren().addAll(label, cityButton, islandButton);

        StackPane root = new StackPane();
        root.setBackground(getBackgroundImage());
        root.getChildren().add(terrainBox);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setFullScreen(true);
    }

    private static Button createTerrainButton(Stage stage, String name, String bgColor,
                                              TerrainGenerator generator, int numPlayers, List<Boolean> iaFlags) {
        Button button = new Button(name);
        button.setStyle("-fx-font-size: 18px; -fx-background-color: " + bgColor + "; -fx-text-fill: white;");
        button.setOnAction(e -> {
            Plateau plateau = new Plateau(14, 33);
            generator.generate(plateau);

            List<Player> players = new ArrayList<>();
            for (int i = 0; i < numPlayers; i++) {
                players.add(new Player("Joueur " + (i + 1), iaFlags.get(i)));
            }

            GameState gameState = new GameState(players, plateau);
            gameState.initializeGame();
            HexagonTile.setSharedGameState(gameState);

            // Exemple : chaque joueur commence avec une unité (A ou T)
            for (int i = 0; i < players.size(); i++) {
                Player p = players.get(i);
                Unit u = (i % 2 == 0)
                        ? new Unit("A", "archer", 20, 5, 2, 3, 3, p)
                        : new Unit("T", "tank", 30, 4, 6, 2, 2, p);
                p.addUnit(u);
                placerUniteDansZoneValide(plateau, u, i);
            }

            GameController controller = new GameController(gameState);

            Button endTurnButton = new Button("Fin de tour");
            endTurnButton.setLayoutX(20);
            endTurnButton.setLayoutY(20);
            endTurnButton.setStyle("-fx-font-size: 16px;");
            endTurnButton.setOnAction(ev -> controller.endTurn());

            Button saveButton = new Button("Sauvegarder");
            saveButton.setLayoutX(140);
            saveButton.setLayoutY(20);
            saveButton.setStyle("-fx-font-size: 16px;");
            saveButton.setOnAction(ev -> SaveManager.sauvegarder(gameState, "savegame.ser"));

            Button returnButton = new Button("Retour Menu");
            returnButton.setLayoutX(280);
            returnButton.setLayoutY(20);
            returnButton.setStyle("-fx-font-size: 16px;");
            returnButton.setOnAction(ev -> GameMenu.createMenuScene(stage));

            plateau.getChildren().addAll(endTurnButton, saveButton, returnButton);

            Scene gameScene = new Scene(plateau, 1280, 800);
            stage.setScene(gameScene);
            stage.setTitle("Wargame - Partie");
        });

        return button;
    }

    // 🔁 Nouvelle méthode de placement intelligent
    private static void placerUniteDansZoneValide(Plateau plateau, Unit unite, int playerIndex) {
        int rows = plateau.getRows();
        int cols = plateau.getCols();

        int[][] zones = {
                {1, 1},                  // Joueur 1
                {rows - 2, cols - 2},    // Joueur 2
                {1, cols - 2},           // Joueur 3
                {rows - 2, 1}            // Joueur 4
        };

        int baseRow = zones[playerIndex][0];
        int baseCol = zones[playerIndex][1];

        for (int rOffset = -1; rOffset <= 1; rOffset++) {
            for (int cOffset = -1; cOffset <= 1; cOffset++) {
                int row = baseRow + rOffset;
                int col = baseCol + cOffset;
                HexagonTile tile = plateau.getCase(row, col);

                if (tile != null && tile.getUnit() == null) {
                    TerrainType t = tile.getTerrainType();
                    if (t == TerrainType.PLAINE || t == TerrainType.FORET || t == TerrainType.COLLINE) {
                        plateau.placerUnite(row, col, unite);
                        return;
                    }
                }
            }
        }

        System.err.println("⚠️ Impossible de placer l’unité de " + unite.getOwner().getName());
    }

    private static Background getBackgroundImage() {
        return new Background(new BackgroundImage(
                new Image(GameMenu.class.getResource("/images/war_background.jpg").toExternalForm(),
                        -1, -1, true, true),
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(100, 100, true, true, true, false)
        ));
    }

    @FunctionalInterface
    private interface TerrainGenerator {
        void generate(Plateau plateau);
    }
}
