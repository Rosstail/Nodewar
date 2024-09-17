package fr.rosstail.nodewar.webmap;

import fr.rosstail.nodewar.territory.Territory;

public class TerritoryDynmap {

    protected Territory territory;
    private final TerritoryDynmapModel territoryDynmapModel;

    private boolean drawLine = true;

    private boolean xSet = false;
    private float x;
    private boolean ySet = false;
    private float y;
    private boolean zSet = false;
    private float z;

    public TerritoryDynmap(Territory territory, TerritoryDynmapModel childModel, TerritoryDynmapModel parentModel) {
        this.territory = territory;
        TerritoryDynmapModel clonedChildModel = childModel.clone();
        TerritoryDynmapModel clonedParentModel = parentModel.clone();
        this.territoryDynmapModel = new TerritoryDynmapModel(clonedChildModel, clonedParentModel);

        if (territoryDynmapModel.getDrawLineStr() != null) {
            drawLine = Boolean.parseBoolean(territoryDynmapModel.getDrawLineStr());
        }
        if (territoryDynmapModel.getxString() != null) {
            x = Float.parseFloat(territoryDynmapModel.getxString());
            xSet = true;
        }
        if (territoryDynmapModel.getyString() != null) {
            y = Float.parseFloat(territoryDynmapModel.getyString());
            ySet = true;
        }
        if (territoryDynmapModel.getzString() != null) {
            z = Float.parseFloat(territoryDynmapModel.getzString());
            zSet = true;
        }
    }

    public Territory getTerritory() {
        return territory;
    }

    public TerritoryDynmapModel getTerritoryDynmapModel() {
        return territoryDynmapModel;
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
