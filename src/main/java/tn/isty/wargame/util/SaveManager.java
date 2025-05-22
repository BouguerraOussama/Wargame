package tn.isty.wargame.util;

import tn.isty.wargame.model.GameState;
import java.io.*;

public class SaveManager {

    public static void sauvegarder(GameState gameState, String chemin) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(chemin))) {
            oos.writeObject(gameState);
            System.out.println("💾 Partie sauvegardée avec succès !");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static GameState charger(String chemin) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(chemin))) {
            GameState gameState = (GameState) ois.readObject();
            System.out.println("📂 Partie chargée avec succès !");
            return gameState;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
