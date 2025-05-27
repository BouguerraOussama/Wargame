package tn.isty.wargame.view;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import tn.isty.wargame.controller.GameController;
import tn.isty.wargame.model.GameState;
import tn.isty.wargame.model.HexagonTile;
import tn.isty.wargame.model.Plateau;
import tn.isty.wargame.model.Player;
import tn.isty.wargame.model.Unit;
import tn.isty.wargame.util.SaveManager;

public class GameMenu {

    public static Scene createMenuScene(Stage stage) {
        Label title = new Label("WARGAME");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 80));

        Button startButton = new Button("Nouvelle Partie");
        Button loadButton = new Button("Charger Partie");
        Button quitButton = new Button("Quitter");

        startButton.setPrefWidth(300);
        loadButton.setPrefWidth(300);
        quitButton.setPrefWidth(300);

        startButton.setStyle("-fx-font-size: 24px; -fx-background-color: #444; -fx-text-fill: white;");
        loadButton.setStyle("-fx-font-size: 24px; -fx-background-color: #444; -fx-text-fill: white;");
        quitButton.setStyle("-fx-font-size: 24px; -fx-background-color: #222; -fx-text-fill: white;");

        startButton.setOnAction(e -> GameSetup.createSetupScene(stage));

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

                Button endTurnButton = new Button("Fin de tour");
                endTurnButton.setLayoutX(20);
                endTurnButton.setLayoutY(20);
                endTurnButton.setStyle("-fx-font-size: 16px;");
                endTurnButton.setOnAction(ev -> controller.endTurn());

                Button saveButton = new Button("Sauvegarder");
                saveButton.setLayoutX(140);
                saveButton.setLayoutY(20);
                saveButton.setStyle("-fx-font-size: 16px;");
                saveButton.setOnAction(ev -> SaveManager.sauvegarder(loadedState, "savegame.ser"));

                Button returnButton = new Button("Retour Menu");
                returnButton.setLayoutX(280);
                returnButton.setLayoutY(20);
                returnButton.setStyle("-fx-font-size: 16px;");
                returnButton.setOnAction(ev -> createMenuScene(stage));

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

        VBox menuBox = new VBox(30, title, startButton, loadButton, quitButton);
        menuBox.setAlignment(Pos.CENTER);

        StackPane root = new StackPane(menuBox);
        root.setBackground(new Background(new BackgroundImage(
                new Image(GameMenu.class.getResource("/images/war_background.jpg").toExternalForm(),
                        -1, -1, true, true),
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(100, 100, true, true, true, false)
        )));

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setWidth(1280);
        stage.setHeight(720);
        stage.setFullScreen(false);
        stage.show();

        return scene;
    }
}
