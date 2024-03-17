package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands;

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
import fr.rosstail.nodewar.territory.dynmap.DynmapHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
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
        Player senderPlayer;
        TeamModel teamModel;
        NwTeam playerNwTeam;
        TeamMember teamMember;
        int ownerId;
        PlayerData playerData;
        TeamDataManager teamDataManager = TeamDataManager.getTeamDataManager();
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_BY_PLAYER_ONLY));
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TOO_FEW_ARGUMENTS));
            return;
        }
        senderPlayer = (Player) sender;
        playerData = PlayerDataManager.getPlayerDataMap().get(senderPlayer.getName());
        teamName = ChatColor.stripColor(args[2].toLowerCase());
        displayName = args[2];
        ownerId = playerData.getId();

        if (teamDataManager.getTeamOfPlayer(senderPlayer) != null) {
            sender.sendMessage("You are already on a team");
            return;
        }

        if (teamDataManager.getStringTeamMap().get(teamName) != null) {
            sender.sendMessage("TeamCreateCommand - This team already exists");
            return;
        }

        teamModel = new TeamModel(teamName, displayName, teamDataManager.generateRandomColor());
        StorageManager.getManager().insertTeamModel(teamModel);

        TeamMemberModel teamMemberModel =
                new TeamMemberModel(teamModel.getId(), ownerId, 5, new Timestamp(System.currentTimeMillis()), senderPlayer.getName());
        StorageManager.getManager().insertTeamMemberModel(teamMemberModel);

        playerNwTeam = new NwTeam(teamModel);
        teamMember = new TeamMember(senderPlayer, playerNwTeam, teamMemberModel);

        teamDataManager.addNewTeam(playerNwTeam);
        playerData.setTeam(playerNwTeam);
        playerNwTeam.getModel().getTeamMemberModelMap().put(teamMemberModel.getId(), teamMemberModel);
        playerNwTeam.getMemberMap().put(senderPlayer, teamMember);

        DynmapHandler.getDynmapHandler().resumeRender();
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
