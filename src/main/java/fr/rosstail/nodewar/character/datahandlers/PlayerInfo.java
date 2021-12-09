package fr.rosstail.conquest.character.datahandlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.rosstail.conquest.character.empires.Empire;
import fr.rosstail.conquest.Conquest;
import fr.rosstail.conquest.required.DataBase;
import fr.rosstail.conquest.territory.zonehandlers.ConquestWorlds;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerInfo
{
    private static final Conquest plugin = Conquest.getInstance();
    private static final Map<Player, PlayerInfo> playerInfoMap;
    private final Player player;
    private Empire empire;
    private Long lastUpdate;
    private Timer timer;
    private JsonObject playerJsonData;
    
    public PlayerInfo(final Player player) {
        this.player = player;
        if (DataBase.isConnected()) {
            this.playerJsonData = null;
        }
        else {
            final String path = plugin.getDataFolder() + "/playerdata/" + player.getUniqueId().toString() + ".json";
            if (new File(path).exists()) {
                try {
                    final JsonParser jsonParser = new JsonParser();
                    final Object parsed = jsonParser.parse(new FileReader(path));
                    this.playerJsonData = (JsonObject)parsed;
                }
                catch (IOException e) {
                    this.playerJsonData = null;
                }
            }
            else {
                this.playerJsonData = null;
            }
        }
    }
    
    public static PlayerInfo gets(final Player player) {
        if (!PlayerInfo.playerInfoMap.containsKey(player)) {
            PlayerInfo.playerInfoMap.put(player, new PlayerInfo(player));
        }
        return PlayerInfo.playerInfoMap.get(player);
    }
    
    public Empire getEmpire() {
        if (this.empire != null) {
            return this.empire;
        }
        return Empire.getNoEmpire();
    }
    
    public void setEmpire(final Empire empire) {
        if (empire == null) {
            this.removePlayerGroup(this.empire);
            this.player.sendMessage("You left your empire.");
        }
        else {
            this.player.sendMessage("You joined the " + empire.getDisplay() + " empire.");
            this.setPlayerGroup(empire);
        }
        this.empire = empire;
    }
    
    public boolean tryJoinEmpire(final Empire empire) {
        if (this.empire == null) {
            this.setEmpire(empire);
            return true;
        }
        return false;
    }
    
    public void leaveEmpire() {
        this.setEmpire(null);
    }
    
    public void setPlayerGroup(final Empire empire) {
        if (empire != null) {
            for (final World world : ConquestWorlds.getUsedWorlds()) {
                Conquest.getPermissions().playerAddGroup(world.getName(), this.player, empire.getName());
            }
        }
    }
    
    private void removePlayerGroup(final Empire empire) {
        if (empire != null) {
            for (final World world : ConquestWorlds.getUsedWorlds()) {
                Conquest.getPermissions().playerRemoveGroup(world.getName(), this.player, empire.getName());
            }
        }
    }
    
    public Long getLastUpdate() {
        return this.lastUpdate;
    }
    
    public void setLastUpdate(final Long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
    
    public Timer getTimer() {
        return this.timer;
    }
    
    public void setTimer(final long delay) {
        (this.timer = new Timer()).schedule(new TimerTask() {
            @Override
            public void run() {
                PlayerInfo.this.updateAll();
            }
        }, delay, delay);
    }
    
    public JsonObject getPlayerJsonData() {
        return this.playerJsonData;
    }
    
    public void setPlayerJsonData(final JsonObject playerJsonData) {
        this.playerJsonData = playerJsonData;
    }
    
    public void loadInfo() {
        if (DataBase.isConnected()) {
            if (!this.databaseLoad()) {
                DataBaseActions.insertPlayerInfo(this.player, this);
            }
        }
        else if (this.playerJsonData != null) {
            if (this.playerJsonData.has("empire")) {
                this.empire = Empire.getEmpires().get(this.playerJsonData.get("empire").getAsString());
            }
            else {
                this.empire = null;
            }
        }
    }
    
    private boolean databaseLoad() {
        final String UUID = String.valueOf(this.player.getUniqueId());
        try {
            if (DataBase.getConnection() != null && !DataBase.getConnection().isClosed()) {
                final PreparedStatement statement = DataBase.getConnection().prepareStatement("SELECT * FROM FECHAR_players_infos WHERE UUID = '" + UUID + "';");
                final ResultSet result = statement.executeQuery();
                if (result.next()) {
                    this.empire = Empire.getEmpires().get(result.getString("empire"));
                    statement.close();
                    return true;
                }
                statement.close();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public void updateAll() {
        final PlayerInfo playerInfo = this;
        if (DataBase.isConnected()) {
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {
                @Override
                public void run() {
                    DataBaseActions.updatePlayerInfo(PlayerInfo.this.player, playerInfo);
                }
            });
        }
        else {
            this.saveDataFile();
        }
    }
    
    public void saveDataFile() {
        final String path = this.plugin.getDataFolder() + "/playerdata/" + this.player.getUniqueId().toString() + ".json";
        try {
            final FileWriter fileWriter = new FileWriter(path);
            final JsonObject baseInfo = new JsonObject();
            if (this.empire != null) {
                baseInfo.addProperty("empire", this.empire.getName());
            }
            baseInfo.addProperty("last-update", System.currentTimeMillis());
            final Gson gson = new GsonBuilder().setPrettyPrinting().create();
            fileWriter.write(gson.toJson(baseInfo));
            fileWriter.flush();
            fileWriter.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    static {
        playerInfoMap = new HashMap<Player, PlayerInfo>();
    }
}
