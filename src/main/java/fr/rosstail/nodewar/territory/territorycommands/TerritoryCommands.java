package fr.rosstail.nodewar.territory.territorycommands;

import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.type.NwTeam;
import fr.rosstail.nodewar.territory.Territory;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TerritoryCommands {
    private TerritoryCommandsModel territoryCommandsModel;

    private long nextOccurrence;

    public TerritoryCommands(TerritoryCommandsModel model) {
        this.territoryCommandsModel = model;
        nextOccurrence = System.currentTimeMillis() + territoryCommandsModel.getInitialDelay();
    }

    public TerritoryCommands(TerritoryCommandsModel childModel, @NotNull TerritoryCommandsModel parentModel) {
        TerritoryCommandsModel clonedParentModel = parentModel.clone();
        if (childModel != null) {
            TerritoryCommandsModel clonedChildModel = childModel.clone();
            this.territoryCommandsModel = new TerritoryCommandsModel(clonedChildModel, clonedParentModel);
        } else {
            this.territoryCommandsModel = clonedParentModel;
        }
        nextOccurrence = System.currentTimeMillis() + territoryCommandsModel.getInitialDelay();
    }

    public TerritoryCommandsModel getTerritoryCommandsModel() {
        return territoryCommandsModel;
    }

    public void setTerritoryCommandsModel(TerritoryCommandsModel territoryCommandsModel) {
        this.territoryCommandsModel = territoryCommandsModel;
    }

    public void handleCommand(Territory territory) {
        NwITeam ownerITeam = territory.getOwnerITeam();
        AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();

        for (String command : getTerritoryCommandsModel().getCommandList()) {
            String target = getTerritoryCommandsModel().getTargetName();
            String finalCommand = adaptMessage.adaptTerritoryMessage(command, territory);
            if (target == null || target.equalsIgnoreCase("server")) {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), PlaceholderAPI.setPlaceholders(null, finalCommand));
            } else if (ownerITeam != null) {
                if (target.equalsIgnoreCase("player")) {
                    if (getTerritoryCommandsModel().isTargetOffline()) {
                        ownerITeam.getMemberMap().forEach((integer, member) -> {
                            if (ownerITeam.getOnlineMemberMap().containsValue(member)) {
                                Player onlineMember = ownerITeam.getOnlineMemberMap().entrySet().stream()
                                        .filter(playerTeamMemberEntry -> playerTeamMemberEntry.getValue() == member)
                                        .findFirst().get().getKey();
                                rewardPlayer(onlineMember, territory, finalCommand);
                            } else {
                                rewardPlayer(member.getModel().getUsername(), territory, finalCommand);
                            }
                        });
                    } else {
                        ownerITeam.getOnlineMemberMap().forEach((player, member) -> {
                            rewardPlayer(player, territory, finalCommand);
                        });
                    }
                } else if (target.equalsIgnoreCase("team")) {
                    rewardTeam(ownerITeam, territory, PlaceholderAPI.setPlaceholders(null, finalCommand));
                }
            }
        }

        setNextOccurrence(System.currentTimeMillis() + getTerritoryCommandsModel().getDelay());
    }

    private void rewardPlayer(Player player, Territory territory, String command) {
        AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();
        command = adaptMessage.adaptMessage(command.replaceAll("\\[player]", player.getName()));
        command = PlaceholderAPI.setPlaceholders(player, command);
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
    }
    private void rewardPlayer(String playerName, Territory territory, String command) {
        AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();
        command = adaptMessage.adaptMessage(command.replaceAll("\\[player]", playerName));
        command = PlaceholderAPI.setPlaceholders(null, command);
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    private void rewardTeam(NwITeam iTeam, Territory territory, String command) {
        AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();
        command = adaptMessage.adaptTerritoryMessage(adaptMessage.adaptTeamMessage(command, iTeam), territory);
        command = PlaceholderAPI.setPlaceholders(null, command);
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    public long getNextOccurrence() {
        return nextOccurrence;
    }

    public void setNextOccurrence(long nextOccurrence) {
        this.nextOccurrence = nextOccurrence;
    }
}
