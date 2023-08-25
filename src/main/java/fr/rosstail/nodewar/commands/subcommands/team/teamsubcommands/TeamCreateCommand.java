package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.TeamSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.*;
import fr.rosstail.nodewar.team.member.TeamMember;
import fr.rosstail.nodewar.team.member.TeamMemberModel;
import fr.rosstail.nodewar.team.type.NwTeam;
import fr.rosstail.nodewar.territory.dynmap.DynmapHandler;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.text.Normalizer;
import java.util.List;

public class TeamCreateCommand extends TeamSubCommand {

    public TeamCreateCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_TEAM_CREATE_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
    }

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getDescription() {
        return "Desc create nodewar team";
    }

    @Override
    public String getSyntax() {
        return "nodewar team create <display>";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team.create";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        String teamName;
        String displayName;
        String shortName;
        Player senderPlayer;
        TeamModel teamModel;
        NwTeam playerNwTeam;
        TeamMember teamMember;
        int ownerId;
        PlayerData playerData;
        TeamManager teamManager = TeamManager.getManager();
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_BY_PLAYER_ONLY));
            return;
        }

        if (args.length < 4) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TOO_FEW_ARGUMENTS));
            return;
        }
        senderPlayer = (Player) sender;
        playerData = PlayerDataManager.getPlayerDataMap().get(senderPlayer.getName());
        teamName = Normalizer.normalize(ChatColor.stripColor(args[2]).toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        displayName = args[2];
        shortName = args[3];
        ownerId = playerData.getId();

        if (teamManager.getPlayerTeam(senderPlayer) != null) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_PLAYER_ALREADY_IN_TEAM));
            return;
        }

        if (teamManager.getStringTeamMap().get(teamName) != null) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_ALREADY_EXIST));
            return;
        }

        if (teamName.length() < ConfigData.getConfigData().team.minimumNameLength || teamName.length() > ConfigData.getConfigData().team.maximumNameLength) {
            String message = LangManager.getMessage(teamName.length() > ConfigData.getConfigData().team.maximumNameLength ? LangMessage.COMMANDS_TEAM_CREATE_TOO_LONG : LangMessage.COMMANDS_TEAM_CREATE_TOO_SHORT);
            sender.sendMessage(message.replaceAll("\\[name]", teamName));
            return;
        }

        if (shortName.length() < ConfigData.getConfigData().team.minimumShortnameLength || shortName.length() > ConfigData.getConfigData().team.maximumShortNameLength) {
            String message = LangManager.getMessage(teamName.length() > ConfigData.getConfigData().team.maximumShortNameLength ? LangMessage.COMMANDS_TEAM_CREATE_TOO_LONG : LangMessage.COMMANDS_TEAM_CREATE_TOO_SHORT);
            sender.sendMessage(message.replaceAll("\\[name]", shortName));
            return;
        }

        if (teamManager.getStringTeamMap().values().stream().anyMatch(nwTeam -> (nwTeam.getShortName().equalsIgnoreCase(shortName)))) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_ALREADY_EXIST));
            return;
        }

        if (Nodewar.getEconomy() != null) {
            Economy economy = Nodewar.getEconomy();
            double price = ConfigData.getConfigData().team.creationCost;
            if (!Nodewar.getEconomy().withdrawPlayer(senderPlayer, price).transactionSuccess()) {
                sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_CREATE_NOT_ENOUGH_MONEY).replaceAll("\\[price]", economy.format(price)));
                return;
            }
        }

        teamModel = new TeamModel(teamName, displayName, shortName, teamManager.generateRandomColor());
        StorageManager.getManager().insertTeamModel(teamModel);

        TeamMemberModel teamMemberModel =
                new TeamMemberModel(teamModel.getId(), ownerId, 5, new Timestamp(System.currentTimeMillis()), senderPlayer.getName());
        StorageManager.getManager().insertTeamMemberModel(teamMemberModel);

        playerNwTeam = new NwTeam(teamModel);
        teamMember = new TeamMember(senderPlayer, playerNwTeam, teamMemberModel);

        sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_CREATE_RESULT));
        teamManager.addNewTeam(playerNwTeam);
        playerNwTeam.getModel().getTeamMemberModelMap().put(teamMemberModel.getId(), teamMemberModel);
        playerNwTeam.getOnlineMemberMap().put(senderPlayer, teamMember);

        DynmapHandler.getDynmapHandler().resumeRender();
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
