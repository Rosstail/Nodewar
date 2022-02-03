package fr.rosstail.nodewar.datahandlers;

import fr.rosstail.nodewar.Nodewar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DataBaseActions
{
    private static Connection connection;
    private static fr.rosstail.nodewar.Nodewar plugin;
    
    public DataBaseActions(final Connection connection, final fr.rosstail.nodewar.Nodewar plugin) {
        try {
            if (connection != null && !connection.isClosed()) {
                this.createTable(connection, this.getPlayerInfoRequest());
            }
            DataBaseActions.connection = connection;
            DataBaseActions.plugin = plugin;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void createTable(final Connection connection, final String string) {
        try {
            if (connection != null && !connection.isClosed()) {
                final Statement statement = connection.createStatement();
                statement.execute(string);
                statement.close();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private String getPlayerInfoRequest() {
        return "CREATE TABLE IF NOT EXISTS " + Nodewar.getDimName() + "_players_info ( UUID varchar(40) PRIMARY KEY UNIQUE NOT NULL,\n empire varchar(30) DEFAULT NULL,\n lastUpdate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP);";
    }
    
    public static void insertPlayerInfo(final Player player) {
        if (player.hasMetadata("NPC")) {
            return;
        }
        final String UUID = player.getUniqueId().toString();
        Bukkit.getScheduler().runTaskAsynchronously(DataBaseActions.plugin, () -> {
            try {
                final PreparedStatement preparedStatement = DataBaseActions.connection.prepareStatement("INSERT INTO " + Nodewar.getDimName() + "_players_info (UUID) VALUES (?);");
                preparedStatement.setString(1, UUID);
                preparedStatement.execute();
                preparedStatement.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
    
    public static void updatePlayerInfo(final Player player, final PlayerInfo playerInfo) {
        if (player.hasMetadata("NPC")) {
            return;
        }
        final String UUID = player.getUniqueId().toString();
        try {
            final PreparedStatement preparedStatement = DataBaseActions.connection.prepareStatement("UPDATE " + Nodewar.getDimName() + "_players_info SET empire = ? WHERE UUID = ?;");
            preparedStatement.setString(1, playerInfo.getEmpire().getName());
            preparedStatement.setString(2, UUID);
            preparedStatement.execute();
            preparedStatement.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
