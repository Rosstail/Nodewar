package fr.rosstail.nodewar.team.type;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.*;
import fr.rosstail.nodewar.team.member.TeamMember;
import fr.rosstail.nodewar.team.member.TeamMemberModel;
import fr.rosstail.nodewar.team.relation.NwTeamRelation;
import me.ulrich.clans.data.ClanData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.sql.Timestamp;
import java.util.Base64;
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
        return clanData.getTag().substring(0, Math.min(clanData.getTag().length(), ConfigData.getConfigData().team.maximumShortNameLength));
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
        return clanData.getSettings().isOpened();
    }

    @Override
    public void setOpen(boolean value) {
        clanData.getSettings().setOpened(value);
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
        return model.getLastUpdate();
    }

    @Override
    public void setLastUpdate(Timestamp value) {
        this.model.setLastUpdate(value);
    }

    @Override
    public ItemStack getBanner() {
        try {
            byte[] bytes = Base64.getDecoder().decode(clanData.getBanner());
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            BukkitObjectInputStream bois = new BukkitObjectInputStream(bais);
            ItemStack item = (ItemStack) bois.readObject();
            bois.close();
            return item;
        } catch (Exception e) {
            throw new IllegalArgumentException("Impossible de désérialiser l'ItemStack", e);
        }
    }

    @Override
    public void setBanner(ItemStack banner) {
        //TODO
        clanData.setBanner(null);
    }

    @Override
    public Map<Player, TeamMember> getOnlineMemberMap() {
        Map<Player, TeamMember> playerTeamMemberMap = new HashMap<>();
        clanData.getOnlineMembers().forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
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
        clanData.getMembers().forEach(uuid -> {
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

        clanData.getRivalAlly().getAlly().forEach(allyUuid -> {
            UcTeam allyUcTeam = (UcTeam) teamManager.getStringTeamMap().values().stream().filter(nwITeam ->
                    ((UcTeam) nwITeam).clanData.getId().equals(allyUuid)
            ).findFirst().orElse(null);
            if (allyUcTeam != null) {
                NwTeamRelation allyRelation = new NwTeamRelation(this, allyUcTeam, RelationType.ALLY, null);
                allies.put(allyUcTeam, allyRelation);
            }
        });

        return allies;
    }

    @Override
    public Map<NwITeam, TeamIRelation> getTruce() {
        return new HashMap<>(); // No Truce in Uc
    }

    @Override
    public Map<NwITeam, TeamIRelation> getEnemies() {
        TeamManager teamManager = TeamManager.getManager();
        Map<NwITeam, TeamIRelation> enemies = new HashMap<>();

        clanData.getRivalAlly().getRival().forEach(rivalUuid -> {
            UcTeam rivalUcTeam = (UcTeam) teamManager.getStringTeamMap().values().stream().filter(nwITeam ->
                    ((UcTeam) nwITeam).clanData.getId().equals(rivalUuid)
            ).findFirst().orElse(null);

            if (rivalUcTeam != null) {
                NwTeamRelation enemyRelation = new NwTeamRelation(this, rivalUcTeam, RelationType.ENEMY, null);
                enemies.put(rivalUcTeam, enemyRelation);
            }
        });

        return enemies;
    }

    @Override
    public TeamIRelation getIRelation(NwITeam relationTeam) {
        return getRelations().get(relationTeam);
    }

    @Override
    public boolean canInvitePlayer(Player sender, @NotNull Player receiver) {
        return false;
    }

    public ClanData getClanData() {
        return this.clanData;
    }
}
