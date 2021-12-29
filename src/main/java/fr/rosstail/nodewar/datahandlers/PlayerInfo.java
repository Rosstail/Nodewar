package fr.rosstail.nodewar.datahandlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.empires.Empire;
import fr.rosstail.nodewar.required.DataBase;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import org.bukkit.Bukkit;
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
    private static final fr.rosstail.nodewar.Nodewar plugin = Nodewar.getInstance();
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
            final String path = plugin.getDataFolder() + "/playerdata/" + player.getUniqueId() + ".json";
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
        Empire previousEmpire = this.empire;
        this.empire = empire;
        if (empire == null) {
            this.removePlayerGroup(previousEmpire);
            player.sendMessage(AdaptMessage.playerMessage(player, LangManager.getMessage(LangMessage.PLAYER_LEAVE_EMPIRE)));
        }
        else {
            setPlayerGroup(empire);
            player.sendMessage(AdaptMessage.playerMessage(player, LangManager.getMessage(LangMessage.PLAYER_JOIN_EMPIRE)));
        }
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
            Nodewar.getPermissions().playerAddGroup(null, this.player, empire.getName());
        }
    }
    
    private void removePlayerGroup(final Empire empire) {
        if (empire != null) {
            Nodewar.getPermissions().playerRemoveGroup(null, this.player, empire.getName());
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
                DataBaseActions.insertPlayerInfo(this.player);
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
                final PreparedStatement statement = DataBase.getConnection().prepareStatement("SELECT * FROM " + Nodewar.getDimName() + "_players_info WHERE UUID = '" + UUID + "';");
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
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> DataBaseActions.updatePlayerInfo(PlayerInfo.this.player, playerInfo));
        }
        else {
            this.saveDataFile();
        }
    }
    
    public void saveDataFile() {
        final String path = plugin.getDataFolder() + "/playerdata/" + this.player.getUniqueId() + ".json";
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
        playerInfoMap = new HashMap<>();
    }
}
