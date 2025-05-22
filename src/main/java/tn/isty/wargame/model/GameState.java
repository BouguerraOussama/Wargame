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

    /** Initialise les unités pour un nouveau jeu */
    public void initializeGame() {
        for (Player player : players) {
            for (Unit unit : player.getUnits()) {
                unit.resetMovement();
                unit.setWasAttackedThisTurn(false);
            }
        }
        board.refreshVisibility();
    }

    /** Passe au joueur suivant et réinitialise les unités */
    public void switchToNextPlayer() {
        Player currentPlayer = getCurrentPlayer();

        for (Unit unit : currentPlayer.getUnits()) {
            if (unit.isAlive()) {
                // Récupération PV si unité n'a pas été attaquée ce tour et mouvement complet
                if (unit.getCurrentMovement() == unit.getMaxMovement() && !unit.wasAttackedThisTurn()) {
                    unit.recoverHealthIfIdle();
                    // Affichage à externaliser si besoin
                    System.out.println("🔧 " + unit.getName() + " récupère des PV (repos)");
                }
                unit.setWasAttackedThisTurn(false);
                unit.resetMovement();
            }
        }

        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        board.refreshVisibility();
    }

    /** Retourne true si la partie est terminée (1 ou 0 joueurs avec unités vivantes) */
    public boolean isGameOver() {
        long remainingPlayers = players.stream()
                .filter(player -> player.getUnits().stream().anyMatch(Unit::isAlive))
                .count();
        return remainingPlayers <= 1;
    }

    /** Renvoie le joueur gagnant (celui qui a des unités vivantes), ou null si aucun */
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

    /**
     * Calcule la distance hexagonale entre deux cases.
     * Formule basée sur coordonnées « cube » pour hexagones décalés.
     */
    public static int calculateHexDistance(HexagonTile a, HexagonTile b) {
        if (a == null || b == null) throw new IllegalArgumentException("HexagonTile arguments cannot be null");

        int colA = a.getCol();
        int rowA = a.getRow() - (colA - (colA & 1)) / 2;

        int colB = b.getCol();
        int rowB = b.getRow() - (colB - (colB & 1)) / 2;

        int dx = colA - colB;
        int dy = rowA - rowB;

        return (Math.abs(dx) + Math.abs(dy) + Math.abs(dx + dy)) / 2;
    }

    /**
     * Vérifie si une unité peut se déplacer vers la destination donnée,
     * en prenant en compte le coût de mouvement du terrain et la présence d'unités.
     */
    public boolean canMove(Unit unit, HexagonTile destination) {
        if (unit == null || destination == null) return false;
        if (!unit.isAlive()) return false;
        if (destination.getUnit() != null) return false; // case occupée

        int cost = board.calculateMovementCostPath(unit.getPosition(), destination);
        return cost <= unit.getCurrentMovement();
    }

    /**
     * Déplace l'unité vers la destination si possible.
     * Met à jour les mouvements restants et l'affichage.
     */
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

    /**
     * Vérifie si un attaquant peut attaquer un défenseur (distance max 1, pas même joueur)
     */
    public boolean canAttack(Unit attacker, Unit defender) {
        if (attacker == null || defender == null) return false;
        if (!attacker.isAlive() || !defender.isAlive()) return false;
        if (attacker.getOwner().equals(defender.getOwner())) return false;

        int distance = calculateHexDistance(attacker.getPosition(), defender.getPosition());
        return distance <= 1;
    }

    /**
     * Résout un combat entre un attaquant et un défenseur,
     * inflige les dégâts et marque la victime comme attaquée.
     */
    public void resolveCombat(Unit attacker, Unit defender) {
        if (!canAttack(attacker, defender)) {
            System.out.println("❌ Combat impossible entre " + attacker + " et " + defender);
            return;
        }

        int baseDamage = attacker.getAttack() - defender.getDefense();
        int terrainModifier = 0; // TODO : appliquer modificateur terrain
        int randomFactor = (int) (Math.random() * 5) - 2;

        int totalDamage = Math.max(1, baseDamage + terrainModifier + randomFactor);
        defender.receiveDamage(totalDamage);
        defender.setWasAttackedThisTurn(true);

        System.out.println("💥 Dégâts infligés : " + totalDamage +
                " (base: " + baseDamage + ", terrain: " + terrainModifier + ", hasard: " + randomFactor + ")");
        System.out.println("❤️ PV restants de " + defender.getName() + " : " + defender.getCurrentHealth());

        // Gérer la mort éventuelle de l’unité
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
}
