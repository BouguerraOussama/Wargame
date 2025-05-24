package tn.isty.wargame.model;

import java.util.List;

public class FrenchArmyFactory implements ArmyFactory {
    @Override
    public List<Unit> createArmy(Player owner) {
        return List.of(
            new Unit("Grenadier", "infanterie", 100, 20, 10, 3, 2, 1, owner),
            new Unit("Lancier", "cavalerie", 80, 25, 5, 4, 2, 1, owner),
            new Unit("Canon", "artillerie", 60, 40, 5, 2, 2, 2, owner),
            new Unit("Sapeur", "soutien", 70, 10, 15, 3, 3, 1, owner),
            new Unit("Officier", "commandement", 90, 15, 15, 3, 3, 1, owner)
        );
    }
}
