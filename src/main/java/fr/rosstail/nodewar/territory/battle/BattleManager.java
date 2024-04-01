package fr.rosstail.nodewar.territory.battle;

import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.battle.types.BattleControl;
import fr.rosstail.nodewar.territory.battle.types.BattleKoth;
import fr.rosstail.nodewar.territory.battle.types.BattleSiege;
import fr.rosstail.nodewar.territory.objective.ObjectiveModel;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class BattleManager {
    public static Map<String, Class<? extends Battle>> battleClassMap = new HashMap<>();

    static {
        battleClassMap.put("control", BattleControl.class);
        battleClassMap.put("siege", BattleSiege.class);
        battleClassMap.put("koth", BattleKoth.class);
    }

    /**
     * Add custom battle from add-ons
     * @param name
     * @param customBattleClass
     * @return
     */
    public static boolean addCustomBattle(String name, Class<? extends Battle> customBattleClass) {
        if (!battleClassMap.containsKey(name)) {
            battleClassMap.put(name, customBattleClass);
            AdaptMessage.print("[Nodewar] Custom battle " + name + " added to the list !", AdaptMessage.prints.OUT);
            return true;
        }
        return false;
    }

    public static void setUpBattle(Territory territory, String objectiveName) {
        if (battleClassMap.containsKey(objectiveName)) {
            Class<? extends Battle> battleClass = battleClassMap.get(objectiveName);

            Constructor<? extends Battle> battleConstructor;
            try {
                // Obtenez le constructeur approprié de Objective
                battleConstructor = battleClass.getDeclaredConstructor(Territory.class);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("Missing appropriate constructor in Battle class.", e);
            }

            try {
                // Instanciez une instance de Battle en passant Territory
                territory.setCurrentBattle(battleConstructor.newInstance(territory));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            AdaptMessage.print("[Nodewar] Custom objective " + objectiveName + " not found!", AdaptMessage.prints.ERROR);
        }
    }

    public static Map<String, Class<? extends Battle>> getBattleClassMap() {
        return battleClassMap;
    }
}