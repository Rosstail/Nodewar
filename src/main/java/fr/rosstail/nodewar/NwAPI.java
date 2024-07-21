package fr.rosstail.nodewar;

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
     * @param name           The identifier of the objective and battle
     * @param nwObjective    The Objective class.
     * @param objectiveModel The Objective model class that structure what can be set on territories(& types) configs
     * @param battle         The Battle class that structure custom comportment of a battle
     * @return if the objective and battle classes have been successfully added.
     */
    @Override
    public boolean addCustomObjective(String name, Class<NwObjective> nwObjective, Class<ObjectiveModel> objectiveModel, Class<Battle> battle) {
        ObjectiveManager objectiveManager = ObjectiveManager.getObjectiveManager();
        BattleManager battleManager = BattleManager.getBattleManager();
        boolean setupObjective = objectiveManager.canAddCustomObjective(name);
        boolean setupBattle = battleManager.canAddCustomBattle(name);

        if (setupObjective && setupBattle) {
            objectiveManager.addCustomObjective(name, nwObjective, objectiveModel);
            battleManager.addCustomBattle(name, battle);
        }

        return setupObjective && setupBattle;
    }

    public static NwAPI getNwAPI() {
        return nwAPI;
    }
}
