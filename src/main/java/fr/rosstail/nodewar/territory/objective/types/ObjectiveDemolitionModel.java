package fr.rosstail.nodewar.territory.objective.types;

import fr.rosstail.nodewar.territory.objective.ObjectiveModel;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ObjectiveDemolitionModel extends ObjectiveModel {
    private String blockStartStr;
    private String blockRatioStartStr;
    private String blockLoseStr;
    private String blockRatioLoseStr;

    private final Set<String> sideBlockStrSet = new HashSet<>();

    private String countAirStr;
    private String countLiquidsStr;
    private String countSolidStr;

    private String durationStr;


    public ObjectiveDemolitionModel(ConfigurationSection section) {
        super(section);
        if (section != null) {
            this.blockStartStr = section.getString("start-block-threshold", "0");
            this.blockRatioStartStr = section.getString("start-ratio-threshold", "0");
            this.blockLoseStr = section.getString("lose-block-threshold", "0");
            this.blockRatioLoseStr = section.getString("lose-ratio-threshold", "0");

            if (section.isList("blocks-patterns")) {
                sideBlockStrSet.addAll(section.getStringList("blocks-patterns"));
            }

            countAirStr = section.getString("count-air");
            countLiquidsStr = section.getString("count-liquids");
            countSolidStr = section.getString("count-solid");
            durationStr = section.getString("duration");
        }
    }

    public ObjectiveDemolitionModel(ObjectiveDemolitionModel childObjectiveModel, @NotNull ObjectiveDemolitionModel parentObjectiveModel) {
        super(childObjectiveModel, parentObjectiveModel);

        this.blockStartStr = childObjectiveModel.blockStartStr != null ? childObjectiveModel.blockStartStr : parentObjectiveModel.blockStartStr;
        this.blockRatioStartStr = childObjectiveModel.blockRatioStartStr != null ? childObjectiveModel.blockRatioStartStr : parentObjectiveModel.blockRatioStartStr;
        this.blockLoseStr = childObjectiveModel.blockLoseStr != null ? childObjectiveModel.blockLoseStr : parentObjectiveModel.blockLoseStr;
        this.blockRatioLoseStr = childObjectiveModel.blockRatioLoseStr != null ? childObjectiveModel.blockRatioLoseStr : parentObjectiveModel.blockRatioLoseStr;

        this.sideBlockStrSet.addAll(childObjectiveModel.sideBlockStrSet);
        this.sideBlockStrSet.addAll(parentObjectiveModel.sideBlockStrSet);

        this.countAirStr = childObjectiveModel.countAirStr != null ? childObjectiveModel.countAirStr : parentObjectiveModel.countAirStr;
        this.countLiquidsStr = childObjectiveModel.countLiquidsStr != null ? childObjectiveModel.countLiquidsStr : parentObjectiveModel.countAirStr;
        this.countSolidStr = childObjectiveModel.countSolidStr != null ? childObjectiveModel.countSolidStr : parentObjectiveModel.countAirStr;

        this.durationStr = childObjectiveModel.durationStr != null ? childObjectiveModel.durationStr : parentObjectiveModel.durationStr;
    }

    public Set<String> getSideBlockStrSet() {
        return sideBlockStrSet;
    }

    public String getBlockLoseStr() {
        return blockLoseStr;
    }

    public String getBlockRatioLoseStr() {
        return blockRatioLoseStr;
    }

    public String getBlockRatioStartStr() {
        return blockRatioStartStr;
    }

    public String getBlockStartStr() {
        return blockStartStr;
    }

    public String getCountAirStr() {
        return countAirStr;
    }

    public String getCountLiquidsStr() {
        return countLiquidsStr;
    }

    public String getCountSolidStr() {
        return countSolidStr;
    }

    public String getDurationStr() {
        return durationStr;
    }
}
