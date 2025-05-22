package tn.isty.wargame.model;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import java.io.Serializable;
import javafx.animation.FillTransition;
import javafx.util.Duration;
import javafx.scene.shape.Shape;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import tn.isty.wargame.controller.GameController;

public class HexagonTile extends StackPane implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final double SIZE = 40;

    // Pour éviter la dépendance statique, mieux vaut passer gameState ou controller via setter
    private static GameState sharedGameState = null; 

    private TerrainType terrainType;
    private Unit unit;
    private int row;
    private int col;

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
        Polygon hex = new Polygon();
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(60 * i - 30);
            double x = SIZE * Math.cos(angle);
            double y = SIZE * Math.sin(angle);
            hex.getPoints().addAll(x, y);
        }
        hex.setStroke(Color.BLACK);
        hex.setFill(getColorForTerrain(terrainType));

        this.setPrefSize(SIZE * 2, SIZE * 2);
        this.getChildren().add(hex);

        unitLabel = new Label();
        unitLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: black;");
        unitLabel.setMouseTransparent(true);
        this.getChildren().add(unitLabel);

        tooltip = new Tooltip();
    }

    private void setupEventHandlers() {
        this.setOnMouseClicked(event -> {
            if (sharedGameState == null) {
                System.out.println("❌ sharedGameState est null");
                return;
            }
            Player current = sharedGameState.getCurrentPlayer();
            // Instancier GameController une fois à l'extérieur idéalement
            GameController controller = new GameController(sharedGameState);

            if (unit != null && unit.getOwner().equals(current)) {
                selectedUnit = unit;
                System.out.println("✅ Sélection : " + unit.getName());

            } else if (selectedUnit != null && unit != null && !unit.getOwner().equals(current)) {
                System.out.println("⚔️ Attaque de " + selectedUnit.getName() + " sur " + unit.getName());
                controller.attack(selectedUnit, unit);
                selectedUnit = null;

            } else if (selectedUnit != null && unit == null) {
                System.out.println("🚶 Tentative de déplacement...");
                controller.moveUnit(selectedUnit, this);
                selectedUnit = null;

            } else {
                System.out.println("🟦 Clic ignoré");
            }
        });
    }

    private Color getColorForTerrain(TerrainType type) {
        switch (type) {
            case PLAINE: return Color.LIGHTGREEN;
            case FORET: return Color.DARKGREEN;
            case MONTAGNE: return Color.DIMGRAY;
            case COLLINE: return Color.SANDYBROWN;
            case FORTERESSE: return Color.DARKRED;
            case EAU: return Color.BLUE;
            default: return Color.GRAY;
        }
    }

    public void updateDisplay() {
        if (unitLabel == null) {
            initUI(); // en cas de désérialisation, réinitialiser l’UI
        }

        Player current = sharedGameState != null ? sharedGameState.getCurrentPlayer() : null;

        boolean visible = sharedGameState != null && (
            sharedGameState.getBoard().isVisible(this) ||
            (unit != null && current != null && unit.getOwner().equals(current))
        );

        Shape hexShape = (Shape) getChildren().get(0);

        if (!visible) {
            hexShape.setFill(Color.DARKGRAY);
            unitLabel.setText("");
            Tooltip.uninstall(this, tooltip);
            return;
        }

        hexShape.setFill(getColorForTerrain(terrainType));

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
        Shape hexShape = (Shape) getChildren().get(0);
        FillTransition ft = new FillTransition(Duration.millis(300), hexShape);
        ft.setFromValue(Color.RED);
        ft.setToValue(getColorForTerrain(terrainType));
        ft.setCycleCount(4);
        ft.setAutoReverse(true);
        ft.play();
    }

    // Getters / setters

    public TerrainType getTerrainType() {
        return terrainType;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
        if (unit != null) unit.setPosition(this);
        updateDisplay();
    }

    public void removeUnit() {
        this.unit = null;
        updateDisplay();
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    // Static accessors for sharedGameState (à documenter clairement)
    public static void setSharedGameState(GameState gameState) {
        sharedGameState = gameState;
    }

    public static GameState getSharedGameState() {
        return sharedGameState;
    }
}
