package fr.rosstail.nodewar.battlefield;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.territory.TerritoryManager;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class BattlefieldManager {

    private static BattlefieldManager battlefieldManager;
    private Nodewar plugin;

    private List<Battlefield> battlefieldList = new ArrayList<>();
    private List<BattlefieldModel> battlefieldModelList = new ArrayList<>();

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
            battlefieldModelList.add(battlefieldModel);
        });

        battlefieldModelList.forEach(battlefieldModel -> {
            StorageManager.getManager().insertBattlefieldModel(battlefieldModel);
            battlefieldList.add(new Battlefield(battlefieldModel));
        });
    }

    public static BattlefieldManager getBattlefieldManager() {
        return battlefieldManager;
    }
}
