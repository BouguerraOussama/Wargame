package tn.isty.wargame.model;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import tn.isty.wargame.view.Logger;

/**
 * Représente l’état courant du jeu (joueurs, plateau, tour).
 * Ce modèle est sérialisable pour permettre la sauvegarde et le chargement des parties.
 */
public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Player> players;
    private int currentPlayerIndex;
    private Plateau board;

    /**
     * Crée un nouvel état de jeu avec les joueurs et le plateau.
     *
     * @param players Liste des joueurs (non vide).
     * @param board   Plateau de jeu.
     * @throws NullPointerException     si players ou board est null.
     * @throws IllegalArgumentException si la liste de joueurs est vide.
     */
    public GameState(List<Player> players, Plateau board) {
        this.players = Objects.requireNonNull(players, "players must not be null");
        this.board = Objects.requireNonNull(board, "board must not be null");
        if (players.isEmpty()) throw new IllegalArgumentException("players list cannot be empty");
        this.currentPlayerIndex = 0;
    }

    /**
     * Initialise l’état du jeu (mouvements, attaques, visibilité).
     */
    public void initializeGame() {
        for (Player player : players) {
            for (Unit unit : player.getUnits()) {
                unit.resetMovement();
                unit.setWasAttackedThisTurn(false);
            }
        }
        board.refreshVisibility();
    }

    /**
     * Passe au joueur suivant et réinitialise l’état de ses unités.
     * Les unités qui n’ont pas bougé et qui n’ont pas été attaquées se régénèrent.
     */
    public void switchToNextPlayer() {
        Player currentPlayer = getCurrentPlayer();

        for (Unit unit : currentPlayer.getUnits()) {
            if (unit.isAlive()) {
                if (unit.getCurrentMovement() == unit.getMaxMovement() && !unit.wasAttackedThisTurn()) {
                    unit.recoverHealthIfIdle(); // régénération
                    Logger.log(unit.getName() + " récupère des PV (repos)");
                }
                unit.setWasAttackedThisTurn(false);
                unit.resetMovement();
            }
        }

        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        board.refreshVisibility();
    }

    /**
     * Vérifie si le jeu est terminé (1 joueur ou moins).
     *
     * @return true si un seul joueur (ou aucun) possède encore des unités vivantes.
     */
    public boolean isGameOver() {
        long remainingPlayers = players.stream()
                .filter(player -> player.getUnits().stream().anyMatch(Unit::isAlive))
                .count();
        return remainingPlayers <= 1;
    }

    /**
     * Renvoie le gagnant du jeu.
     *
     * @return Le joueur encore en vie, ou null s’il n’y en a aucun.
     */
    public Player getWinner() {
        return players.stream()
                .filter(player -> player.getUnits().stream().anyMatch(Unit::isAlive))
                .findFirst()
                .orElse(null);
    }

    /**
     * @return Le joueur actif du tour actuel.
     */
    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    /**
     * @return La liste de tous les joueurs du jeu.
     */
    public List<Player> getAllPlayers() {
        return players;
    }

    /**
     * @return Le plateau de jeu actuel.
     */
    public Plateau getBoard() {
        return board;
    }

    /**
     * Calcule la distance "hexagonale" entre deux tuiles (simplifié).
     *
     * @param a Tuile de départ.
     * @param b Tuile d’arrivée.
     * @return Distance entre les deux tuiles, ou MAX_VALUE si erreur.
     */
    public int calculateHexDistance(HexagonTile a, HexagonTile b) {
        if (a == null || b == null) {
            Logger.log("[ERREUR] HexagonTile null dans le calcul de distance");
            return Integer.MAX_VALUE;
        }

        int dx = a.getCol() - b.getCol();
        int dy = a.getRow() - b.getRow();
        return Math.abs(dx) + Math.abs(dy); // approximation simplifiée
    }

    /**
     * Vérifie si une unité peut se déplacer vers une tuile.
     */
    public boolean canMove(Unit unit, HexagonTile destination) {
        if (unit == null || destination == null) return false;
        if (!unit.isAlive()) return false;
        if (destination.getUnit() != null) return false;

        if (!board.isVisible(destination)) {
            Logger.log("Déplacement interdit dans une zone non visible");
            return false;
        }

        if (board.getMovementCost(destination) >= 999) {
            Logger.log("Déplacement impossible : terrain infranchissable");
            return false;
        }

        List<HexagonTile> adjacentes = board.getAdjacentTiles(unit.getPosition());
        if (!adjacentes.contains(destination)) {
            Logger.log("Déplacement interdit : seule une case adjacente est autorisée");
            return false;
        }

        int cost = board.calculateMovementCostPath(unit.getPosition(), destination);
        return cost <= unit.getCurrentMovement();
    }

    /**
     * Effectue le déplacement de l’unité vers la tuile destination.
     */
    public void moveUnit(Unit unit, HexagonTile destination) {
        if (unit == null || destination == null) return;

        if (!canMove(unit, destination)) {
            Logger.log("Déplacement refusé (coût trop élevé ou case occupée)");
            return;
        }

        HexagonTile from = unit.getPosition();
        if (from != null) {
            from.setUnit(null);
            from.updateDisplay();
        }

        unit.setPosition(destination);
        destination.setUnit(unit);
        int cost = board.calculateMovementCostPath(from, destination);
        unit.setCurrentMovement(unit.getCurrentMovement() - cost);
        destination.updateDisplay();
    }

    /**
     * Vérifie si un attaquant peut attaquer une cible donnée.
     */
    public boolean canAttack(Unit attacker, Unit target) {
        if (attacker == null || target == null) return false;
        if (attacker.getPosition() == null || target.getPosition() == null) return false;

        HexagonTile a = attacker.getPosition();
        HexagonTile b = target.getPosition();
        if (a == null || b == null) return false;

        int distance = calculateHexDistance(a, b);
        return distance <= attacker.getAttackRange();
    }

    /**
     * Résout un combat entre deux unités.
     */
    public void resolveCombat(Unit attacker, Unit defender) {
        if (!canAttack(attacker, defender)) {
            Logger.log("Combat impossible entre " + attacker + " et " + defender);
            return;
        }

        int baseDamage = attacker.getAttack() - defender.getDefense();
        int terrainModifier = getDefenseBonus(defender.getPosition().getTerrainType());
        int randomFactor = (int) (Math.random() * 5) - 2;

        int totalDamage = Math.max(1, baseDamage + terrainModifier + randomFactor);
        defender.receiveDamage(totalDamage);
        defender.setWasAttackedThisTurn(true);

        Logger.log("Dégâts infligés : " + totalDamage +
                " (base: " + baseDamage + ", terrain: " + terrainModifier + ", hasard: " + randomFactor + ")");
        Logger.log("PV restants de " + defender.getName() + " : " + defender.getCurrentHealth());

        if (!defender.isAlive()) {
            Logger.log(defender.getName() + " est mort");
            HexagonTile tile = defender.getPosition();
            if (tile != null) {
                tile.setUnit(null);
                tile.updateDisplay();
            }
            Player owner = defender.getOwner();
            if (owner != null) {
                owner.removeUnit(defender);
            }
        }
    }

    /**
     * Remplace le plateau actuel par un nouveau (ex. après chargement).
     */
    public void setBoard(Plateau board) {
        this.board = Objects.requireNonNull(board);
    }

    /**
     * Renvoie un bonus défensif selon le type de terrain.
     */
    private int getDefenseBonus(TerrainType type) {
        return switch (type) {
            case FORET -> 1;
            case COLLINE -> 2;
            case MONTAGNE -> 3;
            case FORTERESSE -> 4;
            default -> 0;
        };
    }
}
