package fr.rosstail.nodewar.team;

import java.sql.Timestamp;

public class TeamModel {
    private String name;
    private String display;
    private String hexColor;
    private int membersAmount = 0;
    private String ownerUuid;
    private boolean open = false;
    private boolean permanent;
    private Timestamp creationDate = new Timestamp(System.currentTimeMillis());
    private Timestamp lastUpdate = new Timestamp(System.currentTimeMillis());

    public TeamModel(String ownerUuid, String name, String display, String hexColor) {
        this.ownerUuid = ownerUuid;
        this.name = name;
        this.display = display;
        this.hexColor = hexColor;
        this.membersAmount = 1;
        this.permanent = false;
    }

    public TeamModel(String name, String display, String hexColor) {
        this.name = name;
        this.display = display;
        this.hexColor = hexColor;
        this.permanent = true;
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

    public String getOwnerUuid() {
        return ownerUuid;
    }

    public void setOwnerUuid(String ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    public int getMembersAmount() {
        return membersAmount;
    }

    public void setMembersAmount(int membersAmount) {
        this.membersAmount = membersAmount;
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
}
