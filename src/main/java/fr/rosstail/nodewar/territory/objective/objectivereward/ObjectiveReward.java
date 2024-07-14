package fr.rosstail.nodewar.territory.objective.objectivereward;

import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.TeamIRelation;
import fr.rosstail.nodewar.team.RelationType;
import fr.rosstail.nodewar.team.TeamManager;
import fr.rosstail.nodewar.team.member.TeamMemberModel;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.battle.Battle;
import fr.rosstail.nodewar.territory.objective.NwObjective;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class ObjectiveReward {
    private ObjectiveRewardModel objectiveRewardModel;
    private boolean hasPlayerMinimumScore;
    private long playerMinimumScore;
    private boolean hasTeamMinimumScore;
    private long teamMinimumScore;

    private boolean shouldTeamWin;

    public ObjectiveReward(ObjectiveRewardModel model) {
        this.objectiveRewardModel = model;
        this.hasPlayerMinimumScore = model.getMinimumPlayerScoreStr() != null;
        this.playerMinimumScore = model.getMinimumPlayerScoreStr() != null ? Long.parseLong(model.getMinimumPlayerScoreStr()) : 0L;
        this.hasTeamMinimumScore = model.getMinimumTeamScoreStr() != null;
        this.teamMinimumScore = model.getMinimumTeamScoreStr() != null ? Long.parseLong(model.getMinimumTeamScoreStr()) : 0L;
        this.shouldTeamWin = Boolean.parseBoolean(model.getShouldTeamWinStr());
    }

    public ObjectiveReward(ObjectiveRewardModel childModel, @NotNull ObjectiveRewardModel parentModel) {
        ObjectiveRewardModel clonedParentModel = parentModel.clone();
        if (childModel != null) {
            ObjectiveRewardModel clonedChildModel = childModel.clone();
            this.objectiveRewardModel = new ObjectiveRewardModel(clonedChildModel, clonedParentModel);
        } else {
            this.objectiveRewardModel = clonedParentModel;
        }
        this.hasPlayerMinimumScore = objectiveRewardModel.getMinimumPlayerScoreStr() != null;
        this.playerMinimumScore = Long.parseLong(objectiveRewardModel.getMinimumPlayerScoreStr());
        this.hasTeamMinimumScore = objectiveRewardModel.getMinimumTeamScoreStr() != null;
        this.teamMinimumScore = Long.parseLong(objectiveRewardModel.getMinimumTeamScoreStr());
        this.shouldTeamWin = Boolean.parseBoolean(objectiveRewardModel.getShouldTeamWinStr());
    }

    public ObjectiveRewardModel getRewardModel() {
        return objectiveRewardModel;
    }

    public void setRewardModel(ObjectiveRewardModel objectiveRewardModel) {
        this.objectiveRewardModel = objectiveRewardModel;
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

    public void handleReward(Territory territory, NwObjective objective, Battle battle, Map<NwITeam, Integer> iTeamPositionMap) {
        AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();
        for (String command : getRewardModel().getCommandList()) {
            String target = getRewardModel().getTargetName();
            String finalCommand = adaptMessage.adaptTerritoryMessage(command, territory);
            if (target.equalsIgnoreCase("player")) {
                battle.getPlayerScoreMap().forEach((player, score) -> {
                    rewardPlayer(player, battle, iTeamPositionMap , territory, finalCommand);
                });
            } else if (target.equalsIgnoreCase("team")) {
                battle.getTeamScoreMap().forEach((iTeam, score) -> {
                    rewardITeam(iTeam, battle, iTeamPositionMap, territory, finalCommand);
                });
            } else {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
            }
        }
    }

    private void rewardPlayer(Player player, Battle battle, Map<NwITeam, Integer> iTeamPositionMap, Territory territory, String command) {
        AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();
        PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player.getName());
        NwITeam playerTeam;
        if (player.isOnline()) {
            playerTeam = playerData.getTeam();
        } else {
            TeamMemberModel offlineTeamMemberModel = StorageManager.getManager().selectTeamMemberModelByUsername(player.getName());
            if (offlineTeamMemberModel == null) {
                return;
            }
            playerTeam = TeamManager.getManager().getStringTeamMap().values().stream().filter(nwTeam -> nwTeam.getID() == offlineTeamMemberModel.getTeamId()).findAny().orElse(null);
        }

        if (playerTeam != null && shallRewardPlayer(territory, battle, iTeamPositionMap, player, playerTeam)) {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), adaptMessage.adaptMessage(command.replaceAll("\\[player]", player.getName())));
        }
    }

    private void rewardITeam(NwITeam iTeam, Battle battle, Map<NwITeam, Integer> iTeamPositionMap, Territory territory, String command) {
        AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();
        if (shallRewardTeam(territory, battle, iTeamPositionMap, iTeam)) {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), adaptMessage.adaptTeamMessage(command, iTeam));
        }
    }

    private boolean shallRewardPlayer(Territory territory, Battle battle, Map<NwITeam, Integer> iTeamPositionMap, Player player, NwITeam team) {
        if (!shallRewardTeam(territory, battle, iTeamPositionMap, team)) {
            return false;
        }

        return !hasPlayerMinimumScore || playerMinimumScore <= battle.getPlayerScore(player);
    }

    private boolean shallRewardTeam(Territory territory, Battle battle, Map<NwITeam, Integer> teamPositionMap, NwITeam team) {
        int teamPosition = teamPositionMap.get(team);
        String teamRole = getRewardModel().getTeamRole();
        TeamIRelation relation = TeamManager.getManager().getTeamRelation(team, territory.getOwnerITeam());
        RelationType relationType = relation != null ? relation.getType() : RelationType.NEUTRAL;
        List<Integer> teamPositions = getRewardModel().getTeamPositions();

        if (isShouldTeamWin() && teamPosition != 1) {
            return false;
        }

        if (!teamPositions.isEmpty() && !teamPositions.contains(teamPosition)) {
            return false;
        }

        if (hasTeamMinimumScore && teamMinimumScore > battle.getTeamScore(team)) {
            return false;
        }

        if (teamRole != null) {
            if (teamRole.equalsIgnoreCase("attacker")) {
                return territory.getOwnerITeam() == null || relationType.equals(RelationType.ENEMY);
            } else if (teamRole.equalsIgnoreCase("defender")) {
                return relationType.equals(RelationType.TEAM) || relationType.equals(RelationType.ALLY);
            }
        }
        return true;
    }
}
