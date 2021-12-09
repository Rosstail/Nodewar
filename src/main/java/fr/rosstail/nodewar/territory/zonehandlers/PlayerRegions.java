package fr.rosstail.nodewar.territory.zonehandlers;

import java.util.HashMap;
import java.util.HashSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.Set;
import org.bukkit.entity.Player;
import java.util.Map;

public class PlayerRegions
{
    private static Map<Player, Set<ProtectedRegion>> playerRegions;
    private static Map<ProtectedRegion, Set<Player>> regionsPlayer;
    
    public static void setRegionsPlayers(final Map<Player, Set<ProtectedRegion>> playerRegions) {
        PlayerRegions.playerRegions = playerRegions;
    }
    
    public static Map<Player, Set<ProtectedRegion>> getPlayerRegions() {
        return PlayerRegions.playerRegions;
    }
    
    public static Map<ProtectedRegion, Set<Player>> getRegionsPlayer() {
        return PlayerRegions.regionsPlayer;
    }
    
    public static void setRegionsPlayer(final Map<ProtectedRegion, Set<Player>> regionsPlayer) {
        PlayerRegions.regionsPlayer = regionsPlayer;
    }
    
    private static void initRegionPlayer(final ProtectedRegion region) {
        if (!PlayerRegions.regionsPlayer.containsKey(region)) {
            PlayerRegions.regionsPlayer.put(region, new HashSet<Player>());
        }
    }
    
    private static void updatePlayerInRegion(final Player player, final ProtectedRegion region) {
        initRegionPlayer(region);
        if (PlayerRegions.playerRegions.containsKey(player) && PlayerRegions.playerRegions.get(player).contains(region)) {
            PlayerRegions.regionsPlayer.get(region).add(player);
        }
        else {
            PlayerRegions.regionsPlayer.get(region).remove(player);
        }
    }
    
    public static Set<Player> getPlayersInRegion(final ProtectedRegion region) {
        initRegionPlayer(region);
        return PlayerRegions.regionsPlayer.get(region);
    }
    
    public static void updatePlayerInRegions(final Player player, final Set<ProtectedRegion> regions) {
        regions.forEach(region -> updatePlayerInRegion(player, region));
    }
    
    static {
        PlayerRegions.playerRegions = new HashMap<Player, Set<ProtectedRegion>>();
        PlayerRegions.regionsPlayer = new HashMap<ProtectedRegion, Set<Player>>();
    }
}
