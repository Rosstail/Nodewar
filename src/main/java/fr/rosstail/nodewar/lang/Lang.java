package fr.rosstail.nodewar.lang;

import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

public class Lang
{
    private final String langId;
    private String name;
    private final File file;
    private YamlConfiguration configuration;
    
    public Lang(final String langId) {
        this.langId = langId;
        this.file = new File(fr.rosstail.nodewar.Nodewar.getInstance().getDataFolder(), "lang/" + langId + ".yml");
        if (this.file.exists()) {
            this.configuration = YamlConfiguration.loadConfiguration(this.file);
            this.name = this.configuration.getString("lang-name");
        }
    }
    
    public boolean available() {
        return this.file.exists();
    }
    
    public YamlConfiguration getConfiguration() {
        return this.configuration;
    }
    
    public String getId() {
        return this.langId;
    }
    
    public String getName() {
        return this.name;
    }
}
