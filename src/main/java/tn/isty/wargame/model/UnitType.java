package tn.isty.wargame.model;

/**
 * Représente les différents types d’unités disponibles dans le jeu Wargame.
 * Chaque type peut avoir ses propres caractéristiques de déplacement, de combat et d’usage stratégique.
 */
public enum UnitType {

    /** Unités de base au corps-à-corps, souvent robustes mais peu mobiles. */
    INFANTERIE,

    /** Unités rapides, généralement faibles en défense mais efficaces pour les charges. */
    CAVALERIE,

    /** Unités à distance avec une forte puissance de feu mais faible défense. */
    ARTILLERIE,

    /** Unités de support pouvant soigner, réparer ou offrir des bonus. */
    SOUTIEN,

    /** Unités à distance légère, agiles, avec portée étendue mais peu de résistance. */
    ARCHER
}
