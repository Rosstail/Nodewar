package fr.rosstail.nodewar.player;

import org.bukkit.entity.Player;

public class PlayerModel {
    private int id;
    private String uuid;
    private String username;
    private boolean teamOpen = true;

    private long lastDeploy = 0L;
    private long lastUpdate = 0L;


    /**
     * Constructor if the selected player is connected
     * @param player - a user joining the server
     */
    public PlayerModel(Player player) {
        this.uuid = player.getUniqueId().toString();
        this.username = player.getName();
    }

    /**
     * Constructor if the selected player is not connected
     * @param uuid - microsoft/mojang identifier of selected player
     * @param username - current username of the selected player
     */
    public PlayerModel(String uuid, String username) {
        this.uuid = uuid;
        this.username = username;
        this.checkForData();
    }

    /**
     * Creates a copy of an existing PlayerModel. Should only be used to read players data, not
     * @param playerModel
     */
    public PlayerModel(PlayerModel playerModel) {
        this.id = playerModel.getId();
        this.uuid = playerModel.getUuid();
        this.username = playerModel.getUsername();
        this.teamOpen = playerModel.isTeamOpen();
        this.lastDeploy = playerModel.getLastDeploy();
        this.lastUpdate = playerModel.getLastUpdate();
    }

    /**
     * Will check if the player already has data in localstorage or database.
     * @return the success of the check
     */
    private boolean checkForData() {
        return false;
    }

    /*
    Getters setters
     */

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public boolean isTeamOpen() {
        return teamOpen;
    }

    public void setTeamOpen(boolean teamOpen) {
        this.teamOpen = teamOpen;
    }

    public long getLastDeploy() {
        return lastDeploy;
    }

    public void setLastDeploy(long lastDeploy) {
        this.lastDeploy = lastDeploy;
    }
}
