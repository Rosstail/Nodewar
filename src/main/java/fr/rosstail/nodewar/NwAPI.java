package fr.rosstail.nodewar;

import fr.rosstail.nodewar.permission.NwIPermissionManagerHandler;
import fr.rosstail.nodewar.permission.PermissionManager;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.territory.battle.Battle;
import fr.rosstail.nodewar.territory.battle.BattleManager;
import fr.rosstail.nodewar.territory.objective.NwObjective;
import fr.rosstail.nodewar.territory.objective.ObjectiveManager;
import fr.rosstail.nodewar.territory.objective.ObjectiveModel;
import org.bukkit.entity.Player;

public class NwAPI implements NwIAPI {

    private static final NwAPI nwAPI = new NwAPI();

    /**
     * Adds a custom objective to Nodewar. Use it while onLoad()
     *
     * @param name           The identifier of the objective and battleClass
     * @param nwObjectiveClass    The Objective class.
     * @param objectiveModelClass The Objective model class that structure what can be set on territories(& types) configs
     * @param battleClass         The Battle class that structure custom comportment of a battleClass
     * @return if the objective and battle classes have been successfully added.
     */
    @Override
    public boolean addCustomObjective(String name, Class<? extends NwObjective> nwObjectiveClass, Class<? extends ObjectiveModel> objectiveModelClass, Class<? extends Battle> battleClass) {
        boolean setupObjective = ObjectiveManager.canAddCustomObjective(name);
        boolean setupBattle = BattleManager.canAddCustomBattle(name);

        if (setupObjective && setupBattle) {
            ObjectiveManager.addCustomObjective(name, nwObjectiveClass, objectiveModelClass);
            BattleManager.addCustomBattle(name, battleClass);
        }

        return setupObjective && setupBattle;
    }

    /**
     * Adds a custom permission plugin compatibility to Nodewar. Use it while onload()
     *
     * @param name                         The identifier of the permission plugin
     * @param customPermissionHandlerClass The custom permission handler that will interact with the permission plugin
     * @return if the compatibility has been added
     */
    @Override
    public boolean addCustomPermissionManager(String name, Class<? extends NwIPermissionManagerHandler> customPermissionHandlerClass) {
        boolean canAddCustomManager = PermissionManager.canAddCustomManager(name);

        if (canAddCustomManager) {
            PermissionManager.addCustomManager(name, customPermissionHandlerClass);
        }

        return canAddCustomManager;
    }

    /**
     * @return The loaded properties from the config.yml
     */
    @Override
    public ConfigData getConfigData() {
        return ConfigData.getConfigData();
    }

    @Override
    public NwITeam getPlayerTeam(Player player) {
        return PlayerDataManager.getPlayerDataFromMap(player).getTeam();
    }

    public static NwAPI getNwAPI() {
        return nwAPI;
    }
}
