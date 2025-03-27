package fr.rosstail.nodewar.lang;

import fr.rosstail.nodewar.FileResourcesUtils;
import fr.rosstail.nodewar.Nodewar;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public class Lang {

    private static Lang lang = null;
    private final String langId;
    private final String name;

    private final File langFile;
    private final YamlConfiguration langConfig;
    private final YamlConfiguration defaultLangConfig;

    public Lang(String langId) {
        this.langId = langId;
        File wantedLangFile = new File(Nodewar.getInstance().getDataFolder(), "lang/" + langId + ".yml");
        this.defaultLangConfig = FileResourcesUtils.getDefaultFileConfiguration();

        if (wantedLangFile.exists()) {
            this.langFile = wantedLangFile;
            this.langConfig = YamlConfiguration.loadConfiguration(this.langFile);
            this.name = this.langFile.getName();
        } else {
            this.langFile = null;
            this.langConfig = null;
            this.name = langId;
            AdaptMessage.print("Locale lang/" + langId + ".yml does not exists. use en_EN.yml from resources.", AdaptMessage.prints.WARNING);
        }

        for (LangMessage langMessage : LangMessage.values()) {
            String stringPath = langMessage.getText();
            String gotMessage = null;
            if (langConfig != null) {
                if (langConfig.isList(stringPath)) {
                    gotMessage = String.join("\n", langConfig.getStringList(stringPath));
                    langMessage.setDisplayText(AdaptMessage.getAdaptMessage().adaptMessage(gotMessage));
                } else {
                    gotMessage = langConfig.getString(stringPath);
                    if (gotMessage != null) {
                        langMessage.setDisplayText(AdaptMessage.getAdaptMessage().adaptMessage(gotMessage));
                    }
                }
            }

            if (gotMessage == null && !langMessage.isNullable()) {
                if (defaultLangConfig.isList(stringPath)) {
                    langMessage.setDisplayText(AdaptMessage.getAdaptMessage().adaptMessage(String.join("\n", defaultLangConfig.getStringList(stringPath))));
                } else {
                    langMessage.setDisplayText(AdaptMessage.getAdaptMessage().adaptMessage(defaultLangConfig.getString(stringPath)));
                }
            }

        }
    }

    /**
     * @return the configuration model
     */
    public YamlConfiguration getLangConfig() {
        return langConfig;
    }

    /**
     * @return the language id
     */
    public String getId() {
        return langId;
    }

    /**
     * @return the language name
     */
    public String getName() {
        return name;
    }

    public static Lang getLang() {
        return lang;
    }

    public static void initLang(String langId) {
        lang = new Lang(langId);
    }
}