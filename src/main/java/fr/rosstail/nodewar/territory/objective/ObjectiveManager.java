package fr.rosstail.nodewar.territory.objective;

import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryModel;
import fr.rosstail.nodewar.territory.objective.types.*;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class ObjectiveManager {
    private static ObjectiveManager objectiveManager;
    public static Map<String, Map.Entry<Class<? extends NwObjective>, Class<? extends ObjectiveModel>>> objectiveEntryMap = new HashMap<>();
    private final Nodewar plugin;

    public ObjectiveManager(Nodewar plugin) {
        this.plugin = plugin;
    }

    public static void init(Nodewar plugin) {
        if (objectiveManager == null) {
            objectiveManager = new ObjectiveManager(plugin);
        }
    }

    static {
        objectiveEntryMap.put("control", new AbstractMap.SimpleEntry<>(ObjectiveControl.class, ObjectiveControlModel.class));
        objectiveEntryMap.put("demolition", new AbstractMap.SimpleEntry<>(ObjectiveDemolition.class, ObjectiveDemolitionModel.class));
        objectiveEntryMap.put("extermination", new AbstractMap.SimpleEntry<>(ObjectiveExtermination.class, ObjectiveExterminationModel.class));
        objectiveEntryMap.put("siege", new AbstractMap.SimpleEntry<>(ObjectiveSiege.class, ObjectiveSiegeModel.class));
        objectiveEntryMap.put("koth", new AbstractMap.SimpleEntry<>(ObjectiveKoth.class, ObjectiveKothModel.class));
        objectiveEntryMap.put("keep", new AbstractMap.SimpleEntry<>(ObjectiveKeep.class, ObjectiveKeepModel.class));
    }

    public static boolean canAddCustomObjective(String name) {
        return !objectiveEntryMap.containsKey(name);
    }

    /**
     * Add custom objective from add-ons
     *
     * @param name
     * @param customObjectiveClass
     * @return
     */
    public static void addCustomObjective(String name, Class<? extends NwObjective> customObjectiveClass, Class<? extends ObjectiveModel> customObjectiveModelClass) {
        objectiveEntryMap.put(name, new AbstractMap.SimpleEntry<>(customObjectiveClass, customObjectiveModelClass));
        AdaptMessage.print("[Nodewar] Custom objective " + name + " added to the list !", AdaptMessage.prints.OUT);
    }

    public void setupTerritoryObjectiveModel(TerritoryModel territoryPreset, TerritoryModel parentType, String objectiveName, ConfigurationSection objectiveSection) {
        if (objectiveEntryMap.containsKey(objectiveName)) {
            Map.Entry<Class<? extends NwObjective>, Class<? extends ObjectiveModel>> entry = objectiveEntryMap.get(objectiveName);
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
                if (parentType != null && parentType.getObjectiveModel() != null) {
                    territoryPreset.setObjectiveModel(objectiveModelConstructor.newInstance(objectiveChildModel, parentType.getObjectiveModel()));
                } else {
                    territoryPreset.setObjectiveModel(objectiveModelConstructorSingle.newInstance(objectiveSection));
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            territoryPreset.setObjectiveModel(new ObjectiveModel(objectiveSection));
        }
    }

    public NwObjective setUpObjective(Territory territory, String objectiveName) {
        if (objectiveEntryMap.containsKey(objectiveName)) {
            Map.Entry<Class<? extends NwObjective>, Class<? extends ObjectiveModel>> entry = objectiveEntryMap.get(objectiveName);
            Class<? extends NwObjective> objectiveClass = entry.getKey();
            Class<? extends ObjectiveModel> objectiveModelClass = entry.getValue();

            Constructor<? extends NwObjective> objectiveConstructor;
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
                objectiveChildModel = objectiveChildModelConstructor.newInstance((Object) null);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }

            ObjectiveModel objectiveParentModel = objectiveModelClass.cast(territory.getObjectiveModel());

            try {
                return objectiveConstructor.newInstance(territory, objectiveChildModel, objectiveParentModel);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        return new NwObjective(territory, null, null);
    }

    public static Map<String, Map.Entry<Class<? extends NwObjective>, Class<? extends ObjectiveModel>>> getObjectiveEntryMap() {
        return objectiveEntryMap;
    }

    public static ObjectiveManager getManager() {
        return objectiveManager;
    }
}