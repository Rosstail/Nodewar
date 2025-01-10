package fr.rosstail.nodewar.player;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.TeamManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlayerData extends PlayerModel {

    private Player player;

    private final Set<ProtectedRegion> protectedRegionList = new HashSet<>();
    public PlayerData(Player player) {
        super(player);
        this.player = player;
    }

    public PlayerData(String uuid, String username) {
        super(uuid, username);
    }

    public PlayerData(Player player, PlayerModel playerModel) {
        super(playerModel);
        this.player = player;
    }

    public NwITeam getTeam() {
        return TeamManager.getManager().getPlayerTeam(player);
    }

    public Set<ProtectedRegion> getProtectedRegionList() {
        return protectedRegionList;
    }
}
