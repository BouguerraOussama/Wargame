package tn.isty.wargame.util;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class UIUtils {

    public static Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-font-size: 18px; -fx-background-color: #444; -fx-text-fill: white;");
        btn.setPrefWidth(250);
        return btn;
    }

    public static void setSceneWithShake(Stage stage, Scene scene) {
        stage.setScene(scene);
        stage.setFullScreen(false);
        stage.setWidth(1281); // 1 pixel + shake
        stage.setHeight(801);
        stage.setX(stage.getX() + 0.001);
        stage.setY(stage.getY() + 0.001);
    }

    public static Background getBackgroundImage() {
        return new Background(new BackgroundImage(
                new Image(UIUtils.class.getResource("/images/war_background.jpg").toExternalForm(),
                        -1, -1, true, true),
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(100, 100, true, true, true, false)
        ));
    }

    public static Button createWhiteButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-font-size: 16px; -fx-background-color: white; -fx-text-fill: black; -fx-border-radius: 5px; -fx-padding: 10px;");
        btn.setPrefWidth(160);
        return btn;
    }

}
