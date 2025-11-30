package fr.rosstail.nodewar.utils;

public class Cache {
    private final String key;
    private String value;
    private final long lifespan = 2;
    private long lastUpdate = System.currentTimeMillis();

    public Cache(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getLifespan() {
        return lifespan;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
