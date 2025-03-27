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

        BattlefieldManager.getManager().editBattlefieldAnouncementTimer(this, Math.min(model.getOpenDateTime(), model.getCloseDateTime()));

        territoryList = TerritoryManager.getTerritoryManager().getTerritoryMap().values().stream().filter(territory -> (
                model.getTerritoryList().contains(territory.getName())
                        || model.getTerritoryTypeList().contains(territory.getPresetName())
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
        message = message.replaceAll("\\[battlefield_id]", String.valueOf(model.getId()))
                .replaceAll("\\[battlefield_name]", model.getName())
                .replaceAll("\\[battlefield_display]", model.getDisplay())
                .replaceAll("\\[battlefield_start_time]", AdaptMessage.getAdaptMessage().dateTimeFormatter(model.getOpenDateTime()))
                .replaceAll("\\[battlefield_start_time_left]", AdaptMessage.getAdaptMessage().countdownFormatter(model.getOpenDateTime()))
                .replaceAll("\\[battlefield_close_time]", AdaptMessage.getAdaptMessage().dateTimeFormatter(model.getCloseDateTime()))
                .replaceAll("\\[battlefield_close_time_left]", AdaptMessage.getAdaptMessage().countdownFormatter(model.getCloseDateTime()));

        boolean isOpen = model.isOpen();
        if (isOpen) {
            message = message.replaceAll("\\[battlefield_status]", LangManager.getMessage(LangMessage.BATTLEFIELD_OPEN))
                    .replaceAll("\\[battlefield_delay]", AdaptMessage.getAdaptMessage().countdownFormatter(model.getCloseDateTime() - System.currentTimeMillis()));
        } else {
            message = message.replaceAll("\\[battlefield_status]", LangManager.getMessage(LangMessage.BATTLEFIELD_CLOSED))
                    .replaceAll("\\[battlefield_delay]", AdaptMessage.getAdaptMessage().countdownFormatter(model.getOpenDateTime() - System.currentTimeMillis()));
        }

        if (message.contains("[battlefield_territory_list_line]")) {
            StringBuilder territoryListStringBuilder = new StringBuilder();

            territoryList.forEach(territory -> {
                if (territoryList.indexOf(territory) > 0) {
                    territoryListStringBuilder.append("\n");
                }
                territoryListStringBuilder.append(territory.adaptMessage(
                        LangManager.getMessage(LangMessage.COMMANDS_BATTLEFIELD_CHECK_RESULT_TERRITORY_LIST_LINE)));
            });

            message = message.replaceAll("\\[battlefield_territory_list_line]", territoryListStringBuilder.toString());
        }

        return message;
    }
}
