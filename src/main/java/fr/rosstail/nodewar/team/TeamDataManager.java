package fr.rosstail.nodewar.team;

import fr.rosstail.nodewar.Nodewar;

public class TeamDataManager {

    private static TeamDataManager teamDataManager;
    private final Nodewar plugin;
    private TeamDataManager(Nodewar plugin) {
        this.plugin = plugin;
    }

    public static void init(Nodewar plugin) {
        if (teamDataManager == null) {
            teamDataManager = new TeamDataManager(plugin);
        }
    }
}
