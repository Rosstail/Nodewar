package fr.rosstail.nodewar.territory.objective;

import fr.rosstail.nodewar.events.territoryevents.TerritoryOwnerChangeEvent;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.territory.Territory;
import org.bukkit.Bukkit;

public class NwConquestObjective extends NwObjective {
    public NwConquestObjective(Territory territory, ObjectiveModel childModel, ObjectiveModel parentModel) {
        super(territory, childModel, parentModel);
    }

    @Override
    public void win(NwITeam winnerITeam) {
        NwITeam currentOwner = territory.getOwnerITeam();
        super.win(winnerITeam);

        TerritoryOwnerChangeEvent event = new TerritoryOwnerChangeEvent(territory, winnerITeam);
        Bukkit.getPluginManager().callEvent(event);

        if (currentOwner != null) {
            if (currentOwner != winnerITeam) {
                AdaptMessage.getAdaptMessage().alertITeam(currentOwner, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_DEFEND_DEFEAT), territory, true);
                AdaptMessage.getAdaptMessage().alertITeam(winnerITeam, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_ATTACK_VICTORY), territory, true);
            } else {
                AdaptMessage.getAdaptMessage().alertITeam(currentOwner, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_DEFEND_VICTORY), territory, true);
                territory.getCurrentBattle().getTeamScoreMap().keySet().stream().filter(nwITeam -> nwITeam != currentOwner).forEach(attackerITeam -> {
                    AdaptMessage.getAdaptMessage().alertITeam(attackerITeam, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_ATTACK_DEFEAT), territory, true);
                });
            }
        } else {
            AdaptMessage.getAdaptMessage().alertITeam(winnerITeam, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_ATTACK_VICTORY), territory, true);
        }
    }

    @Override
    public void neutralize(NwITeam winnerITeam) {
        NwITeam currentOwner = territory.getOwnerITeam();
        super.neutralize(winnerITeam);

        if (currentOwner != null) {
            AdaptMessage.getAdaptMessage().alertITeam(currentOwner, LangManager.getMessage(LangMessage.TERRITORY_BATTLE_ALERT_GLOBAL_DEFEND_DEFEAT), territory, true);
        }
    }
}
