package fr.rosstail.nodewar.commands.subcommands.admincommands.adminterritorycommands;

import fr.rosstail.nodewar.commands.SubCommand;
import fr.rosstail.nodewar.empires.Empire;
import fr.rosstail.nodewar.empires.EmpireManager;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
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

public class AdminTerritorySetOwnerCommand extends SubCommand {
    @Override
    public String getName() {
        return "setowner";
    }

    @Override
    public String getDescription() {
        return "Set the owner of a territory";
    }

    @Override
    public String getSyntax() {
        return "nodewar admin territory setowner [territory] [empire]";
    }

    @Override
    public String getPermission() {
        return "nodewar.command.admin.territory.setowner";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(AdaptMessage.adapt(LangManager.getMessage(LangMessage.PERMISSION_DENIED)));
            return;
        }

        if (args.length < 5) {
            sender.sendMessage(AdaptMessage.adapt(LangManager.getMessage(LangMessage.TOO_FEW_ARGUMENTS)));
            return;
        }

        String[] location = args[3].split("/");
        if (location.length < 2) {
            sender.sendMessage("The location is not accurate enough !");
            return;
        }
        String worldName = location[0];
        String empireName = args[4];
        EmpireManager empireManager = EmpireManager.getEmpireManager();
        Map<String, Empire> empires = empireManager.getEmpires();
        World world = Bukkit.getWorld(worldName);
        Empire empire = empires.get(empireName);
        if (world == null) {
            sender.sendMessage("The world " + worldName + " does not exist");
            return;
        } else if (!WorldTerritoryManager.getUsedWorlds().containsKey(world)) {
            sender.sendMessage("The world " + worldName + " is not used by Nodewar");
            return;
        }

        if (empire == null) {
            sender.sendMessage("The empire " + empireName + " does not exist");
            return;
        }

        WorldTerritoryManager worldTerritoryManager = WorldTerritoryManager.getUsedWorlds().get(world);
        String territoryName = location[1];
        if (territoryName != null) {
            if (territoryName.equalsIgnoreCase("*")) { //affect every territory
                worldTerritoryManager.getTerritories().forEach((s, territory) -> {
                    Objective objective = territory.getObjective();
                    if (objective != null) {
                        objective.win(empire);
                    } else {
                        territory.setEmpire(empire);
                    }
                });
                sender.sendMessage("The empire " + empire.getDisplay() + " now rules the whole world " + worldName);
            }
            Map<String, Territory> worldTerritories = worldTerritoryManager.getTerritories();
            if (worldTerritories.containsKey(territoryName)) {
                Territory territory = worldTerritories.get(territoryName);
                Objective objective = territory.getObjective();
                if (objective != null) {
                    objective.win(empire);
                } else {
                    territory.setEmpire(empire);
                }
                sender.sendMessage("The territory " + empire.getDisplay() + " new rules " + territory.getDisplay());
            }
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        if (args.length <= 4) {
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
        } else if (args.length <= 5){
            return new ArrayList<>(EmpireManager.getEmpireManager().getEmpires().keySet());
        }
        return null;
    }
}
