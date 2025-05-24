package tn.isty.wargame.model;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Player> players;
    private int currentPlayerIndex;
    private Plateau board;

    public GameState(List<Player> players, Plateau board) {
        this.players = Objects.requireNonNull(players, "players must not be null");
        this.board = Objects.requireNonNull(board, "board must not be null");
        if (players.isEmpty()) throw new IllegalArgumentException("players list cannot be empty");
        this.currentPlayerIndex = 0;
    }

    public void initializeGame() {
        for (Player player : players) {
            for (Unit unit : player.getUnits()) {
                unit.resetMovement();
                unit.setWasAttackedThisTurn(false);
            }
        }
        board.refreshVisibility();
    }

    public void switchToNextPlayer() {
        Player currentPlayer = getCurrentPlayer();

        for (Unit unit : currentPlayer.getUnits()) {
            if (unit.isAlive()) {
                if (unit.getCurrentMovement() == unit.getMaxMovement() && !unit.wasAttackedThisTurn()) {
                    unit.recoverHealthIfIdle();
                    System.out.println("🔧 " + unit.getName() + " récupère des PV (repos)");
                }
                unit.setWasAttackedThisTurn(false);
                unit.resetMovement();
            }
        }

        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        board.refreshVisibility();
    }

    public boolean isGameOver() {
        long remainingPlayers = players.stream()
                .filter(player -> player.getUnits().stream().anyMatch(Unit::isAlive))
                .count();
        return remainingPlayers <= 1;
    }

    public Player getWinner() {
        return players.stream()
                .filter(player -> player.getUnits().stream().anyMatch(Unit::isAlive))
                .findFirst()
                .orElse(null);
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public List<Player> getAllPlayers() {
        return players;
    }

    public Plateau getBoard() {
        return board;
    }

    public int calculateHexDistance(HexagonTile a, HexagonTile b) {
        if (a == null || b == null) {
            System.err.println("[⚠️ ERROR] HexagonTile null in distance calculation");
            return Integer.MAX_VALUE;
        }

        int dx = a.getCol() - b.getCol();
        int dy = a.getRow() - b.getRow();

        return Math.abs(dx) + Math.abs(dy);
    }

    public boolean canMove(Unit unit, HexagonTile destination) {
        if (unit == null || destination == null) return false;
        if (!unit.isAlive()) return false;
        if (destination.getUnit() != null) return false;

        int cost = board.calculateMovementCostPath(unit.getPosition(), destination);
        return cost <= unit.getCurrentMovement();
    }

    public void moveUnit(Unit unit, HexagonTile destination) {
        if (unit == null || destination == null) return;

        if (!canMove(unit, destination)) {
            System.out.println("❌ Déplacement refusé (coût trop élevé ou case occupée)");
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

    public boolean canAttack(Unit attacker, Unit target) {
        if (attacker == null || target == null) return false;
        if (attacker.getPosition() == null || target.getPosition() == null) return false;

        HexagonTile a = attacker.getPosition();
        HexagonTile b = target.getPosition();
        if (a == null || b == null) return false;

        int distance = calculateHexDistance(a, b);
        return distance <= attacker.getAttackRange();
    }

    public void resolveCombat(Unit attacker, Unit defender) {
        if (!canAttack(attacker, defender)) {
            System.out.println("❌ Combat impossible entre " + attacker + " et " + defender);
            return;
        }

        int baseDamage = attacker.getAttack() - defender.getDefense();
        int terrainModifier = getDefenseBonus(defender.getPosition().getTerrainType());
        int randomFactor = (int) (Math.random() * 5) - 2;

        int totalDamage = Math.max(1, baseDamage + terrainModifier + randomFactor);
        defender.receiveDamage(totalDamage);
        defender.setWasAttackedThisTurn(true);

        System.out.println("💥 Dégâts infligés : " + totalDamage +
                " (base: " + baseDamage + ", terrain: " + terrainModifier + ", hasard: " + randomFactor + ")");
        System.out.println("❤️ PV restants de " + defender.getName() + " : " + defender.getCurrentHealth());

        if (!defender.isAlive()) {
            System.out.println("☠️ " + defender.getName() + " est mort !");
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

    public void setBoard(Plateau board) {
        this.board = Objects.requireNonNull(board);
    }

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
