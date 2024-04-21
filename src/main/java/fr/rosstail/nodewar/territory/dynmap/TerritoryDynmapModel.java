package fr.rosstail.nodewar.territory.dynmap;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class TerritoryDynmapModel implements Cloneable {

    private String marker;
    private String drawLineStr;
    private String xString;
    private String yString;
    private String zString;

    public TerritoryDynmapModel(ConfigurationSection section) {
        if (section != null) {
            this.marker = section.getString("marker");
            this.drawLineStr = section.getString("draw-line");
            this.xString = section.getString("x");
            this.yString = section.getString("y");
            this.zString = section.getString("z");
        }
    }

    public TerritoryDynmapModel(TerritoryDynmapModel childObjectiveModel, @NotNull TerritoryDynmapModel parentObjectiveModel) {
        if (childObjectiveModel.getMarker() != null) {
            this.marker = childObjectiveModel.getMarker();
        } else {
            this.marker = parentObjectiveModel.getMarker();
        }
        if (childObjectiveModel.getDrawLineStr() != null) {
            this.drawLineStr = childObjectiveModel.getDrawLineStr();
        } else {
            this.drawLineStr = parentObjectiveModel.getDrawLineStr();
        }
        if (childObjectiveModel.getxString() != null) {
            this.xString = childObjectiveModel.getxString();
        } else if (parentObjectiveModel.getxString() != null) {
            this.xString = parentObjectiveModel.getxString();
        }
        if (childObjectiveModel.getyString() != null) {
            this.yString = childObjectiveModel.getyString();
        } else if (parentObjectiveModel.getyString() != null) {
            this.yString = parentObjectiveModel.getyString();
        }
        if (childObjectiveModel.getzString() != null) {
            this.zString = childObjectiveModel.getzString();
        } else if (parentObjectiveModel.getzString() != null) {
            this.zString = parentObjectiveModel.getzString();
        }
    }

    public String getMarker() {
        return marker;
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }

    public String getDrawLineStr() {
        return drawLineStr;
    }

    public void setDrawLineStr(String drawLineStr) {
        this.drawLineStr = drawLineStr;
    }

    public String getxString() {
        return xString;
    }

    public void setxString(String xString) {
        this.xString = xString;
    }

    public String getyString() {
        return yString;
    }

    public void setyString(String yString) {
        this.yString = yString;
    }

    public String getzString() {
        return zString;
    }

    public void setzString(String zString) {
        this.zString = zString;
    }

    @Override
    public TerritoryDynmapModel clone() {
        try {
            TerritoryDynmapModel clone = (TerritoryDynmapModel) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            clone.setMarker(getMarker());
            clone.setDrawLineStr(getDrawLineStr());
            clone.setxString(getxString());
            clone.setyString(getyString());
            clone.setzString(getzString());

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
