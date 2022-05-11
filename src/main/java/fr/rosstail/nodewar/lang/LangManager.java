package fr.rosstail.nodewar.lang;

import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.required.FileResourcesUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class LangManager
{
    private static Lang lang;
    
    public static void initCurrentLang() {
        LangManager.lang = new Lang(Nodewar.getInstance().getConfig().getString("general.lang"));
        if (!LangManager.lang.available()) {
            try {
                FileResourcesUtils.main("lang", Nodewar.getInstance(), true); // default english
                LangManager.lang = new Lang("en_EN");
            } catch (IOException e) {
                e.printStackTrace();
                LangManager.lang = null;
            }
        }
    }
    
    public static Lang getLang() {
        return LangManager.lang;
    }
    
    public static String getMessage(final LangMessage message) {
        return getMessage(LangManager.lang, message);
    }
    
    public static List<String> getListMessage(final LangMessage message) {
        return getListMessage(LangManager.lang, message);
    }
    
    public static String getMessage(final Lang lang, final LangMessage message) {
        return (lang != null && lang.available()) ? lang.getConfiguration().getString(message.getMessage()) : "no lang selected";
    }
    
    public static List<String> getListMessage(final Lang lang, final LangMessage message) {
        return (lang != null && lang.available()) ? lang.getConfiguration().getStringList(message.getMessage()) : Collections.singletonList("no lang selected");
    }


}
