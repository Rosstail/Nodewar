package fr.rosstail.nodewar.battlefield;

import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryManager;
import fr.rosstail.nodewar.territory.TerritoryType;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Battlefield {

    private final BattlefieldModel model;

    private int lastWarningIndex = -1;

    private final List<Territory> territoryList;

    public Battlefield(BattlefieldModel model) {
        this.model = model;
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
}
