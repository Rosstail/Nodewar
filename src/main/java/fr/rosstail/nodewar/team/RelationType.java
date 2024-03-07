package fr.rosstail.nodewar.team;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum RelationType {
    ALLY(1, true),
    TRUCE(2, true),
    NEUTRAL(3, true),
    ENEMY(4, true),
    TEAM(0, false), // Used only for members of team
    CONTROLLED(0, false) // Used only in colour
    ;

    private final int weight;
    private final boolean selectable;

    RelationType(int weight, boolean selectable) {
        this.weight = weight;
        this.selectable = selectable;
    }

    public static Set<RelationType> getSelectableRelations() {
        return Arrays.stream(RelationType.values()).filter(RelationType::isSelectable).collect(Collectors.toSet());
    }

    public boolean isSelectable() {
        return selectable;
    }

    public int getWeight() {
        return weight;
    }
}
