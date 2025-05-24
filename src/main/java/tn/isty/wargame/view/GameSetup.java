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

    // ✅ Zones de départ 5x5 par joueur (ligne, colonne)
    private static final int[][] ZONES_DEPLACEMENT = {
            {0, 0},    // Joueur 1
            {5, 5},    // Joueur 2
            {0, 5},    // Joueur 3
            {5, 0}     // Joueur 4
    };

    public static Scene createSetupScene(Stage stage) {
        Label label = new Label("Choisissez le nombre de joueurs");
        label.setStyle("-fx-font-size: 24px; -fx-text-fill: white;");

        VBox playerSelectionBox = new VBox(20);
        playerSelectionBox.setAlignment(Pos.CENTER);

        Button[] playerButtons = new Button[3];
        for (int i = 0; i < 3; i++) {
            int playerCount = i + 2;
            Button btn = new Button(playerCount + " joueurs");
            btn.setStyle("-fx-font-size: 18px; -fx-background-color: #444; -fx-text-fill: white;");
            btn.setOnAction(e -> showPlayerChoice(stage, playerCount));
            playerButtons[i] = btn;
        }

        playerSelectionBox.getChildren().add(label);
        playerSelectionBox.getChildren().addAll(playerButtons);

        StackPane root = new StackPane(playerSelectionBox);
        root.setBackground(getBackgroundImage());

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
        for (int i = 0; i < numPlayers; i++) iaFlags.add(false);

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

        StackPane root = new StackPane(layout);
        root.setBackground(getBackgroundImage());

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

        StackPane root = new StackPane(terrainBox);
        root.setBackground(getBackgroundImage());

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
            Plateau plateau = new Plateau(10, 10);
            generator.generate(plateau);

            List<Player> players = new ArrayList<>();
            for (int i = 0; i < numPlayers; i++) {
                players.add(new Player("Joueur " + (i + 1), iaFlags.get(i)));
            }

            showArmyChoice(stage, players, plateau);
        });

        return button;
    }

    private static void showArmyChoice(Stage stage, List<Player> players, Plateau plateau) {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);

        Label label = new Label("Sélectionnez l’armée pour chaque joueur");
        label.setStyle("-fx-font-size: 24px; -fx-text-fill: white;");

        VBox playerSelectors = new VBox(10);

        for (Player player : players) {
            if (player.isAI()) {
                player.setArmyType(ArmyType.ALLEMAGNE); // par défaut
                continue;
            }

            Label playerLabel = new Label(player.getName() + " : ");
            playerLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");

            HBox buttons = new HBox(10);
            buttons.setAlignment(Pos.CENTER);

            for (ArmyType type : ArmyType.values()) {
                Button btn = new Button(type.name());
                btn.setOnAction(e -> {
                    player.setArmyType(type);
                    System.out.println(player.getName() + " a choisi " + type.name());
                    btn.setDisable(true);
                });
                buttons.getChildren().add(btn);
            }

            VBox box = new VBox(5, playerLabel, buttons);
            box.setAlignment(Pos.CENTER);
            playerSelectors.getChildren().add(box);
        }

        Button launchBtn = new Button("Lancer la partie");
        launchBtn.setStyle("-fx-font-size: 18px; -fx-background-color: #222; -fx-text-fill: white;");
        launchBtn.setOnAction(e -> {
            boolean allChosen = players.stream()
                    .filter(p -> !p.isAI())
                    .allMatch(p -> p.getArmyType() != null);
            if (!allChosen) {
                System.out.println("❗ Tous les joueurs humains doivent choisir une armée !");
                return;
            }

            launchGame(stage, players, plateau);
        });

        layout.getChildren().addAll(label, playerSelectors, launchBtn);

        StackPane root = new StackPane(layout);
        root.setBackground(getBackgroundImage());

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setMaximized(true);
    }

    private static void launchGame(Stage stage, List<Player> players, Plateau plateau) {
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            ArmyFactory factory = ArmyFactoryProvider.getFactory(player.getArmyType());
            List<Unit> units = factory.createArmy(player);
            for (Unit unit : units) player.addUnit(unit);

            placerUnitesPourJoueur(player, plateau, i);
        }

        GameState gameState = new GameState(players, plateau);
        gameState.initializeGame();
        HexagonTile.setSharedGameState(gameState);

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
        controller.playTurn();
    }

    private static void placerUnitesPourJoueur(Player joueur, Plateau plateau, int playerIndex) {
        if (playerIndex >= ZONES_DEPLACEMENT.length) {
            System.err.println("❌ Trop de joueurs pour les zones prédéfinies !");
            return;
        }

        List<Unit> unites = joueur.getUnits();
        int baseRow = ZONES_DEPLACEMENT[playerIndex][0];
        int baseCol = ZONES_DEPLACEMENT[playerIndex][1];
        int endRow = Math.min(baseRow + 5, plateau.getRows());
        int endCol = Math.min(baseCol + 5, plateau.getCols());

        int unitIndex = 0;

        for (int row = baseRow; row < endRow; row++) {
            for (int col = baseCol; col < endCol; col++) {
                if (unitIndex >= unites.size()) return;
                if (row >= plateau.getRows() || col >= plateau.getCols()) continue;

                HexagonTile tile = plateau.getCase(row, col);
                if (tile == null || tile.getUnit() != null) continue;

                TerrainType terrain = tile.getTerrainType();
                if (terrain == TerrainType.PLAINE || terrain == TerrainType.FORET || terrain == TerrainType.COLLINE) {
                    plateau.placerUnite(row, col, unites.get(unitIndex));
                    unitIndex++;
                }
            }
        }

        if (unitIndex < unites.size()) {
            System.err.println("⚠️ Seulement " + unitIndex + " unités placées sur " + unites.size() + " pour " + joueur.getName());
        }
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
