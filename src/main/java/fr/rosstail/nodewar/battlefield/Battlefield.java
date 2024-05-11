package fr.rosstail.nodewar.battlefield;

import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryManager;

import java.util.List;
import java.util.stream.Collectors;

public class Battlefield {

    private final BattlefieldModel model;

    private int lastWarningIndex = -1;

    private final List<Territory> territoryList;

    public Battlefield(BattlefieldModel model) {
        this.model = model;

        BattlefieldManager.getBattlefieldManager().editBattlefieldAnouncementTimer(this, Math.min(model.getOpenDateTime(), model.getCloseDateTime()));

        territoryList = TerritoryManager.getTerritoryManager().getTerritoryMap().values().stream().filter(territory -> (
                model.getTerritoryList().contains(territory.getModel().getName())
                        || model.getTerritoryTypeList().contains(territory.getTerritoryType().getName())
        )).collect(Collectors.toList());
    }

    public BattlefieldModel getModel() {
        return model;
    }

    public int getLastWarningIndex() {
        return lastWarningIndex;
    }

    public void setLastWarningIndex(int lastWarningIndex) {
        this.lastWarningIndex = lastWarningIndex;
    }

    public List<Territory> getTerritoryList() {
        return territoryList;
    }

    public String adaptMessage(String message) {
        message = message.replaceAll("\\[battlefield_name]", model.getName());
        message = message.replaceAll("\\[battlefield_display]", model.getDisplay());
        message = message.replaceAll("\\[battlefield_start_time]", AdaptMessage.getAdaptMessage().countdownFormatter(model.getOpenDateTime()));
        message = message.replaceAll("\\[battlefield_close_time]", AdaptMessage.getAdaptMessage().countdownFormatter(model.getCloseDateTime()));

        boolean isOpen = model.isOpen();
        if (isOpen) {
            message = message.replaceAll("\\[battlefield_status]", LangManager.getMessage(LangMessage.BATTLEFIELD_OPEN));
            message = message.replaceAll("\\[battlefield_delay]", AdaptMessage.getAdaptMessage().countdownFormatter(model.getCloseDateTime() - System.currentTimeMillis()));
        } else {
            message = message.replaceAll("\\[battlefield_status]", LangManager.getMessage(LangMessage.BATTLEFIELD_CLOSED));
            message = message.replaceAll("\\[battlefield_delay]", AdaptMessage.getAdaptMessage().countdownFormatter(model.getOpenDateTime() - System.currentTimeMillis()));
        }


        return message;
    }
}
