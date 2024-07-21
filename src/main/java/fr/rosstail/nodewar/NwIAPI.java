package fr.rosstail.nodewar;

import fr.rosstail.nodewar.territory.battle.Battle;
import fr.rosstail.nodewar.territory.objective.NwObjective;
import fr.rosstail.nodewar.territory.objective.ObjectiveModel;

public interface NwIAPI {

    /**
     * Adds a custom objective to Nodewar. Use it while onLoad()
     * @param name The identifier of the objective and battle
     * @param nwObjective The Objective class.
     * @param objectiveModel The Objective model class that structure what can be set on territories(& types) configs
     * @param battle The Battle class that structure custom comportment of a battle
     * @return if the objective and battle classes have been successfully added.
     */
    boolean addCustomObjective(String name, Class<NwObjective> nwObjective, Class<ObjectiveModel> objectiveModel, Class<Battle> battle);
}
