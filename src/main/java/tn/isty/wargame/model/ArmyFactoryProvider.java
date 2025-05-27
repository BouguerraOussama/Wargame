package tn.isty.wargame.model;

/**
 * Fournisseur de fabriques d'armées (Factory Provider).
 * Cette classe agit comme un point central pour récupérer
 * la fabrique correspondante au type d'armée sélectionné.
 */
public class ArmyFactoryProvider {

    /**
     * Retourne une implémentation concrète de {@link ArmyFactory}
     * correspondant au type d'armée spécifié.
     *
     * @param type Le type d’armée choisi (FRANCE, ALLEMAGNE, ROYAUME_UNI).
     * @return Une instance de {@link ArmyFactory} adaptée à ce type.
     */
    public static ArmyFactory getFactory(ArmyType type) {
        // Utilisation du switch expression (Java 14+) pour retourner la bonne fabrique
        return switch (type) {
            case FRANCE -> new FrenchArmyFactory();
            case ALLEMAGNE -> new GermanArmyFactory();
            case ROYAUME_UNI -> new BritishArmyFactory();
        };
    }
}