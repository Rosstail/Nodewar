package fr.rosstail.nodewar.guis.playerguis;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.datahandlers.PlayerInfoManager;
import fr.rosstail.nodewar.empires.EmpireManager;
import fr.rosstail.nodewar.guis.GUIs;
import fr.rosstail.nodewar.empires.Empire;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class EmpiresListGUI {

    public static void initGUI(Player player, fr.rosstail.nodewar.Nodewar plugin) {
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
            AdaptMessage.print("[" + Nodewar.getDimName() + "] Error while reading " + path + " file", AdaptMessage.prints.ERROR);
        }

    }

    private static void initPane(Player player, fr.rosstail.nodewar.Nodewar plugin, FileConfiguration customConfig, StaticPane itemSlot, ChestGui gui) {
        for (String slotName : customConfig.getConfigurationSection("gui.slots").getKeys(false)) {
            String slotPath = "gui.slots." + slotName;
            int posX = customConfig.getInt(slotPath + ".location.x");
            int posY = customConfig.getInt(slotPath + ".location.y");

            String display = AdaptMessage.playerMessage(player, customConfig.getString(slotPath + ".display"));
            Material material = Material.getMaterial(customConfig.getString(slotPath + ".material"));
            List<String> lore = customConfig.getStringList(slotPath + ".lore");
            Map<String, Empire> empires = EmpireManager.getEmpireManager().getEmpires();
            if (empires.containsKey(customConfig.getString(slotPath + ".empire"))) {
                Empire empire = empires.get(customConfig.getString(slotPath + ".empire"));
                itemSlot.addItem(new GuiItem(GUIs.createGuiItem(player, plugin, customConfig, material, display, slotPath,
                        GUIs.adaptLore(player, lore)), event -> {
                    if(PlayerInfoManager.getPlayerInfoManager().getPlayerInfoMap().get(player).tryJoinEmpire(empire)) {
                        player.sendMessage(AdaptMessage.playerMessage(player, LangManager.getMessage(LangMessage.EMPIRE_PLAYER_JOIN)));
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