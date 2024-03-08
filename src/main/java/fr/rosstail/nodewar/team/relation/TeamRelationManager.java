package fr.rosstail.nodewar.team.relation;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.RelationType;
import fr.rosstail.nodewar.team.TeamDataManager;

import java.util.*;

public class TeamRelationManager {

    private static TeamRelationManager teamRelationManager;
    private final Nodewar plugin;
    private static final ArrayList<TeamRelation> relationArrayList = new ArrayList<>();

    private static final HashSet<NwTeamRelationInvite> relationInvitesHashSet = new HashSet<>();

    private TeamRelationManager(Nodewar plugin) {
        this.plugin = plugin;
    }

    public static void init(Nodewar plugin) {
        if (teamRelationManager == null) {
            teamRelationManager = new TeamRelationManager(plugin);
        }
    }

    public void loadRelations() {
        relationArrayList.clear();
        StorageManager.getManager().selectAllTeamRelationModel().forEach(model -> {
            NwTeam first = TeamDataManager.getTeamDataManager().getStringTeamMap().values().stream().filter(team -> (team.getModel().getId() == model.getFirstTeamId())).findFirst().orElse(null);
            NwTeam second = TeamDataManager.getTeamDataManager().getStringTeamMap().values().stream().filter(team -> (team.getModel().getId() == model.getSecondTeamId())).findFirst().orElse(null);
            RelationType type = Arrays.stream(RelationType.values()).filter(relationType -> relationType.getWeight() == model.getRelation()).findFirst().get();
            TeamRelation relation = new TeamRelation(first, second, type, model);
            relationArrayList.add(relation);
        });
    }

    public Map<String, TeamRelation> getTeamRelationMap(NwTeam team) {
        Map<String, TeamRelation> teamRelationMap = new HashMap<>();
        relationArrayList.stream().filter(teamRelation -> (
                teamRelation.getModel().getFirstTeamId() == team.getModel().getId()
                        || teamRelation.getModel().getSecondTeamId() == team.getModel().getId()
        )).forEach(teamRelation -> {

            String otherTeamName;

            if (teamRelation.getFirstTeam().equals(team)) {
                otherTeamName = teamRelation.getSecondTeam().getModel().getName();
            } else {
                otherTeamName = teamRelation.getFirstTeam().getModel().getName();
            }

            teamRelationMap.put(otherTeamName, teamRelation);
        });

        return teamRelationMap;
    }

    public RelationType getRelationBetweenTeams(NwTeam firstTeam, NwTeam secondTeam) {
        if (firstTeam != null && secondTeam != null) {
            if (firstTeam == secondTeam) {
                return RelationType.TEAM;
            }
            if (firstTeam.getRelations().containsKey(secondTeam.getModel().getName())) {
                return firstTeam.getRelations().get(secondTeam.getModel().getName()).getRelationType();
            }
        }

        return ConfigData.getConfigData().team.defaultRelation;
    }

    public static TeamRelationManager getTeamRelationManager() {
        return teamRelationManager;
    }

    public static ArrayList<TeamRelation> getRelationArrayList() {
        return relationArrayList;
    }

    public static HashSet<NwTeamRelationInvite> getRelationInvitesHashSet() {
        return relationInvitesHashSet;
    }
}
