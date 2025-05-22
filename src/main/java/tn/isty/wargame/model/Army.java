package tn.isty.wargame.model;

import java.util.List;

public abstract class Army {
    protected String name;

    public Army(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract List<Unit> createUnits(Player player);
}
