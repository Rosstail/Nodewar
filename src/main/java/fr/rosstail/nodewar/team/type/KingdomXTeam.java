package fr.rosstail.nodewar.team.type;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.*;
import fr.rosstail.nodewar.team.member.TeamMember;
import fr.rosstail.nodewar.team.member.TeamMemberModel;
import fr.rosstail.nodewar.team.relation.NwTeamRelation;
import fr.rosstail.nodewar.utils.Cache;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.kingdoms.constants.group.Kingdom;
import org.kingdoms.constants.group.model.relationships.KingdomRelation;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class KingdomXTeam implements NwITeam {
    private final Kingdom kingdom;
    private final TeamModel model;
    private final Map<String, Cache> cacheMap = new HashMap<>();

    public KingdomXTeam(Kingdom kingdom) {
        this.kingdom = kingdom;
        TeamModel model1 = StorageManager.getManager().selectTeamModelByName(kingdom.getName().toLowerCase());
        if (model1 == null) {
            model1 = new TeamModel(kingdom.getName().toLowerCase(), kingdom.getName(), kingdom.getName(), TeamManager.getManager().generateRandomColor());
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
        return kingdom.getName().toLowerCase();
    }

    @Override
    public String getDisplay() {
        return kingdom.getName();
    }

    @Override
    public String getShortName() {
        return kingdom.getName().substring(0, Math.min(kingdom.getName().length(), ConfigData.getConfigData().team.maximumShortNameLength));
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
        return !kingdom.isPrivate();
    }

    @Override
    public void setOpen(boolean value) {
        kingdom.setPrivate(!value);
    }

    @Override
    public boolean isPermanent() {
        return kingdom.isPermanent();
    }

    @Override
    public void setPermanent(boolean value) {
        kingdom.setPermanent(value);
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
        return model.getLastUpdate();
    }

    @Override
    public void setLastUpdate(Timestamp value) {
        this.model.setLastUpdate(value);
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

        String newMessage = message.replaceAll("\\[team_name]", getName())
                .replaceAll("\\[team_id]", String.valueOf(getID()))
                .replaceAll("\\[team_display]", getDisplay())
                .replaceAll("\\[team_color_display]", "{" + getTeamColor() + "}" + getDisplay())
                .replaceAll("\\[team_short]", getShortName())
                .replaceAll("\\[team_color_short]", "{" + getTeamColor() + "}" + getShortName())
                .replaceAll("\\[team_color]", getTeamColor())
                .replaceAll("\\[team_open]", String.valueOf(isOpen()))
                .replaceAll("\\[team_permanent]", String.valueOf(isPermanent()))
                .replaceAll("\\[team_creation_date]", String.valueOf(getCreationDate()));

        Cache cache = new Cache(message, newMessage);
        cacheMap.put(message, cache);

        return newMessage;
    }

    @Override
    public Map<Player, TeamMember> getOnlineMemberMap() {
        Map<Player, TeamMember> playerTeamMemberMap = new HashMap<>();
        kingdom.getOnlineMembers().forEach(player -> {
            if (player != null) {
                TeamMemberModel memberModel = new TeamMemberModel(getID(), 0, 1, new Timestamp(System.currentTimeMillis()), player.getName());
                TeamMember member = new TeamMember(player, this, memberModel);
                playerTeamMemberMap.put(player, member);
            }
        });
        return playerTeamMemberMap;
    }

    @Override
    public Map<String, TeamMember> getMemberMap() {
        Map<String, TeamMember> playerTeamMemberMap = new HashMap<>();
        kingdom.getMembers().forEach(uuid -> {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            TeamMember member;
            TeamMemberModel memberModel = new TeamMemberModel(getID(), 0, 1, new Timestamp(System.currentTimeMillis()), player.getName());

            if (player.isOnline()) {
                member = new TeamMember((Player) player, this, memberModel);
            } else {
                member = new TeamMember(this, memberModel);
            }
            playerTeamMemberMap.put(player.getName(), member);
        });
        return playerTeamMemberMap;
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
        TeamManager teamManager = TeamManager.getManager();
        Map<NwITeam, TeamIRelation> allies = new HashMap<>();

        kingdom.getRelations().entrySet().stream().filter(uuidKingdomRelationEntry -> (
                        uuidKingdomRelationEntry.getValue().equals(KingdomRelation.ALLY)))
                .forEach(uuidKingdomRelationEntry -> {
                    KingdomXTeam allyKingdomXTeam = (KingdomXTeam) teamManager.getStringTeamMap()
                            .values().stream().filter(nwITeam ->
                                    ((KingdomXTeam) nwITeam).kingdom.getId().equals(uuidKingdomRelationEntry.getKey()));
                    NwTeamRelation allyRelation =
                            new NwTeamRelation(this, allyKingdomXTeam, RelationType.ALLY, null);
                    allies.put(allyKingdomXTeam, allyRelation);
                });

        return allies;
    }

    @Override
    public Map<NwITeam, TeamIRelation> getTruce() {
        TeamManager teamManager = TeamManager.getManager();
        Map<NwITeam, TeamIRelation> allies = new HashMap<>();

        kingdom.getRelations().entrySet().stream().filter(uuidKingdomRelationEntry -> (
                        uuidKingdomRelationEntry.getValue().equals(KingdomRelation.TRUCE)))
                .forEach(uuidKingdomRelationEntry -> {
                    KingdomXTeam allyKingdomXTeam = (KingdomXTeam) teamManager.getStringTeamMap()
                            .values().stream().filter(nwITeam ->
                                    ((KingdomXTeam) nwITeam).kingdom.getId().equals(uuidKingdomRelationEntry.getKey()));
                    NwTeamRelation truceRelation =
                            new NwTeamRelation(this, allyKingdomXTeam, RelationType.TRUCE, null);
                    allies.put(allyKingdomXTeam, truceRelation);
                });

        return allies;
    }

    @Override
    public Map<NwITeam, TeamIRelation> getEnemies() {
        TeamManager teamManager = TeamManager.getManager();
        Map<NwITeam, TeamIRelation> allies = new HashMap<>();

        kingdom.getRelations().entrySet().stream().filter(uuidKingdomRelationEntry -> (
                        uuidKingdomRelationEntry.getValue().equals(KingdomRelation.ENEMY)))
                .forEach(uuidKingdomRelationEntry -> {
                    KingdomXTeam allyKingdomXTeam = (KingdomXTeam) teamManager.getStringTeamMap()
                            .values().stream().filter(nwITeam ->
                                    ((KingdomXTeam) nwITeam).kingdom.getId().equals(uuidKingdomRelationEntry.getKey()));
                    NwTeamRelation enemyRelation =
                            new NwTeamRelation(this, allyKingdomXTeam, RelationType.ENEMY, null);
                    allies.put(allyKingdomXTeam, enemyRelation);
                });

        return allies;
    }

    @Override
    public TeamIRelation getIRelation(NwITeam relationTeam) {
        return getRelations().get(relationTeam);
    }

    @Override
    public boolean canInvitePlayer(Player sender, @NotNull Player receiver) {
        return false;
    }

    public Kingdom getKingdom() {
        return this.kingdom;
    }
}
