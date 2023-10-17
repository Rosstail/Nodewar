package fr.rosstail.nodewar.territory.bossbar;

import fr.rosstail.nodewar.lang.AdaptMessage;
import org.bukkit.boss.BarStyle;

public class TerritoryBossBar {
    private TerritoryBossBarModel territoryBossBarModel;

    private BarStyle barStyle;

    public TerritoryBossBar(TerritoryBossBarModel childModel, TerritoryBossBarModel parentModel) {
        TerritoryBossBarModel clonedChildModel = childModel.clone();
        TerritoryBossBarModel clonedParentModel = parentModel.clone();
        this.territoryBossBarModel = new TerritoryBossBarModel(clonedChildModel, clonedParentModel);

        try {
            this.barStyle = BarStyle.valueOf(this.territoryBossBarModel.getStyle());
        } catch (IllegalArgumentException e) {
            AdaptMessage.print(
                    "The style " + this.territoryBossBarModel.getStyle() +
                            " does not exist. Using SEGMENTED_6 color instead"
                    , AdaptMessage.prints.ERROR);
            this.barStyle = BarStyle.SEGMENTED_6;
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
