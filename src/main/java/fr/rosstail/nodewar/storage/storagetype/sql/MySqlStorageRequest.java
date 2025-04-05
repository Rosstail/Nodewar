package fr.rosstail.nodewar.storage.storagetype.sql;

import fr.rosstail.nodewar.player.PlayerModel;
import fr.rosstail.nodewar.storage.storagetype.SqlStorageRequest;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class MySqlStorageRequest extends SqlStorageRequest {
    private String databaseName;

    public MySqlStorageRequest(String pluginName) {
        super(pluginName);
    }

    @Override
    public void setupStorage(String host, short port, String database, String username, String password) {
        this.databaseName = database;
        this.driver = "com.mysql.jdbc.Driver";
        this.url = "jdbc:mysql://" + host + ":" + port + "/" + database;
        this.username = username;
        this.password = password;

        if (doesTableExists(teamMemberTableName, database)) {
            deleteTeamMemberDuplicate();
            alterTeamMemberTable();
        }

        if (doesTableExists(territoryTableName, database)) {
            alterTerritoryTable();
        }

        createNodewarPlayerTable();
        createNodewarTeamTable();
        createNodewarTeamMemberTable();
        createNodewarTeamRelationTable();
        createNodewarTerritoryTable();
        createNodewarBattlefieldTable();
    }

    @Override
    public void deleteTeamMemberDuplicate() {
        String deleteDuplicatesRequest =
                "DELETE t1 FROM " + teamMemberTableName + " t1 "
                        + "INNER JOIN " + teamMemberTableName + " t2 "
                        + "WHERE t1.id < t2.id AND t1.player_id = t2.player_id;";

        try {
            executeSQLUpdate(deleteDuplicatesRequest);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createNodewarTeamMemberTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + teamMemberTableName + " (" +
                " id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                " player_id INTEGER UNIQUE NOT NULL," +
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
    }

    @Override
    public void updatePlayerModel(PlayerModel model) {
        String query = "UPDATE " + playerTableName + " SET last_update = CURRENT_TIMESTAMP, is_team_open = ?, last_deploy = ? WHERE uuid = ?";
        try {
            super.executeSQLUpdate(query, model.isTeamOpen(), new Timestamp(model.getLastDeploy()), model.getUuid());
            model.setLastUpdate(System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void alterTeamMemberTable() {
        String tableName = getTeamMemberTableName();

        try {
            // Check for UNIQUE constraint
            String listIndexes = "SHOW INDEX FROM " + tableName + ";";
            ResultSet rs = executeSQLQuery(openConnection(), listIndexes);

            List<String> duplicateIndexes = new ArrayList<>();
            while (rs.next()) {
                String columnName = rs.getString("Column_name");
                String indexName = rs.getString("Key_name");
                if ("player_id".equals(columnName)) {
                    duplicateIndexes.add(indexName);
                }
            }
            rs.close();

            // DELETE ALL DUPLICATE
            for (int i = 0; i < duplicateIndexes.size(); i++) {
                String indexName = duplicateIndexes.get(i);
                if (i < duplicateIndexes.size() - 1) { // -1 to keep last index (original/last)
                    String dropIndex = "ALTER TABLE " + tableName + " DROP INDEX " + indexName + ";";
                    executeSQL(dropIndex);
                }
            }

            // Add constraint safely if
            if (duplicateIndexes.isEmpty()) {
                String addUnique = "ALTER TABLE " + tableName + " ADD UNIQUE (player_id);";
                executeSQL(addUnique);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
