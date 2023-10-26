package fr.rosstail.nodewar.player;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.rosstail.nodewar.team.NwTeam;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerData extends PlayerModel {

    private NwTeam nwTeam;

    private final List<ProtectedRegion> protectedRegionList = new ArrayList<>();
    public PlayerData(Player player) {
        super(player);
    }

    public PlayerData(String uuid, String username) {
        super(uuid, username);
    }

    public PlayerData(PlayerModel playerModel) {
        super(playerModel);
    }

    public NwTeam getTeam() {
        return nwTeam;
    }

    public void setTeam(NwTeam nwTeam) {
        this.nwTeam = nwTeam;
    }

    public void removeTeam() {
        this.nwTeam = null;
    }

    public List<ProtectedRegion> getProtectedRegionList() {
        return protectedRegionList;
    }
}
