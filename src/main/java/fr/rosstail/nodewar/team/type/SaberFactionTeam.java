package fr.rosstail.nodewar.team.type;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.*;
import fr.rosstail.nodewar.team.member.TeamMember;
import fr.rosstail.nodewar.team.member.TeamMemberModel;
import fr.rosstail.nodewar.team.relation.NwTeamRelation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SaberFactionTeam implements NwITeam {

    private final Faction faction;
    private final String factionTeamName;
    private final TeamModel model;

    public SaberFactionTeam(Faction faction) {
        this.faction = faction;
        this.factionTeamName = ChatColor.stripColor(faction.getTag().toLowerCase());
        TeamModel model1 = StorageManager.getManager().selectTeamModelByName(factionTeamName);
        if (model1 == null) {
            model1 = new TeamModel(factionTeamName, faction.getTag(), faction.getTag(), TeamManager.getManager().generateRandomColor());
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
        return factionTeamName;
    }

    @Override
    public String getDisplay() {
        return this.faction.getTag();
    }

    @Override
    public String getShortName() {
        return this.faction.getTag();
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
        return this.faction.getOpen();
    }

    @Override
    public void setOpen(boolean value) {
        this.faction.setOpen(value);
    }

    @Override
    public boolean isPermanent() {
        return this.faction.isPermanent();
    }

    @Override
    public void setPermanent(boolean value) {
        this.faction.setPermanent(value);
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
        return new Date(this.faction.getFoundedDate());
    }

    @Override
    public Date getLastUpdate() {
        return this.model.getLastUpdate();
    }

    @Override
    public void setLastUpdate(Timestamp value) {
        this.model.setLastUpdate(value);
    }

    @Override
    public ItemStack getBanner() {
        return faction.getBanner();
    }

    @Override
    public void setBanner(ItemStack banner) {
        faction.setBannerPattern(banner);
    }

    @Override
    public Map<Player, TeamMember> getOnlineMemberMap() {
        Map<Player, TeamMember> playerTeamMemberMap = new HashMap<>();
        faction.getOnlinePlayers().forEach(player -> {
            TeamMemberModel memberModel = new TeamMemberModel(getID(), 0, 1, new Timestamp(System.currentTimeMillis()), player.getName());
            TeamMember member = new TeamMember(player, this, memberModel);
            playerTeamMemberMap.put(player, member);
        });
        return playerTeamMemberMap;
    }

    @Override
    public Map<String, TeamMember> getMemberMap() {
        Map<String, TeamMember> playerTeamMemberMap = new HashMap<>();
        faction.getFPlayers().forEach(fPlayer -> {
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(fPlayer.getId()));
            TeamMemberModel memberModel = new TeamMemberModel(getID(), 0, 1, new Timestamp(System.currentTimeMillis()), player.getName());
            TeamMember member = new TeamMember((Player) player, this, memberModel);
            playerTeamMemberMap.put(player.getName(), member);
        });
        return playerTeamMemberMap;
    }

    @Override
    public int getOnlineMemberAmount() {
        return this.faction.getOnlinePlayers().size();
    }

    @Override
    public int getMemberAmount() {
        return this.faction.getFPlayers().size();
    }

    @Override
    public boolean hasPlayerEnoughClearance(Player player) {
        return false;
    }

    @Override
    public Map<NwITeam, TeamIRelation> getRelations() {
        Map<NwITeam, TeamIRelation> factionRelationMap = new HashMap<>();
        factionRelationMap.putAll(getAllies());
        factionRelationMap.putAll(getEnemies());
        factionRelationMap.putAll(getTruce());
        return factionRelationMap;
    }

    @Override
    public Map<NwITeam, TeamIRelation> getAllies() {
        TeamManager teamManager = TeamManager.getManager();
        Map<NwITeam, TeamIRelation> allies = new HashMap<>();

        Factions factions = Factions.getInstance();

        factions.getAllFactions().stream().filter(otherFaction -> (
                otherFaction != faction &&
                        otherFaction.isWilderness() &&
                        faction.getRelationTo(otherFaction).isAlly()
        )).forEach(otherFaction -> {
            SaberFactionTeam allySaberFactionTeam = (SaberFactionTeam) teamManager.getStringTeamMap().values().stream().filter(nwITeam ->
                    ((SaberFactionTeam) nwITeam).faction == otherFaction).findFirst().orElse(null);

            if (allySaberFactionTeam != null) {
                NwTeamRelation allyRelation = new NwTeamRelation(this, allySaberFactionTeam, RelationType.ALLY, null);
                allies.put(allySaberFactionTeam, allyRelation);
            }
        });

        return allies;
    }

    @Override
    public Map<NwITeam, TeamIRelation> getTruce() {
        TeamManager teamManager = TeamManager.getManager();
        Map<NwITeam, TeamIRelation> truces = new HashMap<>();

        Factions factions = Factions.getInstance();

        factions.getAllFactions().stream().filter(otherFaction -> (
                otherFaction != faction &&
                        otherFaction.isWilderness() &&
                        faction.getRelationTo(otherFaction).isTruce()
        )).forEach(otherFaction -> {
            SaberFactionTeam truceSaberFactionTeam = (SaberFactionTeam) teamManager.getStringTeamMap().values().stream().filter(nwITeam ->
                    ((SaberFactionTeam) nwITeam).faction == otherFaction).findFirst().orElse(null);

            if (truceSaberFactionTeam != null) {
                NwTeamRelation truceRelation = new NwTeamRelation(this, truceSaberFactionTeam, RelationType.TRUCE, null);
                truces.put(truceSaberFactionTeam, truceRelation);
            }
        });

        return truces;
    }

    @Override
    public Map<NwITeam, TeamIRelation> getEnemies() {
        TeamManager teamManager = TeamManager.getManager();
        Map<NwITeam, TeamIRelation> enemies = new HashMap<>();

        Factions factions = Factions.getInstance();

        factions.getAllFactions().stream().filter(otherFaction -> (
                otherFaction != faction &&
                        otherFaction.isWilderness() &&
                        faction.getRelationTo(otherFaction).isEnemy()
                )).forEach(otherFaction -> {
            SaberFactionTeam rivalSaberFactionTeam = (SaberFactionTeam) teamManager.getStringTeamMap().values().stream().filter(nwITeam ->
                    ((SaberFactionTeam) nwITeam).faction == otherFaction).findFirst().orElse(null);

            if (rivalSaberFactionTeam != null) {
                NwTeamRelation enemyRelation = new NwTeamRelation(this, rivalSaberFactionTeam, RelationType.ENEMY, null);
                enemies.put(rivalSaberFactionTeam, enemyRelation);
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

    public Faction getFaction() {
        return faction;
    }
}
