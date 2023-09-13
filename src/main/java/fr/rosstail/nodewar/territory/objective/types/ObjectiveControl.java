package fr.rosstail.nodewar.territory.objective.types;

import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.objective.Objective;

import java.util.List;
import java.util.Map;

public class ObjectiveControl extends Objective {

    float attackerRatio;
    boolean needNeutralize;
    int maxHealth;
    int currentHealth;

    ObjectiveControlModel objectiveControlModel;

    public ObjectiveControl(ObjectiveControlModel territoryModel, ObjectiveControlModel typeModel) {
        ObjectiveControlModel clonedTerritoryObjectiveModel = territoryModel.clone();
        ObjectiveControlModel clonedTypeObjectiveModel = typeModel.clone();
        this.objectiveControlModel = new ObjectiveControlModel(clonedTerritoryObjectiveModel, clonedTypeObjectiveModel);

        this.attackerRatio = Float.parseFloat(this.objectiveControlModel.getAttackerRatioStr());
        this.attackerRatio = Float.parseFloat(this.objectiveControlModel.getAttackerRatioStr());
        this.needNeutralize = Boolean.parseBoolean(this.objectiveControlModel.getNeedNeutralizeStepStr());

        this.maxHealth = Integer.parseInt(this.objectiveControlModel.getMaxHealthStr());
        this.currentHealth = maxHealth;
    }

    @Override
    public String print() {
        return "\n   > Health: " + currentHealth + " / " + maxHealth +
                "\n   > Attacker ratio: " + attackerRatio +
                "\n   > Need neutralize: " + needNeutralize;
    }
}
