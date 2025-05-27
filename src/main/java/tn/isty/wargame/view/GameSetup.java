package tn.isty.wargame.view;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.isty.wargame.controller.GameController;
import tn.isty.wargame.model.*;
import tn.isty.wargame.util.SaveManager;
import tn.isty.wargame.util.UIUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Classe responsable de la création des écrans d'installation et de configuration du jeu.
 * Permet de choisir le nombre de joueurs, les joueurs IA/humains, le terrain,
 * l'armée de chaque joueur et lance la partie.
 */
public class GameSetup {
    /**
     * Zones prédéfinies de placement initial des unités selon l'indice du joueur.
     * Chaque zone est un couple [ligne, colonne].
     */
    private static final int[][] ZONES_DEPLACEMENT = {
        {0, 0}, {0, 10}, {10, 0}, {10, 10}
    };

    /**
     * Crée et affiche la scène initiale où l'on choisit le nombre de joueurs.
     *
     * Stage stage La fenêtre principale JavaFX.
     * return La scène affichée.
     */
    public static Scene createSetupScene(Stage stage) {
        VBox root = new VBox(30);
        root.setAlignment(Pos.CENTER);

        Label label = new Label("Choisissez le nombre de joueurs");
        label.setStyle("-fx-font-size: 24px; -fx-text-fill: white;");

        VBox buttonsBox = new VBox(15);
        buttonsBox.setAlignment(Pos.CENTER);

        for (int i = 2; i <= 4; i++) {
            int count = i;
            Button btn = UIUtils.createMenuButton(count + " joueurs");
            btn.setOnAction(e -> showPlayerChoice(stage, count));
            buttonsBox.getChildren().add(btn);
        }

        root.getChildren().addAll(label, buttonsBox);
        Scene scene = new Scene(UIUtils.wrapWithBackground(root));
        UIUtils.showScene(stage, scene);
        return scene;
    }
    /**
     * Affiche l'écran permettant de choisir quels joueurs sont IA ou humains.
     *
     *  stage      La fenêtre principale JavaFX.
     *  numPlayers Nombre total de joueurs.
     */
    private static void showPlayerChoice(Stage stage, int numPlayers) {
        VBox layout = new VBox(30);
        layout.setAlignment(Pos.CENTER);

        Label label = new Label("Choisissez qui sera IA (max 1)");
        label.setStyle("-fx-font-size: 24px; -fx-text-fill: white;");

        VBox buttonBox = new VBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        List<Boolean> iaFlags = new ArrayList<>(Collections.nCopies(numPlayers, false));

        for (int i = 0; i < numPlayers; i++) {
            int index = i;
            Button toggleBtn = UIUtils.createMenuButton("Joueur " + (i + 1) + " : Humain");
            toggleBtn.setOnAction(e -> {
                boolean isIA = !iaFlags.get(index);
                iaFlags.set(index, isIA);
                toggleBtn.setText("Joueur " + (index + 1) + " : " + (isIA ? "IA" : "Humain"));
            });
            buttonBox.getChildren().add(toggleBtn);
        }

        Button nextButton = UIUtils.createMenuButton("Choisir le terrain");
        nextButton.setOnAction(e -> showTerrainChoice(stage, numPlayers, iaFlags));

        layout.getChildren().addAll(label, buttonBox, nextButton);

        Scene scene = new Scene(UIUtils.wrapWithBackground(layout));
        UIUtils.showScene(stage, scene);
    }
    /**
     * Affiche l'écran de sélection du terrain de jeu.
     *
     *  stage      La fenêtre principale JavaFX.
     *  numPlayers Nombre total de joueurs.
     *  iaFlags    Liste des indicateurs IA/humains pour chaque joueur.
     */
    private static void showTerrainChoice(Stage stage, int numPlayers, List<Boolean> iaFlags) {
        VBox terrainBox = new VBox(30);
        terrainBox.setAlignment(Pos.CENTER);

        Label label = new Label("Choisissez le terrain");
        label.setStyle("-fx-font-size: 24px; -fx-text-fill: white;");

        Button cityButton = UIUtils.createMenuButton("Ville");
        cityButton.setOnAction(e -> prepareBattle(stage, numPlayers, iaFlags, Plateau::generateCityTerrain));

        Button islandButton = UIUtils.createMenuButton("Île");
        islandButton.setOnAction(e -> prepareBattle(stage, numPlayers, iaFlags, Plateau::generateIsland));

        terrainBox.getChildren().addAll(label, cityButton, islandButton);

        Scene scene = new Scene(UIUtils.wrapWithBackground(terrainBox));
        UIUtils.showScene(stage, scene);
    }
    /**
     * Prépare le plateau de jeu selon le terrain choisi et crée les joueurs.
     *
     *  stage      La fenêtre principale JavaFX.
     *  numPlayers Nombre total de joueurs.
     *  iaFlags    Liste des indicateurs IA/humains pour chaque joueur.
     *  generator  Générateur de terrain à appliquer.
     */
    private static void prepareBattle(Stage stage, int numPlayers, List<Boolean> iaFlags, TerrainGenerator generator) {
        Plateau plateau = new Plateau(20, 20);
        generator.generate(plateau);

        List<Player> players = new ArrayList<>();
        for (int i = 0; i < numPlayers; i++) {
            players.add(new Player("Joueur " + (i + 1), iaFlags.get(i)));
        }

        showArmyChoice(stage, players, plateau);
    }
    /**
     * Affiche la scène de sélection d'armée pour chaque joueur humain.
     *
     *  stage   La fenêtre principale JavaFX.
     *  players Liste des joueurs créés.
     *  plateau Le plateau de jeu initialisé.
     */
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
                Button btn = UIUtils.createMenuButton(type.name());
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

        Button launchBtn = UIUtils.createMenuButton("Lancer la partie");
        launchBtn.setOnAction(e -> {
            boolean allChosen = players.stream().filter(p -> !p.isAI()).allMatch(p -> p.getArmyType() != null);
            if (!allChosen) {
                Logger.log("❗ Tous les joueurs humains doivent choisir une armée !");
                return;
            }

            showLoadingScreen(stage, () -> launchGame(stage, players, plateau));
        });

        layout.getChildren().addAll(label, playerSelectors, launchBtn);
        Scene scene = new Scene(UIUtils.wrapWithBackground(layout));
        UIUtils.showScene(stage, scene);
    }
    /**
     * Affiche un écran de chargement animé avant de lancer la partie.
     *
     *  stage        La fenêtre principale JavaFX.
     *  afterLoading Action à exécuter après la fin du chargement.
     */
    private static void showLoadingScreen(Stage stage, Runnable afterLoading) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: black;");

        Image gif = new Image(GameSetup.class.getResource("/images/loading.gif").toExternalForm());
        ImageView loadingGif = new ImageView(gif);
        loadingGif.setFitWidth(100);
        loadingGif.setPreserveRatio(true);

        Label loadingLabel = new Label("Chargement en cours...");
        loadingLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");

        root.getChildren().addAll(loadingGif, loadingLabel);
        Scene loadingScene = new Scene(root);
        UIUtils.showScene(stage, loadingScene);

        PauseTransition pause = new PauseTransition(Duration.seconds(5));
        pause.setOnFinished(e -> afterLoading.run());
        pause.play();
    }
    /**
     * Lance la partie avec les joueurs, le plateau et l'interface de jeu.
     *
     *  stage   La fenêtre principale JavaFX.
     *  players Liste des joueurs configurés.
     *  plateau Le plateau de jeu initialisé.
     */
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

        Button endTurnButton = UIUtils.createWhiteButton("Fin de tour");
        endTurnButton.setOnAction(ev -> controller.endTurn());

        Button saveButton = UIUtils.createWhiteButton("Sauvegarder");
        saveButton.setOnAction(ev -> SaveManager.sauvegarder(gameState, "savegame.ser"));

        Button returnButton = UIUtils.createWhiteButton("Retour Menu");
        returnButton.setOnAction(ev -> {
            Scene menuScene = GameMenu.createMenuScene(stage);
            UIUtils.showScene(stage, menuScene);
        });

        HBox buttonBar = new HBox(15, endTurnButton, saveButton, returnButton);
        buttonBar.setAlignment(Pos.CENTER_LEFT);
        buttonBar.setStyle("-fx-padding: 10px;");

        VBox sidePanel = new VBox(15);
        sidePanel.setStyle("-fx-background-color: #222; -fx-padding: 10;");
        sidePanel.setPrefWidth(400);

        Label playerLabel = new Label("Joueur courant : " + gameState.getCurrentPlayer().getName());
        playerLabel.setStyle("-fx-text-fill: white;");

        Label unitsLabel = new Label("Unités : " + gameState.getCurrentPlayer().getUnits().size());
        unitsLabel.setStyle("-fx-text-fill: white;");

        LogPanel logPanel = new LogPanel();
        VBox.setVgrow(logPanel, Priority.ALWAYS);

        sidePanel.getChildren().addAll(playerLabel, unitsLabel, buttonBar, logPanel);

        plateau.setTranslateX(30);
        plateau.setTranslateY(-80);

        HBox mainLayout = new HBox(10, sidePanel, plateau);
        mainLayout.setPadding(new Insets(10));

        Scene gameScene = new Scene(mainLayout);
        UIUtils.showScene(stage, gameScene);
        stage.setTitle("Wargame - Partie");
        controller.playTurn();
    }
    /**
     * Place les unités d'un joueur dans sa zone de départ sur le plateau.
     *
     *  joueur      Le joueur dont on place les unités.
     *  plateau     Le plateau de jeu.
     *  playerIndex L'indice du joueur (définit la zone).
     */
    private static void placerUnitesPourJoueur(Player joueur, Plateau plateau, int playerIndex) {
        if (playerIndex >= ZONES_DEPLACEMENT.length) {
            Logger.log(" Trop de joueurs pour les zones prédéfinies !");
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

        if (unitIndex < unites.size()) {
            Logger.log(" Recherche hors zone pour " + joueur.getName());
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
            Logger.log(" Seulement " + unitIndex + " unités placées sur " + unites.size() + " pour " + joueur.getName());
        }
    }
    /**
     * Interface fonctionnelle pour générer un terrain sur un plateau.
     */
    @FunctionalInterface
    private interface TerrainGenerator {
        void generate(Plateau plateau);
    }
}
