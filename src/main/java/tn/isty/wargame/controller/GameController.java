package tn.isty.wargame.controller;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.util.Duration;
import tn.isty.wargame.model.*;
import tn.isty.wargame.view.Logger;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Contrôleur principal de la logique du jeu Wargame.
 * Gère le déroulement des tours, les actions des joueurs et de l'IA,
 * ainsi que les interactions entre les unités (mouvement, combat).
 */
public class GameController {

    private final GameState gameState;

    /**
     * Constructeur du GameController.
     * @param gameState l'état de jeu partagé
     */
    public GameController(GameState gameState) {
        this.gameState = gameState;
    }

    /**
     * Démarre une nouvelle partie : initialise les unités,
     * met à jour l'état partagé et lance le premier tour.
     */
    public void startGame() {
        Logger.log("Démarrage de la partie !");
        gameState.initializeGame(); // Création des unités, positionnement, etc.
        HexagonTile.setSharedGameState(gameState); // Synchronisation de l'état pour les tuiles
        playTurn();
    }

    /**
     * Joue le tour du joueur actuel.
     * Si c'est une IA, déclenche son tour automatiquement.
     * Sinon, attend que l'humain interagisse manuellement.
     */
    public void playTurn() {
        Player current = gameState.getCurrentPlayer();
        HexagonTile.setSharedGameState(gameState);
        Logger.log("Tour du joueur : " + current.getName());

        gameState.getBoard().refreshVisibility(); // Mise à jour de la visibilité (ligne de vue)

        // Réinitialisation de l'état d'action de chaque unité
        current.getUnits().forEach(unit -> unit.setHasActed(false));

        // Exécution automatique si joueur IA
        if (current.isAI()) {
            playAITurn(current);
        } else {
            Logger.log("Le joueur humain doit jouer manuellement");
        }
    }

    /**
     * Termine le tour actuel :
     * - Régénère les unités inactives
     * - Passe au joueur suivant
     * - Vérifie la fin de partie
     */
    public void endTurn() {
        Player current = gameState.getCurrentPlayer();

        for (Unit unit : current.getUnits()) {
            if (unit.isAlive() && !unit.hasActed()) {
                unit.recoverHealthIfIdle(); // Régénération pour les unités au repos
                Logger.log(unit.getName() + " récupère des PV (repos)");
            }
        }

        gameState.switchToNextPlayer(); // Passage au joueur suivant

        if (gameState.isGameOver()) {
            Player winner = gameState.getWinner();
            Logger.log(winner != null
                    ? "Partie terminée ! Gagnant : " + winner.getName()
                    : "Match nul !");
            return;
        }

        playTurn();
    }

    /**
     * Déplace une unité vers une case valide si le mouvement est autorisé.
     * @param unit unité à déplacer
     * @param destination case cible
     */
    public void moveUnit(Unit unit, HexagonTile destination) {
        if (gameState.canMove(unit, destination)) {
            gameState.moveUnit(unit, destination); // Mise à jour de la position
            unit.setHasActed(true);
            Logger.log(unit.getName() + " s’est déplacé en " + destination.getRow() + "," + destination.getCol());
            gameState.getBoard().updateAllTiles(); // Rafraîchissement de l'affichage
        } else {
            Logger.log("Déplacement non autorisé");
        }
    }

    /**
     * Lance une attaque entre deux unités si les règles le permettent.
     * @param attacker unité attaquante
     * @param defender unité cible
     */
    public void attack(Unit attacker, Unit defender) {
        if (gameState.canAttack(attacker, defender)) {
            HexagonTile tile = defender.getPosition();
            if (tile != null) tile.playAttackAnimation(); // Animation d'attaque

            gameState.resolveCombat(attacker, defender); // Application des dégâts
            attacker.setHasActed(true);
            Logger.log(attacker.getName() + " attaque " + defender.getName());
            gameState.getBoard().updateAllTiles();
        } else {
            Logger.log("Attaque non autorisée");
        }
    }

    /**
     * Lance le tour automatique d’un joueur IA.
     * @param aiPlayer joueur contrôlé par l'IA
     */
    public void playAITurn(Player aiPlayer) {
        Logger.log("Tour IA : " + aiPlayer.getName());

        // Récupère les unités disponibles (vivantes et n'ayant pas agi)
        List<Unit> units = aiPlayer.getUnits().stream()
                .filter(u -> u.isAlive() && !u.hasActed())
                .toList();

        playNextAIAction(units, 0);
    }

    /**
     * Joue récursivement les actions des unités IA avec une pause entre chaque action.
     * @param units liste des unités IA disponibles
     * @param index index de l’unité à traiter
     */
    private void playNextAIAction(List<Unit> units, int index) {
        if (index >= units.size()) {
            endTurn(); // Fin du tour IA
            return;
        }

        Unit aiUnit = units.get(index);
        PauseTransition pause = new PauseTransition(Duration.seconds(0.6));

        pause.setOnFinished(e -> Platform.runLater(() -> {
            // Cherche une cible valide
            Optional<Unit> target = gameState.getAllPlayers().stream()
                    .filter(p -> p != aiUnit.getOwner())
                    .flatMap(p -> p.getUnits().stream())
                    .filter(Unit::isAlive)
                    .filter(enemy -> gameState.canAttack(aiUnit, enemy))
                    .findFirst();

            if (target.isPresent()) {
                Logger.log("IA attaque avec " + aiUnit.getName() + " -> " + target.get().getName());
                attack(aiUnit, target.get());
            } else {
                // Sinon, déplacement vers l’ennemi le plus proche
                Unit closest = findClosestEnemy(aiUnit, aiUnit.getOwner());
                if (closest != null) {
                    HexagonTile next = findBestStepTowards(aiUnit, closest.getPosition());
                    if (next != null) {
                        Logger.log("IA déplace " + aiUnit.getName() + " vers " +
                                next.getRow() + "," + next.getCol());
                        moveUnit(aiUnit, next);
                    } else {
                        Logger.log(aiUnit.getName() + " reste sur place (aucun chemin)");
                    }
                } else {
                    Logger.log("Aucun ennemi trouvé pour " + aiUnit.getName());
                }
            }

            gameState.getBoard().updateAllTiles();
            playNextAIAction(units, index + 1); // Passe à l’unité suivante
        }));

        pause.play();
    }

    /**
     * Trouve l’unité ennemie vivante la plus proche de l’unité IA.
     * @param aiUnit unité IA
     * @param aiPlayer propriétaire de l’unité IA
     * @return l’ennemi le plus proche, ou null si aucun trouvé
     */
    private Unit findClosestEnemy(Unit aiUnit, Player aiPlayer) {
        return gameState.getAllPlayers().stream()
                .filter(p -> p != aiPlayer)
                .flatMap(p -> p.getUnits().stream())
                .filter(Unit::isAlive)
                .min(Comparator.comparingInt(enemy ->
                        gameState.calculateHexDistance(aiUnit.getPosition(), enemy.getPosition())))
                .orElse(null);
    }

    /**
     * Sélectionne la meilleure case adjacente à une unité pour se rapprocher d’un objectif.
     * @param unit unité à déplacer
     * @param goal case cible à atteindre
     * @return la case voisine optimale, ou null si aucune n’est accessible
     */
    private HexagonTile findBestStepTowards(Unit unit, HexagonTile goal) {
        return gameState.getBoard().getAdjacentTiles(unit.getPosition()).stream()
                .filter(tile -> tile.getUnit() == null) // Case libre
                .filter(tile -> gameState.getBoard().getMovementCost(tile) < 999) // Pas d'obstacle
                .filter(tile -> gameState.getBoard().calculateMovementCostPath(unit.getPosition(), tile) <= unit.getCurrentMovement())
                .min(Comparator.comparingInt(tile -> gameState.calculateHexDistance(tile, goal)))
                .orElse(null);
    }

    /**
     * Met en surbrillance toutes les cases dans la portée d'attaque d'une unité.
     * @param unit unité sélectionnée
     */
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
                boolean visible = plateau.isVisible(tile); // Vérifie si la case est visible

                tile.setHighlighted(inRange && visible); // Surbrillance si applicable
            }
        }
    }

    /**
     * Réinitialise tous les surlignages de la carte.
     */
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
