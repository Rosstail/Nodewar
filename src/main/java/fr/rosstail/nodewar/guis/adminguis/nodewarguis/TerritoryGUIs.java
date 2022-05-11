package fr.rosstail.nodewar.guis.adminguis.nodewarguis;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import fr.rosstail.nodewar.guis.GUIs;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.territory.eventhandlers.customevents.TerritoryVulnerabilityToggleEvent;
import fr.rosstail.nodewar.territory.zonehandlers.Territory;
import fr.rosstail.nodewar.territory.zonehandlers.objective.Objective;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TerritoryGUIs {
    public static void initGUI(Player player, Nodewar plugin, Territory territory, ChestGui previousGui) {

        int invSize = 2;
        ChestGui gui = new ChestGui(invSize, AdaptMessage.playerMessage(player, territory.getDisplay()));

        StaticPane staticPane = initPane(player, plugin, gui, previousGui, territory);

        OutlinePane background = new OutlinePane(0, 0, 9, gui.getRows(), Pane.Priority.LOWEST);
        background.addItem(new GuiItem(new ItemStack(Material.BLACK_STAINED_GLASS_PANE)));
        background.setRepeat(true);
        gui.addPane(background);

        gui.setOnGlobalClick(
                event -> {
                    event.setCancelled(true);
                }
        );

        gui.addPane(staticPane);
        gui.show(player);

    }

    private static StaticPane initPane(Player player, Nodewar plugin, ChestGui gui, ChestGui previousGui, Territory territory) {
        StaticPane staticPane = new StaticPane(0, 0, 9, 2);

        vulnerabilityButton(player, plugin, territory, gui, staticPane);

        staticPane.addItem(new GuiItem(GUIs.createGuiItem(player, plugin, null, Material.RED_BANNER, "&9Empire", null
                , GUIs.adaptLore(player, null)), event -> {
            if (event.isRightClick()) {
                Objective objective = territory.getObjective();
                if (objective != null) {
                    objective.reset();
                }
                player.sendMessage(AdaptMessage.playerMessage(player,
                        AdaptMessage.territoryMessage(territory,LangManager.getMessage(LangMessage.TERRITORY_NEUTRALIZE))));
            } else {
                TerritoryEmpireGUIs.initGUI(player, plugin, gui, territory);
            }
        }), 4, 0);

        staticPane.addItem(new GuiItem(GUIs.createGuiItem(player, plugin, null, Material.BEACON, "&9Capture points", null
                , GUIs.adaptLore(player, null)), event -> {
            TerritoryPointsGUIs.initGUI(player, plugin, territory, gui);
        }), 7, 0);

        staticPane.addItem(new GuiItem(GUIs.createGuiItem(player, plugin, null, Material.BARRIER, "&9Previous Menu", null
                , GUIs.adaptLore(player, null)), event -> {
            previousGui.show(player);
        }), 4, 1);
        return staticPane;
    }

    private static void vulnerabilityButton(Player player, Nodewar plugin, Territory territory, ChestGui gui, StaticPane staticPane) {
        Material vulnerableMaterial;
        if (territory.isVulnerable()) {
            vulnerableMaterial = Material.REDSTONE_TORCH;
        } else {
            vulnerableMaterial = Material.LEVER;
        }
        staticPane.addItem(new GuiItem(GUIs.createGuiItem(player, plugin, null, vulnerableMaterial, "&9Vulnerability", null
                , GUIs.adaptLore(player, null)), event -> {

            TerritoryVulnerabilityToggleEvent toggleEvent = new TerritoryVulnerabilityToggleEvent(territory, !territory.isVulnerable());
            Bukkit.getPluginManager().callEvent(toggleEvent);

            if (!toggleEvent.isCancelled()) {
                vulnerabilityButton(player, plugin, territory, gui, staticPane);
                if (territory.isVulnerable()) {
                    player.sendMessage(AdaptMessage.playerMessage(player,
                            AdaptMessage.territoryMessage(territory, LangManager.getMessage(LangMessage.TERRITORY_VULNERABLE))));
                } else {
                    player.sendMessage(AdaptMessage.playerMessage(player,
                            AdaptMessage.territoryMessage(territory, LangManager.getMessage(LangMessage.TERRITORY_INVULNERABLE))));
                }
            }

            gui.update();
        }), 1, 0);
    }
}