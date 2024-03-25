package fr.rosstail.nodewar.territory.objective.reward;

import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.RelationType;
import fr.rosstail.nodewar.team.relation.TeamRelationManager;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.battle.Battle;
import fr.rosstail.nodewar.territory.objective.Objective;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class Reward {
    private RewardModel rewardModel;
    private boolean hasPlayerMinimumScore;
    private long playerMinimumScore;
    private boolean hasTeamMinimumScore;
    private long teamMinimumScore;

    private boolean shouldTeamWin;

    public Reward(RewardModel model) {
        this.rewardModel = model;
        this.hasPlayerMinimumScore = model.getMinimumPlayerScoreStr() != null;
        this.playerMinimumScore = model.getMinimumPlayerScoreStr() != null ? Long.parseLong(model.getMinimumPlayerScoreStr()) : 0L;
        this.hasTeamMinimumScore = model.getMinimumTeamScoreStr() != null;
        this.teamMinimumScore = model.getMinimumTeamScoreStr() != null ? Long.parseLong(model.getMinimumTeamScoreStr()) : 0L;
        this.shouldTeamWin = Boolean.parseBoolean(model.getShouldTeamWinStr());
    }

    public Reward(RewardModel childModel, @NotNull RewardModel parentModel) {
        RewardModel clonedParentModel = parentModel.clone();
        if (childModel != null) {
            RewardModel clonedChildModel = childModel.clone();
            this.rewardModel = new RewardModel(clonedChildModel, clonedParentModel);
        } else {
            this.rewardModel = clonedParentModel;
        }
        this.hasPlayerMinimumScore = rewardModel.getMinimumPlayerScoreStr() != null;
        this.playerMinimumScore = Long.parseLong(rewardModel.getMinimumPlayerScoreStr());
        this.hasTeamMinimumScore = rewardModel.getMinimumTeamScoreStr() != null;
        this.teamMinimumScore = Long.parseLong(rewardModel.getMinimumTeamScoreStr());
        this.shouldTeamWin = Boolean.parseBoolean(rewardModel.getShouldTeamWinStr());
    }

    public RewardModel getRewardModel() {
        return rewardModel;
    }

    public void setRewardModel(RewardModel rewardModel) {
        this.rewardModel = rewardModel;
    }


    public boolean isHasPlayerMinimumScore() {
        return hasPlayerMinimumScore;
    }

    public void setHasPlayerMinimumScore(boolean hasPlayerMinimumScore) {
        this.hasPlayerMinimumScore = hasPlayerMinimumScore;
    }

    public long getPlayerMinimumScore() {
        return playerMinimumScore;
    }

    public void setPlayerMinimumScore(long playerMinimumScore) {
        this.playerMinimumScore = playerMinimumScore;
    }

    public boolean isHasTeamMinimumScore() {
        return hasTeamMinimumScore;
    }

    public void setHasTeamMinimumScore(boolean hasTeamMinimumScore) {
        this.hasTeamMinimumScore = hasTeamMinimumScore;
    }

    public long getTeamMinimumScore() {
        return teamMinimumScore;
    }

    public void setTeamMinimumScore(long teamMinimumScore) {
        this.teamMinimumScore = teamMinimumScore;
    }

    public boolean isShouldTeamWin() {
        return shouldTeamWin;
    }

    public void setShouldTeamWin(boolean shouldTeamWin) {
        this.shouldTeamWin = shouldTeamWin;
    }

    public void handleReward(Territory territory, Objective objective, Battle battle, Map<NwTeam, Integer> teamPositionMap) {
        AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();
        for (String command : getRewardModel().getCommandList()) {
            String target = getRewardModel().getTargetName();
            String finalCommand = adaptMessage.adaptTerritoryMessage(territory, command);
            if (target.equalsIgnoreCase("player")) {
                battle.getPlayerScoreMap().forEach((player, score) -> {
                    rewardPlayer(player, teamPositionMap , territory, finalCommand);
                });
            } else if (target.equalsIgnoreCase("team")) {
                battle.getTeamScoreMap().forEach((team, score) -> {
                    rewardTeam(team, teamPositionMap, territory, finalCommand);
                });
            } else {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
            }
        }
    }

    private void rewardPlayer(Player player, Map<NwTeam, Integer> teamPositionMap, Territory territory, String command) {
        AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();
        PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player.getName());
        NwTeam playerTeam = playerData.getTeam();

        if (playerTeam != null && shallRewardPlayer(territory, teamPositionMap, player, playerTeam)) {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), adaptMessage.adaptMessage(command.replaceAll("\\[player]", player.getName())));
        }
    }

    private void rewardTeam(NwTeam team, Map<NwTeam, Integer> teamPositionMap, Territory territory, String command) {
        AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();
        if (shallRewardTeam(territory, teamPositionMap, team)) {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), adaptMessage.adaptTeamMessage(command, team));
        }
    }

    private boolean shallRewardPlayer(Territory territory, Map<NwTeam, Integer> teamPositionMap, Player player, NwTeam team) {
        int teamPosition = teamPositionMap.get(team);
        if (!shallRewardTeam(territory, teamPositionMap, team)) {
            return false;
        }

        return true;
    }

    private boolean shallRewardTeam(Territory territory,  Map<NwTeam, Integer> teamPositionMap, NwTeam team) {
        int teamPosition = teamPositionMap.get(team);
        String teamRole = getRewardModel().getTeamRole();
        RelationType relation = TeamRelationManager.getTeamRelationManager().getRelationBetweenTeams(team, territory.getOwnerTeam());
        List<Integer> teamPositions = getRewardModel().getTeamPositions();

        if (isShouldTeamWin() && teamPosition != 1) {
            return false;
        }

        if (!teamPositions.isEmpty() && !teamPositions.contains(teamPosition)) {
            return false;
        }

        if (teamRole != null) {
            if (teamRole.equalsIgnoreCase("attacker")) {
                if (territory.getOwnerTeam() != null && !relation.equals(RelationType.ENEMY)) {
                    return false;
                }
            } else if (teamRole.equalsIgnoreCase("defender")) {
                if (!(relation.equals(RelationType.TEAM) || relation.equals(RelationType.ALLY))) {
                    return false;
                }
            }
        }
        return true;
    }
}
