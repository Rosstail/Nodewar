package fr.rosstail.nodewar.team;

import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum RelationType {
    ALLY(1, true, LangMessage.TEAM_RELATION_ALLY),
    TRUCE(2, true, LangMessage.TEAM_RELATION_TRUCE),
    NEUTRAL(3, true, LangMessage.TEAM_RELATION_NEUTRAL),
    ENEMY(4, true, LangMessage.TEAM_RELATION_ENEMY),
    TEAM(0, false, LangMessage.TEAM_RELATION_TEAM), // Used only for members of team
    CONTROLLED(0, false, LangMessage.TEAM_RELATION_CONTROLLED) // Used only in colour
    ;

    private final int weight;
    private final boolean selectable;

    private final LangMessage displayLangMessage;

    RelationType(int weight, boolean selectable, LangMessage displayLangMessage) {
        this.weight = weight;
        this.selectable = selectable;
        this.displayLangMessage = displayLangMessage;
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

    public String getDisplay() {
        return LangManager.getMessage(displayLangMessage);
    }
}
