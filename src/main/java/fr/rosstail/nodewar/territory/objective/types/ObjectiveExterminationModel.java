package fr.rosstail.nodewar.territory.objective.types;

import fr.rosstail.nodewar.territory.objective.ObjectiveModel;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ObjectiveExterminationModel extends ObjectiveModel {
    private Set<String> sideStrSet = new HashSet<>();
    private String durationStr;
    private String ignoreUnownedSidesStr;


    public ObjectiveExterminationModel(ConfigurationSection section) {
        super(section);
        if (section != null) {

            if (section.isConfigurationSection("sides")) {
                ConfigurationSection sidesConfigSection = section.getConfigurationSection("sides");

                sideStrSet.addAll(sidesConfigSection.getKeys(false));
            }
            durationStr = section.getString("duration");
            ignoreUnownedSidesStr = section.getString("ignore-unowned-sides");
        }
    }

    public ObjectiveExterminationModel(ObjectiveExterminationModel childObjectiveModel, @NotNull ObjectiveExterminationModel parentObjectiveModel) {
        super(childObjectiveModel, parentObjectiveModel);

        this.sideStrSet.addAll(parentObjectiveModel.getSideStrSet());
        this.sideStrSet.addAll(childObjectiveModel.getSideStrSet());

        this.durationStr = childObjectiveModel.durationStr != null ? childObjectiveModel.durationStr : parentObjectiveModel.durationStr;
        this.ignoreUnownedSidesStr = childObjectiveModel.ignoreUnownedSidesStr != null ? childObjectiveModel.ignoreUnownedSidesStr : parentObjectiveModel.ignoreUnownedSidesStr;
    }

    public Set<String> getSideStrSet() {
        return sideStrSet;
    }

    public void setSideStrSet(Set<String> SideStrSet) {
        this.sideStrSet = SideStrSet;
    }

    public String getDurationStr() {
        return durationStr;
    }

    public void setDurationStr(String durationStr) {
        this.durationStr = durationStr;
    }

    public String getIgnoreUnownedSidesStr() {
        return ignoreUnownedSidesStr;
    }

    public void setIgnoreUnownedSidesStr(String ignoreUnownedSidesStr) {
        this.ignoreUnownedSidesStr = ignoreUnownedSidesStr;
    }
}
