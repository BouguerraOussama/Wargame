package tn.isty.wargame.model;

public class ArmyFactoryProvider {
    public static ArmyFactory getFactory(ArmyType type) {
        return switch (type) {
            case FRANCE -> new FrenchArmyFactory();
            case ALLEMAGNE -> new GermanArmyFactory();
            case ROYAUME_UNI -> new BritishArmyFactory();
        };
    }
}
