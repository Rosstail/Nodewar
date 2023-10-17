package fr.rosstail.nodewar.territory.bossbar;

import org.bukkit.configuration.ConfigurationSection;

public class TerritoryBossBarModel implements Cloneable {

    private String style;

    public TerritoryBossBarModel(ConfigurationSection section) {
        if (section == null) {
            return;
        }
        this.style = section.getString("style");
    }

    public TerritoryBossBarModel(TerritoryBossBarModel childModel, TerritoryBossBarModel parentModel) {
        TerritoryBossBarModel clonedParentModel = parentModel.clone();
        System.out.println(childModel.getStyle() + " " +  parentModel.getStyle());
        clonedParentModel.setStyle(childModel.getStyle() != null ? childModel.getStyle() : parentModel.getStyle());
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
        System.out.println("Set style AAAA " + style);
    }

    @Override
    public TerritoryBossBarModel clone() {
        try {
            TerritoryBossBarModel clone = (TerritoryBossBarModel) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            clone.setStyle(getStyle());

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
