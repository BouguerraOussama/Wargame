package tn.isty.wargame.model;

import java.util.List;

public class GermanArmyFactory implements ArmyFactory {
    @Override
    public List<Unit> createArmy(Player owner) {
        return List.of(
            new Unit("Sturmtruppen", "infanterie", 100, 22, 12, 3, 2, 1, owner),
            new Unit("Uhlan", "cavalerie", 85, 27, 5, 4, 2, 1, owner),
            new Unit("Obusier", "artillerie", 55, 45, 5, 2, 2, 2, owner),
            new Unit("Sapeur", "soutien", 75, 10, 15, 3, 3, 1, owner),
            new Unit("Officier", "commandement", 95, 15, 15, 3, 3, 1, owner)
        );
    }
}
