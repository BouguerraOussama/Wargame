package tn.isty.wargame;

import javafx.application.Application;
import javafx.stage.Stage;
import tn.isty.wargame.view.GameMenu;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Wargame - Menu Principal");
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("");
        primaryStage.setScene(GameMenu.createMenuScene(primaryStage)); // récupère la scène
        primaryStage.show(); //  affiche la fenêtre
    }

    public static void main(String[] args) {
        launch(args);
    }
}
