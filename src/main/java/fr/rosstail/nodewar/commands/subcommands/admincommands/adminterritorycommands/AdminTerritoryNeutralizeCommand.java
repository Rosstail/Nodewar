package fr.rosstail.nodewar.commands.subcommands.admincommands.adminterritorycommands;

import fr.rosstail.nodewar.commands.SubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.territory.eventhandlers.customevents.TerritoryOwnerChangeEvent;
import fr.rosstail.nodewar.territory.zonehandlers.Territory;
import fr.rosstail.nodewar.territory.zonehandlers.WorldTerritoryManager;
import fr.rosstail.nodewar.territory.zonehandlers.objective.Objective;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminTerritoryNeutralizeCommand extends SubCommand {
    @Override
    public String getName() {
        return "neutralize";
    }

    @Override
    public String getDescription() {
        return "Removes the owner of a territory";
    }

    @Override
    public String getSyntax() {
        return "nodewar admin territory neutralize [territory]";
    }

    @Override
    public String getPermission() {
        return "nodewar.command.admin.territory.neutralize";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(AdaptMessage.adapt(LangManager.getMessage(LangMessage.PERMISSION_DENIED)));
            return;
        }
        if (args.length < 4) {
            sender.sendMessage(AdaptMessage.adapt(LangManager.getMessage(LangMessage.TOO_FEW_ARGUMENTS)));
            return;
        }

        String[] location = args[3].split("/");
        if (location.length < 2) {
            sender.sendMessage("The location is not accurate enough !");
            return;
        }
        String worldName = location[0];
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            sender.sendMessage("The world " + worldName + " does not exist");
            return;
        } else if (!WorldTerritoryManager.getUsedWorlds().containsKey(world)) {
            sender.sendMessage("The world " + worldName + " is not used by Nodewar");
            return;
        }

        WorldTerritoryManager worldTerritoryManager = WorldTerritoryManager.getUsedWorlds().get(world);
        String territoryName = location[1];
        if (territoryName != null) {
            if (territoryName.equalsIgnoreCase("*")) { //affect every territory
                worldTerritoryManager.getTerritories().forEach((s, territory) -> {
                    TerritoryOwnerChangeEvent event = new TerritoryOwnerChangeEvent(territory, null);
                    Bukkit.getPluginManager().callEvent(event);
                    Objective objective = territory.getObjective();
                    if (objective != null) {
                        objective.reset();
                    }
                });
                sender.sendMessage("All territory in " + worldName + " are now neutralized");
            }
            Map<String, Territory> worldTerritories = worldTerritoryManager.getTerritories();
            if (worldTerritories.containsKey(territoryName)) {
                Territory territory = worldTerritories.get(territoryName);
                TerritoryOwnerChangeEvent event = new TerritoryOwnerChangeEvent(territory, null);
                Bukkit.getPluginManager().callEvent(event);
                Objective objective = territory.getObjective();
                if (objective != null) {
                    objective.reset();
                }
                sender.sendMessage("The territory " + territory.getDisplay() + " is now neutralized");
            }
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        if (args.length <= 4){
            ArrayList<String> territories = new ArrayList<>();
            WorldTerritoryManager.getUsedWorlds().forEach((world, worldTerritoryManager) -> {
                String[] locationArray = args[3].split("/");
                if (locationArray.length == 0 || world.getName().contains(locationArray[0])) {
                    worldTerritoryManager.getTerritories().forEach((s, territory) -> {
                        if (locationArray.length <= 1 || territory.getName().contains(locationArray[1])) {
                            String territoryString = territory.getWorld().getName() + "/" + s;
                            territories.add(territoryString);
                        }
                    });
                }
            });
            return territories;
        }
        return null;
    }
}
