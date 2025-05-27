package tn.isty.wargame.model;

import javafx.animation.FillTransition;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;
import tn.isty.wargame.controller.GameController;
import tn.isty.wargame.view.Logger;

import java.io.InputStream;
import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;

/**
 * Représente une tuile hexagonale sur le plateau de jeu.
 * Elle contient des informations sur le terrain, l’unité présente, la visibilité et les interactions utilisateur.
 */
public class HexagonTile extends StackPane implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final double SIZE = 40;

    private static final Map<TerrainType, Image> textureCache = new EnumMap<>(TerrainType.class);
    private static GameState sharedGameState = null;
    private static Unit selectedUnit = null;

    private final TerrainType terrainType;
    private final int row;
    private final int col;
    private Unit unit;

    // Composants d'affichage (non sérialisés)
    private transient Polygon hexShape;
    private transient Polygon fogOverlay;
    private transient Label unitLabel;
    private transient Tooltip tooltip;
    private transient ImageView unitImageView;

    /**
     * Constructeur de tuile.
     *
     * @param type Type de terrain.
     * @param row  Ligne du plateau.
     * @param col  Colonne du plateau.
     */
    public HexagonTile(TerrainType type, int row, int col) {
        this.terrainType = type;
        this.row = row;
        this.col = col;

        initUI();
        setupEventHandlers();
        updateDisplay();
    }

    /**
     * Initialise l'interface graphique de la tuile.
     */
    private void initUI() {
        hexShape = new Polygon();
        fogOverlay = new Polygon();

        unitImageView = new ImageView();
        unitImageView.setFitWidth(30);
        unitImageView.setFitHeight(30);
        unitImageView.setMouseTransparent(true);

        // Création des 6 points de l’hexagone
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(60 * i - 30);
            double x = SIZE * Math.cos(angle);
            double y = SIZE * Math.sin(angle);
            hexShape.getPoints().addAll(x, y);
            fogOverlay.getPoints().addAll(x, y);
        }

        hexShape.setStroke(Color.BLACK);
        hexShape.setStrokeWidth(1);

        fogOverlay.setFill(Color.rgb(0, 0, 0, 0.6));
        fogOverlay.setVisible(false);
        fogOverlay.setMouseTransparent(true);

        unitLabel = new Label();
        unitLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: black;");
        unitLabel.setMouseTransparent(true);

        tooltip = new Tooltip();

        setPrefSize(SIZE * 2, SIZE * 2);
        getChildren().addAll(hexShape, fogOverlay, unitImageView);
    }

    /**
     * Définit les comportements au clic sur la tuile (sélection, déplacement, attaque).
     */
    private void setupEventHandlers() {
        this.setOnMouseClicked(event -> {
            if (sharedGameState == null) {
                Logger.log("sharedGameState est null");
                return;
            }

            Player current = sharedGameState.getCurrentPlayer();
            GameController controller = new GameController(sharedGameState);

            // Cas 1 : clic sur sa propre unité
            if (unit != null && unit.getOwner().equals(current)) {
                selectedUnit = unit;
                controller.clearHighlights();
                controller.highlightAttackRange(selectedUnit);
                Logger.log("Sélection : " + unit.getName());

            // Cas 2 : clic sur une unité ennemie
            } else if (selectedUnit != null && unit != null && !unit.getOwner().equals(current)) {
                controller.attack(selectedUnit, unit);
                controller.clearHighlights();
                selectedUnit = null;

            // Cas 3 : clic sur une case vide
            } else if (selectedUnit != null && unit == null) {
                controller.moveUnit(selectedUnit, this);
                controller.clearHighlights();
                selectedUnit = null;

            // Autres cas (inutile)
            } else {
                Logger.log("Clic ignoré");
            }
        });
    }

    /**
     * Met à jour l'affichage visuel de la tuile.
     */
    public void updateDisplay() {
        if (hexShape == null || unitLabel == null || fogOverlay == null || unitImageView == null) {
            initUI();
        }

        boolean visible = sharedGameState != null && sharedGameState.getBoard().isVisible(this);

        Image texture = getCachedTerrainTexture(terrainType);
        hexShape.setFill(texture != null ? new ImagePattern(texture) : getColorForTerrain(terrainType));
        fogOverlay.setVisible(!visible);

        if (!visible) {
            unitImageView.setImage(null);
            Tooltip.uninstall(this, tooltip);
            return;
        }

        if (unit != null) {
            Image sprite = getUnitSprite(unit);
            unitImageView.setImage(sprite != null ? sprite : null);

            tooltip.setText("PV : " + unit.getCurrentHealth() +
                            "\nTerrain : " + terrainType +
                            "\nCoût déplacement : " + terrainType.getMoveCost());
        } else {
            unitImageView.setImage(null);
            tooltip.setText("Terrain : " + terrainType +
                            "\nCoût déplacement : " + terrainType.getMoveCost());
        }

        Tooltip.install(this, tooltip);
    }

    /**
     * Charge ou récupère en cache la texture correspondant à un terrain.
     *
     * @param type Le type de terrain.
     * @return Une image ou null si non trouvée.
     */
    private Image getCachedTerrainTexture(TerrainType type) {
        if (textureCache.containsKey(type)) return textureCache.get(type);

        String filename = switch (type) {
            case PLAINE -> "field.jpg";
            case FORET -> "foret.jpg";
            case MONTAGNE -> "mountain.jpg";
            case COLLINE -> "hill.jpg";
            case FORTERESSE -> "mountain.jpg";
            case EAU -> "eau.jpg";
        };

        InputStream is = getClass().getResourceAsStream("/images/" + filename);
        if (is != null) {
            Image image = new Image(is);
            textureCache.put(type, image);
            return image;
        } else {
            Logger.log("Image terrain non trouvée : " + filename);
            return null;
        }
    }

    /**
     * Donne une couleur par défaut au terrain s’il n’y a pas de texture.
     *
     * @param type Le type de terrain.
     * @return La couleur associée.
     */
    private Color getColorForTerrain(TerrainType type) {
        return switch (type) {
            case PLAINE -> Color.LIGHTGREEN;
            case FORET -> Color.DARKGREEN;
            case MONTAGNE -> Color.DIMGRAY;
            case COLLINE -> Color.SANDYBROWN;
            case FORTERESSE -> Color.DARKRED;
            case EAU -> Color.LIGHTBLUE;
            default -> Color.GRAY;
        };
    }

    /**
     * Joue une animation d’attaque temporaire.
     */
    public void playAttackAnimation() {
        FillTransition ft = new FillTransition(Duration.millis(150), hexShape);
        ft.setFromValue(Color.RED);
        ft.setToValue(getColorForTerrain(terrainType));
        ft.setCycleCount(2);
        ft.setAutoReverse(true);
        ft.play();
    }

    /**
     * Met en surbrillance la tuile.
     */
    public void setHighlighted(boolean highlighted) {
        hexShape.setStroke(highlighted ? Color.RED : Color.BLACK);
        hexShape.setStrokeWidth(highlighted ? 3 : 1);
    }

    // Getters et Setters

    public TerrainType getTerrainType() { return terrainType; }
    public Unit getUnit() { return unit; }

    public void setUnit(Unit unit) {
        this.unit = unit;
        if (unit != null) unit.setPosition(this);
        updateDisplay();
    }

    public void removeUnit() {
        this.unit = null;
        updateDisplay();
    }

    public int getRow() { return row; }
    public int getCol() { return col; }

    public static void setSharedGameState(GameState gameState) {
        sharedGameState = gameState;
    }

    public static GameState getSharedGameState() {
        return sharedGameState;
    }

    /**
     * Charge le sprite de l’unité.
     *
     * @param unit L’unité concernée.
     * @return L’image du sprite ou null si absente.
     */
    private Image getUnitSprite(Unit unit) {
        if (unit == null || unit.getSpriteFilename() == null) return null;

        InputStream is = getClass().getResourceAsStream("/sprites/" + unit.getSpriteFilename());
        if (is != null) {
            return new Image(is);
        } else {
            Logger.log("Sprite non trouvé : " + unit.getSpriteFilename());
            return null;
        }
    }
}
