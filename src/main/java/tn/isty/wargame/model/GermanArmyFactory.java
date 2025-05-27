package tn.isty.wargame.model;

import java.util.List;

public class GermanArmyFactory implements ArmyFactory {
    @Override
    public List<Unit> createArmy(Player owner) {
        return List.of(
            new Unit("Sturmtruppen", UnitType.INFANTERIE, 100, 22, 12, 3, 2, 1, owner),
            new Unit("Uhlan", UnitType.CAVALERIE, 85, 27, 5, 4, 2, 1, owner),
            new Unit("Obusier", UnitType.ARTILLERIE, 55, 45, 5, 2, 2, 2, owner),
            new Unit("Sapeur", UnitType.SOUTIEN, 75, 10, 15, 3, 3, 1, owner),
            new Unit("Officier", UnitType.ARCHER, 95, 15, 15, 3, 3, 1, owner)
        );
    }
}