package studio.trc.bukkit.crazyauctionsplus.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import studio.trc.bukkit.crazyauctionsplus.Main;
import studio.trc.bukkit.crazyauctionsplus.api.events.AuctionExpireEvent;
import studio.trc.bukkit.crazyauctionsplus.currency.CurrencyManager;
import studio.trc.bukkit.crazyauctionsplus.database.DatabaseEngine;
import studio.trc.bukkit.crazyauctionsplus.database.GlobalMarket;
import studio.trc.bukkit.crazyauctionsplus.database.Storage;
import studio.trc.bukkit.crazyauctionsplus.database.StorageMethod;
import studio.trc.bukkit.crazyauctionsplus.database.engine.MySQLEngine;
import studio.trc.bukkit.crazyauctionsplus.database.engine.SQLiteEngine;
import studio.trc.bukkit.crazyauctionsplus.database.market.MySQLMarket;
import studio.trc.bukkit.crazyauctionsplus.database.market.SQLiteMarket;
import studio.trc.bukkit.crazyauctionsplus.database.market.YamlMarket;
import studio.trc.bukkit.crazyauctionsplus.database.storage.MySQLStorage;
import studio.trc.bukkit.crazyauctionsplus.database.storage.SQLiteStorage;
import studio.trc.bukkit.crazyauctionsplus.database.storage.YamlStorage;
import studio.trc.bukkit.crazyauctionsplus.util.AuctionProcess.AuctionUpdateThread;
import studio.trc.bukkit.crazyauctionsplus.util.enums.Version;
import studio.trc.bukkit.crazyauctionsplus.util.enums.ShopType;
import studio.trc.bukkit.crazyauctionsplus.util.FileManager.*;

public class PluginControl
{
    public static Map<CommandSender, Boolean> stackTraceVisible = new HashMap<>();
    public static String nmsVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    private static final Pattern hexColorPattern = Pattern.compile("#[a-fA-F0-9]{6}");
    
    public static String color(String text) {
        if (nmsVersion != null && !nmsVersion.startsWith("v1_7") && !nmsVersion.startsWith("v1_8") && !nmsVersion.startsWith("v1_9") && !nmsVersion.startsWith("v1_10") &&
            !nmsVersion.startsWith("v1_11") && !nmsVersion.startsWith("v1_12") && !nmsVersion.startsWith("v1_13") && !nmsVersion.startsWith("v1_14") && !nmsVersion.startsWith("v1_15")) {
            try {
                Matcher matcher = hexColorPattern.matcher(text);
                while (matcher.find()) {
                    String color = text.substring(matcher.start(), matcher.end());
                    text = text.replace(color, net.md_5.bungee.api.ChatColor.of(color).toString());
                    matcher = hexColorPattern.matcher(text);
                }
            } catch (Throwable ignored) {}
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }
    
    public static String getPrefix() {
        return Files.CONFIG.getFile().getString("Settings.Prefix");
    }
    
    @Deprecated
    public static String getPrefix(String msg) {
        return color(Files.CONFIG.getFile().getString("Settings.Prefix") + msg);
    }
    
    @Deprecated
    public static String removeColor(String msg) {
        return ChatColor.stripColor(msg);
    }

    @SuppressWarnings({"deprecation"})
    public static ItemStack legacyItem(Material material, int amount, Integer ty) {
        if (material == null) {
            return new ItemStack(Material.PAPER, amount);
        }
        if (ty.equals(0)) {
            return new ItemStack(material, amount);
        } else {
            return new ItemStack(material, amount, ty.shortValue());
        }
    }

    public static ItemStack defaultItem() {
        if (Version.getCurrentVersion().isNewer(Version.v1_12_R1)) {
            return new ItemStack(Material.RED_TERRACOTTA, 1);
        } else {
            Material material = Material.matchMaterial("STAINED_CLAY");
            return legacyItem(material, 1, 14);
        }
    }

    public static ItemStack makeItem(Material m, int amount, int ty) {
        ItemStack item;
        try {
            item = legacyItem(m, amount, ty);
        } catch (Exception e) {
            item = defaultItem();
            PluginControl.printStackTrace(e);
        }
        return item;
    }
    
    public static ItemStack makeItem(String type, int amount) {
        int ty = 0;
        if (type.contains(":")) {
            String[] b = type.split(":");
            type = b[0];
            ty = Integer.parseInt(b[1]);
        }
        Material m = Material.matchMaterial(type);
        return makeItem(m, amount, ty);
    }
    
    public static ItemStack makeItem(String type, int amount, String name) {
        int ty = 0;
        if (type.contains(":")) {
            String[] b = type.split(":");
            type = b[0];
            ty = Integer.parseInt(b[1]);
        }
        Material m = Material.matchMaterial(type);
        ItemStack item = makeItem(m, amount, ty);
        ItemMeta me = item.getItemMeta();
        if (me != null) {
            me.setDisplayName(color(name));
            item.setItemMeta(me);
        }
        return item;
    }
    
    public static ItemStack makeItem(String type, int amount, String name, List<String> lore) {
        ArrayList<String> l = new ArrayList<>();
        int ty = 0;
        if (type.contains(":")) {
            String[] b = type.split(":");
            type = b[0];
            ty = Integer.parseInt(b[1]);
        }
        Material m = Material.matchMaterial(type);
        ItemStack item = makeItem(m, amount, ty);
        ItemMeta me = item.getItemMeta();
        if (me != null) {
            me.setDisplayName(color(name));
            for (String L : lore)
                l.add(color(L));
            me.setLore(l);
            item.setItemMeta(me);
        }
        return item;
    }
    
    public static ItemStack makeItem(Material material, int amount, int type, String name) {
        ItemStack item = makeItem(material, amount, type);
        ItemMeta m = item.getItemMeta();
        if (m != null) {
            m.setDisplayName(color(name));
            item.setItemMeta(m);
        }
        return item;
    }
    
    public static ItemStack makeItem(Material material, int amount, int type, String name, List<String> lore) {
        ArrayList<String> l = new ArrayList<>();
        ItemStack item = makeItem(material, amount, type);
        ItemMeta m = item.getItemMeta();
        if (m != null) {
            m.setDisplayName(color(name));
            for (String L : lore)
                l.add(color(L));
            m.setLore(l);
            item.setItemMeta(m);
        }
        return item;
    }
    
    public static ItemStack makeItem(Material material, int amount, int type, String name, List<String> lore, Map<Enchantment, Integer> enchants) {
        ItemStack item = makeItem(material, amount, type);
        ItemMeta m = item.getItemMeta();
        if (m != null) {
            m.setDisplayName(name);
            m.setLore(lore);
            item.setItemMeta(m);
        }
        item.addUnsafeEnchantments(enchants);
        return item;
    }
    
    public static ItemStack addLore(ItemStack item, String i) {
        List<String> lore = new ArrayList<>();
        ItemMeta m = item.getItemMeta();
        if (m == null) return item;
        List<String> oldLore = m.hasLore() ? m.getLore() : null;
        if (oldLore != null) {
            lore.addAll(oldLore);
        }
        lore.add(i);
        m.setLore(lore);
        item.setItemMeta(m);
        return item;
    }
    
    public static ItemStack addLore(ItemStack item, List<String> list) {
        List<String> lore = new ArrayList<>();
        ItemMeta m = item.getItemMeta();
        if (m == null) return item;
        List<String> oldLore = m.hasLore() ? m.getLore() : null;
        if (oldLore != null) {
            lore.addAll(oldLore);
        }
        for (String i : list)
            lore.add(color(i));
        m.setLore(lore);
        item.setItemMeta(m);
        return item;
    }
    
    public static Integer getVersion() {
        String ver = Bukkit.getServer().getClass().getPackage().getName();
        ver = ver.substring(ver.lastIndexOf('.') + 1);
        ver = ver.replace("_", "").replace("R", "").replace("v", "");
        return Integer.parseInt(ver);
    }

    @SuppressWarnings({"deprecation"})
    public static ItemStack getItemInHand(Player player) {
        if (getVersion() >= 191) {
            return player.getInventory().getItemInMainHand();
        } else {
            return player.getItemInHand();
        }
    }

    @SuppressWarnings({"deprecation"})
    public static void setItemInHand(Player player, ItemStack item) {
        if (getVersion() >= 191) {
            player.getInventory().setItemInMainHand(item);
        } else {
            player.setItemInHand(item);
        }
    }
    
    public static boolean isNumber(String value) {
        if (value.equalsIgnoreCase("Infinity")) return false;
        if (value.equalsIgnoreCase("NaN")) return false;
        try {
            Double.valueOf(value);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public static boolean isNotInt(String value) {
        try {
            Integer.parseInt(value);
            return false;
        } catch (NumberFormatException ex) {
            return true;
        }
    }

    public static Player getPlayer(String name) {
        try {
            return Bukkit.getServer().getPlayer(name);
        } catch (Exception e) {
            return null;
        }
    }
    
    public static Player getPlayer(UUID uuid) {
        try {
            return Bukkit.getServer().getPlayer(uuid);
        } catch (Exception e) {
            return null;
        }
    }
    
    @Deprecated
    public static OfflinePlayer getOfflinePlayer(String name) {
        return Bukkit.getServer().getOfflinePlayer(name);
    }
    
    public static OfflinePlayer getOfflinePlayer(UUID uuid) {
        return Bukkit.getOfflinePlayer(uuid);
    }
    
    public static Location getLoc(Player player) {
        return player.getLocation();
    }
    
    public static void runCMD(Player player, String CMD) {
        player.performCommand(CMD);
    }
    
    @Deprecated
    public static boolean isOnline(String name) {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOnline(UUID uuid) {
        return Bukkit.getPlayer(uuid) != null;
    }
    
    public static boolean isOnline(String name, CommandSender p) {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        MessageUtil.sendMessage(p, "Not-Online");
        return false;
    }
    
    public static boolean hasCommandPermission(Player player, String perm, boolean message) {
        if (Files.CONFIG.getFile().getBoolean("Settings.Permissions.Commands." + perm + ".Default")) return true;
        if (!player.hasPermission(Files.CONFIG.getFile().getString("Settings.Permissions.Commands." + perm + ".Permission"))) {
            if (message) MessageUtil.sendMessage(player, "No-Permission");
            return false;
        }
        return true;
    }
    
    public static boolean hasCommandPermission(CommandSender sender, String perm, boolean message) {
        if (Files.CONFIG.getFile().getBoolean("Settings.Permissions.Commands." + perm + ".Default")) return true;
        if (!sender.hasPermission(Files.CONFIG.getFile().getString("Settings.Permissions.Commands." + perm + ".Permission"))) {
            if (message) MessageUtil.sendMessage(sender, "No-Permission");
            return false;
        }
        return true;
    }
    
    public static boolean hasPermission(Player player, String path, boolean message) {
        if (Files.CONFIG.getFile().getBoolean("Settings." + path + ".Default")) return true;
        if (!player.hasPermission(Files.CONFIG.getFile().getString("Settings." + path + ".Permission"))) {
            if (message) MessageUtil.sendMessage(player, "No-Permission");
            return false;
        }
        return true;
    }
    
    public static boolean notBypassLimit(Player player, ShopType type) {
        ProtectedConfiguration config = Files.CONFIG.getFile();
        switch (type) {
            case SELL: {
                if (config.getBoolean("Settings.Permissions.Market.Sell-Bypass.Default")) return false;
                return !player.hasPermission(config.getString("Settings.Permissions.Market.Sell-Bypass.Permission"));
            }
            case BUY: {
                if (config.getBoolean("Settings.Permissions.Market.Buy-Bypass.Default")) return false;
                return !player.hasPermission(config.getString("Settings.Permissions.Market.Buy-Bypass.Permission"));
            }
            case BID: {
                if (config.getBoolean("Settings.Permissions.Market.Bid-Bypass.Default")) return false;
                return !player.hasPermission(config.getString("Settings.Permissions.Market.Bid-Bypass.Permission"));
            }
            default: {
                return true;
            }
        }
    }
    
    public static boolean notBypassTaxRate(Player player, ShopType type) {
        ProtectedConfiguration config = Files.CONFIG.getFile();
        switch (type) {
            case SELL: {
                if (config.getBoolean("Settings.Permissions.Market.Sell-Tax-Rate-Bypass.Default")) return false;
                return !player.hasPermission(config.getString("Settings.Permissions.Market.Sell-Tax-Rate-Bypass.Permission"));
            }
            case BUY: {
                if (config.getBoolean("Settings.Permissions.Market.Buy-Tax-Rate-Bypass.Default")) return false;
                return !player.hasPermission(config.getString("Settings.Permissions.Market.Buy-Tax-Rate-Bypass.Permission"));
            }
            case BID: {
                if (config.getBoolean("Settings.Permissions.Market.Bid-Tax-Rate-Bypass.Default")) return false;
                return !player.hasPermission(config.getString("Settings.Permissions.Market.Bid-Tax-Rate-Bypass.Permission"));
            }
            default: {
                return true;
            }
        }
    }
    
    public static boolean hasMarketPermission(Player player, String perm) {
        ProtectedConfiguration config = Files.CONFIG.getFile();
        if (config.getBoolean("Settings.Permissions.Market." + perm + ".Default")) return true;
        return player.hasPermission(config.getString("Settings.Permissions.Market." + perm + ".Permission"));
    }
    
    public static int getLimit(Player player, ShopType type) {
        switch (type) {
            case SELL: {
                return getMarketGroup(player).getSellLimit();
            }
            case BUY: {
                return getMarketGroup(player).getBuyLimit();
            }
            case BID: {
                return getMarketGroup(player).getBidLimit();
            }
            default: {
                return 0;
            }
        }
    }
    
    public static double getTaxRate(Player player, ShopType type) {
        switch (type) {
            case SELL: {
                return getMarketGroup(player).getSellTaxRate();
            }
            case BUY: {
                return getMarketGroup(player).getBuyTaxRate();
            }
            case BID: {
                return getMarketGroup(player).getBidTaxRate();
            }
            default: {
                return 0;
            }
        }
    }
    
    public static MarketGroup getMarketGroup(Player player) {
        ProtectedConfiguration config = Files.CONFIG.getFile();
        ConfigurationSection section = config.getConfigurationSection("Settings.Permissions.Market.Permission-Groups");
        if (section != null) for (String groups : section.getKeys(false)) {
            if (section.getBoolean(groups + ".Default")) return new MarketGroup(groups);
            String permission = section.getString(groups + ".Permission");
            if (permission != null && player.hasPermission(permission)) {
                return new MarketGroup(groups);
            }
        }
        throw new IllegalStateException("插件配置错误，无法读取玩家 " + player.getName() + " 的权限组");
    }
    
    public static List<ItemStack> getPage(List<ItemStack> list, Integer page) {
        List<ItemStack> items = new ArrayList<>();
        if (page <= 0) page = 1;
        int max = 45;
        int index = page * max - max;
        int endIndex = index >= list.size() ? list.size() - 1 : index + max;
        for (; index < endIndex; index++) {
            if (index < list.size()) items.add(list.get(index));
        }
        for (; items.isEmpty(); page--) {
            if (page <= 0) break;
            index = page * max - max;
            endIndex = index >= list.size() ? list.size() - 1 : index + max;
            for (; index < endIndex; index++) {
                if (index < list.size()) items.add(list.get(index));
            }
        }
        return items;
    }
    
    public static List<Long> getMarketPageUIDs(List<Long> list, Integer page) {
        List<Long> items = new ArrayList<>();
        if (page <= 0) page = 1;
        int max = 45;
        int index = page * max - max;
        int endIndex = index >= list.size() ? list.size() - 1 : index + max;
        for (; index < endIndex; index++) {
            if (index < list.size()) items.add(list.get(index));
        }
        for (; items.isEmpty(); page--) {
            if (page <= 0) break;
            index = page * max - max;
            endIndex = index >= list.size() ? list.size() - 1 : index + max;
            for (; index < endIndex; index++) {
                if (index < list.size()) items.add(list.get(index));
            }
        }
        return items;
    }
    
    public static List<Long> getMailPageUIDs(List<Long> list, Integer page) {
        List<Long> items = new ArrayList<>();
        if (page <= 0) page = 1;
        int max = 45;
        int index = page * max - max;
        int endIndex = index >= list.size() ? list.size() - 1 : index + max;
        for (; index < endIndex; index++) {
            if (index < list.size()) items.add(list.get(index));
        }
        for (; items.isEmpty(); page--) {
            if (page <= 0) break;
            index = page * max - max;
            endIndex = index >= list.size() ? list.size() - 1 : index + max;
            for (; index < endIndex; index++) {
                if (index < list.size()) items.add(list.get(index));
            }
        }
        return items;
    }
    
    public static int getMaxPage(List<ItemStack> list) {
        int maxPage = 1;
        int amount = list.size();
        while (amount > 45) {
            amount -= 45;
            maxPage++;
        }
        return maxPage;
    }
    
    public static int getMaterialAmount(Player player, Material material, ItemMeta meta) {
        if (player == null) return 0;
        if (Files.CONFIG.getFile().getBoolean("Settings.Item-NBT-comparison")) {
            int amount = 0;
            for (ItemStack targetItem : player.getInventory().getContents()) {
                if (targetItem == null) continue;
                if (targetItem.getType().equals(material) && meta.equals(targetItem.getItemMeta())) {
                    amount += targetItem.getAmount();
                }
            }
            return amount;
        } else {
            int amount = 0;
            for (ItemStack targetItem : player.getInventory().getContents()) {
                if (targetItem == null) continue;
                if (targetItem.getType().equals(material)) {
                    amount += targetItem.getAmount();
                }
            }
            return amount;
        }
    }
    
    public static String convertToTime(long time, boolean isExpire) {
        if (isExpire) {
            return MessageUtil.getValue("Date-Settings.Never");
        }
        Calendar C = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        int total = ((int) (cal.getTimeInMillis() / 1000) - (int) (C.getTimeInMillis() / 1000));
        int D = 0, H = 0, M = 0, S = 0;
        while (total > 86400) {
            total -= 86400; D++;
        }
        while (total > 3600) {
            total -= 3600; H++;
        }
        while (total > 60) {
            total -= 60; M++;
        }
        S += total;
        StringBuilder sb = new StringBuilder();
        if (D > 0) {
            sb.append(D).append(MessageUtil.getValue("Date-Settings.Day")).append(" ");
        }
        if (H > 0) {
            sb.append(H).append(MessageUtil.getValue("Date-Settings.Hour")).append(" ");
        }
        if (M > 0) {
            sb.append(M).append(MessageUtil.getValue("Date-Settings.Minute")).append(" ");
        }
        if (S > 0) {
            sb.append(S).append(MessageUtil.getValue("Date-Settings.Second"));
        }
        return sb.toString();
    }
    
    public static long convertToMill(String time) {
        Calendar cal = Calendar.getInstance();
        for (String i : time.split(" ")) {
            if (i.contains("D") || i.contains("d")) {
                cal.add(Calendar.DATE, Integer.parseInt(i.replace("D", "").replace("d", "")));
            }
            if (i.contains("H") || i.contains("h")) {
                cal.add(Calendar.HOUR, Integer.parseInt(i.replace("H", "").replace("h", "")));
            }
            if (i.contains("M") || i.contains("m")) {
                cal.add(Calendar.MINUTE, Integer.parseInt(i.replace("M", "").replace("m", "")));
            }
            if (i.contains("S") || i.contains("s")) {
                cal.add(Calendar.SECOND, Integer.parseInt(i.replace("S", "").replace("s", "")));
            }
        }
        return cal.getTimeInMillis();
    }
    
    public static boolean isInvFull(Player player) {
        return player.getInventory().firstEmpty() == -1;
    }
    
    public static boolean isItemBlacklisted(ItemStack item) {
        return FileManager.Files.CONFIG.getFile().getStringList("Settings.BlackList").stream().anyMatch(id -> item.getType() == PluginControl.makeItem(id, 1).getType());
    }
    
    public static boolean isItemLoreBlacklisted(ItemStack item) {
        if (item.getItemMeta() == null || item.getItemMeta().getLore() == null) return false;
        return FileManager.Files.CONFIG.getFile().getStringList("Settings.Lore-Blacklist")
                .stream().anyMatch(text -> 
                        item.getItemMeta().getLore().stream()
                                .anyMatch(lore -> lore.contains(text)));
    }
    
    public static boolean isWorldDisabled(Player player) {
        if (player != null) {
            for (String worlds : Files.CONFIG.getFile().getStringList("Settings.Disabled-Worlds")) {
                if (worlds.equalsIgnoreCase(player.getWorld().getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasNoMaterial(Player player, ItemStack item) {
        return !hasMaterial(player, item.getType(), item.getItemMeta(), item.getAmount());
    }
    
    public static boolean hasMaterial(Player player, Material material, ItemMeta meta, int amountRequired) {
        if (player == null) return false;
        int amount = getMaterialAmount(player, material, meta);
        return amountRequired <= amount;
    }

    public static boolean takeMaterial(Player player, ItemStack item) {
        return takeMaterial(player, item.getType(), item.getItemMeta(), item.getAmount());
    }
    
    public static boolean takeMaterial(Player player, Material material, ItemMeta meta, int amountRequired) {
        if (player == null) return false;
        boolean isChanged = false;
        ItemStack[] contents = player.getInventory().getContents();
        if (Files.CONFIG.getFile().getBoolean("Settings.Item-NBT-comparison")) {
            for (int sort = 0; sort < contents.length;sort++) {
                ItemStack targetItem = contents[sort];
                if (targetItem == null) continue;
                if (targetItem.getType().equals(material) && meta.equals(targetItem.getItemMeta())) {
                    if (amountRequired > targetItem.getAmount()) {
                        amountRequired -= targetItem.getAmount();
                        player.getInventory().setItem(sort, new ItemStack(Material.AIR));
                        isChanged = true;
                    } else {
                        if (targetItem.getAmount() == amountRequired) {
                            player.getInventory().setItem(sort, new ItemStack(Material.AIR));
                        } else {
                            targetItem.setAmount(targetItem.getAmount() - amountRequired);
                        }
                        isChanged = true;
                        break;
                    }
                }
            }
        } else {
            for (int sort = 0; sort < contents.length;sort++) {
                ItemStack targetItem = contents[sort];
                if (targetItem == null) continue;
                if (targetItem.getType().equals(material)) {
                    if (amountRequired > targetItem.getAmount()) {
                        amountRequired -= targetItem.getAmount();
                        player.getInventory().setItem(sort, new ItemStack(Material.AIR));
                        isChanged = true;
                    } else {
                        if (targetItem.getAmount() == amountRequired) {
                            player.getInventory().setItem(sort, new ItemStack(Material.AIR));
                        } else {
                            targetItem.setAmount(targetItem.getAmount() - amountRequired);
                        }
                        isChanged = true;
                        break;
                    }
                }
            }
        }
        return isChanged;
    }
    
    public static void printStackTrace(Exception ex) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (StackTraceElement ste : ex.getStackTrace()) {
            sb.append(ste.toString());
            sb.append("\n");
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (stackTraceVisible.containsKey(player)) {
                if (stackTraceVisible.get(player)) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("%stacktrace%", sb.toString());
                    MessageUtil.sendMessage(player, "Admin-Command.PrintStackTrace.Messages", placeholders);
                }
            }
        }
        if (stackTraceVisible.containsKey(Bukkit.getServer().getConsoleSender())) {
            if (stackTraceVisible.get(Bukkit.getServer().getConsoleSender())) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("%stacktrace%", sb.toString());
                MessageUtil.sendMessage(Bukkit.getServer().getConsoleSender(), "Admin-Command.PrintStackTrace.Messages", placeholders);
            }
        }
    }
    
    public static void updateCacheData() {
        if (FileManager.isBackingUp()) return;
        if (FileManager.isRollingBack()) return;
        if (FileManager.isSyncing()) return;
        boolean shouldSave = false;
        GlobalMarket market = GlobalMarket.getMarket();
        List<MarketGoods> array = market.getItems();
        if (!array.isEmpty()) {
            for (int i = array.size() - 1;i > -1;i--) {
                MarketGoods mg = array.get(i);
                if (mg == null) {
                    continue;
                } else if (mg.getItem() == null) {
                    market.removeGoods(mg);
                    continue;
                }
                if (mg.expired()) {
                    switch (mg.getShopType()) {
                        case BUY: {
                            UUID owner = mg.getItemOwner().getUUID();
                            Player player = getPlayer(owner);
                            if (player != null) {
                                Map<String, String> placeholders = new HashMap<>();
                                placeholders.put("%item%", LangUtilsHook.getItemName(mg.getItem()));
                                MessageUtil.sendMessage(player, "Item-Has-Expired", placeholders);
                            }
                            AuctionExpireEvent event = new AuctionExpireEvent(player, mg, ShopType.BUY);
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    Bukkit.getPluginManager().callEvent(event);
                                }
                            }.runTask(Main.getInstance());
                            CurrencyManager.addMoney(Bukkit.getOfflinePlayer(owner), mg.getReward());
                            market.removeGoods(mg.getUID());
                            break;
                        }
                        case SELL: {
                            UUID owner = mg.getItemOwner().getUUID();
                            Player player = getPlayer(owner);
                            if (player != null) {
                                Map<String, String> placeholders = new HashMap<>();
                                placeholders.put("%item%", LangUtilsHook.getItemName(mg.getItem()));
                                MessageUtil.sendMessage(player, "Item-Has-Expired", placeholders);
                            }
                            AuctionExpireEvent event = new AuctionExpireEvent(player, mg, ShopType.SELL);
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    Bukkit.getPluginManager().callEvent(event);
                                }
                            }.runTask(Main.getInstance());
                            Storage playerdata = Storage.getPlayer(getOfflinePlayer(owner));
                            ItemMail im = new ItemMail(playerdata.makeUID(), getOfflinePlayer(owner), mg.getItem(), PluginControl.convertToMill(FileManager.Files.CONFIG.getFile().getString("Settings.Full-Expire-Time")), mg.getAddedTime(), false);
                            playerdata.addItem(im);
                            market.removeGoods(mg.getUID());
                            break;
                        }
                    }
                    shouldSave = true;
                }
            }
        }
        if (shouldSave) market.saveData();
    }
    
    public static boolean useMySQLStorage() {
        return Files.CONFIG.getFile().getBoolean("Settings.MySQL-Storage.Enabled");
    }
    
    public static boolean useSQLiteStorage() {
        return Files.CONFIG.getFile().getBoolean("Settings.SQLite-Storage.Enabled");
    }
    
    public static boolean useSplitDatabase() {
        return Files.CONFIG.getFile().getBoolean("Settings.Split-Database.Enabled");
    }
    
    public static boolean isGlobalMarketAutomaticUpdate() {
        return Files.CONFIG.getFile().getBoolean("Settings.Global-Market-Automatic-Update.Enabled");
    }
    
    public static boolean automaticBackup() {
        return Files.CONFIG.getFile().getBoolean("Settings.Automatic-Backup");
    }
    
    public static double getGlobalMarketAutomaticUpdateDelay() {
        return Files.CONFIG.getFile().getDouble("Settings.Global-Market-Automatic-Update.Update-Delay");
    }
    
    public static StorageMethod getItemMailStorageMethod() {
        if (!useSplitDatabase()) {
            if (useMySQLStorage()) {
                return StorageMethod.MySQL;
            } else if (useSQLiteStorage()) {
                return StorageMethod.SQLite;
            } else {
                return StorageMethod.YAML;
            }
        }
        try {
            return StorageMethod.valueOf(Files.CONFIG.getFile().getString("Settings.Split-Database.Item-Mail").toUpperCase().replace("MYSQL", "MySQL").replace("SQLITE", "SQLite"));
        } catch (IllegalArgumentException ex) {
            PluginControl.printStackTrace(ex);
            return StorageMethod.YAML;
        }
    }
    
    public static StorageMethod getMarketStorageMethod() {
        if (!useSplitDatabase()) {
            if (useMySQLStorage()) {
                return StorageMethod.MySQL;
            } else if (useSQLiteStorage()) {
                return StorageMethod.SQLite;
            } else {
                return StorageMethod.YAML;
            }
        }
        try {
            return StorageMethod.valueOf(Files.CONFIG.getFile().getString("Settings.Split-Database.Market").toUpperCase().replace("MYSQL", "MySQL").replace("SQLITE", "SQLite"));
        } catch (IllegalArgumentException ex) {
            PluginControl.printStackTrace(ex);
            return StorageMethod.YAML;
        }
    }
    
    private static long backupFilesAcquisitionTime = 0;
    private static List<String> backupFiles = new ArrayList<>();
    
    public static List<String> getBackupFiles() {
        /*
          Since TabComplete is obtained every time you enter it,
          in order to prevent a large amount of useless IO performance from being wasted,
          the default limit is to obtain it every 5 seconds.
         */
        if (System.currentTimeMillis() - backupFilesAcquisitionTime <= 5000) {
            return backupFiles;
        }
        List<String> list = new ArrayList<>();
        File folder = new File("plugins/CrazyAuctionsPlus/Backup/");
        if (!folder.exists()) return list;
        File[] files = folder.listFiles();
        if (files != null) for (File f : files) {
            list.add(f.getName());
        }
        backupFiles = list;
        backupFilesAcquisitionTime = System.currentTimeMillis();
        return list;
    }
    
    public static boolean reload(ReloadType type) {
        FileManager fm = FileManager.getInstance();
        try {
            switch (type) {
                case ALL: {
                    fm.logInfo(true).setup(Main.getInstance());
                    if (AuctionUpdateThread.thread != null) AuctionUpdateThread.thread.stop();
                    if (PluginControl.useSplitDatabase()) {
                        boolean database_MySQL = false;
                        boolean database_SQLite = false;
                        switch (PluginControl.getItemMailStorageMethod()) {
                            case MySQL: {
                                MySQLStorage.cache.clear();
                                database_MySQL = true;
                                break;
                            }
                            case SQLite: {
                                SQLiteStorage.cache.clear();
                                database_SQLite = true;
                                break;
                            }
                            case YAML: {
                                YamlStorage.cache.clear();
                                break;
                            }
                        }
                        
                        switch (PluginControl.getMarketStorageMethod()) {
                            case MySQL: {
                                database_MySQL = true;
                                break;
                            }
                            case SQLite: {
                                database_SQLite = true;
                                break;
                            }
                            case YAML: {
                                fm.reloadDatabaseFile();
                                break;
                            }
                        }
                        
                        if (database_MySQL) {
                            MySQLEngine.getInstance().reloadConnectionParameters();
                        }
                        
                        if (database_SQLite) {
                            SQLiteEngine.getInstance().reloadConnectionParameters();
                        }
                        
                        GlobalMarket.getMarket().reloadData();
                    } else if (PluginControl.useMySQLStorage()) {
                        MySQLEngine.getInstance().reloadConnectionParameters();
                        MySQLStorage.cache.clear();
                        MySQLMarket.getInstance().reloadData();
                    } else if (PluginControl.useSQLiteStorage()) {
                        SQLiteEngine.getInstance().reloadConnectionParameters();
                        SQLiteStorage.cache.clear();
                        SQLiteMarket.getInstance().reloadData();
                    } else {
                        YamlStorage.cache.clear();
                        YamlMarket.getInstance().reloadData();
                    }
                    if (Files.CONFIG.getFile().getBoolean("Settings.Auction-Process-Settings.Countdown-Tips.Enabled")) {
                        new AuctionUpdateThread(Files.CONFIG.getFile().getDouble("Settings.Auction-Process-Settings.Countdown-Tips.Update-Delay")).start();
                    }
                    return true;
                }
                case CONFIG: {
                    if (AuctionUpdateThread.thread != null) AuctionUpdateThread.thread.stop();
                    fm.reloadConfig();
                    if (Files.CONFIG.getFile().getBoolean("Settings.Auction-Process-Settings.Countdown-Tips.Enabled")) {
                        new AuctionUpdateThread(Files.CONFIG.getFile().getDouble("Settings.Auction-Process-Settings.Countdown-Tips.Update-Delay")).start();
                    }
                    return true;
                }
                case DATABASE: {
                    if (PluginControl.useSplitDatabase()) {
                        boolean database_MySQL = false;
                        boolean database_SQLite = false;
                        switch (PluginControl.getItemMailStorageMethod()) {
                            case MySQL: {
                                MySQLStorage.cache.clear();
                                database_MySQL = true;
                                break;
                            }
                            case SQLite: {
                                SQLiteStorage.cache.clear();
                                database_SQLite = true;
                                break;
                            }
                            case YAML: {
                                YamlStorage.cache.clear();
                                break;
                            }
                        }
                        
                        switch (PluginControl.getMarketStorageMethod()) {
                            case MySQL: {
                                MySQLMarket.getInstance().reloadData();
                                database_MySQL = true;
                                break;
                            }
                            case SQLite: {
                                SQLiteMarket.getInstance().reloadData();
                                database_SQLite = true;
                                break;
                            }
                            case YAML: {
                                fm.reloadDatabaseFile();
                                break;
                            }
                        }
                        
                        if (database_MySQL) {
                            MySQLEngine.getInstance().reloadConnectionParameters();
                        }
                        
                        if (database_SQLite) {
                            SQLiteEngine.getInstance().reloadConnectionParameters();
                        }
                        
                    } else if (PluginControl.useMySQLStorage()) {
                        MySQLEngine.getInstance().reloadConnectionParameters();
                        MySQLStorage.cache.clear();
                        MySQLMarket.getInstance().reloadData();
                    } else if (PluginControl.useSQLiteStorage()) {
                        SQLiteEngine.getInstance().reloadConnectionParameters();
                        SQLiteStorage.cache.clear();
                        SQLiteMarket.getInstance().reloadData();
                    } else {
                        fm.reloadDatabaseFile();
                        YamlStorage.cache.clear();
                    }
                    return true;
                }
                case MARKET: {
                    if (PluginControl.useMySQLStorage()) {
                        MySQLMarket.getInstance().reloadData();
                    } else if (PluginControl.useSQLiteStorage()) {
                        SQLiteMarket.getInstance().reloadData();
                    } else {
                        fm.reloadDatabaseFile();
                    }
                    return true;
                }
                case MESSAGES: {
                    fm.reloadMessages();
                    return true;
                }
                case PLAYERDATA: {
                    if (PluginControl.useMySQLStorage()) {
                        MySQLStorage.cache.clear();
                    } else if (PluginControl.useSQLiteStorage()) {
                        SQLiteStorage.cache.clear();
                    } else {
                        YamlStorage.cache.clear();
                    }
                    return true;
                }
                case CATEGORY: {
                    fm.reloadCategoryFile();
                    return true;
                }
                case ITEMCOLLECTION: {
                    fm.reloadItemCollectionFile();
                    return true;
                }
                default: {
                    return false;
                }
            }
        } catch (Exception ex) {
            PluginControl.printStackTrace(ex);
            return false;
        }
    }

    public static boolean isPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    public enum ReloadType {
        
        /*
          Config.yml
         */ /**
         * Config.yml
         */
        CONFIG,
        
        /**
         * Data for all players
         */
        PLAYERDATA,
        
        /**
         * Market Commodity Data (Database.yml, or related database data
         */
        MARKET,
        
        /**
         * MessageUtil.yml
         */
        MESSAGES,
        
        /**
         * Category.yml
         */
        CATEGORY,
        
        /**
         * ItemCollection.yml
         */
        ITEMCOLLECTION,
        
        /**
         * Refers to MySQL, SQLite connections, including loaded cached data.
         */
        DATABASE,
        
        /**
         * All settings including database, language, etc.
         */
        ALL
    }
    
    public static class RollBackMethod {
        private final File rollBackFile;
        private final FileManager fm;
        private final CommandSender[] senders;
        
        public RollBackMethod(File rollBackFile, FileManager fm, CommandSender... senders) {
            this.rollBackFile = rollBackFile;
            this.fm = fm;
            this.senders = senders;
        }
        
        public static void backup() throws SQLException, IOException {
            String fileName = MessageUtil.getValue("Admin-Command.Backup.Backup-Name").replace("%date%", new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date())) + ".db";
            GlobalMarket market = GlobalMarket.getMarket();
            File folder = new File("plugins/CrazyAuctionsPlus/Backup");
            if (!folder.exists()) folder.mkdir();
            File file = new File(folder, fileName);
            if (!file.exists()) {
                file.createNewFile();
            } else {
                file.delete();
                file.createNewFile();
            }
            try (Connection DBFile = DriverManager.getConnection("jdbc:sqlite:plugins/CrazyAuctionsPlus/Backup/" + fileName)) {
                DBFile.prepareStatement("CREATE TABLE IF NOT EXISTS ItemMail" +
                    "("
                    + "UUID VARCHAR(36) NOT NULL PRIMARY KEY,"
                    + "Name VARCHAR(16) NOT NULL,"
                    + "YamlData LONGTEXT" +
                    ");").executeUpdate();
                DBFile.prepareStatement("CREATE TABLE IF NOT EXISTS Market" +
                    "("
                    + "YamlMarket LONGTEXT" +
                    ");").executeUpdate();
                PreparedStatement statement = DBFile.prepareStatement("INSERT INTO Market (YamlMarket) VALUES(?)");
                statement.setString(1, market.getYamlData().saveToString());
                statement.executeUpdate();
                if (PluginControl.useSplitDatabase()) {
                    switch (PluginControl.getItemMailStorageMethod()) {
                        case MySQL: {
                            MySQLEngine.backupPlayerData(DBFile);
                            break;
                        }
                        case SQLite: {
                            SQLiteEngine.backupPlayerData(DBFile);
                            break;
                        }
                        case YAML:
                        default: {
                            File playerFolder = new File("plugins/CrazyAuctionsPlus/Players/");
                            if (playerFolder.exists()) {
                                File[] files = playerFolder.listFiles();
                                if (files != null) for (File f : files) {
                                    if (f.getName().endsWith(".yml")) {
                                        YamlConfiguration yaml = new YamlConfiguration();
                                        try {
                                            yaml.load(f);
                                        } catch (IOException | InvalidConfigurationException ex) {
                                            PluginControl.printStackTrace(ex);
                                            continue;
                                        }
                                        PreparedStatement ps = DBFile.prepareStatement("INSERT INTO ItemMail (Name, UUID, YamlData) VALUES(?, ?, ?)");
                                        ps.setString(1, yaml.get("Name") != null ? yaml.getString("Name") : "null");
                                        ps.setString(2, f.getName());
                                        ps.setString(3, yaml.get("Items") != null ? yaml.saveToString() : "{}");
                                        ps.executeUpdate();
                                    }
                                }
                            }
                            break;
                        }
                    }
                } else if (PluginControl.useMySQLStorage()) {
                    MySQLEngine.backupPlayerData(DBFile);
                } else if (PluginControl.useSQLiteStorage()) {
                    SQLiteEngine.backupPlayerData(DBFile);
                } else {
                    File playerFolder = new File("plugins/CrazyAuctionsPlus/Players/");
                    if (playerFolder.exists()) {
                        File[] files = playerFolder.listFiles();
                        if (files != null) for (File f : files) {
                            if (f.getName().endsWith(".yml")) {
                                YamlConfiguration yaml = new YamlConfiguration();
                                try {
                                    yaml.load(f);
                                } catch (IOException | InvalidConfigurationException ex) {
                                    PluginControl.printStackTrace(ex);
                                    continue;
                                }
                                PreparedStatement ps = DBFile.prepareStatement("INSERT INTO ItemMail (Name, UUID, YamlData) VALUES(?, ?, ?)");
                                ps.setString(1, yaml.get("Name") != null ? yaml.getString("Name") : "null");
                                ps.setString(2, f.getName());
                                ps.setString(3, yaml.get("Items") != null ? yaml.saveToString() : "{}");
                                ps.executeUpdate();
                            }
                        }
                    }
                }
            }
        }
        
        /**
         * 
         * Perform the rollback method
         * 
         * @param backup If an error occurs during the rollback process,
         *                there is a chance that data will be lost,
         *                so we recommend that you back up the current data before rolling back.
         */
        public void rollBack(boolean backup) {
            try {
                if (backup) {
                    backup();
                }
                if (rollBackFile.exists()) {
                    try (Connection sqlConnection = DriverManager.getConnection("jdbc:sqlite:plugins/CrazyAuctionsPlus/Backup/" + rollBackFile.getName())) {
                        
                        // Roll Back Market Database.
                        ResultSet marketRS = sqlConnection.prepareStatement("SELECT YamlMarket FROM Market").executeQuery();
                        if (marketRS.next()) {
                            if (PluginControl.useSplitDatabase()) {
                                switch (PluginControl.getMarketStorageMethod()) {
                                    case MySQL: {
                                        DatabaseEngine engine = MySQLEngine.getInstance();
                                        PreparedStatement statement = engine.getConnection().prepareStatement("UPDATE " + MySQLEngine.getDatabaseName() + "." + MySQLEngine.getMarketTable() + " SET " +
                                                "YamlMarket = ?");
                                        statement.setString(1, marketRS.getString("YamlMarket"));
                                        engine.executeUpdate(statement);
                                        GlobalMarket.getMarket().reloadData();
                                        break;
                                    }
                                    case SQLite: {
                                        DatabaseEngine engine = SQLiteEngine.getInstance();
                                        PreparedStatement statement = engine.getConnection().prepareStatement("UPDATE " + SQLiteEngine.getMarketTable() + " SET " +
                                                "YamlMarket = ?");
                                        statement.setString(1, marketRS.getString("YamlMarket"));
                                        engine.executeUpdate(statement);
                                        GlobalMarket.getMarket().reloadData();
                                        break;
                                    }
                                    case YAML:
                                    default: {
                                        String yamlData = marketRS.getString("YamlMarket");
                                        File databaseFile = new File("plugins/CrazyAuctionsPlus/Database.yml");
                                        if (!databaseFile.exists()) {
                                            databaseFile.createNewFile();
                                        }
                                        try (OutputStream out = java.nio.file.Files.newOutputStream(databaseFile.toPath())) {
                                            out.write(yamlData.getBytes());
                                        }
                                        fm.reloadDatabaseFile();
                                        break;
                                    }
                                }
                            } else if (PluginControl.useMySQLStorage()) {
                                DatabaseEngine engine = MySQLEngine.getInstance();
                                PreparedStatement statement = engine.getConnection().prepareStatement("UPDATE " + MySQLEngine.getDatabaseName() + "." + MySQLEngine.getMarketTable() + " SET " +
                                        "YamlMarket = ?");
                                statement.setString(1, marketRS.getString("YamlMarket"));
                                engine.executeUpdate(statement);
                                GlobalMarket.getMarket().reloadData();
                            } else if (PluginControl.useSQLiteStorage()) {
                                DatabaseEngine engine = SQLiteEngine.getInstance();
                                PreparedStatement statement = engine.getConnection().prepareStatement("UPDATE " + SQLiteEngine.getMarketTable() + " SET " +
                                        "YamlMarket = ?");
                                statement.setString(1, marketRS.getString("YamlMarket"));
                                engine.executeUpdate(statement);
                                GlobalMarket.getMarket().reloadData();
                            } else {
                                String yamlData = marketRS.getString("YamlMarket");
                                File databaseFile = new File("plugins/CrazyAuctionsPlus/Database.yml");
                                if (!databaseFile.exists()) {
                                    databaseFile.createNewFile();
                                }
                                try (OutputStream out = java.nio.file.Files.newOutputStream(databaseFile.toPath())) {
                                    out.write(yamlData.getBytes());
                                }
                                fm.reloadDatabaseFile();
                            }
                        }
                        
                        // Roll Back Item Mail
                        ResultSet itemMailRS = sqlConnection.prepareStatement("SELECT * FROM ItemMail").executeQuery();
                        if (PluginControl.useSplitDatabase()) {
                            switch (PluginControl.getMarketStorageMethod()) {
                                case MySQL: {
                                    DatabaseEngine engine = MySQLEngine.getInstance();
                                    engine.executeUpdate(engine.getConnection().prepareStatement("DELETE FROM " + MySQLEngine.getDatabaseName()+ "." + MySQLEngine.getItemMailTable()));
                                    while (itemMailRS.next()) {
                                        String uuid = itemMailRS.getString("UUID");
                                        String name = itemMailRS.getString("Name");
                                        String yaml = itemMailRS.getString("YamlData");
                                        PreparedStatement statement = engine.getConnection().prepareStatement("INSERT INTO " + MySQLEngine.getDatabaseName()+ "." + MySQLEngine.getItemMailTable() + " (Name, UUID, YamlData) VALUES(?, ?, ?)");
                                        statement.setString(1, name);
                                        statement.setString(2, uuid);
                                        statement.setString(3, yaml);
                                        engine.executeUpdate(statement);
                                    }
                                    MySQLStorage.cache.clear();
                                    break;
                                }
                                case SQLite: {
                                    DatabaseEngine engine = SQLiteEngine.getInstance();
                                    engine.executeUpdate(engine.getConnection().prepareStatement("DELETE FROM " + SQLiteEngine.getItemMailTable()));
                                    while (itemMailRS.next()) {
                                        String uuid = itemMailRS.getString("UUID");
                                        String name = itemMailRS.getString("Name");
                                        String yaml = itemMailRS.getString("YamlData");
                                        PreparedStatement statement = engine.getConnection().prepareStatement("INSERT INTO " + SQLiteEngine.getItemMailTable() + " (Name, UUID, YamlData) VALUES(?, ?, ?)");
                                        statement.setString(1, name);
                                        statement.setString(2, uuid);
                                        statement.setString(3, yaml);
                                        engine.executeUpdate(statement);
                                    }
                                    SQLiteStorage.cache.clear();
                                    break;
                                }
                                case YAML:
                                default: {
                                    File path = new File("plugins/CrazyAuctionsPlus/Players/");
                                    if (!path.exists()) {
                                        path.mkdir();
                                    }
                                    while (itemMailRS.next()) {
                                        File dataFile = new File(path, itemMailRS.getString("UUID") + ".yml");
                                        if (dataFile.exists()) {
                                            try (OutputStream out = java.nio.file.Files.newOutputStream(dataFile.toPath())) {
                                                out.write(itemMailRS.getString("YamlData").getBytes());
                                            }
                                        } else {
                                            dataFile.createNewFile();
                                            try (OutputStream out = java.nio.file.Files.newOutputStream(dataFile.toPath())) {
                                                out.write(itemMailRS.getString("YamlData").getBytes());
                                            }
                                        }
                                    }
                                    YamlStorage.cache.clear();
                                    break;
                                }
                            }
                        } else if (PluginControl.useMySQLStorage()) {
                            DatabaseEngine engine = MySQLEngine.getInstance();
                            engine.executeUpdate(engine.getConnection().prepareStatement("DELETE FROM " + MySQLEngine.getDatabaseName()+ "." + MySQLEngine.getItemMailTable()));
                            while (itemMailRS.next()) {
                                String uuid = itemMailRS.getString("UUID");
                                String name = itemMailRS.getString("Name");
                                String yaml = itemMailRS.getString("YamlData");
                                PreparedStatement statement = engine.getConnection().prepareStatement("INSERT INTO " + MySQLEngine.getDatabaseName()+ "." + MySQLEngine.getItemMailTable() + " (Name, UUID, YamlData) VALUES(?, ?, ?)");
                                statement.setString(1, name);
                                statement.setString(2, uuid);
                                statement.setString(3, yaml);
                                engine.executeUpdate(statement);
                            }
                            MySQLStorage.cache.clear();
                        } else if (PluginControl.useSQLiteStorage()) {
                            DatabaseEngine engine = SQLiteEngine.getInstance();
                            engine.executeUpdate(engine.getConnection().prepareStatement("DELETE FROM " + SQLiteEngine.getItemMailTable()));
                            while (itemMailRS.next()) {
                                String uuid = itemMailRS.getString("UUID");
                                String name = itemMailRS.getString("Name");
                                String yaml = itemMailRS.getString("YamlData");
                                PreparedStatement statement = engine.getConnection().prepareStatement("INSERT INTO " + SQLiteEngine.getItemMailTable() + " (Name, UUID, YamlData) VALUES(?, ?, ?)");
                                statement.setString(1, name);
                                statement.setString(2, uuid);
                                statement.setString(3, yaml);
                                engine.executeUpdate(statement);
                            }
                            SQLiteStorage.cache.clear();
                        } else {
                            File path = new File("plugins/CrazyAuctionsPlus/Players/");
                            if (!path.exists()) {
                                path.mkdir();
                            }
                            while (itemMailRS.next()) {
                                File dataFile = new File(path, itemMailRS.getString("UUID") + ".yml");
                                if (dataFile.exists()) {
                                    try (OutputStream out = java.nio.file.Files.newOutputStream(dataFile.toPath())) {
                                        out.write(itemMailRS.getString("YamlData").getBytes());
                                    }
                                } else {
                                    dataFile.createNewFile();
                                    try (OutputStream out = java.nio.file.Files.newOutputStream(dataFile.toPath())) {
                                        out.write(itemMailRS.getString("YamlData").getBytes());
                                    }
                                }
                            }
                            YamlStorage.cache.clear();
                        }
                    }
                }
                for (CommandSender sender : senders) {
                    if (sender != null) {
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("%file%", rollBackFile.getName());
                        MessageUtil.sendMessage(sender, "Admin-Command.RollBack.Successfully", placeholders);
                    }
                }
            } catch (Exception ex) {
                for (CommandSender sender : senders) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("%error%", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
                    MessageUtil.sendMessage(sender, "Admin-Command.RollBack.Failed", placeholders);
                }
                PluginControl.printStackTrace(ex);
            }
        }
    }
}