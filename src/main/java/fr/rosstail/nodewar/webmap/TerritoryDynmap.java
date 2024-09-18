package fr.rosstail.nodewar.webmap;

import fr.rosstail.nodewar.territory.Territory;

public class TerritoryDynmap {

    protected Territory territory;
    private final TerritoryWebmapModel territoryWebmapModel;

    private boolean drawLine = true;

    private boolean xSet = false;
    private float x;
    private boolean ySet = false;
    private float y;
    private boolean zSet = false;
    private float z;

    public TerritoryDynmap(Territory territory, TerritoryWebmapModel childModel, TerritoryWebmapModel parentModel) {
        this.territory = territory;
        TerritoryWebmapModel clonedChildModel = childModel.clone();
        TerritoryWebmapModel clonedParentModel = parentModel.clone();
        this.territoryWebmapModel = new TerritoryWebmapModel(clonedChildModel, clonedParentModel);

        if (territoryWebmapModel.getDrawLineStr() != null) {
            drawLine = Boolean.parseBoolean(territoryWebmapModel.getDrawLineStr());
        }
        if (territoryWebmapModel.getxString() != null) {
            x = Float.parseFloat(territoryWebmapModel.getxString());
            xSet = true;
        }
        if (territoryWebmapModel.getyString() != null) {
            y = Float.parseFloat(territoryWebmapModel.getyString());
            ySet = true;
        }
        if (territoryWebmapModel.getzString() != null) {
            z = Float.parseFloat(territoryWebmapModel.getzString());
            zSet = true;
        }
    }

    public Territory getTerritory() {
        return territory;
    }

    public TerritoryWebmapModel getTerritoryDynmapModel() {
        return territoryWebmapModel;
    }

    public boolean isDrawLine() {
        return drawLine;
    }

    public float getX() {
        return x;
    }

    public boolean isxSet() {
        return xSet;
    }

    public float getY() {
        return y;
    }

    public boolean isySet() {
        return ySet;
    }

    public float getZ() {
        return z;
    }

    public boolean iszSet() {
        return zSet;
    }
}
