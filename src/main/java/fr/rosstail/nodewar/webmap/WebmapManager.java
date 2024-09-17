package fr.rosstail.nodewar.webmap;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.webmap.types.BluemapHandler;
import fr.rosstail.nodewar.webmap.types.DynmapHandler;
import fr.rosstail.nodewar.webmap.types.SquaremapHandler;
import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WebmapManager {

    private class WebmapJob implements Runnable {

        /**
         * When an object implementing interface {@code Runnable} is used
         * to create a thread, starting the thread causes the object's
         * {@code run} method to be called in that separately executing
         * thread.
         * <p>
         * The general contract of the method {@code run} is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            territoryToEraseSet.forEach(territory -> {
                eraseTerritoryMarker(territory);
                eraseTerritorySurface(territory);
                // TODO
                eraseLineBetweenTerritories(territory, territory);
            });
            territoryToEraseSet.clear();

            territoryToEditSet.forEach(territory -> {
                editTerritoryMarker(territory);
                editTerritorySurface(territory);
                // TODO
                territory.getAttackRequirements().getTargetTerritoryList().forEach(targetTerritory -> {
                    editLineBetweenTerritories(territory, targetTerritory);
                });
            });
            territoryToEditSet.clear();

            territoryToDrawSet.forEach(territory -> {
                drawTerritoryMarker(territory);
                drawTerritorySurface(territory);
                if (territory.getDynmapInfo().isDrawLine()) {
                    territory.getAttackRequirements().getTargetTerritoryList().stream().filter(targetTerritory -> (
                            targetTerritory.getDynmapInfo().isDrawLine())).forEach(targetTerritory -> {
                        drawLineBetweenTerritories(territory, targetTerritory);
                    });
                }
            });
            territoryToDrawSet.clear();
        }
    }

    private int updatesPerTick = 1; //1-20
    long updatePeriod;
    boolean use3d;
    String infoWindow;
    boolean pause;
    boolean stop;
    int maxDepth;
    Nodewar plugin;

    private static String webmapPlugin;
    public static Map<String, Class<? extends NwIWebmapHandler>> iWebmapManagerMap = new HashMap<>();
    private final Set<NwIWebmapHandler> iWebmapHandlerSet = new HashSet<>();
    private static WebmapManager manager;

    private final Set<Territory> territoryToDrawSet = new HashSet<>();
    private final Set<Territory> territoryToEraseSet = new HashSet<>();
    private final Set<Territory> territoryToEditSet = new HashSet<>();

    static {
        iWebmapManagerMap.put("BlueMap", BluemapHandler.class);
        iWebmapManagerMap.put("dynmap", DynmapHandler.class);
        iWebmapManagerMap.put("squaremap", SquaremapHandler.class);
    }

    public WebmapManager(Nodewar plugin) {
        this.plugin = plugin;
    }

    public static boolean canAddCustomManager(String name) {
        return (!iWebmapManagerMap.containsKey(name));
    }

    /**
     * Add custom objective from add-ons
     *
     * @param name
     * @param customWebmapHandlerClass
     * @return
     */
    public static void addCustomManager(String name, Class<? extends NwIWebmapHandler> customWebmapHandlerClass) {
        iWebmapManagerMap.put(name, customWebmapHandlerClass);
        AdaptMessage.print("[Nodewar] Custom webmapmanager " + name + " added to the list !", AdaptMessage.prints.OUT);
    }

    public static void init(Nodewar plugin) {
        if (manager == null) {
            manager = new WebmapManager(plugin);
        }
    }

    public Set<String> getUsedSystems() {
        Set<String> systems = ConfigData.getConfigData().webmap.pluginList;
        Set<String> compatibleSystems = new HashSet<>();
        if (systems.contains("auto")) {
            iWebmapManagerMap.forEach((s, aClass) -> {
                if (Bukkit.getServer().getPluginManager().getPlugin(s) != null) {
                    compatibleSystems.add(s);
                }
            });
        }
        for (Map.Entry<String, Class<? extends NwIWebmapHandler>> entry :
                iWebmapManagerMap.entrySet().stream()
                        .filter(stringClassEntry -> !stringClassEntry.getKey()
                                .equalsIgnoreCase("auto")).collect(Collectors.toSet())) {
            String s1 = entry.getKey();
            if (Bukkit.getServer().getPluginManager().getPlugin(s1) != null) {
                if (s1.startsWith("!")) {
                    compatibleSystems.remove(s1.split("!")[1]);
                } else {
                    compatibleSystems.add(s1);
                }
            }
        }


        return compatibleSystems;
    }

    public void loadManager() {
        Set<String> usedSystemSet = getUsedSystems();

        if (!usedSystemSet.isEmpty()) {
            usedSystemSet.forEach(s -> {
                try {
                    Class<? extends NwIWebmapHandler> managerClass = iWebmapManagerMap.get(s);
                    Constructor<? extends NwIWebmapHandler> managerConstructor;
                    managerConstructor = managerClass.getDeclaredConstructor();
                    iWebmapHandlerSet.add(managerConstructor.newInstance());
                    AdaptMessage.print("[Nodewar] Using " + s + " webmap", AdaptMessage.prints.OUT);
                } catch (NoSuchMethodException e) {
                    throw new IllegalArgumentException("Missing appropriate constructor in WebmapHandler class.", e);
                } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (Exception e) {
                    AdaptMessage.print("[Nodewar] Failed webmap hook with " + s + ".", AdaptMessage.prints.ERROR);
                }
            });
            createMarkerSet();
            plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new WebmapJob(), updatePeriod, updatePeriod);
        } else {
            AdaptMessage.print("[Nodewar] Using no webmap", AdaptMessage.prints.OUT);
        }
    }

    public static WebmapManager getManager() {
        return manager;
    }

    public boolean addTerritoryToDraw(Territory territory) {
        return territoryToDrawSet.add(territory);
    }

    public boolean addTerritoryToEdit(Territory territory) {
        return territoryToEditSet.add(territory);
    }

    public boolean addTerritoryToErase(Territory territory) {
        return territoryToEraseSet.add(territory);
    }

    public boolean addTerritorySetToDraw(Set<Territory> territorySet) {
        return territoryToDrawSet.addAll(territorySet);
    }

    public boolean addTerritorySetToEdit(Set<Territory> territorySet) {
        return territoryToEditSet.addAll(territorySet);
    }

    public boolean addTerritorySetToErase(Set<Territory> territorySet) {
        return territoryToEraseSet.addAll(territorySet);
    }

    public void createMarkerSet() {
        iWebmapHandlerSet.forEach(NwIWebmapHandler::createMarkerSet);
    }

    public void drawTerritoryMarker(Territory territory) {
        iWebmapHandlerSet.forEach(nwIWebmapHandler -> {
            nwIWebmapHandler.drawTerritoryMarker(territory);
        });
    }

    public void editTerritoryMarker(Territory territory) {
        iWebmapHandlerSet.forEach(nwIWebmapHandler -> {
            nwIWebmapHandler.editTerritoryMarker(territory);
        });
    }

    public void drawTerritorySurface(Territory territory) {
        iWebmapHandlerSet.forEach(nwIWebmapHandler -> {
            nwIWebmapHandler.drawTerritorySurface(territory);
        });
    }

    void editTerritorySurface(Territory territory) {
        iWebmapHandlerSet.forEach(nwIWebmapHandler -> {
            nwIWebmapHandler.editTerritorySurface(territory);
        });
    }

    public void drawLineBetweenTerritories(Territory startTerritory, Territory endTerritory) {
        iWebmapHandlerSet.forEach(nwIWebmapHandler -> {
            nwIWebmapHandler.drawLineBetweenTerritories(startTerritory, endTerritory);
        });
    }

    public void editLineBetweenTerritories(Territory startTerritory, Territory endTerritory) {
        iWebmapHandlerSet.forEach(nwIWebmapHandler -> {
            nwIWebmapHandler.editLineBetweenTerritories(startTerritory, endTerritory);
        });
    }

    public void eraseTerritoryMarker(Territory territory) {
        iWebmapHandlerSet.forEach(nwIWebmapHandler -> {
            nwIWebmapHandler.eraseTerritoryMarker(territory);
        });
    }

    public void eraseTerritorySurface(Territory territory) {
        iWebmapHandlerSet.forEach(nwIWebmapHandler -> {
            nwIWebmapHandler.eraseTerritorySurface(territory);
        });
    }

    public void eraseLineBetweenTerritories(Territory territory, Territory otherTerritory) {
        iWebmapHandlerSet.forEach(nwIWebmapHandler -> {
            nwIWebmapHandler.eraseLineBetweenTerritories(territory, otherTerritory);
        });
    }

    public String convertYamlToHtml(String[] yamlLines) {
        StringBuilder htmlBuilder = new StringBuilder();
        int indentSize = 20;

        // Pattern to match & or § followed by a color code character
        Pattern colorCodePattern = Pattern.compile("[&§][0-9a-fk-or]");

        htmlBuilder.append("<div style=\"background-color:").append(ConfigData.getConfigData().webmap.backgroundColor).append("\">");
        for (String line : yamlLines) {
            // Calculate indent level
            int indentLevel = line.indexOf(line.trim()) / 2;
            int margin = indentLevel * indentSize;

            // Find all color codes in the line
            Matcher matcher = colorCodePattern.matcher(line);
            StringBuffer sb = new StringBuffer();

            // Replace color codes with corresponding HTML tags
            while (matcher.find()) {
                String code = matcher.group();
                String replacement = convertColorCodeToHtml(code);
                matcher.appendReplacement(sb, replacement);
            }
            matcher.appendTail(sb);

            // Wrap line in a div with proper margin
            htmlBuilder
                    .append("<div style=\"margin-left:").append(margin).append("px;\">")
                    .append(sb)
                    .append("</div>");
        }
        htmlBuilder.append("</div>");

        return htmlBuilder.toString();
    }

    private String convertColorCodeToHtml(String code) {
        switch (code.charAt(1)) {
            case '0':
                return "<span style=\"color:#000000;\">";
            case '1':
                return "<span style=\"color:#0000AA;\">";
            case '2':
                return "<span style=\"color:#00AA00;\">";
            case '3':
                return "<span style=\"color:#00AAAA;\">";
            case '4':
                return "<span style=\"color:#AA0000;\">";
            case '5':
                return "<span style=\"color:#AA00AA;\">";
            case '6':
                return "<span style=\"color:#FFAA00;\">";
            case '7':
                return "<span style=\"color:#AAAAAA;\">";
            case '8':
                return "<span style=\"color:#555555;\">";
            case '9':
                return "<span style=\"color:#5555FF;\">";
            case 'a':
                return "<span style=\"color:#55FF55;\">";
            case 'b':
                return "<span style=\"color:#55FFFF;\">";
            case 'c':
                return "<span style=\"color:#FF5555;\">";
            case 'd':
                return "<span style=\"color:#FF55FF;\">";
            case 'e':
                return "<span style=\"color:#FFFF55;\">";
            case 'f':
                return "<span style=\"color:#FFFFFF;\">";
            // Missing K (Random)
            case 'l':
                return "<span style=\"font-weight:900;\">";
            // Missing M (Strike)
            case 'n':
                return "<span style=\"text-decoration:underline;\">";
            case 'o':
                return "<span style=\"font-weight:italic;\">";
            case 'r':
                return "</span>";
            default:
                return "";
        }
    }
}
