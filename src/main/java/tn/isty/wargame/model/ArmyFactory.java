package tn.isty.wargame.model;

import java.util.List;

/**
 * Interface représentant une fabrique d'armée (pattern Factory).
 * Chaque implémentation de cette interface permet de créer une armée
 * spécifique (ex: française, allemande, britannique) pour un joueur donné.
 */
public interface ArmyFactory {

    /**
     * Crée et retourne une liste d'unités appartenant à une armée personnalisée
     * pour le joueur spécifié.
     *
     * @param player Le joueur auquel appartient l'armée générée.
     * @return Une liste d'unités constituant l'armée du joueur.
     */
    List<Unit> createArmy(Player player);
}
