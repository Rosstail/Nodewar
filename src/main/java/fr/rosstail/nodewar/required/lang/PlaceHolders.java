package fr.rosstail.conquest.required.lang;

import fr.rosstail.conquest.Conquest;

public enum PlaceHolders
{
    PLAYER_NAME("%" + Conquest.getDimName() + "_player%"),
    PLAYER_EMPIRE_STARTER("%" + Conquest.getDimName() + "_player_empire_"),
    PLAYER_EMPIRE("%" + Conquest.getDimName() + "_player_empire%");
    
    private final String text;
    
    PlaceHolders(final String text) {
        this.text = text;
    }
    
    public String getText() {
        return this.text;
    }
}
