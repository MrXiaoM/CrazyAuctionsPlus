package studio.trc.bukkit.crazyauctionsplus.event;

import studio.trc.bukkit.crazyauctionsplus.util.MessageUtil;
import studio.trc.bukkit.crazyauctionsplus.util.Category;
import studio.trc.bukkit.crazyauctionsplus.util.enums.ShopType;
import studio.trc.bukkit.crazyauctionsplus.util.FileManager;
import studio.trc.bukkit.crazyauctionsplus.util.FileManager.Files;
import studio.trc.bukkit.crazyauctionsplus.util.PluginControl;
import studio.trc.bukkit.crazyauctionsplus.database.Storage;

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
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                PluginControl.printStackTrace(ex);
            }
            if (player == null) return;
            Storage data = Storage.getPlayer(player);
            if (data.getMailNumber() > 0) {
                MessageUtil.sendMessage(player, "Email-of-player-owned-items");
            }
        }).start();
    }
}
