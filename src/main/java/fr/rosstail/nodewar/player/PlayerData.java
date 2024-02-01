package fr.rosstail.nodewar.player;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.TeamDataManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

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
        removePlayerGroup(this.nwTeam);
        setPlayerGroup(nwTeam);
        this.nwTeam = nwTeam;
    }

    public void setPlayerGroup(final NwTeam nwTeam) {
        if (nwTeam != null) {
            Nodewar.getPermissions().playerAddGroup(null, this.player, nwTeam.getModel().getName());
        }
    }

    private void removePlayerGroup(final NwTeam nwTeam) {
        if (nwTeam != null) {
            Nodewar.getPermissions().playerRemoveGroup(null, this.player, nwTeam.getModel().getName());
        }
    }

    public void removeTeam() {
        this.nwTeam = null;
    }

    public List<ProtectedRegion> getProtectedRegionList() {
        return protectedRegionList;
    }
}
