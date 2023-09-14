package fr.rosstail.nodewar.territory.objective.reward;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class RewardModel implements Cloneable {

    private List<String> commandStringList = new ArrayList<>();

    public RewardModel(ConfigurationSection section) {
        if (section != null) {
            this.commandStringList.addAll(section.getStringList("commands.default.global-reward-commands"));
        }
    }

    public RewardModel(RewardModel childRewardModel, RewardModel parentRewardModel) {
        RewardModel cloneChildRewardModel = childRewardModel.clone();
        RewardModel cloneParentRewardModel = parentRewardModel.clone();

        if (!cloneChildRewardModel.getCommandStringList().isEmpty() || !cloneParentRewardModel.getCommandStringList().isEmpty()) {
            commandStringList.addAll(!cloneChildRewardModel.getCommandStringList().isEmpty() ? cloneChildRewardModel.getCommandStringList() : cloneParentRewardModel.getCommandStringList());
        }
    }

    public List<String> getCommandStringList() {
        return commandStringList;
    }

    public void setCommandStringList(List<String> commandStringList) {
        this.commandStringList = commandStringList;
    }

    @Override
    public RewardModel clone() {
        try {
            RewardModel clone = (RewardModel) super.clone();
            clone.setCommandStringList(new ArrayList<>(getCommandStringList()));

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
