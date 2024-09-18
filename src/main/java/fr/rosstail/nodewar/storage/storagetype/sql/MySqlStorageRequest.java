package fr.rosstail.nodewar.storage.storagetype.sql;

import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.player.PlayerModel;
import fr.rosstail.nodewar.storage.storagetype.SqlStorageRequest;

import java.sql.SQLException;
import java.sql.Timestamp;

public class MySqlStorageRequest extends SqlStorageRequest {

    public MySqlStorageRequest(String pluginName) {
        super(pluginName);
    }

    @Override
    public void setupStorage(String host, short port, String database, String username, String password) {
        this.driver = "com.mysql.jdbc.Driver";
        this.url = "jdbc:mysql://" + host + ":" + port + "/" + database;
        this.username = username;
        this.password = password;
        super.setupStorage(host, port, database, username, password);
    }

    @Override
    public void createNodewarTeamMemberTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + teamMemberTableName + " (" +
                " id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                " player_id INTEGER NOT NULL," +
                " team_id INTEGER NOT NULL," +
                " player_rank INTEGER NOT NULL," +
                " join_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                " FOREIGN KEY (player_id) REFERENCES " + playerTableName + "(id) ON DELETE CASCADE," +
                " FOREIGN KEY (team_id) REFERENCES " + teamTableName + "(id) ON DELETE CASCADE" +
                ");";

        executeSQL(query);
    }

    @Override
    public void createNodewarTeamRelationTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + teamRelationTableName + " (" +
                " id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                " first_team_id INTEGER NOT NULL," +
                " second_team_id INTEGER NOT NULL," +
                " relation_type INTEGER NOT NULL," +
                " FOREIGN KEY (first_team_id) REFERENCES " + teamTableName + "(id) ON DELETE CASCADE," +
                " FOREIGN KEY (second_team_id) REFERENCES " + teamTableName + "(id) ON DELETE CASCADE" +
                ");";
        executeSQL(query);
    }

    @Override
    public void createNodewarTerritoryTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + territoryTableName + " ( " +
                " id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                " name varchar(40) UNIQUE NOT NULL," +
                " owner_team_id INTEGER," +
                " last_update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                " FOREIGN KEY (owner_team_id) REFERENCES " + teamTableName + "(id) ON DELETE SET NULL" +
                ") CHARACTER SET utf8 COLLATE utf8_unicode_ci;";
        executeSQL(query);
        try {
            String dropTableQuery = "ALTER TABLE " + territoryTableName + " DROP COLUMN `world`;";
            executeSQLUpdate(dropTableQuery);
            AdaptMessage.print("DROPPED THE USELESS COLUMN world ON " + territoryTableName + " COLUMN.", AdaptMessage.prints.OUT);
        } catch (SQLException ignored) {}
    }

    @Override
    public void updatePlayerModel(PlayerModel model) {
        String query = "UPDATE " + playerTableName + " SET username = ?, last_update = CURRENT_TIMESTAMP, is_team_open = ?, last_deploy = ? WHERE uuid = ?";
        try {
            super.executeSQLUpdate(query, model.getUsername(), model.isTeamOpen(), new Timestamp(model.getLastDeploy()), model.getUuid());
            model.setLastUpdate(System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
