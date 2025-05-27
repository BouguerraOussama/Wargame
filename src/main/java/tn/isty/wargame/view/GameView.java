package tn.isty.wargame.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tn.isty.wargame.controller.GameController;
import tn.isty.wargame.model.GameState;
import tn.isty.wargame.model.Plateau;
import tn.isty.wargame.util.SaveManager;
import tn.isty.wargame.util.UIUtils;
/**
 * Classe responsable de la création et de la gestion de la vue principale du jeu.
 * Cette classe génère la scène JavaFX qui contient l'interface utilisateur complète
 * du jeu, incluant le plateau de jeu, les contrôles et le panneau de log.
 */
public class GameView {
    /**
     * Crée la scène principale du jeu avec tous ses composants graphiques.
     *
     * Stage stage     La fenêtre principale sur laquelle afficher la scène.
     * GameState gameState L'état courant du jeu contenant les données du plateau et des joueurs.
     * return Une instance de  Scene contenant l'interface complète du jeu.
     */
    public static Scene createGameScene(Stage stage, GameState gameState) {
        GameController controller = new GameController(gameState);
        Plateau plateau = gameState.getBoard();

        Button endTurnButton = UIUtils.createWhiteButton("Fin de tour");
        endTurnButton.setOnAction(ev -> controller.endTurn());

        Button saveButton = UIUtils.createWhiteButton("Sauvegarder");
        saveButton.setOnAction(ev -> SaveManager.sauvegarder(gameState));

        Button returnButton = UIUtils.createWhiteButton("Retour Menu");
        returnButton.setOnAction(ev -> UIUtils.showScene(stage, GameMenu.createMenuScene(stage)));

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

        controller.playTurn();

        return new Scene(mainLayout);
    }
}
