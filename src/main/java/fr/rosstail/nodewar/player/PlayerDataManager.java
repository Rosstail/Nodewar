package fr.rosstail.nodewar.player;

import fr.rosstail.nodewar.Nodewar;
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

    public static PlayerData initPlayerDataToMap(PlayerData model) {
        return playerDataMap.put(model.getUsername(), model);
    }

    public static PlayerData removePlayerDataFromMap(Player player) {
        return playerDataMap.remove(player.getName());
    }

    public static String getPlayerNameFromUUID(String uuid) {
        String playerName = "UnknownPlayer";
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

                playerName = extractPlayerNameFromUUID(response);

            }

            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return playerName;
    }

    /**
     * Get player name using UUID from Mojang API
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
     * @param username the name of targeted player
     * @return
     */
    public static String getPlayerUUIDFromName(String username) {
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

    /*public static ItemStack getPlayerHead(final String playerName, final ItemStack item) {

        final boolean isNewVersion = Arrays.stream(Material.values()).map((Function<? super Material, ?>)Enum::name).collect((Collector<? super Object, ?, List<? super Object>>) Collectors.toList()).contains("PLAYER_HEAD");
        final Material type = Material.matchMaterial(isNewVersion ? "PLAYER_HEAD" : "SKULL_ITEM");
        final ItemStack newItem = new ItemStack(type, item.getAmount());
        if (!isNewVersion) {
            item.setDurability((short)3);
        }
        final SkullMeta skullMeta = (SkullMeta)newItem.getItemMeta();
        skullMeta.setOwner(playerName);
        newItem.setItemMeta(skullMeta);
        return newItem;

        return null;
    }*/

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
}
