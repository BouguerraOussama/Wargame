package tn.isty.wargame.model;

import java.util.List;

public interface ArmyFactory {
    List<Unit> createArmy(Player player);
}
