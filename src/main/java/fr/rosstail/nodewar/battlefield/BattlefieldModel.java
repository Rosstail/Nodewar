package fr.rosstail.nodewar.battlefield;

import fr.rosstail.nodewar.lang.AdaptMessage;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BattlefieldModel {

    private int id;
    private final String name;
    private final String display;
    private Set<String> startDaysStrSet;
    private Set<String> startTimesStrSet;
    private String durationStr;

    private long openDateTime;
    private long duration;
    private long closeDateTime;

    private boolean open;
    private boolean resetTeam;
    private boolean endBattleOnBattlefieldEnd;
    private List<String> territoryTypeList;
    private List<String> territoryList;

    private boolean announcement;

    @Deprecated(since = "2.2.3", forRemoval = true)
    private String fromTimeStr;
    @Deprecated(since = "2.2.3", forRemoval = true)
    private Set<String> toDayStrSet;
    @Deprecated(since = "2.2.3", forRemoval = true)
    private String toTimeStr;

    public BattlefieldModel(ConfigurationSection section) {
        this.name = section.getName();
        this.display = section.getString("display");
        this.startDaysStrSet = new HashSet<>(section.getStringList("start-days"));
        this.startTimesStrSet = new HashSet<>(section.getStringList("start-times"));

        if (startDaysStrSet.isEmpty()) {
            startDaysStrSet = new HashSet<>();
            if (section.getString("from.day") != null) {
                startDaysStrSet.add(section.getString("from.day"));
            }
        }
        if (startTimesStrSet.isEmpty()) {
            startTimesStrSet = new HashSet<>();
            if (section.getString("from.time") != null) {
                startTimesStrSet.add(section.getString("from.time"));
            }
        }

        durationStr = section.getString("duration");

        Set<String> startTimeStrSet = startTimesStrSet != null
                ? startTimesStrSet
                : fromTimeStr != null
                    ? Collections.singleton(fromTimeStr)
                    : new HashSet<>();
        this.openDateTime = BattlefieldManager.getManager().getNextDateTimeMillis(startDaysStrSet, startTimeStrSet);

        if (durationStr != null) {
            duration = AdaptMessage.evalDuration(durationStr);
            this.closeDateTime = openDateTime + duration;
        } else { // Deprecation maintenance
            this.toDayStrSet = new HashSet<>(section.getStringList("to.days"));
            this.toTimeStr = section.getString("to.time");
            if (toDayStrSet.isEmpty()) {
                toDayStrSet = new HashSet<>();
                if (section.getString("to.day") != null) {
                    toDayStrSet.add(section.getString("to.day"));
                }
            }

            this.closeDateTime = BattlefieldManager.getManager().getNextDateTimeMillis(toDayStrSet, Collections.singleton(toTimeStr));
            this.duration = closeDateTime - openDateTime;
        }

        this.resetTeam = section.getBoolean("reset-team", false);
        this.endBattleOnBattlefieldEnd = section.getBoolean("end-battle-on-battlefield-end", false);

        territoryTypeList = section.getStringList("territory-types");
        territoryList = section.getStringList("territories");
        announcement = section.getBoolean("announcement", true);
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

    public Set<String> getStartDaysStrSet() {
        return startDaysStrSet;
    }

    public void setStartDaysStrSet(Set<String> startDaysStrSet) {
        this.startDaysStrSet = startDaysStrSet;
    }

    public String getFromTimeStr() {
        return fromTimeStr;
    }

    public void setFromTimeStr(String fromTimeStr) {
        this.fromTimeStr = fromTimeStr;
    }

    @Deprecated(since = "2.2.3", forRemoval = true)
    public Set<String> getToDayStrSet() {
        return toDayStrSet;
    }

    public long getOpenDateTime() {
        return openDateTime;
    }

    public Set<String> getStartTimesStrSet() {
        return startTimesStrSet;
    }

    public void setStartTimesStrSet(Set<String> startTimesStrSet) {
        this.startTimesStrSet = startTimesStrSet;
    }

    public void setOpenDateTime(long openDateTime) {
        this.openDateTime = openDateTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getDurationStr() {
        return durationStr;
    }

    public void setDurationStr(String durationStr) {
        this.durationStr = durationStr;
    }

    @Deprecated(since = "2.2.3", forRemoval = true)
    public void setToDayStrSet(Set<String> toDayStrSet) {
        this.toDayStrSet = toDayStrSet;
    }

    @Deprecated(since = "2.2.3", forRemoval = true)
    public String getToTimeStr() {
        return toTimeStr;
    }

    public long getCloseDateTime() {
        return closeDateTime;
    }

    public void setCloseDateTime(long closeDateTime) {
        this.closeDateTime = closeDateTime;
    }

    @Deprecated(since = "2.2.3", forRemoval = true)
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

    public boolean isEndBattleOnBattlefieldEnd() {
        return endBattleOnBattlefieldEnd;
    }

    public void setEndBattleOnBattlefieldEnd(boolean endBattleOnBattlefieldEnd) {
        this.endBattleOnBattlefieldEnd = endBattleOnBattlefieldEnd;
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

    public boolean isAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(boolean announcement) {
        this.announcement = announcement;
    }
}
