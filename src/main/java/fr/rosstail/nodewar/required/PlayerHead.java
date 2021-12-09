package fr.rosstail.nodewar.required;

import java.util.HashMap;

import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class PlayerHead
{
    private static Map<fr.rosstail.nodewar.Nodewar, PlayerHead> getSets;

    public PlayerHead() {
    }
    
    public static PlayerHead gets(final fr.rosstail.nodewar.Nodewar plugin) {
        if (!PlayerHead.getSets.containsKey(plugin)) {
            PlayerHead.getSets.put(plugin, new PlayerHead());
        }
        return PlayerHead.getSets.get(plugin);
    }
    
    public ItemStack getPlayerHead(final String playerName, final ItemStack item) {
        /*
        final boolean isNewVersion = Arrays.stream(Material.values()).map((Function<? super Material, ?>)Enum::name).collect((Collector<? super Object, ?, List<? super Object>>)Collectors.toList()).contains("PLAYER_HEAD");
        final Material type = Material.matchMaterial(isNewVersion ? "PLAYER_HEAD" : "SKULL_ITEM");
        final ItemStack newItem = new ItemStack(type, item.getAmount());
        if (!isNewVersion) {
            item.setDurability((short)3);
        }
        final SkullMeta skullMeta = (SkullMeta)newItem.getItemMeta();
        skullMeta.setOwner(playerName);
        newItem.setItemMeta(skullMeta);
        return newItem;
        */
        return null;
    }
    
    static {
        PlayerHead.getSets = new HashMap<>();
    }
}
