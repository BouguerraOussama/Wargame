package tn.isty.wargame.controller;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.util.Duration;
import tn.isty.wargame.model.*;
import tn.isty.wargame.view.Logger;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class GameController {

    private final GameState gameState;

    public GameController(GameState gameState) {
        this.gameState = gameState;
    }

    public void startGame() {
        Logger.log("🎮 Démarrage de la partie !");
        gameState.initializeGame();
        HexagonTile.setSharedGameState(gameState);
        playTurn();
    }

    public void playTurn() {
        Player current = gameState.getCurrentPlayer();
        HexagonTile.setSharedGameState(gameState);
        Logger.log("🔁 Tour du joueur : " + current.getName());
        gameState.getBoard().refreshVisibility();

        current.getUnits().forEach(unit -> unit.setHasActed(false));

        if (current.isAI()) {
            playAITurn(current);
        } else {
            Logger.log("🕹️ Le joueur humain doit jouer manuellement");
        }
    }

    public void endTurn() {
        Player current = gameState.getCurrentPlayer();

        for (Unit unit : current.getUnits()) {
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
            gameState.getBoard().updateAllTiles(); // 🔄 Met à jour l’affichage
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
            gameState.getBoard().updateAllTiles(); // 🔄
        } else {
            Logger.log("❌ Attaque non autorisée");
        }
    }

    public void playAITurn(Player aiPlayer) {
        Logger.log("🤖 Tour IA : " + aiPlayer.getName());

        List<Unit> units = aiPlayer.getUnits().stream()
                .filter(u -> u.isAlive() && !u.hasActed())
                .toList();

        playNextAIAction(units, 0);
    }

    private void playNextAIAction(List<Unit> units, int index) {
        if (index >= units.size()) {
            endTurn();
            return;
        }

        Unit aiUnit = units.get(index);
        PauseTransition pause = new PauseTransition(Duration.seconds(0.6));
        pause.setOnFinished(e -> Platform.runLater(() -> {
            Optional<Unit> target = gameState.getAllPlayers().stream()
                    .filter(p -> p != aiUnit.getOwner())
                    .flatMap(p -> p.getUnits().stream())
                    .filter(Unit::isAlive)
                    .filter(enemy -> gameState.canAttack(aiUnit, enemy))
                    .findFirst();

            if (target.isPresent()) {
                Logger.log("🤖 IA attaque avec " + aiUnit.getName() + " -> " + target.get().getName());
                attack(aiUnit, target.get());
            } else {
                Unit closest = findClosestEnemy(aiUnit, aiUnit.getOwner());
                if (closest != null) {
                    HexagonTile next = findBestStepTowards(aiUnit, closest.getPosition());
                    if (next != null) {
                        Logger.log("🤖 IA déplace " + aiUnit.getName() + " vers " +
                                next.getRow() + "," + next.getCol());
                        moveUnit(aiUnit, next);
                    } else {
                        Logger.log("🤖 " + aiUnit.getName() + " reste sur place (aucun chemin)");
                    }
                } else {
                    Logger.log("🤖 Aucun ennemi trouvé pour " + aiUnit.getName());
                }
            }

            gameState.getBoard().updateAllTiles();
            playNextAIAction(units, index + 1);
        }));
        pause.play();
    }

    private Unit findClosestEnemy(Unit aiUnit, Player aiPlayer) {
        return gameState.getAllPlayers().stream()
                .filter(p -> p != aiPlayer)
                .flatMap(p -> p.getUnits().stream())
                .filter(Unit::isAlive)
                .min(Comparator.comparingInt(enemy ->
                        gameState.calculateHexDistance(aiUnit.getPosition(), enemy.getPosition())))
                .orElse(null);
    }

    private HexagonTile findBestStepTowards(Unit unit, HexagonTile goal) {
        return gameState.getBoard().getAdjacentTiles(unit.getPosition()).stream()
                .filter(tile -> tile.getUnit() == null)
                .filter(tile -> gameState.getBoard().getMovementCost(tile) < 999)
                .filter(tile -> gameState.getBoard().calculateMovementCostPath(unit.getPosition(), tile) <= unit.getCurrentMovement())
                .min(Comparator.comparingInt(tile -> gameState.calculateHexDistance(tile, goal)))
                .orElse(null);
    }

    public void highlightAttackRange(Unit unit) {
        if (unit == null || unit.getPosition() == null) return;

        Plateau plateau = gameState.getBoard();
        int range = unit.getAttackRange();

        for (int row = 0; row < plateau.getRows(); row++) {
            for (int col = 0; col < plateau.getCols(); col++) {
                HexagonTile tile = plateau.getCase(row, col);
                if (tile == null) continue;

                int distance = gameState.calculateHexDistance(unit.getPosition(), tile);
                boolean inRange = distance <= range && distance > 0;
                boolean visible = plateau.isVisible(tile);

                tile.setHighlighted(inRange && visible);
            }
        }
    }

    public void clearHighlights() {
        Plateau plateau = gameState.getBoard();
        for (int row = 0; row < plateau.getRows(); row++) {
            for (int col = 0; col < plateau.getCols(); col++) {
                HexagonTile tile = plateau.getCase(row, col);
                if (tile != null) tile.setHighlighted(false);
            }
        }
    }
}
