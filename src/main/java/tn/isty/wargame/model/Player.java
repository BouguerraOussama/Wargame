package tn.isty.wargame.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente un joueur dans le jeu.
 * Un joueur peut être humain ou contrôlé par une IA, et possède une liste d'unités associées.
 * Il peut également appartenir à un type d'armée spécifique (France, Allemagne, etc.).
 */
public class Player implements Serializable {
    private String name;
    private List<Unit> units;
    private boolean isAI;
    private ArmyType armyType;

    /**
     * Constructeur du joueur.
     *
     * @param name le nom du joueur
     * @param isAI true si le joueur est contrôlé par l’IA, false s’il est humain
     */
    public Player(String name, boolean isAI) {
        this.name = name;
        this.isAI = isAI;
        this.units = new ArrayList<>();
    }

    /**
     * @return le nom du joueur
     */
    public String getName() {
        return name;
    }

    /**
     * @return la liste des unités du joueur
     */
    public List<Unit> getUnits() {
        return units;
    }

    /**
     * @return true si le joueur est une IA, false sinon
     */
    public boolean isAI() {
        return isAI;
    }

    /**
     * @return le type d’armée du joueur (France, Allemagne, Royaume-Uni, etc.)
     */
    public ArmyType getArmyType() {
        return armyType;
    }

    /**
     * Définit le type d’armée du joueur.
     *
     * @param armyType le type d’armée à assigner
     */
    public void setArmyType(ArmyType armyType) {
        this.armyType = armyType;
    }

    /**
     * Ajoute une unité à la liste d’unités du joueur.
     *
     * @param unit l’unité à ajouter
     */
    public void addUnit(Unit unit) {
        units.add(unit);
    }

    /**
     * Retire une unité de la liste d’unités du joueur.
     *
     * @param unit l’unité à retirer
     */
    public void removeUnit(Unit unit) {
        units.remove(unit);
    }

    /**
     * Vérifie si le joueur possède encore des unités en vie.
     *
     * @return true si au moins une unité a des points de vie > 0, false sinon
     */
    public boolean hasUnitsAlive() {
        return units.stream().anyMatch(unit -> unit.getCurrentHealth() > 0);
    }

    /**
     * Réinitialise les points de déplacement de toutes les unités du joueur.
     * Cette méthode est typiquement appelée au début d’un nouveau tour.
     */
    public void resetUnitsMovement() {
        for (Unit unit : units) {
            unit.resetMovement();
        }
    }
}
