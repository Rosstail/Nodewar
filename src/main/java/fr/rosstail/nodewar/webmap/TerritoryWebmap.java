package fr.rosstail.nodewar.webmap;

import fr.rosstail.nodewar.territory.Territory;

public class TerritoryWebmap extends TerritoryWebmapModel {

    protected Territory territory;

    private boolean drawLine = true;

    private boolean xSet;
    private float x;

    private boolean ySet;
    private float y;

    private boolean zSet;
    private float z;

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
}
