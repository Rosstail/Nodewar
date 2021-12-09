package fr.rosstail.conquest.territory;

import java.util.HashMap;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import java.util.ArrayList;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import java.util.TimerTask;
import java.util.Timer;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.List;
import org.bukkit.World;
import org.bukkit.entity.Player;
import java.util.Map;

public class WorldGuardInteractions
{
    private static Map<Player, World> playersWorlds;
    private static Map<Player, List<ProtectedRegion>> playersProtectedRegions;
    
    public static void setPlayersDataForWorlds(final List<World> worldList) {
        System.out.println(worldList.toString());
        final Timer T = new Timer();
        T.schedule(new TimerTask() {
            @Override
            public void run() {
                for (final World world : worldList) {
                    final List<Player> players = world.getPlayers();
                    for (final Player player : players) {
                        setPlayersData(player);
                    }
                }
            }
        }, 0L, 50L);
    }
    
    private static void setPlayersData(final Player player) {
        final LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        final Location location = localPlayer.getLocation();
        final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        final RegionQuery query = container.createQuery();
        final ApplicableRegionSet set = query.getApplicableRegions(location);
        final Object[] playerRegions = set.getRegions().toArray();
        final ArrayList<ProtectedRegion> playerProtectedRegions = new ArrayList<ProtectedRegion>();
        for (final Object o : playerRegions) {
            playerProtectedRegions.add((ProtectedRegion)o);
        }
        setPlayerWorld(player);
        setPlayersProtectedRegions(player, playerProtectedRegions);
    }
    
    private static void setPlayerWorld(final Player player) {
        if (WorldGuardInteractions.playersWorlds.containsKey(player)) {
            WorldGuardInteractions.playersWorlds.replace(player, player.getWorld());
        }
        else {
            WorldGuardInteractions.playersWorlds.put(player, player.getWorld());
        }
    }
    
    private static void setPlayersProtectedRegions(final Player player, final List<ProtectedRegion> regions) {
        if (WorldGuardInteractions.playersProtectedRegions.containsKey(player)) {
            WorldGuardInteractions.playersProtectedRegions.replace(player, regions);
        }
        else {
            WorldGuardInteractions.playersProtectedRegions.put(player, regions);
        }
    }
    
    public static Map<Player, List<ProtectedRegion>> getPlayersProtectedRegions() {
        return WorldGuardInteractions.playersProtectedRegions;
    }
    
    public static Map<Player, World> getPlayersWorlds() {
        return WorldGuardInteractions.playersWorlds;
    }
    
    static {
        WorldGuardInteractions.playersWorlds = new HashMap<>();
        WorldGuardInteractions.playersProtectedRegions = new HashMap<>();
    }
}
