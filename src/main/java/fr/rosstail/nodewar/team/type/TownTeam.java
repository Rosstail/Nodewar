package fr.rosstail.nodewar.team.type;

import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.*;
import fr.rosstail.nodewar.team.member.TeamMember;
import fr.rosstail.nodewar.team.member.TeamMemberModel;
import fr.rosstail.nodewar.team.relation.TownyTeamRelation;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TownTeam implements NwITeam {

    private final Town town;
    private final TeamModel model;

    public TownTeam(Town town) {
        this.town = town;
        TeamModel model1 = StorageManager.getManager().selectTeamModelByName(town.getName().toLowerCase());
        if (model1 == null) {
            model1 = new TeamModel(town.getName(), town.getFormattedName(), town.getName(), town.getMapColorHexCode());
            StorageManager.getManager().insertTeamModel(model1);
        }
        this.model = model1;
    }

    public TeamModel getModel() {
        return model;
    }

    @Override
    public int getID() {
        return model.getId();
    }

    @Override
    public String getName() {
        return town.getName().toLowerCase();
    }

    @Override
    public String getDisplay() {
        return town.getFormattedName();
    }

    @Override
    public String getShortName() {
        return town.getName();
    }

    @Override
    public String getTeamColor() {
        return "#" + town.getMapColorHexCode();
    }

    @Override
    public void setTeamColor(String value) {
        town.setMapColorHexCode(value);
    }

    @Override
    public boolean isOpen() {
        return town.isOpen();
    }

    @Override
    public void setOpen(boolean value) {
        town.setOpen(value);
    }

    @Override
    public boolean isPermanent() {
        return true;
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
        return this.model.getCreationDate();
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
        //TODO
        return null;
    }

    @Override
    public void setBanner(ItemStack banner) {
        //TODO
    }

    @Override
    public Map<Player, TeamMember> getOnlineMemberMap() {
        Map<Player, TeamMember> playerTeamMemberMap = new HashMap<>();
        town.getResidents().forEach(resident -> {
            Player player = resident.getPlayer();
            TeamMemberModel memberModel = new TeamMemberModel(getID(), 0, 1, new Timestamp(System.currentTimeMillis()), resident.getName());
            TeamMember member = new TeamMember(player, this, memberModel);
            playerTeamMemberMap.put(player, member);
        });
        return playerTeamMemberMap;
    }

    @Override
    public Map<String, TeamMember> getMemberMap() {
        Map<String, TeamMember> memberMap = new HashMap<>();

        town.getResidents().forEach(resident -> {
            int rank = 1;
            if (resident.isMayor()) {
                rank = 5;
            }

            TeamMemberModel memberModel = new TeamMemberModel(getID(), 0, rank, new Timestamp(System.currentTimeMillis()), resident.getName());
            memberMap.put(resident.getName(), new TeamMember(this, memberModel));
        });
        return memberMap;
    }

    @Override
    public int getOnlineMemberAmount() {
        return (int) town.getResidents().stream().filter(Resident::isOnline).count();
    }

    @Override
    public int getMemberAmount() {
        return town.getResidents().size();
    }

    @Override
    public boolean hasPlayerEnoughClearance(Player player) {
        return false;
    }

    @Override
    public Map<NwITeam, TeamIRelation> getRelations() {
        Map<NwITeam, TeamIRelation> teamRelationMap = new HashMap<>();
        teamRelationMap.putAll(getAllies());
        teamRelationMap.putAll(getEnemies());
        teamRelationMap.putAll(getTruce());
        return teamRelationMap;
    }

    @Override
    public Map<NwITeam, TeamIRelation> getAllies() {
        Map<NwITeam, TeamIRelation> alliesMap = new HashMap<>();
        town.getAllies().forEach(allyTown -> {
            TownTeam allyTeam = (TownTeam) TeamManager.getManager().getTeam(allyTown.getName());
            TownyTeamRelation townyAllyRelation = new TownyTeamRelation(this, allyTeam, RelationType.ALLY);
            alliesMap.put(allyTeam, townyAllyRelation);
        });
        return alliesMap;
    }

    @Override
    public Map<NwITeam, TeamIRelation> getTruce() {
        Map<NwITeam, TeamIRelation> truceMap = new HashMap<>();
        town.getTrustedTowns().forEach(trustTown -> {
            TownTeam trustTeam = (TownTeam) TeamManager.getManager().getTeam(trustTown.getName());
            TownyTeamRelation townyTRustRelation = new TownyTeamRelation(this, trustTeam, RelationType.TRUCE);
            truceMap.put(trustTeam, townyTRustRelation);
        });
        return truceMap;
    }

    @Override
    public Map<NwITeam, TeamIRelation> getEnemies() {
        Map<NwITeam, TeamIRelation> enemiesMap = new HashMap<>();
        town.getEnemies().forEach(enemyTown -> {
            TownTeam enemyTeam = (TownTeam) TeamManager.getManager().getTeam(enemyTown.getName());
            TownyTeamRelation townyEnemyRelation = new TownyTeamRelation(this, enemyTeam, RelationType.ENEMY);
            enemiesMap.put(enemyTeam, townyEnemyRelation);
        });
        return enemiesMap;
    }

    @Override
    public TeamIRelation getIRelation(NwITeam relationTeam) {
        TownTeam relationTownTeam;
        if (relationTeam != null) {
            relationTownTeam = (TownTeam) relationTeam;
            if (town.getAllies().contains(relationTownTeam.getTown())) {
                return new TownyTeamRelation(this, relationTownTeam, RelationType.ALLY);
            } else if (town.getTrustedTowns().contains(relationTownTeam.getTown())) {
                return new TownyTeamRelation(this, relationTownTeam, RelationType.TRUCE);
            } else if (town.getEnemies().contains(relationTownTeam.getTown())) {
                return new TownyTeamRelation(this, relationTownTeam, RelationType.ENEMY);
            }
        }
        return null;
    }

    @Override
    public boolean canInvitePlayer(Player sender, @NotNull Player receiver) {
        return false;
    }

    public Town getTown() {
        return town;
    }
}
