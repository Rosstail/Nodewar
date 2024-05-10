package fr.rosstail.nodewar.battlefield;

import org.bukkit.configuration.ConfigurationSection;

import java.time.DayOfWeek;
import java.util.List;

public class BattlefieldModel {

    private int id;
    private final String name;
    private final String display;
    private String fromDayStr;
    private String fromTimeStr;

    private long openDateTime;
    private String toDayStr;
    private String toTimeStr;

    private long closeDateTime;

    private boolean open;
    private boolean resetTeam;
    private boolean closeOnBattleEnd;
    private List<String> territoryTypeList;
    private List<String> territoryList;

    public BattlefieldModel(ConfigurationSection section) {
        this.name = section.getName();
        this.display = section.getString("display");
        this.fromDayStr = section.getString("from.day");
        this.fromTimeStr = section.getString("from.time");

        this.toDayStr = section.getString("to.day");
        this.toTimeStr = section.getString("to.time");
        String[] startTimeStr = fromTimeStr.split(":");
        String[] endTimeStr = toTimeStr.split(":");

        this.openDateTime = BattlefieldManager.getBattlefieldManager().getNextDayTime(DayOfWeek.valueOf(fromDayStr.toUpperCase()), Integer.parseInt(startTimeStr[0]), Integer.parseInt(startTimeStr[1]));
        this.closeDateTime = BattlefieldManager.getBattlefieldManager().getNextDayTime(DayOfWeek.valueOf(toDayStr.toUpperCase()), Integer.parseInt(endTimeStr[0]), Integer.parseInt(endTimeStr[1]));

        this.resetTeam = section.getBoolean("reset-team", false);
        this.closeOnBattleEnd = section.getBoolean("clone-on-battle-end", false);

        territoryTypeList = section.getStringList("territory-types");
        territoryList = section.getStringList("territories");
    }

    public BattlefieldModel(String name) {
        this.name = name;
        this.display = null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public long getOpenDateTime() {
        return openDateTime;
    }

    public void setOpenDateTime(long openDateTime) {
        this.openDateTime = openDateTime;
    }

    public void setToDayStr(String toDayStr) {
        this.toDayStr = toDayStr;
    }

    public String getToTimeStr() {
        return toTimeStr;
    }

    public long getCloseDateTime() {
        return closeDateTime;
    }

    public void setCloseDateTime(long closeDateTime) {
        this.closeDateTime = closeDateTime;
    }

    public void setToTimeStr(String toTimeStr) {
        this.toTimeStr = toTimeStr;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
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
