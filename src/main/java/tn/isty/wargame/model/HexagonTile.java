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

public class HexagonTile extends StackPane implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final double SIZE = 40;

    private static final Map<TerrainType, Image> textureCache = new EnumMap<>(TerrainType.class);

    private static GameState sharedGameState = null;
    private static Unit selectedUnit = null;

    private final TerrainType terrainType;
    private Unit unit;
    private final int row;
    private final int col;

    private transient Polygon hexShape;
    private transient Polygon fogOverlay;
    private transient Label unitLabel;
    private transient Tooltip tooltip;

    private transient ImageView unitImageView;


    public HexagonTile(TerrainType type, int row, int col) {
        this.terrainType = type;
        this.row = row;
        this.col = col;

        initUI();
        setupEventHandlers();
        updateDisplay();
    }

    private void initUI() {
        hexShape = new Polygon();
        fogOverlay = new Polygon();

        unitImageView = new ImageView();
        unitImageView.setFitWidth(30);
        unitImageView.setFitHeight(30);
        unitImageView.setMouseTransparent(true);

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

    private void setupEventHandlers() {
        this.setOnMouseClicked(event -> {
            if (sharedGameState == null) {
                Logger.log(" sharedGameState est null");
                return;
            }

            Player current = sharedGameState.getCurrentPlayer();
            GameController controller = new GameController(sharedGameState);

            if (unit != null && unit.getOwner().equals(current)) {
                selectedUnit = unit;
                controller.clearHighlights();
                controller.highlightAttackRange(selectedUnit);
                Logger.log("✅ Sélection : " + unit.getName());

            } else if (selectedUnit != null && unit != null && !unit.getOwner().equals(current)) {
                controller.attack(selectedUnit, unit);
                controller.clearHighlights();
                selectedUnit = null;

            } else if (selectedUnit != null && unit == null) {
                controller.moveUnit(selectedUnit, this);
                controller.clearHighlights();
                selectedUnit = null;

            } else {
                Logger.log("🟦 Clic ignoré");
            }
        });
    }

    public void updateDisplay() {
        if (hexShape == null || unitLabel == null || fogOverlay == null || unitImageView == null) {
            initUI();
        }

        boolean visible = sharedGameState != null && sharedGameState.getBoard().isVisible(this);

        Image texture = getCachedTerrainTexture(terrainType);
        hexShape.setFill(texture != null ? new ImagePattern(texture) : getColorForTerrain(terrainType));

        fogOverlay.setVisible(!visible);

        if (!visible) {
            // Cache tout si la tuile n'est pas visible
            unitImageView.setImage(null);
            Tooltip.uninstall(this, tooltip);
            return;
        }

        if (unit != null) {
            // Affiche le sprite de l’unité
            Image sprite = getUnitSprite(unit);
            if (sprite != null) {
                unitImageView.setImage(sprite);
            } else {
                unitImageView.setImage(null);
                Logger.log("❌ Sprite manquant pour : " + unit.getUnitType());
            }

            // Met à jour le tooltip
            Player current = sharedGameState != null ? sharedGameState.getCurrentPlayer() : null;
            tooltip.setText("PV : " + unit.getCurrentHealth()
                    + "\nTerrain : " + terrainType
                    + "\nCoût déplacement : " + terrainType.getMoveCost());
        } else {
            // Pas d’unité : pas de sprite
            unitImageView.setImage(null);
            tooltip.setText("Terrain : " + terrainType
                    + "\nCoût déplacement : " + terrainType.getMoveCost());
        }

        Tooltip.install(this, tooltip);
    }


    private Image getCachedTerrainTexture(TerrainType type) {
        if (textureCache.containsKey(type)) {
            return textureCache.get(type);
        }

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
            Logger.log("❌ Image terrain non trouvée : " + filename);
            return null;
        }
    }

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

    public void playAttackAnimation() {
        FillTransition ft = new FillTransition(Duration.millis(150), hexShape);
        ft.setFromValue(Color.RED);
        ft.setToValue(getColorForTerrain(terrainType));
        ft.setCycleCount(2);
        ft.setAutoReverse(true);
        ft.play();
    }

    public void setHighlighted(boolean highlighted) {
        hexShape.setStroke(highlighted ? Color.RED : Color.BLACK);
        hexShape.setStrokeWidth(highlighted ? 3 : 1);
    }

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
    private Image getUnitSprite(Unit unit) {
        if (unit == null || unit.getSpriteFilename() == null) return null;

        InputStream is = getClass().getResourceAsStream("/sprites/" + unit.getSpriteFilename());
        if (is != null) {
            return new Image(is);
        } else {
            Logger.log("❌ Sprite non trouvé : " + unit.getSpriteFilename());
            return null;
        }
    }

}
