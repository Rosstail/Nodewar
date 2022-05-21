package fr.rosstail.nodewar.territory.zonehandlers.objective;

import fr.rosstail.nodewar.territory.zonehandlers.Territory;
import fr.rosstail.nodewar.territory.zonehandlers.objective.objectives.ControlPoint;
import fr.rosstail.nodewar.territory.zonehandlers.objective.objectives.KingOfTheHill;
import fr.rosstail.nodewar.territory.zonehandlers.objective.objectives.Struggle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class ObjectiveManager {
    public static Map<String, Class<? extends Objective>> objectivesTypes = new HashMap<>();

    static {
        objectivesTypes.put("control", ControlPoint.class);
        objectivesTypes.put("struggle", Struggle.class);
        objectivesTypes.put("koth", KingOfTheHill.class);
    }

    /**
     * Add custom objective from add-ons
     * @param name
     * @param type
     * @return
     */
    public static boolean addCustomObjectiveType(String name, Class<? extends Objective> type) {
        if (!objectivesTypes.containsKey(name)) {
            objectivesTypes.put(name, type);
            System.out.println("[Nodewar] Objective type " + name + " added to the list !");
            return true;
        }
        return false;
    }

    public static void setUpObjective(Territory territory) {
        FileConfiguration config = territory.getConfig();
        ConfigurationSection objectiveSection = config.getConfigurationSection(territory.getName() + ".options.objective");
        Objective objective = null;

        if (objectiveSection != null) {
            String objectiveType = objectiveSection.getString(".type");
            if (objectiveType != null) {
                for (Map.Entry<String, Class<? extends Objective>> entry : objectivesTypes.entrySet()) {
                    String s = entry.getKey();
                    Class<? extends Objective> aClass = entry.getValue();
                    if (objectiveType.equalsIgnoreCase(s)) {
                        try {
                            objective = aClass.getDeclaredConstructor(Territory.class).newInstance(territory);
                            objective.start();
                            break;
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        territory.setObjective(objective);
    }

    public static Map<String, Class<? extends Objective>> getObjectivesTypes() {
        return objectivesTypes;
    }
}