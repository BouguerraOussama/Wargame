package tn.isty.wargame.model;

import java.util.List;

public class BritishArmyFactory implements ArmyFactory {
    @Override
    public List<Unit> createArmy(Player owner) {
        return List.of(
            new Unit("Rifleman", "infanterie", 100, 18, 11, 3, 2, 1, owner),
            new Unit("Cavalry", "cavalerie", 80, 24, 6, 4, 2, 1, owner),
            new Unit("Tankette", "artillerie", 70, 35, 10, 2, 2, 2, owner),
            new Unit("Medic", "soutien", 60, 8, 18, 3, 3, 1, owner),
            new Unit("Sniper", "commandement", 50, 50, 5, 3, 3, 3, owner)
        );
    }
}
