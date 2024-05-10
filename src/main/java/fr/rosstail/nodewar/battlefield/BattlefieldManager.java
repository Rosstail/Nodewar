package fr.rosstail.nodewar.battlefield;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.territory.dynmap.DynmapHandler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.dynmap.DynmapAPI;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BattlefieldManager {

    private static BattlefieldManager battlefieldManager;
    private Nodewar plugin;

    private List<Long> alertTimeList = new ArrayList<>();

    private List<Battlefield> battlefieldList = new ArrayList<>();
    private int scheduler;

    public BattlefieldManager(Nodewar plugin) {
        this.plugin = plugin;
    }

    public static void init(Nodewar plugin) {
        if (battlefieldManager == null) {
            battlefieldManager = new BattlefieldManager(plugin);
        }
    }

    public void loadBattlefieldList() {
        long now = System.currentTimeMillis();

        ConfigData.getConfigData().battlefield.alertTimers.forEach(s -> {
            alertTimeList.add(AdaptMessage.evalDuration(s));
        });
        Collections.sort(alertTimeList);

        ConfigurationSection battlefieldListSection = ConfigData.getConfigData().battlefield.configFile.getConfigurationSection("battlefield.list");

        battlefieldListSection.getKeys(false).forEach(s -> {
            System.out.println(s);
            BattlefieldModel dbBattlefieldModel = StorageManager.getManager().selectBattlefieldModel(s);
            BattlefieldModel battlefieldModel = new BattlefieldModel(battlefieldListSection.getConfigurationSection(s));

            Battlefield battlefield = new Battlefield(battlefieldModel);
            battlefieldList.add(battlefield);

            if (dbBattlefieldModel == null) {
                StorageManager.getManager().insertBattlefieldModel(battlefieldModel);
            } else {
                battlefieldModel.setId(dbBattlefieldModel.getId());
                battlefieldModel.setOpen(dbBattlefieldModel.isOpen());

                long dbOpenTime = dbBattlefieldModel.getOpenDateTime();
                long dbCloseTime = dbBattlefieldModel.getCloseDateTime();

                if (now >= dbOpenTime && now <= dbCloseTime) {
                    battlefieldModel.setOpenDateTime(dbOpenTime);
                    battlefieldModel.setCloseDateTime(dbCloseTime);
                    openBattlefield(battlefield);
                } else {
                    closeBattlefield(battlefield);
                }
            }
        });
    }

    public void openBattlefield(Battlefield battlefield) {
        AdaptMessage.print(battlefield.getModel().getDisplay() + " battlefield is now open !", AdaptMessage.prints.OUT);
        battlefield.getModel().setOpen(true);
        battlefield.getTerritoryList().forEach(territory -> {
            territory.getModel().setUnderProtection(false);
        });

        StorageManager.getManager().updateBattlefieldModel(battlefield.getModel(), true);
    }

    public void closeBattlefield(Battlefield battlefield) {
        AdaptMessage.print(battlefield.getModel().getDisplay() + " battlefield is now closed !", AdaptMessage.prints.OUT);
        battlefield.getModel().setOpen(false);
        battlefield.getTerritoryList().forEach(territory -> {
            territory.getModel().setUnderProtection(true);
        });
        String[] startTimeStr = battlefield.getModel().getFromTimeStr().split(":");
        String[] endTimeStr = battlefield.getModel().getToTimeStr().split(":");

        battlefield.getModel().setOpenDateTime(getNextDayTime(DayOfWeek.valueOf(battlefield.getModel().getFromDayStr().toUpperCase()), Integer.parseInt(startTimeStr[0]), Integer.parseInt(startTimeStr[1])));
        battlefield.getModel().setCloseDateTime(getNextDayTime(DayOfWeek.valueOf(battlefield.getModel().getToDayStr().toUpperCase()), Integer.parseInt(endTimeStr[0]), Integer.parseInt(endTimeStr[1])));
        StorageManager.getManager().updateBattlefieldModel(battlefield.getModel(), true);
    }

    public long getNextDayTime(DayOfWeek day, int hour, int minute) {

        LocalDateTime now = LocalDateTime.now();
        DayOfWeek todayDay = now.getDayOfWeek();
        int addDay = (day.getValue() - todayDay.getValue() + 7) % 7;
        if (addDay == 0 && (hour < now.getHour() || (hour == now.getHour() && minute <= now.getMinute()))) {
            addDay = 7;
        }
        LocalDateTime nextDay = now.plusDays(addDay);
        nextDay = nextDay.withHour(hour).withMinute(minute).withSecond(0).withNano(0);
        return nextDay.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000L;
    }

    private void test() {
        long now = System.currentTimeMillis();

        battlefieldList.forEach(battlefield -> {

        });

        battlefieldList.stream().filter(battlefield -> battlefield.getLastWarningIndex() < 0 || battlefield.getLastWarningIndex() >= alertTimeList.size()).forEach(battlefield -> {
            battlefield.setLastWarningIndex(0);
        });


        List<Battlefield> battlefieldListToClose = battlefieldList.stream().filter(battlefield -> (battlefield.getModel().isOpen())
                        && battlefield.getModel().getCloseDateTime() < now)
                .collect(Collectors.toList());

        List<Battlefield> battlefieldListToOpen = battlefieldList.stream().filter(battlefield -> (!battlefield.getModel().isOpen())
                        && battlefield.getModel().getOpenDateTime() <= now && battlefield.getModel().getCloseDateTime() >= now)
                .collect(Collectors.toList());

        if (!battlefieldListToClose.isEmpty() || !battlefieldListToOpen.isEmpty()) {
            battlefieldListToClose.forEach(this::closeBattlefield);
            battlefieldListToOpen.forEach(this::openBattlefield);

            DynmapHandler.getDynmapHandler().resumeRender();
        }
    }

    public void startBattlefieldDispatcher() {
        Runnable handleRequestExpiration = this::test;
        scheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(Nodewar.getInstance(), handleRequestExpiration, 20L, 20L);
    }

    public static BattlefieldManager getBattlefieldManager() {
        return battlefieldManager;
    }
}
