package fr.rosstail.nodewar.territory.objective.types;

import fr.rosstail.nodewar.territory.objective.Objective;

public class ObjectiveSiege extends Objective {

    private int maxHealth;
    private int currentHealth;

    private final ObjectiveSiegeModel objectiveSiegeModel;

    public ObjectiveSiege(ObjectiveSiegeModel territoryModel, ObjectiveSiegeModel typeModel) {
        ObjectiveSiegeModel clonedTerritoryObjectiveModel = territoryModel.clone();
        ObjectiveSiegeModel clonedTypeObjectiveModel = typeModel.clone();
        this.objectiveSiegeModel = new ObjectiveSiegeModel(clonedTerritoryObjectiveModel, clonedTypeObjectiveModel);

        this.maxHealth = Integer.parseInt(this.objectiveSiegeModel.getMaxHealthString());
        this.currentHealth = this.maxHealth;
    }

    public ObjectiveSiegeModel getObjectiveSiegeModel() {
        return objectiveSiegeModel;
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
}
