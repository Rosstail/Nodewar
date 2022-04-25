package fr.rosstail.nodewar.guis.adminguis.nodewarguis;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import fr.rosstail.nodewar.empires.EmpireManager;
import fr.rosstail.nodewar.guis.GUIs;
import fr.rosstail.nodewar.empires.Empire;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.territory.zonehandlers.Territory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class TerritoryEmpireGUIs {

    public static void initGUI(Player player, Nodewar plugin, ChestGui previousGui, Territory territory) {

        int invSize = 6;
        String display = territory.getDisplay() + "'s Empires - Page 1";
        ChestGui gui = new ChestGui(invSize, AdaptMessage.playerMessage(player, display));

        PaginatedPane paginatedPane = new PaginatedPane(0, 0, 9, invSize);

        OutlinePane background = new OutlinePane(0, 0, 9, gui.getRows(), Pane.Priority.LOWEST);
        background.addItem(new GuiItem(new ItemStack(Material.BLACK_STAINED_GLASS_PANE)));
        background.setRepeat(true);
        gui.addPane(background);

        int page = 0;
        while (true) {
            StaticPane staticPane = initPane(player, plugin, gui, previousGui, paginatedPane, territory, page);
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

    private static StaticPane initPane(Player player, Nodewar plugin, ChestGui gui, ChestGui previousGui, PaginatedPane paginatedPane, Territory territory, int page) {
        StaticPane staticPane = new StaticPane(0, 0, 9, 6);

        ArrayList<Empire> empires = new ArrayList<>(EmpireManager.getEmpireManager().getEmpires().values());

        int index = 45 * page;

        int posY = 0;
        int posX = 0;

        staticPane.addItem(new GuiItem(GUIs.createGuiItem(player, plugin, null, Material.BARRIER, "&9Previous Menu", null
                , GUIs.adaptLore(player, null)), event -> {
            previousGui.show(player);
        }), 4, 5);

        if (page > 0) {
            staticPane.addItem(new GuiItem(GUIs.createGuiItem(player, plugin, null, Material.ARROW, "&9Previous page", null
                    , GUIs.adaptLore(player, null)), event -> {
                paginatedPane.setPage(page - 1);
                gui.setTitle(AdaptMessage.playerMessage(player, territory.getDisplay() + "'s Empire - Page " + (page)));
                gui.update();
            }), 0, 5);
        }

        while (posY != 5) {
            while (posX != 9) {
                if (empires.size() - 1 < index) {
                    return staticPane;
                }
                Empire empire = empires.get(index);

                staticPane.addItem(new GuiItem(GUIs.createGuiItem(player, plugin, null, Material.RED_BANNER, empire.getDisplay(), null
                        , GUIs.adaptLore(player, null)), event -> {
                    territory.getObjective().cancel();
                    if (empire != EmpireManager.getEmpireManager().getNoEmpire()) {
                        AdaptMessage.playerMessage(player, AdaptMessage.territoryMessage(territory, LangManager.getMessage(LangMessage.TERRITORY_SET_EMPIRE)));
                    } else {
                        AdaptMessage.playerMessage(player, AdaptMessage.territoryMessage(territory, LangManager.getMessage(LangMessage.TERRITORY_NEUTRALIZE)));
                    }
                    previousGui.show(player);
                }), posX, posY);

                posX++;
                index++;
            }
            posX = 0;
            posY++;
        }

        if (empires.size() > index) {
            staticPane.addItem(new GuiItem(GUIs.createGuiItem(player, plugin, null, Material.SPECTRAL_ARROW, "&Next page", null
                    , GUIs.adaptLore(player, null)), event -> {
                paginatedPane.setPage(page + 1);
                gui.setTitle(AdaptMessage.playerMessage(player, territory.getDisplay() + "'s Empire - Page " + (page + 1)));
                gui.update();
            }), 8, 5);
        }
        return staticPane;
    }
}