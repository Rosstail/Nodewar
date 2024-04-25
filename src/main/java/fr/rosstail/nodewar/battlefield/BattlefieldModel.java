package fr.rosstail.nodewar.battlefield;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public class BattlefieldModel {
    private final String name;
    private final String display;
    private String fromDayStr;
    private String fromTimeStr;
    private String toDayStr;
    private String toTimeStr;
    private boolean resetTeam;
    private boolean closeOnBattleEnd;
    private List<String> territoryTypeList;
    private List<String> territoryList;

    BattlefieldModel(ConfigurationSection section) {
        this.name = "test";
        this.display = section.getString("display");
        this.fromDayStr = section.getString("from.day");
        this.fromTimeStr = section.getString("from.time");

        this.toDayStr = section.getString("to.day");
        this.toTimeStr = section.getString("to.time");

        this.resetTeam = section.getBoolean("reset-team", false);
        this.closeOnBattleEnd = section.getBoolean("clone-on-battle-end", false);

        territoryTypeList = section.getStringList("territory-types");
        territoryList = section.getStringList("territories");
    }

    public String getName() {
        return name;
    }

    public String getDisplay() {
        return display;
    }

    public String getFromDayStr() {
        return fromDayStr;
    }

    public void setFromDayStr(String fromDayStr) {
        this.fromDayStr = fromDayStr;
    }

    public String getFromTimeStr() {
        return fromTimeStr;
    }

    public void setFromTimeStr(String fromTimeStr) {
        this.fromTimeStr = fromTimeStr;
    }

    public String getToDayStr() {
        return toDayStr;
    }

    public void setToDayStr(String toDayStr) {
        this.toDayStr = toDayStr;
    }

    public String getToTimeStr() {
        return toTimeStr;
    }

    public void setToTimeStr(String toTimeStr) {
        this.toTimeStr = toTimeStr;
    }

    public boolean isResetTeam() {
        return resetTeam;
    }

    public void setResetTeam(boolean resetTeam) {
        this.resetTeam = resetTeam;
    }

    public boolean isCloseOnBattleEnd() {
        return closeOnBattleEnd;
    }

    public void setCloseOnBattleEnd(boolean closeOnBattleEnd) {
        this.closeOnBattleEnd = closeOnBattleEnd;
    }

    public List<String> getTerritoryTypeList() {
        return territoryTypeList;
    }

    public void setTerritoryTypeList(List<String> territoryTypeList) {
        this.territoryTypeList = territoryTypeList;
    }

    public List<String> getTerritoryList() {
        return territoryList;
    }

    public void setTerritoryList(List<String> territoryList) {
        this.territoryList = territoryList;
    }
}
