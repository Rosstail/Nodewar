package fr.rosstail.nodewar.territory.bossbar;

import org.bukkit.configuration.ConfigurationSection;

public class TerritoryBossBarModel implements Cloneable {

    private String style;

    public TerritoryBossBarModel(ConfigurationSection section) {
        if (section == null) {
            return;
        }
        this.style = section.getString("style", "SEGMENTED_6");
    }

    public TerritoryBossBarModel(TerritoryBossBarModel childModel, TerritoryBossBarModel parentModel) {
        TerritoryBossBarModel clonedChildModel = childModel.clone();
        TerritoryBossBarModel clonedParentModel = parentModel.clone();
        setStyle(
                clonedChildModel.getStyle() != null ? clonedChildModel.getStyle() : clonedParentModel.getStyle()
        );
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    @Override
    public TerritoryBossBarModel clone() {
        try {
            TerritoryBossBarModel clone = (TerritoryBossBarModel) super.clone();
            clone.setStyle(getStyle());

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
