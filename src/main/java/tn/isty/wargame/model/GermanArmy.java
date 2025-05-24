package tn.isty.wargame.model;

import java.util.ArrayList;
import java.util.List;

public class GermanArmy extends Army {

    public GermanArmy() {
        super("Allemagne");
    }

    @Override
    public List<Unit> createUnits(Player player) {
        List<Unit> units = new ArrayList<>();
        units.add(new Unit("G-Inf", "Infanterie lourde", 35, 6, 4, 2, 2, 5, player));
        units.add(new Unit("G-Tank", "Panzer I", 40, 9, 5, 2, 2, 4, player));
        units.add(new Unit("G-Art", "Obusier", 22, 11, 3, 1, 4, 3, player));
        units.add(new Unit("G-Flamme", "Lance-flammes", 28, 7, 2, 2, 3, 3, player));
        units.add(new Unit("G-Off", "Officier", 30, 6, 4, 3, 3, 4, player));
        return units;
    }
}
