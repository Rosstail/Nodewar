package fr.rosstail.nodewar.territory.objective.types;

import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.objective.Objective;

public class ObjectiveKoth extends Objective {

    private ObjectiveKothModel objectiveKothModel;

    public ObjectiveKoth(Territory territory) {
        super(territory);
    }

    @Override
    public NwTeam checkNeutralization() {
        return null;
    }

    @Override
    public NwTeam checkWinner() {
        return null;
    }

    @Override
    public void applyProgress() {

    }
}
