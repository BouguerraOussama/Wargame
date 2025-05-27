package tn.isty.wargame.model;

import java.io.Serializable;

/**
 * Représente une unité dans le jeu de stratégie.
 * Chaque unité possède des caractéristiques (PV, attaque, défense, mouvement, portée),
 * un propriétaire (joueur), une position sur la carte et un comportement de tour.
 */
public class Unit implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Nom du fichier image représentant le sprite de l’unité. */
    private final String spriteFilename;

    /** Nom personnalisé de l’unité (ex. : "Infanterie légère"). */
    private final String name;

    /** Type d’unité (infanterie, cavalerie, etc.). */
    private final UnitType type;

    /** Valeurs statistiques de base. */
    private final int maxHealth, attack, defense, maxMovement, visionRange, attackRange;

    /** Valeurs dynamiques au cours du jeu. */
    private int currentHealth, currentMovement;

    /** Joueur possédant l’unité. */
    private Player owner;

    /** Position actuelle de l’unité sur la carte. */
    private HexagonTile position;

    /** Indique si l’unité a été attaquée ce tour. */
    private boolean wasAttackedThisTurn = false;

    /** Indique si l’unité a déjà agi ce tour (mouvement ou attaque). */
    private boolean hasActed = false;

    /**
     * Constructeur de l’unité avec toutes ses caractéristiques.
     *
     * @param spriteFilename Nom du fichier du sprite
     * @param name Nom de l’unité
     * @param type Type d’unité
     * @param maxHealth Points de vie maximum
     * @param attack Puissance d’attaque
     * @param defense Valeur de défense
     * @param maxMovement Points de déplacement max par tour
     * @param visionRange Rayon de vision
     * @param attackRange Portée d’attaque
     * @param owner Joueur propriétaire
     */
    public Unit(String spriteFilename, String name, UnitType type, int maxHealth, int attack, int defense,
                int maxMovement, int visionRange, int attackRange, Player owner) {
        this.spriteFilename = spriteFilename;
        this.name = name;
        this.type = type;
        this.maxHealth = maxHealth;
        this.attack = attack;
        this.defense = defense;
        this.maxMovement = maxMovement;
        this.visionRange = visionRange;
        this.attackRange = attackRange;

        this.currentHealth = maxHealth;
        this.currentMovement = maxMovement;
        this.owner = owner;
    }

    // === Getters ===

    public String getName() { return name; }

    public UnitType getUnitType() { return type; }

    public int getCurrentHealth() { return currentHealth; }

    public int getAttack() { return attack; }

    public int getDefense() { return defense; }

    public int getCurrentMovement() { return currentMovement; }

    public int getMaxMovement() { return maxMovement; }

    public int getVisionRange() { return visionRange; }

    public int getAttackRange() { return attackRange; }

    public Player getOwner() { return owner; }

    public HexagonTile getPosition() { return position; }

    public String getSpriteFilename() { return spriteFilename; }

    public boolean wasAttackedThisTurn() { return wasAttackedThisTurn; }

    public boolean hasActed() { return hasActed; }

    // === Setters ===

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

    public void setHasActed(boolean hasActed) {
        this.hasActed = hasActed;
    }

    // === Méthodes de jeu ===

    /**
     * Réinitialise les points de mouvement au début du tour.
     */
    public void resetMovement() {
        this.currentMovement = maxMovement;
    }

    /**
     * Tente de déplacer l’unité vers une nouvelle case.
     *
     * @param newPosition Nouvelle position souhaitée
     * @param movementCost Coût en points de mouvement
     * @return true si le déplacement a réussi
     */
    public boolean moveTo(HexagonTile newPosition, int movementCost) {
        if (movementCost <= currentMovement && movementCost > 0) {
            this.position = newPosition;
            this.currentMovement -= movementCost;
            this.hasActed = true;
            return true;
        }
        return false;
    }

    /**
     * Réinitialise les paramètres dynamiques en début de tour.
     */
    public void startTurn() {
        this.currentMovement = maxMovement;
        this.wasAttackedThisTurn = false;
        this.hasActed = false;
    }

    /**
     * Applique des dégâts à l’unité.
     *
     * @param amount Nombre de points de vie à retirer
     */
    public void receiveDamage(int amount) {
        if (amount < 0) return;
        this.currentHealth -= amount;
        if (this.currentHealth < 0) this.currentHealth = 0;
        this.wasAttackedThisTurn = true;
    }

    /**
     * Régénère une partie des PV si l’unité n’a pas été attaquée et est restée immobile.
     */
    public void recoverHealthIfIdle() {
        if (!wasAttackedThisTurn && currentHealth > 0 && currentHealth < maxHealth) {
            int recovered = (int) Math.ceil(maxHealth * 0.10);
            currentHealth = Math.min(maxHealth, currentHealth + recovered);
        }
    }

    /**
     * Vérifie si l’unité est encore en vie.
     *
     * @return true si les PV sont positifs
     */
    public boolean isAlive() {
        return currentHealth > 0;
    }

    @Override
    public String toString() {
        return name + " [" + type.name() + "] HP:" + currentHealth + " MV:" + currentMovement;
    }
}
