package fr.rosstail.nodewar.team;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum RelationType {
    NEUTRAL(true),
    TEAM(true),
    ALLY(true),
    TRUCE(true),
    ENEMY(true),
    CONTROLLED(false) // Used only in colour
    ;

    private final boolean selectable;

    RelationType(boolean selectable) {
        this.selectable = selectable;
    }

    public static Set<RelationType> getSelectableRelations() {
        return Arrays.stream(RelationType.values()).filter(RelationType::isSelectable).collect(Collectors.toSet());
    }

    public boolean isSelectable() {
        return selectable;
    }
}
