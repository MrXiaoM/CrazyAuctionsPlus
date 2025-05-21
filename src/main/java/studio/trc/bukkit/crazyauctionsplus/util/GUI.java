package studio.trc.bukkit.crazyauctionsplus.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import studio.trc.bukkit.crazyauctionsplus.database.GlobalMarket;
import studio.trc.bukkit.crazyauctionsplus.database.Storage;
import studio.trc.bukkit.crazyauctionsplus.event.GUIAction;
import studio.trc.bukkit.crazyauctionsplus.util.FileManager.ProtectedConfiguration;
import studio.trc.bukkit.crazyauctionsplus.util.enums.ShopType;
import studio.trc.bukkit.crazyauctionsplus.util.enums.Version;

public class GUI
{
    /**
     * Unknown... from old CrazyAuctions plug-in.
     */
    protected final static Map<UUID, Integer> bidding = new HashMap();
    
    /**
     * Record the UID of each player's last selected auction item.
     */
    protected final static Map<UUID, Long> biddingID = new HashMap();
    
    /**
     * Keep track of the categories each player is using. (Shop type)
     */
    protected final static Map<UUID, ShopType> shopType = new HashMap(); 
    
    /**
     * Keep track of the categories each player is using. (Item categories)
     */
    protected final static Map<UUID, Category> shopCategory = new HashMap();
    
    /**
     * "List< Long >": UID of this item in the global market.
     */
    protected final static Map<UUID, List<Long>> itemUID = new HashMap();
    
    /**
     * "List< Long >": UID of this item in the item mail.
     */
    protected final static Map<UUID, List<Long>> mailUID = new HashMap(); 
    
    /**
     * Unknown... from old CrazyAuctions plug-in.
     */
    protected final static Map<UUID, Long> IDs = new HashMap();
    
    /**
     * Record the owner of the mailbox opened by the player. 
     * 
     * @since 1.1.4-SNAPSHOT-2
     * It may be removed in a future version. (Custom GUI may be supported in the future)
     */
    public final static Map<UUID, UUID> openingMail = new HashMap();
    
    /**
     * Record the type of GUI window opened by the player.
     */
    public final static Map<UUID, GUIType> openingGUI = new HashMap();
    
    public static void openShop(Player player, ShopType type, Category cat, int page) {
        if (FileManager.isBackingUp() || FileManager.isRollingBack() || PluginControl.isWorldDisabled(player)) {
            player.closeInventory();
            return;
        }
        PluginControl.updateCacheData();
        FileManager.ProtectedConfiguration config = FileManager.Files.CONFIG.getFile();
        List<ItemStack> items = new ArrayList();
        List<Long> ID = new ArrayList();
        GlobalMarket market = GlobalMarket.getMarket();
        if (cat != null) {
            shopCategory.put(player.getUniqueId(), cat);
        } else {
            shopCategory.put(player.getUniqueId(), Category.getDefaultCategory());
        }
        for (MarketGoods mg : market.getItems()) {
            List<String> lore = new ArrayList();
            if ((cat.isWhitelist() ? cat.getAllItemMeta().contains(mg.getItem().getItemMeta()) : !cat.getAllItemMeta().contains(mg.getItem().getItemMeta())) || cat.getItems().contains(mg.getItem().getType()) || cat.equals(Category.getDefaultCategory())) {
                switch (type) {
                    case BID: {
                        if (mg.getShopType().equals(ShopType.BID)) {
                            String owner = mg.getItemOwner().getName();
                            String topbidder = mg.getTopBidder().split(":")[0];
                            MessageUtil.getValueList("BiddingItemLore").stream().forEach(l -> {
                                lore.add(l.replace("%topbid%", String.valueOf(mg.getPrice())).replace("%owner%", owner).replace("%addedtime%", new SimpleDateFormat(MessageUtil.getValue("Date-Format")).format(new Date(mg.getAddedTime()))).replace("%topbidder%", topbidder).replace("%time%", PluginControl.convertToTime(mg.getTimeTillExpire(), false)));
                            });
                            if (mg.getItem() == null) continue;
                            items.add(PluginControl.addLore(mg.getItem().clone(), lore));
                            ID.add(mg.getUID());
                        }
                        break;
                    }
                    case BUY: {
                        if (mg.getShopType().equals(ShopType.BUY)) {
                            MessageUtil.getValueList("BuyingItemLore").stream().forEach(l -> {
                                String reward = String.valueOf(mg.getReward());
                                String owner = mg.getItemOwner().getName();
                                lore.add(l.replace("%reward%", reward)
                                        .replace("%owner%", owner)
                                        .replace("%addedtime%", new SimpleDateFormat(MessageUtil.getValue("Date-Format")).format(new Date(mg.getAddedTime())))
                                        .replace("%time%", PluginControl.convertToTime(mg.getTimeTillExpire(), false)));
                            });
                            items.add(PluginControl.addLore(mg.getItem().clone(), lore));
                            ID.add(mg.getUID());
                        }
                        break;
                    }
                    case SELL: {
                        if (mg.getShopType().equals(ShopType.SELL)) {
                            MessageUtil.getValueList("SellingItemLore").stream().forEach(l -> {
                                lore.add(l.replace("%price%", String.valueOf(mg.getPrice())).replace("%Owner%", mg.getItemOwner().getName()).replace("%owner%", mg.getItemOwner().getName()).replace("%addedtime%", new SimpleDateFormat(MessageUtil.getValue("Date-Format")).format(new Date(mg.getAddedTime()))).replace("%time%", PluginControl.convertToTime(mg.getTimeTillExpire(), false)));
                            });
                            if (mg.getItem() == null) continue;
                            items.add(PluginControl.addLore(mg.getItem().clone(), lore));
                            ID.add(mg.getUID());
                        }
                        break;
                    }
                    case ANY: {
                        switch (mg.getShopType()) {
                            case BID: {
                                String owner = mg.getItemOwner().getName();
                                String topbidder = mg.getTopBidder().split(":")[0];
                                MessageUtil.getValueList("BiddingItemLore").stream().forEach(l -> {
                                    lore.add(l.replace("%topbid%", String.valueOf(mg.getPrice()))
                                            .replace("%addedtime%", new SimpleDateFormat(MessageUtil.getValue("Date-Format")).format(new Date(mg.getAddedTime())))
                                            .replace("%owner%", owner).replace("%topbidder%", topbidder).replace("%time%", PluginControl.convertToTime(mg.getTimeTillExpire(), false)));
                                });
                                if (mg.getItem() == null) continue;
                                items.add(PluginControl.addLore(mg.getItem().clone(), lore));
                                ID.add(mg.getUID());
                                break;
                            }
                            case BUY: {
                                MessageUtil.getValueList("BuyingItemLore").stream().forEach(l -> {
                                    String reward = String.valueOf(mg.getReward());
                                    String owner = mg.getItemOwner().getName();
                                    lore.add(l.replace("%reward%", reward)
                                            .replace("%Owner%", owner) .replace("%owner%", owner)
                                            .replace("%addedtime%", new SimpleDateFormat(MessageUtil.getValue("Date-Format")).format(new Date(mg.getAddedTime())))
                                            .replace("%time%", PluginControl.convertToTime(mg.getTimeTillExpire(), false)));
                                });
                                items.add(PluginControl.addLore(mg.getItem().clone(), lore));
                                ID.add(mg.getUID());
                                break;
                            }
                            case SELL: {
                                MessageUtil.getValueList("SellingItemLore").stream().forEach(l -> {
                                    lore.add(l.replace("%price%", String.valueOf(mg.getPrice())).replace("%addedtime%", new SimpleDateFormat(MessageUtil.getValue("Date-Format")).format(new Date(mg.getAddedTime()))).replace("%owner%", mg.getItemOwner().getName()).replace("%time%", PluginControl.convertToTime(mg.getTimeTillExpire(), false)));
                                });
                                if (mg.getItem() == null) continue;
                                items.add(PluginControl.addLore(mg.getItem().clone(), lore));
                                ID.add(mg.getUID());
                                break;
                            }
                        }
                    }
                }
            }
        }
        int maxPage = PluginControl.getMaxPage(items);
        for (; page > maxPage; page--) {}
        Inventory inv;
        GUIType guiType;
        if (type == null) {
            type = ShopType.ANY;
        }
        switch (type) {
            case ANY: {
                inv = Bukkit.createInventory(null, 54, PluginControl.color(config.getString("Settings.Main-GUIName") + " #" + page));
                guiType = GUIType.GLOBALMARKET_MAIN;
                break;
            }
            case SELL: {
                inv = Bukkit.createInventory(null, 54, PluginControl.color(config.getString("Settings.Sell-GUIName") + " #" + page));
                guiType = GUIType.GLOBALMARKET_SELL;
                break;
            }
            case BUY: {
                inv = Bukkit.createInventory(null, 54, PluginControl.color(config.getString("Settings.Buy-GUIName") + " #" + page));
                guiType = GUIType.GLOBALMARKET_BUY;
                break;
            }
            case BID: {
                inv = Bukkit.createInventory(null, 54, PluginControl.color(config.getString("Settings.Bid-GUIName") + " #" + page));
                guiType = GUIType.GLOBALMARKET_BID;
                break;
            }
            default: {
                throw new NullPointerException("ShopType is null");
            }
        }
        List<String> options = new ArrayList();
        options.add("Commoditys");
        options.add("Items-Mail");
        options.add("PreviousPage");
        options.add("Refesh");
        options.add("NextPage");
        options.add("Category");
        options.add("Custom");
        switch (type) {
            case SELL: {
                shopType.put(player.getUniqueId(), ShopType.SELL);
                if (CrazyAuctions.getInstance().isSellingEnabled()) {
                    options.add("Shopping.Selling");
                }
                options.add("WhatIsThis.SellingShop");
                break;
            }
            case BID: {
                shopType.put(player.getUniqueId(), ShopType.BID);
                if (CrazyAuctions.getInstance().isBiddingEnabled()) {
                    options.add("Shopping.Bidding");
                }
                options.add("WhatIsThis.BiddingShop");
                break;
            }
            case BUY: {
                shopType.put(player.getUniqueId(), ShopType.BUY);
                if (CrazyAuctions.getInstance().isBuyingEnabled()) {
                    options.add("Shopping.Buying");
                }
                options.add("WhatIsThis.BuyingShop");
                break;
            }
            case ANY: {
                shopType.put(player.getUniqueId(), ShopType.ANY);
                options.add("Shopping.Others");
                options.add("WhatIsThis.MainShop");
                break;
            }
        }
        for (String o : options) {
            if (config.contains("Settings.GUISettings.OtherSettings." + o + ".Toggle")) {
                if (!config.getBoolean("Settings.GUISettings.OtherSettings." + o + ".Toggle")) {
                    continue;
                }
            }
            String id = config.getString("Settings.GUISettings.OtherSettings." + o + ".Item");
            String name = config.getString("Settings.GUISettings.OtherSettings." + o + ".Name");
            List<String> lore = new ArrayList();
            int slot = config.getInt("Settings.GUISettings.OtherSettings." + o + ".Slot");
            if (config.contains("Settings.GUISettings.OtherSettings." + o + ".Lore")) {
                for (String l : config.getStringList("Settings.GUISettings.OtherSettings." + o + ".Lore")) {
                    lore.add(l.replace("%category%", shopCategory.get(player.getUniqueId()).getDisplayName() != null ? shopCategory.get(player.getUniqueId()).getDisplayName() : shopCategory.get(player.getUniqueId()).getName()));
                }
                inv.setItem(slot - 1, PluginControl.makeItem(id, 1, name, lore));
            } else {
                inv.setItem(slot - 1, PluginControl.makeItem(id, 1, name));
            }
        }
        for (ItemStack item : PluginControl.getPage(items, page)) {
            int slot = inv.firstEmpty();
            inv.setItem(slot, item);
        }
        List<Long> Id = new ArrayList(PluginControl.getMarketPageUIDs(ID, page));
        itemUID.put(player.getUniqueId(), Id);
        player.openInventory(inv);
        GUIAction.openingGUI.put(player.getUniqueId(), guiType);
    }
    
    public static void openCategories(Player player, ShopType shop) {
        if (FileManager.isBackingUp() || FileManager.isRollingBack() || PluginControl.isWorldDisabled(player)) {
            player.closeInventory();
            return;
        }
        PluginControl.updateCacheData();
        ProtectedConfiguration config = FileManager.Files.CONFIG.getFile();
        int size = config.getInt("Settings.GUISettings.Category-Settings.GUI-Size");
        if (size != 54 && size != 45 && size != 36 && size != 27 && size != 18 && size != 9) {
            size = 54;
        }
        Inventory inv = Bukkit.createInventory(null, size, PluginControl.color(config.getString("Settings.Categories")));
        List<String> options = new ArrayList();
        options.add("OtherSettings.Categories-Back");
        options.add("OtherSettings.WhatIsThis.Categories");
        for (String option : config.getConfigurationSection("Settings.GUISettings.Category-Settings.Custom-Category").getKeys(false)) {
            options.add("Category-Settings.Custom-Category." + option);
        }
        options.add("Category-Settings.ShopType-Category.Selling");
        options.add("Category-Settings.ShopType-Category.Buying");
        options.add("Category-Settings.ShopType-Category.Bidding");
        options.add("Category-Settings.ShopType-Category.None");
        for (String o : options) {
            if (config.contains("Settings.GUISettings." + o + ".Toggle")) {
                if (!config.getBoolean("Settings.GUISettings." + o + ".Toggle")) {
                    continue;
                }
            }
            String id = config.getString("Settings.GUISettings." + o + ".Item");
            String name = config.getString("Settings.GUISettings." + o + ".Name");
            int slot = config.getInt("Settings.GUISettings." + o + ".Slot");
            if (config.contains("Settings.GUISettings." + o + ".Lore")) {
                inv.setItem(slot - 1, PluginControl.makeItem(id, 1, name, config.getStringList("Settings.GUISettings." + o + ".Lore")));
            } else {
                inv.setItem(slot - 1, PluginControl.makeItem(id, 1, name));
            }
        }
        shopType.put(player.getUniqueId(), shop);
        player.openInventory(inv);
        GUIAction.openingGUI.put(player.getUniqueId(), GUIType.CATEGORY);
    }
    
    public static void openPlayersCurrentList(Player player, int page) {
        if (FileManager.isBackingUp() || FileManager.isRollingBack() || PluginControl.isWorldDisabled(player)) {
            player.closeInventory();
            return;
        }
        PluginControl.updateCacheData();
        FileManager.ProtectedConfiguration config = FileManager.Files.CONFIG.getFile();
        List<ItemStack> items = new ArrayList();
        List<Long> ID = new ArrayList();
        GlobalMarket market = GlobalMarket.getMarket();
        Inventory inv = Bukkit.createInventory(null, 54, PluginControl.color(config.getString("Settings.Player-Items-List")));
        List<String> options = new ArrayList();
        options.add("Player-Items-List-Back");
        options.add("WhatIsThis.CurrentItems");
        for (String o : options) {
            if (config.contains("Settings.GUISettings.OtherSettings." + o + ".Toggle")) {
                if (!config.getBoolean("Settings.GUISettings.OtherSettings." + o + ".Toggle")) {
                    continue;
                }
            }
            String id = config.getString("Settings.GUISettings.OtherSettings." + o + ".Item");
            String name = config.getString("Settings.GUISettings.OtherSettings." + o + ".Name");
            int slot = config.getInt("Settings.GUISettings.OtherSettings." + o + ".Slot");
            if (config.contains("Settings.GUISettings.OtherSettings." + o + ".Lore")) {
                inv.setItem(slot - 1, PluginControl.makeItem(id, 1, name, config.getStringList("Settings.GUISettings.OtherSettings." + o + ".Lore")));
            } else {
                inv.setItem(slot - 1, PluginControl.makeItem(id, 1, name));
            }
        }
        for (MarketGoods mg : market.getItems()) {
            if (mg.getItemOwner().getUUID().equals(player.getUniqueId())) {
                List<String> lore = new ArrayList();
                if (mg.getShopType().equals(ShopType.BID) || mg.getShopType().equals(ShopType.ANY)) {
                    String owner = mg.getItemOwner().getName();
                    String topbidder = mg.getTopBidder().split(":")[0];
                    for (String l : MessageUtil.getValueList("CurrentBiddingItemLore")) {
                        lore.add(l.replace("%price%", String.valueOf(mg.getPrice())).replace("%topbid%", String.valueOf(mg.getPrice())).replace("%owner%", owner).replace("%topbidder%", topbidder).replace("%addedtime%", new SimpleDateFormat(MessageUtil.getValue("Date-Format")).format(new Date(mg.getAddedTime()))).replace("%time%", PluginControl.convertToTime(mg.getTimeTillExpire(), false)));
                    }
                    if (mg.getItem() == null) continue;
                    items.add(PluginControl.addLore(mg.getItem().clone(), lore));
                    ID.add(mg.getUID());
                }
                if (mg.getShopType().equals(ShopType.BUY) || mg.getShopType().equals(ShopType.ANY)) {
                    for (String l : MessageUtil.getValueList("CurrentBuyingItemLore")) {
                        String reward = String.valueOf(mg.getReward());
                        String owner = mg.getItemOwner().getName();
                        lore.add(l.replace("%reward%", reward)
                                .replace("%Owner%", owner) .replace("%owner%", owner)
                                .replace("%addedtime%", new SimpleDateFormat(MessageUtil.getValue("Date-Format")).format(new Date(mg.getAddedTime())))
                                .replace("%time%", PluginControl.convertToTime(mg.getTimeTillExpire(), false)));
                    }
                    items.add(PluginControl.addLore(mg.getItem().clone(), lore));
                    ID.add(mg.getUID());
                }
                if (mg.getShopType().equals(ShopType.SELL) || mg.getShopType().equals(ShopType.ANY)) {
                    for (String l : MessageUtil.getValueList("CurrentSellingItemLore")) {
                        lore.add(l.replace("%price%", String.valueOf(mg.getPrice())).replace("%Owner%", mg.getItemOwner().getName()).replace("%owner%", mg.getItemOwner().getName()).replace("%addedtime%", new SimpleDateFormat(MessageUtil.getValue("Date-Format")).format(new Date(mg.getAddedTime()))).replace("%time%", PluginControl.convertToTime(mg.getTimeTillExpire(), false)));
                    }
                    if (mg.getItem() == null) continue;
                    items.add(PluginControl.addLore(mg.getItem().clone(), lore));
                    ID.add(mg.getUID());
                }
            }
        }
        for (ItemStack item : PluginControl.getPage(items, page)) {
            int slot = inv.firstEmpty();
            inv.setItem(slot, item);
        }
        List<Long> Id = new ArrayList(PluginControl.getMarketPageUIDs(ID, page));
        itemUID.put(player.getUniqueId(), Id);
        player.openInventory(inv);
        GUIAction.openingGUI.put(player.getUniqueId(), GUIType.ITEM_LIST);
    }
    
    public static void openPlayersMail(Player player, int page) {
        if (FileManager.isBackingUp() || FileManager.isRollingBack() || PluginControl.isWorldDisabled(player)) {
            player.closeInventory();
            return;
        }
        PluginControl.updateCacheData();
        FileManager.ProtectedConfiguration config = FileManager.Files.CONFIG.getFile();
        List<ItemStack> items = new ArrayList();
        List<Long> ID = new ArrayList();
        Storage playerdata = Storage.getPlayer(player);
        if (!playerdata.getMailBox().isEmpty()) {
            for (ItemMail im : playerdata.getMailBox()) {
                if (im.getItem() == null) continue;
                List<String> lore = new ArrayList();
                for (String l : MessageUtil.getValueList("Item-Mail-Lore")) {
                    lore.add(l.replace("%addedtime%", new SimpleDateFormat(MessageUtil.getValue("Date-Format")).format(new Date(im.getAddedTime()))).replace("%time%", PluginControl.convertToTime(im.getFullTime(), im.isNeverExpire())));
                }
                items.add(PluginControl.addLore(im.getItem().clone(), lore));
                ID.add(im.getUID());
            }
        }
        int maxPage = PluginControl.getMaxPage(items);
        for (; page > maxPage; page--) {}
        Inventory inv = Bukkit.createInventory(null, 54, PluginControl.color(config.getString("Settings.Player-Items-Mail") + " #" + page));
        List<String> options = new ArrayList();
        options.add("Player-Items-Mail-Back");
        options.add("PreviousPage");
        options.add("Return");
        options.add("NextPage");
        options.add("WhatIsThis.Items-Mail");
        for (String o : options) {
            if (config.contains("Settings.GUISettings.OtherSettings." + o + ".Toggle")) {
                if (!config.getBoolean("Settings.GUISettings.OtherSettings." + o + ".Toggle")) {
                    continue;
                }
            }
            String id = config.getString("Settings.GUISettings.OtherSettings." + o + ".Item");
            String name = config.getString("Settings.GUISettings.OtherSettings." + o + ".Name");
            int slot = config.getInt("Settings.GUISettings.OtherSettings." + o + ".Slot");
            if (config.contains("Settings.GUISettings.OtherSettings." + o + ".Lore")) {
                inv.setItem(slot - 1, PluginControl.makeItem(id, 1, name, config.getStringList("Settings.GUISettings.OtherSettings." + o + ".Lore")));
            } else {
                inv.setItem(slot - 1, PluginControl.makeItem(id, 1, name));
            }
        }
        for (ItemStack item : PluginControl.getPage(items, page)) {
            int slot = inv.firstEmpty();
            inv.setItem(slot, item);
        }
        List<Long> Id = new ArrayList(PluginControl.getMailPageUIDs(ID, page));
        mailUID.put(player.getUniqueId(), Id);
        player.openInventory(inv);
        GUIAction.openingGUI.put(player.getUniqueId(), GUIType.ITEM_MAIL);
        GUIAction.openingMail.put(player.getUniqueId(), player.getUniqueId());
    }
    
    public static void openPlayersMail(Player player, int page, UUID uuid) {
        if (FileManager.isBackingUp() || FileManager.isRollingBack() || PluginControl.isWorldDisabled(player)) {
            player.closeInventory();
            return;
        }
        PluginControl.updateCacheData();
        FileManager.ProtectedConfiguration config = FileManager.Files.CONFIG.getFile();
        List<ItemStack> items = new ArrayList();
        List<Long> ID = new ArrayList();
        Storage playerdata = Storage.getPlayer(uuid);
        if (!playerdata.getMailBox().isEmpty()) {
            for (ItemMail im : playerdata.getMailBox()) {
                if (im.getItem() == null) continue;
                List<String> lore = new ArrayList();
                for (String l : MessageUtil.getValueList("Item-Mail-Lore")) {
                    lore.add(l.replace("%addedtime%", new SimpleDateFormat(MessageUtil.getValue("Date-Format")).format(new Date(im.getAddedTime()))).replace("%time%", PluginControl.convertToTime(im.getFullTime(), im.isNeverExpire())));
                }
                items.add(PluginControl.addLore(im.getItem().clone(), lore));
                ID.add(im.getUID());
            }
        }
        int maxPage = PluginControl.getMaxPage(items);
        for (; page > maxPage; page--) {}
        Inventory inv = Bukkit.createInventory(null, 54, PluginControl.color(config.getString("Settings.Player-Items-Mail") + " #" + page));
        List<String> options = new ArrayList();
        options.add("Player-Items-Mail-Back");
        options.add("PreviousPage");
        options.add("Return");
        options.add("NextPage");
        options.add("WhatIsThis.Items-Mail");
        for (String o : options) {
            if (config.contains("Settings.GUISettings.OtherSettings." + o + ".Toggle")) {
                if (!config.getBoolean("Settings.GUISettings.OtherSettings." + o + ".Toggle")) {
                    continue;
                }
            }
            String id = config.getString("Settings.GUISettings.OtherSettings." + o + ".Item");
            String name = config.getString("Settings.GUISettings.OtherSettings." + o + ".Name");
            int slot = config.getInt("Settings.GUISettings.OtherSettings." + o + ".Slot");
            if (config.contains("Settings.GUISettings.OtherSettings." + o + ".Lore")) {
                inv.setItem(slot - 1, PluginControl.makeItem(id, 1, name, config.getStringList("Settings.GUISettings.OtherSettings." + o + ".Lore")));
            } else {
                inv.setItem(slot - 1, PluginControl.makeItem(id, 1, name));
            }
        }
        for (ItemStack item : PluginControl.getPage(items, page)) {
            int slot = inv.firstEmpty();
            inv.setItem(slot, item);
        }
        List<Long> Id = new ArrayList(PluginControl.getMailPageUIDs(ID, page));
        mailUID.put(player.getUniqueId(), Id);
        player.openInventory(inv);
        GUIAction.openingGUI.put(player.getUniqueId(), GUIType.ITEM_MAIL);
        GUIAction.openingMail.put(player.getUniqueId(), uuid);
    }
    
    public static void openBuying(Player player, long uid) {
        if (FileManager.isBackingUp() || FileManager.isRollingBack() || PluginControl.isWorldDisabled(player)) {
            player.closeInventory();
            return;
        }
        PluginControl.updateCacheData();
        FileManager.ProtectedConfiguration config = FileManager.Files.CONFIG.getFile();
        GlobalMarket market = GlobalMarket.getMarket();
        if (market.getMarketGoods(uid) == null) {
            openShop(player, ShopType.SELL, shopCategory.get(player.getUniqueId()), 1);
            player.sendMessage(MessageUtil.getValue("Item-Doesnt-Exist"));
            return;
        }
        Inventory inv = Bukkit.createInventory(null, 9, PluginControl.color(config.getString("Settings.Buying-Item")));
        List<String> options = new ArrayList();
        options.add("Confirm");
        options.add("Cancel");
        for (String o : options) {
            String id = config.getString("Settings.GUISettings.OtherSettings." + o + ".Item");
            String name = config.getString("Settings.GUISettings.OtherSettings." + o + ".Name");
            ItemStack item;
            if (config.contains("Settings.GUISettings.OtherSettings." + o + ".Lore")) {
                item = PluginControl.makeItem(id, 1, name, config.getStringList("Settings.GUISettings.OtherSettings." + o + ".Lore"));
            } else {
                item = PluginControl.makeItem(id, 1, name);
            }
            if (o.equals("Confirm")) {
                inv.setItem(0, item);
                inv.setItem(1, item);
                inv.setItem(2, item);
                inv.setItem(3, item);
            }
            if (o.equals("Cancel")) {
                inv.setItem(5, item);
                inv.setItem(6, item);
                inv.setItem(7, item);
                inv.setItem(8, item);
            }
        }
        MarketGoods mg = market.getMarketGoods(uid);
        ItemStack item = market.getMarketGoods(uid).getItem();
        List<String> lore = new ArrayList();
        for (String l : MessageUtil.getValueList("SellingItemLore")) {
            lore.add(l.replace("%price%", String.valueOf(mg.getPrice())).replace("%addedtime%", new SimpleDateFormat(MessageUtil.getValue("Date-Format")).format(new Date(mg.getAddedTime()))).replace("%owner%", mg.getItemOwner().getName()).replace("%time%", PluginControl.convertToTime(mg.getTimeTillExpire(), false)));
        }
        inv.setItem(4, PluginControl.addLore(item.clone(), lore));
        IDs.put(player.getUniqueId(), uid);
        player.openInventory(inv);
        GUIAction.openingGUI.put(player.getUniqueId(), GUIType.BUYING_ITEM);
    }
    
    public static void openSelling(Player player, long uid) {
        if (FileManager.isBackingUp() || FileManager.isRollingBack() || PluginControl.isWorldDisabled(player)) {
            player.closeInventory();
            return;
        }
        PluginControl.updateCacheData();
        FileManager.ProtectedConfiguration config = FileManager.Files.CONFIG.getFile();
        GlobalMarket market = GlobalMarket.getMarket();
        if (market.getMarketGoods(uid) == null) {
            openShop(player, ShopType.BUY, shopCategory.get(player.getUniqueId()), 1);
            player.sendMessage(MessageUtil.getValue("Item-Doesnt-Exist"));
            return;
        }
        Inventory inv = Bukkit.createInventory(null, 9, PluginControl.color(config.getString("Settings.Selling-Item")));
        List<String> options = new ArrayList();
        options.add("Confirm");
        options.add("Cancel");
        for (String o : options) {
            String id = config.getString("Settings.GUISettings.OtherSettings." + o + ".Item");
            String name = config.getString("Settings.GUISettings.OtherSettings." + o + ".Name");
            ItemStack item;
            if (config.contains("Settings.GUISettings.OtherSettings." + o + ".Lore")) {
                item = PluginControl.makeItem(id, 1, name, config.getStringList("Settings.GUISettings.OtherSettings." + o + ".Lore"));
            } else {
                item = PluginControl.makeItem(id, 1, name);
            }
            if (o.equals("Confirm")) {
                inv.setItem(0, item);
                inv.setItem(1, item);
                inv.setItem(2, item);
                inv.setItem(3, item);
            }
            if (o.equals("Cancel")) {
                inv.setItem(5, item);
                inv.setItem(6, item);
                inv.setItem(7, item);
                inv.setItem(8, item);
            }
        }
        MarketGoods mg = market.getMarketGoods(uid);
        ItemStack item = market.getMarketGoods(uid).getItem();
        List<String> lore = new ArrayList();
        for (String l : MessageUtil.getValueList("BuyingItemLore")) {
            String owner = mg.getItemOwner().getName();
            lore.add(l.replace("%reward%", String.valueOf(mg.getReward())).replace("%addedtime%", new SimpleDateFormat(MessageUtil.getValue("Date-Format")).format(new Date(mg.getAddedTime()))).replace("%reward%", String.valueOf(mg.getReward())).replace("%owner%", owner).replace("%time%", PluginControl.convertToTime(mg.getTimeTillExpire(), false)));
        }
        inv.setItem(4, PluginControl.addLore(item.clone(), lore));
        IDs.put(player.getUniqueId(), uid);
        player.openInventory(inv);
        GUIAction.openingGUI.put(player.getUniqueId(), GUIType.SELLING_ITEM);
    }
    
    public static void openBidding(Player player, long uid) {
        if (FileManager.isBackingUp() || FileManager.isRollingBack() || PluginControl.isWorldDisabled(player)) {
            player.closeInventory();
            return;
        }
        PluginControl.updateCacheData();
        FileManager.ProtectedConfiguration config = FileManager.Files.CONFIG.getFile();
        GlobalMarket market = GlobalMarket.getMarket();
        if (market.getMarketGoods(uid) == null) {
            openShop(player, ShopType.BID, shopCategory.get(player.getUniqueId()), 1);
            player.sendMessage(MessageUtil.getValue("Item-Doesnt-Exist"));
            return;
        }
        MarketGoods mg = market.getMarketGoods(uid);
        bidding.put(player.getUniqueId(), (int) mg.getPrice());
        Inventory inv = Bukkit.createInventory(null, 27, PluginControl.color(config.getString("Settings.Bidding-On-Item")));
        if (!bidding.containsKey(player.getUniqueId())) bidding.put(player.getUniqueId(), 0);
        config.getConfig().getConfigurationSection("Settings.GUISettings.Auction-Settings.Bidding-Buttons").getKeys(false).stream().forEach(price -> {
            inv.setItem(config.getConfig().getInt("Settings.GUISettings.Auction-Settings.Bidding-Buttons." + price + ".Slot"), PluginControl.makeItem(config.getConfig().getString("Settings.GUISettings.Auction-Settings.Bidding-Buttons." + price + ".Item"), 1, config.getConfig().getString("Settings.GUISettings.Auction-Settings.Bidding-Buttons." + price + ".Name")));
        });
        inv.setItem(13, getBiddingGlass(player, uid));
        inv.setItem(22, PluginControl.makeItem(config.getString("Settings.GUISettings.Auction-Settings.Bid.Item"), 1, config.getString("Settings.GUISettings.Auction-Settings.Bid.Name"), config.getStringList("Settings.GUISettings.Auction-Settings.Bid.Lore")));
        
        inv.setItem(4, getBiddingItem(player, uid));
        player.openInventory(inv);

        GUIAction.openingGUI.put(player.getUniqueId(), GUIType.BIDDING_ITEM);
    }
    
    public static void openViewer(Player player, UUID uuid, int page) {
        if (FileManager.isBackingUp() || FileManager.isRollingBack() || PluginControl.isWorldDisabled(player)) {
            player.closeInventory();
            return;
        }
        PluginControl.updateCacheData();
        FileManager.ProtectedConfiguration config = FileManager.Files.CONFIG.getFile();
        GlobalMarket market = GlobalMarket.getMarket();
        List<ItemStack> items = new ArrayList();
        List<Long> ID = new ArrayList();
        for (MarketGoods mg : market.getItems()) {
            if (mg.getItemOwner().getUUID().equals(uuid)) {
                List<String> lore = new ArrayList();
                if (mg.getShopType().equals(ShopType.BID) || mg.getShopType().equals(ShopType.ANY)) {
                    String owner = mg.getItemOwner().getName();
                    String topbidder = mg.getTopBidder().split(":")[0];
                    MessageUtil.getValueList("BiddingItemLore").stream().forEach(l -> {
                        lore.add(l.replace("%topbid%", String.valueOf(mg.getPrice())).replace("%owner%", owner).replace("%addedtime%", new SimpleDateFormat(MessageUtil.getValue("Date-Format")).format(new Date(mg.getAddedTime()))).replace("%topbidder%", topbidder).replace("%time%", PluginControl.convertToTime(mg.getTimeTillExpire(), false)));
                    });
                    if (mg.getItem() == null) continue;
                    items.add(PluginControl.addLore(mg.getItem().clone(), lore));
                    ID.add(mg.getUID());
                }
                if (mg.getShopType().equals(ShopType.BUY) || mg.getShopType().equals(ShopType.ANY)) {
                    MessageUtil.getValueList("BuyingItemLore").stream().forEach(l -> {
                        String reward = String.valueOf(mg.getReward());
                        String owner = mg.getItemOwner().getName();
                        lore.add(l.replace("%reward%", reward)
                                .replace("%owner%", owner)
                                .replace("%addedtime%", new SimpleDateFormat(MessageUtil.getValue("Date-Format")).format(new Date(mg.getAddedTime())))
                                .replace("%time%", PluginControl.convertToTime(mg.getTimeTillExpire(), false)));
                    });
                    items.add(PluginControl.addLore(mg.getItem().clone(), lore));
                    ID.add(mg.getUID());
                }
                if (mg.getShopType().equals(ShopType.SELL) || mg.getShopType().equals(ShopType.ANY)) {
                    MessageUtil.getValueList("SellingItemLore").stream().forEach(l -> {
                        lore.add(l.replace("%price%", String.valueOf(mg.getPrice())).replace("%owner%", mg.getItemOwner().getName()).replace("%addedtime%", new SimpleDateFormat(MessageUtil.getValue("Date-Format")).format(new Date(mg.getAddedTime()))).replace("%time%", PluginControl.convertToTime(mg.getTimeTillExpire(), false)));
                    });
                    if (mg.getItem() == null) continue;
                    items.add(PluginControl.addLore(mg.getItem().clone(), lore));
                    ID.add(mg.getUID());
                }
            }
        }
        int maxPage = PluginControl.getMaxPage(items);
        for (; page > maxPage; page--) {}
        Inventory inv = Bukkit.createInventory(null, 54, PluginControl.color(config.getString("Settings.Player-Viewer-GUIName") + " #" + page));
        List<String> options = new ArrayList();
        options.add("WhatIsThis.Viewing");
        for (String o : options) {
            if (config.contains("Settings.GUISettings.OtherSettings." + o + ".Toggle")) {
                if (!config.getBoolean("Settings.GUISettings.OtherSettings." + o + ".Toggle")) {
                    continue;
                }
            }
            String id = config.getString("Settings.GUISettings.OtherSettings." + o + ".Item");
            String name = config.getString("Settings.GUISettings.OtherSettings." + o + ".Name");
            int slot = config.getInt("Settings.GUISettings.OtherSettings." + o + ".Slot");
            if (config.contains("Settings.GUISettings.OtherSettings." + o + ".Lore")) {
                inv.setItem(slot - 1, PluginControl.makeItem(id, 1, name, config.getStringList("Settings.GUISettings.OtherSettings." + o + ".Lore")));
            } else {
                inv.setItem(slot - 1, PluginControl.makeItem(id, 1, name));
            }
        }
        for (ItemStack item : PluginControl.getPage(items, page)) {
            int slot = inv.firstEmpty();
            inv.setItem(slot, item);
        }
        itemUID.put(player.getUniqueId(), new ArrayList(PluginControl.getMarketPageUIDs(ID, page)));
        player.openInventory(inv);
        GUIAction.openingGUI.put(player.getUniqueId(), GUIType.ITEM_VIEWER);
    }
    
    public static ItemStack getBiddingGlass(Player player, long uid) {
        FileManager.ProtectedConfiguration config = FileManager.Files.CONFIG.getFile();
        String id = config.getString("Settings.GUISettings.Auction-Settings.Bidding.Item");
        String name = config.getString("Settings.GUISettings.Auction-Settings.Bidding.Name");
        MarketGoods mg = GlobalMarket.getMarket().getMarketGoods(uid);
        ItemStack item;
        int bid = bidding.get(player.getUniqueId());
        if (config.contains("Settings.GUISettings.Auction-Settings.Bidding.Lore")) {
            List<String> lore = new ArrayList();
            config.getStringList("Settings.GUISettings.Auction-Settings.Bidding.Lore").stream().forEach(l -> {
                lore.add(l.replace("%bid%", String.valueOf(bid)).replace("%topbid%", String.valueOf(mg.getPrice())));
            });
            item = PluginControl.makeItem(id, 1, name, lore);
        } else {
            item = PluginControl.makeItem(id, 1, name);
        }
        return item;
    }
    
    public static ItemStack getBiddingItem(Player player, long uid) {
        GlobalMarket market = GlobalMarket.getMarket();
        MarketGoods mg = market.getMarketGoods(uid);
        String owner = mg.getItemOwner().getName(); 
        String topbidder = mg.getTopBidder().split(":")[0];
        ItemStack item = mg.getItem();
        List<String> lore = new ArrayList();
        String price = String.valueOf(mg.getPrice());
        String time = PluginControl.convertToTime(mg.getTimeTillExpire(), false);
        for (String l : MessageUtil.getValueList("BiddingItemLore")) {
            lore.add(l.replace("%topbid%", price).replace("%owner%", owner).replace("%topbidder%", topbidder).replace("%addedtime%", new SimpleDateFormat(MessageUtil.getValue("Date-Format")).format(new Date(mg.getAddedTime()))).replace("%time%", time));
        }
        return PluginControl.addLore(item.clone(), lore);
    }
    
    protected static void playClick(Player player) {
        if (FileManager.Files.CONFIG.getFile().contains("Settings.Sounds.Toggle")) {
            if (FileManager.Files.CONFIG.getFile().getBoolean("Settings.Sounds.Toggle")) {
                String sound = FileManager.Files.CONFIG.getFile().getString("Settings.Sounds.Sound");
                try {
                    player.playSound(player.getLocation(), Sound.valueOf(sound), 1, 1);
                } catch (Exception e) {
                    PluginControl.printStackTrace(e);
                }
            }
        } else {
            if (PluginControl.getVersion() >= 191) {
                player.playSound(player.getLocation(), Sound.valueOf("UI_BUTTON_CLICK"), 1, 1);
            } else {
                player.playSound(player.getLocation(), Sound.valueOf("CLICK"), 1, 1);
            }
        }
    }
    
    public static void setCategory(Player player, Category cat) {
        shopCategory.put(player.getUniqueId(), cat);
    }
    
    public static void setShopType(Player player, ShopType type) {
        shopType.put(player.getUniqueId(), type);
    }
    
    public static GUIType getOpeningGUI(Player player) {
        if (!openingGUI.containsKey(player.getUniqueId())) {
            return null;
        }
        return openingGUI.get(player.getUniqueId());
    }
    
    public static enum GUIType {
        
        /**
         * Global Market: Main GUI.
         */
        GLOBALMARKET_MAIN,
        
        /**
         * Global Market: Item Sales GUI.
         */
        GLOBALMARKET_SELL,
        
        /**
         * Global Market: Item Acquisition GUI.
         */
        GLOBALMARKET_BUY,
        
        /**
         * Global Market: Item Auction GUI.
         */
        GLOBALMARKET_BID,
        
        /**
         * Own Item List GUI.
         */
        ITEM_LIST,
        
        /**
         * Single player product GUI.
         */
        ITEM_VIEWER,
        
        /**
         * Item mail GUI.
         */
        ITEM_MAIL,
        
        /**
         * Category GUI.
         */
        CATEGORY,
        
        /**
         * Buying GUI for an item.
         */
        SELLING_ITEM,
        
        /**
         * GUI for selling an item.
         */
        BUYING_ITEM,
        
        /**
         * Auction GUI for an item
         */
        BIDDING_ITEM
    }
}
