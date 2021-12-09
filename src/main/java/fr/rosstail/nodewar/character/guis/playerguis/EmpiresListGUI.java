package fr.rosstail.conquest.character.guis.playerguis;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import fr.rosstail.conquest.character.guis.GUIs;
import fr.rosstail.conquest.character.datahandlers.PlayerInfo;
import fr.rosstail.conquest.character.empires.Empire;
import fr.rosstail.conquest.Conquest;
import fr.rosstail.conquest.required.lang.AdaptMessage;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class EmpiresListGUI {

    public static void initGUI(Player player, Conquest plugin) {
        FileConfiguration customConfig = new YamlConfiguration();
        String path = "gui/empireListGUI.yml";
        File file = new File(plugin.getDataFolder(), path);

        try {
            customConfig.load(file);

            ChestGui gui = new ChestGui(customConfig.getInt("gui.size"),
                    AdaptMessage.playerMessage(player, customConfig.getString("gui.display")));

            OutlinePane background = new OutlinePane(0, 0, 9, gui.getRows(), Pane.Priority.LOWEST);
            background.addItem(new GuiItem(new ItemStack(Material.BLACK_STAINED_GLASS_PANE)));
            background.setRepeat(true);

            gui.addPane(background);
            StaticPane itemSlot = new StaticPane(customConfig.getInt( "gui.item-slot.x"), customConfig.getInt( "gui.item-slot.y"),
                    9, gui.getRows(), Pane.Priority.HIGHEST);


            initPane(player, plugin, customConfig, itemSlot, gui);

            gui.setOnGlobalClick(
                    event -> {
                        event.setCancelled(true);
                        initPane(player, plugin, customConfig, itemSlot, gui);
                        gui.update();
                    }
            );

            gui.addPane(itemSlot);
            gui.show(player);
        } catch (IOException | InvalidConfigurationException e) {
            System.out.println("[FE_CHARACTER] Error while reading " + path + " file");
            System.out.println(e);
        }

    }

    private static void initPane(Player player, Conquest plugin, FileConfiguration customConfig, StaticPane itemSlot, ChestGui gui) {
        for (String slotName : customConfig.getConfigurationSection("gui.slots").getKeys(false)) {
            String slotPath = "gui.slots." + slotName;
            int posX = customConfig.getInt(slotPath + ".location.x");
            int posY = customConfig.getInt(slotPath + ".location.y");

            String display = AdaptMessage.playerMessage(player, customConfig.getString(slotPath + ".display"));
            Material material = Material.getMaterial(customConfig.getString(slotPath + ".material"));
            List<String> lore = customConfig.getStringList(slotPath + ".lore");
            if (Empire.getEmpires().containsKey(customConfig.getString(slotPath + ".empire"))) {
                Empire empire = Empire.getEmpires().get(customConfig.getString(slotPath + ".empire"));
                itemSlot.addItem(new GuiItem(GUIs.createGuiItem(player, plugin, customConfig, material, display, slotPath,
                        GUIs.adaptLore(player, lore)), event -> {
                    if(PlayerInfo.gets(player).tryJoinEmpire(empire)) {
                        player.sendMessage("vous avez rejoins l'empire " + empire.getDisplay() + " avec succès.");
                    } else {
                        player.sendMessage("Vous ne pouvez pas rejoindre cet empire.");
                    }
                    initPane(player, plugin, customConfig, itemSlot, gui);
                    gui.update();
                }), posX, posY);
            } else {
                itemSlot.addItem(new GuiItem(GUIs.createGuiItem(player, plugin, customConfig, material, display, slotPath,
                        GUIs.adaptLore(player, lore))), posX, posY);
            }
        }
    }
}