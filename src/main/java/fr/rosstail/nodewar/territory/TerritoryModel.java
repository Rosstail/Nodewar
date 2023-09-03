package fr.rosstail.nodewar.territory;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.util.List;

public class TerritoryModel {

    private int id;
    private String worldName;
    private String name;
    private String display;
    private String ownerName;
    private List<String> regionStringList;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public List<String> getRegionStringList() {
        return regionStringList;
    }
}
