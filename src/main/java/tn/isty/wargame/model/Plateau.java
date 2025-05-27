package tn.isty.wargame.model;

import javafx.scene.layout.Pane;
import java.io.Serializable;
import java.util.*;

public class Plateau extends Pane implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final double tileSize = 40;
    private static final double paddingTop = 60;

    private int rows;
    private int cols;

    private HexagonTile[][] grille;
    private TerrainType[][] terrainGrid;

    public Plateau(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;

        this.setPrefSize(1280, 800);
        terrainGrid = new TerrainType[rows][cols];
        generatePlateau();

        this.widthProperty().addListener((obs, oldVal, newVal) -> afficherTerrain());
        this.heightProperty().addListener((obs, oldVal, newVal) -> afficherTerrain());
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }

    private void generatePlateau() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                terrainGrid[row][col] = TerrainType.PLAINE;
            }
        }
        afficherTerrain();
    }

    // 🔧 Correction : version sans arguments (utilisée par GameSetup)
    public void generateIsland() {
        int centerX = rows / 2;
        int centerY = cols / 2;
        int radius = Math.min(rows, cols) / 3;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double distance = Math.sqrt(Math.pow(row - centerX, 2) + Math.pow(col - centerY, 2));
                if (distance < radius * 0.6) terrainGrid[row][col] = TerrainType.PLAINE;
                else if (distance < radius * 0.8) terrainGrid[row][col] = TerrainType.FORET;
                else if (distance < radius) terrainGrid[row][col] = TerrainType.COLLINE;
                else terrainGrid[row][col] = TerrainType.EAU;
            }
        }

        terrainGrid[centerX][centerY] = TerrainType.FORTERESSE;
        afficherTerrain();
    }

    // 🔧 Correction : version sans arguments (utilisée par GameSetup)
    public void generateCityTerrain() {
        double centerX = rows / 2.0;
        double centerY = cols / 2.0;
        double minDim = Math.min(rows, cols) / 4.0;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double dist = Math.sqrt(Math.pow(row - centerX, 2) + Math.pow(col - centerY, 2));
                if (dist < minDim * 0.6) terrainGrid[row][col] = TerrainType.FORTERESSE;
                else if (dist < minDim * 0.8) terrainGrid[row][col] = TerrainType.FORET;
                else terrainGrid[row][col] = TerrainType.PLAINE;
            }
        }

        terrainGrid[(int) centerX][(int) centerY] = TerrainType.FORTERESSE;
        afficherTerrain();
    }

    public void afficherTerrain() {
        double hexHeight = 2 * tileSize;
        double hexWidth = Math.sqrt(3) * tileSize;
        double vertSpacing = hexHeight * 3.0 / 4.0;

        Unit[][] sauvegardeUnites = new Unit[rows][cols];
        if (grille != null) {
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    HexagonTile ancienne = grille[row][col];
                    if (ancienne != null && ancienne.getUnit() != null) {
                        sauvegardeUnites[row][col] = ancienne.getUnit();
                    }
                }
            }
        }

        this.getChildren().removeIf(node -> node instanceof HexagonTile);
        grille = new HexagonTile[rows][cols];

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                TerrainType type = terrainGrid[row][col];
                HexagonTile hex = new HexagonTile(type, row, col);

                double x = col * hexWidth;
                if (row % 2 != 0) x += hexWidth / 2;
                double y = paddingTop + row * vertSpacing;

                hex.setLayoutX(x);
                hex.setLayoutY(y);

                if (sauvegardeUnites[row][col] != null) {
                    hex.setUnit(sauvegardeUnites[row][col]);
                }

                this.getChildren().add(hex);
                grille[row][col] = hex;
            }
        }
    }

    public HexagonTile getCase(int row, int col) {
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            return grille[row][col];
        } else {
            return null;
        }
    }

    public void placerUnite(int row, int col, Unit unite) {
        HexagonTile tile = getCase(row, col);
        if (tile != null && tile.getUnit() == null) {
            tile.setUnit(unite);
            unite.setPosition(tile);
        }
    }

    public void refreshVisibility() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                HexagonTile tile = grille[row][col];
                if (tile != null) tile.updateDisplay();
            }
        }
    }

    public boolean isReachable(HexagonTile from, HexagonTile to, int movementPoints) {
        return calculateMovementCostPath(from, to) <= movementPoints;
    }

    public int calculateMovementCostPath(HexagonTile from, HexagonTile to) {
        return getMovementCost(to);
    }

    public int getMovementCost(HexagonTile tile) {
        if (tile == null) return Integer.MAX_VALUE;
        return tile.getTerrainType().getMoveCost();
    }

    public List<HexagonTile> getAdjacentTiles(HexagonTile from) {
        List<HexagonTile> neighbors = new ArrayList<>();
        int row = from.getRow();
        int col = from.getCol();

        int[][] offsetsEven = {{-1, 0}, {-1, -1}, {0, -1}, {1, 0}, {0, 1}, {-1, 1}};
        int[][] offsetsOdd = {{-1, 0}, {1, -1}, {0, -1}, {1, 0}, {1, 1}, {0, 1}};
        int[][] offsets = (col % 2 == 0) ? offsetsEven : offsetsOdd;

        for (int[] offset : offsets) {
            int newRow = row + offset[0];
            int newCol = col + offset[1];
            HexagonTile neighbor = getCase(newRow, newCol);
            if (neighbor != null) neighbors.add(neighbor);
        }

        return neighbors;
    }

    public boolean isVisible(HexagonTile tile) {
        if (tile == null) return false;

        GameState gameState = HexagonTile.getSharedGameState();
        if (gameState == null) return false;

        Player currentPlayer = gameState.getCurrentPlayer();
        for (Player p : gameState.getAllPlayers()) {
            for (Unit unit : p.getUnits()) {
                if (unit.getOwner().equals(currentPlayer)) {
                    HexagonTile pos = unit.getPosition();
                    if (pos != null && calculerDistance(pos, tile) <= unit.getVisionRange()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private int calculerDistance(HexagonTile a, HexagonTile b) {
        int colA = a.getCol();
        int rowA = a.getRow() - (a.getCol() - (a.getCol() & 1)) / 2;

        int colB = b.getCol();
        int rowB = b.getRow() - (b.getCol() - (b.getCol() & 1)) / 2;

        int dx = colA - colB;
        int dy = rowA - rowB;

        return (Math.abs(dx) + Math.abs(dy) + Math.abs(dx + dy)) / 2;
    }

    public TerrainType getTerrain(int row, int col) {
        return terrainGrid[row][col];
    }
}
