package studio.trc.bukkit.crazyauctionsplus.event;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import studio.trc.bukkit.crazyauctionsplus.api.events.AuctionListEvent;
import studio.trc.bukkit.crazyauctionsplus.api.events.AuctionNewBidEvent;
import studio.trc.bukkit.crazyauctionsplus.api.events.AuctionWinBidEvent;
import studio.trc.bukkit.crazyauctionsplus.util.AuctionProcess;
import studio.trc.bukkit.crazyauctionsplus.util.FileManager.ProtectedConfiguration;
import studio.trc.bukkit.crazyauctionsplus.util.FileManager.Files;
import studio.trc.bukkit.crazyauctionsplus.util.LangUtilsHook;
import studio.trc.bukkit.crazyauctionsplus.util.MarketGoods;
import studio.trc.bukkit.crazyauctionsplus.util.PluginControl;
import studio.trc.bukkit.crazyauctionsplus.util.enums.ShopType;

public class AuctionEvents
    extends AuctionProcess
    implements Listener
{
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void starting(AuctionListEvent e) {
        if (!e.getShopType().equals(ShopType.BID)) return;
        Player p = e.getPlayer();
        ProtectedConfiguration config = Files.CONFIG.getFile();
        if (config.getBoolean("Settings.Auction-Process-Settings.Starting.Enabled")) {
            String item = LangUtilsHook.getItemName(e.getItem());
            for (String message : config.getStringList("Settings.Auction-Process-Settings.Starting.Messages")) {
                Bukkit.broadcastMessage(message.replace("%player%", p.getName()).replace("%money%", String.valueOf(e.getMoney())).replace("%item%", item).replace("{prefix}", PluginControl.getPrefix()).replace("&", "§"));
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void bidding(AuctionNewBidEvent e) {
        Player p = e.getPlayer();
        ProtectedConfiguration config = Files.CONFIG.getFile();
        MarketGoods mg = e.getMarketGoods();
        if (config.getBoolean("Settings.Auction-Process-Settings.Bidding.Enabled")) {
            String item = LangUtilsHook.getItemName(mg.getItem());
            for (String message : config.getStringList("Settings.Auction-Process-Settings.Bidding.Messages")) {
                Bukkit.broadcastMessage(message.replace("%bidder%", p.getName()).replace("%price%", String.valueOf(e.getPrice())).replace("%item%", item).replace("{prefix}", PluginControl.getPrefix()).replace("&", "§"));
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void ending(AuctionWinBidEvent e) {
        Player p = e.getPlayer();
        ProtectedConfiguration config = Files.CONFIG.getFile();
        MarketGoods mg = e.getMarketGoods();
        if (config.getBoolean("Settings.Auction-Process-Settings.Ending.Enabled")) {
            String item = LangUtilsHook.getItemName(mg.getItem());
            for (String message : config.getStringList("Settings.Auction-Process-Settings.Ending.Messages")) {
                Bukkit.broadcastMessage(message.replace("%bidder%", p.getName()).replace("%price%", String.valueOf(e.getPrice())).replace("%item%", item).replace("{prefix}", PluginControl.getPrefix()).replace("&", "§"));
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void newBid(AuctionNewBidEvent e) {
        Player p = e.getPlayer();
        ProtectedConfiguration config = Files.CONFIG.getFile();
        if (config.getBoolean("Settings.Auction-Process-Settings.Bid-Overtime.Enabled")) {
            MarketGoods mg = e.getMarketGoods();
            for (String time : config.getConfigurationSection("Settings.Auction-Process-Settings.Bid-Overtime.Times").getKeys(false)) {
                try {
                    double timeTillExpire = Double.parseDouble(time);
                    if (timeTillExpire * 1000 >= mg.getTimeTillExpire() - System.currentTimeMillis()) {
                        double overtime = config.getDouble("Settings.Auction-Process-Settings.Bid-Overtime.Times." + time + ".Overtime");
                        mg.setTimeTillExpire(mg.getTimeTillExpire() + (long) (overtime * 1000));
                        String item = LangUtilsHook.getItemName(mg.getItem());
                        if (config.get("Settings.Auction-Process-Settings.Bid-Overtime.Times." + time + ".Messages") != null) {
                            for (String message : config.getStringList("Settings.Auction-Process-Settings.Bid-Overtime.Times." + time + ".Messages")) {
                                Bukkit.broadcastMessage(message.replace("%bidder%", p.getName()).replace("%price%", String.valueOf(e.getPrice())).replace("%item%", item).replace("{prefix}", PluginControl.getPrefix()).replace("&", "§"));
                            }
                        }
                        break;
                    }
                } catch (NumberFormatException ex) {
                    PluginControl.printStackTrace(ex);
                }
            }
        }
    }
} 
