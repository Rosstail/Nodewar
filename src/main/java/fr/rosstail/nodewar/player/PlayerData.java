package fr.rosstail.nodewar.player;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.rosstail.nodewar.team.NwTeam;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static fr.rosstail.nodewar.permissionmannager.PermissionManagerHandler.removePlayerGroup;
import static fr.rosstail.nodewar.permissionmannager.PermissionManagerHandler.setPlayerGroup;

public class PlayerData extends PlayerModel {

    private Player player;
    private NwTeam nwTeam;

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

    public NwTeam getTeam() {
        return nwTeam;
    }

    public void setTeam(NwTeam nwTeam) {
        removePlayerGroup(player, nwTeam);
        if (nwTeam != null) {
            setPlayerGroup(player, nwTeam);
        }
        this.nwTeam = nwTeam;
    }

    public void removeTeam() {
        this.nwTeam = null;
    }

    public List<ProtectedRegion> getProtectedRegionList() {
        return protectedRegionList;
    }
}
