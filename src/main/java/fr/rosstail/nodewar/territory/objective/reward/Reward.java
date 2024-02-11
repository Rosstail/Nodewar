package fr.rosstail.nodewar.territory.objective.reward;

import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.RelationType;
import fr.rosstail.nodewar.team.relation.TeamRelationManager;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryManager;
import fr.rosstail.nodewar.territory.objective.Objective;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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

    public void handleReward(Territory territory, Objective objective, ArrayList<NwTeam> participatingTeam) {
        AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();
        for (String command : getRewardModel().getCommandList()) {
            String target = getRewardModel().getTargetName();
            String finalCommand = adaptMessage.adaptTerritoryMessage(territory, command);
            if (target.equalsIgnoreCase("player")) {
                territory.getPlayers().forEach(player -> {
                    PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player.getName());
                    NwTeam playerTeam = playerData.getTeam();

                    if (playerTeam != null && shallRewardTarget(territory, participatingTeam, playerTeam)) {
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), finalCommand.replaceAll("\\[player]", player.getName()));
                    }
                });
            } else if (target.equalsIgnoreCase("team")) {
                participatingTeam.forEach(team -> {
                    if (shallRewardTarget(territory, participatingTeam, team)) {
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), adaptMessage.adaptTeamMessage(team, finalCommand));
                    }
                });
            } else {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
            }
        }
    }

    private boolean shallRewardTarget(Territory territory, List<NwTeam> participatingTeams, NwTeam team) {
        int teamIndex = participatingTeams.indexOf(team);
        List<Integer> teamPositions = getRewardModel().getTeamPositions();
        String teamRole = getRewardModel().getTeamRole();
        RelationType relation = TeamRelationManager.getTeamRelationManager().getRelationBetweenTeams(team, territory.getOwnerTeam());

        System.out.println(rewardModel.getName() + " - " + team.getModel().getName());

        if (isShouldTeamWin() && team != participatingTeams.get(0)) {
            System.out.println("Pas la première équipe");
            return false;
        }
        if (!teamPositions.isEmpty() && !teamPositions.contains(teamIndex + 1)) {
            System.out.println("Pas le bon emplacement pour l'équipe " + (teamIndex + 1));
            return false;
        }

        if (teamRole != null) {
            if (teamRole.equalsIgnoreCase("attacker")) {
                if (territory.getOwnerTeam() != null && !relation.equals(RelationType.ENEMY)) {
                    System.out.println("Pas ennemi");
                    return false;
                }
            } else if (teamRole.equalsIgnoreCase("defender")) {
                if (!(relation.equals(RelationType.TEAM) || relation.equals(RelationType.ALLY))) {
                    System.out.println("Pas allié");
                    return false;
                }
            }
        }
        System.out.println("Noiiiice.");
        return true;
    }
}
