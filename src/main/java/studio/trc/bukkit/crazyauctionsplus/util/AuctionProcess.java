package studio.trc.bukkit.crazyauctionsplus.util;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import studio.trc.bukkit.crazyauctionsplus.Main;
import studio.trc.bukkit.crazyauctionsplus.api.events.AuctionWinBidEvent;
import studio.trc.bukkit.crazyauctionsplus.currency.CurrencyManager;
import studio.trc.bukkit.crazyauctionsplus.database.GlobalMarket;
import studio.trc.bukkit.crazyauctionsplus.database.Storage;
import studio.trc.bukkit.crazyauctionsplus.util.FileManager.Files;
import studio.trc.bukkit.crazyauctionsplus.util.FileManager.ProtectedConfiguration;
import studio.trc.bukkit.crazyauctionsplus.util.enums.ShopType;

public class AuctionProcess
{
    public static void updateAuctionData() {
        if (FileManager.isBackingUp()) return;
        if (FileManager.isRollingBack()) return;
        if (FileManager.isSyncing()) return;
        ProtectedConfiguration config = Files.CONFIG.getFile();
        GlobalMarket market = GlobalMarket.getMarket();
        List<MarketGoods> array = market.getItems();
        if (!array.isEmpty()) {
            for (int i = array.size() - 1;i > -1;i--) {
                MarketGoods mg = array.get(i);
                if (mg == null) {
                    continue;
                }
                if (mg.getItem() == null) {
                    market.removeGoods(mg);
                    continue;
                }
                if (mg.getShopType().equals(ShopType.BID)) {
                    if (mg.expired()) {
                        if (mg.getTopBidder().equalsIgnoreCase("None")) {
                            Storage playerdata = Storage.getPlayer(mg.getItemOwner().getUUID());
                            Map<String, String> placeholders = new HashMap();
                            String item;
                            try {
                                item = mg.getItem().getItemMeta().hasDisplayName() ? mg.getItem().getItemMeta().getDisplayName() : (String) mg.getItem().getClass().getMethod("getI18NDisplayName").invoke(mg.getItem());
                            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                item = mg.getItem().getItemMeta().hasDisplayName() ? mg.getItem().getItemMeta().getDisplayName() : mg.getItem().getType().toString().toLowerCase().replace("_", " ");
                            }
                            placeholders.put("%item%", item);
                            ItemMail im = new ItemMail(playerdata.makeUID(), mg.getItemOwner().getUUID(), mg.getItem(), PluginControl.convertToMill(FileManager.Files.CONFIG.getFile().getString("Settings.Full-Expire-Time")), mg.getAddedTime(), false);
                            playerdata.addItem(im);
                            market.removeGoods(mg.getUID());
                            if (mg.getItemOwner().getPlayer() != null) {
                                MessageUtil.sendMessage(mg.getItemOwner().getPlayer(), "Item-Has-Expired", placeholders);
                            }
                        } else {
                            UUID buyer = UUID.fromString(mg.getTopBidder().split(":")[1]);
                            UUID seller = mg.getItemOwner().getUUID();
                            CurrencyManager.addMoney(Bukkit.getOfflinePlayer(seller), mg.getPrice());
                            Storage playerdata = Storage.getPlayer(buyer);
                            ItemMail im = new ItemMail(playerdata.makeUID(), PluginControl.getOfflinePlayer(buyer), mg.getItem(), PluginControl.convertToMill(FileManager.Files.CONFIG.getFile().getString("Settings.Full-Expire-Time")), mg.getAddedTime(), false);
                            playerdata.addItem(im);
                            market.removeGoods(mg.getUID());
                            double price = mg.getPrice();
                            Map<String, String> placeholders = new HashMap();
                            placeholders.put("%price%", String.valueOf(mg.getPrice()));
                            placeholders.put("%player%", PluginControl.getOfflinePlayer(buyer).getName());
                            if (PluginControl.isOnline(buyer) && PluginControl.getPlayer(buyer) != null) {
                                Player player = Bukkit.getPlayer(buyer);
                                AuctionWinBidEvent event = new AuctionWinBidEvent(player, mg, price);
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        Bukkit.getPluginManager().callEvent(event);
                                    }
                                }.runTask(Main.getInstance());
                                MessageUtil.sendMessage(player, "Win-Bidding", placeholders);
                            }
                            Player player = Bukkit.getPlayer(seller);
                            if (player != null) {
                                MessageUtil.sendMessage(player, "Someone-Won-Players-Bid", placeholders);
                            }
                        }
                    } else {
                        if (config.getBoolean("Settings.Auction-Process-Settings.Countdown-Tips.Enabled")) {
                            long l = (mg.getTimeTillExpire() - System.currentTimeMillis()) / 1000;
                            if (config.get("Settings.Auction-Process-Settings.Countdown-Tips.Times." + l) != null) {
                                String item = LangUtilsHook.getItemName(mg.getItem());
                                for (String message : config.getStringList("Settings.Auction-Process-Settings.Countdown-Tips.Times." + l)) {
                                    Bukkit.broadcastMessage(message.replace("%owner%", mg.getItemOwner().getName()).replace("%item%", item).replace("{prefix}", PluginControl.getPrefix()).replace("&", "§"));
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    public static class AuctionUpdateThread extends Thread {
        
        public static AuctionUpdateThread thread;
        
        private final double updateDelay;
        
        public AuctionUpdateThread(double updateDelay) {
            this.updateDelay = updateDelay;
            thread = AuctionUpdateThread.this;
        }
        
        public double getUpdateDelay() {
            return updateDelay;
        }
        
        @Override
        public void run() {
            while (true) {
                try {
                    sleep((long) (updateDelay * 1000));
                    updateAuctionData();
                } catch (Exception ex) {
                    PluginControl.printStackTrace(ex);
                }
            }
        }
    }
}
