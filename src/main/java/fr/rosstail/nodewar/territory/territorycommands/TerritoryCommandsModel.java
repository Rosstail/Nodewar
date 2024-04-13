package fr.rosstail.nodewar.territory.territorycommands;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class TerritoryCommandsModel implements Cloneable {

    private String name;
    private String targetName; // Server (default) / Team / Player
    private boolean targetOffline;
    private List<String> commandList = new ArrayList<>();
    private long delay;
    private long initialDelay;

    public TerritoryCommandsModel(ConfigurationSection section) {
        this.name = section.getName();
        this.targetName = section.getString("target");
        this.commandList.addAll(section.getStringList("commands"));
        this.delay = section.getLong("delay") * 1000L;
        this.initialDelay = section.getLong("initial-delay") * 1000L;
        this.targetOffline = section.getBoolean("target-offline-player", false);
    }

    public TerritoryCommandsModel(TerritoryCommandsModel childTerritoryCommandsModel, TerritoryCommandsModel parentTerritoryCommandsModel) {
        TerritoryCommandsModel cloneChildTerritoryCommandsModel = childTerritoryCommandsModel.clone();
        TerritoryCommandsModel cloneParentTerritoryCommandsModel = parentTerritoryCommandsModel.clone();

        this.name = cloneChildTerritoryCommandsModel.getName();
        this.targetName = cloneChildTerritoryCommandsModel.getTargetName() != null ? cloneChildTerritoryCommandsModel.getTargetName() : cloneParentTerritoryCommandsModel.getTargetName();

        if (!cloneChildTerritoryCommandsModel.getCommandList().isEmpty()) {
            commandList.addAll(cloneChildTerritoryCommandsModel.getCommandList());
        } else if (!cloneParentTerritoryCommandsModel.getCommandList().isEmpty()) {
            commandList.addAll(cloneParentTerritoryCommandsModel.getCommandList());
        }

        if (cloneChildTerritoryCommandsModel.getInitialDelay() != 0L) {
            this.initialDelay = cloneChildTerritoryCommandsModel.getInitialDelay();
        } else {
            this.initialDelay = cloneParentTerritoryCommandsModel.getInitialDelay();
        }
        if (cloneChildTerritoryCommandsModel.getDelay() != 0L) {
            this.delay = cloneChildTerritoryCommandsModel.getDelay();
        } else {
            this.delay = cloneParentTerritoryCommandsModel.getDelay();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTargetName() {
        return targetName;
    }

    public long getInitialDelay() {
        return initialDelay;
    }

    public long getDelay() {
        return delay;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }
    public List<String> getCommandList() {
        return commandList;
    }

    public void setCommandList(List<String> commandList) {
        this.commandList = commandList;
    }

    public void setInitialDelay(long initialDelay) {
        this.initialDelay = initialDelay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public boolean isTargetOffline() {
        return targetOffline;
    }

    public void setTargetOffline(boolean targetOffline) {
        this.targetOffline = targetOffline;
    }

    @Override
    public TerritoryCommandsModel clone() {
        try {
            TerritoryCommandsModel clone = (TerritoryCommandsModel) super.clone();
            System.out.println("CLONE " + getName());
            clone.setName(getName());
            clone.setTargetName(getTargetName());
            clone.setCommandList(new ArrayList<>(getCommandList()));
            clone.setInitialDelay(getInitialDelay());
            clone.setDelay(getDelay());
            clone.setTargetOffline(isTargetOffline());

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
