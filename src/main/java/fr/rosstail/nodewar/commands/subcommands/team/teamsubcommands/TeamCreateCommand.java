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
        Player senderPlayer = null;
        NwTeam playerNwTeam;
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        if (args.length >= 3) {
            teamName = ChatColor.stripColor(args[2].toLowerCase());
            displayName = args[2];
            int ownerId = 0;

            if (TeamDataManager.getTeamDataManager().getStringTeamMap().get(teamName) != null) {
                sender.sendMessage("TeamCreateCommand - This team already exist in storage");
                return;
            }
            TeamModel teamModel = new TeamModel(teamName, displayName);
            if (sender instanceof Player) {
                senderPlayer = (Player) sender;
                PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(senderPlayer.getName());
                ownerId = playerData.getId();

                playerNwTeam = TeamDataManager.getTeamDataManager().getTeamOfPlayer(senderPlayer);
                if (playerNwTeam != null) {
                    sender.sendMessage("You are already on a team");
                    return;
                }
            } else {
                teamModel.setPermanent(true);
            }

            boolean insertTeam = StorageManager.getManager().insertTeamModel(teamModel);
            if (insertTeam) {
                playerNwTeam = new NwTeam(teamModel);
                TeamDataManager.getTeamDataManager().addNewTeam(playerNwTeam);

                sender.sendMessage("Team created successfully " + teamModel.getId());

                if (senderPlayer != null) {
                    PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(senderPlayer.getName());
                    playerData.setTeam(playerNwTeam);

                    TeamMemberModel teamMemberModel =
                            new TeamMemberModel(teamModel.getId(), ownerId, 0, new Timestamp(System.currentTimeMillis()));
                    TeamMember teamMember = new TeamMember(senderPlayer, playerNwTeam, teamMemberModel);

                    boolean insertPlayerTeam = StorageManager.getManager().insertTeamMemberModel(teamMemberModel);
                    if (insertPlayerTeam) {
                        playerNwTeam.getModel().getTeamMemberModelMap().put(teamMemberModel.getId(), teamMemberModel);
                        playerNwTeam.getMemberMap().put(senderPlayer, teamMember);
                    } else {
                        sender.sendMessage("player added unsuccessfully");
                    }
                }
            } else {
                sender.sendMessage("Team added unsuccessfully");
            }
            DynmapHandler.getDynmapHandler().resumeRender();
        } else {
            sender.sendMessage("TeamCreateCommand - Not enough args");
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
