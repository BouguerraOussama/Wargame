package tn.isty.wargame.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Player implements Serializable {
    private String name;
    private List<Unit> units;
    private boolean isAI;
    private ArmyType armyType; // ✅ pour gérer les armées personnalisées

    public Player(String name, boolean isAI) {
        this.name = name;
        this.isAI = isAI;
        this.units = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<Unit> getUnits() {
        return units;
    }

    public boolean isAI() {
        return isAI;
    }

    public ArmyType getArmyType() {
        return armyType;
    }

    public void setArmyType(ArmyType armyType) { // ✅ méthode à ajouter
        this.armyType = armyType;
    }

    public void addUnit(Unit unit) {
        units.add(unit);
    }

    public void removeUnit(Unit unit) {
        units.remove(unit);
    }

    public boolean hasUnitsAlive() {
        return units.stream().anyMatch(unit -> unit.getCurrentHealth() > 0);
    }

    public void resetUnitsMovement() {
        for (Unit unit : units) {
            unit.resetMovement();
        }
    }
}
