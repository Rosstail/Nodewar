package fr.rosstail.nodewar.team;

import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.storage.StorageManager;

import java.util.HashMap;
import java.util.Map;

public class TeamDataManager {

    private static TeamDataManager teamDataManager;
    private final Nodewar plugin;

    private final Map<String, Team> stringTeamMap = new HashMap<>();
    private TeamDataManager(Nodewar plugin) {
        this.plugin = plugin;
    }

    public static void init(Nodewar plugin) {
        if (teamDataManager == null) {
            teamDataManager = new TeamDataManager(plugin);
        }
    }

    public void loadTeams() {
        stringTeamMap.clear();
        StorageManager.getManager().selectAllTeamModel().forEach((s, teamModel) -> {
            Team team = new Team(teamModel);
            stringTeamMap.put(s, team);
        });
    }

    public void addNewTeam(Team team) {
        getStringTeamMap().put(team.getTeamModel().getName(), team);
    }

    public void removeDeletedTeam(String teamName) {
        getStringTeamMap().remove(teamName);
    }

    public Map<String, Team> getStringTeamMap() {
        return stringTeamMap;
    }

    public static TeamDataManager getTeamDataManager() {
        return teamDataManager;
    }
}
