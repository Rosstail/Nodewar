package fr.rosstail.nodewar.battlefield;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.events.territoryevents.TerritoryOwnerNeutralizeEvent;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.webmap.OldDynmapHandler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BattlefieldManager {

    private static BattlefieldManager battlefieldManager;
    private final Nodewar plugin;

    private final List<Long> alertTimeList = new ArrayList<>();

    private final List<Battlefield> battlefieldList = new ArrayList<>();
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
        alertTimeList.add(31536000000000L); // 10 years

        ConfigurationSection battlefieldListSection = ConfigData.getConfigData().battlefield.configFile.getConfigurationSection("battlefield.list");
        if (battlefieldListSection != null) {
            battlefieldListSection.getKeys(false).forEach(s -> {
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
    }

    public void openBattlefield(Battlefield battlefield) {
        if (battlefield.getModel().isResetTeam()) {
            battlefield.getTerritoryList().forEach(territory -> {
                TerritoryOwnerNeutralizeEvent event = new TerritoryOwnerNeutralizeEvent(territory, null);
                Bukkit.getPluginManager().callEvent(event);
            });
        }
        if (battlefield.getModel().isAnnouncement()) {
            String announcement = battlefield.adaptMessage(LangManager.getMessage(LangMessage.BATTLEFIELD_ANNOUNCEMENT_OPEN));
            Bukkit.broadcastMessage(AdaptMessage.getAdaptMessage().adaptMessage(announcement));
        }
        battlefield.getModel().setOpen(true);
        battlefield.getTerritoryList().forEach(territory -> {
            territory.getModel().setUnderProtection(false);
        });

        StorageManager.getManager().updateBattlefieldModel(battlefield.getModel(), true);

        OldDynmapHandler.getDynmapHandler().resumeRender();
    }

    public void closeBattlefield(Battlefield battlefield) {
        if (battlefield.getModel().isAnnouncement()) {
            String announcement = battlefield.adaptMessage(LangManager.getMessage(LangMessage.BATTLEFIELD_ANNOUNCEMENT_CLOSE));
            Bukkit.broadcastMessage(AdaptMessage.getAdaptMessage().adaptMessage(announcement));
        }
        battlefield.getModel().setOpen(false);
        if (battlefield.getModel().isEndBattleOnBattlefieldEnd()) {
            battlefield.getTerritoryList().forEach(territory -> {
                if (territory.getCurrentBattle() != null) {
                    if (territory.getCurrentBattle().isBattleStarted()) {
                        if (territory.getOwnerITeam() != null) {
                            territory.getObjective().win(territory.getOwnerITeam());
                        } else {
                            territory.getObjective().neutralize(territory.getOwnerITeam());
                        }
                    }
                }
                territory.getModel().setUnderProtection(true);
            });
        }
        String[] startTimeStr = battlefield.getModel().getFromTimeStr().split(":");
        String[] endTimeStr = battlefield.getModel().getToTimeStr().split(":");

        battlefield.getModel().setOpenDateTime(getNextDayTime(DayOfWeek.valueOf(battlefield.getModel().getFromDayStr().toUpperCase()), Integer.parseInt(startTimeStr[0]), Integer.parseInt(startTimeStr[1])));
        battlefield.getModel().setCloseDateTime(getNextDayTime(DayOfWeek.valueOf(battlefield.getModel().getToDayStr().toUpperCase()), Integer.parseInt(endTimeStr[0]), Integer.parseInt(endTimeStr[1])));

        StorageManager.getManager().updateBattlefieldModel(battlefield.getModel(), true);

        OldDynmapHandler.getDynmapHandler().resumeRender();
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

    private void handleAlertAndAccess() {
        long now = System.currentTimeMillis();
        if (!alertTimeList.isEmpty()) {

            battlefieldList.stream().filter(battlefield -> battlefield.getModel().isAnnouncement()).forEach(battlefield -> {
                if (battlefield.getModel().getOpenDateTime() > now) {
                    long delay = battlefield.getModel().getOpenDateTime() - now;
                    if (battlefield.getLastWarningIndex() > 0) {
                        long nextAlertDelay = alertTimeList.get(battlefield.getLastWarningIndex() - 1);

                        if (nextAlertDelay > delay) {
                            String announcement = LangManager.getMessage(LangMessage.BATTLEFIELD_ANNOUNCEMENT_OPEN_DELAY);
                            announcement = announcement.replaceAll("\\[delay]", AdaptMessage.getAdaptMessage().countdownFormatter(nextAlertDelay));
                            announcement = battlefield.adaptMessage(announcement);
                            Bukkit.broadcastMessage(AdaptMessage.getAdaptMessage().adaptMessage(announcement));

                            editBattlefieldAnouncementTimer(battlefield, battlefield.getModel().getOpenDateTime());
                        }
                    }
                } else if (battlefield.getModel().getCloseDateTime() > now) {
                    long delay = battlefield.getModel().getCloseDateTime() - now;
                    if (battlefield.getLastWarningIndex() > 0) {
                        long nextAlertDelay = alertTimeList.get(battlefield.getLastWarningIndex() - 1);

                        if (nextAlertDelay > delay) {
                            String announcement = LangManager.getMessage(LangMessage.BATTLEFIELD_ANNOUNCEMENT_CLOSE_DELAY);
                            announcement = announcement.replaceAll("\\[delay]", AdaptMessage.getAdaptMessage().countdownFormatter(nextAlertDelay));
                            announcement = battlefield.adaptMessage(announcement);
                            Bukkit.broadcastMessage(AdaptMessage.getAdaptMessage().adaptMessage(announcement));

                            editBattlefieldAnouncementTimer(battlefield, battlefield.getModel().getCloseDateTime());
                        }
                    }
                }
            });
        }

        battlefieldList.stream().filter(battlefield -> battlefield.getLastWarningIndex() <= 0).forEach(battlefield -> {
            battlefield.setLastWarningIndex(alertTimeList.size() - 1);
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

            OldDynmapHandler.getDynmapHandler().resumeRender();
        }
    }

    public void editBattlefieldAnouncementTimer(Battlefield battlefield, long dateTime) {
        if (alertTimeList.isEmpty()) {
            battlefield.setLastWarningIndex(-1);
            return;
        }

        long delay = dateTime - System.currentTimeMillis();

        List<Long> higherValues = alertTimeList.stream().filter(timer -> (timer > delay)).collect(Collectors.toList());

        if (higherValues.isEmpty()) {
            battlefield.setLastWarningIndex(-1);
            return;
        }

        int index = alertTimeList.indexOf(Collections.min(higherValues));

        battlefield.setLastWarningIndex(index);
    }

    public void startBattlefieldDispatcher() {
        Runnable handleRequestExpiration = this::handleAlertAndAccess;
        scheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(Nodewar.getInstance(), handleRequestExpiration, 20L, 20L);
    }

    public static BattlefieldManager getBattlefieldManager() {
        return battlefieldManager;
    }

    public List<Long> getAlertTimeList() {
        return alertTimeList;
    }
}
