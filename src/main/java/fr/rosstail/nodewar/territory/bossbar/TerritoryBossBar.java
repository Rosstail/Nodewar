package fr.rosstail.nodewar.territory.bossbar;

import fr.rosstail.nodewar.lang.AdaptMessage;
import org.apache.commons.lang.ObjectUtils;
import org.bukkit.boss.BarStyle;

public class TerritoryBossBar {
    private TerritoryBossBarModel territoryBossBarModel;

    private BarStyle barStyle;

    public TerritoryBossBar(TerritoryBossBarModel childModel, TerritoryBossBarModel parentModel) {
        TerritoryBossBarModel clonedChildModel = childModel.clone();
        TerritoryBossBarModel clonedParentModel = parentModel.clone();
        this.territoryBossBarModel = new TerritoryBossBarModel(clonedChildModel, clonedParentModel);
        String styleString = this.territoryBossBarModel.getStyle();
        if (styleString != null) {
            try {
                this.barStyle = BarStyle.valueOf(styleString.toUpperCase());
            } catch (IllegalArgumentException e) {
                AdaptMessage.print(
                        "the style " + this.territoryBossBarModel.getStyle() +
                                " does not exist. Using SEGMENTED_6 color instead"
                        , AdaptMessage.prints.ERROR);
                this.barStyle = BarStyle.SEGMENTED_6;
            }
        } else {
            this.barStyle = BarStyle.SOLID;
        }
    }


    public TerritoryBossBarModel getBossBarModel() {
        return territoryBossBarModel;
    }

    public void setBossBarModel(TerritoryBossBarModel territoryBossBarModel) {
        this.territoryBossBarModel = territoryBossBarModel;
    }

    public BarStyle getBarStyle() {
        return barStyle;
    }

    public void setBarStyle(BarStyle barStyle) {
        this.barStyle = barStyle;
    }
}
