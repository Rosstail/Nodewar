package fr.rosstail.conquest.required.lang;

import java.util.Collections;
import java.util.List;
import fr.rosstail.conquest.Conquest;

public class LangManager
{
    private static Lang currentLang;
    
    public static void initCurrentLang() {
        LangManager.currentLang = new Lang(Conquest.getInstance().getConfig().getString("general.lang"));
        if (!LangManager.currentLang.available()) {
            LangManager.currentLang = null;
        }
    }
    
    public static Lang getCurrentLang() {
        return LangManager.currentLang;
    }
    
    public static String getMessage(final LangMessage message) {
        return getMessage(LangManager.currentLang, message);
    }
    
    public static List<String> getListMessage(final LangMessage message) {
        return getListMessage(LangManager.currentLang, message);
    }
    
    public static String getMessage(final Lang lang, final LangMessage message) {
        return (lang != null && lang.available()) ? lang.getConfiguration().getString(message.getId()) : "no-lang selected";
    }
    
    public static List<String> getListMessage(final Lang lang, final LangMessage message) {
        return (lang != null && lang.available()) ? lang.getConfiguration().getStringList(message.getId()) : Collections.singletonList("no-lang selected");
    }
}
