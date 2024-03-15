package fr.rosstail.nodewar.team;

import fr.rosstail.nodewar.team.member.TeamMemberModel;
import fr.rosstail.nodewar.team.relation.TeamRelationModel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

public class TeamModel {
    private int id;
    private String name;
    private String display;
    private String teamColor;
    private boolean open = false;
    private boolean permanent = false;
    private boolean openRelation = true;
    private Timestamp creationDate = new Timestamp(System.currentTimeMillis());
    private Timestamp lastUpdate = new Timestamp(System.currentTimeMillis());

    private final Map<Integer, TeamMemberModel> teamMemberModelMap = new HashMap<>();
    private final Map<Integer, TeamRelationModel> teamRelationModelMap = new HashMap<>();


    public TeamModel(String name, String display, String teamColor) {
        this.name = name;
        this.display = display;
        this.teamColor = teamColor;
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

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public String getTeamColor() {
        return teamColor;
    }

    public void setTeamColor(String teamColor) {
        this.teamColor = teamColor;
    }

    public Timestamp getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Timestamp creationDate) {
        this.creationDate = creationDate;
    }

    public boolean isPermanent() {
        return permanent;
    }

    public void setPermanent(boolean permanent) {
        this.permanent = permanent;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public Timestamp getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Timestamp lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public boolean isOpenRelation() {
        return openRelation;
    }

    public void setOpenRelation(boolean openRelation) {
        this.openRelation = openRelation;
    }

    public Map<Integer, TeamMemberModel> getTeamMemberModelMap() {
        return teamMemberModelMap;
    }

    public Map<Integer, TeamRelationModel> getTeamRelationModelMap() {
        return teamRelationModelMap;
    }
}
