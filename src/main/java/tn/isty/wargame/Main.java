package tn.isty.wargame;

import javafx.application.Application;
import javafx.stage.Stage;
import tn.isty.wargame.view.GameMenu;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        GameMenu.createMenuScene(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
