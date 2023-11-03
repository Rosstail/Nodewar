package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.TeamSubCommand;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.TeamMemberModel;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TeamCheckCommand extends TeamSubCommand {
    @Override
    public String getName() {
        return "check";
    }

    @Override
    public String getDescription() {
        return "Desc check nodewar team";
    }

    @Override
    public String getSyntax() {
        return "nodewar team check";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team.join";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        Player senderPlayer;
        String teamName;
        NwTeam nwTeam;
        PlayerData playerData;
        TeamMemberModel teamMemberModel;
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Must be sent by player");
            return;
        }
        senderPlayer = ((Player) sender).getPlayer();
        playerData = PlayerDataManager.getPlayerDataMap().get(senderPlayer.getName());

        if (playerData.getTeam() == null) {
            sender.sendMessage("You are not on a team");
            return;
        }

        NwTeam playerTeam = playerData.getTeam();
        sender.sendMessage(playerTeam.getModel().getName() + " / " + playerTeam.getModel().getDisplay());
        sender.sendMessage(" > Rank: " + playerTeam.getMemberMap().get(senderPlayer).getRank().toString());
        sender.sendMessage(" > Members: " + playerTeam.getMemberMap().size() + " / " + playerTeam.getModel().getTeamMemberModelMap().size());
        sender.sendMessage(" > Connected:");
        playerTeam.getMemberMap().forEach((player, teamMember) -> {
            sender.sendMessage("    - " + player.getName());
        });
        sender.sendMessage(" > Relations:");
        playerTeam.getRelationMap().forEach((s, teamRelation) -> {
            sender.sendMessage("    - " + s + " " + teamRelation.getRelationType().toString());
        });
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
