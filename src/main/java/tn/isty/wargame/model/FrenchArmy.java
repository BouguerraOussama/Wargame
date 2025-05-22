package tn.isty.wargame.model;

import java.util.ArrayList;
import java.util.List;

public class FrenchArmy extends Army {

    public FrenchArmy() {
        super("France");
    }

    @Override
    public List<Unit> createUnits(Player player) {
        List<Unit> units = new ArrayList<>();
        units.add(new Unit("F-Inf", "Infanterie", 30, 6, 3, 3, 3, player));
        units.add(new Unit("F-Tank", "Char FT", 35, 8, 4, 2, 2, player));
        units.add(new Unit("F-Art", "Artillerie", 20, 10, 2, 1, 4, player));
        units.add(new Unit("F-Sniper", "Tireur", 25, 9, 1, 2, 4, player));
        units.add(new Unit("F-Officier", "Commandant", 28, 5, 3, 3, 3, player));
        return units;
    }
}
