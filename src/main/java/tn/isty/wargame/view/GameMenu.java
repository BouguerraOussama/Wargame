package tn.isty.wargame.view;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import tn.isty.wargame.controller.GameController;
import tn.isty.wargame.model.*;
import tn.isty.wargame.util.SaveManager;
import tn.isty.wargame.util.UIUtils;

public class GameMenu {

    public static Scene createMenuScene(Stage stage) {
        Label title = new Label("WARGAME");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 80));

        Button startButton = UIUtils.createMenuButton("Nouvelle Partie");
        Button loadButton = UIUtils.createMenuButton("Charger Partie");
        Button helpButton = UIUtils.createMenuButton("Aide / Tutoriel");
        Button quitButton = UIUtils.createMenuButton("Quitter");

        startButton.setOnAction(e -> GameSetup.createSetupScene(stage));
        helpButton.setOnAction(e -> TutorialScreen.show(stage));

        loadButton.setOnAction(e -> {
            GameState loadedState = SaveManager.charger("savegame.ser");

            if (loadedState != null) {
                Plateau oldPlateau = loadedState.getBoard();
                Plateau newPlateau = new Plateau(oldPlateau.getRows(), oldPlateau.getCols());

                for (Player player : loadedState.getAllPlayers()) {
                    for (Unit unit : player.getUnits()) {
                        int row = unit.getPosition().getRow();
                        int col = unit.getPosition().getCol();
                        HexagonTile newTile = newPlateau.getCase(row, col);
                        newTile.setUnit(unit);
                        unit.setPosition(newTile);
                    }
                }

                loadedState.setBoard(newPlateau);
                HexagonTile.setSharedGameState(loadedState);
                newPlateau.refreshVisibility();

                GameController controller = new GameController(loadedState);

                Button endTurnButton = UIUtils.createMenuButton("Fin de tour");
                endTurnButton.setLayoutX(20);
                endTurnButton.setLayoutY(20);
                endTurnButton.setOnAction(ev -> controller.endTurn());

                Button saveButton = UIUtils.createMenuButton("Sauvegarder");
                saveButton.setLayoutX(140);
                saveButton.setLayoutY(20);
                saveButton.setOnAction(ev -> SaveManager.sauvegarder(loadedState, "savegame.ser"));

                Button returnButton = UIUtils.createMenuButton("Retour Menu");
                returnButton.setLayoutX(280);
                returnButton.setLayoutY(20);
                returnButton.setOnAction(ev -> UIUtils.setSceneWithShake(stage, createMenuScene(stage)));

                newPlateau.getChildren().addAll(endTurnButton, saveButton, returnButton);

                VBox sidePanel = new VBox(10);
                sidePanel.setStyle("-fx-background-color: #222; -fx-padding: 10;");
                sidePanel.setPrefWidth(200);
                Label playerLabel = new Label("Joueur courant : " + loadedState.getCurrentPlayer().getName());
                playerLabel.setStyle("-fx-text-fill: white;");
                sidePanel.getChildren().add(playerLabel);

                HBox mainLayout = new HBox(sidePanel, newPlateau);
                Scene gameScene = new Scene(mainLayout, 1280, 800);

                stage.setScene(gameScene);
                stage.setTitle("Wargame - Partie chargée");
                controller.playTurn();
            } else {
                Logger.log("❌ Échec du chargement de la partie.");
            }
        });

        quitButton.setOnAction(e -> stage.close());

        VBox menuBox = new VBox(30, title, startButton, loadButton, helpButton, quitButton);
        menuBox.setAlignment(Pos.CENTER);

        StackPane root = new StackPane(menuBox);
        root.setBackground(UIUtils.getBackgroundImage());

        Scene scene = new Scene(root, 1280, 800);
        UIUtils.setSceneWithShake(stage, scene);
        return scene;
    }
}