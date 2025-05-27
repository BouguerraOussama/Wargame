package tn.isty.wargame.model;

import java.util.List;

/**
 * Fabrique concrète pour créer une armée britannique.
 * Cette classe implémente l'interface {@link ArmyFactory} et retourne
 * une liste d'unités typiques de l’armée du Royaume-Uni.
 *
 * Chaque unité possède ses propres caractéristiques :
 * - Points de vie (PV)
 * - Attaque
 * - Défense
 * - Portée
 * - Mouvement
 * - Vision
 *
 * Les unités sont affectées au joueur passé en paramètre.
 */
public class BritishArmyFactory implements ArmyFactory {

    /**
     * Crée la liste des unités pour un joueur de l’armée britannique.
     *
     * @param owner Le joueur auquel appartiennent les unités.
     * @return Une liste de 5 unités spécifiques à l’armée britannique.
     */
    @Override
    public List<Unit> createArmy(Player owner) {
        return List.of(
                // Soldat d'infanterie – unité de base équilibrée
                new Unit("rifleman.png", "Rifleman", UnitType.INFANTERIE,
                        100, 18, 11, 3, 2, 1, owner),

                // Cavalerie – mobile, rapide mais plus vulnérable
                new Unit("cavalry.png", "Cavalry", UnitType.CAVALERIE,
                        80, 24, 6, 4, 2, 1, owner),

                // Tankette – unité blindée avec fort potentiel offensif
                new Unit("tankette.png", "Tankette", UnitType.ARTILLERIE,
                        70, 35, 10, 2, 2, 2, owner),

                // Médecin – soutien avec faible attaque mais forte défense et soin
                new Unit("medic.png", "Medic", UnitType.SOUTIEN,
                        60, 8, 18, 3, 3, 1, owner),

                // Sniper – attaque à longue distance, mais très fragile
                new Unit("sniper.png", "Sniper", UnitType.ARCHER,
                        50, 50, 5, 3, 3, 3, owner)
        );
    }
}
