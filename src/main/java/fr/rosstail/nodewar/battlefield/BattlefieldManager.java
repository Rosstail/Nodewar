package fr.rosstail.nodewar.battlefield;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.events.territoryevents.TerritoryOwnerNeutralizeEvent;
import fr.rosstail.nodewar.events.territoryevents.TerritoryProtectionChangeEvent;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.webmap.WebmapManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

public class BattlefieldManager {

    private static BattlefieldManager manager;
    private final Nodewar plugin;

    private final List<Long> alertTimeList = new ArrayList<>();

    private final List<Battlefield> battlefieldList = new ArrayList<>();
    private int scheduler;

    public BattlefieldManager(Nodewar plugin) {
        this.plugin = plugin;
    }

    public static void init(Nodewar plugin) {
        if (manager == null) {
            manager = new BattlefieldManager(plugin);
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
                WebmapManager.getManager().addTerritoryToEdit(territory);
            });
        }
        if (battlefield.getModel().isAnnouncement()) {
            String announcement = battlefield.adaptMessage(LangManager.getMessage(LangMessage.BATTLEFIELD_ANNOUNCEMENT_OPEN));
            Bukkit.broadcastMessage(AdaptMessage.getAdaptMessage().adaptMessage(announcement));
        }
        battlefield.getModel().setOpen(true);
        battlefield.getTerritoryList().forEach(territory -> {
            territory.getModel().setUnderProtection(false);

            TerritoryProtectionChangeEvent territoryProtectionChangeEvent = new TerritoryProtectionChangeEvent(territory, false);
            Bukkit.getPluginManager().callEvent(territoryProtectionChangeEvent);

            WebmapManager.getManager().addTerritoryToEdit(territory);
        });

        StorageManager.getManager().updateBattlefieldModel(battlefield.getModel(), true);
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

                TerritoryProtectionChangeEvent territoryProtectionChangeEvent = new TerritoryProtectionChangeEvent(territory, true);
                Bukkit.getPluginManager().callEvent(territoryProtectionChangeEvent);

                WebmapManager.getManager().addTerritoryToEdit(territory);
            });
        }

        battlefield.getModel().setOpenDateTime(getNextDateTimeMillis(battlefield.getModel().getStartDaysStrSet(), battlefield.getModel().getStartTimesStrSet()));
        battlefield.getModel().setCloseDateTime(battlefield.getModel().getOpenDateTime() + battlefield.getModel().getDuration());

        StorageManager.getManager().updateBattlefieldModel(battlefield.getModel(), true);

    }


    public long getNextDateTimeMillis(Set<String> daysOfWeekStr, Set<String> timeStrSet) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        Set<DayOfWeek> targetDays = new HashSet<>();
        for (String day : daysOfWeekStr) {
            targetDays.add(DayOfWeek.valueOf(day.toUpperCase()));
        }

        Set<LocalTime> targetTimes = new HashSet<>();
        if (!timeStrSet.isEmpty()) {
            for (String time : timeStrSet) {
                String[] parts = time.split(":");
                int hour = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1]);
                targetTimes.add(LocalTime.of(hour, minute));
            }
        } else {
            targetTimes.add(LocalTime.of(0, 0));
        }

        LocalDateTime nextDateTime = null;
        long smallestDifference = Long.MAX_VALUE;

        for (int i = 0; i <= 7; i++) { // 7 next days
            LocalDate currentDate = today.plusDays(i);
            DayOfWeek currentDayOfWeek = currentDate.getDayOfWeek();

            if (!targetDays.isEmpty() && !targetDays.contains(currentDayOfWeek)) {
                continue;
            }

            for (LocalTime targetTime : targetTimes) {
                LocalDateTime candidateDateTime = LocalDateTime.of(currentDate, targetTime);

                if (i == 0 && candidateDateTime.isBefore(now)) {
                    continue;
                }

                long difference = Duration.between(now, candidateDateTime).toMillis();
                if (difference >= 0 && difference < smallestDifference) {
                    smallestDifference = difference;
                    nextDateTime = candidateDateTime;
                }
            }
        }

        if (nextDateTime != null) {
            return nextDateTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000L;
        }

        return now.withDayOfMonth(now.getDayOfMonth() + 1).withHour(0).withMinute(0).withSecond(0).withNano(0)
                .atZone(ZoneId.systemDefault()).toEpochSecond() * 1000L;
    }

    @Deprecated(since = "2.2.3", forRemoval = true)
    public long getNextDateTimeMillis(Set<String> daysOfWeekStr, int hour, int minute) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalTime targetTime = LocalTime.of(hour, minute);

        Set<DayOfWeek> targetDays = new HashSet<>();
        for (String day : daysOfWeekStr) {
            targetDays.add(DayOfWeek.valueOf(day.toUpperCase()));
        }

        LocalDateTime nextDateTime;
        if (targetDays.isEmpty()) { //tomorrow or today
            if (now.toLocalTime().isBefore(targetTime)) {
                nextDateTime = LocalDateTime.of(today, targetTime);
            } else {
                nextDateTime = LocalDateTime.of(today.plusDays(1), targetTime);
            }
        } else {
            LocalDate nextDate = today;
            for (int i = 0; i <= 7; i++) {
                nextDate = today.plusDays(i);
                if (targetDays.contains(nextDate.getDayOfWeek())) {
                    if (i == 0 && now.toLocalTime().isAfter(targetTime)) { // today but hour/minute late
                        continue;
                    }
                    break; //next nearest date found
                }
            }
            nextDateTime = LocalDateTime.of(nextDate, targetTime);
        }

        return nextDateTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000L;
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

            battlefieldListToOpen.forEach(battlefield -> battlefield.getTerritoryList().forEach(territory -> {
                WebmapManager.getManager().addTerritoryToEdit(territory);
            }));

            battlefieldListToClose.forEach(battlefield -> battlefield.getTerritoryList().forEach(territory -> {
                WebmapManager.getManager().addTerritoryToEdit(territory);
            }));
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

    public List<Battlefield> getBattlefieldList() {
        return battlefieldList;
    }

    public static BattlefieldManager getManager() {
        return manager;
    }

    @Deprecated(forRemoval = true, since = "2.1.8")
    public static BattlefieldManager getBattlefieldManager() {
        return manager;
    }

    public List<Long> getAlertTimeList() {
        return alertTimeList;
    }
}
