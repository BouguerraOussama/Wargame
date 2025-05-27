package tn.isty.wargame.model;

import java.util.List;

/**
 * Fabrique concrète pour créer une armée française dans le jeu Wargame.
 * Implémente l'interface ArmyFactory.
 * 
 * Cette armée est équilibrée avec un bon mélange d'infanterie, cavalerie,
 * artillerie, soutien et commandement. Chaque unité possède des caractéristiques
 * spécifiques correspondant à son rôle sur le champ de bataille.
 */
public class FrenchArmyFactory implements ArmyFactory {

    /**
     * Crée une liste de 5 unités spécifiques à l’armée française.
     *
     * @param owner Le joueur auquel appartiennent les unités créées.
     * @return Une liste d'objets Unit représentant l’armée française.
     */
    @Override
    public List<Unit> createArmy(Player owner) {
        return List.of(
                // Grenadier – unité d’infanterie de base avec stats équilibrées
                new Unit("grenadier.png", "Grenadier", UnitType.INFANTERIE, 100, 20, 10, 3, 2, 1, owner),

                // Lancier – cavalerie rapide, bonne mobilité, dégâts moyens
                new Unit("lancier.png", "Lancier", UnitType.CAVALERIE, 80, 25, 5, 4, 2, 1, owner),

                // Canon – artillerie puissante mais peu résistante et lente
                new Unit("canon.png", "Canon", UnitType.ARTILLERIE, 60, 40, 5, 2, 2, 2, owner),

                // Sapeur – unité de soutien, bonne capacité de soin
                new Unit("sapeur (2).png", "Sapeur", UnitType.SOUTIEN, 70, 10, 15, 3, 3, 1, owner),

                // Officier – unité de commandement, bon moral, équilibrée en stats
                new Unit("officier.png", "Officier", UnitType.ARCHER, 90, 15, 15, 3, 3, 1, owner)
        );
    }
}
