package fr.rosstail.nodewar.commandhandlers;

import fr.rosstail.nodewar.commandhandlers.enums.Commands;
import fr.rosstail.nodewar.datahandlers.PlayerInfo;
import fr.rosstail.nodewar.empires.Empire;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.guis.adminguis.nodewarguis.WorldsGUIs;
import fr.rosstail.nodewar.guis.adminguis.playerGUIs.PlayerAdminGUI;
import fr.rosstail.nodewar.guis.playerguis.EmpiresListGUI;
import fr.rosstail.nodewar.required.lang.AdaptMessage;
import fr.rosstail.nodewar.required.lang.LangManager;
import fr.rosstail.nodewar.required.lang.LangMessage;
import fr.rosstail.nodewar.territory.eventhandlers.customevents.PointOwnerChange;
import fr.rosstail.nodewar.territory.eventhandlers.customevents.TerritoryOwnerChange;
import fr.rosstail.nodewar.territory.eventhandlers.customevents.TerritoryVulnerabilityToggle;
import fr.rosstail.nodewar.territory.zonehandlers.CapturePoint;
import fr.rosstail.nodewar.territory.zonehandlers.WorldTerritoryManager;
import fr.rosstail.nodewar.territory.zonehandlers.Territory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
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
                this.listCommand(sender);
            }
            else if (command.startsWith(Commands.COMMAND_EMPIRE_JOIN.getCommand())) {
                this.joinCommand(sender, args);
            }
            else if (command.startsWith(Commands.COMMAND_EMPIRE_LEAVE.getCommand())) {
                this.leaveCommand(sender);
            }
            else {
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
            NodewarCommands.doesNotHavePermission(sender, Commands.COMMAND_EMPIRE.getPermission());
        }
    }

    private void listCommand(final CommandSender sender) {
        if (NodewarCommands.isSenderPlayer(sender)) {
            final Player player = (Player)sender;
            if (player.hasPermission(Commands.COMMAND_EMPIRE_LIST.getPermission())) {
                EmpiresListGUI.initGUI(player, this.plugin);
            }
            else {
                NodewarCommands.doesNotHavePermission(player, Commands.COMMAND_EMPIRE_LIST.getPermission());
            }
        }
        else {
            NodewarCommands.playerOnly(sender);
        }
    }

    private void joinCommand(final CommandSender sender, final String[] args) {
        if (args.length > 2) {
            if (Empire.getEmpires().containsKey(args[2])) {
                if (NodewarCommands.isSenderPlayer(sender)) {
                    final Player player = (Player)sender;
                    if (player.hasPermission(Commands.COMMAND_EMPIRE_JOIN.getPermission())) {
                        final PlayerInfo playerInfo = PlayerInfo.gets(player);
                        final Empire playerEmpire = playerInfo.getEmpire();
                        final Empire empire = Empire.getEmpires().get(args[2]);
                        if (!playerInfo.tryJoinEmpire(empire)) {
                            if (empire == null) {
                                player.sendMessage(AdaptMessage.playerMessage(player, LangManager.getMessage(LangMessage.EMPIRE_DOES_NOT_EXIST)));
                            } else if (playerEmpire != null) {
                                player.sendMessage(AdaptMessage.playerMessage(player, LangManager.getMessage(LangMessage.PLAYER_ALREADY_IN_EMPIRE)));
                            }
                        }
                    }
                    else {
                        NodewarCommands.doesNotHavePermission(player, Commands.COMMAND_EMPIRE_JOIN.getPermission());
                    }
                } else {
                    NodewarCommands.playerOnly(sender);
                }
            }
        }
        else {
            tooFewArguments(sender);
        }
    }

    private void leaveCommand(final CommandSender sender) {
        if (NodewarCommands.isSenderPlayer(sender)) {
            PlayerInfo.gets((Player)sender).leaveEmpire();
        }
        else {
            NodewarCommands.playerOnly(sender);
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
                if (location.length > 2) {
                    String worldName = location[0];
                    String territoryName = location[1];

                    World world = Bukkit.getWorld(worldName);
                    if (world != null && territoryName != null && WorldTerritoryManager.getUsedWorlds().containsKey(world)) {
                        Map<String, Territory> worldTerritories = WorldTerritoryManager.getUsedWorlds().get(world).getTerritories();
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
                    tooFewArguments(sender);
                }
                ;
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

                World world = Bukkit.getWorld(worldName);
                Empire empire = Empire.getEmpires().get(empireName);
                if (world == null) {
                    sender.sendMessage(LangManager.getMessage(LangMessage.LOCATION_DOES_NOT_EXIST));
                    return;
                } else if (!WorldTerritoryManager.getUsedWorlds().containsKey(world)){
                    sender.sendMessage(AdaptMessage.worldMessage(world, LangManager.getMessage(LangMessage.WORLD_NOT_USED)));
                    return;
                }
                if (empire == null) {
                    sender.sendMessage(AdaptMessage.empireMessage(Empire.getNoEmpire(), LangManager.getMessage(LangMessage.EMPIRE_DOES_NOT_EXIST)));
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
        if (sender.hasPermission(Commands.COMMAND_TERRITORY_VULNERABILITY.getPermission())) {
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
            this.playerGUICommand(sender, args);
        }
        else if (command.startsWith(Commands.COMMAND_ADMIN_NODEWAR.getCommand())) {
            this.nodewarGUICommand(sender, args);
        }
        else if (command.startsWith(Commands.COMMAND_ADMIN_EMPIRE_PLAYER_SET.getCommand())) {
            this.setEmpireCommand(sender, args);
        }
        else if (command.startsWith(Commands.COMMAND_ADMIN_EMPIRE_PLAYER_REMOVE.getCommand())) {
            this.removeEmpireCommand(sender, args);
        }
        else {
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
                final Empire empire = Empire.getEmpires().get(args[4]);
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
                commands.add("join");
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
                Empire.getEmpires().forEach((s, empire) -> empiresName.add(s));
                empiresName.sort(Comparator.comparing(String::toString));
                commands.addAll(empiresName);
            } else if (string.startsWith(Commands.COMMAND_ADMIN_EMPIRE.getCommand())) {
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
            if (string.startsWith(Commands.COMMAND_ADMIN_EMPIRE_PLAYER_SET.getCommand()) || string.startsWith(Commands.COMMAND_ADMIN_EMPIRE_PLAYER_REMOVE.getCommand())) {
                final ArrayList<String> playersName = new ArrayList<>();
                Bukkit.getOnlinePlayers().forEach(player -> playersName.add(player.getName()));
                playersName.sort(Comparator.comparing(String::toString));
                commands.addAll(playersName);
            } else if (string.startsWith(Commands.COMMAND_TERRITORY.getCommand())) {
                if (args[2].equalsIgnoreCase("setempire")) {
                    Empire.getEmpires().forEach((s, empire) -> commands.add(s));
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
                Empire.getEmpires().forEach((s, empire) -> empiresName.add(s));
                empiresName.sort(Comparator.comparing(String::toString));
                commands.addAll(empiresName);
            }
            StringUtil.copyPartialMatches(args[4], commands, completions);
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
