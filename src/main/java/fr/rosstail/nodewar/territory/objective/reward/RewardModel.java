package fr.rosstail.nodewar.territory.objective.reward;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class RewardModel implements Cloneable {

    private String name;
    private String targetName; // Server (default) / Teams / Players
    private String minimumTeamScoreStr;
    private String minimumPlayerScoreStr;
    private String teamRole; // Any / Attacker / Defender
    private String playerTeamRole; // Any / Attacker / Defender

    private String shouldTeamWinStr; // Any / true / false

    private List<Integer> teamPositions = new ArrayList<>(); // 0 (Any), 1, 2 etc...
    private List<String> commandList = new ArrayList<>();

    public RewardModel(ConfigurationSection section) {
        if (section != null) {
            this.name = section.getName();
            this.targetName = section.getString("target");
            this.minimumTeamScoreStr = section.getString("team-minimum-score");
            this.minimumPlayerScoreStr = section.getString("player-minimum-score");
            this.teamRole = section.getString("team-role");
            this.playerTeamRole = section.getString("player-team-role");
            this.shouldTeamWinStr = section.getString("should-team-win");
            this.teamPositions.addAll(section.getIntegerList("team-positions"));
            this.commandList.addAll(section.getStringList("commands"));
        }
    }

    public RewardModel(RewardModel childRewardModel, RewardModel parentRewardModel) {
        RewardModel cloneChildRewardModel = childRewardModel.clone();
        RewardModel cloneParentRewardModel = parentRewardModel.clone();

        this.name = childRewardModel.getName();
        this.targetName = cloneChildRewardModel.getTargetName() != null ? cloneChildRewardModel.getTargetName() : cloneParentRewardModel.getTargetName();
        this.minimumTeamScoreStr = cloneChildRewardModel.getMinimumTeamScoreStr() != null ? cloneChildRewardModel.getMinimumTeamScoreStr() : cloneParentRewardModel.getMinimumTeamScoreStr();
        this.minimumTeamScoreStr = cloneChildRewardModel.getMinimumPlayerScoreStr() != null ? cloneChildRewardModel.getMinimumPlayerScoreStr() : cloneParentRewardModel.getMinimumPlayerScoreStr();
        this.playerTeamRole = cloneChildRewardModel.getTeamRole() != null ? cloneChildRewardModel.getTeamRole() : cloneParentRewardModel.getTeamRole();
        this.shouldTeamWinStr = cloneChildRewardModel.getShouldTeamWinStr() != null ? cloneChildRewardModel.getShouldTeamWinStr() : cloneParentRewardModel.getShouldTeamWinStr();

        if (!cloneChildRewardModel.getTeamPositions().isEmpty() || !cloneParentRewardModel.getTeamPositions().isEmpty()) {
            teamPositions.addAll(!cloneChildRewardModel.getTeamPositions().isEmpty() ? cloneChildRewardModel.getTeamPositions() : cloneParentRewardModel.getTeamPositions());
        }

        if (!cloneChildRewardModel.getCommandList().isEmpty() || !cloneParentRewardModel.getCommandList().isEmpty()) {
            commandList.addAll(!cloneChildRewardModel.getCommandList().isEmpty() ? cloneChildRewardModel.getCommandList() : cloneParentRewardModel.getCommandList());
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getMinimumTeamScoreStr() {
        return minimumTeamScoreStr;
    }

    public void setMinimumTeamScoreStr(String minimumTeamScoreStr) {
        this.minimumTeamScoreStr = minimumTeamScoreStr;
    }

    public String getMinimumPlayerScoreStr() {
        return minimumPlayerScoreStr;
    }

    public void setMinimumPlayerScoreStr(String minimumPlayerScoreStr) {
        this.minimumPlayerScoreStr = minimumPlayerScoreStr;
    }

    public String getTeamRole() {
        return teamRole;
    }

    public void setTeamRole(String teamRole) {
        this.teamRole = teamRole;
    }

    public String getPlayerTeamRole() {
        return playerTeamRole;
    }

    public void setPlayerTeamRole(String playerTeamRole) {
        this.playerTeamRole = playerTeamRole;
    }

    public String getShouldTeamWinStr() {
        return shouldTeamWinStr;
    }

    public void setShouldTeamWinStr(String shouldTeamWinStr) {
        this.shouldTeamWinStr = shouldTeamWinStr;
    }

    public List<Integer> getTeamPositions() {
        return teamPositions;
    }

    public void setTeamPositions(List<Integer> teamPositions) {
        this.teamPositions = teamPositions;
    }

    public List<String> getCommandList() {
        return commandList;
    }

    public void setCommandList(List<String> commandList) {
        this.commandList = commandList;
    }

    @Override
    public RewardModel clone() {
        try {
            RewardModel clone = (RewardModel) super.clone();
            clone.setName(clone.getName());
            clone.setTargetName(clone.getTargetName());
            clone.setMinimumTeamScoreStr(getMinimumTeamScoreStr());
            clone.setMinimumPlayerScoreStr(getMinimumPlayerScoreStr());
            clone.setTeamRole(clone.getTeamRole());
            clone.setPlayerTeamRole(clone.getPlayerTeamRole());
            clone.setShouldTeamWinStr(getShouldTeamWinStr());
            clone.setTeamPositions(new ArrayList<>(getTeamPositions()));
            clone.setCommandList(new ArrayList<>(getCommandList()));

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
