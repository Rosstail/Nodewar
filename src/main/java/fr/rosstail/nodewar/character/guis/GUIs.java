package fr.rosstail.conquest.character.guis;

import fr.rosstail.conquest.Conquest;
import fr.rosstail.conquest.required.PlayerHead;
import fr.rosstail.conquest.required.lang.AdaptMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GUIs
{
    public static ItemStack createGuiItem(final Player player, final Conquest plugin, final FileConfiguration customConfig, final Material material, final String display, final String path, final List<String> lore) {
        ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', display));
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        if (customConfig != null) {
            String playerSkullName = customConfig.getString(path + ".player");
            if ((material == Material.PLAYER_HEAD || material == Material.LEGACY_SKULL_ITEM) && playerSkullName != null) {
                playerSkullName = AdaptMessage.playerMessage(player, playerSkullName);
                item = PlayerHead.gets(plugin).getPlayerHead(playerSkullName, item);
            }
            if (Integer.parseInt(Bukkit.getServer().getBukkitVersion().split("-")[0].split("\\.")[1]) >= 14) {
                meta.setCustomModelData(Integer.valueOf(customConfig.getInt(path + ".custom-model-data")));
            }
            if (customConfig.getString(path + ".unbreakable") != null && customConfig.getBoolean(path + ".unbreakable")) {
                meta.setUnbreakable(true);
            }
            if (meta instanceof Damageable) {
                ((Damageable)meta).setDamage(customConfig.getInt(path + ".durability"));
            }
        }
        else if ((material == Material.PLAYER_HEAD || material == Material.LEGACY_SKULL_ITEM) && player != null) {
            item = PlayerHead.gets(plugin).getPlayerHead(player.getName(), item);
        }
        item.setItemMeta(meta);
        return item;
    }
    
    public static List<String> adaptLore(final Player player, final List<String> list) {
        final ArrayList<String> lore = new ArrayList<String>();
        if (list != null) {
            for (String line : list) {
                lore.add(AdaptMessage.playerMessage(player, line));
            }
        }
        return lore;
    }
}
