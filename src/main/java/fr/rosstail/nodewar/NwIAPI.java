package fr.rosstail.nodewar;

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
}
