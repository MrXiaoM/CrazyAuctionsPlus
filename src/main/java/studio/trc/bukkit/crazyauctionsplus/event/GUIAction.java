package studio.trc.bukkit.crazyauctionsplus.event;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.meta.ItemMeta;
import studio.trc.bukkit.crazyauctionsplus.util.*;
import studio.trc.bukkit.crazyauctionsplus.util.enums.ShopType;
import studio.trc.bukkit.crazyauctionsplus.util.FileManager.*;
import studio.trc.bukkit.crazyauctionsplus.util.enums.CancelledReason;
import studio.trc.bukkit.crazyauctionsplus.database.GlobalMarket;
import studio.trc.bukkit.crazyauctionsplus.database.Storage;
import studio.trc.bukkit.crazyauctionsplus.api.events.AuctionSellEvent;
import studio.trc.bukkit.crazyauctionsplus.api.events.AuctionBuyEvent;
import studio.trc.bukkit.crazyauctionsplus.api.events.AuctionCancelledEvent;
import studio.trc.bukkit.crazyauctionsplus.api.events.AuctionNewBidEvent;
import studio.trc.bukkit.crazyauctionsplus.currency.CurrencyManager;
import studio.trc.bukkit.crazyauctionsplus.Main;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GUIAction
    extends GUI
    implements Listener
{
    private final static Main plugin = Main.getInstance();
    
    public final static Map<UUID, Object[]> repricing = new HashMap<>();
    
    @EventHandler
    public void onInvClose(InventoryCloseEvent e) {
        openingGUI.remove(e.getPlayer().getUniqueId());
        ProtectedConfiguration config = Files.CONFIG.getFile();
        Player player = (Player) e.getPlayer();
        if (e.getView().getTitle().contains(PluginControl.color(config.getString("Settings.Bidding-On-Item")))) {
            bidding.remove(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInvClick(InventoryClickEvent e) {
        if (!openingGUI.containsKey(e.getWhoClicked().getUniqueId())) {
            return;
        }
        Inventory inv = e.getInventory();
        if (!(inv.getHolder() instanceof GUI.Holder)) return;
        ProtectedConfiguration config = Files.CONFIG.getFile();
        GlobalMarket market = GlobalMarket.getMarket();
        Player player = (Player) e.getWhoClicked();
        GUI.Holder holder = (GUI.Holder) inv.getHolder();
        GUIType type = holder.getType();
        if (type.equals(GUIType.CATEGORY) || openingGUI.get(player.getUniqueId()).equals(GUIType.CATEGORY)) {
            e.setCancelled(true);
            if (FileManager.isBackingUp() || FileManager.isRollingBack() || PluginControl.isWorldDisabled(player)) {
                player.closeInventory();
                return;
            }
            int slot = e.getRawSlot();
            if (slot <= inv.getSize()) {
                if (e.getCurrentItem() != null) {
                    clickCategory(config, market, player, inv, slot, e.getCurrentItem(), e);
                }
            }
            return;
        }
        if (type.equals(GUIType.BIDDING_ITEM) || openingGUI.get(player.getUniqueId()).equals(GUIType.BIDDING_ITEM)) {
            e.setCancelled(true);
            if (FileManager.isBackingUp() || FileManager.isRollingBack() || PluginControl.isWorldDisabled(player)) {
                player.closeInventory();
                return;
            }
            int slot = e.getRawSlot();
            if (slot <= inv.getSize()) {
                if (e.getCurrentItem() != null) {
                    clickBiddingItem(config, market, player, inv, slot, e.getCurrentItem(), e);
                }
            }
            return;
        }
        if (type.equals(GUIType.GLOBALMARKET_MAIN) ||
                type.equals(GUIType.GLOBALMARKET_SELL) ||
                type.equals(GUIType.GLOBALMARKET_BID) ||
                type.equals(GUIType.GLOBALMARKET_BUY) ||
                type.equals(GUIType.ITEM_VIEWER) ||
                openingGUI.get(player.getUniqueId()).equals(GUIType.GLOBALMARKET_MAIN) ||
                openingGUI.get(player.getUniqueId()).equals(GUIType.GLOBALMARKET_SELL) ||
                openingGUI.get(player.getUniqueId()).equals(GUIType.GLOBALMARKET_BID) ||
                openingGUI.get(player.getUniqueId()).equals(GUIType.GLOBALMARKET_BUY) ||
                openingGUI.get(player.getUniqueId()).equals(GUIType.ITEM_VIEWER)) {
            e.setCancelled(true);
            if (FileManager.isBackingUp() || FileManager.isRollingBack() || PluginControl.isWorldDisabled(player)) {
                player.closeInventory();
                return;
            }
            int slot = e.getRawSlot();
            if (slot <= inv.getSize()) {
                if (e.getCurrentItem() != null) {
                    clickGlobalMarket(config, market, player, inv, slot, e.getCurrentItem(), e);
                }
            }
            return;
        }
        if (type.equals(GUIType.BUYING_ITEM) || openingGUI.get(player.getUniqueId()).equals(GUIType.BUYING_ITEM)) {
            e.setCancelled(true);
            if (FileManager.isBackingUp() || FileManager.isRollingBack() || PluginControl.isWorldDisabled(player)) {
                player.closeInventory();
                return;
            }
            int slot = e.getRawSlot();
            if (slot <= inv.getSize()) {
                if (e.getCurrentItem() != null) {
                    clickBuyingItem(config, market, player, inv, slot, e.getCurrentItem(), e);
                }
            }
            return;
        }
        if (type.equals(GUIType.SELLING_ITEM) || openingGUI.get(player.getUniqueId()).equals(GUIType.SELLING_ITEM)) {
            e.setCancelled(true);
            if (FileManager.isBackingUp() || FileManager.isRollingBack() || PluginControl.isWorldDisabled(player)) {
                player.closeInventory();
                return;
            }
            int slot = e.getRawSlot();
            if (slot <= inv.getSize()) {
                if (e.getCurrentItem() != null) {
                    clickSellingItem(config, market, player, inv, slot, e.getCurrentItem(), e);
                }
            }
            return;
        }
        if (type.equals(GUIType.ITEM_LIST) || openingGUI.get(player.getUniqueId()).equals(GUIType.ITEM_LIST)) {
            e.setCancelled(true);
            if (FileManager.isBackingUp() || FileManager.isRollingBack() || PluginControl.isWorldDisabled(player)) {
                player.closeInventory();
                return;
            }
            int slot = e.getRawSlot();
            if (slot <= inv.getSize()) {
                if (e.getCurrentItem() != null) {
                    clickItemList(config, market, player, inv, slot, e.getCurrentItem(), e);
                }
            }
            return;
        }
        if (type.equals(GUIType.ITEM_MAIL) || openingGUI.get(player.getUniqueId()).equals(GUIType.ITEM_MAIL)) {
            e.setCancelled(true);
            if (FileManager.isBackingUp() || FileManager.isRollingBack() || PluginControl.isWorldDisabled(player)) {
                player.closeInventory();
                return;
            }
            int slot = e.getRawSlot();
            if (slot <= inv.getSize()) {
                if (e.getCurrentItem() != null) {
                    clickItemMail(config, market, player, inv, slot, e.getCurrentItem(), e);
                }
            }
        }
    }

    private void clickCategory(
            ProtectedConfiguration config,
            GlobalMarket market,
            Player player,
            Inventory inv,
            int slot, ItemStack item,
            InventoryClickEvent e
    ) {
        String itemFlag = getFlag(item);
        if (itemFlag != null) {
            for (String name : config.getConfigurationSection("Settings.GUISettings.Category-Settings.Custom-Category").getKeys(false)) {
                Category category = Category.getModule(config.getString("Settings.GUISettings.Category-Settings.Custom-Category." + name + ".Category-Module"));
                if (category == null) continue;
                if (itemFlag.equals("Category-Settings.Custom-Category." + name)) {
                    openShop(player, shopType.get(player.getUniqueId()), category, 1);
                    playClick(player);
                    return;
                }
            }
            if (itemFlag.equals("OtherSettings.Categories-Back")) {
                openShop(player, shopType.get(player.getUniqueId()), shopCategory.get(player.getUniqueId()), 1);
                playClick(player);
                return;
            }
            if (itemFlag.equals("Category-Settings.ShopType-Category.Selling")) {
                openShop(player, ShopType.SELL, shopCategory.get(player.getUniqueId()), 1);
                shopType.put(player.getUniqueId(), ShopType.SELL);
                playClick(player);
                return;
            }
            if (itemFlag.equals("Category-Settings.ShopType-Category.Buying")) {
                openShop(player, ShopType.BUY, shopCategory.get(player.getUniqueId()), 1);
                shopType.put(player.getUniqueId(), ShopType.BUY);
                playClick(player);
                return;
            }
            if (itemFlag.equals("Category-Settings.ShopType-Category.Bidding")) {
                openShop(player, ShopType.BID, shopCategory.get(player.getUniqueId()), 1);
                shopType.put(player.getUniqueId(), ShopType.BID);
                playClick(player);
                return;
            }
            if (itemFlag.equals("Category-Settings.ShopType-Category.None")) {
                openShop(player, ShopType.ANY, shopCategory.get(player.getUniqueId()), 1);
                shopType.put(player.getUniqueId(), ShopType.ANY);
                playClick(player);
                return;
            }
        }
    }

    private void clickBiddingItem(
            ProtectedConfiguration config,
            GlobalMarket market,
            Player player,
            Inventory inv,
            int slot, ItemStack item,
            InventoryClickEvent e
    ) {
        String itemFlag = getFlag(item);
        if (itemFlag != null) {
            if (itemFlag.equals("Bidding-Buttons.Bid")) {
                long ID = biddingID.get(player.getUniqueId());
                double bid = bidding.get(player.getUniqueId());
                MarketGoods mg = market.getMarketGoods(ID);
                if (mg == null) {
                    e.setCancelled(true);
                    player.closeInventory();
                    return;
                }
                String topBidder = mg.getTopBidder();
                if (CurrencyManager.getMoney(player) < bid) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("%Money_Needed%", String.valueOf(bid - CurrencyManager.getMoney(player)));
                    placeholders.put("%money_needed%", String.valueOf(bid - CurrencyManager.getMoney(player)));
                    MessageUtil.sendMessage(player, "Need-More-Money", placeholders);
                    return;
                }
                if (mg.getPrice() > bid) {
                    MessageUtil.sendMessage(player, "Bid-More-Money");
                    return;
                }
                if (mg.getPrice() >= bid && !topBidder.equalsIgnoreCase("None")) {
                    MessageUtil.sendMessage(player, "Bid-More-Money");
                    return;
                }
                if (!topBidder.equalsIgnoreCase("None")) {
                    String[] oldTopBidder = mg.getTopBidder().split(":");
                    CurrencyManager.addMoney(Bukkit.getOfflinePlayer(UUID.fromString(oldTopBidder[1])), mg.getPrice());
                }
                Bukkit.getPluginManager().callEvent(new AuctionNewBidEvent(player, mg, bid));
                CurrencyManager.removeMoney(player, bid);
                mg.setPrice(bid);
                mg.setTopBidder(player.getName() + ":" + player.getUniqueId());
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("%Bid%", String.valueOf(bid));
                placeholders.put("%bid%", String.valueOf(bid));
                MessageUtil.sendMessage(player, "Bid-Msg", placeholders);
                bidding.put(player.getUniqueId(), 0);
                player.closeInventory();
                playClick(player);
                return;
            } else {
                Map<String, Integer> priceEdits = new HashMap<>();
                ConfigurationSection section = config.getConfig().getConfigurationSection("Settings.GUISettings.Auction-Settings.Bidding-Buttons");
                if (section != null) for (String price : section.getKeys(false)) {
                    if (PluginControl.isNumber(price)) {
                        String name = "Bidding-Buttons." + price;
                        priceEdits.put(name, Integer.valueOf(price));
                    }
                }
                for (String price : priceEdits.keySet()) {
                    if (itemFlag.equals(price)) {
                        try {
                            bidding.put(player.getUniqueId(), (bidding.get(player.getUniqueId()) + priceEdits.get(price)));
                            inv.setItem(4, getBiddingItem(player, biddingID.get(player.getUniqueId())));
                            inv.setItem(13, getBiddingGlass(player, biddingID.get(player.getUniqueId())));
                            playClick(player);
                            return;
                        } catch (Exception ex) {
                            player.closeInventory();
                            MessageUtil.sendMessage(player, "Item-Doesnt-Exist");
                            return;
                        }
                    }
                }
            }
        }
    }

    private void clickGlobalMarket(
            ProtectedConfiguration config,
            GlobalMarket market,
            Player player,
            Inventory inv,
            int slot, ItemStack item,
            InventoryClickEvent e
    ) {
        String itemFlag = getFlag(item);
        if (itemFlag != null) {
            if (itemFlag.equals("NextPage")) {
                PluginControl.updateCacheData();
                int page = Integer.parseInt(e.getView().getTitle().split("#")[1]);
                openShop(player, shopType.get(player.getUniqueId()), shopCategory.get(player.getUniqueId()), page + 1);
                playClick(player);
                return;
            }
            if (itemFlag.equals("PreviousPage")) {
                PluginControl.updateCacheData();
                int page = Integer.parseInt(e.getView().getTitle().split("#")[1]);
                if (page == 1) page++;
                openShop(player, shopType.get(player.getUniqueId()), shopCategory.get(player.getUniqueId()), page - 1);
                playClick(player);
                return;
            }
            if (itemFlag.equals("Refesh")) {
                PluginControl.updateCacheData();
                int page = Integer.parseInt(e.getView().getTitle().split("#")[1]);
                openShop(player, shopType.get(player.getUniqueId()), shopCategory.get(player.getUniqueId()), page);
                playClick(player);
                return;
            }
            if (itemFlag.equals("Shopping.Others")) {
                openShop(player, ShopType.SELL, shopCategory.get(player.getUniqueId()), 1);
                playClick(player);
                return;
            }
            if (itemFlag.equals("Shopping.Selling")) {
                openShop(player, ShopType.BUY, shopCategory.get(player.getUniqueId()), 1);
                playClick(player);
                return;
            }
            if (itemFlag.equals("Shopping.Buying")) {
                openShop(player, ShopType.BID, shopCategory.get(player.getUniqueId()), 1);
                playClick(player);
                return;
            }
            if (itemFlag.equals("Shopping.Bidding")) {
                openShop(player, ShopType.ANY, shopCategory.get(player.getUniqueId()), 1);
                playClick(player);
                return;
            }
            if (itemFlag.equals("Items-Mail")) {
                openPlayersMail(player, 1);
                playClick(player);
                return;
            }
            if (itemFlag.equals("Commoditys")) {
                openPlayersCurrentList(player, 1);
                playClick(player);
                return;
            }
            if (itemFlag.equals("Category")) {
                openCategories(player, shopType.get(player.getUniqueId()));
                playClick(player);
                return;
            }
            if (itemFlag.equals("Custom")) {
                List<String> commands = config.getStringList("Settings.GUISettings.OtherSettings.Custom.Commands");
                for (String line : PAPI.setPlaceholders(player, commands)) {
                    String lower = line.toLowerCase();
                    if (lower.startsWith("server:")) {
                        String command = line.substring(7)
                                .replace("%player%", player.getName())
                                .replace("%player_uuid%", player.getUniqueId().toString());
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
                    } else if (lower.startsWith("player:")) {
                        String command = line.substring(7)
                                .replace("%player%", player.getName())
                                .replace("%player_uuid%", player.getUniqueId().toString());
                        player.performCommand(command);
                    } else if (lower.startsWith("messages:")) {
                        String message = line.substring(9);
                        AdventureUtil.sendMessage(player, message);
                    } else if (lower.startsWith("message:")) {
                        String message = line.substring(8);
                        AdventureUtil.sendMessage(player, message);
                    }
                }
                playClick(player);
                if (config.getBoolean("Settings.GUISettings.OtherSettings.Custom.Close")) {
                    player.closeInventory();
                }
                return;
            }
            if (itemFlag.equals("Your-Item") || itemFlag.equals("Cant-Afford") || itemFlag.equals("Top-Bidder") || itemFlag.equals("Not-owned")) {
                return;
            }
        }
        Map<Integer, MenuIcon> map = itemUID.get(player.getUniqueId());
        MenuIcon icon = map == null ? null : map.get(slot);
        if (icon != null) {
            long uid = icon.uid;
            MarketGoods mgs = null;
            for (MarketGoods goods : market.getItems()) {
                if (goods.getUID() == uid) {
                    mgs = goods;
                    break;
                }
            }
            if (mgs != null) {
                if (PluginControl.hasMarketPermission(player, "Cancelled-Item")) {
                    if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                        UUID owner = mgs.getItemOwner().getUUID();
                        Player p = Bukkit.getPlayer(owner);
                        if (p != null) {
                            MessageUtil.sendMessage(p, "Admin-Force-Cancelled-To-Player");
                        }
                        switch (mgs.getShopType()) {
                            case BID: {
                                AuctionCancelledEvent event = new AuctionCancelledEvent((p != null ? p : Bukkit.getOfflinePlayer(owner)), mgs, CancelledReason.ADMIN_FORCE_CANCEL, ShopType.BID);
                                Bukkit.getPluginManager().callEvent(event);
                                Storage playerData = Storage.getPlayer(Bukkit.getOfflinePlayer(owner));
                                if (mgs.getTopBidder() != null && !mgs.getTopBidder().equalsIgnoreCase("None")) {
                                    OfflinePlayer op = Bukkit.getOfflinePlayer(UUID.fromString(mgs.getTopBidder().split(":")[1]));
                                    if (op != null) {
                                        CurrencyManager.addMoney(op, mgs.getPrice());
                                    }
                                }
                                playerData.addItem(new ItemMail(playerData.makeUID(), Bukkit.getOfflinePlayer(owner), mgs.getItem(), mgs.getFullTime(), System.currentTimeMillis(), false));
                                market.removeGoods(mgs.getUID());
                                break;
                            }
                            case BUY: {
                                AuctionCancelledEvent event = new AuctionCancelledEvent((p != null ? p : Bukkit.getOfflinePlayer(owner)), mgs, CancelledReason.ADMIN_FORCE_CANCEL, ShopType.BUY);
                                Bukkit.getPluginManager().callEvent(event);
                                CurrencyManager.addMoney(Bukkit.getOfflinePlayer(owner), mgs.getReward());
                                market.removeGoods(uid);
                                break;
                            }
                            case SELL: {
                                AuctionCancelledEvent event = new AuctionCancelledEvent((p != null ? p : Bukkit.getOfflinePlayer(owner)), mgs, CancelledReason.ADMIN_FORCE_CANCEL, ShopType.SELL);
                                Bukkit.getPluginManager().callEvent(event);
                                Storage playerData = Storage.getPlayer(Bukkit.getOfflinePlayer(owner));
                                playerData.addItem(new ItemMail(playerData.makeUID(), Bukkit.getOfflinePlayer(owner), mgs.getItem(), mgs.getFullTime(), System.currentTimeMillis(), false));
                                market.removeGoods(mgs.getUID());
                                break;
                            }
                        }
                        MessageUtil.sendMessage(player, "Admin-Force-Cancelled");
                        playClick(player);
                        int page = Integer.parseInt(e.getView().getTitle().split("#")[1]);
                        openShop(player, shopType.get(player.getUniqueId()), shopCategory.get(player.getUniqueId()), page);
                        return;
                    }
                }
                Runnable restoreItem = () -> inv.setItem(slot, item);
                if (mgs.getItemOwner().getUUID().equals(player.getUniqueId())) {
                    inv.setItem(slot, makeStandardIcon(config, "Settings.GUISettings.OtherSettings.Your-Item", "Your-Item"));
                    playClick(player);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, restoreItem, 3 * 20);
                    return;
                }
                double cost = mgs.getPrice();
                if (mgs.getShopType().equals(ShopType.BUY)) {
                    cost = mgs.getReward();
                }
                if (CurrencyManager.getMoney(player) < cost && !mgs.getShopType().equals(ShopType.BUY)) {
                    inv.setItem(slot, makeStandardIcon(config, "Settings.GUISettings.OtherSettings.Cant-Afford", "Cant-Afford"));
                    playClick(player);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, restoreItem, 3 * 20);
                    return;
                } else if (mgs.getShopType().equals(ShopType.BUY) && PluginControl.hasNoMaterial(player, mgs.getItem())) {
                    inv.setItem(slot, makeStandardIcon(config, "Settings.GUISettings.OtherSettings.Not-owned", "Not-owned"));
                    playClick(player);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, restoreItem, 3 * 20);
                    return;
                }
                switch (mgs.getShopType()) {
                    case BID: {
                        if (!mgs.getTopBidder().equalsIgnoreCase("None") && UUID.fromString(mgs.getTopBidder().split(":")[1]).equals(player.getUniqueId())) {
                            inv.setItem(slot, makeStandardIcon(config, "Settings.GUISettings.Auction-Settings.Top-Bidder", "Top-Bidder"));
                            playClick(player);
                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, restoreItem, 3 * 20);
                            return;
                        }
                        playClick(player);
                        openBidding(player, mgs.getUID());
                        biddingID.put(player.getUniqueId(), mgs.getUID());
                        break;
                    }
                    case BUY: {
                        playClick(player);
                        openSelling(player, mgs.getUID());
                        break;
                    }
                    case SELL: {
                        playClick(player);
                        openBuying(player, mgs.getUID());
                        break;
                    }
                }
                return;
            }
            playClick(player);
            openShop(player, shopType.get(player.getUniqueId()), shopCategory.get(player.getUniqueId()), 1);
            MessageUtil.sendMessage(player, "Item-Doesnt-Exist");
            return;
        }
        playClick(player);
        return;
    }

    private void clickBuyingItem(
            ProtectedConfiguration config,
            GlobalMarket market,
            Player player,
            Inventory inv,
            int slot, ItemStack item,
            InventoryClickEvent e
    ) {
        String itemFlag = getFlag(item);
        if (itemFlag != null) {
            if (itemFlag.equals("Confirm")) {
                long uid = IDs.get(player.getUniqueId());
                MarketGoods mg = market.getMarketGoods(uid);
                if (mg == null) {
                    playClick(player);
                    openShop(player, shopType.get(player.getUniqueId()), shopCategory.get(player.getUniqueId()), 1);
                    MessageUtil.sendMessage(player, "Item-Doesnt-Exist");
                    return;
                }
                if (PluginControl.isInvFull(player)) {
                    playClick(player);
                    player.closeInventory();
                    MessageUtil.sendMessage(player, "Inventory-Full");
                    return;
                }
                if (CurrencyManager.getMoney(player) < mg.getPrice()) {
                    playClick(player);
                    player.closeInventory();
                    HashMap<String, String> placeholders = new HashMap<>();
                    placeholders.put("%Money_Needed%", String.valueOf(mg.getPrice() - CurrencyManager.getMoney(player)));
                    placeholders.put("%money_needed%", String.valueOf(mg.getPrice() - CurrencyManager.getMoney(player)));
                    MessageUtil.sendMessage(player, "Need-More-Money", placeholders);
                    return;
                }
                UUID owner = mg.getItemOwner().getUUID();
                Bukkit.getPluginManager().callEvent(new AuctionBuyEvent(player, mg, mg.getPrice()));
                CurrencyManager.removeMoney(player, mg.getPrice());
                CurrencyManager.addMoney(PluginControl.getOfflinePlayer(owner), mg.getPrice());
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("%Price%", String.valueOf(mg.getPrice()));
                placeholders.put("%price%", String.valueOf(mg.getPrice()));
                placeholders.put("%Player%", player.getName());
                placeholders.put("%player%", player.getName());
                MessageUtil.sendMessage(player, "Bought-Item", placeholders);
                if (PluginControl.isOnline(owner) && PluginControl.getPlayer(owner) != null) {
                    Player p = PluginControl.getPlayer(owner);
                    MessageUtil.sendMessage(p, "Player-Bought-Item", placeholders);
                }
                if (player.getInventory().firstEmpty() != -1) {
                    player.getInventory().addItem(mg.getItem());
                } else {
                    player.getWorld().dropItem(player.getLocation(), mg.getItem());
                }
                market.removeGoods(uid);
                playClick(player);
                openShop(player, shopType.get(player.getUniqueId()), shopCategory.get(player.getUniqueId()), 1);
                return;
            }
            if (itemFlag.equals("Cancel")) {
                openShop(player, shopType.get(player.getUniqueId()), shopCategory.get(player.getUniqueId()), 1);
                playClick(player);
                return;
            }
        }
    }

    private void clickSellingItem(
            ProtectedConfiguration config,
            GlobalMarket market,
            Player player,
            Inventory inv,
            int slot, ItemStack item,
            InventoryClickEvent e
    ) {
        String itemFlag = getFlag(item);
        if (itemFlag != null) {
            if (itemFlag.equals("Confirm")) {
                long uid = IDs.get(player.getUniqueId());
                MarketGoods mg = market.getMarketGoods(uid);
                if (mg == null) {
                    playClick(player);
                    openShop(player, shopType.get(player.getUniqueId()), shopCategory.get(player.getUniqueId()), 1);
                    MessageUtil.sendMessage(player, "Item-Doesnt-Exist");
                    return;
                }
                ItemStack i = mg.getItem();
                if (PluginControl.hasNoMaterial(player, i)) {
                    playClick(player);
                    MessageUtil.sendMessage(player, "Item-Not-Found");
                    return;
                }
                UUID owner = mg.getItemOwner().getUUID();
                Bukkit.getPluginManager().callEvent(new AuctionSellEvent(player, mg, mg.getReward()));
                HashMap<String, String> placeholders = new HashMap<>();
                placeholders.put("%reward%", String.valueOf(mg.getReward()));
                placeholders.put("%Player%", player.getName());
                placeholders.put("%player%", player.getName());
                if (!PluginControl.takeMaterial(player, i)) {
                    MessageUtil.sendMessage(player, "Item-Not-Found");
                    return;
                }
                CurrencyManager.addMoney(player, mg.getReward());
                Storage playerData = Storage.getPlayer(Bukkit.getOfflinePlayer(owner));
                playerData.addItem(new ItemMail(playerData.makeUID(), Bukkit.getOfflinePlayer(owner), mg.getItem(), PluginControl.convertToMill(FileManager.Files.CONFIG.getFile().getString("Settings.Full-Expire-Time")), System.currentTimeMillis(), true));
                market.removeGoods(uid);
                MessageUtil.sendMessage(player, "Sell-Item", placeholders);
                if (PluginControl.isOnline(owner) && PluginControl.getPlayer(owner) != null) {
                    Player p = PluginControl.getPlayer(owner);
                    MessageUtil.sendMessage(p, "Player-Sell-Item", placeholders);
                }
                playClick(player);
                openShop(player, shopType.get(player.getUniqueId()), shopCategory.get(player.getUniqueId()), 1);
                return;
            }
            if (itemFlag.equals("Cancel")) {
                openShop(player, shopType.get(player.getUniqueId()), shopCategory.get(player.getUniqueId()), 1);
                playClick(player);
                return;
            }
        }
    }
    private void clickItemList(
            ProtectedConfiguration config,
            GlobalMarket market,
            Player player,
            Inventory inv,
            int slot, ItemStack item,
            InventoryClickEvent e
    ) {
        String itemFlag = getFlag(item);
        if (itemFlag != null) {
            if (itemFlag.equals("Player-Items-List-Back")) {
                openShop(player, shopType.get(player.getUniqueId()), shopCategory.get(player.getUniqueId()), 1);
                playClick(player);
                return;
            }

            Map<Integer, MenuIcon> map = itemUID.get(player.getUniqueId());
            MenuIcon icon = map == null ? null : map.get(slot);
            if (icon != null) {
                long uid = icon.uid;
                boolean Repricing = e.getClick().equals(ClickType.RIGHT) || e.getClick().equals(ClickType.SHIFT_RIGHT);
                MarketGoods mg = market.getMarketGoods(uid);
                if (mg == null) {
                    playClick(player);
                    openShop(player, shopType.get(player.getUniqueId()), shopCategory.get(player.getUniqueId()), 1);
                    MessageUtil.sendMessage(player, "Item-Doesnt-Exist");
                } else switch (mg.getShopType()) {
                    case BID: {
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("%item%", LangUtilsHook.getItemName(mg.getItem()));
                        MessageUtil.sendMessage(player, "Cancelled-Item-On-Bid", placeholders);
                        AuctionCancelledEvent event = new AuctionCancelledEvent(player, mg, CancelledReason.PLAYER_FORCE_CANCEL, ShopType.BID);
                        Bukkit.getPluginManager().callEvent(event);
                        if (mg.getTopBidder() != null && !mg.getTopBidder().equalsIgnoreCase("None")) {
                            OfflinePlayer op = Bukkit.getOfflinePlayer(UUID.fromString(mg.getTopBidder().split(":")[1]));
                            if (op != null) {
                                CurrencyManager.addMoney(op, mg.getPrice());
                            }
                        }
                        Storage playerData = Storage.getPlayer(mg.getItemOwner().getUUID());
                        playerData.addItem(new ItemMail(playerData.makeUID(), mg.getItemOwner().getUUID(), mg.getItem(), PluginControl.convertToMill(FileManager.Files.CONFIG.getFile().getString("Settings.Full-Expire-Time")), System.currentTimeMillis(), false));
                        market.removeGoods(uid);
                        repricing.remove(player.getUniqueId());
                        playClick(player);
                        openPlayersCurrentList(player, 1);
                        return;
                    }
                    case BUY: {
                        if (Repricing) {
                            repricing.put(player.getUniqueId(), new Object[]{mg, String.valueOf(System.currentTimeMillis() + (config.getInt("Settings.Repricing-Timeout") * 1000L))});
                            Map<String, String> placeholders = new HashMap<>();
                            placeholders.put("%item%", LangUtilsHook.getItemName(mg.getItem()));
                            placeholders.put("%timeout%", config.getString("Settings.Repricing-Timeout"));
                            MessageUtil.sendMessage(player, "Repricing", placeholders);
                            playClick(player);
                            player.closeInventory();
                            return;
                        }
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("%reward%", String.valueOf(mg.getReward()));
                        placeholders.put("%item%", LangUtilsHook.getItemName(mg.getItem()));
                        MessageUtil.sendMessage(player, "Cancelled-Item-On-Buy", placeholders);
                        AuctionCancelledEvent event = new AuctionCancelledEvent(player, mg, CancelledReason.PLAYER_FORCE_CANCEL, ShopType.BUY);
                        Bukkit.getPluginManager().callEvent(event);
                        CurrencyManager.addMoney(player, mg.getReward());
                        market.removeGoods(uid);
                        repricing.remove(player.getUniqueId());
                        playClick(player);
                        openPlayersCurrentList(player, 1);
                        return;
                    }
                    case SELL: {
                        if (Repricing) {
                            repricing.put(player.getUniqueId(), new Object[]{mg, String.valueOf(System.currentTimeMillis() + (config.getInt("Settings.Repricing-Timeout") * 1000L))});
                            Map<String, String> placeholders = new HashMap<>();
                            placeholders.put("%item%", LangUtilsHook.getItemName(mg.getItem()));
                            placeholders.put("%timeout%", config.getString("Settings.Repricing-Timeout"));
                            MessageUtil.sendMessage(player, "Repricing", placeholders);
                            playClick(player);
                            player.closeInventory();
                            return;
                        }
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("%item%", LangUtilsHook.getItemName(mg.getItem()));
                        MessageUtil.sendMessage(player, "Cancelled-Item-On-Sale", placeholders);
                        AuctionCancelledEvent event = new AuctionCancelledEvent(player, mg, CancelledReason.PLAYER_FORCE_CANCEL, ShopType.SELL);
                        Bukkit.getPluginManager().callEvent(event);
                        Storage playerData = Storage.getPlayer(mg.getItemOwner().getUUID());
                        playerData.addItem(new ItemMail(playerData.makeUID(), mg.getItemOwner().getUUID(), mg.getItem(), PluginControl.convertToMill(FileManager.Files.CONFIG.getFile().getString("Settings.Full-Expire-Time")), System.currentTimeMillis(), false));
                        market.removeGoods(uid);
                        repricing.remove(player.getUniqueId());
                        playClick(player);
                        openPlayersCurrentList(player, 1);
                        return;
                    }
                }
                return;
            }
        }
    }
    private void clickItemMail(
            ProtectedConfiguration config,
            GlobalMarket market,
            Player player,
            Inventory inv,
            int slot, ItemStack item,
            InventoryClickEvent e
    ) {
        Storage playerData = Storage.getPlayer(openingMail.get(player.getUniqueId()));
        String itemFlag = getFlag(item);
        if (itemFlag != null) {
            if (itemFlag.equals("Player-Items-Mail-Back")) {
                PluginControl.updateCacheData();
                playClick(player);
                openShop(player, shopType.get(player.getUniqueId()), shopCategory.get(player.getUniqueId()), 1);
                return;
            }
            if (itemFlag.equals("PreviousPage")) {
                PluginControl.updateCacheData();
                int page = Integer.parseInt(e.getView().getTitle().split("#")[1]);
                if (page == 1) page++;
                playClick(player);
                openPlayersMail(player, (page - 1));
                return;
            }
            if (itemFlag.equals("NextPage")) {
                PluginControl.updateCacheData();
                int page = Integer.parseInt(e.getView().getTitle().split("#")[1]);
                playClick(player);
                openPlayersMail(player, (page + 1));
                return;
            }
            if (itemFlag.equals("Return")) {
                PluginControl.updateCacheData();
                int page = Integer.parseInt(e.getView().getTitle().split("#")[1]);
                for (ItemMail im : playerData.getMailBox()) {
                    if (PluginControl.isInvFull(player)) {
                        MessageUtil.sendMessage(player, "Inventory-Full");
                        playerData.saveData();
                        return;
                    }
                    im.giveItem();
                }
                playerData.clearMailBox();
                MessageUtil.sendMessage(player, "Got-All-Item-Back");
                playClick(player);
                openPlayersMail(player, page);
                return;
            }
        }
        Map<Integer, MenuIcon> map = mailUID.get(player.getUniqueId());
        MenuIcon icon = map == null ? null : map.get(slot);
        if (icon != null) {
            long uid = icon.uid;
            for (ItemMail im : playerData.getMailBox()) {
                if (uid == im.getUID()) {
                    if (!PluginControl.isInvFull(player)) {
                        MessageUtil.sendMessage(player, "Got-Item-Back");
                        im.giveItem();
                        playerData.saveData();
                        playClick(player);
                        openPlayersMail(player, 1);
                    } else {
                        MessageUtil.sendMessage(player, "Inventory-Full");
                    }
                    return;
                }
            }
            playClick(player);
            openShop(player, shopType.get(player.getUniqueId()), shopCategory.get(player.getUniqueId()), 1);
            MessageUtil.sendMessage(player, "Item-Doesnt-Exist");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRepricing(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        if (repricing.get(player.getUniqueId()) != null) {
            ProtectedConfiguration config = Files.CONFIG.getFile();
            if (!PluginControl.isNumber(e.getMessage())) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("%Arg%", e.getMessage());
                placeholders.put("%arg%", e.getMessage());
                MessageUtil.sendMessage(player, "Not-A-Valid-Number", placeholders);
                repricing.remove(player.getUniqueId());
                e.setCancelled(true);
                return;
            }
            MarketGoods mg;
            try {
                mg = (MarketGoods) repricing.get(player.getUniqueId())[0];
            } catch (ClassCastException ex) {
                PluginControl.printStackTrace(ex);
                return;
            }
            if (mg != null && mg.getItem() != null) {
                double money = Double.parseDouble(e.getMessage());
                switch (mg.getShopType()) {
                    case BUY: {
                        if (money < config.getDouble("Settings.Minimum-Buy-Reward")) {
                            Map<String, String> placeholders = new HashMap<>();
                            placeholders.put("%reward%", String.valueOf(config.getDouble("Settings.Minimum-Buy-Reward")));
                            MessageUtil.sendMessage(player, "Buy-Reward-To-Low", placeholders);
                            repricing.remove(player.getUniqueId());
                            e.setCancelled(true);
                            return;
                        }
                        if (money > config.getLong("Settings.Max-Beginning-Buy-Reward")) {
                            Map<String, String> placeholders = new HashMap<>();
                            placeholders.put("%reward%", String.valueOf(config.getDouble("Settings.Max-Beginning-Buy-Reward")));
                            MessageUtil.sendMessage(player, "Buy-Reward-To-High", placeholders);
                            repricing.remove(player.getUniqueId());
                            e.setCancelled(true);
                            return;
                        }
                        if (CurrencyManager.getMoney(player) < money) { 
                            HashMap<String, String> placeholders = new HashMap<>();
                            placeholders.put("%Money_Needed%", String.valueOf(money - CurrencyManager.getMoney(player)));
                            placeholders.put("%money_needed%", String.valueOf(money - CurrencyManager.getMoney(player)));
                            MessageUtil.sendMessage(player, "Need-More-Money", placeholders);
                            repricing.remove(player.getUniqueId());
                            e.setCancelled(true);
                            return;
                        }
                        double tax = 0;
                        if (PluginControl.notBypassTaxRate(player, ShopType.BUY)) {
                            tax = money * PluginControl.getTaxRate(player, ShopType.BUY);
                        }
                        if (CurrencyManager.getMoney(player) < money + tax) { 
                            HashMap<String, String> placeholders = new HashMap<>();
                            placeholders.put("%Money_Needed%", String.valueOf((money + tax) - CurrencyManager.getMoney(player)));
                            placeholders.put("%money_needed%", String.valueOf((money + tax) - CurrencyManager.getMoney(player)));
                            MessageUtil.sendMessage(player, "Need-More-Money", placeholders);
                            e.setCancelled(true);
                            return;
                        }
                        CurrencyManager.removeMoney(player, money + tax);
                        CurrencyManager.addMoney(player, mg.getReward());
                        mg.setPrice(money);
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("%money%", String.valueOf(money));
                        placeholders.put("%tax%", String.valueOf(tax));
                        placeholders.put("%item%", LangUtilsHook.getItemName(mg.getItem()));
                        MessageUtil.sendMessage(player, "Repricing-Succeeded", placeholders);
                        repricing.remove(player.getUniqueId());
                        e.setCancelled(true);
                        break;
                    }
                    case SELL: {
                        if (money < config.getDouble("Settings.Minimum-Sell-Price")) {
                            Map<String, String> placeholders = new HashMap<>();
                            placeholders.put("%price%", String.valueOf(config.getDouble("Settings.Minimum-Sell-Price")));
                            MessageUtil.sendMessage(player, "Sell-Price-To-Low", placeholders);
                            repricing.remove(player.getUniqueId());
                            e.setCancelled(true);
                            return;
                        }
                        if (money > config.getLong("Settings.Max-Beginning-Sell-Price")) {
                            Map<String, String> placeholders = new HashMap<>();
                            placeholders.put("%price%", String.valueOf(config.getDouble("Settings.Max-Beginning-Sell-Price")));
                            MessageUtil.sendMessage(player, "Sell-Price-To-High", placeholders);
                            repricing.remove(player.getUniqueId());
                            e.setCancelled(true);
                            return;
                        }
                        double tax = 0;
                        if (PluginControl.notBypassTaxRate(player, ShopType.SELL)) {
                            tax = money * PluginControl.getTaxRate(player, ShopType.SELL);
                        }
                        if (CurrencyManager.getMoney(player) < tax) {
                            HashMap<String, String> placeholders = new HashMap<>();
                            placeholders.put("%Money_Needed%", String.valueOf(tax - CurrencyManager.getMoney(player)));
                            placeholders.put("%money_needed%", String.valueOf(tax - CurrencyManager.getMoney(player)));
                            MessageUtil.sendMessage(player, "Need-More-Money", placeholders);
                            e.setCancelled(true);
                            return;
                        }
                        CurrencyManager.removeMoney(player, tax);
                        mg.setPrice(money);
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("%money%", String.valueOf(money));
                        placeholders.put("%tax%", String.valueOf(tax));
                        placeholders.put("%item%", LangUtilsHook.getItemName(mg.getItem()));
                        MessageUtil.sendMessage(player, "Repricing-Succeeded", placeholders);
                        repricing.remove(player.getUniqueId());
                        e.setCancelled(true);
                        break;
                    }
                }
            } else {
                MessageUtil.sendMessage(player, "Repricing-Failed");
                repricing.remove(player.getUniqueId());
                e.setCancelled(true);
            }
        }
    }
}
