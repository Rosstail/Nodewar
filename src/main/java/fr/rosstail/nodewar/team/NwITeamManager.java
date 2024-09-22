package fr.rosstail.nodewar.team;

import fr.rosstail.nodewar.team.member.TeamMember;
import fr.rosstail.nodewar.team.member.TeamMemberModel;
import fr.rosstail.nodewar.team.relation.NwTeamRelationRequest;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    TeamMember addOnlineTeamMember(NwITeam nwITeam, Player player);

    TeamMemberModel addTeamMember(NwITeam nwITeam, String playerName);
    void deleteOnlineTeamMember(NwITeam nwITeam, Player player, boolean disband);

    void deleteTeamMember(NwITeam nwITeam, String playerName, boolean disband);

    TeamIRelation getRelation(NwITeam firstTeam, NwITeam secondTeam);
    Map<NwITeam, TeamIRelation> getRelationMap(NwITeam nwITeam);

    void createRelation(NwITeam originITeam, NwITeam targetITeam, RelationType type);

    void deleteRelation(NwITeam originTeam, NwITeam targetITeam);

    NwTeamRelationRequest getTeamRelationRequest(NwITeam firstTeam, NwITeam secondTeam);

    Set<NwTeamRelationRequest> getTeamRelationRequestSet();
    void createRelationRequest(NwITeam originITeam, NwITeam targetITeam, RelationType type);

    void deleteRelationRequest(NwITeam originTeam, NwITeam targetITeam);
}
