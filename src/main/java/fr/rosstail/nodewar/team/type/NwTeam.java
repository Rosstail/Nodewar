package fr.rosstail.nodewar.team.type;

import fr.rosstail.nodewar.permission.PermissionManager;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.RelationType;
import fr.rosstail.nodewar.team.TeamIRelation;
import fr.rosstail.nodewar.team.TeamModel;
import fr.rosstail.nodewar.team.member.TeamMember;
import fr.rosstail.nodewar.team.relation.NwTeamRelation;
import fr.rosstail.nodewar.utils.Cache;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NwTeam implements NwITeam {

    private final TeamModel model;
    private final Map<Player, TeamMember> onlineMemberMap = new HashMap<>();
    private final Map<NwTeam, NwTeamRelation> declaredRelationMap = new HashMap<>();
    private final Map<String, Cache> cacheMap = new HashMap<>();

    public NwTeam(TeamModel model) {
        this.model = model;
        PermissionManager.getManager().createGroup(getName());
    }

    public TeamModel getModel() {
        return model;
    }


    @Override
    public NwTeamRelation getIRelation(NwITeam relationITeam) {
        NwTeam relationTeam = (NwTeam) relationITeam;
        return declaredRelationMap.get(relationTeam);
    }

    @Override
    public boolean canInvitePlayer(Player sender, @NotNull Player receiver) {
        return false;
    }

    @Override
    public int getID() {
        return model.getId();
    }

    @Override
    public String getName() {
        return model.getName();
    }

    @Override
    public String getShortName() {
        return model.getShortName();
    }

    @Override
    public String getTeamColor() {
        return model.getTeamColor();
    }

    @Override
    public void setTeamColor(String value) {
        model.setTeamColor(value);
    }

    @Override
    public boolean isOpen() {
        return model.isOpen();
    }

    @Override
    public void setOpen(boolean value) {
        model.setOpen(value);
    }

    @Override
    public boolean isPermanent() {
        return model.isPermanent();
    }

    @Override
    public void setPermanent(boolean value) {
        model.setPermanent(value);
    }

    @Override
    public boolean isOpenRelation() {
        return model.isOpenRelation();
    }

    @Override
    public void setOpenRelation(boolean value) {
        model.setOpenRelation(value);
    }

    @Override
    public Date getCreationDate() {
        return model.getCreationDate();
    }

    @Override
    public Date getLastUpdate() {
        return model.getLastUpdate();
    }

    @Override
    public void setLastUpdate(Timestamp value) {
        model.setLastUpdate(value);
    }

    @Override
    public ItemStack getBanner() {
        //TODO
        return null;
    }

    @Override
    public void setBanner(ItemStack banner) {
        //TODO
    }

    @Override
    public Map<String, Cache> getCacheMap() {
        return cacheMap;
    }

    @Override
    public String adaptMessage(String message) {
        if (message == null) {
            return null;
        }

        if (cacheMap.containsKey(message)) {
            return cacheMap.get(message).getValue();
        }

        String newMessage = message
                .replaceAll("\\[team(_name)?]", getName())
                .replaceAll("\\[team_id]", String.valueOf(getID()))
                .replaceAll("\\[team_disp(lay)?]", getDisplay())
                .replaceAll("\\[team_(cdisp|color_display)]", "{" + getTeamColor() + "}" + getDisplay())
                .replaceAll("\\[team_short]", getShortName())
                .replaceAll("\\[team_(cshort|color_short)]", "{" + getTeamColor() + "}" + getShortName())
                .replaceAll("\\[team_(clr|color)?]", getTeamColor())
                .replaceAll("\\[team_open]", String.valueOf(isOpen()))
                .replaceAll("\\[team_perm(anent)?]", String.valueOf(isPermanent()))
                .replaceAll("\\[team_creation(_date)?]", String.valueOf(getCreationDate()));

        Cache cache = new Cache(message, newMessage);
        cacheMap.put(message, cache);

        return newMessage;
    }

    @Override
    public Map<Player, TeamMember> getOnlineMemberMap() {
        return onlineMemberMap;
    }

    @Override
    public Map<String, TeamMember> getMemberMap() {
        Map<String, TeamMember> teamMemberMap = new HashMap<>();
        model.getTeamMemberModelMap().forEach((integer, teamMemberModel) -> {
            teamMemberMap.put(teamMemberModel.getUsername(), new TeamMember(this, teamMemberModel));
        });
        return teamMemberMap;
    }

    @Override
    public int getOnlineMemberAmount() {
        return onlineMemberMap.size();
    }

    @Override
    public int getMemberAmount() {
        return model.getTeamMemberModelMap().size();
    }

    @Override
    public boolean hasPlayerEnoughClearance(Player player) {
        return false;
    }

    @Override
    public Map<NwITeam, TeamIRelation> getRelations() {
        return new HashMap<>(declaredRelationMap);
    }

    @Override
    public Map<NwITeam, TeamIRelation> getAllies() {
        Map<NwITeam, TeamIRelation> alliesMap = new HashMap<>();
        declaredRelationMap.entrySet().stream().filter(nwTeamNwTeamRelationEntry -> (
                nwTeamNwTeamRelationEntry.getValue().getType() == RelationType.ALLY
        )).forEach(nwTeamNwTeamRelationEntry -> {
            alliesMap.put(nwTeamNwTeamRelationEntry.getKey(), nwTeamNwTeamRelationEntry.getValue());
        });
        return alliesMap;
    }

    @Override
    public Map<NwITeam, TeamIRelation> getTruce() {
        Map<NwITeam, TeamIRelation> truceMap = new HashMap<>();
        declaredRelationMap.entrySet().stream().filter(nwTeamNwTeamRelationEntry -> (
                nwTeamNwTeamRelationEntry.getValue().getType() == RelationType.TRUCE
        )).forEach(nwTeamNwTeamRelationEntry -> {
            truceMap.put(nwTeamNwTeamRelationEntry.getKey(), nwTeamNwTeamRelationEntry.getValue());
        });
        return truceMap;
    }

    @Override
    public Map<NwITeam, TeamIRelation> getEnemies() {
        Map<NwITeam, TeamIRelation> enemiesMap = new HashMap<>();
        declaredRelationMap.entrySet().stream().filter(nwTeamNwTeamRelationEntry -> (
                nwTeamNwTeamRelationEntry.getValue().getType() == RelationType.ENEMY
        )).forEach(nwTeamNwTeamRelationEntry -> {
            enemiesMap.put(nwTeamNwTeamRelationEntry.getKey(), nwTeamNwTeamRelationEntry.getValue());
        });
        return enemiesMap;
    }

    public void addRelation(NwTeam targetTeam, NwTeamRelation relation) {
        declaredRelationMap.put(targetTeam, relation);
    }

    public void removeRelation(NwTeam targetTeam) {
        declaredRelationMap.remove(targetTeam);
    }

    @Override
    public String getDisplay() {
        return model.getDisplay();
    }
}
