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
import fr.rosstail.nodewar.territory.battle.BattleStatus;
import fr.rosstail.nodewar.territory.bossbar.TerritoryBossBar;
import fr.rosstail.nodewar.territory.objective.NwObjective;
import fr.rosstail.nodewar.territory.objective.ObjectiveManager;
import fr.rosstail.nodewar.territory.territorycommands.TerritoryCommands;
import fr.rosstail.nodewar.utils.Cache;
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

    private final String genericDisplay;
    private final String genericDesc;
    private final String genericWebmapDesc;
    private final Map<RelationType, String> genericBossbarTitles = new HashMap<>();

    private TerritoryActivity currentActivity = TerritoryActivity.INACTIVE;
    private long lastDescUpdate = 0L;
    private long lastActivityUpdate = System.currentTimeMillis();

    private Map<String, Cache> textCache = new HashMap<>();

    Territory(@NotNull TerritoryModel model) {
        super(model);

        TerritoryModel parentModel = TerritoryManager.getTerritoryManager().getTerritoryPresetModelFromMap(getPresetName());

        this.world = Bukkit.getWorld(getWorldName());
        this.objective = ObjectiveManager.getManager().setUpObjective(this, model.getObjectiveTypeName());

        this.territoryBossBar = new TerritoryBossBar(getBossBarModel(), parentModel.getBossBarModel());
        this.attackRequirements = new AttackRequirements(this, getAttackRequirementsModel(), parentModel.getAttackRequirementsModel());
        this.webmapInfo = new TerritoryWebmap(this, getTerritoryWebmapModel(), parentModel.getTerritoryWebmapModel());

        String genericText = adaptGenericMessage(LangManager.getMessage(LangMessage.TERRITORY_DESCRIPTION));

        this.genericDisplay = genericText;
        this.genericDesc = genericText;
        this.genericWebmapDesc = genericText;

        if (ConfigData.getConfigData().bossbar.enabled) {
            for (RelationType relation : RelationType.values()) {
                String territoryName;
                if (getOwnerITeam() != null) {
                    territoryName = LangManager.getMessage(LangMessage.TERRITORY_BOSSBAR_GLOBAL_OCCUPIED);
                } else {
                    territoryName = LangManager.getMessage(LangMessage.TERRITORY_BOSSBAR_GLOBAL_WILD);
                }

                territoryName = AdaptMessage.getInstance().adaptTerritoryMessage(territoryName, this);

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
            if (currentBattle != null && currentBattle.isStarted()) {
                bossBarTitle = LangManager.getMessage(LangMessage.TERRITORY_BOSSBAR_GLOBAL_BATTLE);
            } else if (currentBattle != null && currentBattle.isBattleOnEnd()) {
                if (currentBattle.isEnded()) {
                    bossBarTitle = LangManager.getMessage(LangMessage.TERRITORY_BOSSBAR_GLOBAL_BATTLE_ENDED);
                } else {
                    bossBarTitle = LangManager.getMessage(LangMessage.TERRITORY_BOSSBAR_GLOBAL_BATTLE_ENDING);
                }
            } else if (getOwnerITeam() != null) {
                bossBarTitle = LangManager.getMessage(LangMessage.TERRITORY_BOSSBAR_GLOBAL_OCCUPIED);
            } else {
                bossBarTitle = LangManager.getMessage(LangMessage.TERRITORY_BOSSBAR_GLOBAL_WILD);
            }
            bossBarTitle = AdaptMessage.getInstance().adaptMessage(AdaptMessage.getInstance().adaptTerritoryMessage(bossBarTitle, this));
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
        double centerY;
        double centerZ;

        if (webmapInfo.isxSet()) {
            centerX = webmapInfo.getX();
        } else {
            centerX = (min.getX() + max.getX()) / 2.0;
        }

        if (webmapInfo.isySet()) {
            centerY = webmapInfo.getY();
        } else {
            centerY = (min.getY() + max.getY()) / 2.0;
        }

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

    public String adaptGenericMessage(String message) {
        return message.replaceAll("\\[terr(iroty)?_desc(ription)?]", LangManager.getMessage(LangMessage.TERRITORY_DESCRIPTION))
                .replaceAll("\\[terr(iroty)?_desc_line]", Matcher.quoteReplacement(String.join("\n", getDescription())))
                .replaceAll("\\[terr(iroty)?_id]", String.valueOf(getId()))
                .replaceAll("\\[terr(iroty)?_prefix]", getPrefix())
                .replaceAll("\\[terr(iroty)?_suffix]", getSuffix())
                .replaceAll("\\[terr(iroty)?_name]", getName())
                .replaceAll("\\[terr(iroty)?_disp(lay)?]", getDisplay())
                .replaceAll("\\[terr(iroty)?_world]", getWorldName())
                .replaceAll("\\[terr(iroty)?_(preset|type)]", getPresetName())
                .replaceAll("\\[terr(iroty)?_(preset|type)_disp(lay)?]", getPresetDisplay());
    }

    public String adaptMessage(String message) {
        if (message == null) {
            return null;
        }

        if (textCache.containsKey(message)) {
            return textCache.get(message).getValue();
        }

        String newMessage = message;
        String protectionVulnerable = LangManager.getMessage(isUnderProtection() ? LangMessage.TERRITORY_PROTECTED : LangMessage.TERRITORY_VULNERABLE);
        String protectionVulnerableShort = LangManager.getMessage(isUnderProtection() ? LangMessage.TERRITORY_PROTECTED_SHORT : LangMessage.TERRITORY_VULNERABLE_SHORT);


        newMessage = adaptGenericMessage(newMessage)
                .replaceAll("\\[terr(itory)?_desc(ription)?]", genericDesc)
                .replaceAll("\\[terr(itory)?_desc_line]", Matcher.quoteReplacement(String.join("\n", getDescription())))
                .replaceAll("\\[terr(itory)?_id]", String.valueOf(getId()))
                .replaceAll("\\[terr(itory)?_prefix]", getPrefix())
                .replaceAll("\\[terr(itory)?_suffix]", getSuffix())
                .replaceAll("\\[terr(itory)?(_name)?]", getName())
                .replaceAll("\\[terr(itory)?_disp(lay)?]", getDisplay())
                .replaceAll("\\[terr(itory)?_world]", getWorldName())
                .replaceAll("\\[terr(itory)?_(preset|type)]", getPresetName())
                .replaceAll("\\[terr(itory)?_(preset|type)_disp(lay)?]", getPresetDisplay())
                .replaceAll("\\[terr(itory)?_protected]", protectionVulnerable)
                .replaceAll("\\[terr(itory)?_protected_short]", protectionVulnerableShort);

        if (attackRequirements != null) {
            newMessage = attackRequirements.adaptMessage(newMessage);
        }


        newMessage = AdaptMessage.getInstance().adaptTeamMessage(
                newMessage.replaceAll("\\[terr(iroty)?_team", "[team"),
                getOwnerITeam()
        );

        if (currentBattle != null) {
            newMessage = currentBattle.adaptMessage(newMessage);
        }

        if (objective != null) {
            newMessage = objective.adaptMessage(newMessage);
        }

        Cache cache = new Cache(message, newMessage);
        textCache.put(message, cache);

        return newMessage;
    }

    public void updateDesc() {
        long now = System.currentTimeMillis();
        if (lastDescUpdate + (currentActivity.delayBetweenUpdates * 1000L) <= now) {

            if (currentActivity != TerritoryActivity.BATTLE) {
                updateAllBossBar();
            }

            setLastDescUpdate(now);
        }
    }

    public void updateActivity() {
        Battle battle = currentBattle;
        TerritoryActivity activity = currentActivity;
        TerritoryActivity newActivity = null;
        long now = System.currentTimeMillis();

        Set<Map.Entry<String, Cache>> toDelete = textCache.entrySet().stream().filter(dictionary -> (
                dictionary.getValue().getLastUpdate() + dictionary.getValue().getLifespan() * 1000 < now
        )).collect(Collectors.toSet());

        if (!toDelete.isEmpty()) {
            textCache.entrySet().removeAll(toDelete);
        }

        if (battle.getBattleStatus() == BattleStatus.ONGOING) {
            if (activity != TerritoryActivity.BATTLE) {
                newActivity = TerritoryActivity.BATTLE;
            }
        } else if (activity == TerritoryActivity.EMPTY || activity == TerritoryActivity.INACTIVE) {
            if (!players.isEmpty()) {
                newActivity = TerritoryActivity.DEFAULT;
            }
            if (activity == TerritoryActivity.EMPTY && lastActivityUpdate + 60000L < System.currentTimeMillis()) { // inactive after 1 minute
                newActivity = TerritoryActivity.INACTIVE;
            }
        } else if (players.isEmpty()) {
            newActivity = TerritoryActivity.EMPTY;
        }

        if (newActivity != null) {
            setCurrentActivity(newActivity);
            setLastActivityUpdate(System.currentTimeMillis());
        }
    }

    public String getGenericDisplay() {
        return genericDisplay;
    }

    public String getGenericDesc() {
        return genericDesc;
    }

    public String getGenericWebmapDesc() {
        return genericWebmapDesc;
    }

    public TerritoryActivity getCurrentActivity() {
        return currentActivity;
    }

    public void setCurrentActivity(TerritoryActivity activity) {
        System.out.println(getName() + " goes from " + currentActivity.name() + " to " + activity.name());
        this.currentActivity = activity;
    }

    public void setLastActivityUpdate(long lastActivityUpdate) {
        this.lastActivityUpdate = lastActivityUpdate;
    }

    public void setLastDescUpdate(long lastDescUpdate) {
        this.lastDescUpdate = lastDescUpdate;
    }

    public long getLastDescUpdate() {
        return lastDescUpdate;
    }
}