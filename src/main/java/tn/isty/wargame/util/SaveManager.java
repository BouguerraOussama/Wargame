package tn.isty.wargame.util;

import tn.isty.wargame.model.GameState;
import tn.isty.wargame.model.Plateau;

import java.io.*;

/**
 * Classe utilitaire pour gérer la sauvegarde et le chargement des parties de jeu.
 * Utilise la sérialisation Java pour écrire et lire l'état du jeu dans un fichier.
 */
public class SaveManager {

    //  Fichier dans le dossier du projet
    private static final String DEFAULT_SAVE_FILE = System.getProperty("user.dir") + File.separator + "savegame.ser";
    /**
     * Sauvegarde l'état de jeu dans le fichier par défaut.
     *
     */
    public static void sauvegarder(GameState gameState) {
        sauvegarder(gameState, DEFAULT_SAVE_FILE);
    }
    /**
     * Charge l'état de jeu depuis le fichier par défaut.
     *
     */
    public static GameState charger() {
        return charger(DEFAULT_SAVE_FILE);
    }
    /**
     * Sauvegarde l'état de jeu dans un fichier spécifié.
     */
    public static void sauvegarder(GameState gameState, String filename) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(gameState);
            System.out.println("Partie sauvegardée dans : " + filename);
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde : " + e.getMessage());
        }
    }
    /**
     * Charge l'état de jeu depuis un fichier spécifié.
     * Après chargement, réinitialise les composants JavaFX du plateau et rafraîchit la visibilité.
     * Supprime ensuite le fichier de sauvegarde pour éviter des rechargements multiples.
     */
    
    public static GameState charger(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            System.err.println(" Fichier de sauvegarde introuvable : " + file.getAbsolutePath());
            return null;
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            GameState gameState = (GameState) in.readObject();
            System.out.println(" Partie chargée depuis : " + filename);

            // 🔥 Ajout critique pour corriger l'affichage :
            Plateau plateau = gameState.getBoard();
            if (plateau != null) {
                plateau.afficherTerrain(); //  Recrée les composants JavaFX
                plateau.refreshVisibility();
            }

            if (file.delete()) {
                System.out.println("Fichier de sauvegarde supprimé : " + filename);
            } else {
                System.err.println("Impossible de supprimer le fichier de sauvegarde.");
            }

            return gameState;
        } catch (IOException | ClassNotFoundException e) {
           System.err.println(" Échec du chargement de la partie : " + e.getMessage());
            return null;
        }
    }

    
}