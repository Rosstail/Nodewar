package fr.rosstail.nodewar.player;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.flags.Flags;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.events.playerevents.PlayerDeployEvent;
import fr.rosstail.nodewar.events.playerevents.PlayerInitDeployEvent;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class PlayerDataManager {
    private static final Nodewar plugin = Nodewar.getInstance();
    private static final AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();

    private static final Map<String, PlayerData> playerDataMap = new HashMap<>();
    private static final Map<Player, PlayerInitDeployEvent> playerInitDeployEventMap = new HashMap<>();
    private static int deployScheduler;

    public static PlayerData initPlayerDataToMap(PlayerData model) {
        return playerDataMap.put(model.getUsername(), model);
    }

    public static PlayerData removePlayerDataFromMap(Player player) {
        return playerDataMap.remove(player.getName());
    }

    public static String getPlayerNameFromUUID(String uuid) {
        if (!Bukkit.getOnlineMode()) {
            return uuid;
        }
        try {
            URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                reader.close();

                String response = responseBuilder.toString();

                return extractPlayerNameFromUUID(response);

            }

            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get player name using UUID from Mojang API
     *
     * @param response
     * @return
     */
    private static String extractPlayerNameFromUUID(String response) {
        int index = response.indexOf("\"name\" : \"");
        if (index != -1) {
            int startIndex = index + "\"name\" : \"".length();
            int endIndex = response.indexOf("\"", startIndex);
            if (endIndex != -1) {
                return response.substring(startIndex, endIndex);
            }
        }
        return null;
    }

    /**
     * Get player name using username from Mojang API
     *
     * @param username the name of targeted player
     * @return
     */
    public static String getPlayerUUIDFromName(String username) {
        if (!Bukkit.getOnlineMode()) {
            return username;
        }
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                reader.close();

                String uuid = extractUUID(responseBuilder.toString());

                if (uuid != null) {
                    return uuid.substring(0, 8) + "-" +
                            uuid.substring(8, 12) + "-" +
                            uuid.substring(12, 16) + "-" +
                            uuid.substring(16, 20) + "-" +
                            uuid.substring(20);
                } else {
                    AdaptMessage.print("Impossible to get UUID of " + username, AdaptMessage.prints.WARNING);
                }
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                AdaptMessage.print(LangManager.getMessage(LangMessage.COMMANDS_PLAYER_DOES_NOT_EXIST).replaceAll("\\[player]", username), AdaptMessage.prints.WARNING);
            } else {
                AdaptMessage.print("HTTP request error in PlayerDataManager#getPlayerUUIDFromName\n" + responseCode, AdaptMessage.prints.WARNING);
            }

            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String extractUUID(String response) {
        int index = response.indexOf("\"id\" : \"");
        if (index != -1) {
            int startIndex = index + "\"id\" : \"".length();
            int endIndex = response.indexOf("\"", startIndex);
            if (endIndex != -1) {
                return response.substring(startIndex, endIndex);
            }
        }
        return null;
    }

    public static void startDeployHandler() {
        Runnable handlePlayerDeployEvents = () -> {
            for (Map.Entry<Player, PlayerInitDeployEvent> entry : playerInitDeployEventMap.entrySet()) {
                Player player = entry.getKey();
                PlayerInitDeployEvent playerInitDeployEvent = entry.getValue();
                if (playerInitDeployEvent.isCancelled()) {
                    playerInitDeployEventMap.remove(player);
                    if (playerInitDeployEvent.getStartTime() > System.currentTimeMillis()) {
                        player.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_DEPLOY_CANCELLED));
                    }
                } else {
                    if (playerInitDeployEvent.getStartTime() <= System.currentTimeMillis()) {
                        if (playerInitDeployEvent.getTerritory().getOwnerTeam() != playerInitDeployEvent.getPlayerTeam()) {
                            playerInitDeployEvent.setCancelled(true);
                        } else {
                            PlayerDeployEvent playerDeployEvent = new PlayerDeployEvent(player,
                                    BukkitAdapter.adapt(playerInitDeployEvent.getProtectedRegion().getFlag(Flags.TELE_LOC)));
                            Bukkit.getPluginManager().callEvent(playerDeployEvent);
                        }
                        playerInitDeployEventMap.remove(player);
                    } else if (playerInitDeployEvent.getTickLeft() % 20 == 0) {
                        player.sendMessage((playerInitDeployEvent.getTickLeft() / 20) + " before deploy");
                    }
                    playerInitDeployEvent.setTickLeft(playerInitDeployEvent.getTickLeft() - 1);
                }
            }
        };

        deployScheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(Nodewar.getInstance(), handlePlayerDeployEvents, 1L, 1L);
    }

    public static Map<Player, PlayerInitDeployEvent> getPlayerInitDeployEventMap() {
        return playerInitDeployEventMap;
    }

    public static void cancelPlayerDeploy(Player player) {
        PlayerInitDeployEvent playerInitDeployEvent = playerInitDeployEventMap.get(player);
        if (playerInitDeployEvent != null) {
            playerInitDeployEvent.setCancelled(true);
        }
    }

    public static Map<String, PlayerData> getPlayerDataMap() {
        return playerDataMap;
    }

    public static void stopTimer(int scheduler) {
        Bukkit.getScheduler().cancelTask(scheduler);
    }

    public static void saveAllPlayerModelToStorage() {
        getPlayerDataMap().forEach((s, model) -> {
            StorageManager.getManager().updatePlayerModel(model, true);
        });
    }

    public static PlayerData getPlayerDataFromMap(Player player) {
        return getPlayerDataMap().get(player.getName());
    }
}
