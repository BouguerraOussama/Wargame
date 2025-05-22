package tn.isty.wargame.model;

public enum TerrainType {
    PLAINE(1),
    FORET(2),
    MONTAGNE(3),
    COLLINE(2),
    FORTERESSE(1),
    EAU(999);

    private final int moveCost;

    TerrainType(int moveCost) {
        this.moveCost = moveCost;
    }

    public int getMoveCost() {
        return moveCost;
    }
}