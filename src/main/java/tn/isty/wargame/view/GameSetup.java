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
import java.util.Collections;
import java.util.List;

public class GameSetup {

    private static final int[][] ZONES_DEPLACEMENT = {
        {0, 0}, {0, 10}, {10, 0}, {10, 10}
    };

    public static Scene createSetupScene(Stage stage) {
        VBox root = new VBox(30);
        root.setAlignment(Pos.CENTER);
        root.setBackground(getBackgroundImage());

        Label label = new Label("Choisissez le nombre de joueurs");
        label.setStyle("-fx-font-size: 24px; -fx-text-fill: white;");

        VBox buttonsBox = new VBox(15);
        buttonsBox.setAlignment(Pos.CENTER);

        for (int i = 2; i <= 4; i++) {
            int count = i;
            Button btn = createMenuButton(count + " joueurs");
            btn.setOnAction(e -> showPlayerChoice(stage, count));
            buttonsBox.getChildren().add(btn);
        }

        root.getChildren().addAll(label, buttonsBox);

        StackPane container = new StackPane(root);
        container.setAlignment(Pos.CENTER);
        container.setBackground(getBackgroundImage());

        Scene scene = new Scene(container, 1280, 800);
        setSceneWithShake(stage, scene);
        return scene;
    }

    private static void showPlayerChoice(Stage stage, int numPlayers) {
        VBox layout = new VBox(30);
        layout.setAlignment(Pos.CENTER);

        Label label = new Label("Choisissez qui sera IA (max 1)");
        label.setStyle("-fx-font-size: 24px; -fx-text-fill: white;");

        VBox buttonBox = new VBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        List<Boolean> iaFlags = new ArrayList<>();
        for (int i = 0; i < numPlayers; i++) iaFlags.add(false);

        for (int i = 0; i < numPlayers; i++) {
            int index = i;
            Button toggleBtn = createMenuButton("Joueur " + (i + 1) + " : Humain");
            toggleBtn.setOnAction(e -> {
                boolean isIA = !iaFlags.get(index);
                iaFlags.set(index, isIA);
                toggleBtn.setText("Joueur " + (index + 1) + " : " + (isIA ? "IA" : "Humain"));
            });
            buttonBox.getChildren().add(toggleBtn);
        }

        Button nextButton = createMenuButton("Choisir le terrain");
        nextButton.setOnAction(e -> showTerrainChoice(stage, numPlayers, iaFlags));

        layout.getChildren().addAll(label, buttonBox, nextButton);

        StackPane root = new StackPane(layout);
        root.setAlignment(Pos.CENTER);
        root.setBackground(getBackgroundImage());

        Scene scene = new Scene(root);
        setSceneWithShake(stage, scene);
    }

    private static void showTerrainChoice(Stage stage, int numPlayers, List<Boolean> iaFlags) {
        VBox terrainBox = new VBox(30);
        terrainBox.setAlignment(Pos.CENTER);

        Label label = new Label("Choisissez le terrain");
        label.setStyle("-fx-font-size: 24px; -fx-text-fill: white;");

        Button cityButton = createMenuButton("Ville");
        cityButton.setOnAction(e -> prepareBattle(stage, numPlayers, iaFlags, Plateau::generateCityTerrain));

        Button islandButton = createMenuButton("Île");
        islandButton.setOnAction(e -> prepareBattle(stage, numPlayers, iaFlags, Plateau::generateIsland));

        terrainBox.getChildren().addAll(label, cityButton, islandButton);

        StackPane root = new StackPane(terrainBox);
        root.setBackground(getBackgroundImage());

        Scene scene = new Scene(root);
        setSceneWithShake(stage, scene);
    }

    private static void prepareBattle(Stage stage, int numPlayers, List<Boolean> iaFlags, TerrainGenerator generator) {
        Plateau plateau = new Plateau(20, 20);
        generator.generate(plateau);

        List<Player> players = new ArrayList<>();
        for (int i = 0; i < numPlayers; i++) {
            players.add(new Player("Joueur " + (i + 1), iaFlags.get(i)));
        }

        showArmyChoice(stage, players, plateau);
    }

    private static void showArmyChoice(Stage stage, List<Player> players, Plateau plateau) {
        VBox layout = new VBox(25);
        layout.setAlignment(Pos.CENTER);

        Label label = new Label("Sélectionnez l’armée pour chaque joueur");
        label.setStyle("-fx-font-size: 24px; -fx-text-fill: white;");

        VBox playerSelectors = new VBox(15);
        playerSelectors.setAlignment(Pos.CENTER);

        for (Player player : players) {
            if (player.isAI()) {
                player.setArmyType(ArmyType.ALLEMAGNE);
                continue;
            }

            Label playerLabel = new Label(player.getName() + " : ");
            playerLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");

            HBox buttons = new HBox(10);
            buttons.setAlignment(Pos.CENTER);

            for (ArmyType type : ArmyType.values()) {
                Button btn = createMenuButton(type.name());
                btn.setOnAction(e -> {
                    player.setArmyType(type);
                    btn.setDisable(true);
                    Logger.log(player.getName() + " a choisi " + type.name());
                });
                buttons.getChildren().add(btn);
            }

            VBox box = new VBox(5, playerLabel, buttons);
            box.setAlignment(Pos.CENTER);
            playerSelectors.getChildren().add(box);
        }

        Button launchBtn = createMenuButton("Lancer la partie");
        launchBtn.setOnAction(e -> {
            boolean allChosen = players.stream().filter(p -> !p.isAI()).allMatch(p -> p.getArmyType() != null);
            if (!allChosen) {
                Logger.log("❗ Tous les joueurs humains doivent choisir une armée !");
                return;
            }

            // Ajout du shake juste avant la transition finale
            setSceneWithShake(stage, stage.getScene());
            launchGame(stage, players, plateau);
        });

        layout.getChildren().addAll(label, playerSelectors, launchBtn);

        StackPane root = new StackPane(layout);
        root.setBackground(getBackgroundImage());

        Scene scene = new Scene(root);
        setSceneWithShake(stage, scene);
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
        endTurnButton.setStyle("-fx-font-size: 14px;");
        endTurnButton.setOnAction(ev -> controller.endTurn());

        Button saveButton = new Button("Sauvegarder");
        saveButton.setLayoutX(140);
        saveButton.setLayoutY(20);
        saveButton.setStyle("-fx-font-size: 14px;");
        saveButton.setOnAction(ev -> SaveManager.sauvegarder(gameState, "savegame.ser"));

        Button returnButton = new Button("Retour Menu");
        returnButton.setLayoutX(260);
        returnButton.setLayoutY(20);
        returnButton.setStyle("-fx-font-size: 14px;");
        returnButton.setOnAction(ev -> GameMenu.createMenuScene(stage));

        plateau.getChildren().addAll(endTurnButton, saveButton, returnButton);

        VBox sidePanel = new VBox();
        sidePanel.setStyle("-fx-background-color: #222; -fx-padding: 10;");
        sidePanel.setPrefWidth(300);

        Label playerLabel = new Label("Joueur courant : " + gameState.getCurrentPlayer().getName());
        playerLabel.setStyle("-fx-text-fill: white;");
        Label unitsLabel = new Label("Unités : " + gameState.getCurrentPlayer().getUnits().size());
        unitsLabel.setStyle("-fx-text-fill: white;");

        LogPanel logPanel = new LogPanel();

        sidePanel.getChildren().addAll(playerLabel, unitsLabel, logPanel);
        VBox.setVgrow(logPanel, Priority.ALWAYS);

        HBox mainLayout = new HBox(sidePanel, plateau);
        Scene gameScene2 = new Scene(mainLayout, 1280, 800);
        stage.setScene(gameScene2);
        stage.setTitle("Wargame - Partie");

        controller.playTurn();
    }

    private static void placerUnitesPourJoueur(Player joueur, Plateau plateau, int playerIndex) {
        if (playerIndex >= ZONES_DEPLACEMENT.length) {
            Logger.log("❌ Trop de joueurs pour les zones prédéfinies !");
            return;
        }

        List<Unit> unites = joueur.getUnits();
        int baseRow = ZONES_DEPLACEMENT[playerIndex][0];
        int baseCol = ZONES_DEPLACEMENT[playerIndex][1];
        int endRow = Math.min(baseRow + 5, plateau.getRows());
        int endCol = Math.min(baseCol + 5, plateau.getCols());

        List<HexagonTile> candidates = new ArrayList<>();
        for (int row = baseRow; row < endRow; row++) {
            for (int col = baseCol; col < endCol; col++) {
                HexagonTile tile = plateau.getCase(row, col);
                if (tile != null && tile.getUnit() == null) {
                    TerrainType terrain = tile.getTerrainType();
                    if (terrain == TerrainType.PLAINE || terrain == TerrainType.FORET || terrain == TerrainType.COLLINE) {
                        candidates.add(tile);
                    }
                }
            }
        }

        Collections.shuffle(candidates);
        int unitIndex = 0;
        for (HexagonTile tile : candidates) {
            if (unitIndex >= unites.size()) break;
            plateau.placerUnite(tile.getRow(), tile.getCol(), unites.get(unitIndex++));
        }

        // Si pas toutes les unités placées, chercher ailleurs
        if (unitIndex < unites.size()) {
            Logger.log("⚠️ Recherche hors zone pour " + joueur.getName());
            for (int row = 0; row < plateau.getRows(); row++) {
                for (int col = 0; col < plateau.getCols(); col++) {
                    if (unitIndex >= unites.size()) break;
                    HexagonTile tile = plateau.getCase(row, col);
                    if (tile.getUnit() == null) {
                        TerrainType terrain = tile.getTerrainType();
                        if (terrain == TerrainType.PLAINE || terrain == TerrainType.FORET || terrain == TerrainType.COLLINE) {
                            plateau.placerUnite(row, col, unites.get(unitIndex++));
                        }
                    }
                }
            }
        }

        if (unitIndex < unites.size()) {
            Logger.log("⚠️ Seulement " + unitIndex + " unités placées sur " + unites.size() + " pour " + joueur.getName());
        }
    }

    private static Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-font-size: 18px; -fx-background-color: #444; -fx-text-fill: white;");
        btn.setPrefWidth(250);
        return btn;
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

    private static void setSceneWithShake(Stage stage, Scene scene) {
        stage.setScene(scene);
        stage.setFullScreen(false);
        stage.setWidth(1281);
        stage.setHeight(801);
        stage.setX(stage.getX() + 0.001);
        stage.setY(stage.getY() + 0.001);
    }

    @FunctionalInterface
    private interface TerrainGenerator {
        void generate(Plateau plateau);
    }
}
