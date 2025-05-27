package tn.isty.wargame.model;

/**
 * Enumération représentant les différents types de terrain dans le jeu.
 * Chaque terrain possède un coût de déplacement associé (moveCost), 
 * utilisé pour déterminer combien de points de mouvement une unité doit dépenser pour le traverser.
 */
public enum TerrainType {
    
    /** Terrain de base, facile à traverser. */
    PLAINE(1),
    
    /** Terrain boisé, ralentit légèrement les déplacements. */
    FORET(2),
    
    /** Terrain très difficile d'accès (non utilisé ici, mais potentiellement exploitable). */
    MONTAGNE(3),
    
    /** Terrain vallonné, modérément difficile à traverser. */
    COLLINE(2),
    
    /** Terrain stratégique facile d'accès, souvent utilisé comme centre de contrôle. */
    FORTERESSE(1),
    
    /** Terrain infranchissable sauf pour certaines unités ou par voie navale. */
    EAU(999);

    /** Coût de déplacement pour traverser ce terrain. */
    private final int moveCost;

    /**
     * Constructeur de l'enum TerrainType.
     *
     * @param moveCost Le coût de déplacement associé à ce type de terrain.
     */
    TerrainType(int moveCost) {
        this.moveCost = moveCost;
    }

    /**
     * Retourne le coût de déplacement associé à ce terrain.
     *
     * @return coût de déplacement en points de mouvement
     */
    public int getMoveCost() {
        return moveCost;
    }
}
