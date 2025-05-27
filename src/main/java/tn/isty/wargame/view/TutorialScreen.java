package tn.isty.wargame.view;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.isty.wargame.util.UIUtils;

public class TutorialScreen {

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
        rules.setMaxWidth(700);

        Button backButton = UIUtils.createMenuButton("⬅ Retour au menu");
        backButton.setOnAction(e -> {
            Scene menuScene = GameMenu.createMenuScene(stage);
            UIUtils.setSceneWithShake(stage, menuScene);
        });

        VBox layout = new VBox(25, title, rules, backButton);
        layout.setAlignment(Pos.CENTER);

        StackPane root = new StackPane(layout);
        root.setBackground(UIUtils.getBackgroundImage());

        Scene scene = new Scene(root, 1280, 800);
        UIUtils.setSceneWithShake(stage, scene);
    }
}
