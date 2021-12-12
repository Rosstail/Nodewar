
package fr.rosstail.nodewar.guis.adminguis.nodewarguis;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import fr.rosstail.nodewar.guis.GUIs;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.required.lang.AdaptMessage;
import fr.rosstail.nodewar.territory.zonehandlers.CapturePoint;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PointGUIs {
    public static void initGUI(Player player, Nodewar plugin, CapturePoint point, ChestGui previousGui) {

        int invSize = 2;
        ChestGui gui = new ChestGui(invSize, AdaptMessage.playerMessage(player, point.getDisplay()));

        StaticPane staticPane = initPane(player, plugin, gui, previousGui, point);

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

    private static StaticPane initPane(Player player, Nodewar plugin, ChestGui gui, ChestGui previousGui, CapturePoint point) {
        StaticPane staticPane = new StaticPane(0, 0, 9, 2);

        staticPane.addItem(new GuiItem(GUIs.createGuiItem(player, plugin, null, Material.RED_BANNER, "&9Empire", null
                , GUIs.adaptLore(player, null)), event -> {
            PointGUIs.initGUI(player, plugin, point, previousGui);
            if (event.isRightClick()) {
                point.cancelAttack(null);
                System.out.println(point.getDisplay() + " is now neutral");
            } else {
                PointEmpireGUIs.initGUI(player, plugin, gui, point);
            }
        }), 4, 0);

        staticPane.addItem(new GuiItem(GUIs.createGuiItem(player, plugin, null, Material.BARRIER, "&9Previous Menu", null
                , GUIs.adaptLore(player, null)), event -> {
            previousGui.show(player);
        }), 4, 1);
        return staticPane;
    }
}