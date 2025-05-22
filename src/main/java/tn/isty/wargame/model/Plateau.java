package tn.isty.wargame.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javafx.scene.layout.Pane;

public class Plateau extends Pane implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int rows;
    private final int cols;
    private final double tileSize = 40;

    private HexagonTile[][] grille;
    private TerrainType[][] terrainGrid;

    public Plateau(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.setPrefSize(1920, 1080);

        terrainGrid = new TerrainType[rows][cols];
        generatePlateau();
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

    public void generateIsland(int rows, int cols) {
        this.getChildren().clear();
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

    public void generateCityTerrain(int rows, int cols) {
        this.getChildren().clear();

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

    private void afficherTerrain() {
        double hexWidth = tileSize * 2;              // largeur (distance horizontale)
        double hexHeight = Math.sqrt(3) * tileSize; // hauteur (distance verticale)

        grille = new HexagonTile[rows][cols];

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                TerrainType type = terrainGrid[row][col];
                HexagonTile hex = new HexagonTile(type, row, col);

                double x = col * (hexWidth * 0.75);
                double y = row * hexHeight + (col % 2) * (hexHeight / 2);

                hex.setLayoutX(x);
                hex.setLayoutY(y);
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

    public void afficherConsole() {
        System.out.println("=== Plateau de jeu ===");
        for (int row = 0; row < rows; row++) {
            if (row % 2 != 0) System.out.print("  ");
            for (int col = 0; col < cols; col++) {
                HexagonTile tile = grille[row][col];
                Unit u = tile.getUnit();
                System.out.print(u != null ? "[" + u.getName().charAt(0) + "] " : "[ ] ");
            }
            System.out.println();
        }
    }

    public void refreshVisibility() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                grille[row][col].updateDisplay();
            }
        }
    }

    public boolean isReachable(HexagonTile from, HexagonTile to, int movementPoints) {
        if (from == null || to == null) return false;
        int cost = calculateMovementCostPath(from, to);
        return cost <= movementPoints;
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
                    if (pos == null) continue;
                    if (calculerDistance(pos, tile) <= unit.getVisionRange()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private int hexDistance(HexagonTile a, HexagonTile b) {
        int colA = a.getCol();
        int rowA = a.getRow() - (a.getCol() - (a.getCol() & 1)) / 2;

        int colB = b.getCol();
        int rowB = b.getRow() - (b.getCol() - (b.getCol() & 1)) / 2;

        int dx = colA - colB;
        int dy = rowA - rowB;

        return (Math.abs(dx) + Math.abs(dy) + Math.abs(dx + dy)) / 2;
    }

    private int calculerDistance(HexagonTile a, HexagonTile b) {
        return hexDistance(a, b);
    }

    public TerrainType getTerrain(int row, int col) {
        return terrainGrid[row][col];
    }

    public int getMovementCost(HexagonTile tile) {
        if (tile == null) return Integer.MAX_VALUE;
        TerrainType type = getTerrain(tile.getRow(), tile.getCol());

        switch (type) {
            case PLAINE: return 1;
            case FORET: return 2;
            case COLLINE: return 2;
            case FORTERESSE: return 1;
            case MONTAGNE: return 3;
            case EAU: return 999;
            default: return 1;
        }
    }

    public int calculateMovementCostPath(HexagonTile from, HexagonTile to) {
        return getMovementCost(to);
    }

    public List<HexagonTile> getReachableTiles(Unit unit) {
        List<HexagonTile> reachable = new ArrayList<>();
        Map<HexagonTile, Integer> costMap = new HashMap<>();
        Queue<HexagonTile> queue = new LinkedList<>();

        HexagonTile start = unit.getPosition();
        costMap.put(start, 0);
        queue.add(start);

        while (!queue.isEmpty()) {
            HexagonTile current = queue.poll();
            int currentCost = costMap.get(current);

            for (HexagonTile neighbor : getAdjacentTiles(current)) {
                int moveCost = getMovementCost(neighbor);
                if (moveCost >= 999) continue;

                int newCost = currentCost + moveCost;
                if (newCost <= unit.getCurrentMovement()
                        && (!costMap.containsKey(neighbor) || newCost < costMap.get(neighbor))) {
                    costMap.put(neighbor, newCost);
                    reachable.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        return reachable;
    }

    // ✅ Nouvelle méthode : retourne les 6 voisins d’une tuile
    public List<HexagonTile> getAdjacentTiles(HexagonTile from) {
        List<HexagonTile> neighbors = new ArrayList<>();

        int row = from.getRow();
        int col = from.getCol();

        int[][] offsetsEven = {{-1, 0}, {-1, -1}, {0, -1}, {1, 0}, {0, 1}, {-1, 1}};
        int[][] offsetsOdd  = {{-1, 0}, {1, -1}, {0, -1}, {1, 0}, {1, 1}, {0, 1}};
        int[][] offsets = (col % 2 == 0) ? offsetsEven : offsetsOdd;

        for (int[] offset : offsets) {
            int newRow = row + offset[0];
            int newCol = col + offset[1];

            HexagonTile neighbor = getCase(newRow, newCol);
            if (neighbor != null) {
                neighbors.add(neighbor);
            }
        }

        return neighbors;
    }
}
