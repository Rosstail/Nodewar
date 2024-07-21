package fr.rosstail.nodewar;

import fr.rosstail.nodewar.permissionmannager.NwIPermissionManagerHandler;
import fr.rosstail.nodewar.permissionmannager.PermissionManager;
import fr.rosstail.nodewar.territory.battle.Battle;
import fr.rosstail.nodewar.territory.battle.BattleManager;
import fr.rosstail.nodewar.territory.objective.NwObjective;
import fr.rosstail.nodewar.territory.objective.ObjectiveManager;
import fr.rosstail.nodewar.territory.objective.ObjectiveModel;

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
        ObjectiveManager objectiveManager = ObjectiveManager.getObjectiveManager();
        BattleManager battleManager = BattleManager.getBattleManager();
        boolean setupObjective = objectiveManager.canAddCustomObjective(name);
        boolean setupBattle = battleManager.canAddCustomBattle(name);

        if (setupObjective && setupBattle) {
            objectiveManager.addCustomObjective(name, nwObjectiveClass, objectiveModelClass);
            battleManager.addCustomBattle(name, battleClass);
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
        boolean canAddCustomManager = PermissionManager.getManager().canAddCustomManager(name);

        if (canAddCustomManager) {
            PermissionManager.getManager().addCustomManager(name, customPermissionHandlerClass);
        }

        return canAddCustomManager;
    }

    public static NwAPI getNwAPI() {
        return nwAPI;
    }
}
