package tn.isty.wargame.model;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;

public class HexGridTest extends Application {

    private static final double TILE_SIZE = 30; // rayon hexagone
    private Plateau plateau;

    public enum TerrainType {
        PLAINE,
        FORET,
        MONTAGNE,
        COLLINE,
        FORTERESSE,
        EAU,
        DESERT
    }

    public enum PlateauType {
        VILLE,
        ILE,
        DESERT
    }

    public class Plateau {
        private int rows;
        private int cols;
        private TerrainType[][] terrainMap;

        public Plateau(int cols, int rows) {
            this.cols = cols;
            this.rows = rows;
            terrainMap = new TerrainType[cols][rows];
        }

        public void generate(PlateauType type) {
            switch (type) {
                case VILLE:
                    generateVille();
                    break;
                case ILE:
                    generateIsland();
                    break;
                case DESERT:
                    generateDesert();
                    break;
                default:
                    fillDefault();
                    break;
            }
        }

        private void fillDefault() {
            for (int x = 0; x < cols; x++) {
                for (int y = 0; y < rows; y++) {
                    terrainMap[x][y] = TerrainType.PLAINE;
                }
            }
        }

        private void generateVille() {
            fillDefault();
            int centerX = cols / 2;
            int centerY = rows / 2;

            for (int x = centerX - 3; x <= centerX + 3; x++) {
                for (int y = centerY - 3; y <= centerY + 3; y++) {
                    if (x >= 0 && x < cols && y >= 0 && y < rows) {
                        terrainMap[x][y] = TerrainType.FORTERESSE;
                    }
                }
            }
            for (int x = 0; x < centerX - 3; x++) {
                for (int y = 0; y < rows; y++) {
                    terrainMap[x][y] = TerrainType.FORET;
                }
            }
            for (int x = centerX + 4; x < cols; x++) {
                for (int y = 0; y < rows; y++) {
                    terrainMap[x][y] = TerrainType.MONTAGNE;
                }
            }
            for (int x = centerX - 3; x <= centerX + 3; x++) {
                for (int y = 0; y < centerY - 4; y++) {
                    terrainMap[x][y] = TerrainType.COLLINE;
                }
            }
            int riverX = centerX;
            for (int y = centerY; y < centerY + 5 && y < rows; y++) {
                terrainMap[riverX][y] = TerrainType.EAU;
            }
        }

        private void generateIsland() {
            for (int x = 0; x < cols; x++) {
                for (int y = 0; y < rows; y++) {
                    if (x < 2 || x >= cols - 2 || y < 2 || y >= rows - 2) {
                        terrainMap[x][y] = TerrainType.EAU;
                    } else {
                        terrainMap[x][y] = TerrainType.PLAINE;
                    }
                }
            }
            int centerX = cols / 2;
            int centerY = rows / 2;
            for (int x = centerX - 3; x <= centerX + 3; x++) {
                for (int y = centerY - 3; y <= centerY + 3; y++) {
                    terrainMap[x][y] = TerrainType.FORET;
                }
            }
        }

        private void generateDesert() {
            for (int x = 0; x < cols; x++) {
                for (int y = 0; y < rows; y++) {
                    terrainMap[x][y] = TerrainType.COLLINE;
                }
            }
            int centerX = cols / 2;
            int centerY = rows / 2;
            for (int x = centerX - 2; x <= centerX + 2; x++) {
                for (int y = centerY - 2; y <= centerY + 2; y++) {
                    terrainMap[x][y] = TerrainType.EAU;
                }
            }
            for (int x = cols - 5; x < cols; x++) {
                for (int y = rows / 3; y < rows * 2 / 3; y++) {
                    terrainMap[x][y] = TerrainType.MONTAGNE;
                }
            }
        }

        public TerrainType getTerrainAt(int col, int row) {
            if (col < 0 || col >= cols || row < 0 || row >= rows) return null;
            return terrainMap[col][row];
        }

        public int getCols() {
            return cols;
        }

        public int getRows() {
            return rows;
        }
    }

    private Color getColorForTerrain(TerrainType type) {
        switch (type) {
            case PLAINE:
                return Color.LIGHTGREEN;
            case FORET:
                return Color.DARKGREEN;
            case MONTAGNE:
                return Color.DIMGRAY;
            case COLLINE:
                return Color.SANDYBROWN;
            case FORTERESSE:
                return Color.DARKRED;
            case EAU:
                return Color.BLUE;
            case DESERT:
                return Color.KHAKI;
            default:
                return Color.GRAY;
        }
    }

    private Polygon createHexagon(double radius) {
        Polygon hex = new Polygon();
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(60 * i);
            double x = radius * Math.cos(angle);
            double y = radius * Math.sin(angle);
            hex.getPoints().addAll(x, y);
        }
        return hex;
    }


    @Override
    public void start(Stage stage) {
        // Obtenir la résolution de l'écran
        double screenWidth = javafx.stage.Screen.getPrimary().getBounds().getWidth();
        double screenHeight = javafx.stage.Screen.getPrimary().getBounds().getHeight();

        // Dimensions d’un hexagone
        double hexWidth = 2 * TILE_SIZE;
        double hexHeight = Math.sqrt(3) * TILE_SIZE;

        // Calcul du nombre de colonnes et de lignes selon la taille de l'écran
        int cols = (int) (screenWidth / (hexWidth * 0.75));
        int rows = (int) (screenHeight / hexHeight);

        plateau = new Plateau(cols, rows);
        plateau.generate(PlateauType.DESERT);

        Pane root = new Pane();

        for (int col = 0; col < cols; col++) {
            for (int row = 0; row < rows; row++) {
                TerrainType terrain = plateau.getTerrainAt(col, row);
                if (terrain == null) continue;

                Polygon hex = createHexagon(TILE_SIZE);

                double x = col * hexWidth * 0.75;
                double y = row * hexHeight + (col % 2) * (hexHeight / 2);

                hex.setLayoutX(x);
                hex.setLayoutY(y);

                hex.setFill(getColorForTerrain(terrain));
                hex.setStroke(Color.BLACK);

                root.getChildren().add(hex);
            }
        }




        for (int col = 0; col < cols; col++) {
            for (int row = 0; row < rows; row++) {
                TerrainType terrain = plateau.getTerrainAt(col, row);
                if (terrain == null) continue;

                Polygon hex = createHexagon(TILE_SIZE);

                double x = col * hexWidth * 0.75;
                double y = row * hexHeight + (col % 2) * (hexHeight / 2);

                hex.setLayoutX(x);
                hex.setLayoutY(y);

                hex.setFill(getColorForTerrain(terrain));
                hex.setStroke(Color.BLACK);

                root.getChildren().add(hex);
            }
        }

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Hex Grid - Plateau : " + PlateauType.ILE);
        stage.setFullScreen(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
