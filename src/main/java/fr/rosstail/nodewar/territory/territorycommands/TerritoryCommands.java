package fr.rosstail.nodewar.territory.territorycommands;

import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.territory.Territory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TerritoryCommands {
    private TerritoryCommandsModel territoryCommandsModel;

    private long nextOccurrence;

    public TerritoryCommands(TerritoryCommandsModel model) {
        this.territoryCommandsModel = model;
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
        NwTeam ownerTeam = territory.getOwnerTeam();
        AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();

        for (String command : getTerritoryCommandsModel().getCommandList()) {
            String target = getTerritoryCommandsModel().getTargetName();
            String finalCommand = adaptMessage.adaptTerritoryMessage(command, territory);
            if (target == null || target.equalsIgnoreCase("server")) {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), adaptMessage.adaptTerritoryMessage(finalCommand, territory));
            } else if (ownerTeam != null) {
                if (target.equalsIgnoreCase("player")) {
                    if (getTerritoryCommandsModel().isTargetOffline()) {
                        ownerTeam.getModel().getTeamMemberModelMap().forEach((integer, teamMemberModel) -> {
                            rewardPlayer(teamMemberModel.getUsername(), territory, finalCommand);
                        });
                    } else {
                        ownerTeam.getMemberMap().forEach((player, member) -> {
                            rewardPlayer(player, territory, finalCommand);
                        });
                    }
                } else if (target.equalsIgnoreCase("team")) {
                    rewardTeam(ownerTeam, territory, finalCommand);
                }
            }
        }

        setNextOccurrence(System.currentTimeMillis() + getTerritoryCommandsModel().getDelay());
    }

    private void rewardPlayer(Player player, Territory territory, String command) {
        AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), adaptMessage.adaptMessage(command.replaceAll("\\[player]", player.getName())));
    }
    private void rewardPlayer(String playerName, Territory territory, String command) {
        AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), adaptMessage.adaptMessage(command.replaceAll("\\[player]", playerName)));
    }

    private void rewardTeam(NwTeam team, Territory territory, String command) {
        AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), adaptMessage.adaptTerritoryMessage(adaptMessage.adaptTeamMessage(command, team), territory));
    }

    public long getNextOccurrence() {
        return nextOccurrence;
    }

    public void setNextOccurrence(long nextOccurrence) {
        this.nextOccurrence = nextOccurrence;
    }
}
