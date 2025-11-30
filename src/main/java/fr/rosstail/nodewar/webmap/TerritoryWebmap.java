package fr.rosstail.nodewar.webmap;

import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.territory.Territory;

public class TerritoryWebmap extends TerritoryWebmapModel {

    protected Territory territory;

    private boolean drawLine = true;

    private final boolean xSet;
    private float x;

    private final boolean ySet;
    private float y;

    private final boolean zSet;
    private float z;

    private final String genericDesc;

    public TerritoryWebmap(Territory territory, TerritoryWebmapModel childModel, TerritoryWebmapModel parentModel) {
        super(new TerritoryWebmapModel(childModel, parentModel));
        this.territory = territory;

        this.drawLine = getDrawLineStr() == null || Boolean.parseBoolean(getDrawLineStr());

        this.xSet = getxString() != null;
        this.x = getxString() != null ? Float.parseFloat(getxString()) : 0F;
        this.ySet = getyString() != null;
        this.y = getyString() != null ? Float.parseFloat(getyString()) : 0F;
        this.zSet = getzString() != null;
        this.z = getzString() != null ? Float.parseFloat(getzString()) : 0F;

        this.genericDesc = adaptGenericMessage(LangManager.getMessage(LangMessage.WEBMAP_TERRITORY_DESCRIPTION));
    }

    public Territory getTerritory() {
        return territory;
    }

    public boolean isDrawLine() {
        return drawLine;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public boolean isxSet() {
        return xSet;
    }

    public boolean isySet() {
        return ySet;
    }

    public boolean iszSet() {
        return zSet;
    }

    public String adaptGenericMessage(String message) {
        if (message == null) {
            return null;
        }

        return message
                .replaceAll("\\[terr(itory)?_w(eb)?map_desc(ription)?]", LangManager.getMessage(LangMessage.WEBMAP_TERRITORY_DESCRIPTION))
                .replaceAll("\\[terr(itory)?_w(eb)?map_battle_status_waiting]", LangManager.getMessage(LangMessage.WEBMAP_TERRITORY_BATTLE_STATUS_WAITING))
                .replaceAll("\\[terr(itory)?_w(eb)?map_battle_status_ongoing]", LangManager.getMessage(LangMessage.WEBMAP_TERRITORY_BATTLE_STATUS_ONGOING))
                .replaceAll("\\[terr(itory)?_w(eb)?map_battle_status_ending]", LangManager.getMessage(LangMessage.WEBMAP_TERRITORY_BATTLE_STATUS_ENDING))
                .replaceAll("\\[terr(itory)?_w(eb)?map_battle_status_ended]", LangManager.getMessage(LangMessage.WEBMAP_TERRITORY_BATTLE_STATUS_ENDED))
                .replaceAll("\\[terr(itory)?_w(eb)?map_battle_status_waiting_short]", LangManager.getMessage(LangMessage.WEBMAP_TERRITORY_BATTLE_STATUS_WAITING_SHORT))
                .replaceAll("\\[terr(itory)?_w(eb)?map_battle_status_ongoing_short]", LangManager.getMessage(LangMessage.WEBMAP_TERRITORY_BATTLE_STATUS_ONGOING_SHORT))
                .replaceAll("\\[terr(itory)?_w(eb)?map_battle_status_ending_short]", LangManager.getMessage(LangMessage.WEBMAP_TERRITORY_BATTLE_STATUS_ENDING_SHORT))
                .replaceAll("\\[terr(itory)?_w(eb)?map_battle_status_ended_short]", LangManager.getMessage(LangMessage.WEBMAP_TERRITORY_BATTLE_STATUS_ENDED_SHORT))
        ;
    }

    public String adaptMessage(String message) {
        if (message == null) {
            return null;
        }

        if (territory.isUnderProtection()) {
            message = message.replaceAll("\\[terr(itory)?_webmap_protected]", LangManager.getMessage(LangMessage.WEBMAP_TERRITORY_PROTECTED))
                    .replaceAll("\\[terr(itory)?_webmap_protected_short]", LangManager.getMessage(LangMessage.WEBMAP_TERRITORY_PROTECTED_SHORT));
        } else {
            message = message.replaceAll("\\[terr(itory)?_webmap_protected]", LangManager.getMessage(LangMessage.WEBMAP_TERRITORY_VULNERABLE))
                    .replaceAll("\\[terr(itory)?_webmap_protected_short]", LangManager.getMessage(LangMessage.WEBMAP_TERRITORY_VULNERABLE_SHORT));
        }

        return territory.adaptMessage(message);
    }

    public String getGenericDesc() {
        return genericDesc;
    }
}
