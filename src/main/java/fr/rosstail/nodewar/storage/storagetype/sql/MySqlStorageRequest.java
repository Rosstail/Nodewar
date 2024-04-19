package fr.rosstail.nodewar.storage.storagetype.sql;

import fr.rosstail.nodewar.storage.storagetype.SqlStorageRequest;

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
                " FOREIGN KEY (player_id) REFERENCES " + playerTableName + "(id) ON DELETE CASCADE" +
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
                " world varchar(40) NOT NULL," +
                " owner_team_id INTEGER," +
                " last_update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                " FOREIGN KEY (owner_team_id) REFERENCES " + teamTableName + "(id) ON DELETE SET NULL" +
                ");";
        executeSQL(query);
    }


}
