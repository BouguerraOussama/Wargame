package tn.isty.wargame.view;
/**
 * Classe utilitaire pour l'enregistrement des messages de log.
 *
 * Elle permet d'envoyer des messages au panneau de journalisation (LogPanel).
 */
public class Logger {
/**
 * Enregistre un message dans le panneau de log.
 */
    public static void log(String message) {
        LogPanel.append(message);
    }
}
