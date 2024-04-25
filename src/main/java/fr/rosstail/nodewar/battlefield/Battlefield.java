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
    private long lastUpdate = System.currentTimeMillis();
    private long startDate;
    private long endDate;

    private final List<Territory> territoryList;

    public Battlefield(BattlefieldModel model) {
        this.model = model;
        DayOfWeek startDay = DayOfWeek.valueOf(model.getFromDayStr().toUpperCase());
        DayOfWeek endDay = DayOfWeek.valueOf(model.getToDayStr().toUpperCase());
        String[] startTimeStr = model.getFromTimeStr().split(":");
        String[] endTimeStr = model.getToTimeStr().split(":");

        this.startDate = getNextDayTime(startDay, Integer.parseInt(startTimeStr[0]), Integer.parseInt(startTimeStr[1]));
        this.endDate = getNextDayTime(endDay, Integer.parseInt(endTimeStr[0]), Integer.parseInt(endTimeStr[1]));

        territoryList = TerritoryManager.getTerritoryManager().getTerritoryMap().values().stream().filter(territory -> (
                model.getTerritoryList().contains(territory.getModel().getName())
                        || model.getTerritoryTypeList().contains(territory.getTerritoryType().getName())
        )).collect(Collectors.toList());
    }


    public static long getNextDayTime(DayOfWeek jour, int hour, int minute) {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek todayDay = now.getDayOfWeek();
        int addDay = (jour.getValue() - todayDay.getValue() + 7) % 7;
        if (addDay == 0 && (hour < now.getHour() || (hour == now.getHour() && minute <= now.getMinute()))) {
            addDay = 7;
        }
        LocalDateTime nextDay = now.plusDays(addDay);
        nextDay = nextDay.withHour(hour).withMinute(minute).withSecond(0).withNano(0);
        return nextDay.atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public BattlefieldModel getModel() {
        return model;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public List<Territory> getTerritoryList() {
        return territoryList;
    }
}
