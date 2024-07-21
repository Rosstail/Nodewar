package fr.rosstail.nodewar;

import fr.rosstail.nodewar.permissionmannager.NwIPermissionManagerHandler;
import fr.rosstail.nodewar.territory.battle.Battle;
import fr.rosstail.nodewar.territory.objective.NwObjective;
import fr.rosstail.nodewar.territory.objective.ObjectiveModel;

public interface NwIAPI {

    /**
     * Adds a custom objective to Nodewar. Use it while onLoad()
     * @param name The identifier of the objective and battle
     * @param nwObjectiveClass The Objective class.
     * @param objectiveModelClass The Objective model class that structure what can be set on territories(& types) configs
     * @param battleClass The Battle class that structure custom comportment of a battle
     * @return if the objective and battle classes have been successfully added.
     */
    boolean addCustomObjective(String name, Class<? extends NwObjective> nwObjectiveClass, Class<? extends ObjectiveModel> objectiveModelClass, Class<? extends Battle> battleClass);

    /**
     * Adds a custom permission plugin compatibility to Nodewar. Use it while onload()
     * @param name The identifier of the permission plugin
     * @param customPermissionHandlerClass The custom permission handler that will interact with the permission plugin
     * @return if the compatibility has been added
     */
    boolean addCustomPermissionManager(String name, Class<? extends NwIPermissionManagerHandler> customPermissionHandlerClass);
}
