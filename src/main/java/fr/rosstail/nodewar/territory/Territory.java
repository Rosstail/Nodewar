package fr.rosstail.nodewar.territory;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.rosstail.nodewar.territory.objective.Objective;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Territory extends TerritoryModel {

    private List<ProtectedRegion> regionList;
    private List<Player> playerList;

    private final List<ProtectedRegion> protectedRegionList = new ArrayList<>();

    private Objective objective;


}
