package fr.rosstail.nodewar.territory.objective;

import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.objective.types.*;
import fr.rosstail.nodewar.territory.TerritoryType;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class ObjectiveManager {
    public static Map<String, Map.Entry<Class<? extends Objective>, Class<? extends ObjectiveModel>>> objectiveEntryMap = new HashMap<>();

    static {
        objectiveEntryMap.put("control", new AbstractMap.SimpleEntry<>(ObjectiveControl.class, ObjectiveControlModel.class));
        objectiveEntryMap.put("siege", new AbstractMap.SimpleEntry<>(ObjectiveSiege.class, ObjectiveSiegeModel.class));
        objectiveEntryMap.put("koth", new AbstractMap.SimpleEntry<>(ObjectiveKoth.class, ObjectiveKothModel.class));
    }

    /**
     * Add custom objective from add-ons
     * @param name
     * @param customObjectiveClass
     * @return
     */
    public static boolean addCustomObjective(String name, Class<? extends Objective> customObjectiveClass, Class<? extends ObjectiveModel> customObjectiveModelClass) {
        if (!objectiveEntryMap.containsKey(name)) {
            objectiveEntryMap.put(name, new AbstractMap.SimpleEntry<>(customObjectiveClass, customObjectiveModelClass));
            AdaptMessage.print("[Nodewar] Custom objective " + name + " added to the list !", AdaptMessage.prints.OUT);
            return true;
        }
        return false;
    }

    public static void setupObjectiveModelToTerritoryType(TerritoryType territoryType, TerritoryType parentType, String objectiveName, ConfigurationSection objectiveSection) {
        if (objectiveEntryMap.containsKey(objectiveName)) {
            Map.Entry<Class<? extends Objective>, Class<? extends ObjectiveModel>> entry = objectiveEntryMap.get(objectiveName);
            Class<? extends ObjectiveModel> objectiveModelClass = entry.getValue();

            Constructor<? extends ObjectiveModel> objectiveModelConstructor;
            Constructor<? extends ObjectiveModel> objectiveModelConstructorSingle;

            Constructor<? extends ObjectiveModel> objectiveChildModelConstructor;
            try {
                objectiveChildModelConstructor = objectiveModelClass.getDeclaredConstructor(ConfigurationSection.class);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("Missing appropriate constructor in ObjectiveModel class.", e);
            }

            ObjectiveModel objectiveChildModel;
            try {
                objectiveChildModel = objectiveChildModelConstructor.newInstance(objectiveSection);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }

            try {
                objectiveModelConstructor = objectiveModelClass.getDeclaredConstructor(entry.getValue(), entry.getValue());
                objectiveModelConstructorSingle = objectiveModelClass.getDeclaredConstructor(ConfigurationSection.class);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("Missing appropriate constructor in ObjectiveModel class.", e);
            }

            try {
                if (parentType != null) {
                    territoryType.setObjectiveModel(objectiveModelConstructor.newInstance(objectiveChildModel, parentType.getObjectiveModel()));
                } else {
                    territoryType.setObjectiveModel(objectiveModelConstructorSingle.newInstance(objectiveSection));
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            AdaptMessage.print("[Nodewar] Custom objective model " + objectiveName + " not found!", AdaptMessage.prints.ERROR);
            territoryType.setObjectiveModel(new ObjectiveModel(null));
        }
    }

    public static void setUpObjectiveToTerritory(Territory territory, ConfigurationSection objectiveSection, String objectiveName) {
        if (objectiveEntryMap.containsKey(objectiveName)) {
            Map.Entry<Class<? extends Objective>, Class<? extends ObjectiveModel>> entry = objectiveEntryMap.get(objectiveName);
            Class<? extends Objective> objectiveClass = entry.getKey();
            Class<? extends ObjectiveModel> objectiveModelClass = entry.getValue();

            Constructor<? extends Objective> objectiveConstructor;
            try {
                objectiveConstructor = objectiveClass.getDeclaredConstructor(Territory.class, entry.getValue(), entry.getValue());
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("Missing appropriate constructor in Objective class.", e);
            }

            Constructor<? extends ObjectiveModel> objectiveChildModelConstructor;
            try {
                objectiveChildModelConstructor = objectiveModelClass.getDeclaredConstructor(ConfigurationSection.class);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("Missing appropriate constructor in ObjectiveModel class.", e);
            }

            ObjectiveModel objectiveChildModel;
            try {
                objectiveChildModel = objectiveChildModelConstructor.newInstance(objectiveSection).clone();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }

            ObjectiveModel objectiveParentModel = objectiveModelClass.cast(territory.getTerritoryType().getObjectiveModel()).clone();

            try {
                Objective objective = objectiveConstructor.newInstance(territory, objectiveChildModel, objectiveParentModel);
                territory.setObjective(objective);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            AdaptMessage.print("[Nodewar] Custom objective " + objectiveName + " not found!", AdaptMessage.prints.ERROR);
        }
    }

    public static Map<String, Map.Entry<Class<? extends Objective>, Class<? extends ObjectiveModel>>> getObjectiveEntryMap() {
        return objectiveEntryMap;
    }
}