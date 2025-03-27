package fr.rosstail.nodewar.config;

import fr.rosstail.nodewar.ConfigData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public abstract class ConfigUpdater {

    public float getVersion() {
        return 2.0f;
    }

    public abstract void update(@NotNull File configFile);
}
