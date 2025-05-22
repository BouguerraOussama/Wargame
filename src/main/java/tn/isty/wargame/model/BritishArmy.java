package tn.isty.wargame.model;

import java.util.ArrayList;
import java.util.List;

public class BritishArmy extends Army {

    public BritishArmy() {
        super("Royaume-Uni");
    }

    @Override
    public List<Unit> createUnits(Player player) {
        List<Unit> units = new ArrayList<>();
        units.add(new Unit("B-Inf", "Tommies", 30, 6, 3, 3, 4, player));
        units.add(new Unit("B-Tank", "Mark V", 38, 7, 4, 2, 2, player));
        units.add(new Unit("B-Art", "Canon", 20, 10, 2, 1, 5, player));
        units.add(new Unit("B-Sniper", "Fusil de précision", 24, 10, 1, 2, 5, player));
        units.add(new Unit("B-Off", "Major", 28, 5, 3, 3, 3, player));
        return units;
    }
}
