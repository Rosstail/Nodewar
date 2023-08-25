package fr.rosstail.nodewar.team;

import fr.rosstail.nodewar.team.type.NwTeam;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

public interface NwITeamManager {

    void loadTeams();

    NwITeam getPlayerTeam(Player player);
    NwITeam getTeam(String name);
    Map<String, NwITeam> getStringITeamMap();
    void addITeam(String name, NwITeam team);
    void removeITeam(String name);

    HashSet<Object> getInviteHashSet();

    void addTeamInvite(NwITeam nwITeam, Player sender, @NotNull Player receiver);
    void removeTeamInvite(List<NwTeamInvite> inviteList);

    void addTeamMember(NwITeam nwITeam, Player player);
    void deleteTeamMember(NwITeam nwITeam, Player player, boolean disband);

    TeamIRelation getRelation(NwITeam firstTeam, NwITeam secondTeam);
    Map<NwITeam, TeamIRelation> getRelationMap(NwITeam nwITeam);

    void createRelation(NwITeam originITeam, NwITeam targetITeam, RelationType type);

    void deleteRelation(NwITeam originTeam, NwITeam targetITeam);
}
