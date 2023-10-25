package fr.rosstail.nodewar.player;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.rosstail.nodewar.team.Team;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerData extends PlayerModel {

    private Team team;

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

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public void removeTeam() {
        this.team = null;
    }

    public List<ProtectedRegion> getProtectedRegionList() {
        return protectedRegionList;
    }
}
