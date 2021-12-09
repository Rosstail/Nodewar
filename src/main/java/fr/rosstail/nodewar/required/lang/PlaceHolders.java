package fr.rosstail.nodewar.required.lang;

public enum PlaceHolders
{
    PLAYER_NAME("%" + fr.rosstail.nodewar.Nodewar.getDimName() + "_player%"),
    PLAYER_EMPIRE_STARTER("%" + fr.rosstail.nodewar.Nodewar.getDimName() + "_player_empire_"),
    PLAYER_EMPIRE("%" + fr.rosstail.nodewar.Nodewar.getDimName() + "_player_empire%");
    
    private final String text;
    
    PlaceHolders(final String text) {
        this.text = text;
    }
    
    public String getText() {
        return this.text;
    }
}
