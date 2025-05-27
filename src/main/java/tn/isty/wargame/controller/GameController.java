package tn.isty.wargame.controller;

import tn.isty.wargame.model.GameState;
import tn.isty.wargame.model.HexagonTile;
import tn.isty.wargame.model.Player;
import tn.isty.wargame.model.TerrainType;
import tn.isty.wargame.model.Unit;
import tn.isty.wargame.view.Logger;

import java.util.List;

public class GameController {

    private final GameState gameState;

    public GameController(GameState gameState) {
        this.gameState = gameState;
    }

    public void startGame() {
        Logger.log("🎮 Démarrage de la partie !");
        gameState.initializeGame();
        playTurn();
    }

    public void playTurn() {
        Player current = gameState.getCurrentPlayer();
        Logger.log("🔁 Tour du joueur : " + current.getName());
        gameState.getBoard().refreshVisibility();

        for (Unit unit : current.getUnits()) {
            unit.setHasActed(false); // réinitialisation en début de tour
        }

        if (current.isAI()) {
            playAITurn(current);
        } else {
            Logger.log("🕹️ Le joueur humain doit jouer manuellement");
        }
    }

    public void endTurn() {
        Player currentPlayer = gameState.getCurrentPlayer();

        for (Unit unit : currentPlayer.getUnits()) {
            if (unit.isAlive() && !unit.hasActed()) {
                unit.recoverHealthIfIdle();
                Logger.log("🔧 " + unit.getName() + " récupère des PV (repos)");
            }
        }

        gameState.switchToNextPlayer();

        if (gameState.isGameOver()) {
            Player winner = gameState.getWinner();
            Logger.log(winner != null
                    ? "🎉 Partie terminée ! Gagnant : " + winner.getName()
                    : "🎯 Match nul !");
            return;
        }

        playTurn();
    }

    public void moveUnit(Unit unit, HexagonTile destination) {
        if (gameState.canMove(unit, destination)) {
            gameState.moveUnit(unit, destination);
            unit.setHasActed(true);
            Logger.log(unit.getName() + " s’est déplacé en " + destination.getRow() + "," + destination.getCol());
        } else {
            Logger.log("❌ Déplacement non autorisé");
        }
    }

    public void attack(Unit attacker, Unit defender) {
        if (gameState.canAttack(attacker, defender)) {
            HexagonTile tile = defender.getPosition();
            if (tile != null) tile.playAttackAnimation();

            gameState.resolveCombat(attacker, defender);
            attacker.setHasActed(true);
            Logger.log(attacker.getName() + " attaque " + defender.getName());
        } else {
            Logger.log("❌ Attaque non autorisée");
        }
    }

    public void playAITurn(Player aiPlayer) {
        Logger.log("🤖 Tour de l’IA : " + aiPlayer.getName());

        for (Unit aiUnit : aiPlayer.getUnits()) {
            if (!aiUnit.isAlive()) continue;

            // 1. Essayer d'attaquer une unité ennemie
            for (Player opponent : gameState.getAllPlayers()) {
                if (opponent == aiPlayer) continue;

                for (Unit target : opponent.getUnits()) {
                    if (!target.isAlive()) continue;

                    if (gameState.canAttack(aiUnit, target)) {
                        Logger.log("🤖 IA attaque avec " + aiUnit.getName() + " -> " + target.getName());
                        attack(aiUnit, target);
                        return; // Une action par tour
                    }
                }
            }

            // 2. Trouver l’ennemi vivant le plus proche
            Unit closest = null;
            int minDist = Integer.MAX_VALUE;
            for (Player opponent : gameState.getAllPlayers()) {
                if (opponent == aiPlayer) continue;
                for (Unit enemy : opponent.getUnits()) {
                    if (!enemy.isAlive()) continue;

                    int dist = gameState.calculateHexDistance(aiUnit.getPosition(), enemy.getPosition());
                    if (dist < minDist) {
                        minDist = dist;
                        closest = enemy;
                    }
                }
            }

            // 3. Se déplacer vers la cible
            if (closest != null) {
                HexagonTile from = aiUnit.getPosition();
                HexagonTile to = findStepToward(from, closest.getPosition(), aiUnit);

                if (to != null && gameState.canMove(aiUnit, to)) {
                    TerrainType terrain = to.getTerrainType();
                    if (terrain.getMoveCost() >= 999) {
                        Logger.log("🌊 IA évite terrain interdit (eau)");
                        continue;
                    }

                    Logger.log("🤖 IA déplace " + aiUnit.getName() + " vers " + to.getRow() + "," + to.getCol());
                    moveUnit(aiUnit, to);
                    return; // Une action par tour
                }
            }

            // 4. Si rien à faire
            Logger.log("🤖 " + aiUnit.getName() + " reste sur place.");
        }

        endTurn(); // ➡️ Fin du tour IA
    }

    /**
     * Trouve une tuile adjacente qui rapproche de la cible (bête mais meilleure que random).
     */
    private HexagonTile findStepToward(HexagonTile from, HexagonTile to, Unit unit) {
        List<HexagonTile> neighbors = gameState.getBoard().getAdjacentTiles(from);
        HexagonTile best = null;
        int bestDist = Integer.MAX_VALUE;

        for (HexagonTile neighbor : neighbors) {
            if (neighbor.getUnit() != null) continue; // Éviter collisions
            if (gameState.getBoard().getMovementCost(neighbor) >= 999) continue; // Infranchissable

            int cost = gameState.getBoard().calculateMovementCostPath(from, neighbor);
            if (cost > unit.getCurrentMovement()) continue;

            int dist = gameState.calculateHexDistance(neighbor, to);
            if (dist < bestDist) {
                bestDist = dist;
                best = neighbor;
            }
        }

        return best;
    }
}
