package tn.isty.wargame.model;

import java.util.List;

/**
 * Fabrique représentant l’armée allemande.
 * Elle permet de générer une armée composée de 5 unités spécifiques à l’Allemagne.
 */
public class GermanArmyFactory implements ArmyFactory {

    /**
     * Crée une armée allemande composée de 5 unités types :
     * Infanterie, Cavalerie, Artillerie, Soutien, Archer.
     *
     * @param owner Le joueur auquel les unités appartiendront.
     * @return Une liste d’unités configurées pour l’armée allemande.
     */
    @Override
    public List<Unit> createArmy(Player owner) {
        return List.of(
                // Unité d'infanterie : équilibrée, résistante et puissante au corps-à-corps
                new Unit("sturmtruppen.png", "Sturmtruppen", UnitType.INFANTERIE,
                        100, 22, 12, 3, 2, 1, owner),

                // Cavalerie rapide, forte en attaque mais peu défensive
                new Unit("uhlan.png", "Uhlan", UnitType.CAVALERIE,
                        85, 27, 5, 4, 2, 1, owner),

                // Artillerie puissante à distance, faible en points de vie
                new Unit("obusier.png", "Obusier", UnitType.ARTILLERIE,
                        55, 45, 5, 2, 2, 2, owner),

                // Soutien spécialisé dans les réparations ou soins, utilitaire
                new Unit("sapeur.png", "Sapeur", UnitType.SOUTIEN,
                        75, 10, 15, 3, 3, 1, owner),

                // Unité de commandement, moyenne en attaque/défense, portée élevée
                new Unit("officier.png", "Officier", UnitType.ARCHER,
                        95, 15, 15, 3, 3, 1, owner)
        );
    }
}
