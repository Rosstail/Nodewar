package fr.rosstail.nodewar.territory.battle;

import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.battle.types.BattleControl;
import fr.rosstail.nodewar.territory.battle.types.BattleKeep;
import fr.rosstail.nodewar.territory.battle.types.BattleKoth;
import fr.rosstail.nodewar.territory.battle.types.BattleSiege;
import fr.rosstail.nodewar.territory.objective.ObjectiveModel;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class BattleManager {
    public static BattleManager battleManager;
    public static Map<String, Class<? extends Battle>> battleClassMap = new HashMap<>();
    private Nodewar plugin;

    public BattleManager(Nodewar plugin) {
        this.plugin = plugin;
    }

    public static void init(Nodewar plugin) {
        if (battleManager == null) {
            battleManager = new BattleManager(plugin);
        }
    }

    static {
        battleClassMap.put("control", BattleControl.class);
        battleClassMap.put("siege", BattleSiege.class);
        battleClassMap.put("koth", BattleKoth.class);
        battleClassMap.put("keep", BattleKeep.class);
    }

    public boolean canAddCustomBattle(String name) {
        return !battleClassMap.containsKey(name);
    }

    /**
     * Add custom battle from add-ons
     * @param name
     * @param customBattleClass
     * @return
     */
    public void addCustomBattle(String name, Class<? extends Battle> customBattleClass) {
        battleClassMap.put(name, customBattleClass);
        AdaptMessage.print("[Nodewar] Custom battle " + name + " added to the list !", AdaptMessage.prints.OUT);
    }

    public void setUpBattle(Territory territory, String objectiveName) {
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
            territory.setCurrentBattle(new Battle(territory));
        }
    }

    public static Map<String, Class<? extends Battle>> getBattleClassMap() {
        return battleClassMap;
    }

    public static BattleManager getBattleManager() {
        return battleManager;
    }
}