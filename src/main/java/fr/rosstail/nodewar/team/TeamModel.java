package fr.rosstail.nodewar.team;

import fr.rosstail.nodewar.team.member.TeamMemberModel;
import fr.rosstail.nodewar.team.relation.TeamRelationModel;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class TeamModel {
    private int id;
    private String name;
    private String display;
    private String hexColor = "#CACACA";
    private boolean open = false;
    private boolean permanent = false;
    private Timestamp creationDate = new Timestamp(System.currentTimeMillis());
    private Timestamp lastUpdate = new Timestamp(System.currentTimeMillis());

    private final Map<Integer, TeamMemberModel> teamMemberModelMap = new HashMap<>();
    private final Map<Integer, TeamRelationModel> teamRelationModelMap = new HashMap<>();


    public TeamModel(String name, String display) {
        this.name = name;
        this.display = display;
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

    public String getHexColor() {
        return hexColor;
    }

    public void setHexColor(String hexColor) {
        this.hexColor = hexColor;
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

    public Map<Integer, TeamMemberModel> getTeamMemberModelMap() {
        return teamMemberModelMap;
    }

    public Map<Integer, TeamRelationModel> getTeamRelationModelMap() {
        return teamRelationModelMap;
    }
}
