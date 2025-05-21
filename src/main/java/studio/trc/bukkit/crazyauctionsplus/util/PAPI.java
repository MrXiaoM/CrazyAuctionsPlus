package studio.trc.bukkit.crazyauctionsplus.util;

import com.google.common.collect.Lists;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;

import java.util.List;

public class PAPI {
    private static boolean isEnabled = false;
    public static void initialize() {
        isEnabled = PluginControl.isPresent("me.clip.placeholderapi.PlaceholderAPI");
    }
    public static String setPlaceholders(OfflinePlayer player, String s) {
        if (!isEnabled) return player == null ? s : s.replace("%player_name%", String.valueOf(player.getName()));
        return PlaceholderAPI.setPlaceholders(player, s);
    }
    public static List<String> setPlaceholders(OfflinePlayer player, List<String> s) {
        if (!isEnabled) return player == null ? s : Lists.newArrayList(String.join("\n", s).replace("%player_name%", String.valueOf(player.getName())).split("\n"));
        return PlaceholderAPI.setPlaceholders(player, s);
    }
}
