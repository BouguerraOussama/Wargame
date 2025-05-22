package tn.isty.wargame.model;

import java.io.Serializable;

public class Unit implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final String type;           // ex: "archer", "infanterie"
    private final int maxHealth;
    private final int attack;
    private final int defense;
    private final int maxMovement;
    private final int visionRange;

    private int currentHealth;
    private int currentMovement;

    private Player owner;                // le joueur à qui appartient l’unité
    private HexagonTile position;        // case actuelle sur le plateau

    private boolean wasAttackedThisTurn = false;
    private boolean hasActed = false;

    public Unit(String name, String type, int maxHealth, int attack, int defense, int maxMovement, int visionRange, Player owner) {
        this.name = name;
        this.type = type;
        this.maxHealth = maxHealth;
        this.attack = attack;
        this.defense = defense;
        this.maxMovement = maxMovement;
        this.visionRange = visionRange;

        this.currentHealth = maxHealth;
        this.currentMovement = maxMovement;
        this.owner = owner;
    }

    // Getters
    public String getName() { return name; }
    public String getType() { return type; }
    public int getCurrentHealth() { return currentHealth; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getCurrentMovement() { return currentMovement; }
    public int getMaxMovement() { return maxMovement; }
    public int getVisionRange() { return visionRange; }
    public Player getOwner() { return owner; }
    public HexagonTile getPosition() { return position; }

    // Setters
    public void setPosition(HexagonTile position) {
        this.position = position;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public void setWasAttackedThisTurn(boolean wasAttackedThisTurn) {
        this.wasAttackedThisTurn = wasAttackedThisTurn;
    }
    public void setCurrentHealth(int currentHealth) {
        this.currentHealth = currentHealth;
    }
    public void setCurrentMovement(int currentMovement) {
        this.currentMovement = currentMovement;
    }
    public void resetMovement() {
        this.currentMovement = maxMovement;
    }
    


    // Méthode pour déplacer l’unité en consommant du mouvement
    public boolean moveTo(HexagonTile newPosition, int movementCost) {
        if (movementCost <= currentMovement && movementCost > 0) {
            this.position = newPosition;
            this.currentMovement -= movementCost;
            this.hasActed = true;
            return true;
        }
        return false; // mouvement impossible
    }

    // Réinitialiser les points de mouvement et flags en début de tour
    public void startTurn() {
        this.currentMovement = maxMovement;
        this.wasAttackedThisTurn = false;
        this.hasActed = false;
    }

    public boolean wasAttackedThisTurn() {
        return wasAttackedThisTurn;
    }

    public boolean hasActed() {
        return hasActed;
    }

    public void setHasActed(boolean hasActed) {
        this.hasActed = hasActed;
    }

    // Appliquer des dégâts
    public void receiveDamage(int amount) {
        if (amount < 0) return; // pas de soins ici
        this.currentHealth -= amount;
        if (this.currentHealth < 0) this.currentHealth = 0;
        this.wasAttackedThisTurn = true;
    }

    // Récupérer des PV (10% maxHealth) si l’unité n’a pas été attaquée ce tour
    public void recoverHealthIfIdle() {
        if (!wasAttackedThisTurn && currentHealth > 0 && currentHealth < maxHealth) {
            int recovered = (int) Math.ceil(maxHealth * 0.10);
            currentHealth = Math.min(maxHealth, currentHealth + recovered);
        }
    }

    public boolean isAlive() {
        return currentHealth > 0;
    }

    @Override
    public String toString() {
        return name + " [" + type + "] HP:" + currentHealth + " MV:" + currentMovement;
    }
}
