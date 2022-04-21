package fr.rosstail.nodewar.required;

import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.datahandlers.PlayerInfo;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;

import java.sql.*;

public class DataBaseInteractions {
    fr.rosstail.nodewar.Nodewar plugin;
    private static DataBaseInteractions dataBaseInteractions = null;
    public String connector;
    public String host;
    public String database;
    public String username;
    public String password;
    public int port;
    private final Configuration config;

    private DataBaseInteractions(final Nodewar plugin) {
        this.plugin = plugin;
        this.config = plugin.getCustomConfig();

        prepareConnection();
        createTable(createPlayerInfoTableString());
    }

    public static void init(Nodewar plugin) {
        dataBaseInteractions = new DataBaseInteractions(plugin);
    }

    private void prepareConnection() {
        this.connector = this.config.getString("mysql.connector");
        this.host = this.config.getString("mysql.host");
        this.database = this.config.getString("mysql.database");
        this.username = this.config.getString("mysql.username");
        this.password = this.config.getString("mysql.password");
        this.port = this.config.getInt("mysql.port");
    }

    public Connection openConnection() {
        synchronized (this) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                return DriverManager.getConnection("jdbc:" + this.connector + "://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void closeConnection(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTable(final String string) {
        Connection connection = openConnection();
        try {
            final Statement statement = connection.createStatement();
            statement.execute(string);
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertPlayerInfo(final Player player) {
        if (player.hasMetadata("NPC")) {
            return;
        }
        final String UUID = player.getUniqueId().toString();
        Connection connection = openConnection();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                final PreparedStatement preparedStatement = connection.
                        prepareStatement("INSERT INTO " + Nodewar.getDimName() + "_players_info (UUID) VALUES (?);");
                preparedStatement.setString(1, UUID);
                preparedStatement.execute();
                preparedStatement.close();
                closeConnection(connection);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void updatePlayerInfo(final Player player, final PlayerInfo playerInfo) {
        if (player.hasMetadata("NPC")) {
            return;
        }
        Connection connection = openConnection();
        final String UUID = player.getUniqueId().toString();
        try {
            final PreparedStatement preparedStatement = connection.
                    prepareStatement("UPDATE " + Nodewar.getDimName() + "_players_info SET empire = ? WHERE UUID = ?;");
            preparedStatement.setString(1, playerInfo.getEmpire().getName());
            preparedStatement.setString(2, UUID);
            preparedStatement.execute();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String createPlayerInfoTableString() {
        return "CREATE TABLE IF NOT EXISTS " + Nodewar.getDimName() + "_players_info" +
                " ( UUID varchar(40) PRIMARY KEY UNIQUE NOT NULL,\n empire varchar(30) DEFAULT NULL," +
                "\n lastUpdate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP);";
    }

    public static DataBaseInteractions get() {
        return dataBaseInteractions;
    }
}
