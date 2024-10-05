package fr.rosstail.nodewar.team.type;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.*;
import fr.rosstail.nodewar.team.member.TeamMember;
import fr.rosstail.nodewar.team.member.TeamMemberModel;
import fr.rosstail.nodewar.team.relation.TownyTeamRelation;
import me.ulrich.clans.data.ClanData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UcTeam implements NwITeam {
    private final ClanData clanData;
    private final TeamModel model;

    public UcTeam(ClanData clanData) {
        this.clanData = clanData;
        TeamModel model1 = StorageManager.getManager().selectTeamModelByName(clanData.getTag().toLowerCase());
        if (model1 == null) {
            model1 = new TeamModel(clanData.getTag().toLowerCase(), clanData.getTag(), clanData.getTag(), TeamManager.getManager().generateRandomColor());
            StorageManager.getManager().insertTeamModel(model1);
        }
        this.model = model1;
    }

    @Override
    public int getID() {
        return this.model.getId();
    }

    @Override
    public String getName() {
        return clanData.getTag().toLowerCase();
    }

    @Override
    public String getDisplay() {
        return clanData.getTag();
    }

    @Override
    public String getShortName() {
        return clanData.getTag().substring(0,Math.min(clanData.getTag().length(), ConfigData.getConfigData().team.maximumShortNameLength));
    }

    @Override
    public String getTeamColor() {
        return model.getTeamColor();
    }

    @Override
    public void setTeamColor(String value) {
        this.model.setTeamColor(value);
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public void setOpen(boolean value) {

    }

    @Override
    public boolean isPermanent() {
        return false;
    }

    @Override
    public void setPermanent(boolean value) {

    }

    @Override
    public boolean isOpenRelation() {
        return false;
    }

    @Override
    public void setOpenRelation(boolean value) {

    }

    @Override
    public Date getCreationDate() {
        return null;
    }

    @Override
    public Date getLastUpdate() {
        return null;
    }

    @Override
    public void setLastUpdate(Timestamp value) {

    }

    @Override
    public Map<Player, TeamMember> getOnlineMemberMap() {
        Map<Player, TeamMember> playerTeamMemberMap = new HashMap<>();
        clanData.getOnlineMembers().forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            TeamMemberModel memberModel = new TeamMemberModel(getID(), 0, 1, new Timestamp(System.currentTimeMillis()), player.getName());
            TeamMember member = new TeamMember(player, this, memberModel);
            playerTeamMemberMap.put(Bukkit.getPlayer(uuid), member);
        });
        return playerTeamMemberMap;
    }

    @Override
    public Map<String, TeamMember> getMemberMap() {
        return new HashMap<>();
    }

    @Override
    public int getOnlineMemberAmount() {
        return -1;
    }

    @Override
    public int getMemberAmount() {
        return -1;
    }

    /**
     * Has player enough clearance to perform an action
     *
     * @param player
     */
    @Override
    public boolean hasPlayerEnoughClearance(Player player) {
        return false;
    }

    @Override
    public Map<NwITeam, TeamIRelation> getRelations() {
        Map<NwITeam, TeamIRelation> clanRelationMap = new HashMap<>();
        clanRelationMap.putAll(getAllies());
        clanRelationMap.putAll(getEnemies());
        clanRelationMap.putAll(getTruce());
        return clanRelationMap;
    }

    @Override
    public Map<NwITeam, TeamIRelation> getAllies() {
        return new HashMap<>();
    }

    @Override
    public Map<NwITeam, TeamIRelation> getTruce() {
        return new HashMap<>();
    }

    @Override
    public Map<NwITeam, TeamIRelation> getEnemies() {
        return new HashMap<>();
    }

    @Override
    public TeamIRelation getIRelation(NwITeam relationTeam) {
        return null;
    }

    @Override
    public boolean canInvitePlayer(Player sender, @NotNull Player receiver) {
        return false;
    }

    public ClanData getClanData() {
        return this.clanData;
    }
}
