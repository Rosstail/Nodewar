package fr.rosstail.nodewar.territory.zonehandlers.objective;

import fr.rosstail.nodewar.territory.zonehandlers.objective.objectives.ControlPoint;
import fr.rosstail.nodewar.territory.zonehandlers.objective.objectives.KingOfTheHill;
import fr.rosstail.nodewar.territory.zonehandlers.objective.objectives.Struggle;

import java.util.HashMap;
import java.util.Map;

public class ObjectiveManager {
    public static Map<String, Class> objectivesTypes = new HashMap<>();

    static {
        objectivesTypes.put("control", ControlPoint.class);
        objectivesTypes.put("struggle", Struggle.class);
        objectivesTypes.put("koth", KingOfTheHill.class);
    }

    public static boolean addCustomObjectiveType(String name, Class type) {
        if (!objectivesTypes.containsKey(name)) {
            objectivesTypes.put(name, type);
            System.out.println("[Nodewar] Objective type " + name + " added to the list !");
            return true;
        }
        return false;
    }

    public static Map<String, Class> getObjectivesTypes() {
        return objectivesTypes;
    }
}
