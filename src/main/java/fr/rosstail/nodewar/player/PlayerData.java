package fr.rosstail.nodewar.player;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.TeamManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerData extends PlayerModel {

    private Player player;

    private final List<ProtectedRegion> protectedRegionList = new ArrayList<>();
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

    public List<ProtectedRegion> getProtectedRegionList() {
        return protectedRegionList;
    }
}
