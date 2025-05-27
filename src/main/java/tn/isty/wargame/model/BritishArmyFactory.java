package tn.isty.wargame.model;

import java.util.List;

public class BritishArmyFactory implements ArmyFactory {
    @Override
    public List<Unit> createArmy(Player owner) {
        return List.of(
            new Unit("Rifleman", UnitType.INFANTERIE, 100, 18, 11, 3, 2, 1, owner),
            new Unit("Cavalry", UnitType.CAVALERIE, 80, 24, 6, 4, 2, 1, owner),
            new Unit("Tankette", UnitType.ARTILLERIE, 70, 35, 10, 2, 2, 2, owner),
            new Unit("Medic", UnitType.SOUTIEN, 60, 8, 18, 3, 3, 1, owner),
            new Unit("Sniper", UnitType.ARCHER, 50, 50, 5, 3, 3, 3, owner)
        );
    }
}
