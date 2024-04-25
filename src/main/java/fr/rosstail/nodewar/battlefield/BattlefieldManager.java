package fr.rosstail.nodewar.battlefield;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.territory.TerritoryManager;
import org.bukkit.configuration.ConfigurationSection;

public class BattlefieldManager {

    private static BattlefieldManager battlefieldManager;
    private Nodewar plugin;

    public BattlefieldManager(Nodewar plugin) {
        this.plugin = plugin;
    }

    public static void init(Nodewar plugin) {
        if (battlefieldManager == null) {
            battlefieldManager = new BattlefieldManager(plugin);
        }
    }

    public void loadBattlefieldList() {
        ConfigurationSection battlefieldSection = ConfigData.getConfigData().battlefield.configFile.getConfigurationSection("battlefield.list");
        battlefieldSection.getKeys(false).forEach(s -> {
            BattlefieldModel battlefieldModel = new BattlefieldModel(battlefieldSection.getConfigurationSection(s));
            Battlefield battlefield = new Battlefield(battlefieldModel);
            System.out.println("BATTLEFIELD -> " + battlefield.getModel().getDisplay() + " " + battlefield.getStartDate());
        });
    }

    public static BattlefieldManager getBattlefieldManager() {
        return battlefieldManager;
    }
}
