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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import org.jetbrains.annotations.NotNull;
import studio.trc.bukkit.crazyauctionsplus.database.GlobalMarket;
import studio.trc.bukkit.crazyauctionsplus.database.Storage;
import studio.trc.bukkit.crazyauctionsplus.event.GUIAction;
import studio.trc.bukkit.crazyauctionsplus.util.FileManager.ProtectedConfiguration;
import studio.trc.bukkit.crazyauctionsplus.util.enums.ShopType;

import static studio.trc.bukkit.crazyauctionsplus.util.MenuIcon.icon;

public class GUI
{
    /**
     * Unknown... from old CrazyAuctions plug-in.
     */
    protected final static Map<UUID, Integer> bidding = new HashMap<>();
    
    /**
     * Record the UID of each player's last selected auction item.
     */
    protected final static Map<UUID, Long> biddingID = new HashMap<>();
    
    /**
     * Keep track of the categories each player is using. (Shop type)
     */
    protected final static Map<UUID, ShopType> shopType = new HashMap<>();
    
    /**
     * Keep track of the categories each player is using. (Item categories)
     */
    protected final static Map<UUID, Category> shopCategory = new HashMap<>();
    
    /**
     * "List< Long >": UID of this item in the global market.
     */
    protected final static Map<UUID, Map<Integer, MenuIcon>> itemUID = new HashMap<>();
    
    /**
     * "List< Long >": UID of this item in the item mail.
     */
    protected final static Map<UUID, Map<Integer, MenuIcon>> mailUID = new HashMap<>();
    
    /**
     * Unknown... from old CrazyAuctions plug-in.
     */
    protected final static Map<UUID, Long> IDs = new HashMap<>();
    
    /**
     * Record the owner of the mailbox opened by the player. 
     * 
     * @since 1.1.4-SNAPSHOT-2
     * It may be removed in a future version. (Custom GUI may be supported in the future)
     */
    public final static Map<UUID, UUID> openingMail = new HashMap<>();
    
    /**
     * Record the type of GUI window opened by the player.
     */
    public final static Map<UUID, GUIType> openingGUI = new HashMap<>();

    public static List<Integer> getInvIndexes(ProtectedConfiguration section, String key) {
        List<Integer> list = new ArrayList<>();
        char[] array = String.join("", section.getStringList(key)).toCharArray();
        for (int i = 0; i < array.length; i++) {
            if (array[i] == '#') {
                list.add(i);
            }
        }
        return list;
    }

    private static Map<Integer, MenuIcon> getPage(Inventory inv, List<MenuIcon> items, List<Integer> indexes, int page) {
        Map<Integer, MenuIcon> ids = new HashMap<>();
        List<MenuIcon> iconList = PluginControl.getPage(items, page, indexes.size());
        int pageLength = Math.min(indexes.size(), iconList.size());
        for (int i = 0; i < pageLength; i++) {
            int slot = indexes.get(i);
            MenuIcon icon = iconList.get(i);
            inv.setItem(slot, icon.item);
            ids.put(slot, icon);
        }
        return ids;
    }

    public static void openShop(Player player, ShopType type, Category cat, int page) {
        if (FileManager.isBackingUp() || FileManager.isRollingBack() || PluginControl.isWorldDisabled(player)) {
            player.closeInventory();
            return;
        }
        PluginControl.updateCacheData();
        FileManager.ProtectedConfiguration config = FileManager.Files.CONFIG.getFile();
        List<MenuIcon> items = new ArrayList<>();
        GlobalMarket market = GlobalMarket.getMarket();
        if (cat != null) {
            shopCategory.put(player.getUniqueId(), cat);
        } else {
            shopCategory.put(player.getUniqueId(), Category.getDefaultCategory());
        }
        for (MarketGoods mg : market.getItems()) {
            List<String> lore = new ArrayList<>();
            if ((cat.isWhitelist() == cat.getAllItemMeta().contains(mg.getItem().getItemMeta()))
                    || cat.getItems().contains(mg.getItem().getType())
                    || cat.equals(Category.getDefaultCategory())) {
                switch (type) {
                    case BID: {
                        if (mg.getShopType().equals(ShopType.BID)) {
                            String owner = mg.getItemOwner().getName();
                            String topBidder = mg.getTopBidder().split(":")[0];
                            for (String l : MessageUtil.getValueList("BiddingItemLore")) {
                                String price = String.valueOf(mg.getPrice());
                                String addedTime = new SimpleDateFormat(MessageUtil.getValue("Date-Format")).format(new Date(mg.getAddedTime()));
                                String time = PluginControl.convertToTime(mg.getTimeTillExpire(), false);
                                lore.add(l.replace("%topbid%", price)
                                        .replace("%owner%", owner)
                                        .replace("%addedtime%", addedTime)
                                        .replace("%topbidder%", topBidder)
                                        .replace("%time%", time));
                            }

                            if (mg.getItem() == null) continue;
                            items.add(icon(mg.getUID(), PluginControl.addLore(mg.getItem().clone(), lore)));
                        }
                        break;
                    }
                    case BUY: {
                        if (mg.getShopType().equals(ShopType.BUY)) {
                            for (String l : MessageUtil.getValueList("BuyingItemLore")) {
                                String reward = String.valueOf(mg.getReward());
                                String owner = mg.getItemOwner().getName();
                                String addedTime = new SimpleDateFormat(MessageUtil.getValue("Date-Format")).format(new Date(mg.getAddedTime()));
                                String time = PluginControl.convertToTime(mg.getTimeTillExpire(), false);
                                lore.add(l.replace("%reward%", reward)
                                        .replace("%owner%", owner)
                                        .replace("%addedtime%", addedTime)
                                        .replace("%time%", time));
                            }
                            items.add(icon(mg.getUID(), PluginControl.addLore(mg.getItem().clone(), lore)));
                        }
                        break;
                    }
                    case SELL: {
                        if (mg.getShopType().equals(ShopType.SELL)) {
                            for (String l : MessageUtil.getValueList("SellingItemLore")) {
                                String price = String.valueOf(mg.getPrice());
                                String owner = mg.getItemOwner().getName();
                                String addedTime = new SimpleDateFormat(MessageUtil.getValue("Date-Format")).format(new Date(mg.getAddedTime()));
                                String time = PluginControl.convertToTime(mg.getTimeTillExpire(), false);
                                lore.add(l.replace("%price%", price)
                                         .replace("%Owner%", owner)
                                         .replace("%owner%", owner)
                                         .replace("%addedtime%", addedTime)
                                         .replace("%time%", time));
                            }
                            if (mg.getItem() == null) continue;
                            items.add(icon(mg.getUID(), PluginControl.addLore(mg.getItem().clone(), lore)));
                        }
                        break;
                    }
                    case ANY: {
                        switch (mg.getShopType()) {
                            case BID: {
                                String owner = mg.getItemOwner().getName();
                                String topbidder = mg.getTopBidder().split(":")[0];
                                for (String l : MessageUtil.getValueList("BiddingItemLore")) {
                                    String price = String.valueOf(mg.getPrice());
                                    String addedTime = new SimpleDateFormat(MessageUtil.getValue("Date-Format")).format(new Date(mg.getAddedTime()));
                                    String time = PluginControl.convertToTime(mg.getTimeTillExpire(), false);
                                    lore.add(l.replace("%topbid%", price)
                                            .replace("%addedtime%", addedTime)
                                            .replace("%owner%", owner)
                                            .replace("%topbidder%", topbidder)
                                            .replace("%time%", time));
                                }
                                if (mg.getItem() == null) continue;
                                items.add(icon(mg.getUID(), PluginControl.addLore(mg.getItem().clone(), lore)));
                                break;
                            }
                            case BUY: {
                                for (String l : MessageUtil.getValueList("BuyingItemLore")) {
                                    String reward = String.valueOf(mg.getReward());
                                    String owner = mg.getItemOwner().getName();
                                    String addedTime = new SimpleDateFormat(MessageUtil.getValue("Date-Format")).format(new Date(mg.getAddedTime()));
                                    String time = PluginControl.convertToTime(mg.getTimeTillExpire(), false);
                                    lore.add(l.replace("%reward%", reward)
                                            .replace("%Owner%", owner)
                                            .replace("%owner%", owner)
                                            .replace("%addedtime%", addedTime)
                                            .replace("%time%", time));
                                }
                                items.add(icon(mg.getUID(), PluginControl.addLore(mg.getItem().clone(), lore)));
                                break;
                            }
                            case SELL: {
                                for (String l : MessageUtil.getValueList("SellingItemLore")) {
                                    String price = String.valueOf(mg.getPrice());
                                    String addedTime = new SimpleDateFormat(MessageUtil.getValue("Date-Format")).format(new Date(mg.getAddedTime()));
                                    String owner = mg.getItemOwner().getName();
                                    String time = PluginControl.convertToTime(mg.getTimeTillExpire(), false);
                                    lore.add(l.replace("%price%", price)
                                            .replace("%addedtime%", addedTime)
                                            .replace("%owner%", owner)
                                            .replace("%time%", time));
                                }
                                if (mg.getItem() == null) continue;
                                items.add(icon(mg.getUID(), PluginControl.addLore(mg.getItem().clone(), lore)));
                                break;
                            }
                        }
                    }
                }
            }
        }
        int maxPage = PluginControl.getMaxPage(items);
        while (page > maxPage) page--;
        Inventory inv;
        GUIType guiType;
        if (type == null) {
            type = ShopType.ANY;
        }
        switch (type) {
            case ANY: {
                guiType = GUIType.GLOBALMARKET_MAIN;
                inv = holder(guiType).with(54, PluginControl.color(player, config.getString("Settings.Main-GUIName") + " #" + page));
                break;
            }
            case SELL: {
                guiType = GUIType.GLOBALMARKET_SELL;
                inv = holder(guiType).with(54, PluginControl.color(player, config.getString("Settings.Sell-GUIName") + " #" + page));
                break;
            }
            case BUY: {
                guiType = GUIType.GLOBALMARKET_BUY;
                inv = holder(guiType).with(54, PluginControl.color(player, config.getString("Settings.Buy-GUIName") + " #" + page));
                break;
            }
            case BID: {
                guiType = GUIType.GLOBALMARKET_BID;
                inv = holder(guiType).with(54, PluginControl.color(player, config.getString("Settings.Bid-GUIName") + " #" + page));
                break;
            }
            default: {
                throw new NullPointerException("ShopType is null");
            }
        }
        List<String> options = new ArrayList<>();
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
        // 添加选项按钮
        for (String o : options) {
            if (config.contains("Settings.GUISettings.OtherSettings." + o + ".Toggle")) {
                if (!config.getBoolean("Settings.GUISettings.OtherSettings." + o + ".Toggle")) {
                    continue;
                }
            }
            String id = config.getString("Settings.GUISettings.OtherSettings." + o + ".Item");
            String name = config.getString("Settings.GUISettings.OtherSettings." + o + ".Name");
            String customModelKey = "Settings.GUISettings.OtherSettings." + o + ".CustomModelData";
            Integer customModel = config.contains(customModelKey) ? config.getInt(customModelKey) : null;
            List<String> lore = new ArrayList<>();
            int slot = config.getInt("Settings.GUISettings.OtherSettings." + o + ".Slot");
            if (config.contains("Settings.GUISettings.OtherSettings." + o + ".Lore")) {
                for (String l : config.getStringList("Settings.GUISettings.OtherSettings." + o + ".Lore")) {
                    Category category = shopCategory.get(player.getUniqueId());
                    lore.add(l.replace("%category%", category.getDisplayName() != null
                            ? category.getDisplayName()
                            : category.getName()));
                }
            }
            ItemStack item = lore.isEmpty()
                    ? PluginControl.makeItem(id, 1, name)
                    : PluginControl.makeItem(id, 1, name, lore);
            if (customModel != null) {
                AdventureItemStack.setCustomModelData(item, customModel);
            }
            inv.setItem(slot - 1, item);
        }
        List<Integer> indexes = getInvIndexes(config, "Settings.GUISettings.OtherSettings.Content-Slots");
        itemUID.put(player.getUniqueId(), getPage(inv, items, indexes, page));
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
        GUIType guiType = GUIType.CATEGORY;
        Inventory inv = holder(guiType).with(size, PluginControl.color(player, config.getString("Settings.Categories")));
        List<String> options = new ArrayList<>();
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
            String customModelKey = "Settings.GUISettings." + o + ".CustomModelData";
            Integer customModel = config.contains(customModelKey) ? config.getInt(customModelKey) : null;
            String name = config.getString("Settings.GUISettings." + o + ".Name");
            int slot = config.getInt("Settings.GUISettings." + o + ".Slot");
            List<String> lore;
            if (config.contains("Settings.GUISettings." + o + ".Lore")) {
                lore = config.getStringList("Settings.GUISettings." + o + ".Lore");
            } else {
                lore = null;
            }
            ItemStack item = lore == null
                    ? PluginControl.makeItem(id, 1, name)
                    : PluginControl.makeItem(id, 1, name, lore);
            if (customModel != null) {
                AdventureItemStack.setCustomModelData(item, customModel);
            }
            inv.setItem(slot - 1, item);
        }
        shopType.put(player.getUniqueId(), shop);
        player.openInventory(inv);
        GUIAction.openingGUI.put(player.getUniqueId(), guiType);
    }
    
    public static void openPlayersCurrentList(Player player, int page) {
        if (FileManager.isBackingUp() || FileManager.isRollingBack() || PluginControl.isWorldDisabled(player)) {
            player.closeInventory();
            return;
        }
        PluginControl.updateCacheData();
        FileManager.ProtectedConfiguration config = FileManager.Files.CONFIG.getFile();
        List<MenuIcon> items = new ArrayList<>();
        GlobalMarket market = GlobalMarket.getMarket();
        GUIType guiType = GUIType.ITEM_LIST;
        Inventory inv = holder(guiType).with(54, PluginControl.color(player, config.getString("Settings.Player-Items-List")));
        List<String> options = new ArrayList<>();
        options.add("Player-Items-List-Back");
        options.add("WhatIsThis.CurrentItems");
        for (String o : options) {
            if (config.contains("Settings.GUISettings.OtherSettings." + o + ".Toggle")) {
                if (!config.getBoolean("Settings.GUISettings.OtherSettings." + o + ".Toggle")) {
                    continue;
                }
            }
            String id = config.getString("Settings.GUISettings.OtherSettings." + o + ".Item");
            String customModelKey = "Settings.GUISettings.OtherSettings." + o + ".CustomModelData";
            Integer customModel = config.contains(customModelKey) ? config.getInt(customModelKey) : null;
            String name = config.getString("Settings.GUISettings.OtherSettings." + o + ".Name");
            int slot = config.getInt("Settings.GUISettings.OtherSettings." + o + ".Slot");
            List<String> lore;
            if (config.contains("Settings.GUISettings.OtherSettings." + o + ".Lore")) {
                lore = config.getStringList("Settings.GUISettings.OtherSettings." + o + ".Lore");
            } else {
                lore = null;
            }
            ItemStack item = lore == null
                    ? PluginControl.makeItem(id, 1, name)
                    : PluginControl.makeItem(id, 1, name, lore);
            if (customModel != null) {
                AdventureItemStack.setCustomModelData(item, customModel);
            }
            inv.setItem(slot - 1, item);
        }
        for (MarketGoods mg : market.getItems()) {
            if (mg.getItemOwner().getUUID().equals(player.getUniqueId())) {
                List<String> lore = new ArrayList<>();
                if (mg.getShopType().equals(ShopType.BID) || mg.getShopType().equals(ShopType.ANY)) {
                    String owner = mg.getItemOwner().getName();
                    String topbidder = mg.getTopBidder().split(":")[0];
                    for (String l : MessageUtil.getValueList("CurrentBiddingItemLore")) {
                        lore.add(l.replace("%price%", String.valueOf(mg.getPrice())).replace("%topbid%", String.valueOf(mg.getPrice())).replace("%owner%", owner).replace("%topbidder%", topbidder).replace("%addedtime%", new SimpleDateFormat(MessageUtil.getValue("Date-Format")).format(new Date(mg.getAddedTime()))).replace("%time%", PluginControl.convertToTime(mg.getTimeTillExpire(), false)));
                    }
                    if (mg.getItem() == null) continue;
                    items.add(icon(mg.getUID(), PluginControl.addLore(mg.getItem().clone(), lore)));
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
                    items.add(icon(mg.getUID(), PluginControl.addLore(mg.getItem().clone(), lore)));
                }
                if (mg.getShopType().equals(ShopType.SELL) || mg.getShopType().equals(ShopType.ANY)) {
                    for (String l : MessageUtil.getValueList("CurrentSellingItemLore")) {
                        lore.add(l.replace("%price%", String.valueOf(mg.getPrice())).replace("%Owner%", mg.getItemOwner().getName()).replace("%owner%", mg.getItemOwner().getName()).replace("%addedtime%", new SimpleDateFormat(MessageUtil.getValue("Date-Format")).format(new Date(mg.getAddedTime()))).replace("%time%", PluginControl.convertToTime(mg.getTimeTillExpire(), false)));
                    }
                    if (mg.getItem() == null) continue;
                    items.add(icon(mg.getUID(), PluginControl.addLore(mg.getItem().clone(), lore)));
                }
            }
        }
        List<Integer> indexes = getInvIndexes(config, "Settings.GUISettings.OtherSettings.Content-Slots");
        itemUID.put(player.getUniqueId(), getPage(inv, items, indexes, page));
        player.openInventory(inv);
        GUIAction.openingGUI.put(player.getUniqueId(), guiType);
    }
    
    public static void openPlayersMail(Player player, int page) {
        if (FileManager.isBackingUp() || FileManager.isRollingBack() || PluginControl.isWorldDisabled(player)) {
            player.closeInventory();
            return;
        }
        PluginControl.updateCacheData();
        FileManager.ProtectedConfiguration config = FileManager.Files.CONFIG.getFile();
        List<MenuIcon> items = new ArrayList<>();
        Storage playerData = Storage.getPlayer(player);
        if (!playerData.getMailBox().isEmpty()) {
            for (ItemMail im : playerData.getMailBox()) {
                if (im.getItem() == null) continue;
                List<String> lore = new ArrayList<>();
                for (String l : MessageUtil.getValueList("Item-Mail-Lore")) {
                    lore.add(l.replace("%addedtime%", new SimpleDateFormat(MessageUtil.getValue("Date-Format")).format(new Date(im.getAddedTime()))).replace("%time%", PluginControl.convertToTime(im.getFullTime(), im.isNeverExpire())));
                }
                items.add(icon(im.getUID(), PluginControl.addLore(im.getItem().clone(), lore)));
            }
        }
        int maxPage = PluginControl.getMaxPage(items);
        while (page > maxPage) page--;
        GUIType guiType = GUIType.ITEM_MAIL;
        Inventory inv = holder(guiType).with(54, PluginControl.color(player, config.getString("Settings.Player-Items-Mail") + " #" + page));
        List<String> options = new ArrayList<>();
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
            String customModelKey = "Settings.GUISettings.OtherSettings." + o + ".CustomModelData";
            Integer customModel = config.contains(customModelKey) ? config.getInt(customModelKey) : null;
            String name = config.getString("Settings.GUISettings.OtherSettings." + o + ".Name");
            int slot = config.getInt("Settings.GUISettings.OtherSettings." + o + ".Slot");
            List<String> lore;
            if (config.contains("Settings.GUISettings.OtherSettings." + o + ".Lore")) {
                lore = config.getStringList("Settings.GUISettings.OtherSettings." + o + ".Lore");
            } else {
                lore = null;
            }
            ItemStack item = lore == null
                    ? PluginControl.makeItem(id, 1, name)
                    : PluginControl.makeItem(id, 1, name, lore);
            if (customModel != null) {
                AdventureItemStack.setCustomModelData(item, customModel);
            }
            inv.setItem(slot - 1, item);
        }
        List<Integer> indexes = getInvIndexes(config, "Settings.GUISettings.OtherSettings.Mail-Slots");
        mailUID.put(player.getUniqueId(), getPage(inv, items, indexes, page));
        player.openInventory(inv);
        GUIAction.openingGUI.put(player.getUniqueId(), guiType);
        GUIAction.openingMail.put(player.getUniqueId(), player.getUniqueId());
    }
    
    public static void openPlayersMail(Player player, int page, UUID uuid) {
        if (FileManager.isBackingUp() || FileManager.isRollingBack() || PluginControl.isWorldDisabled(player)) {
            player.closeInventory();
            return;
        }
        PluginControl.updateCacheData();
        FileManager.ProtectedConfiguration config = FileManager.Files.CONFIG.getFile();
        List<MenuIcon> items = new ArrayList<>();
        Storage playerData = Storage.getPlayer(uuid);
        if (!playerData.getMailBox().isEmpty()) {
            for (ItemMail im : playerData.getMailBox()) {
                if (im.getItem() == null) continue;
                List<String> lore = new ArrayList<>();
                for (String l : MessageUtil.getValueList("Item-Mail-Lore")) {
                    lore.add(l.replace("%addedtime%", new SimpleDateFormat(MessageUtil.getValue("Date-Format")).format(new Date(im.getAddedTime()))).replace("%time%", PluginControl.convertToTime(im.getFullTime(), im.isNeverExpire())));
                }
                items.add(icon(im.getUID(), PluginControl.addLore(im.getItem().clone(), lore)));
            }
        }
        int maxPage = PluginControl.getMaxPage(items);
        while (page > maxPage) page--;
        GUIType guiType = GUIType.ITEM_MAIL;
        Inventory inv = holder(guiType).with(54, PluginControl.color(player, config.getString("Settings.Player-Items-Mail") + " #" + page));
        List<String> options = new ArrayList<>();
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
            String customModelKey = "Settings.GUISettings.OtherSettings." + o + ".CustomModelData";
            Integer customModel = config.contains(customModelKey) ? config.getInt(customModelKey) : null;
            String name = config.getString("Settings.GUISettings.OtherSettings." + o + ".Name");
            int slot = config.getInt("Settings.GUISettings.OtherSettings." + o + ".Slot");
            List<String> lore;
            if (config.contains("Settings.GUISettings.OtherSettings." + o + ".Lore")) {
                lore = config.getStringList("Settings.GUISettings.OtherSettings." + o + ".Lore");
            } else {
                lore = null;
            }
            ItemStack item = lore == null
                    ? PluginControl.makeItem(id, 1, name)
                    : PluginControl.makeItem(id, 1, name, lore);
            if (customModel != null) {
                AdventureItemStack.setCustomModelData(item, customModel);
            }
            inv.setItem(slot - 1, item);
        }
        List<Integer> indexes = getInvIndexes(config, "Settings.GUISettings.OtherSettings.Mail-Slots");
        mailUID.put(player.getUniqueId(), getPage(inv, items, indexes, page));
        player.openInventory(inv);
        GUIAction.openingGUI.put(player.getUniqueId(), guiType);
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
        GUIType guiType = GUIType.BUYING_ITEM;
        Inventory inv = holder(guiType).with(9, PluginControl.color(player, config.getString("Settings.Buying-Item")));
        List<String> options = new ArrayList<>();
        options.add("Confirm");
        options.add("Cancel");
        for (String o : options) {
            String id = config.getString("Settings.GUISettings.OtherSettings." + o + ".Item");
            String customModelKey = "Settings.GUISettings.OtherSettings." + o + ".CustomModelData";
            Integer customModel = config.contains(customModelKey) ? config.getInt(customModelKey) : null;
            String name = config.getString("Settings.GUISettings.OtherSettings." + o + ".Name");
            List<String> lore;
            if (config.contains("Settings.GUISettings.OtherSettings." + o + ".Lore")) {
                lore = config.getStringList("Settings.GUISettings.OtherSettings." + o + ".Lore");
            } else {
                lore = null;
            }
            ItemStack item = lore == null
                    ? PluginControl.makeItem(id, 1, name)
                    : PluginControl.makeItem(id, 1, name, lore);
            if (customModel != null) {
                AdventureItemStack.setCustomModelData(item, customModel);
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
        List<String> lore = new ArrayList<>();
        for (String l : MessageUtil.getValueList("SellingItemLore")) {
            lore.add(l.replace("%price%", String.valueOf(mg.getPrice())).replace("%addedtime%", new SimpleDateFormat(MessageUtil.getValue("Date-Format")).format(new Date(mg.getAddedTime()))).replace("%owner%", mg.getItemOwner().getName()).replace("%time%", PluginControl.convertToTime(mg.getTimeTillExpire(), false)));
        }
        inv.setItem(4, PluginControl.addLore(item.clone(), lore));
        IDs.put(player.getUniqueId(), uid);
        player.openInventory(inv);
        GUIAction.openingGUI.put(player.getUniqueId(), guiType);
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
        GUIType guiType = GUIType.SELLING_ITEM;
        Inventory inv = holder(guiType).with(9, PluginControl.color(player, config.getString("Settings.Selling-Item")));
        List<String> options = new ArrayList<>();
        options.add("Confirm");
        options.add("Cancel");
        for (String o : options) {
            String id = config.getString("Settings.GUISettings.OtherSettings." + o + ".Item");
            String customModelKey = "Settings.GUISettings.OtherSettings." + o + ".CustomModelData";
            Integer customModel = config.contains(customModelKey) ? config.getInt(customModelKey) : null;
            String name = config.getString("Settings.GUISettings.OtherSettings." + o + ".Name");
            List<String> lore;
            if (config.contains("Settings.GUISettings.OtherSettings." + o + ".Lore")) {
                lore = config.getStringList("Settings.GUISettings.OtherSettings." + o + ".Lore");
            } else {
                lore = null;
            }
            ItemStack item = lore == null
                    ? PluginControl.makeItem(id, 1, name)
                    : PluginControl.makeItem(id, 1, name, lore);
            if (customModel != null) {
                AdventureItemStack.setCustomModelData(item, customModel);
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
        List<String> lore = new ArrayList<>();
        for (String l : MessageUtil.getValueList("BuyingItemLore")) {
            String owner = mg.getItemOwner().getName();
            lore.add(l.replace("%reward%", String.valueOf(mg.getReward())).replace("%addedtime%", new SimpleDateFormat(MessageUtil.getValue("Date-Format")).format(new Date(mg.getAddedTime()))).replace("%reward%", String.valueOf(mg.getReward())).replace("%owner%", owner).replace("%time%", PluginControl.convertToTime(mg.getTimeTillExpire(), false)));
        }
        inv.setItem(4, PluginControl.addLore(item.clone(), lore));
        IDs.put(player.getUniqueId(), uid);
        player.openInventory(inv);
        GUIAction.openingGUI.put(player.getUniqueId(), guiType);
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
        GUIType guiType = GUIType.BIDDING_ITEM;
        Inventory inv = holder(guiType).with(27, PluginControl.color(player, config.getString("Settings.Bidding-On-Item")));
        if (!bidding.containsKey(player.getUniqueId())) bidding.put(player.getUniqueId(), 0);
        ConfigurationSection section = config.getConfig().getConfigurationSection("Settings.GUISettings.Auction-Settings.Bidding-Buttons");
        if (section != null) for (String price : section.getKeys(false)) {
            int slot = config.getConfig().getInt("Settings.GUISettings.Auction-Settings.Bidding-Buttons." + price + ".Slot");
            String item = config.getConfig().getString("Settings.GUISettings.Auction-Settings.Bidding-Buttons." + price + ".Item", "PAPER");
            String name = config.getConfig().getString("Settings.GUISettings.Auction-Settings.Bidding-Buttons." + price + ".Name");
            inv.setItem(slot, PluginControl.makeItem(item, 1, name));
        }
        inv.setItem(13, getBiddingGlass(player, uid));
        inv.setItem(22, PluginControl.makeItem(config.getString("Settings.GUISettings.Auction-Settings.Bid.Item"), 1, config.getString("Settings.GUISettings.Auction-Settings.Bid.Name"), config.getStringList("Settings.GUISettings.Auction-Settings.Bid.Lore")));
        
        inv.setItem(4, getBiddingItem(player, uid));
        player.openInventory(inv);

        GUIAction.openingGUI.put(player.getUniqueId(), guiType);
    }
    
    public static void openViewer(Player player, UUID uuid, int page) {
        if (FileManager.isBackingUp() || FileManager.isRollingBack() || PluginControl.isWorldDisabled(player)) {
            player.closeInventory();
            return;
        }
        PluginControl.updateCacheData();
        FileManager.ProtectedConfiguration config = FileManager.Files.CONFIG.getFile();
        GlobalMarket market = GlobalMarket.getMarket();
        List<MenuIcon> items = new ArrayList<>();
        for (MarketGoods mg : market.getItems()) {
            if (mg.getItemOwner().getUUID().equals(uuid)) {
                List<String> lore = new ArrayList<>();
                if (mg.getShopType().equals(ShopType.BID) || mg.getShopType().equals(ShopType.ANY)) {
                    String owner = mg.getItemOwner().getName();
                    String topbidder = mg.getTopBidder().split(":")[0];
                    for (String l : MessageUtil.getValueList("BiddingItemLore")) {
                        String price = String.valueOf(mg.getPrice());
                        String addedTime = new SimpleDateFormat(MessageUtil.getValue("Date-Format")).format(new Date(mg.getAddedTime()));
                        String time = PluginControl.convertToTime(mg.getTimeTillExpire(), false);
                        lore.add(l.replace("%topbid%", price)
                                .replace("%owner%", owner)
                                .replace("%addedtime%", addedTime)
                                .replace("%topbidder%", topbidder)
                                .replace("%time%", time));
                    }
                    if (mg.getItem() == null) continue;
                    items.add(icon(mg.getUID(), PluginControl.addLore(mg.getItem().clone(), lore)));
                }
                if (mg.getShopType().equals(ShopType.BUY) || mg.getShopType().equals(ShopType.ANY)) {
                    for (String l : MessageUtil.getValueList("BuyingItemLore")) {
                        String reward = String.valueOf(mg.getReward());
                        String owner = mg.getItemOwner().getName();
                        String addedTime = new SimpleDateFormat(MessageUtil.getValue("Date-Format")).format(new Date(mg.getAddedTime()));
                        String time = PluginControl.convertToTime(mg.getTimeTillExpire(), false);
                        lore.add(l.replace("%reward%", reward)
                                .replace("%owner%", owner)
                                .replace("%addedtime%", addedTime)
                                .replace("%time%", time));
                    }
                    items.add(icon(mg.getUID(), PluginControl.addLore(mg.getItem().clone(), lore)));
                }
                if (mg.getShopType().equals(ShopType.SELL) || mg.getShopType().equals(ShopType.ANY)) {
                    for (String l : MessageUtil.getValueList("SellingItemLore")) {
                        String price = String.valueOf(mg.getPrice());
                        String owner = mg.getItemOwner().getName();
                        String addedTime = new SimpleDateFormat(MessageUtil.getValue("Date-Format")).format(new Date(mg.getAddedTime()));
                        String time = PluginControl.convertToTime(mg.getTimeTillExpire(), false);
                        lore.add(l.replace("%price%", price)
                                .replace("%owner%", owner)
                                .replace("%addedtime%", addedTime)
                                .replace("%time%", time));
                    }
                    if (mg.getItem() == null) continue;
                    items.add(icon(mg.getUID(), PluginControl.addLore(mg.getItem().clone(), lore)));
                }
            }
        }
        int maxPage = PluginControl.getMaxPage(items);
        while (page > maxPage) page--;
        GUIType guiType = GUIType.ITEM_VIEWER;
        Inventory inv = holder(guiType).with(54, PluginControl.color(player, config.getString("Settings.Player-Viewer-GUIName") + " #" + page));
        List<String> options = new ArrayList<>();
        options.add("WhatIsThis.Viewing");
        for (String o : options) {
            if (config.contains("Settings.GUISettings.OtherSettings." + o + ".Toggle")) {
                if (!config.getBoolean("Settings.GUISettings.OtherSettings." + o + ".Toggle")) {
                    continue;
                }
            }
            String id = config.getString("Settings.GUISettings.OtherSettings." + o + ".Item");
            String customModelKey = "Settings.GUISettings.OtherSettings." + o + ".CustomModelData";
            Integer customModel = config.contains(customModelKey) ? config.getInt(customModelKey) : null;
            String name = config.getString("Settings.GUISettings.OtherSettings." + o + ".Name");
            int slot = config.getInt("Settings.GUISettings.OtherSettings." + o + ".Slot");
            List<String> lore;
            if (config.contains("Settings.GUISettings.OtherSettings." + o + ".Lore")) {
                lore = config.getStringList("Settings.GUISettings.OtherSettings." + o + ".Lore");
            } else {
                lore = null;
            }
            ItemStack item = lore == null
                    ? PluginControl.makeItem(id, 1, name)
                    : PluginControl.makeItem(id, 1, name, lore);
            if (customModel != null) {
                AdventureItemStack.setCustomModelData(item, customModel);
            }
            inv.setItem(slot - 1, item);
        }
        List<Integer> indexes = getInvIndexes(config, "Settings.GUISettings.OtherSettings.Content-Slots");
        itemUID.put(player.getUniqueId(), getPage(inv, items, indexes, page));
        player.openInventory(inv);
        GUIAction.openingGUI.put(player.getUniqueId(), guiType);
    }
    
    public static ItemStack getBiddingGlass(Player player, long uid) {
        FileManager.ProtectedConfiguration config = FileManager.Files.CONFIG.getFile();
        String id = config.getString("Settings.GUISettings.Auction-Settings.Bidding.Item");
        String name = config.getString("Settings.GUISettings.Auction-Settings.Bidding.Name");
        MarketGoods mg = GlobalMarket.getMarket().getMarketGoods(uid);
        ItemStack item;
        int bid = bidding.get(player.getUniqueId());
        if (config.contains("Settings.GUISettings.Auction-Settings.Bidding.Lore")) {
            List<String> lore = new ArrayList<>();
            for (String l : config.getStringList("Settings.GUISettings.Auction-Settings.Bidding.Lore")) {
                String price = String.valueOf(mg.getPrice());
                lore.add(l.replace("%bid%", String.valueOf(bid))
                        .replace("%topbid%", price));
            }
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
        List<String> lore = new ArrayList<>();
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

    public static class Holder implements InventoryHolder {
        private Inventory inventory;
        private final GUIType type;
        private Holder(GUIType type) {
            this.type = type;
        }
        private Inventory with(int size, String title) {
            return inventory = Bukkit.createInventory(this, size, title);
        }
        public GUIType getType() {
            return type;
        }
        @NotNull
        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }

    private static Holder holder(GUIType type) {
        return new Holder(type);
    }
    
    public enum GUIType {
        
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
