package fr.rosstail.nodewar.team;

import fr.rosstail.nodewar.team.member.TeamMember;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

public interface NwITeam {
    int getID();
    String getName();
    String getDisplay();
    String getShortName();
    String getTeamColor();
    void setTeamColor(String value);
    boolean isOpen();
    void setOpen(boolean value);
    boolean isPermanent();
    void setPermanent(boolean value);
    boolean isOpenRelation();
    void setOpenRelation(boolean value);
    Date getCreationDate();
    Date getLastUpdate();
    void setLastUpdate(Timestamp value);

    Map<Player, TeamMember> getOnlineMemberMap();

    Map<String, TeamMember> getMemberMap();

    int getOnlineMemberAmount();
    int getMemberAmount();

    /**
     * Has player enough clearance to perform an action
     */
    boolean hasPlayerEnoughClearance(Player player);

    Map<NwITeam, TeamIRelation> getRelations();

    Map<NwITeam, TeamIRelation> getAllies();
    Map<NwITeam, TeamIRelation> getTruce();
    Map<NwITeam, TeamIRelation> getEnemies();

    TeamIRelation getIRelation(NwITeam relationTeam);

    boolean canInvitePlayer(Player sender, @NotNull Player receiver);
}
