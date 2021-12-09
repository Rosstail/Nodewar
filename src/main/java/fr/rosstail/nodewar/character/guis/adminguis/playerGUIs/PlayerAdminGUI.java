package fr.rosstail.nodewar.character.guis.adminguis.playerGUIs;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import fr.rosstail.nodewar.character.guis.GUIs;
import fr.rosstail.nodewar.character.datahandlers.PlayerInfo;
import fr.rosstail.nodewar.required.lang.AdaptMessage;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.character.datahandlers.PlayerInfo;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerAdminGUI {
    public static void initGUI(Player player, Nodewar plugin, Player target, ChestGui previousGui) {

        int invSize = 1;
        ChestGui gui = new ChestGui(invSize, AdaptMessage.playerMessage(target, target.getName()));

        StaticPane staticPane = initPane(player, plugin, gui, previousGui, target);

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

    private static StaticPane initPane(Player player, Nodewar plugin, ChestGui gui, ChestGui previousGui, Player target) {
        StaticPane staticPane = new StaticPane(0, 0, 9, 1);

        staticPane.addItem(new GuiItem(GUIs.createGuiItem(target, plugin, null, Material.RED_BANNER, "&9Empire", null
                , GUIs.adaptLore(target, null)), event -> {
            PlayerInfo targetInfo = PlayerInfo.gets(target);
            if (event.isRightClick()) {
                targetInfo.leaveEmpire();
            } else {
                PlayerAdminEmpireGUIs.initGUI(player, plugin, gui, target);
            }
        }), 6, 0);
        return staticPane;
    }

}