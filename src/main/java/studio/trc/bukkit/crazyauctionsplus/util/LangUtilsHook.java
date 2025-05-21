package studio.trc.bukkit.crazyauctionsplus.util;

import com.meowj.langutils.LangUtils;
import com.meowj.langutils.lang.LanguageHelper;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LangUtilsHook {
    private static boolean isPluginPresent = false;
    private static String lang = "en_us";
    public static void initialize() {
        isPluginPresent = Bukkit.getPluginManager().isPluginEnabled("LangUtils");
        if (isPluginPresent) lang = LangUtils.plugin.config.getString("FallbackLanguage", "en_us");
    }
    public static String getItemName(ItemStack item) {
        return getItemName(item, lang);
    }
    public static String getItemName(ItemStack item, String locale) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName()) return meta.getDisplayName();
        if (isPluginPresent) {
            return LanguageHelper.getItemName(item, locale);
        }
        try {
            return (String) item.getClass().getMethod("getI18NDisplayName").invoke(item);
        } catch (Exception ex) {
            return item.getType().toString().toLowerCase().replace("_", " ");
        }
    }
}
