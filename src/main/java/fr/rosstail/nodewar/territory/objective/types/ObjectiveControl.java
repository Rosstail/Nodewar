package fr.rosstail.nodewar.territory.objective.types;

import fr.rosstail.nodewar.territory.objective.Objective;

public class ObjectiveControl extends Objective {

    private float attackerRatio;
    private boolean needNeutralize;
    private int maxHealth;
    private int currentHealth;

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

    public float getAttackerRatio() {
        return attackerRatio;
    }

    public void setAttackerRatio(float attackerRatio) {
        this.attackerRatio = attackerRatio;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public void setCurrentHealth(int currentHealth) {
        this.currentHealth = currentHealth;
    }

    public boolean isNeedNeutralize() {
        return needNeutralize;
    }

    public void setNeedNeutralize(boolean needNeutralize) {
        this.needNeutralize = needNeutralize;
    }

    @Override
    public String print() {
        return "\n   > Health: " + currentHealth + " / " + maxHealth +
                "\n   > Attacker ratio: " + attackerRatio +
                "\n   > Need neutralize: " + needNeutralize;
    }
}
