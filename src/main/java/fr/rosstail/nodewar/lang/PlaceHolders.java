package fr.rosstail.nodewar.lang;

import fr.rosstail.nodewar.Nodewar;

public enum PlaceHolders
{
    PLAYER_NAME("%player%"),
    PLAYER_EMPIRE_STARTER("%" + Nodewar.getDimName() + "_player_empire"),
    PLAYER_EMPIRE("%" + Nodewar.getDimName() + "_player_empire%"),
    PLAYER_EMPIRE_DISPLAY("%" + Nodewar.getDimName() + "_player_empire_display%"),

    EMPIRE_STARTER("%" + Nodewar.getDimName() + "_empire_"),
    EMPIRE_NAME("%" + Nodewar.getDimName() + "_empire_name%"),
    EMPIRE_DISPLAY("%" + Nodewar.getDimName() + "_empire_display%"),
    EMPIRE_FRIENDLY_FIRE("%" + Nodewar.getDimName() + "_empire_friendly_fire%"),

    WORLD_STARTER("%" + Nodewar.getDimName() + "_world_"),
    WORLD_NAME("%" + Nodewar.getDimName() + "_world_name%"),

    TERRITORY_STARTER("%" + Nodewar.getDimName() + "_territory_"),
    TERRITORY_NAME("%" + Nodewar.getDimName() + "_territory_name%"),
    TERRITORY_WORLD("%" + Nodewar.getDimName() + "_territory_world%"),
    TERRITORY_REGION("%" + Nodewar.getDimName() + "_territory_region%"),
    TERRITORY_DISPLAY("%" + Nodewar.getDimName() + "_territory_display%"),
    TERRITORY_VULNERABLE("%" + Nodewar.getDimName() + "_territory_vulnerability%"),
    TERRITORY_MAXIMUM_RESISTANCE("%" + Nodewar.getDimName() + "_territory_max_resistance%"),
    TERRITORY_RESISTANCE("%" + Nodewar.getDimName() + "_territory_resistance%"),
    TERRITORY_REGEN_DAMAGE("%" + Nodewar.getDimName() + "_territory_regendamage%"),
    TERRITORY_EMPIRE_OWNER("%" + Nodewar.getDimName() + "_territory_empire_owner%"),
    TERRITORY_EMPIRE_ADVANTAGE("%" + Nodewar.getDimName() + "_territory_empire_advantage%"),
    TERRITORY_ON_ATTACK("%" + Nodewar.getDimName() + "_territory_damaged%"),
    TERRITORY_TERRITORY("%" + Nodewar.getDimName() + "_territory_territory%"),
    TERRITORY_ATTACKER_RATIO("%" + Nodewar.getDimName() + "_point_attacker_ratio%");

    private final String text;
    
    PlaceHolders(final String text) {
        this.text = text;
    }
    
    public String getText() {
        return this.text;
    }
}
