package tn.isty.wargame.model;

import javafx.animation.FillTransition;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;
import tn.isty.wargame.controller.GameController;
import tn.isty.wargame.view.Logger;

import java.io.InputStream;
import java.io.Serializable;

public class HexagonTile extends StackPane implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final double SIZE = 40;

    private static GameState sharedGameState = null;

    private TerrainType terrainType;
    private Unit unit;
    private int row;
    private int col;

    private transient Polygon hexShape;
    private transient Polygon fogOverlay;
    private transient Label unitLabel;
    private transient Tooltip tooltip;

    private static Unit selectedUnit = null;

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

        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(60 * i - 30);
            double x = SIZE * Math.cos(angle);
            double y = SIZE * Math.sin(angle);
            hexShape.getPoints().addAll(x, y);
            fogOverlay.getPoints().addAll(x, y);
        }

        hexShape.setStroke(Color.BLACK);

        fogOverlay.setFill(Color.rgb(0, 0, 0, 0.6)); // gris semi-transparent
        fogOverlay.setVisible(false); // visible uniquement si le tile est dans le brouillard
        fogOverlay.setMouseTransparent(true);

        unitLabel = new Label();
        unitLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: black;");
        unitLabel.setMouseTransparent(true);

        this.setPrefSize(SIZE * 2, SIZE * 2);
        this.getChildren().addAll(hexShape, fogOverlay, unitLabel);
        tooltip = new Tooltip();
    }

    private void setupEventHandlers() {
        this.setOnMouseClicked(event -> {
            if (sharedGameState == null) {
                Logger.log("❌ sharedGameState est null");
                return;
            }

            Player current = sharedGameState.getCurrentPlayer();
            GameController controller = new GameController(sharedGameState);

            if (unit != null && unit.getOwner().equals(current)) {
                selectedUnit = unit;
                Logger.log("✅ Sélection : " + unit.getName());
            } else if (selectedUnit != null && unit != null && !unit.getOwner().equals(current)) {
                Logger.log("⚔️ Attaque de " + selectedUnit.getName() + " sur " + unit.getName());
                controller.attack(selectedUnit, unit);
                selectedUnit = null;
            } else if (selectedUnit != null && unit == null) {
                Logger.log("🚶 Déplacement...");
                controller.moveUnit(selectedUnit, this);
                selectedUnit = null;
            } else {
                Logger.log("🟦 Clic ignoré");
            }
        });
    }

    public void updateDisplay() {
        if (unitLabel == null || hexShape == null || fogOverlay == null) {
            initUI();
        }

        Player current = sharedGameState != null ? sharedGameState.getCurrentPlayer() : null;

        boolean visible = sharedGameState != null && (
                sharedGameState.getBoard().isVisible(this) ||
                (unit != null && current != null && unit.getOwner().equals(current))
        );

        Image texture = loadTerrainTexture(terrainType);
        if (texture != null) {
            hexShape.setFill(new ImagePattern(texture));
        } else {
            hexShape.setFill(getColorForTerrain(terrainType));
        }

        fogOverlay.setVisible(!visible);

        if (!visible) {
            unitLabel.setText("");
            Tooltip.uninstall(this, tooltip);
            return;
        }

        if (unit != null) {
            unitLabel.setText(unit.getName() + " (" + unit.getType() + ")");
            unitLabel.setTextFill(current != null && unit.getOwner().equals(current) ? Color.BLUE : Color.CRIMSON);

            tooltip.setText("PV : " + unit.getCurrentHealth() +
                    "\nTerrain : " + terrainType +
                    "\nCoût déplacement : " + terrainType.getMoveCost());
            Tooltip.install(this, tooltip);
        } else {
            unitLabel.setText("");
            tooltip.setText("Terrain : " + terrainType +
                    "\nCoût déplacement : " + terrainType.getMoveCost());
            Tooltip.install(this, tooltip);
        }
    }

    public void playAttackAnimation() {
        FillTransition ft = new FillTransition(Duration.millis(300), hexShape);
        ft.setFromValue(Color.RED);
        ft.setToValue(getColorForTerrain(terrainType));
        ft.setCycleCount(4);
        ft.setAutoReverse(true);
        ft.play();
    }

    private Image loadTerrainTexture(TerrainType type) {
        String filename = switch (type) {
            case PLAINE -> "field.jpg";
            case FORET -> "foret.jpg";
            case MONTAGNE -> "mountain.jpg";
            case COLLINE -> "hill.jpg";
            case FORTERESSE -> "mountain.jpg";
            case EAU -> "eau.jpg";
        };
        InputStream is = getClass().getResourceAsStream("/images/" + filename);
        if (is == null) {
            Logger.log("❌ Image terrain non trouvée : " + filename);
            return null;
        }
        return new Image(is);
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

    // --- Getters & Setters

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
}
