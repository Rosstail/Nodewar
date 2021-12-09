package fr.rosstail.nodewar.character.guis.adminguis.conquestguis;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import fr.rosstail.nodewar.character.guis.GUIs;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.required.lang.AdaptMessage;
import fr.rosstail.nodewar.territory.zonehandlers.NodeWarWorlds;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class WorldsGUIs {

    public static void initGUI(Player player, Nodewar plugin) {

        int invSize = 6;
        String display = "Worlds - Page 1";
        ChestGui gui = new ChestGui(invSize, AdaptMessage.playerMessage(player, display));

        PaginatedPane paginatedPane = new PaginatedPane(0, 0, 9, invSize);

        OutlinePane background = new OutlinePane(0, 0, 9, gui.getRows(), Pane.Priority.LOWEST);
        background.addItem(new GuiItem(new ItemStack(Material.BLACK_STAINED_GLASS_PANE)));
        background.setRepeat(true);
        gui.addPane(background);

        int page = 0;
        while (true) {
            StaticPane staticPane = initPane(player, plugin, gui, paginatedPane, page);
            paginatedPane.addPane(page, staticPane);
            if (staticPane.getItems().size() < 47) { //9 * 5 lines + 2 buttons
                break;
            }
            page++;
        }

        gui.setOnGlobalClick(
                event -> {
                    event.setCancelled(true);
                }
        );

        gui.addPane(paginatedPane);
        gui.show(player);

    }

    private static StaticPane initPane(Player player, Nodewar plugin, ChestGui gui, PaginatedPane paginatedPane, int page) {
        StaticPane staticPane = new StaticPane(0, 0, 9, 6);

        List<World> worlds = NodeWarWorlds.getUsedWorlds();
        int index = 45 * page;

        int posY = 0;
        int posX = 0;

        if (page > 0) {
            staticPane.addItem(new GuiItem(GUIs.createGuiItem(player, plugin, null, Material.ARROW, "&9Previous page", null
                    , GUIs.adaptLore(player, null)), event -> {
                paginatedPane.setPage(page - 1);
                gui.setTitle(AdaptMessage.playerMessage(player, "&cWorlds - Page " + (page)));
                gui.update();
            }), 0, 5);
        }

        while (posY != 5) {
            while (posX != 9) {
                if (worlds.size() - 1 < index) {
                    return staticPane;
                }
                World world = worlds.get(index);

                staticPane.addItem(new GuiItem(GUIs.createGuiItem(player, plugin, null, Material.HEART_OF_THE_SEA, world.getName(), null
                        , GUIs.adaptLore(player, null)), event -> {
                    WorldTerritoriesGUIs.initGUI(player, plugin, world, gui);
                }), posX, posY);

                posX++;
                index++;
            }
            posY++;
        }

        if (worlds.size() > index) {
            staticPane.addItem(new GuiItem(GUIs.createGuiItem(player, plugin, null, Material.SPECTRAL_ARROW, "&Next page", null
                    , GUIs.adaptLore(player, null)), event -> {
                paginatedPane.setPage(page + 1);
                gui.setTitle(AdaptMessage.playerMessage(player, "Worlds - Page " + (page + 1)));
                gui.update();
            }), 8, 5);
        }
        return staticPane;
    }
}