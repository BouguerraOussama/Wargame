package tn.isty.wargame.view;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import tn.isty.wargame.util.UIUtils;

public class TutorialScreen {
    /**
     * Classe affichant l'écran tutoriel pour les débutants.
     *
     * Cet écran présente les règles de base du jeu, les objectifs,
     * ainsi que les contrôles essentiels à connaître.
     *
     */
    public static void show(Stage stage) {
        Label title = new Label("🧠 Aide pour débutants");
        title.setStyle("-fx-font-size: 28px; -fx-text-fill: white;");

        Label rules = new Label("""
                📌 Objectif : Détruire toutes les unités ennemies ou être le dernier joueur debout.

                ▶️ Déplacements :
                - Une unité a un nombre limité de points de mouvement (MV).
                - Les terrains comme montagnes ou eau ralentissent ou bloquent.

                ⚔️ Attaques :
                - Une unité peut attaquer une cible dans sa portée (AR).
                - Les dégâts dépendent des stats et du terrain.

                🛡️ Défense :
                - Les forêts, collines, montagnes offrent des bonus de défense.
                - Plus une unité est loin de l'ennemi, plus elle est à l'abri.

                ❤️ Régénération :
                - Si une unité ne fait rien et n’est pas attaquée, elle récupère 10% de PV.

                👁️ Brouillard :
                - Vous ne voyez que ce que vos unités peuvent observer.
                - Les ennemis dans le brouillard sont invisibles.

                ✅ À chaque tour, vous pouvez : vous déplacer OU attaquer.
                """);

        rules.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
        rules.setWrapText(true);
        rules.setMaxWidth(800);

        Button backButton = UIUtils.createMenuButton("⬅ Retour au menu");
        backButton.setOnAction(e -> UIUtils.showScene(stage, GameMenu.createMenuScene(stage)));

        VBox layout = new VBox(25, title, rules, backButton);
        layout.setAlignment(Pos.CENTER);

        Rectangle overlay = new Rectangle();
        overlay.setFill(Color.rgb(0, 0, 0, 0.7));
        overlay.widthProperty().bind(stage.widthProperty());
        overlay.heightProperty().bind(stage.heightProperty());

        StackPane root = new StackPane(UIUtils.getBackgroundImagePane(), overlay, layout);
        Scene scene = new Scene(root);
        UIUtils.showScene(stage, scene);
    }
}