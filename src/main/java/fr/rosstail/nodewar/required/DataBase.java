package fr.rosstail.nodewar.required;

import java.sql.DriverManager;
import java.sql.SQLException;
import fr.rosstail.nodewar.datahandlers.DataBaseActions;
import fr.rosstail.nodewar.lang.AdaptMessage;
import org.bukkit.configuration.Configuration;
import java.sql.Connection;

public class DataBase
{
    fr.rosstail.nodewar.Nodewar plugin;
    private static DataBase dataBase;
    private Connection connection;
    public String connector;
    public String host;
    public String database;
    public String username;
    public String password;
    public int port;
    private final Configuration config;
    
    private DataBase(final fr.rosstail.nodewar.Nodewar plugin) {
        this.plugin = plugin;
        this.config = plugin.getCustomConfig();
    }
    
    public static DataBase gets(final fr.rosstail.nodewar.Nodewar plugin) {
        if (DataBase.dataBase == null) {
            DataBase.dataBase = new DataBase(plugin);
        }
        return DataBase.dataBase;
    }
    
    public boolean isConnexionEnabled() {
        return this.config.getString("mysql.connector") != null && !this.config.getString("mysql.connector").equalsIgnoreCase("none");
    }
    
    public void prepareConnection() {
        this.connector = this.config.getString("mysql.connector");
        this.host = this.config.getString("mysql.host");
        this.database = this.config.getString("mysql.database");
        this.username = this.config.getString("mysql.username");
        this.password = this.config.getString("mysql.password");
        this.port = this.config.getInt("mysql.port");
        try {
            this.openConnection();
            new DataBaseActions(this.connection, this.plugin);
        }
        catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void openConnection() throws SQLException, ClassNotFoundException {
        synchronized (this) {
            if (this.connection == null || this.connection.isClosed()) {
                Class.forName("com.mysql.jdbc.Driver");
                this.connection = DriverManager.getConnection("jdbc:" + this.connector + "://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);
            }
        }
    }
    
    public void closeConnection() {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                this.connection.close();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static boolean isConnected() {
        try {
            final Connection connection = getConnection();
            if (connection != null && !connection.isClosed() && connection.isValid(20)) {
                return true;
            }
        }
        catch (SQLException e) {
            AdaptMessage.print("No connexion to database!", AdaptMessage.prints.ERROR);
        }
        return false;
    }
    
    public static Connection getConnection() {
        return DataBase.dataBase.connection;
    }
    
    static {
        DataBase.dataBase = null;
    }
}
