package fr.rosstail.nodewar.territory;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.RelationType;
import fr.rosstail.nodewar.team.TeamManager;
import fr.rosstail.nodewar.team.type.NwTeam;
import fr.rosstail.nodewar.territory.attackrequirements.AttackRequirements;
import fr.rosstail.nodewar.territory.battle.Battle;
import fr.rosstail.nodewar.territory.battle.BattleManager;
import fr.rosstail.nodewar.territory.bossbar.TerritoryBossBar;
import fr.rosstail.nodewar.territory.objective.NwObjective;
import fr.rosstail.nodewar.territory.objective.ObjectiveManager;
import fr.rosstail.nodewar.territory.objective.types.ObjectiveDemolition;
import fr.rosstail.nodewar.territory.territorycommands.TerritoryCommands;
import fr.rosstail.nodewar.webmap.TerritoryWebmap;
import fr.rosstail.nodewar.webmap.WebmapManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class Territory extends TerritoryModel {

    private final World world;
    private final Map<NwTeam, List<Player>> teamPlayerList = new HashMap<>();

    private final List<Territory> subTerritoryList = new ArrayList<>();

    private final NwObjective objective;

    private Battle currentBattle;
    private Battle previousBattle;

    private final TerritoryBossBar territoryBossBar;

    private final AttackRequirements attackRequirements;
    private final TerritoryWebmap webmapInfo;

    private final List<Player> players = new ArrayList<>();

    private final Map<RelationType, BossBar> relationBossBarMap = new HashMap<>();

    private NwITeam ownerNwITeam;
    private NwITeam previousNwITeam;

    private List<TerritoryCommands> territoryCommandsList = new ArrayList<>();

    Territory(@NotNull TerritoryModel model) {
        super(model);

        TerritoryModel parentModel = TerritoryManager.getTerritoryManager().getTerritoryPresetModelFromMap(getPresetName());

        this.world = Bukkit.getWorld(getWorldName());
        this.objective = ObjectiveManager.getManager().setUpObjective(this, model.getObjectiveTypeName());

        this.territoryBossBar = new TerritoryBossBar(getBossBarModel(), parentModel.getBossBarModel());
        this.attackRequirements = new AttackRequirements(this, getAttackRequirementsModel(), parentModel.getAttackRequirementsModel());
        this.webmapInfo = new TerritoryWebmap(this, getTerritoryWebmapModel(), parentModel.getTerritoryWebmapModel());

        if (ConfigData.getConfigData().bossbar.enabled) {
            for (RelationType relation : RelationType.values()) {
                String territoryName;
                if (getOwnerITeam() != null) {
                    territoryName = LangManager.getMessage(LangMessage.TERRITORY_BOSSBAR_GLOBAL_OCCUPIED);
                } else {
                    territoryName = LangManager.getMessage(LangMessage.TERRITORY_BOSSBAR_GLOBAL_WILD);
                }

                territoryName = AdaptMessage.getAdaptMessage().adaptTerritoryMessage(territoryName, this);

                relationBossBarMap.put(relation, Bukkit.createBossBar(
                        territoryName,
                        ConfigData.getConfigData().bossbar.stringBarColorMap.get(relation.toString().toLowerCase()),
                        territoryBossBar.getBarStyle()
                ));
            }
        }
    }

    public void updateRegionList() {
        final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        final RegionManager regions = container.get(BukkitAdapter.adapt(world));
        if (regions != null) {
            WebmapManager.getManager().eraseTerritoryMarker(this);
            WebmapManager.getManager().eraseTerritorySurface(this);
            WebmapManager.getManager().eraseLineBetweenTerritories(this, this);
            WebmapManager.getManager().eraseTerritoryMarker(this);
            WebmapManager.getManager().drawTerritoryMarker(this);
        }
        updateAllBossBar();
    }

    public void updateAllBossBarText() {
        if (players.isEmpty()) {
            return;
        }
        getRelationBossBarMap().forEach((relationType, bossBar) -> {
            String bossBarTitle;
            if (currentBattle != null && currentBattle.isBattleStarted()) {
                bossBarTitle = LangManager.getMessage(LangMessage.TERRITORY_BOSSBAR_GLOBAL_BATTLE);
            } else if (currentBattle != null && currentBattle.isBattleOnEnd()) {
                if (currentBattle.isBattleEnded()) {
                    bossBarTitle = LangManager.getMessage(LangMessage.TERRITORY_BOSSBAR_GLOBAL_BATTLE_ENDED);
                } else {
                    bossBarTitle = LangManager.getMessage(LangMessage.TERRITORY_BOSSBAR_GLOBAL_BATTLE_ENDING);
                }
            } else if (getOwnerITeam() != null) {
                bossBarTitle = LangManager.getMessage(LangMessage.TERRITORY_BOSSBAR_GLOBAL_OCCUPIED);
            } else {
                bossBarTitle = LangManager.getMessage(LangMessage.TERRITORY_BOSSBAR_GLOBAL_WILD);
            }
            bossBarTitle = AdaptMessage.getAdaptMessage().adaptMessage(AdaptMessage.getAdaptMessage().adaptTerritoryMessage(bossBarTitle, this));
            bossBar.setTitle(bossBarTitle);
        });
    }

    public void updateAllBossBar() {
        if (territoryBossBar == null) {
            return;
        }
        getRelationBossBarMap().forEach((relationType, bossBar) -> {
            bossBar.removeAll();
        });
        updateAllBossBarText();

        getPlayers().forEach(this::addPlayerToBossBar);
    }

    public void addPlayerToBossBar(Player player) {
        if (territoryBossBar == null) {
            return;
        }

        RelationType type = RelationType.NEUTRAL;
        NwITeam territoryUsedTeam = null;
        PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player.getName());
        NwITeam playerTeam = playerData.getTeam();

        if (ownerNwITeam != null) {
            territoryUsedTeam = ownerNwITeam;
        } else if (currentBattle != null) {
            if (currentBattle.getAdvantagedITeam() != null) {
                territoryUsedTeam = currentBattle.getAdvantagedITeam();
            }
        }

        if (territoryUsedTeam != null) {
            if (playerTeam != null) {
                if (territoryUsedTeam == playerTeam) {
                    type = RelationType.TEAM;
                } else if (playerTeam.getRelations().containsKey(territoryUsedTeam)) {
                    type = TeamManager.getManager().getTeamRelationType(playerTeam, ownerNwITeam);
                } else if (ConfigData.getConfigData().team.defaultRelation == RelationType.NEUTRAL) {
                    type = RelationType.CONTROLLED;
                } else {
                    type = ConfigData.getConfigData().team.defaultRelation;
                }
            }
        }
        getRelationBossBarMap().get(type).addPlayer(player);
    }

    public List<Territory> getSubTerritoryList() {
        return subTerritoryList;
    }

    public NwObjective getObjective() {
        return objective;
    }

    public AttackRequirements getAttackRequirements() {
        return attackRequirements;
    }

    public World getWorld() {
        return world;
    }

    public List<ProtectedRegion> getProtectedRegionList() {
        List<ProtectedRegion> protectedRegionList = new ArrayList<>();
        final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        final RegionManager regions = container.get(BukkitAdapter.adapt(world));

        if (regions != null) {
            protectedRegionList = getRegionStringSet()
                    .stream().filter(regions::hasRegion)
                    .map(regions::getRegion)
                    .collect(Collectors.toList());
        }
        updateAllBossBar();
        return protectedRegionList;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Map<RelationType, BossBar> getRelationBossBarMap() {
        return relationBossBarMap;
    }

    public NwITeam getOwnerITeam() {
        return ownerNwITeam;
    }

    public void setOwnerITeam(NwITeam ownerITeam) {
        setPreviousNwITeam(this.ownerNwITeam);
        this.ownerNwITeam = ownerITeam;
        setOwnerName(ownerITeam != null ? ownerITeam.getName() : null);
        StorageManager.getManager().updateTerritoryModel(this, true);
        updateTerritoryRegionGroups();
    }

    public NwITeam getPreviousNwITeam() {
        return previousNwITeam;
    }

    public void setPreviousNwITeam(NwITeam previousNwITeam) {
        this.previousNwITeam = previousNwITeam;
    }

    public void updateTerritoryRegionGroups() {
        getProtectedRegionList().forEach(protectedRegion -> {
            protectedRegion.getMembers().getGroups().stream().filter(s -> (
                    s.startsWith("nw_")
            )).forEach(s -> protectedRegion.getMembers().removeGroup(s));
            if (getOwnerITeam() != null) {
                protectedRegion.getMembers().addGroup("nw_" + getOwnerITeam().getName());
            }
        });
    }

    public Battle getPreviousBattle() {
        return previousBattle;
    }

    public Battle getCurrentBattle() {
        return currentBattle;
    }

    public void setCurrentBattle(Battle currentBattle) {
        this.currentBattle = currentBattle;
    }

    public void setPreviousBattle(Battle previousBattle) {
        this.previousBattle = previousBattle;
    }

    public List<TerritoryCommands> getTerritoryCommandList() {
        return territoryCommandsList;
    }

    public void setTerritoryCommandList(List<TerritoryCommands> territoryCommandsList) {
        this.territoryCommandsList = territoryCommandsList;
    }

    public void setupBattle() {
        if (currentBattle != null) {
            setPreviousBattle(currentBattle);
        }

        BattleManager.getBattleManager().setUpBattle(this, getObjectiveTypeName());
        updateAllBossBar();
    }

    public Set<Player> getEffectivePlayers() {
        return getPlayers().stream().filter(player ->
                !player.isSleeping() &&
                        (player.getGameMode().equals(GameMode.SURVIVAL) || player.getGameMode().equals(GameMode.ADVENTURE))).collect(Collectors.toSet());
    }

    public Map<NwITeam, Set<Player>> getNwITeamEffectivePlayerAmountOnTerritory() {
        Map<NwITeam, Set<Player>> iTeamPlayerMap = new HashMap<>();
        if (getOwnerITeam() != null) {
            iTeamPlayerMap.put(getOwnerITeam(), new HashSet<>()); //guarantee
        }

        for (Player player : getEffectivePlayers()) {
            PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player.getName());
            NwITeam playerNwTeam = playerData.getTeam();

            if (playerNwTeam != null) {
                if (!iTeamPlayerMap.containsKey(playerNwTeam)) {
                    iTeamPlayerMap.put(playerNwTeam, new HashSet<>(Collections.singleton(player)));
                } else {
                    iTeamPlayerMap.get(playerNwTeam).add(player);
                }
            }
        }

        return iTeamPlayerMap;
    }

    public void resetCommandsDelay() {
        territoryCommandsList.forEach(territoryCommands -> {
            territoryCommands.setNextOccurrence(System.currentTimeMillis() + territoryCommands.getTerritoryCommandsModel().getInitialDelay());
        });
    }

    public Location getCenter() {
        List<ProtectedRegion> protectedRegionList = getProtectedRegionList();
        if (protectedRegionList.isEmpty()) {
            return null;
        }
        ProtectedRegion firstRegion = protectedRegionList.get(0);

        BlockVector3 min = firstRegion.getMinimumPoint();
        BlockVector3 max = firstRegion.getMaximumPoint();

        double centerX;

        if (webmapInfo.isxSet()) {
            centerX = webmapInfo.getX();
        } else {
            centerX = (min.getX() + max.getX()) / 2.0;
        }
        double centerY;

        if (webmapInfo.isySet()) {
            centerY = webmapInfo.getY();
        } else {
            centerY = (min.getY() + max.getY()) / 2.0;
        }
        double centerZ;

        if (webmapInfo.iszSet()) {
            centerZ = webmapInfo.getZ();
        } else {
            centerZ = (min.getZ() + max.getZ()) / 2.0;
        }

        return new Location(world, centerX, centerY, centerZ);
    }

    public TerritoryWebmap getWebmapInfo() {
        return webmapInfo;
    }

    public String adaptMessage(String message) {
        if (message == null) {
            return null;
        }

        message = message.replaceAll("\\[territory_description]", LangManager.getMessage(LangMessage.TERRITORY_DESCRIPTION))
                .replaceAll("\\[territory_desc_line]", Matcher.quoteReplacement(String.join("\n", getDescription())))
                .replaceAll("\\[territory_id]", String.valueOf(getId()))
                .replaceAll("\\[territory_prefix]", getPrefix())
                .replaceAll("\\[territory_suffix]", getSuffix())
                .replaceAll("\\[territory_name]", getName())
                .replaceAll("\\[territory_display]", getDisplay())
                .replaceAll("\\[territory_world]", getWorldName())
                .replaceAll("\\[territory_(preset|type)]", getPresetName())
                .replaceAll("\\[territory_(preset|type)_display]", getPresetDisplay());

        if (isUnderProtection()) {
            message = message.replaceAll("\\[territory_protected]", LangManager.getMessage(LangMessage.TERRITORY_PROTECTED))
                    .replaceAll("\\[territory_protected_short]", LangManager.getMessage(LangMessage.TERRITORY_PROTECTED_SHORT));
        } else {
            message = message.replaceAll("\\[territory_protected]", LangManager.getMessage(LangMessage.TERRITORY_VULNERABLE))
                    .replaceAll("\\[territory_protected_short]", LangManager.getMessage(LangMessage.TERRITORY_VULNERABLE_SHORT));
        }

        if (attackRequirements != null) {
            message = attackRequirements.adaptMessage(message);
        }


        message = AdaptMessage.getAdaptMessage().adaptTeamMessage(
                message.replaceAll("\\[territory_team", "[team"),
                getOwnerITeam()
        );

        if (currentBattle != null) {
            message = currentBattle.adaptMessage(message);
        }

        if (objective != null) {
            message = objective.adaptMessage(message);
        }

        return message;
    }
}