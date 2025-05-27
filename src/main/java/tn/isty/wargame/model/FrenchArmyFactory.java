package tn.isty.wargame.model;

import java.util.List;

public class FrenchArmyFactory implements ArmyFactory {
    @Override
    public List<Unit> createArmy(Player owner) {
        return List.of(
                new Unit("grenadier.png", "Grenadier", UnitType.INFANTERIE, 100, 20, 10, 3, 2, 1, owner),
                new Unit("lancier.png", "Lancier", UnitType.CAVALERIE, 80, 25, 5, 4, 2, 1, owner),
                new Unit("canon.png", "Canon", UnitType.ARTILLERIE, 60, 40, 5, 2, 2, 2, owner),
                new Unit("sapeur (2).png", "Sapeur", UnitType.SOUTIEN, 70, 10, 15, 3, 3, 1, owner),
                new Unit("officier.png", "Officier", UnitType.ARCHER, 90, 15, 15, 3, 3, 1, owner)
        );
    }

}
