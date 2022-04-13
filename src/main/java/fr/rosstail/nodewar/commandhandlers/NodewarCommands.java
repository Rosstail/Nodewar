package fr.rosstail.nodewar.commandhandlers;

import fr.rosstail.nodewar.commandhandlers.enums.Commands;
import fr.rosstail.nodewar.datahandlers.PlayerInfo;
import fr.rosstail.nodewar.empires.Empire;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.empires.EmpireManager;
import fr.rosstail.nodewar.guis.adminguis.nodewarguis.WorldsGUIs;
import fr.rosstail.nodewar.guis.adminguis.playerGUIs.PlayerAdminGUI;
import fr.rosstail.nodewar.guis.playerguis.EmpiresListGUI;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.territory.eventhandlers.customevents.PointOwnerChange;
import fr.rosstail.nodewar.territory.eventhandlers.customevents.TerritoryOwnerChange;
import fr.rosstail.nodewar.territory.eventhandlers.customevents.TerritoryVulnerabilityToggle;
import fr.rosstail.nodewar.territory.zonehandlers.CapturePoint;
import fr.rosstail.nodewar.territory.zonehandlers.WorldTerritoryManager;
import fr.rosstail.nodewar.territory.zonehandlers.Territory;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.util.StringUtil;

import java.util.*;

public class NodewarCommands implements CommandExecutor, TabExecutor
{
    public static final NodewarCommands commands = new NodewarCommands(Nodewar.getInstance());
    private final Nodewar plugin;

    public NodewarCommands(final Nodewar plugin) {
        this.plugin = plugin;
    }
    
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!isSenderPlayer(sender) || sender.hasPermission(Commands.COMMAND_FE.getPermission())) {
            final String string = String.join(" ", args);
            if (string.startsWith(Commands.COMMAND_EMPIRE.getCommand())) {
                empireCommands(sender, string, args);
            } else if (string.startsWith(Commands.COMMAND_TERRITORY.getCommand())) {
                territoryCommands(sender, string, args);
            } else if (string.startsWith(Commands.COMMAND_ADMIN.getCommand())) {
                adminCommands(sender, string, args);
            } else {
                Player player = null;
                if (sender instanceof Player) {
                    player = ((Player) sender).getPlayer();
                }
                Player finalPlayer = player;
                LangManager.getListMessage(LangMessage.HELP).forEach(s ->
                        sender.sendMessage(AdaptMessage.playerMessage(finalPlayer, s)));
            }
        } else {
            doesNotHavePermission(sender, Commands.COMMAND_FE.getPermission());
        }
        return true;
    }


    private void empireCommands(final CommandSender sender, final String command, final String[] args) {
        if (!NodewarCommands.isSenderPlayer(sender) || sender.hasPermission(Commands.COMMAND_EMPIRE.getPermission())) {
            if (command.startsWith(Commands.COMMAND_EMPIRE_LIST.getCommand())) {
                this.empireListCommand(sender);
            } else if (command.startsWith(Commands.COMMAND_EMPIRE_JOIN.getCommand())) {
                this.empireJoinCommand(sender, args);
            } else if (command.startsWith(Commands.COMMAND_EMPIRE_CREATE.getCommand())) {
                this.empireCreateCommand(sender, args);
            } else if (command.startsWith(Commands.COMMAND_EMPIRE_EDIT.getCommand())) {
                this.empireEditCommand(sender, args);
            } else if (command.startsWith(Commands.COMMAND_EMPIRE_DISBAND.getCommand())) {
                this.empireDisbandCommand(sender, args);
            } else if (command.startsWith(Commands.COMMAND_EMPIRE_LEAVE.getCommand())) {
                this.leaveCommand(sender);
            } else {
                Player player = null;
                if (sender instanceof Player) {
                    player = ((Player) sender).getPlayer();
                }
                Player finalPlayer = player;
                LangManager.getListMessage(LangMessage.EMPIRE_HELP).forEach(s ->
                        sender.sendMessage(AdaptMessage.playerMessage(finalPlayer, s)));
            }
        }
        else {
            doesNotHavePermission(sender, Commands.COMMAND_EMPIRE.getPermission());
        }
    }

    private void empireListCommand(final CommandSender sender) {
        if (NodewarCommands.isSenderPlayer(sender)) {
            final Player player = (Player)sender;
            if (player.hasPermission(Commands.COMMAND_EMPIRE_LIST.getPermission())) {
                EmpiresListGUI.initGUI(player, this.plugin);
            }
            else {
                doesNotHavePermission(player, Commands.COMMAND_EMPIRE_LIST.getPermission());
            }
        }
        else {
            playerOnly(sender);
        }
    }

    private void empireJoinCommand(final CommandSender sender, final String[] args) {
        if (args.length > 2) {
            String empireName = args[2];
            EmpireManager empireManager = EmpireManager.getEmpireManager();
            Map<String, Empire> empires = empireManager.getEmpires();
            if (empires.containsKey(empireName)) {
                if (NodewarCommands.isSenderPlayer(sender)) {
                    final Player player = (Player) sender;
                    if (player.hasPermission(Commands.COMMAND_EMPIRE_JOIN.getPermission())) {
                        final PlayerInfo playerInfo = PlayerInfo.gets(player);
                        final Empire playerEmpire = playerInfo.getEmpire();
                        final Empire empire = empires.get(empireName);
                        if (!playerInfo.tryJoinEmpire(empire)) {
                            if (empire == null) {
                                player.sendMessage(AdaptMessage.playerMessage(player, LangManager.getMessage(LangMessage.EMPIRE_DOES_NOT_EXIST)));
                            } else if (playerEmpire != null) {
                                player.sendMessage(AdaptMessage.playerMessage(player, LangManager.getMessage(LangMessage.PLAYER_ALREADY_IN_EMPIRE)));
                            }
                        }
                    }
                    else {
                        doesNotHavePermission(player, Commands.COMMAND_EMPIRE_JOIN.getPermission());
                    }
                } else {
                    playerOnly(sender);
                }
            }
        }
        else {
            tooFewArguments(sender);
        }
    }

    private void empireCreateCommand(final CommandSender sender, final String[] args) {
        if (NodewarCommands.isSenderPlayer(sender)) {
            EmpireManager empireManager = EmpireManager.getEmpireManager();
            Player player = (Player) sender;
            PlayerInfo playerInfo = PlayerInfo.getPlayerInfoMap().get(player);
            Empire noEmpire = EmpireManager.getEmpireManager().getNoEmpire();
            if (player.hasPermission(Commands.COMMAND_EMPIRE_CREATE.getPermission())) {
                if (args.length > 2) {
                    String empireID = args[2];
                    if (empireManager.getEmpires().containsKey(args[2])) {
                        player.sendMessage("Empire already existing");
                    } else if (playerInfo.getEmpire() == noEmpire) {
                        Empire empire = empireManager.getSet(player, empireID);
                        sender.sendMessage("Empire created.");
                        playerInfo.tryJoinEmpire(empire);
                    } else {
                        player.sendMessage(AdaptMessage.playerMessage(player, LangManager.getMessage(LangMessage.PLAYER_ALREADY_IN_EMPIRE)));
                    }
                } else {
                    tooFewArguments(sender);
                }
            } else {
                doesNotHavePermission(player, Commands.COMMAND_EMPIRE_CREATE.getPermission());
            }
        } else {
            playerOnly(sender);
        }
    }

    private void empireDisbandCommand(final CommandSender sender, final String[] args) {
        if (NodewarCommands.isSenderPlayer(sender)) {
            EmpireManager empireManager = EmpireManager.getEmpireManager();
            Player player = (Player) sender;
            Empire playerEmpire = PlayerInfo.getPlayerInfoMap().get(player).getEmpire();
            Empire noEmpire = EmpireManager.getEmpireManager().getNoEmpire();
            if (player.hasPermission(Commands.COMMAND_EMPIRE_DISBAND.getPermission())) {
                if (playerEmpire != noEmpire) {
                    if (player.getUniqueId().toString().equals(playerEmpire.getOwnerUUID())) {
                        empireManager.getEmpires().remove(playerEmpire.getName());
                        for (Map.Entry<Player, PlayerInfo> entry : PlayerInfo.getPlayerInfoMap().entrySet()) {
                            PlayerInfo playerInfo = entry.getValue();
                            if (playerInfo.getEmpire() == playerEmpire) {
                                playerInfo.leaveEmpire();
                            }
                        }
                        playerEmpire.deleteConfig();
                        sender.sendMessage("Empire disbanded.");
                    } else {
                        sender.sendMessage("You are not the owner of " + playerEmpire.getDisplay());
                    }
                } else {
                    sender.sendMessage("Not in an empire.");
                }
            } else {
                doesNotHavePermission(player, Commands.COMMAND_EMPIRE_DISBAND.getPermission());
            }
        } else {
            playerOnly(sender);
        }
    }

    private void empireEditCommand(final CommandSender sender, final String[] args) {
        if (NodewarCommands.isSenderPlayer(sender)) {
            Player player = (Player) sender;
            if (args.length > 3) {
                Empire playerEmpire = PlayerInfo.getPlayerInfoMap().get(player).getEmpire();
                Empire noEmpire = EmpireManager.getEmpireManager().getNoEmpire();
                if (player.hasPermission(Commands.COMMAND_EMPIRE_EDIT.getPermission())) {
                    if (playerEmpire != noEmpire) {
                        if (player.getUniqueId().toString().equals(playerEmpire.getOwnerUUID())) {
                            if (args[2].equalsIgnoreCase("display")) {
                                playerEmpire.setDisplay(AdaptMessage.playerMessage(player, args[3]));
                                sender.sendMessage("Edited successfully !");
                                playerEmpire.saveConfigFile();
                            } else if (args[2].equalsIgnoreCase("friendlyfire")) {
                                playerEmpire.setFriendlyFire(args[3].equalsIgnoreCase("true"));
                                sender.sendMessage("Edited successfully !");
                                playerEmpire.saveConfigFile();
                            } else if (args[2].equalsIgnoreCase("bossbarcolor")){
                                try {
                                    playerEmpire.setBarColor(BarColor.valueOf(args[3]));
                                    sender.sendMessage("Edited successfully !");
                                    playerEmpire.saveConfigFile();
                                } catch (Exception e) {
                                    sender.sendMessage("Color doesn't match.");
                                }
                            } else if (args[2].equalsIgnoreCase("setowner")){
                                for (Player player1 : Bukkit.getOnlinePlayers()) {
                                    if (player1.getName().equalsIgnoreCase(args[3])) {
                                        if (PlayerInfo.gets(player1).getEmpire().equals(playerEmpire)) {
                                            playerEmpire.setOwnerUUID(player1.getUniqueId().toString());
                                            sender.sendMessage("Edited successfully !");
                                            playerEmpire.saveConfigFile();
                                        } else {
                                            sender.sendMessage("The player must be in the empire already.");
                                        }
                                        break;
                                    }
                                }
                            }
                        } else {
                            sender.sendMessage("You are not the owner of " + playerEmpire.getDisplay());
                        }
                    } else {
                        sender.sendMessage("Not in an empire.");
                    }
                } else {
                    tooFewArguments(sender);
                }
            } else {
                doesNotHavePermission(player, Commands.COMMAND_EMPIRE_EDIT.getPermission());
            }
        } else {
            playerOnly(sender);
        }
    }

    private void leaveCommand(final CommandSender sender) {
        if (NodewarCommands.isSenderPlayer(sender)) {
            Player player = (Player) sender;
            PlayerInfo playerInfo = PlayerInfo.gets(player);
            Empire playerEmpire = playerInfo.getEmpire();

            if (!Objects.equals(playerEmpire.getOwnerUUID(), player.getUniqueId().toString())) {
                PlayerInfo.gets((Player) sender).leaveEmpire();
            } else {
                sender.sendMessage("You must disband or delegate your empire to leave it.");
            }
        }
        else {
            playerOnly(sender);
        }
    }

    private void territoryCommands(final CommandSender sender, final String command, final String[] args) {
        if (args.length >= 3) {
            if (args[2].equalsIgnoreCase(Commands.COMMAND_TERRITORY_VULNERABILITY.getCommand())) {
                territoryVulnerabilityCommand(sender, command, args);
            } else if (args[2].equalsIgnoreCase(Commands.COMMAND_TERRITORY_SET_EMPIRE.getCommand())) {
                territoryPointSetEmpireCommand(sender, command, args);
            } else if (args[2].equalsIgnoreCase(Commands.COMMAND_TERRITORY_NEUTRALIZE.getCommand())) {
                territoryPointNeutralizeCommand(sender, command, args);
            }
        }
    }

    private void territoryVulnerabilityCommand(final CommandSender sender, final String command, final String[] args) {
        if (sender.hasPermission(Commands.COMMAND_TERRITORY_VULNERABILITY.getPermission())) {
            if (args.length >= 4) {
                String[] location = args[1].split("/");
                boolean value = Boolean.parseBoolean(args[3]);
                String worldName = location[0];
                World world = Bukkit.getWorld(worldName);

                WorldTerritoryManager worldTerritoryManager = WorldTerritoryManager.getUsedWorlds().get(world);
                if (worldTerritoryManager != null) {
                    if (location.length >= 2) {
                        String territoryName = location[1];

                        if (world != null && territoryName != null && WorldTerritoryManager.getUsedWorlds().containsKey(world)) {
                            Map<String, Territory> worldTerritories = worldTerritoryManager.getTerritories();
                            if (worldTerritories.containsKey(territoryName)) {
                                Territory territory = worldTerritories.get(territoryName);
                                TerritoryVulnerabilityToggle event = new TerritoryVulnerabilityToggle(territory, value);
                                Bukkit.getPluginManager().callEvent(event);
                                if (!event.isCancelled()) {
                                    sender.sendMessage(AdaptMessage.territoryMessage(territory,
                                            (value ? LangManager.getMessage(LangMessage.TERRITORY_VULNERABLE)
                                                    : LangManager.getMessage(LangMessage.TERRITORY_INVULNERABLE))));
                                }
                            }
                        }
                    } else {
                        worldTerritoryManager.getTerritories().forEach((s, territory) -> {
                            TerritoryVulnerabilityToggle event = new TerritoryVulnerabilityToggle(territory, value);
                            Bukkit.getPluginManager().callEvent(event);
                        });
                        sender.sendMessage(AdaptMessage.worldMessage(world,
                                (value ? LangManager.getMessage(LangMessage.WORLD_VULNERABLE)
                                        : LangManager.getMessage(LangMessage.WORLD_INVULNERABLE))));
                    }
                }
            } else {
                tooFewArguments(sender);
            }
        } else {
            NodewarCommands.doesNotHavePermission(sender, Commands.COMMAND_TERRITORY_VULNERABILITY.getPermission());
        }
    }

    private void territoryPointSetEmpireCommand(final CommandSender sender, final String command, final String[] args) {
        if (sender.hasPermission(Commands.COMMAND_TERRITORY_SET_EMPIRE.getPermission())) {
            if (args.length >= 4) {
                String[] location = args[1].split("/");
                String worldName = location[0];
                String empireName = args[3];
                EmpireManager empireManager = EmpireManager.getEmpireManager();
                Map<String, Empire> empires = empireManager.getEmpires();
                World world = Bukkit.getWorld(worldName);
                Empire empire = empires.get(empireName);
                if (world == null) {
                    sender.sendMessage(LangManager.getMessage(LangMessage.LOCATION_DOES_NOT_EXIST));
                    return;
                } else if (!WorldTerritoryManager.getUsedWorlds().containsKey(world)){
                    sender.sendMessage(AdaptMessage.worldMessage(world, LangManager.getMessage(LangMessage.WORLD_NOT_USED)));
                    return;
                }
                if (empire == null) {
                    sender.sendMessage(AdaptMessage.empireMessage(empireManager.getNoEmpire(), LangManager.getMessage(LangMessage.EMPIRE_DOES_NOT_EXIST)));
                    return;
                }

                WorldTerritoryManager worldTerritoryManager = WorldTerritoryManager.getUsedWorlds().get(world);
                if (location.length >= 2) {
                    String territoryName = location[1];
                    if (territoryName != null) {
                        Map<String, Territory> worldTerritories = worldTerritoryManager.getTerritories();
                        if (worldTerritories.containsKey(territoryName)) {
                            Territory territory = worldTerritories.get(territoryName);

                            if (location.length >= 3) {
                                String pointName = location[2];
                                if (territory.getCapturePoints().containsKey(pointName)) {
                                    CapturePoint point = territory.getCapturePoints().get(pointName);
                                    PointOwnerChange event = new PointOwnerChange(point, empire);
                                    Bukkit.getPluginManager().callEvent(event);

                                    if (!event.isCancelled()) {
                                        sender.sendMessage(AdaptMessage.pointMessage(point, LangManager.getMessage(LangMessage.POINT_SET_EMPIRE)));
                                    }
                                }
                            } else {
                                TerritoryOwnerChange event = new TerritoryOwnerChange(territory, empire);
                                Bukkit.getPluginManager().callEvent(event);
                                if (!event.isCancelled()) {
                                    sender.sendMessage(AdaptMessage.territoryMessage(territory, LangManager.getMessage(LangMessage.TERRITORY_SET_EMPIRE)));
                                    territory.cancelAttack(territory.getEmpire());
                                }
                            }
                        }
                    }
                } else {
                    worldTerritoryManager.getTerritories().forEach((s, territory) -> {
                        TerritoryOwnerChange event = new TerritoryOwnerChange(territory, empire);
                        Bukkit.getPluginManager().callEvent(event);
                        if (!event.isCancelled()) {
                            territory.cancelAttack(territory.getEmpire());
                        }
                    });
                    sender.sendMessage(AdaptMessage.worldMessage(world, LangManager.getMessage(LangMessage.WORLD_SET_EMPIRE)));
                }
            } else {
                tooFewArguments(sender);
            }
        } else {
            NodewarCommands.doesNotHavePermission(sender, Commands.COMMAND_TERRITORY_SET_EMPIRE.getPermission());
        }
    }

    private void territoryPointNeutralizeCommand(final CommandSender sender, final String command, final String[] args) {
        if (sender.hasPermission(Commands.COMMAND_TERRITORY_NEUTRALIZE.getPermission())) {
            if (args.length >= 3) {
                String[] location = args[1].split("/");
                String worldName = location[0];
                World world = Bukkit.getWorld(worldName);

                if (world == null) {
                    sender.sendMessage(LangManager.getMessage(LangMessage.LOCATION_DOES_NOT_EXIST));
                    return;
                } else if (!WorldTerritoryManager.getUsedWorlds().containsKey(world)){
                    sender.sendMessage(AdaptMessage.worldMessage(world, LangManager.getMessage(LangMessage.WORLD_NOT_USED)));
                    return;
                }

                WorldTerritoryManager worldTerritoryManager = WorldTerritoryManager.getUsedWorlds().get(world);
                Map<String, Territory> worldTerritories = worldTerritoryManager.getTerritories();

                if (location.length >= 2) {
                    String territoryName = location[1];
                    if (territoryName != null && WorldTerritoryManager.getUsedWorlds().containsKey(world)) {
                        if (worldTerritories.containsKey(territoryName)) {
                            Territory territory = worldTerritories.get(territoryName);

                            if (location.length >= 3) {
                                String pointName = location[2];
                                if (territory.getCapturePoints().containsKey(pointName)) {
                                    CapturePoint point = territory.getCapturePoints().get(pointName);
                                    PointOwnerChange event = new PointOwnerChange(point, null);
                                    Bukkit.getPluginManager().callEvent(event);

                                    if (!event.isCancelled()) {
                                        sender.sendMessage(AdaptMessage.pointMessage(point, LangManager.getMessage(LangMessage.POINT_NEUTRALIZE)));
                                    }
                                }
                            } else {
                                TerritoryOwnerChange event = new TerritoryOwnerChange(territory, null);
                                Bukkit.getPluginManager().callEvent(event);
                                if (!event.isCancelled()) {
                                    sender.sendMessage(AdaptMessage.territoryMessage(territory, LangManager.getMessage(LangMessage.TERRITORY_NEUTRALIZE)));
                                    territory.cancelAttack(null);
                                }
                            }
                        }
                    }
                } else {
                    worldTerritoryManager.getTerritories().forEach((s, territory) -> {
                        TerritoryOwnerChange event = new TerritoryOwnerChange(territory, null);
                        Bukkit.getPluginManager().callEvent(event);
                        if (!event.isCancelled()) {
                            territory.cancelAttack(null);
                        }
                    });
                    sender.sendMessage(AdaptMessage.worldMessage(world, LangManager.getMessage(LangMessage.WORLD_NEUTRALIZE)));
                }
            } else {
                tooFewArguments(sender);
            }
        } else {
            NodewarCommands.doesNotHavePermission(sender, Commands.COMMAND_TERRITORY_NEUTRALIZE.getPermission());
        }
    }

    private void adminCommands(final CommandSender sender, final String command, final String[] args) {
        if (command.startsWith(Commands.COMMAND_ADMIN_PLAYER.getCommand())) {
            playerGUICommand(sender, args);
        } else if (command.startsWith(Commands.COMMAND_ADMIN_NODEWAR.getCommand())) {
            nodewarGUICommand(sender, args);
        } else if (command.startsWith(Commands.COMMAND_ADMIN_EMPIRE_PLAYER_SET.getCommand())) {
            setEmpireCommand(sender, args);
        } else if (command.startsWith(Commands.COMMAND_ADMIN_EMPIRE_PLAYER_REMOVE.getCommand())) {
            removeEmpireCommand(sender, args);
        } else if (command.startsWith(Commands.COMMAND_ADMIN_EMPIRE_CREATE.getCommand())) {
            adminEmpireCreateCommand(sender, args);
        } else if (command.startsWith(Commands.COMMAND_ADMIN_EMPIRE_DISBAND.getCommand())) {
            adminEmpireDisbandCommand(sender, args);
        }  else if (command.startsWith(Commands.COMMAND_ADMIN_EMPIRE_EDIT.getCommand())) {
            adminEmpireEditCommand(sender, args);
        } else {
            Player player = null;
            if (sender instanceof Player) {
                player = ((Player) sender).getPlayer();
            }
            Player finalPlayer = player;
            LangManager.getListMessage(LangMessage.ADMIN_HELP).forEach(s ->
                    sender.sendMessage(AdaptMessage.playerMessage(finalPlayer, s)));
        }
    }

    private void setEmpireCommand(final CommandSender sender, final String[] args) {
        if (sender.hasPermission(Commands.COMMAND_ADMIN_EMPIRE.getPermission())) {
            if (args.length == 5) {
                final Player target = Bukkit.getServer().getPlayerExact(args[3]);
                final Empire empire = EmpireManager.getEmpireManager().getEmpires().get(args[4]);
                if (target != null) {
                    if (empire != null) {
                        PlayerInfo.gets(target).setEmpire(empire);
                        sender.sendMessage(AdaptMessage.playerMessage(target, LangManager.getMessage(LangMessage.PLAYER_SET_EMPIRE)));
                    }
                    else {
                        AdaptMessage.playerMessage(target, LangManager.getMessage(LangMessage.EMPIRE_DOES_NOT_EXIST));
                    }
                }
                else {
                    NodewarCommands.discPlayer(sender);
                }
            }
            else {
                NodewarCommands.tooFewArguments(sender);
            }
        }
        else {
            NodewarCommands.doesNotHavePermission(sender, Commands.COMMAND_ADMIN_EMPIRE.getPermission());
        }
    }

    private void adminEmpireCreateCommand(final CommandSender sender, final String[] args) {
        if (sender.hasPermission(Commands.COMMAND_ADMIN_EMPIRE_CREATE.getPermission())) {
            EmpireManager empireManager = EmpireManager.getEmpireManager();
            if (args.length > 3) {
                String empireID = args[3];
                if (empireManager.getEmpires().containsKey(empireID)) {
                    sender.sendMessage("Empire already existing");
                } else {
                    Empire empire = empireManager.getSet((Player) null, empireID);
                    sender.sendMessage("Empire " + empireID + " created");
                }
            } else {
                tooFewArguments(sender);
            }
        } else {
            doesNotHavePermission(sender, Commands.COMMAND_ADMIN_EMPIRE_CREATE.getPermission());
        }
    }

    private void adminEmpireDisbandCommand(final CommandSender sender, final String[] args) {
        if (sender.hasPermission(Commands.COMMAND_ADMIN_EMPIRE_DISBAND.getPermission())) {
            EmpireManager empireManager = EmpireManager.getEmpireManager();
            if (args.length > 3) {
                Empire targetEmpire = empireManager.getEmpires().get(args[3]);
                if (targetEmpire != null) {
                    empireManager.getEmpires().remove(targetEmpire.getName());
                    for (Map.Entry<Player, PlayerInfo> entry : PlayerInfo.getPlayerInfoMap().entrySet()) {
                        PlayerInfo playerInfo = entry.getValue();
                        if (playerInfo.getEmpire() == targetEmpire) {
                            playerInfo.leaveEmpire();
                        }
                    }
                    targetEmpire.deleteConfig();
                    sender.sendMessage("Empire disbanded.");
                } else {
                    sender.sendMessage("This empire doesn't exist.");
                }
            } else {
                tooFewArguments(sender);
            }
        } else {
            doesNotHavePermission(sender, Commands.COMMAND_ADMIN_EMPIRE_DISBAND.getPermission());
        }
    }

    private void adminEmpireEditCommand(final CommandSender sender, final String[] args) {
        if (sender.hasPermission(Commands.COMMAND_ADMIN_EMPIRE_EDIT.getPermission())) {
            if (args.length > 5) {
                EmpireManager empireManager = EmpireManager.getEmpireManager();
                Empire targetEmpire;
                if (args[3].equalsIgnoreCase("none")) {
                    targetEmpire = empireManager.getNoEmpire();
                } else {
                    targetEmpire = empireManager.getEmpires().get(args[3]);
                }
                if (args[4].equalsIgnoreCase("display")) {
                    targetEmpire.setDisplay(AdaptMessage.playerMessage(null, args[5]));
                    sender.sendMessage("Edited successfully !");
                    targetEmpire.saveConfigFile();
                } else if (args[4].equalsIgnoreCase("friendlyfire")) {
                    targetEmpire.setFriendlyFire(args[5].equalsIgnoreCase("true"));
                    sender.sendMessage("Edited successfully !");
                    targetEmpire.saveConfigFile();
                } else if (args[4].equalsIgnoreCase("bossbarcolor")){
                    try {
                        targetEmpire.setBarColor(BarColor.valueOf(args[5]));
                        sender.sendMessage("Edited successfully !");
                        targetEmpire.saveConfigFile();
                    } catch (Exception e) {
                        sender.sendMessage("Color doesn't match.");
                    }
                } else if (args[4].equalsIgnoreCase("setowner")){
                    for (Player player1 : Bukkit.getOnlinePlayers()) {
                        if (player1.getName().equalsIgnoreCase(args[5])) {
                            if (PlayerInfo.gets(player1).getEmpire().equals(targetEmpire)) {
                                targetEmpire.setOwnerUUID(player1.getUniqueId().toString());
                                sender.sendMessage("Edited successfully !");
                                targetEmpire.saveConfigFile();
                            } else {
                                sender.sendMessage("The player must be in the empire already.");
                            }
                            break;
                        }
                    }
                }
            } else {
                tooFewArguments(sender);
            }
        } else {
            doesNotHavePermission(sender, Commands.COMMAND_ADMIN_EMPIRE_EDIT.getPermission());
        }
    }

    private void removeEmpireCommand(final CommandSender sender, final String[] args) {
        if (sender.hasPermission(Commands.COMMAND_ADMIN_EMPIRE.getPermission())) {
            if (args.length == 4) {
                final Player target = Bukkit.getServer().getPlayerExact(args[3]);
                if (target != null) {
                    PlayerInfo.gets(target).setEmpire(null);
                    sender.sendMessage(AdaptMessage.playerMessage(target, LangManager.getMessage(LangMessage.PLAYER_REMOVE_EMPIRE)));
                }
                else {
                    NodewarCommands.discPlayer(sender);
                }
            }
            else {
                NodewarCommands.tooFewArguments(sender);
            }
        }
        else {
            NodewarCommands.doesNotHavePermission(sender, Commands.COMMAND_ADMIN_EMPIRE.getPermission());
        }
    }

    private void playerGUICommand(final CommandSender sender, final String[] args) {
        if (NodewarCommands.isSenderPlayer(sender)) {
            final Player playerSender = (Player)sender;
            if (playerSender.hasPermission(Commands.COMMAND_ADMIN_PLAYER.getPermission())) {
                if (args.length == 3) {
                    final Player target = Bukkit.getServer().getPlayerExact(args[2]);
                    if (target != null) {
                        PlayerAdminGUI.initGUI(playerSender, this.plugin, target, null);
                    }
                    else {
                        NodewarCommands.discPlayer(sender);
                    }
                }
                else {
                    NodewarCommands.tooFewArguments(sender);
                }
            }
            else {
                NodewarCommands.doesNotHavePermission(playerSender, Commands.COMMAND_ADMIN_PLAYER.getPermission());
            }
        }
        else {
            NodewarCommands.playerOnly(sender);
        }
    }

    private void nodewarGUICommand(final CommandSender sender, final String[] args) {
        if (NodewarCommands.isSenderPlayer(sender)) {
            final Player player = (Player)sender;
            if (player.hasPermission(Commands.COMMAND_ADMIN_NODEWAR.getPermission())) {
                WorldsGUIs.initGUI(player, this.plugin);
            }
            else {
                NodewarCommands.doesNotHavePermission(player, Commands.COMMAND_ADMIN_NODEWAR.getPermission());
            }
        }
        else {
            NodewarCommands.playerOnly(sender);
        }
    }





    public List<String> onTabComplete(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        final List<String> completions = new ArrayList<>();
        final List<String> commands = new ArrayList<>();
        Map<String, Empire> empires = EmpireManager.getEmpireManager().getEmpires();
        final String string = String.join(" ", args);
        if (args.length <= 1) {
            commands.add("admin");
            commands.add("empire");
            commands.add("help");
            commands.add("territory");
            StringUtil.copyPartialMatches(args[0], commands, (Collection<String>)completions);
        }
        else if (args.length <= 2) {
            if (string.startsWith(Commands.COMMAND_EMPIRE.getCommand())) {
                commands.add("create");
                commands.add("disband");
                commands.add("join");
                commands.add("edit");
                commands.add("leave");
                commands.add("list");
            } else if (string.startsWith(Commands.COMMAND_ADMIN.getCommand())) {
                commands.add("empire");
                commands.add("territory");
                commands.add("player");
            } else if (string.startsWith(Commands.COMMAND_TERRITORY.getCommand())) {
                WorldTerritoryManager.getUsedWorlds().forEach((world, worldTerritoryManager) -> {
                    commands.add(world.getName());
                    worldTerritoryManager.getTerritories().forEach((s, territory) -> {
                        String territoryString = territory.getWorld().getName() + "/" + s;
                        commands.add(territoryString);
                        territory.getCapturePoints().forEach((s1, capturePoint) -> {
                            commands.add(territoryString + "/" + s1);
                        });
                    });
                });
            }
            StringUtil.copyPartialMatches(args[1], commands, completions);
        }
        else if (args.length <= 3) {
            if (string.startsWith(Commands.COMMAND_EMPIRE_JOIN.getCommand())) {
                final ArrayList<String> empiresName = new ArrayList<>();
                empires.forEach((s, empire) -> empiresName.add(s));
                empiresName.sort(Comparator.comparing(String::toString));
                commands.addAll(empiresName);
            } else if (string.startsWith(Commands.COMMAND_EMPIRE_EDIT.getCommand())) {
                commands.add("display");
                commands.add("bossbarcolor");
                commands.add("friendlyfire");
                commands.add("setowner");
            } else if (string.startsWith(Commands.COMMAND_ADMIN_EMPIRE.getCommand())) {
                commands.add("create");
                commands.add("edit");
                commands.add("disband");
                commands.add("set");
                commands.add("remove");
            } else if (string.startsWith(Commands.COMMAND_ADMIN_PLAYER.getCommand())) {
                final ArrayList<String> playersName = new ArrayList<>();
                Bukkit.getOnlinePlayers().forEach(player -> playersName.add(player.getName()));
                playersName.sort(Comparator.comparing(String::toString));
                commands.addAll(playersName);
            } else if (string.startsWith(Commands.COMMAND_TERRITORY.getCommand())) {
                commands.add("setempire");
                commands.add("neutralize");
                commands.add("vulnerability");
            }
            StringUtil.copyPartialMatches(args[2], commands, completions);
        }
        else if (args.length <= 4) {
            if (string.startsWith(Commands.COMMAND_EMPIRE_EDIT.getCommand() + " bossbarcolor")){
                commands.add("BLUE");
                commands.add("GREEN");
                commands.add("PINK");
                commands.add("PURPLE");
                commands.add("RED");
                commands.add("WHITE");
            } else if (string.startsWith(Commands.COMMAND_ADMIN_EMPIRE.getCommand())) {
                for (Map.Entry<String, Empire> entry : EmpireManager.getEmpireManager().getEmpires().entrySet()) {
                    String s = entry.getKey();
                    commands.add(s);
                }
            } if (string.startsWith(Commands.COMMAND_ADMIN_EMPIRE_PLAYER_SET.getCommand()) || string.startsWith(Commands.COMMAND_ADMIN_EMPIRE_PLAYER_REMOVE.getCommand())) {
                final ArrayList<String> playersName = new ArrayList<>();
                Bukkit.getOnlinePlayers().forEach(player -> playersName.add(player.getName()));
                playersName.sort(Comparator.comparing(String::toString));
                commands.addAll(playersName);
            } else if (string.startsWith(Commands.COMMAND_TERRITORY.getCommand())) {
                if (args[2].equalsIgnoreCase("setempire")) {
                    empires.forEach((s, empire) -> commands.add(s));
                } else if (args[2].equalsIgnoreCase("vulnerability")) {
                    commands.add("true");
                    commands.add("false");
                }
            }
            StringUtil.copyPartialMatches(args[3], commands, (Collection<String>)completions);
        }
        else if (args.length <= 5) {
            if (string.startsWith(Commands.COMMAND_ADMIN_EMPIRE_PLAYER_SET.getCommand())) {
                final ArrayList<String> empiresName = new ArrayList<>();
                empires.forEach((s, empire) -> empiresName.add(s));
                empiresName.sort(Comparator.comparing(String::toString));
                commands.addAll(empiresName);
            } else if (string.startsWith(Commands.COMMAND_ADMIN_EMPIRE.getCommand())) {
                commands.add("display");
                commands.add("bossbarcolor");
                commands.add("friendlyfire");
                commands.add("setowner");
            }
            StringUtil.copyPartialMatches(args[4], commands, completions);
        } else if (args.length <= 6) {
            if (string.startsWith(Commands.COMMAND_ADMIN_EMPIRE_EDIT.getCommand())){
                commands.add("BLUE");
                commands.add("GREEN");
                commands.add("PINK");
                commands.add("PURPLE");
                commands.add("RED");
                commands.add("WHITE");
            }
            StringUtil.copyPartialMatches(args[5], commands, completions);
        }
        Collections.sort(completions);
        return completions;
    }
    
    public static boolean isSenderPlayer(final CommandSender sender) {
        return sender instanceof Player;
    }
    
    public static void discPlayer(final CommandSender sender) {
        Player player = null;
        if (sender instanceof Player) {
            player = ((Player) sender).getPlayer();
        }
        sender.sendMessage(AdaptMessage.playerMessage((player), LangManager.getMessage(LangMessage.DISCONNECTED_PLAYER)));
    }

    public static void playerOnly(final CommandSender sender) {
        Player player = null;
        if (sender instanceof Player) {
            player = ((Player) sender).getPlayer();
        }
        sender.sendMessage(AdaptMessage.playerMessage(player, LangManager.getMessage(LangMessage.BY_PLAYER_ONLY)));
    }
    
    public static void tooFewArguments(final CommandSender sender) {
        Player player = null;
        if (sender instanceof Player) {
            player = ((Player) sender).getPlayer();
        }
        sender.sendMessage(AdaptMessage.playerMessage(player, LangManager.getMessage(LangMessage.TOO_FEW_ARGUMENTS)));
    }
    
    public static void doesNotHavePermission(final CommandSender sender, final String permission) {
        Player player = null;
        if (sender instanceof Player) {
            player = ((Player) sender).getPlayer();
        }
        sender.sendMessage(AdaptMessage.playerMessage(player,
                LangManager.getMessage(LangMessage.PERMISSION_DENIED).replaceAll("%permission%", permission)));
    }
}
