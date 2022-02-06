package studio.trc.bukkit.crazyauctionsplus.event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import studio.trc.bukkit.crazyauctionsplus.util.enums.Messages;
import studio.trc.bukkit.crazyauctionsplus.util.Category;
import studio.trc.bukkit.crazyauctionsplus.util.enums.ShopType;
import studio.trc.bukkit.crazyauctionsplus.util.FileManager;
import studio.trc.bukkit.crazyauctionsplus.util.FileManager.Files;
import studio.trc.bukkit.crazyauctionsplus.util.PluginControl;
import studio.trc.bukkit.crazyauctionsplus.util.Updater;
import studio.trc.bukkit.crazyauctionsplus.database.Storage;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class Join
    implements Listener
{
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        GUIAction.setCategory(player, Category.getDefaultCategory());
        GUIAction.setShopType(player, ShopType.ANY);
        if (FileManager.isBackingUp() || FileManager.isRollingBack() || PluginControl.isWorldDisabled(player)) {
            return;
        }
        if (!Files.CONFIG.getFile().getBoolean("Settings.Join-Message")) return;
        new Thread(() -> {
            try {
                /**
                 * Hi, I'm Dean.
                 * Today is Jul, 2021, I'm writting this message.
                 * This code was developed in 2020
                 * I can't understand WHY I FUCKING WRITTEN LIKE THIS.
                 * WTF is Thread.sleep(2000) ?????
                 * Why did I do this?? I don't know.
                 * So I make a decision, I want to remake this plugin.
                 * If you interesting, you can follow me at spigotmc or Github https://github.com/TRCStudioDean
                 */
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                PluginControl.printStackTrace(ex);
            }
            if (player == null) return;
            Storage data = Storage.getPlayer(player);
            if (data.getMailNumber() > 0) {
                Messages.sendMessage(player, "Email-of-player-owned-items");
            }
        }).start();
        
        /**
         * Written at Jul, 2021
         */
//        if (PluginControl.enableUpdater()) {
            String now = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String checkUpdateTime = new SimpleDateFormat("yyyy-MM-dd").format(Updater.getTimeOfLastCheckUpdate());
            if (!now.equals(checkUpdateTime)) {
                Updater.checkUpdate();
            }
//        }
        if (Updater.isFoundANewVersion()) {
            if (PluginControl.hasPermission(player, "Permissions.Updater", false)) {
                String nowVersion = Bukkit.getPluginManager().getPlugin("CrazyAuctionsPlus").getDescription().getVersion();
                Map<String, String> placeholders = new HashMap();
                    placeholders.put("%nowVersion%", nowVersion);
                    placeholders.put("%version%", Updater.getNewVersion());
                    placeholders.put("%link%", Updater.getLink());
                    placeholders.put("%description%", Updater.getDescription());
                Messages.sendMessage(player, "Updater.Checked", placeholders);
            }
        }
    }
}
