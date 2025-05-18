package fr.rosstail.nodewar.storage.storagetype.sql;

import fr.rosstail.nodewar.storage.storagetype.SqlStorageManager;

public class MongoDbStorageManager extends SqlStorageManager {

    public MongoDbStorageManager(String pluginName) {
        super(pluginName);
    }

    @Override
    public void setupStorage(String host, short port, String database, String username, String password) {
        this.driver = "mongodb.jdbc.MongoDriver";
        this.url = "jdbc:mongodb://" + host + ":" + port + "/" + database;
        this.username = username;
        this.password = password;
        super.setupStorage(host, port, database, username, password);
    }
}
