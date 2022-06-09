package studio.trc.bukkit.crazyauctionsplus.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import studio.trc.bukkit.crazyauctionsplus.currency.CurrencyManager;
import studio.trc.bukkit.crazyauctionsplus.database.GlobalMarket;
import studio.trc.bukkit.crazyauctionsplus.database.engine.MySQLEngine;
import studio.trc.bukkit.crazyauctionsplus.database.engine.SQLiteEngine;
import studio.trc.bukkit.crazyauctionsplus.database.StorageMethod;
import studio.trc.bukkit.crazyauctionsplus.database.Storage;
import studio.trc.bukkit.crazyauctionsplus.util.GUI;
import studio.trc.bukkit.crazyauctionsplus.util.ItemMail;
import studio.trc.bukkit.crazyauctionsplus.util.Category;
import studio.trc.bukkit.crazyauctionsplus.util.CrazyAuctions;
import studio.trc.bukkit.crazyauctionsplus.util.FileManager;
import studio.trc.bukkit.crazyauctionsplus.util.enums.Messages;
import studio.trc.bukkit.crazyauctionsplus.util.enums.ShopType;
import studio.trc.bukkit.crazyauctionsplus.util.enums.Version;
import studio.trc.bukkit.crazyauctionsplus.util.PluginControl;
import studio.trc.bukkit.crazyauctionsplus.util.PluginControl.ReloadType;
import studio.trc.bukkit.crazyauctionsplus.util.FileManager.Files;
import studio.trc.bukkit.crazyauctionsplus.util.ItemOwner;
import studio.trc.bukkit.crazyauctionsplus.util.MarketGoods;
import studio.trc.bukkit.crazyauctionsplus.util.ItemCollection;
import studio.trc.bukkit.crazyauctionsplus.api.events.AuctionListEvent;
import studio.trc.bukkit.crazyauctionsplus.event.GUIAction;
import studio.trc.bukkit.crazyauctionsplus.Main;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class PluginCommand
    implements CommandExecutor, TabCompleter
{
    public static FileManager fileManager = FileManager.getInstance();
    public static CrazyAuctions crazyAuctions = CrazyAuctions.getInstance();
    
    private final static Map<CommandSender, String> marketConfirm = new HashMap();
    private final static Map<CommandSender, String> itemMailConfirm = new HashMap();
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lable, String[] args) {
        if (lable.equalsIgnoreCase("CrazyAuctions") || lable.equalsIgnoreCase("CrazyAuction") || lable.equalsIgnoreCase("CA") || lable.equalsIgnoreCase("CAP") || lable.equalsIgnoreCase("CrazyAuctionsPlus")) {
            if (FileManager.isBackingUp()) {
                Messages.sendMessage(sender, "Admin-Command.Backup.BackingUp");
                return true;
            }
            if (FileManager.isRollingBack()) {
                Messages.sendMessage(sender, "Admin-Command.RollBack.RollingBack");
                return true;
            }
            if (args.length == 0) {
                if (!PluginControl.hasCommandPermission(sender, "Access", true)) return true;
                Map<String, String> placeholders = new HashMap();
                placeholders.put("%version%", Main.getInstance().getDescription().getVersion());
                Messages.sendMessage(sender, "CrazyAuctions-Main", placeholders);
                return true;
            }
            if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("Help")) {
                    if (!PluginControl.hasCommandPermission(sender, "Help", true)) return true;
                    Messages.sendMessage(sender, "Help-Menu");
                    return true;
                }
                if (args[0].equalsIgnoreCase("Reload")) {
                    if (!PluginControl.hasCommandPermission(sender, "Reload", true)) return true;
                    if (args.length == 1) {
                        PluginControl.reload(ReloadType.ALL);
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (GUI.openingGUI.containsKey(player.getUniqueId())) {
                                player.closeInventory();
                            }
                        }
                        Messages.sendMessage(sender, "Reload");
                    } else if (args.length >= 2) {
                        if (args[1].equalsIgnoreCase("database")) {
                            if (!PluginControl.hasCommandPermission(sender, "Reload.SubCommands.Database", true)) return true;
                            PluginControl.reload(ReloadType.DATABASE);
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                if (GUI.openingGUI.containsKey(player.getUniqueId())) {
                                    player.closeInventory();
                                }
                            }
                            Messages.sendMessage(sender, "Reload-Database");
                        } else if (args[1].equalsIgnoreCase("config")) {
                            if (!PluginControl.hasCommandPermission(sender, "Reload.SubCommands.Config", true)) return true;
                            PluginControl.reload(ReloadType.CONFIG);
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                if (GUI.openingGUI.containsKey(player.getUniqueId())) {
                                    player.closeInventory();
                                }
                            }
                            Messages.sendMessage(sender, "Reload-Config");
                        } else if (args[1].equalsIgnoreCase("market")) {
                            if (!PluginControl.hasCommandPermission(sender, "Reload.SubCommands.Market", true)) return true;
                            PluginControl.reload(ReloadType.MARKET);
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                if (GUI.openingGUI.containsKey(player.getUniqueId())) {
                                    player.closeInventory();
                                }
                            }
                            Messages.sendMessage(sender, "Reload-Market");
                        } else if (args[1].equalsIgnoreCase("messages")) {
                            if (!PluginControl.hasCommandPermission(sender, "Reload.SubCommands.Messages", true)) return true;
                            PluginControl.reload(ReloadType.MESSAGES);
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                if (GUI.openingGUI.containsKey(player.getUniqueId())) {
                                    player.closeInventory();
                                }
                            }
                            Messages.sendMessage(sender, "Reload-Messages");
                        } else if (args[1].equalsIgnoreCase("playerdata")) {
                            if (!PluginControl.hasCommandPermission(sender, "Reload.SubCommands.PlayerData", true)) return true;
                            PluginControl.reload(ReloadType.PLAYERDATA);
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                if (GUI.openingGUI.containsKey(player.getUniqueId())) {
                                    player.closeInventory();
                                }
                            }
                            Messages.sendMessage(sender, "Reload-PlayerData");
                        } else if (args[1].equalsIgnoreCase("category")) {
                            if (!PluginControl.hasCommandPermission(sender, "Reload.SubCommands.Category", true)) return true;
                            PluginControl.reload(ReloadType.CATEGORY);
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                if (GUI.openingGUI.containsKey(player.getUniqueId())) {
                                    player.closeInventory();
                                }
                            }
                            Messages.sendMessage(sender, "Reload-Category");
                        } else if (args[1].equalsIgnoreCase("itemcollection")) {
                            if (!PluginControl.hasCommandPermission(sender, "Reload.SubCommands.ItemCollection", true)) return true;
                            PluginControl.reload(ReloadType.ITEMCOLLECTION);
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                if (GUI.openingGUI.containsKey(player.getUniqueId())) {
                                    player.closeInventory();
                                }
                            }
                            Messages.sendMessage(sender, "Reload-ItemCollection");
                        } else {
                            PluginControl.reload(ReloadType.ALL);
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                if (GUI.openingGUI.containsKey(player.getUniqueId())) {
                                    player.closeInventory();
                                }
                            }
                            Messages.sendMessage(sender, "Reload");
                        }
                    }
                    return true;
                }
                if (args[0].equalsIgnoreCase("Admin")) {
                    if (!PluginControl.hasCommandPermission(sender, "Admin", true)) return true;
                    if (args.length == 1) {
                        Messages.sendMessage(sender, "Admin-Menu");
                        return true;
                    } else if (args.length >= 2) {
                        if (args[1].equalsIgnoreCase("backup")) {
                            if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Backup", true)) return true;
                            if (FileManager.isBackingUp()) {
                                Messages.sendMessage(sender, "Admin-Command.Backup.BackingUp");
                                return true;
                            }
                            Messages.sendMessage(sender, "Admin-Command.Backup.Starting");
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                if (GUI.openingGUI.containsKey(player.getUniqueId())) {
                                    player.closeInventory();
                                }
                            }
                            FileManager.backup(sender);
                            return true;
                        } else if (args[1].equalsIgnoreCase("rollback")) {
                            if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.RollBack", true)) return true;
                            if (FileManager.isRollingBack()) {
                                Messages.sendMessage(sender, "Admin-Command.RollBack.RollingBack");
                                return true;
                            }
                            if (args.length == 2) {
                                Messages.sendMessage(sender, "Admin-Command.RollBack.Help");
                                return true;
                            } else if (args.length >= 3) {
                                File backupFile = new File("plugins/CrazyAuctionsPlus/Backup/" + args[2]);
                                if (backupFile.exists()) {
                                    Messages.sendMessage(sender, "Admin-Command.RollBack.Starting");
                                    for (Player player : Bukkit.getOnlinePlayers()) {
                                        if (GUI.openingGUI.containsKey(player.getUniqueId())) {
                                            player.closeInventory();
                                        }
                                    }
                                    FileManager.rollBack(backupFile, sender);
                                    return true;
                                } else {
                                    Map<String, String> placeholders = new HashMap();
                                    placeholders.put("%file%", args[2]);
                                    Messages.sendMessage(sender, "Admin-Command.RollBack.Backup-Not-Exist", placeholders);
                                    return true;
                                }
                            }
                        } else if (args[1].equalsIgnoreCase("info")) {
                            if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Info", true)) return true;
                            if (args.length == 2) {
                                Messages.sendMessage(sender, "Admin-Command.Info.Help");
                                return true;
                            } else if (args.length >= 3) {
                                Player player = Bukkit.getPlayer(args[2]);
                                if (player == null) {
                                    OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(args[2]);
                                    if (offlineplayer != null) {
                                        int items = 0;
                                        String database;
                                        if (PluginControl.useSplitDatabase()) {
                                            switch (PluginControl.getItemMailStorageMethod()) {
                                                case MySQL: {
                                                    database = "[MySQL] [Database: " + MySQLEngine.getDatabaseName() + "] -> [Table: " + MySQLEngine.getItemMailTable() + "] -> [Colunm: UUID:" + offlineplayer.getUniqueId() + "]";
                                                    break;
                                                }
                                                case SQLite: {
                                                    database = "[SQLite] [" + SQLiteEngine.getFilePath() + SQLiteEngine.getFileName() + "] -> [Table: " + MySQLEngine.getItemMailTable() + "] -> [Colunm: UUID:" + offlineplayer.getUniqueId() + "]";
                                                    break;
                                                }
                                                default: {
                                                    database = new File("plugins/CrazyAuctionsPlus/Players/" + offlineplayer.getUniqueId() + ".yml").getPath();
                                                    break;
                                                }
                                            }
                                        } else if (PluginControl.useMySQLStorage()) {
                                            database = "[MySQL] [Database: " + MySQLEngine.getDatabaseName() + "] -> [Table: " + MySQLEngine.getItemMailTable() + "] -> [Colunm: UUID:" + offlineplayer.getUniqueId() + "]";
                                        } else if (PluginControl.useSQLiteStorage()) {
                                            database = "[SQLite] [" + SQLiteEngine.getFilePath() + SQLiteEngine.getFileName() + "] -> [Table: " + MySQLEngine.getItemMailTable() + "] -> [Colunm: UUID:" + offlineplayer.getUniqueId() + "]";
                                        } else {
                                            database = new File("plugins/CrazyAuctionsPlus/Players/" + offlineplayer.getUniqueId() + ".yml").getPath();
                                        }
                                        for (MarketGoods mg : GlobalMarket.getMarket().getItems()) {
                                            if (mg.getItemOwner().getUUID().equals(offlineplayer.getUniqueId())) {
                                                items++;
                                            }
                                        }
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("%player%", offlineplayer.getName());
                                        placeholders.put("%group%", Messages.getValue("Admin-Command.Info.Unknown"));
                                        placeholders.put("%items%", String.valueOf(items));
                                        placeholders.put("%database%", database);
                                        Messages.sendMessage(sender, "Admin-Command.Info.Info-Messages", placeholders);
                                    } else {
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("%player%", args[2]);
                                        Messages.sendMessage(sender, "Admin-Command.Info.Unknown-Player", placeholders);
                                    }
                                } else {
                                    int items = 0;
                                    String group = PluginControl.getMarketGroup(player).getGroupName();
                                    String database;
                                    if (PluginControl.useSplitDatabase()) {
                                        switch (PluginControl.getItemMailStorageMethod()) {
                                            case MySQL: {
                                                database = "[MySQL] [Database: " + MySQLEngine.getDatabaseName() + "] -> [Table: " + MySQLEngine.getItemMailTable() + "] -> [Colunm: UUID:" + player.getUniqueId() + "]";
                                                break;
                                            }
                                            case SQLite: {
                                                database = "[SQLite] [" + SQLiteEngine.getFilePath() + SQLiteEngine.getFileName() + "] -> [Table: " + MySQLEngine.getItemMailTable() + "] -> [Colunm: UUID:" + player.getUniqueId() + "]";
                                                break;
                                            }
                                            default: {
                                                database = new File("plugins/CrazyAuctionsPlus/Players/" + player.getUniqueId() + ".yml").getPath();
                                                break;
                                            }
                                        }
                                    } else if (PluginControl.useMySQLStorage()) {
                                        database = "[MySQL] [Database: " + MySQLEngine.getDatabaseName() + "] -> [Table: " + MySQLEngine.getItemMailTable() + "] -> [Colunm: UUID:" + player.getUniqueId() + "]";
                                    } else if (PluginControl.useSQLiteStorage()) {
                                        database = "[SQLite] [" + SQLiteEngine.getFilePath() + SQLiteEngine.getFileName() + "] -> [Table: " + MySQLEngine.getItemMailTable() + "] -> [Colunm: UUID:" + player.getUniqueId() + "]";
                                    } else {
                                        database = new File("plugins/CrazyAuctionsPlus/Players/" + player.getUniqueId() + ".yml").getPath();
                                    }
                                    for (MarketGoods mg : GlobalMarket.getMarket().getItems()) {
                                        if (mg.getItemOwner().getUUID().equals(player.getUniqueId())) {
                                            items++;
                                        }
                                    }
                                    Map<String, String> placeholders = new HashMap();
                                    placeholders.put("%player%", player.getName());
                                    placeholders.put("%group%", group);
                                    placeholders.put("%items%", String.valueOf(items));
                                    placeholders.put("%database%", database);
                                    Messages.sendMessage(sender, "Admin-Command.Info.Info-Messages", placeholders);
                                    return true;
                                }
                            }
                        } else if (args[1].equalsIgnoreCase("synchronize")) {
                            if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Synchronize", true)) return true;
                            if (FileManager.isSyncing()) {
                                Messages.sendMessage(sender, "Admin-Command.Synchronize.Syncing");
                                return true;
                            }
                            Messages.sendMessage(sender, "Admin-Command.Synchronize.Starting");
                            FileManager.synchronize(sender);
                        } else if (args[1].equalsIgnoreCase("printstacktrace")) {
                            if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.PrintStackTrace", true)) return true;
                            if (PluginControl.stackTraceVisible.containsKey(sender)) {
                                if (PluginControl.stackTraceVisible.get(sender)) {
                                    PluginControl.stackTraceVisible.put(sender, false);
                                    Messages.sendMessage(sender, "Admin-Command.PrintStackTrace.Turn-Off");
                                } else {
                                    PluginControl.stackTraceVisible.put(sender, true);
                                    Messages.sendMessage(sender, "Admin-Command.PrintStackTrace.Turn-On");
                                }
                            } else {
                                PluginControl.stackTraceVisible.put(sender, true);
                                Messages.sendMessage(sender, "Admin-Command.PrintStackTrace.Turn-On");
                            }
                        } else if (args[1].equalsIgnoreCase("market")) {
                            if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Market", true)) return true;
                            if (args.length == 2) {
                                Messages.sendMessage(sender, "Admin-Command.Market.Help");
                                return true;
                            } else if (args.length >= 3) {
                                GlobalMarket market = GlobalMarket.getMarket();
                                if (args[2].equalsIgnoreCase("confirm")) {
                                    if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Market.SubCommands.Confirm", true)) return true;
                                    if (marketConfirm.containsKey(sender)) {
                                        Bukkit.dispatchCommand(sender, marketConfirm.get(sender));
                                        return true;
                                    } else {
                                        Messages.sendMessage(sender, "Admin-Command.Market.Confirm.Invalid");
                                        return true;
                                    }
                                } else if (args[2].equalsIgnoreCase("list")) {
                                    if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Market.SubCommands.List", true)) return true;
                                    if (args.length == 3) {
                                        List<MarketGoods> list = market.getItems();
                                        if (list.isEmpty()) {
                                            Messages.sendMessage(sender, "Admin-Command.Market.List.Empty");
                                            return true;
                                        }
                                        int page = 1;
                                        int nosp = 9;
                                        try {
                                            nosp = Integer.valueOf(Messages.getValue("Admin-Command.Market.List.Number-Of-Single-Page"));
                                        } catch (NumberFormatException ex) {}
                                        StringBuilder formatList = new StringBuilder();
                                        for (int i = page * nosp - nosp;i < list.size() && i < page * nosp;i++) {
                                            String format = Messages.getValue("Admin-Command.Market.List.Format").replace("%uid%", String.valueOf(list.get(i).getUID())).replace("%money%", String.valueOf(list.get(i).getShopType().equals(ShopType.BUY) ? list.get(i).getReward() : list.get(i).getPrice())).replace("%owner%", list.get(i).getItemOwner().getName());
                                            try {
                                                format = format.replace("%item%", list.get(i).getItem().getItemMeta().hasDisplayName() ? list.get(i).getItem().getItemMeta().getDisplayName() : (String) list.get(i).getItem().getClass().getMethod("getI18NDisplayName").invoke(list.get(i).getItem()));
                                            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                                format = format.replace("%item%", list.get(i).getItem().getItemMeta().hasDisplayName() ? list.get(i).getItem().getItemMeta().getDisplayName() : list.get(i).getItem().getType().toString().toLowerCase().replace("_", " "));
                                            }
                                            formatList.append(format);
                                        }
                                        int maxpage = ((int) list.size() / nosp) + 1;
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("%format%", formatList.toString());
                                        placeholders.put("%page%", String.valueOf(page));
                                        placeholders.put("%maxpage%", String.valueOf(maxpage));
                                        placeholders.put("%nextpage%", String.valueOf(page + 1));
                                        Map<String, Boolean> visible = new HashMap();
                                        visible.put("{hasnext}", maxpage > page);
                                        Messages.sendMessage(sender, "Admin-Command.Market.List.Messages", placeholders, visible);
                                        return true;
                                    } else if (args.length >= 4) {
                                        List<MarketGoods> list = market.getItems();
                                        if (list.isEmpty()) {
                                            Messages.sendMessage(sender, "Admin-Command.Market.List.Empty");
                                            return true;
                                        }
                                        int page = 1;
                                        try {
                                            page = Integer.valueOf(args[3]);
                                        } catch (NumberFormatException ex) {}
                                        int nosp = 9;
                                        try {
                                            nosp = Integer.valueOf(Messages.getValue("Admin-Command.Market.List.Number-Of-Single-Page"));
                                        } catch (NumberFormatException ex) {}
                                        StringBuilder formatList = new StringBuilder();
                                        int maxpage = ((int) list.size() / nosp) + 1;
                                        if (maxpage < page) {
                                            page = maxpage;
                                        }
                                        for (int i = page * nosp - nosp;i < list.size() && i < page * nosp;i++) {
                                            String format = Messages.getValue("Admin-Command.Market.List.Format").replace("%uid%", String.valueOf(list.get(i).getUID())).replace("%money%", String.valueOf(list.get(i).getShopType().equals(ShopType.BUY) ? list.get(i).getReward() : list.get(i).getPrice())).replace("%owner%", list.get(i).getItemOwner().getName());
                                            try {
                                                format = format.replace("%item%", list.get(i).getItem().getItemMeta().hasDisplayName() ? list.get(i).getItem().getItemMeta().getDisplayName() : (String) list.get(i).getItem().getClass().getMethod("getI18NDisplayName").invoke(list.get(i).getItem()));
                                            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                                format = format.replace("%item%", list.get(i).getItem().getItemMeta().hasDisplayName() ? list.get(i).getItem().getItemMeta().getDisplayName() : list.get(i).getItem().getType().toString().toLowerCase().replace("_", " "));
                                            }
                                            formatList.append(format);
                                        }
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("%format%", formatList.toString());
                                        placeholders.put("%page%", String.valueOf(page));
                                        placeholders.put("%maxpage%", String.valueOf(maxpage));
                                        placeholders.put("%nextpage%", String.valueOf(page + 1));
                                        Map<String, Boolean> visible = new HashMap();
                                        visible.put("{hasnext}", maxpage > page);
                                        Messages.sendMessage(sender, "Admin-Command.Market.List.Messages", placeholders, visible);
                                        return true;
                                    }
                                } else if (args[2].equalsIgnoreCase("clear")) {
                                    if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Market.SubCommands.Clear", true)) return true;
                                    if (marketConfirm.containsKey(sender) && marketConfirm.get(sender).equalsIgnoreCase("ca admin market clear")) {
                                        market.clearGlobalMarket();
                                        marketConfirm.remove(sender);
                                        Messages.sendMessage(sender, "Admin-Command.Market.Clear");
                                        return true;
                                    } else {
                                        Messages.sendMessage(sender, "Admin-Command.Market.Confirm.Confirm");
                                        marketConfirm.put(sender, "ca admin market clear");
                                        return true;
                                    }
                                } else if (args[2].equalsIgnoreCase("repricing")) {
                                    if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Market.SubCommands.Repricing", true)) return true;
                                    if (args.length <= 4) {
                                        Messages.sendMessage(sender, "Admin-Command.Market.Repricing.Help");
                                        return true;
                                    } else if (args.length >= 5) {
                                        long uid;
                                        double money;
                                        try {
                                            uid = Long.valueOf(args[3]);
                                        } catch (NumberFormatException ex) {
                                            Map<String, String> placeholders = new HashMap();
                                            placeholders.put("%arg%", args[3]);
                                            Messages.sendMessage(sender, "Admin-Command.Market.Repricing.Not-A-Valid-Number", placeholders);
                                            return true;
                                        }
                                        try {
                                            money = Double.valueOf(args[4]);
                                        } catch (NumberFormatException ex) {
                                            Map<String, String> placeholders = new HashMap();
                                            placeholders.put("%arg%", args[4]);
                                            Messages.sendMessage(sender, "Admin-Command.Market.Repricing.Not-A-Valid-Number", placeholders);
                                            return true;
                                        }
                                        MarketGoods goods = market.getMarketGoods(uid);
                                        if (goods == null) {
                                            Map<String, String> placeholders = new HashMap();
                                            placeholders.put("%uid%", String.valueOf(uid));
                                            Messages.sendMessage(sender, "Admin-Command.Market.Repricing.Not-Exist", placeholders);
                                            return true;
                                        }
                                        Map<String, String> placeholders = new HashMap();
                                        try {
                                            placeholders.put("%item%", goods.getItem().getItemMeta().hasDisplayName() ? goods.getItem().getItemMeta().getDisplayName() : (String) goods.getItem().getClass().getMethod("getI18NDisplayName").invoke(goods.getItem()));
                                        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                            placeholders.put("%item%", goods.getItem().getItemMeta().hasDisplayName() ? goods.getItem().getItemMeta().getDisplayName() : (String) goods.getItem().getType().toString().toLowerCase().replace("_", " "));
                                        }
                                        placeholders.put("%uid%", String.valueOf(uid));
                                        placeholders.put("%money%", String.valueOf(money));
                                        if (goods.getShopType().equals(ShopType.BUY)) {
                                            goods.setPrice(money);
                                        } else {
                                            goods.setPrice(money);
                                        }
                                        Messages.sendMessage(sender, "Admin-Command.Market.Repricing.Succeeded", placeholders);
                                        return true;
                                    }
                                } else if (args[2].equalsIgnoreCase("delete")) {
                                    if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Market.SubCommands.Delete", true)) return true;
                                    if (args.length == 3) {
                                        Messages.sendMessage(sender, "Admin-Command.Market.Delete.Help");
                                        return true;
                                    } else if (args.length >= 4) {
                                        long uid;
                                        try {
                                            uid = Long.valueOf(args[3]);
                                        } catch (NumberFormatException ex) {
                                            Map<String, String> placeholders = new HashMap();
                                            placeholders.put("%arg%", args[3]);
                                            Messages.sendMessage(sender, "Admin-Command.Market.Delete.Not-A-Valid-Number", placeholders);
                                            return true;
                                        }
                                        MarketGoods goods = market.getMarketGoods(uid);
                                        if (goods == null) {
                                            Map<String, String> placeholders = new HashMap();
                                            placeholders.put("%uid%", String.valueOf(uid));
                                            Messages.sendMessage(sender, "Admin-Command.Market.Delete.Not-Exist", placeholders);
                                            return true;
                                        }
                                        Map<String, String> placeholders = new HashMap();
                                        try {
                                            placeholders.put("%item%", goods.getItem().getItemMeta().hasDisplayName() ? goods.getItem().getItemMeta().getDisplayName() : (String) goods.getItem().getClass().getMethod("getI18NDisplayName").invoke(goods.getItem()));
                                        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                            placeholders.put("%item%", goods.getItem().getItemMeta().hasDisplayName() ? goods.getItem().getItemMeta().getDisplayName() : (String) goods.getItem().getType().toString().toLowerCase().replace("_", " "));
                                        }
                                        placeholders.put("%uid%", String.valueOf(uid));
                                        market.removeGoods(uid);
                                        Messages.sendMessage(sender, "Admin-Command.Market.Delete.Succeeded", placeholders);
                                    }
                                } else if (args[2].equalsIgnoreCase("download")) {
                                    if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Market.SubCommands.Download", true)) return true;
                                    if (PluginControl.getMarketStorageMethod().equals(StorageMethod.YAML)) {
                                        Messages.sendMessage(sender, "Admin-Command.Market.Download.Only-Database-Mode");
                                        return true;
                                    }
                                    if (marketConfirm.containsKey(sender) && marketConfirm.get(sender).equalsIgnoreCase("ca admin market download")) {
                                        String fileName = Files.CONFIG.getFile().getString("Settings.Upload.Market").replace("%date%", new SimpleDateFormat("yyyy-MM-hh-HH-mm-ss").format(new Date()));
                                        File file = new File(fileName);
                                        if (file.getParent() != null) {
                                            new File(file.getParent()).mkdirs();
                                        }
                                        if (!file.exists()) {
                                            try {
                                                file.createNewFile();
                                            } catch (IOException ex) {
                                                PluginControl.printStackTrace(ex);
                                            }
                                        }
                                        try (OutputStream out = new FileOutputStream(file)) {
                                            out.write(market.getYamlData().saveToString().getBytes());
                                        } catch (IOException ex) {
                                            Map<String, String> placeholders = new HashMap();
                                            placeholders.put("%error%", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
                                            Messages.sendMessage(sender, "Admin-Command.Market.Download.Failed", placeholders);
                                            marketConfirm.remove(sender);
                                            PluginControl.printStackTrace(ex);
                                            return true;
                                        }
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("%path%", fileName);
                                        Messages.sendMessage(sender, "Admin-Command.Market.Download.Succeeded", placeholders);
                                        marketConfirm.remove(sender);
                                        return true;
                                    } else {
                                        Messages.sendMessage(sender, "Admin-Command.Market.Confirm.Confirm");
                                        marketConfirm.put(sender, "ca admin market download");
                                        return true;
                                    }
                                } else if (args[2].equalsIgnoreCase("upload")) {
                                    if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Market.SubCommands.Upload", true)) return true;
                                    if (PluginControl.getMarketStorageMethod().equals(StorageMethod.YAML)) {
                                        Messages.sendMessage(sender, "Admin-Command.Market.Upload.Only-Database-Mode");
                                        return true;
                                    }
                                    if (marketConfirm.containsKey(sender) && marketConfirm.get(sender).equalsIgnoreCase("ca admin market upload")) {
                                        String fileName = Files.CONFIG.getFile().getString("Settings.Upload.Market").replace("%date%", new SimpleDateFormat("yyyy-MM-hh-HH-mm-ss").format(new Date()));
                                        File file = new File(fileName);
                                        if (!file.exists()) {
                                            Map<String, String> placeholders = new HashMap();
                                            placeholders.put("%file%", fileName);
                                            Messages.sendMessage(sender, "Admin-Command.Market.Upload.File-Not-Exist", placeholders);
                                            marketConfirm.remove(sender);
                                            return true;
                                        }
                                        FileConfiguration config = new YamlConfiguration();
                                        try (Reader reader = new InputStreamReader(new FileInputStream(file), "UTF-8")) {
                                            config.load(reader);
                                        } catch (IOException | InvalidConfigurationException ex) {
                                            Map<String, String> placeholders = new HashMap();
                                            placeholders.put("%error%", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
                                            Messages.sendMessage(sender, "Admin-Command.Market.Upload.Failed", placeholders);
                                            marketConfirm.remove(sender);
                                            PluginControl.printStackTrace(ex);
                                            return true;
                                        }
                                        switch (PluginControl.getMarketStorageMethod()) {
                                            case MySQL: {
                                                MySQLEngine engine = MySQLEngine.getInstance();
                                                try {
                                                    PreparedStatement statement = engine.getConnection().prepareStatement("UPDATE " + MySQLEngine.getDatabaseName() + "." + MySQLEngine.getMarketTable() + " SET "
                                                            + "YamlMarket = ?");
                                                    statement.setString(1, config.saveToString());
                                                    statement.executeUpdate();
                                                } catch (SQLException ex) {
                                                    Map<String, String> placeholders = new HashMap();
                                                    placeholders.put("%error%", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
                                                    Messages.sendMessage(sender, "Admin-Command.Market.Upload.Failed", placeholders);
                                                    marketConfirm.remove(sender);
                                                    PluginControl.printStackTrace(ex);
                                                    return true;
                                                }
                                                break;
                                            }
                                            case SQLite: {
                                                SQLiteEngine engine = SQLiteEngine.getInstance();
                                                try {
                                                    PreparedStatement statement = engine.getConnection().prepareStatement("UPDATE " + SQLiteEngine.getMarketTable() + " SET "
                                                            + "YamlMarket = ?");
                                                    statement.setString(1, config.saveToString());
                                                    statement.executeUpdate();
                                                } catch (SQLException ex) {
                                                    Map<String, String> placeholders = new HashMap();
                                                    placeholders.put("%error%", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
                                                    Messages.sendMessage(sender, "Admin-Command.Market.Upload.Failed", placeholders);
                                                    marketConfirm.remove(sender);
                                                    PluginControl.printStackTrace(ex);
                                                    return true;
                                                }
                                                break;
                                            }
                                        }
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("%file%", fileName);
                                        Messages.sendMessage(sender, "Admin-Command.Market.Upload.Succeeded", placeholders);
                                        marketConfirm.remove(sender);
                                        return true;
                                    } else {
                                        Messages.sendMessage(sender, "Admin-Command.Market.Confirm.Confirm");
                                        marketConfirm.put(sender, "ca admin market upload");
                                        return true;
                                    }
                                }
                            } else {
                                Messages.sendMessage(sender, "Admin-Command.Market.Help");
                                return true;
                            }
                        } else if (args[1].equalsIgnoreCase("player")) {
                            if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Player", true)) return true;
                            if (args.length == 2) {
                                Messages.sendMessage(sender, "Admin-Command.Player.Help");
                                return true;
                            } else if (args.length == 3) {
                                if (args[2].equalsIgnoreCase("confirm")) {
                                    if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Player.SubCommands.Confirm", true)) return true;
                                    if (itemMailConfirm.containsKey(sender)) {
                                        Bukkit.dispatchCommand(sender, itemMailConfirm.get(sender));
                                        return true;
                                    } else {
                                        Messages.sendMessage(sender, "Admin-Command.Player.Confirm.Invalid");
                                        return true;
                                    }
                                } else {
                                    Messages.sendMessage(sender, "Admin-Command.Player.Help");
                                    return true;
                                }
                            } else if (args.length >= 4) {
                                if (args[3].equalsIgnoreCase("list")) {
                                    if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Player.SubCommands.List", true)) return true;
                                    Player player = Bukkit.getPlayer(args[2]);
                                    UUID uuid;
                                    String name;
                                    if (player != null) {
                                        uuid = player.getUniqueId();
                                        name = player.getName();
                                    } else {
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("%player%", args[2]);
                                        Messages.sendMessage(sender, "Admin-Command.Player.List.Please-Wait", placeholders);
                                        OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(args[2]);
                                        if (offlineplayer != null) {
                                            uuid = offlineplayer.getUniqueId();
                                            name = offlineplayer.getName();
                                        } else {
                                            Messages.sendMessage(sender, "Admin-Command.Player.List.Player-Not-Exist", placeholders);
                                            return true;
                                        }
                                    }
                                    if (args.length == 4) {
                                        List<ItemMail> list = Storage.getPlayer(uuid).getMailBox();
                                        if (list.isEmpty()) {
                                            Messages.sendMessage(sender, "Admin-Command.Player.List.Empty");
                                            return true;
                                        }
                                        int page = 1;
                                        int nosp = 9;
                                        try {
                                            nosp = Integer.valueOf(Messages.getValue("Admin-Command.Player.List.Number-Of-Single-Page"));
                                        } catch (NumberFormatException ex) {}
                                        StringBuilder formatList = new StringBuilder();
                                        for (int i = page * nosp - nosp;i < list.size() && i < page * nosp;i++) {
                                            String format = Messages.getValue("Admin-Command.Player.List.Format").replace("%uid%", String.valueOf(list.get(i).getUID()));
                                            try {
                                                format = format.replace("%item%", list.get(i).getItem().getItemMeta().hasDisplayName() ? list.get(i).getItem().getItemMeta().getDisplayName() : (String) list.get(i).getItem().getClass().getMethod("getI18NDisplayName").invoke(list.get(i).getItem()));
                                            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                                format = format.replace("%item%", list.get(i).getItem().getItemMeta().hasDisplayName() ? list.get(i).getItem().getItemMeta().getDisplayName() : list.get(i).getItem().getType().toString().toLowerCase().replace("_", " "));
                                            }
                                            formatList.append(format);
                                        }
                                        int maxpage = ((int) list.size() / nosp) + 1;
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("%player%", name);
                                        placeholders.put("%format%", formatList.toString());
                                        placeholders.put("%page%", String.valueOf(page));
                                        placeholders.put("%maxpage%", String.valueOf(maxpage));
                                        placeholders.put("%nextpage%", String.valueOf(page + 1));
                                        Map<String, Boolean> visible = new HashMap();
                                        visible.put("{hasnext}", maxpage > page);
                                        Messages.sendMessage(sender, "Admin-Command.Player.List.Messages", placeholders, visible);
                                        return true;
                                    } else if (args.length >= 5) {
                                        List<ItemMail> list = Storage.getPlayer(uuid).getMailBox();
                                        if (list.isEmpty()) {
                                            Messages.sendMessage(sender, "Admin-Command.Player.List.Empty");
                                            return true;
                                        }
                                        int page = 1;
                                        try {
                                            page = Integer.valueOf(args[4]);
                                        } catch (NumberFormatException ex) {}
                                        int nosp = 9;
                                        try {
                                            nosp = Integer.valueOf(Messages.getValue("Admin-Command.Player.List.Number-Of-Single-Page"));
                                        } catch (NumberFormatException ex) {}
                                        StringBuilder formatList = new StringBuilder();
                                        int maxpage = ((int) list.size() / nosp) + 1;
                                        if (maxpage < page) {
                                            page = maxpage;
                                        }
                                        for (int i = page * nosp - nosp;i < list.size() && i < page * nosp;i++) {
                                            String format = Messages.getValue("Admin-Command.Player.List.Format").replace("%uid%", String.valueOf(list.get(i).getUID()));
                                            try {
                                                format = format.replace("%item%", list.get(i).getItem().getItemMeta().hasDisplayName() ? list.get(i).getItem().getItemMeta().getDisplayName() : (String) list.get(i).getItem().getClass().getMethod("getI18NDisplayName").invoke(list.get(i).getItem()));
                                            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                                format = format.replace("%item%", list.get(i).getItem().getItemMeta().hasDisplayName() ? list.get(i).getItem().getItemMeta().getDisplayName() : list.get(i).getItem().getType().toString().toLowerCase().replace("_", " "));
                                            }
                                            formatList.append(format);
                                        }
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("%player%", name);
                                        placeholders.put("%format%", formatList.toString());
                                        placeholders.put("%page%", String.valueOf(page));
                                        placeholders.put("%maxpage%", String.valueOf(maxpage));
                                        placeholders.put("%nextpage%", String.valueOf(page + 1));
                                        Map<String, Boolean> visible = new HashMap();
                                        visible.put("{hasnext}", maxpage > page);
                                        Messages.sendMessage(sender, "Admin-Command.Player.List.Messages", placeholders, visible);
                                        return true;
                                    }
                                } else if (args[3].equalsIgnoreCase("clear")) {
                                    if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Player.SubCommands.Clear", true)) return true;
                                    if (args.length == 4) {
                                        Messages.sendMessage(sender, "Admin-Command.Player.Clear.Help");
                                        return true;
                                    }
                                    Player player = Bukkit.getPlayer(args[2]);
                                    UUID uuid;
                                    String name;
                                    if (player != null) {
                                        uuid = player.getUniqueId();
                                        name = player.getName();
                                    } else {
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("%player%", args[2]);
                                        Messages.sendMessage(sender, "Admin-Command.Player.Clear.Please-Wait", placeholders);
                                        OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(args[2]);
                                        if (offlineplayer != null) {
                                            uuid = offlineplayer.getUniqueId();
                                            name = offlineplayer.getName();
                                        } else {
                                            Messages.sendMessage(sender, "Admin-Command.Player.Clear.Player-Not-Exist", placeholders);
                                            return true;
                                        }
                                    }
                                    if (args[4].equalsIgnoreCase("market")) {
                                        GlobalMarket market = GlobalMarket.getMarket();
                                        if (itemMailConfirm.containsKey(sender) && itemMailConfirm.get(sender).equalsIgnoreCase("ca admin player " + name + " clear market")) {
                                            List<MarketGoods> marketGoods = market.getItems();
                                            for (int i = marketGoods.size() - 1;i > -1;i--) {
                                                if (marketGoods.get(i).getItemOwner().getUUID().equals(uuid)) {
                                                    marketGoods.remove(i);
                                                }
                                            }
                                            market.saveData();
                                            itemMailConfirm.remove(sender);
                                            Map<String, String> placeholders = new HashMap();
                                            placeholders.put("%player%", name);
                                            Messages.sendMessage(sender, "Admin-Command.Player.Clear.Market", placeholders);
                                            return true;
                                        } else {
                                            Messages.sendMessage(sender, "Admin-Command.Player.Confirm.Confirm");
                                            itemMailConfirm.put(sender, "ca admin player " + name + " clear market");
                                            return true;
                                        }
                                    } else if (args[4].equalsIgnoreCase("mail")) {
                                        if (itemMailConfirm.containsKey(sender) && itemMailConfirm.get(sender).equalsIgnoreCase("ca admin player " + name + " clear mail")) {
                                            Storage.getPlayer(uuid).clearMailBox();
                                            itemMailConfirm.remove(sender);
                                            Map<String, String> placeholders = new HashMap();
                                            placeholders.put("%player%", name);
                                            Messages.sendMessage(sender, "Admin-Command.Player.Clear.ItemMail", placeholders);
                                            return true;
                                        } else {
                                            Messages.sendMessage(sender, "Admin-Command.Player.Confirm.Confirm");
                                            itemMailConfirm.put(sender, "ca admin player " + name + " clear mail");
                                            return true;
                                        }
                                    } else {
                                        Messages.sendMessage(sender, "Admin-Command.Player.Clear.Help");
                                        return true;
                                    }
                                } else if (args[3].equalsIgnoreCase("delete")) {
                                    if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Player.SubCommands.Delete", true)) return true;
                                    if (args.length == 4) {
                                        Messages.sendMessage(sender, "Admin-Command.Player.Delete.Help");
                                        return true;
                                    } else if (args.length >= 5) {
                                        Player player = Bukkit.getPlayer(args[2]);
                                        UUID uuid;
                                        String name;
                                        if (player != null) {
                                            uuid = player.getUniqueId();
                                            name = player.getName();
                                        } else {
                                            Map<String, String> placeholders = new HashMap();
                                            placeholders.put("%player%", args[2]);
                                            Messages.sendMessage(sender, "Admin-Command.Player.Delete.Please-Wait", placeholders);
                                            OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(args[2]);
                                            if (offlineplayer != null) {
                                                uuid = offlineplayer.getUniqueId();
                                                name = offlineplayer.getName();
                                            } else {
                                                Messages.sendMessage(sender, "Admin-Command.Player.Delete.Player-Not-Exist", placeholders);
                                                return true;
                                            }
                                        }
                                        Storage playerdata = Storage.getPlayer(uuid);
                                        long uid;
                                        try {
                                            uid = Long.valueOf(args[4]);
                                        } catch (NumberFormatException ex) {
                                            Map<String, String> placeholders = new HashMap();
                                            placeholders.put("%arg%", args[4]);
                                            Messages.sendMessage(sender, "Admin-Command.Player.Delete.Not-A-Valid-Number", placeholders);
                                            return true;
                                        }
                                        ItemMail mail = playerdata.getMail(uid);
                                        if (mail == null) {
                                            Map<String, String> placeholders = new HashMap();
                                            placeholders.put("%uid%", String.valueOf(uid));
                                            Messages.sendMessage(sender, "Admin-Command.Player.Delete.Not-Exist", placeholders);
                                            return true;
                                        }
                                        Map<String, String> placeholders = new HashMap();
                                        try {
                                            placeholders.put("%item%", mail.getItem().getItemMeta().hasDisplayName() ? mail.getItem().getItemMeta().getDisplayName() : (String) mail.getItem().getClass().getMethod("getI18NDisplayName").invoke(mail.getItem()));
                                        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                            placeholders.put("%item%", mail.getItem().getItemMeta().hasDisplayName() ? mail.getItem().getItemMeta().getDisplayName() : (String) mail.getItem().getType().toString().toLowerCase().replace("_", " "));
                                        }
                                        placeholders.put("%uid%", String.valueOf(uid));
                                        placeholders.put("%player%", name);
                                        playerdata.removeItem(mail);
                                        Messages.sendMessage(sender, "Admin-Command.Player.Delete.Succeeded", placeholders);
                                    }
                                } else if (args[3].equalsIgnoreCase("view")) {
                                    if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Player.SubCommands.View", true)) return true;
                                    if (!(sender instanceof Player)) {
                                        Messages.sendMessage(sender, "Admin-Command.Player.View.Player-Only");
                                        return true;
                                    }
                                    Player player = Bukkit.getPlayer(args[2]);
                                    UUID uuid;
                                    String name;
                                    if (player != null) {
                                        uuid = player.getUniqueId();
                                        name = player.getName();
                                    } else {
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("%player%", args[2]);
                                        Messages.sendMessage(sender, "Admin-Command.Player.View.Please-Wait", placeholders);
                                        OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(args[2]);
                                        if (offlineplayer != null) {
                                            uuid = offlineplayer.getUniqueId();
                                            name = offlineplayer.getName();
                                        } else {
                                            Messages.sendMessage(sender, "Admin-Command.Player.View.Player-Not-Exist", placeholders);
                                            return true;
                                        }
                                    }
                                    GUI.openPlayersMail((Player) sender, 1, uuid);
                                    Map<String, String> placeholders = new HashMap();
                                    placeholders.put("%player%", name);
                                    Messages.sendMessage(sender, "Admin-Command.Player.View.Succeeded", placeholders);
                                    return true;
                                } else if (args[3].equalsIgnoreCase("download")) {
                                    if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Player.SubCommands.Download", true)) return true;
                                    Player player = Bukkit.getPlayer(args[2]);
                                    UUID uuid;
                                    String name;
                                    if (player != null) {
                                        uuid = player.getUniqueId();
                                        name = player.getName();
                                    } else {
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("%player%", args[2]);
                                        Messages.sendMessage(sender, "Admin-Command.Player.Download.Please-Wait", placeholders);
                                        OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(args[2]);
                                        if (offlineplayer != null) {
                                            uuid = offlineplayer.getUniqueId();
                                            name = offlineplayer.getName();
                                        } else {
                                            Messages.sendMessage(sender, "Admin-Command.Player.Download.Player-Not-Exist", placeholders);
                                            return true;
                                        }
                                    }
                                    if (PluginControl.getItemMailStorageMethod().equals(StorageMethod.YAML)) {
                                        Messages.sendMessage(sender, "Admin-Command.Player.Download.Only-Database-Mode");
                                        return true;
                                    }
                                    if (itemMailConfirm.containsKey(sender) && itemMailConfirm.get(sender).equalsIgnoreCase("ca admin player " + name + " download")) {
                                        String fileName = Files.CONFIG.getFile().getString("Settings.Download.PlayerData").replace("%player%", name).replace("%uuid%", uuid.toString()).replace("%date%", new SimpleDateFormat("yyyy-MM-hh-HH-mm-ss").format(new Date()));
                                        File file = new File(fileName);
                                        if (file.getParent() != null) {
                                            new File(file.getParent()).mkdirs();
                                        }
                                        if (!file.exists()) {
                                            try {
                                                file.createNewFile();
                                            } catch (IOException ex) {
                                                PluginControl.printStackTrace(ex);
                                            }
                                        }
                                        try (OutputStream out = new FileOutputStream(file)) {
                                            out.write(Storage.getPlayer(uuid).getYamlData().saveToString().getBytes());
                                        } catch (IOException ex) {
                                            Map<String, String> placeholders = new HashMap();
                                            placeholders.put("%error%", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
                                            Messages.sendMessage(sender, "Admin-Command.Player.Download.Failed", placeholders);
                                            itemMailConfirm.remove(sender);
                                            PluginControl.printStackTrace(ex);
                                            return true;
                                        }
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("%path%", fileName);
                                        placeholders.put("%player%", name);
                                        Messages.sendMessage(sender, "Admin-Command.Player.Download.Succeeded", placeholders);
                                        itemMailConfirm.remove(sender);
                                        return true;
                                    } else {
                                        Messages.sendMessage(sender, "Admin-Command.Player.Confirm.Confirm");
                                        itemMailConfirm.put(sender, "ca admin player " + name + " download");
                                        return true;
                                    }
                                } else if (args[3].equalsIgnoreCase("upload")) {
                                    if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Player.SubCommands.Upload", true)) return true;
                                    Player player = Bukkit.getPlayer(args[2]);
                                    UUID uuid;
                                    String name;
                                    if (player != null) {
                                        uuid = player.getUniqueId();
                                        name = player.getName();
                                    } else {
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("%player%", args[2]);
                                        Messages.sendMessage(sender, "Admin-Command.Player.Upload.Please-Wait", placeholders);
                                        OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(args[2]);
                                        if (offlineplayer != null) {
                                            uuid = offlineplayer.getUniqueId();
                                            name = offlineplayer.getName();
                                        } else {
                                            Messages.sendMessage(sender, "Admin-Command.Player.Upload.Player-Not-Exist", placeholders);
                                            return true;
                                        }
                                    }
                                    if (PluginControl.getItemMailStorageMethod().equals(StorageMethod.YAML)) {
                                        Messages.sendMessage(sender, "Admin-Command.Player.Download.Only-Database-Mode");
                                        return true;
                                    }
                                    if (itemMailConfirm.containsKey(sender) && itemMailConfirm.get(sender).equalsIgnoreCase("ca admin player " + name + " upload")) {
                                        String fileName = Files.CONFIG.getFile().getString("Settings.Upload.PlayerData").replace("%player%", name).replace("%uuid%", uuid.toString()).replace("%date%", new SimpleDateFormat("yyyy-MM-hh-HH-mm-ss").format(new Date()));
                                        File file = new File(fileName);
                                        if (!file.exists()) {
                                            Map<String, String> placeholders = new HashMap();
                                            placeholders.put("%file%", fileName);
                                            Messages.sendMessage(sender, "Admin-Command.Player.Upload.File-Not-Exist", placeholders);
                                            itemMailConfirm.remove(sender);
                                            return true;
                                        }
                                        FileConfiguration config = new YamlConfiguration();
                                        try (Reader reader = new InputStreamReader(new FileInputStream(file), "UTF-8")) {
                                            config.load(reader);
                                        } catch (IOException | InvalidConfigurationException ex) {
                                            Map<String, String> placeholders = new HashMap();
                                            placeholders.put("%error%", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
                                            Messages.sendMessage(sender, "Admin-Command.Player.Upload.Failed", placeholders);
                                            itemMailConfirm.remove(sender);
                                            PluginControl.printStackTrace(ex);
                                            return true;
                                        }
                                        switch (PluginControl.getMarketStorageMethod()) {
                                            case MySQL: {
                                                MySQLEngine engine = MySQLEngine.getInstance();
                                                try {
                                                    PreparedStatement statement = engine.getConnection().prepareStatement("UPDATE " + MySQLEngine.getDatabaseName() + "." + MySQLEngine.getItemMailTable() + " SET "
                                                            + "YamlData = ? WHERE UUID = ?");
                                                    statement.setString(1, config.saveToString());
                                                    statement.setString(2, uuid.toString());
                                                    statement.executeUpdate();
                                                } catch (SQLException ex) {
                                                    Map<String, String> placeholders = new HashMap();
                                                    placeholders.put("%error%", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
                                                    Messages.sendMessage(sender, "Admin-Command.Player.Upload.Failed", placeholders);
                                                    itemMailConfirm.remove(sender);
                                                    PluginControl.printStackTrace(ex);
                                                    return true;
                                                }
                                                break;
                                            }
                                            case SQLite: {
                                                SQLiteEngine engine = SQLiteEngine.getInstance();
                                                try {
                                                    PreparedStatement statement = engine.getConnection().prepareStatement("UPDATE " + SQLiteEngine.getItemMailTable() + " SET "
                                                            + "YamlMarket = ? WHERE UUID = ?");
                                                    statement.setString(1, config.saveToString());
                                                    statement.setString(2, uuid.toString());
                                                    statement.executeUpdate();
                                                } catch (SQLException ex) {
                                                    Map<String, String> placeholders = new HashMap();
                                                    placeholders.put("%error%", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
                                                    Messages.sendMessage(sender, "Admin-Command.Player.Upload.Failed", placeholders);
                                                    itemMailConfirm.remove(sender);
                                                    PluginControl.printStackTrace(ex);
                                                    return true;
                                                }
                                                break;
                                            }
                                        }
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("%file%", fileName);
                                        placeholders.put("%player%", name);
                                        Messages.sendMessage(sender, "Admin-Command.Player.Upload.Succeeded", placeholders);
                                        itemMailConfirm.remove(sender);
                                        return true;
                                    } else {
                                        Messages.sendMessage(sender, "Admin-Command.Player.Confirm.Confirm");
                                        itemMailConfirm.put(sender, "ca admin player " + name + " upload");
                                        return true;
                                    }
                                }
                            }
                        } else if (args[1].equalsIgnoreCase("itemcollection")) {
                            if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.ItemCollection", true)) return true;
                            if (args.length == 2) {
                                Messages.sendMessage(sender, "Admin-Command.ItemCollection.Help");
                            } else if (args.length >= 3) {
                                if (args[2].equalsIgnoreCase("add")) {
                                    if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.ItemCollection.SubCommands.Add", true)) return true;
                                    if (args.length <= 3) {
                                        Messages.sendMessage(sender, "Admin-Command.ItemCollection.Add.Help");
                                        return true;
                                    } else {
                                        if (sender instanceof Player) {
                                            Player player = (Player) sender;
                                            if (player.getItemInHand() == null) {
                                                Messages.sendMessage(sender, "Admin-Command.ItemCollection.Add.Doesnt-Have-Item-In-Hand");
                                                return true;
                                            }
                                            if (ItemCollection.addItem(player.getItemInHand(), args[3])) {
                                                Map<String, String> placeholders = new HashMap();
                                                placeholders.put("%item%", args[3]);
                                                Messages.sendMessage(sender, "Admin-Command.ItemCollection.Add.Successfully", placeholders);
                                            } else {
                                                Messages.sendMessage(sender, "Admin-Command.ItemCollection.Add.Already-Exist");
                                            }
                                        } else {
                                            Messages.sendMessage(sender, "Players-Only");
                                            return true;
                                        }
                                    }
                                } else if (args[2].equalsIgnoreCase("delete") || args[2].equalsIgnoreCase("remove")) {
                                    if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.ItemCollection.SubCommands.Delete", true)) return true;
                                    if (args.length <= 3) {
                                        Messages.sendMessage(sender, "Admin-Command.ItemCollection.Delete.Help");
                                        return true;
                                    } else {
                                        try {
                                            long uid = Long.valueOf(args[3]);
                                            for (ItemCollection ic : ItemCollection.getCollection()) {
                                                if (ic.getUID() == uid) {
                                                    Map<String, String> placeholders = new HashMap();
                                                    placeholders.put("%item%", ic.getDisplayName());
                                                    ItemCollection.deleteItem(uid);
                                                    Messages.sendMessage(sender, "Admin-Command.ItemCollection.Delete.Successfully", placeholders);
                                                    return true;
                                                }
                                            }
                                            Map<String, String> placeholders = new HashMap();
                                            placeholders.put("%item%", args[3]);
                                            Messages.sendMessage(sender, "Admin-Command.ItemCollection.Delete.Item-Not-Exist", placeholders);
                                        } catch (NumberFormatException ex) {
                                            String displayName = args[3];
                                            for (ItemCollection ic : ItemCollection.getCollection()) {
                                                if (ic.getDisplayName().equalsIgnoreCase(displayName)) {
                                                    Map<String, String> placeholders = new HashMap();
                                                    placeholders.put("%item%", ic.getDisplayName());
                                                    ItemCollection.deleteItem(displayName);
                                                    Messages.sendMessage(sender, "Admin-Command.ItemCollection.Delete.Successfully", placeholders);
                                                    return true;
                                                }
                                            }
                                            Map<String, String> placeholders = new HashMap();
                                            placeholders.put("%item%", args[3]);
                                            Messages.sendMessage(sender, "Admin-Command.ItemCollection.Delete.Item-Not-Exist", placeholders);
                                        }
                                    }
                                } else if (args[2].equalsIgnoreCase("list")) {
                                    if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.ItemCollection.SubCommands.List", true)) return true;
                                    if (ItemCollection.getCollection().isEmpty()) {
                                        Messages.sendMessage(sender, "Admin-Command.ItemCollection.List.Empty-Collection");
                                        return true;
                                    } else {
                                        String format = Messages.getValue("Admin-Command.ItemCollection.List.List-Format");
                                        List<String> list = new ArrayList();
                                        for (ItemCollection collection : ItemCollection.getCollection()) {
                                            list.add(format.replace("%uid%", String.valueOf(collection.getUID())).replace("%item%", collection.getDisplayName()));
                                        }
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("%list%", list.toString().substring(1, list.toString().length() - 1));
                                        Messages.sendMessage(sender, "Admin-Command.ItemCollection.List.Messages", placeholders);
                                    }
                                } else if (args[2].equalsIgnoreCase("give")) {
                                    if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.ItemCollection.SubCommands.Give", true)) return true;
                                    if (args.length == 3) {
                                        Messages.sendMessage(sender, "Admin-Command.ItemCollection.Give.Help");
                                        return true;
                                    } else if (args.length == 4) {
                                        if (sender instanceof Player) {
                                            Player player = (Player) sender;
                                            try {
                                                long uid = Long.valueOf(args[3]);
                                                for (ItemCollection ic : ItemCollection.getCollection()) {
                                                    if (ic.getUID() == uid) {
                                                        Map<String, String> placeholders = new HashMap();
                                                        placeholders.put("%item%", ic.getDisplayName());
                                                        placeholders.put("%player%", player.getName());
                                                        player.getInventory().addItem(ic.getItem());
                                                        Messages.sendMessage(sender, "Admin-Command.ItemCollection.Give.Successfully", placeholders);
                                                        return true;
                                                    }
                                                }
                                                Map<String, String> placeholders = new HashMap();
                                                placeholders.put("%item%", args[3]);
                                                Messages.sendMessage(sender, "Admin-Command.ItemCollection.Give.Item-Not-Exist", placeholders);
                                            } catch (NumberFormatException ex) {
                                                String displayName = args[3];
                                                for (ItemCollection ic : ItemCollection.getCollection()) {
                                                    if (ic.getDisplayName().equalsIgnoreCase(displayName)) {
                                                        Map<String, String> placeholders = new HashMap();
                                                        placeholders.put("%item%", ic.getDisplayName());
                                                        placeholders.put("%player%", player.getName());
                                                        player.getInventory().addItem(ic.getItem());
                                                        Messages.sendMessage(sender, "Admin-Command.ItemCollection.Give.Successfully", placeholders);
                                                        return true;
                                                    }
                                                }
                                                Map<String, String> placeholders = new HashMap();
                                                placeholders.put("%item%", args[3]);
                                                Messages.sendMessage(sender, "Admin-Command.ItemCollection.Give.Item-Not-Exist", placeholders);
                                            }
                                        } else {
                                            Messages.sendMessage(sender, "Admin-Command.ItemCollection.Give.Help");
                                            return true;
                                        }
                                    } else if (args.length >= 5) {
                                        Player player = Bukkit.getPlayer(args[4]);
                                        if (player == null) {
                                            Map<String, String> placeholders = new HashMap();
                                            placeholders.put("%player%", args[4]);
                                            Messages.sendMessage(sender, "Admin-Command.ItemCollection.Give.Player-Offline", placeholders);
                                            return true;
                                        } else {
                                            try {
                                                long uid = Long.valueOf(args[3]);
                                                for (ItemCollection ic : ItemCollection.getCollection()) {
                                                    if (ic.getUID() == uid) {
                                                        Map<String, String> placeholders = new HashMap();
                                                        placeholders.put("%item%", ic.getDisplayName());
                                                        placeholders.put("%player%", player.getName());
                                                        player.getInventory().addItem(ic.getItem());
                                                        Messages.sendMessage(sender, "Admin-Command.ItemCollection.Give.Successfully", placeholders);
                                                        return true;
                                                    }
                                                }
                                                Map<String, String> placeholders = new HashMap();
                                                placeholders.put("%item%", args[3]);
                                                Messages.sendMessage(sender, "Admin-Command.ItemCollection.Give.Item-Not-Exist", placeholders);
                                            } catch (NumberFormatException ex) {
                                                String displayName = args[3];
                                                for (ItemCollection ic : ItemCollection.getCollection()) {
                                                    if (ic.getDisplayName().equalsIgnoreCase(displayName)) {
                                                        Map<String, String> placeholders = new HashMap();
                                                        placeholders.put("%item%", ic.getDisplayName());
                                                        placeholders.put("%player%", player.getName());
                                                        player.getInventory().addItem(ic.getItem());
                                                        Messages.sendMessage(sender, "Admin-Command.ItemCollection.Give.Successfully", placeholders);
                                                        return true;
                                                    }
                                                }
                                                Map<String, String> placeholders = new HashMap();
                                                placeholders.put("%item%", args[3]);
                                                Messages.sendMessage(sender, "Admin-Command.ItemCollection.Give.Item-Not-Exist", placeholders);
                                            }
                                        }
                                    }
                                } else {
                                    Messages.sendMessage(sender, "Admin-Command.ItemCollection.Help");
                                }
                            }
                        } else {
                            Messages.sendMessage(sender, "Admin-Menu");
                        }
                        return true;
                    }
                }
                if (args[0].equalsIgnoreCase("Gui")) {
                    if (!(sender instanceof Player)) {
                        Messages.sendMessage(sender, "Players-Only");
                        return true;
                    }
                    if (!PluginControl.hasCommandPermission(sender, "Gui", true)) return true;
                    Player player = (Player) sender;
                    if (PluginControl.isWorldDisabled(player)) {
                        Messages.sendMessage(sender, "World-Disabled");
                        return true;
                    }
                    if (args.length == 1) {
                        if (Files.CONFIG.getFile().getBoolean("Settings.Category-Page-Opens-First")) {
                            GUIAction.setShopType(player, ShopType.ANY);
                            GUIAction.setCategory(player, Category.getDefaultCategory());
                            GUIAction.openCategories(player, ShopType.ANY);
                        } else {
                            GUIAction.openShop(player, ShopType.ANY, Category.getDefaultCategory(), 1);
                        }
                        return true;
                    } else if (args.length == 2) {
                        if (args[1].equalsIgnoreCase("sell")) {
                            GUIAction.openShop(player, ShopType.SELL, Category.getDefaultCategory(), 1);
                            return true;
                        } else if (args[1].equalsIgnoreCase("buy")) {
                            GUIAction.openShop(player, ShopType.BUY, Category.getDefaultCategory(), 1);
                            return true;
                        } else if (args[1].equalsIgnoreCase("bid")) {
                            GUIAction.openShop(player, ShopType.BID, Category.getDefaultCategory(), 1);
                            return true;
                        } else {
                            GUIAction.openShop(player, ShopType.ANY, Category.getDefaultCategory(), 1);
                            return true;
                        }
                    } else if (args.length >= 3) {
                        if (!PluginControl.hasCommandPermission(sender, "Gui-Others-Player", true)) return true;
                        Player target = Bukkit.getPlayer(args[2]);
                        if (target == null) {
                            return true;
                        }
                    }
                }
                if (args[0].equalsIgnoreCase("View")) {
                    if (!(sender instanceof Player)) {
                        Messages.sendMessage(sender, "Players-Only");
                        return true;
                    }
                    Player player = (Player) sender;
                    if (PluginControl.isWorldDisabled(player)) {
                        Messages.sendMessage(sender, "World-Disabled");
                        return true;
                    }
                    if (args.length == 1) {
                        if (!PluginControl.hasCommandPermission(sender, "View", true)) return true;
                        GUIAction.openViewer(player, player.getUniqueId(), 0);
                        return true;
                    }
                    if (args.length >= 2) {
                        if (!PluginControl.hasCommandPermission(sender, "View-Others-Player", true)) return true;
                        Player target = Bukkit.getPlayer(args[1]);
                        if (target != null) {
                            GUIAction.openViewer(player, target.getUniqueId(), 1);
                            return true;
                        } else {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    GUIAction.openViewer(player, Bukkit.getOfflinePlayer(args[1]).getUniqueId(), 1);
                                }
                            }.runTaskLater(Main.getInstance(), 1);
                            return true;
                        }
                    }
                    Messages.sendMessage(sender, "CrazyAuctions-View");
                    return true;
                }
                if (args[0].equalsIgnoreCase("Mail")) {
                    if (!PluginControl.hasCommandPermission(sender, "Mail", true)) return true;
                    if (!(sender instanceof Player)) {
                        Messages.sendMessage(sender, "Players-Only");
                        return true;
                    }
                    Player player = (Player) sender;
                    if (PluginControl.isWorldDisabled(player)) {
                        Messages.sendMessage(sender, "World-Disabled");
                        return true;
                    }
                    GUIAction.openPlayersMail(player, 1);
                    return true;
                }
                if (args[0].equalsIgnoreCase("Listed")) {
                    if (!PluginControl.hasCommandPermission(sender, "Listed", true)) return true;
                    if (!(sender instanceof Player)) {
                        Messages.sendMessage(sender, "Players-Only");
                        return true;
                    }
                    Player player = (Player) sender;
                    if (PluginControl.isWorldDisabled(player)) {
                        Messages.sendMessage(sender, "World-Disabled");
                        return true;
                    }
                    GUIAction.openPlayersCurrentList(player, 1);
                    return true;
                }
                if (args[0].equalsIgnoreCase("Buy")) {
                    if (!(sender instanceof Player)) {
                        Messages.sendMessage(sender, "Players-Only");
                        return true;
                    }
                    if (args.length == 1) {
                        Messages.sendMessage(sender, "CrazyAuctions-Buy");
                        return true;
                    }
                    if (args.length >= 2) {
                        Player player = (Player) sender;
                        if (PluginControl.isWorldDisabled(player)) {
                            Messages.sendMessage(sender, "World-Disabled");
                            return true;
                        }
                        if (!crazyAuctions.isBuyingEnabled()) {
                            Messages.sendMessage(player, "Buying-Disabled");
                            return true;
                        }
                        if (!PluginControl.hasCommandPermission(player, "Buy", true)) return true;
                        if (!PluginControl.isNumber(args[1])) {
                            Map<String, String> placeholders = new HashMap();
                            placeholders.put("%arg%", args[1]);
                            Messages.sendMessage(player, "Not-A-Valid-Number", placeholders);
                            return true;
                        }
                        double reward = Double.valueOf(args[1]);
                        double tax = 0;
                        if (!PluginControl.bypassTaxRate(player, ShopType.BUY)) {
                            tax = reward * PluginControl.getTaxRate(player, ShopType.BUY);
                        }
                        if (CurrencyManager.getMoney(player) < reward + tax) { 
                            HashMap<String, String> placeholders = new HashMap();
                            placeholders.put("%Money_Needed%", String.valueOf((reward + tax) - CurrencyManager.getMoney(player)));
                            placeholders.put("%money_needed%", String.valueOf((reward + tax) - CurrencyManager.getMoney(player)));
                            Messages.sendMessage(player, "Need-More-Money", placeholders);
                            return true;
                        }
                        if (reward < FileManager.Files.CONFIG.getFile().getDouble("Settings.Minimum-Buy-Reward")) {
                            Map<String, String> placeholders = new HashMap();
                            placeholders.put("%reward%", String.valueOf(FileManager.Files.CONFIG.getFile().getDouble("Settings.Minimum-Buy-Reward")));
                            Messages.sendMessage(player, "Buy-Reward-To-Low", placeholders);
                            return true;
                        }
                        if (reward > FileManager.Files.CONFIG.getFile().getDouble("Settings.Max-Beginning-Buy-Reward")) {
                            Map<String, String> placeholders = new HashMap();
                            placeholders.put("%reward%", String.valueOf(FileManager.Files.CONFIG.getFile().getDouble("Settings.Max-Beginning-Buy-Reward")));
                            Messages.sendMessage(player, "Buy-Reward-To-High", placeholders);
                            return true;
                        }
                        if (!PluginControl.bypassLimit(player, ShopType.BUY)) {
                            int limit = PluginControl.getLimit(player, ShopType.BUY);
                            if (limit > -1) {
                                if (crazyAuctions.getNumberOfPlayerItems(player, ShopType.BUY) >= limit) {
                                    Map<String, String> placeholders = new HashMap();
                                    placeholders.put("%number%", String.valueOf(limit));
                                    Messages.sendMessage(player, "Max-Buying-Items", placeholders);
                                    return true;
                                }
                            }
                        }
                        int amount = 1;
                        if (args.length >= 3) {
                            if (!PluginControl.isInt(args[2])) {
                                Map<String, String> placeholders = new HashMap();
                                placeholders.put("%arg%", args[1]);
                                Messages.sendMessage(player, "Not-A-Valid-Number", placeholders);
                                return true;
                            } else {
                                amount = Integer.valueOf(args[2]);
                            }
                        }
                        if (amount > 64) {
                            Messages.sendMessage(player, "Too-Many-Items");
                            return true;
                        }
                        UUID owner = player.getUniqueId();
                        GlobalMarket market = GlobalMarket.getMarket();
                        ItemStack item;
                        if (args.length >= 4) {
                            try {
                                item = new ItemStack(Material.valueOf(args[3].toUpperCase()), amount);
                            } catch (IllegalArgumentException ex) {
                                Map<String, String> placeholders = new HashMap();
                                placeholders.put("%item%", args[3]);
                                Messages.sendMessage(sender, "Unknown-Item", placeholders);
                                return true;
                            }
                            if (item.getType().equals(Material.AIR)) {
                                Map<String, String> placeholders = new HashMap();
                                placeholders.put("%item%", args[3]);
                                Messages.sendMessage(sender, "Unknown-Item", placeholders);
                                return true;
                            }
                        } else if (PluginControl.getItemInHand(player).getType() != Material.AIR) {
                            item = PluginControl.getItemInHand(player).clone();
                        } else {
                            Messages.sendMessage(sender, "CrazyAuctions-Buy");
                            return true;
                        }
                        if (PluginControl.isItemBlacklisted(item)) {
                            Messages.sendMessage(player, "Item-BlackListed");
                            return true;
                        }
                        if (PluginControl.isItemLoreBlacklisted(item)) {
                            Messages.sendMessage(player, "Item-LoreBlackListed");
                            return true;
                        }
                        item.setAmount(amount);
                        MarketGoods goods = new MarketGoods(
                            market.makeUID(),
                            ShopType.BUY,
                            new ItemOwner(owner, player.getName()),
                            item,
                            PluginControl.convertToMill(FileManager.Files.CONFIG.getFile().getString("Settings.Buy-Time")),
                            PluginControl.convertToMill(FileManager.Files.CONFIG.getFile().getString("Settings.Full-Expire-Time")),
                            System.currentTimeMillis(),
                            reward
                        );
                        CurrencyManager.removeMoney(player, reward + tax);
                        market.addGoods(goods);
                        Bukkit.getPluginManager().callEvent(new AuctionListEvent(player, ShopType.BUY, item, reward, tax));
                        Map<String, String> placeholders = new HashMap();
                        placeholders.put("%reward%", String.valueOf(reward));
                        placeholders.put("%tax%", String.valueOf(tax));
                        try {
                            placeholders.put("%item%", item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : (String) item.getClass().getMethod("getI18NDisplayName").invoke(item));
                        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                            placeholders.put("%item%", item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().toString().toLowerCase().replace("_", " "));
                        }
                        Messages.sendMessage(player, "Added-Item-For-Acquisition", placeholders);
                    }
                    return true;
                }
                if (args[0].equalsIgnoreCase("Sell") || args[0].equalsIgnoreCase("Bid")) {
                    if (!(sender instanceof Player)) {
                        Messages.sendMessage(sender, "Players-Only");
                        return true;
                    }
                    if (args.length >= 2) {
                        Player player = (Player) sender;
                        if (PluginControl.isWorldDisabled(player)) {
                            Messages.sendMessage(sender, "World-Disabled");
                            return true;
                        }
                        ShopType type = ShopType.SELL;
                        if (args[0].equalsIgnoreCase("Sell")) {
                            if (!crazyAuctions.isSellingEnabled()) {
                                Messages.sendMessage(player, "Selling-Disabled");
                                return true;
                            }
                            if (!PluginControl.hasCommandPermission(player, "Sell", true)) {
                                Messages.sendMessage(player, "No-Permission");
                                return true;
                            }
                        } else if (args[0].equalsIgnoreCase("Bid")) {
                            type = ShopType.BID;
                            if (!crazyAuctions.isBiddingEnabled()) {
                                Messages.sendMessage(player, "Bidding-Disabled");
                                return true;
                            }
                            if (!PluginControl.hasCommandPermission(player, "Bid", true)) {
                                Messages.sendMessage(player, "No-Permission");
                                return true;
                            }
                        }
                        ItemStack item = PluginControl.getItemInHand(player);
                        int amount = item.getAmount();
                        if (args.length >= 3) {
                            if (!PluginControl.isInt(args[2])) {
                                Map<String, String> placeholders = new HashMap();
                                placeholders.put("%arg%", args[2]);
                                Messages.sendMessage(player, "Not-A-Valid-Number", placeholders);
                                return true;
                            }
                            amount = Integer.parseInt(args[2]);
                            if (amount <= 0) amount = 1;
                            if (amount > item.getAmount()) amount = item.getAmount();
                        }
                        if (PluginControl.getItemInHand(player).getType() == Material.AIR) {
                            Messages.sendMessage(player, "Doesnt-Have-Item-In-Hand");
                            return false;
                        }
                        if (!PluginControl.isNumber(args[1])) {
                            Map<String, String> placeholders = new HashMap();
                            placeholders.put("%arg%", args[1]);
                            Messages.sendMessage(player, "Not-A-Valid-Number", placeholders);
                            return true;
                        }
                        double price = Double.valueOf(args[1]);
                        double tax = 0;
                        if (args[0].equalsIgnoreCase("Sell")) {
                            if (!crazyAuctions.isSellingEnabled()) {
                                Messages.sendMessage(player, "Selling-Disable");
                                return true;
                            }
                            if (!PluginControl.hasCommandPermission(player, "Sell", true)) {
                                Messages.sendMessage(player, "No-Permission");
                                return true;
                            }
                            if (price < FileManager.Files.CONFIG.getFile().getDouble("Settings.Minimum-Sell-Price")) {
                                Map<String, String> placeholders = new HashMap();
                                placeholders.put("%price%", String.valueOf(FileManager.Files.CONFIG.getFile().getDouble("Settings.Minimum-Sell-Price")));
                                Messages.sendMessage(player, "Sell-Price-To-Low", placeholders);
                                return true;
                            }
                            if (price > FileManager.Files.CONFIG.getFile().getDouble("Settings.Max-Beginning-Sell-Price")) {
                                Map<String, String> placeholders = new HashMap();
                                placeholders.put("%price%", String.valueOf(FileManager.Files.CONFIG.getFile().getDouble("Settings.Max-Beginning-Sell-Price")));
                                Messages.sendMessage(player, "Sell-Price-To-High", placeholders);
                                return true;
                            }
                            if (!PluginControl.bypassLimit(player, ShopType.SELL)) {
                                int limit = PluginControl.getLimit(player, ShopType.SELL);
                                if (limit > -1) {
                                    if (crazyAuctions.getNumberOfPlayerItems(player, ShopType.SELL) >= limit) {
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("%number%", String.valueOf(limit));
                                        Messages.sendMessage(player, "Max-Selling-Items", placeholders);
                                        return true;
                                    }
                                }
                            }
                            if (!PluginControl.bypassTaxRate(player, ShopType.SELL)) {
                                tax = price * PluginControl.getTaxRate(player, ShopType.SELL);
                                if (CurrencyManager.getMoney(player) < tax) { 
                                    HashMap<String, String> placeholders = new HashMap();
                                    placeholders.put("%Money_Needed%", String.valueOf(tax - CurrencyManager.getMoney(player)));
                                    placeholders.put("%money_needed%", String.valueOf(tax - CurrencyManager.getMoney(player)));
                                    Messages.sendMessage(player, "Need-More-Money", placeholders);
                                    return true;
                                }
                            }
                        }
                        if (args[0].equalsIgnoreCase("Bid")) {
                            if (price < FileManager.Files.CONFIG.getFile().getDouble("Settings.Minimum-Bid-Price")) {
                                Map<String, String> placeholders = new HashMap();
                                placeholders.put("%price%", String.valueOf(FileManager.Files.CONFIG.getFile().getDouble("Settings.Minimum-Bid-Price")));
                                Messages.sendMessage(player, "Bid-Price-To-Low", placeholders);
                                return true;
                            }
                            if (price > FileManager.Files.CONFIG.getFile().getDouble("Settings.Max-Beginning-Bid-Price")) {
                                Map<String, String> placeholders = new HashMap();
                                placeholders.put("%price%", String.valueOf(FileManager.Files.CONFIG.getFile().getDouble("Settings.Max-Beginning-Bid-Price")));
                                Messages.sendMessage(player, "Bid-Price-To-High", placeholders);
                                return true;
                            }
                            if (!PluginControl.bypassLimit(player, ShopType.BID)) {
                                int limit = PluginControl.getLimit(player, ShopType.BID);
                                if (limit > -1) {
                                    if (crazyAuctions.getNumberOfPlayerItems(player, ShopType.BID) >= limit) {
                                        Map<String, String> placeholders = new HashMap();
                                        placeholders.put("%number%", String.valueOf(limit));
                                        Messages.sendMessage(player, "Max-Bidding-Items", placeholders);
                                        return true;
                                    }
                                }
                            }
                            if (!PluginControl.bypassTaxRate(player, ShopType.BID)) {
                                tax = price * PluginControl.getTaxRate(player, ShopType.BID);
                                if (CurrencyManager.getMoney(player) < tax) { 
                                    HashMap<String, String> placeholders = new HashMap();
                                    placeholders.put("%Money_Needed%", String.valueOf(tax - CurrencyManager.getMoney(player)));
                                    placeholders.put("%money_needed%", String.valueOf(tax - CurrencyManager.getMoney(player)));
                                    Messages.sendMessage(player, "Need-More-Money", placeholders);
                                    return true;
                                }
                            }
                        }
                        if (PluginControl.isItemBlacklisted(item)) {
                            Messages.sendMessage(player, "Item-BlackListed");
                            return true;
                        }
                        if (PluginControl.isItemLoreBlacklisted(item)) {
                            Messages.sendMessage(player, "Item-LoreBlackListed");
                            return true;
                        }
                        if (!FileManager.Files.CONFIG.getFile().getBoolean("Settings.Allow-Damaged-Items")) {
                            for (Material i : getDamageableItems()) {
                                if (item.getType() == i) {
                                    if (item.getDurability() > 0) {
                                        Messages.sendMessage(player, "Item-Damaged");
                                        return true;
                                    }
                                }
                            }
                        }
                        UUID owner = player.getUniqueId();
                        ItemStack is = item.clone();
                        is.setAmount(amount);
                        GlobalMarket market = GlobalMarket.getMarket();
                        MarketGoods goods = new MarketGoods(
                            market.makeUID(),
                            type,
                            new ItemOwner(owner, player.getName()),
                            is,
                            type.equals(ShopType.BID) ? PluginControl.convertToMill(FileManager.Files.CONFIG.getFile().getString("Settings.Bid-Time")) : PluginControl.convertToMill(FileManager.Files.CONFIG.getFile().getString("Settings.Sell-Time")),
                            PluginControl.convertToMill(FileManager.Files.CONFIG.getFile().getString("Settings.Full-Expire-Time")),
                            System.currentTimeMillis(),
                            price,
                            "None"
                        );
                        market.addGoods(goods);
                        Bukkit.getPluginManager().callEvent(new AuctionListEvent(player, type, is, price, tax));
                        CurrencyManager.removeMoney(player, tax);
                        Map<String, String> placeholders = new HashMap();
                        placeholders.put("%Price%", String.valueOf(price));
                        placeholders.put("%price%", String.valueOf(price));
                        if (type.equals(ShopType.BID)) {
                            placeholders.put("%tax%", String.valueOf(tax));
                            Messages.sendMessage(player, "Added-Item-For-Bid", placeholders);
                        } else {
                            placeholders.put("%tax%", String.valueOf(tax));
                            Messages.sendMessage(player, "Added-Item-For-Sale", placeholders);
                        }
                        if (item.getAmount() <= 1 || (item.getAmount() - amount) <= 0) {
                            PluginControl.setItemInHand(player, new ItemStack(Material.AIR));
                        } else {
                            item.setAmount(item.getAmount() - amount);
                        }
                        return true;
                    }
                    if (args[0].equalsIgnoreCase("Sell")) {
                        Messages.sendMessage(sender, "CrazyAuctions-Sell");
                    } else if (args[0].equalsIgnoreCase("Bid")) {
                        Messages.sendMessage(sender, "CrazyAuctions-Bid");
                    }
                    return true;
                }
            }
        }
        Messages.sendMessage(sender, "CrazyAuctions-Help");
        return false;
    }
    
    private ArrayList<Material> getDamageableItems() {
        ArrayList<Material> ma = new ArrayList();
        if (Version.getCurrentVersion().isNewer(Version.v1_12_R1)) {
            ma.add(Material.matchMaterial("GOLDEN_HELMET"));
            ma.add(Material.matchMaterial("GOLDEN_CHESTPLATE"));
            ma.add(Material.matchMaterial("GOLDEN_LEGGINGS"));
            ma.add(Material.matchMaterial("GOLDEN_BOOTS"));
            ma.add(Material.matchMaterial("WOODEN_SWORD"));
            ma.add(Material.matchMaterial("WOODEN_AXE"));
            ma.add(Material.matchMaterial("WOODEN_PICKAXE"));
            ma.add(Material.matchMaterial("WOODEN_AXE"));
            ma.add(Material.matchMaterial("WOODEN_SHOVEL"));
            ma.add(Material.matchMaterial("STONE_SHOVEL"));
            ma.add(Material.matchMaterial("IRON_SHOVEL"));
            ma.add(Material.matchMaterial("DIAMOND_SHOVEL"));
            ma.add(Material.matchMaterial("WOODEN_HOE"));
            ma.add(Material.matchMaterial("GOLDEN_HOE"));
            ma.add(Material.matchMaterial("CROSSBOW"));
            ma.add(Material.matchMaterial("TRIDENT"));
            ma.add(Material.matchMaterial("TURTLE_HELMET"));
        } else {
            ma.add(Material.matchMaterial("GOLD_HELMET"));
            ma.add(Material.matchMaterial("GOLD_CHESTPLATE"));
            ma.add(Material.matchMaterial("GOLD_LEGGINGS"));
            ma.add(Material.matchMaterial("GOLD_BOOTS"));
            ma.add(Material.matchMaterial("WOOD_SWORD"));
            ma.add(Material.matchMaterial("WOOD_AXE"));
            ma.add(Material.matchMaterial("WOOD_PICKAXE"));
            ma.add(Material.matchMaterial("WOOD_AXE"));
            ma.add(Material.matchMaterial("WOOD_SPADE"));
            ma.add(Material.matchMaterial("STONE_SPADE"));
            ma.add(Material.matchMaterial("IRON_SPADE"));
            ma.add(Material.matchMaterial("DIAMOND_SPADE"));
            ma.add(Material.matchMaterial("WOOD_HOE"));
            ma.add(Material.matchMaterial("GOLD_HOE"));
        }
        ma.add(Material.DIAMOND_HELMET);
        ma.add(Material.DIAMOND_CHESTPLATE);
        ma.add(Material.DIAMOND_LEGGINGS);
        ma.add(Material.DIAMOND_BOOTS);
        ma.add(Material.CHAINMAIL_HELMET);
        ma.add(Material.CHAINMAIL_CHESTPLATE);
        ma.add(Material.CHAINMAIL_LEGGINGS);
        ma.add(Material.CHAINMAIL_BOOTS);
        ma.add(Material.IRON_HELMET);
        ma.add(Material.IRON_CHESTPLATE);
        ma.add(Material.IRON_LEGGINGS);
        ma.add(Material.IRON_BOOTS);
        ma.add(Material.LEATHER_HELMET);
        ma.add(Material.LEATHER_CHESTPLATE);
        ma.add(Material.LEATHER_LEGGINGS);
        ma.add(Material.LEATHER_BOOTS);
        ma.add(Material.BOW);
        ma.add(Material.STONE_SWORD);
        ma.add(Material.IRON_SWORD);
        ma.add(Material.DIAMOND_SWORD);
        ma.add(Material.STONE_AXE);
        ma.add(Material.IRON_AXE);
        ma.add(Material.DIAMOND_AXE);
        ma.add(Material.STONE_PICKAXE);
        ma.add(Material.IRON_PICKAXE);
        ma.add(Material.DIAMOND_PICKAXE);
        ma.add(Material.STONE_AXE);
        ma.add(Material.IRON_AXE);
        ma.add(Material.DIAMOND_AXE);
        ma.add(Material.STONE_HOE);
        ma.add(Material.IRON_HOE);
        ma.add(Material.DIAMOND_HOE);
        ma.add(Material.FLINT_AND_STEEL);
        ma.add(Material.ANVIL);
        ma.add(Material.FISHING_ROD);
        return ma;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (FileManager.isBackingUp()) return new ArrayList();
        if (args.length == 1) {
            if (args[0].toLowerCase().startsWith("h") && PluginControl.hasCommandPermission(sender, "Help", false)) {
                return Arrays.asList("help");
            } else if (args[0].toLowerCase().startsWith("r") && PluginControl.hasCommandPermission(sender, "Reload", false)) {
                return Arrays.asList("reload");
            } else if (args[0].toLowerCase().startsWith("s") && PluginControl.hasCommandPermission(sender, "Sell", false)) {
                return Arrays.asList("sell");
            } else if (args[0].toLowerCase().startsWith("b")) {
                if (args[0].toLowerCase().startsWith("bi") && PluginControl.hasCommandPermission(sender, "Bid", false)) return Arrays.asList("bid");
                if (args[0].toLowerCase().startsWith("bu") && PluginControl.hasCommandPermission(sender, "Buy", false)) return Arrays.asList("buy");
                List<String> list = new ArrayList();
                if (PluginControl.hasCommandPermission(sender, "Bid", false)) list.add("bid");
                if (PluginControl.hasCommandPermission(sender, "Buy", false)) list.add("buy");
                return list;
            } else if (args[0].toLowerCase().startsWith("l") && PluginControl.hasCommandPermission(sender, "Listed", false)) {
                return Arrays.asList("listed");
            } else if (args[0].toLowerCase().startsWith("m") && PluginControl.hasCommandPermission(sender, "Mail", false)) {
                return Arrays.asList("mail");
            } else if (args[0].toLowerCase().startsWith("v") && PluginControl.hasCommandPermission(sender, "View", false)) {
                return Arrays.asList("view");
            } else if (args[0].toLowerCase().startsWith("g") && PluginControl.hasCommandPermission(sender, "Gui", false)) {
                return Arrays.asList("gui");
            } else if (args[0].toLowerCase().startsWith("a") && PluginControl.hasCommandPermission(sender, "Admin", false)) {
                return Arrays.asList("admin");
            }
            List<String> list = new ArrayList();
            if (PluginControl.hasCommandPermission(sender, "Help", false)) list.add("help");
            if (PluginControl.hasCommandPermission(sender, "Gui", false)) list.add("gui");
            if (PluginControl.hasCommandPermission(sender, "Sell", false)) list.add("sell");
            if (PluginControl.hasCommandPermission(sender, "Buy", false)) list.add("buy");
            if (PluginControl.hasCommandPermission(sender, "Bid", false)) list.add("bid");
            if (PluginControl.hasCommandPermission(sender, "View", false)) list.add("view");
            if (PluginControl.hasCommandPermission(sender, "Listed", false)) list.add("listed");
            if (PluginControl.hasCommandPermission(sender, "Mail", false)) list.add("mail");
            if (PluginControl.hasCommandPermission(sender, "Reload", false)) list.add("reload");
            if (PluginControl.hasCommandPermission(sender, "Admin", false)) list.add("admin");
            return list;
        } else if (args.length >= 2) {
            if (args[0].equalsIgnoreCase("reload") && PluginControl.hasCommandPermission(sender, "Reload", false)) {
                List<String> list = new ArrayList();
                for (String text : new String[]{"all", "database", "config", "messages", "market", "playerdata", "category", "itemcollection"}) {
                    if (text.toLowerCase().startsWith(args[1].toLowerCase())) {
                        list.add(text);
                    }
                }
                return list;
            }
            if (args[0].equalsIgnoreCase("admin") && PluginControl.hasCommandPermission(sender, "Admin", false)) {
                if (args.length >= 3) {
                    if (args[1].equalsIgnoreCase("rollback") && PluginControl.hasCommandPermission(sender, "Admin.SubCommands.RollBack", false)) {
                        List<String> list = new ArrayList();
                        for (String string : PluginControl.getBackupFiles()) {
                            if (string.toLowerCase().startsWith(args[2].toLowerCase())) {
                                list.add(string);
                            }
                        }
                        return list;
                    }
                    if (args[1].equalsIgnoreCase("info") && PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Info", false)) {
                        List<String> list = new ArrayList();
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (p.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                                list.add(p.getName());
                            }
                        }
                        return list;
                    }
                    if (args[1].equalsIgnoreCase("market") && PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Market", false)) {
                        List<String> list = new ArrayList();
                        String[] subCommands = {"confirm", "clear", "list", "repricing", "delete", "download", "upload"};
                        for (String commands : subCommands) {
                            if (commands.toLowerCase().startsWith(args[2].toLowerCase())) {
                                list.add(commands);
                            }
                        }
                        return list;
                    }
                    if (args[1].equalsIgnoreCase("player") && PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Player", false)) {
                        if (args.length == 3) {
                            List<String> list = new ArrayList();
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                if (player.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                                    list.add(player.getName());
                                }
                            }
                            if (PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Player.SubCommands.Confirm", false)) list.add("confirm");
                            return list;
                        } else if (args.length == 4) {
                            List<String> list = new ArrayList();
                            String[] subCommands = {"clear", "list", "view", "delete", "download", "upload"};
                            for (String commands : subCommands) {
                                if (commands.toLowerCase().startsWith(args[3].toLowerCase())) {
                                    list.add(commands);
                                }
                            }
                            return list;
                        } else if (args.length >= 5) {
                            if (args[3].equalsIgnoreCase("clear") && PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Player.SubCommands.Clear", false)) {
                                List<String> list = new ArrayList();
                                String[] subCommands = {"market", "mail"};
                                for (String commands : subCommands) {
                                    if (commands.toLowerCase().startsWith(args[4].toLowerCase())) {
                                        list.add(commands);
                                    }
                                }
                                return list;
                            }
                        }
                    }
                    if (args[1].equalsIgnoreCase("itemcollection") && PluginControl.hasCommandPermission(sender, "Admin.SubCommands.ItemCollection", false)) {
                        if (args.length == 3) {
                            List<String> list = new ArrayList();
                            for (String text : new String[]{"help", "add", "delete", "give", "list"}) {
                                if (text.toLowerCase().startsWith(args[2].toLowerCase())) {
                                    list.add(text);
                                }
                            }
                            return list;
                        } else if (args.length >= 4) {
                            if (args[2].equalsIgnoreCase("delete")) {
                                List<String> list = new ArrayList();
                                for (ItemCollection ic : ItemCollection.getCollection()) {
                                    if (ic.getDisplayName().toLowerCase().startsWith(args[3].toLowerCase())) {
                                        list.add(ic.getDisplayName());
                                    }
                                }
                                return list;
                            } else if (args[2].equalsIgnoreCase("give")) {
                                if (args.length == 4) {
                                    List<String> list = new ArrayList();
                                    for (ItemCollection ic : ItemCollection.getCollection()) {
                                        if (ic.getDisplayName().toLowerCase().startsWith(args[3].toLowerCase())) {
                                            list.add(ic.getDisplayName());
                                        }
                                    }
                                    return list;
                                } else {
                                    List<String> list = new ArrayList();
                                    for (Player p : Bukkit.getOnlinePlayers()) {
                                        if (p.getName().toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
                                            list.add(p.getName());
                                        }
                                    }
                                    return list;
                                }
                            } else {
                                return new ArrayList();
                            }
                        }
                    }
                }
                List<String> list = new ArrayList();
                for (String text : new String[]{"backup", "rollback", "info", "market", "player", "synchronize", "itemcollection", "printstacktrace"}) {
                    if (text.toLowerCase().startsWith(args[1].toLowerCase())) {
                        list.add(text);
                    }
                }
                return list;
            }
            if (args[0].equalsIgnoreCase("view") && PluginControl.hasCommandPermission(sender, "View-Others-Player", false)) {
                List<String> players = new ArrayList();
                for (Player ps : Bukkit.getOnlinePlayers()) {
                    if (ps.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        players.add(ps.getName());
                    }
                }
                return players;
            }
            if (args[0].equalsIgnoreCase("buy") && args.length == 4 && PluginControl.hasCommandPermission(sender, "Buy", false)) {
                if (sender instanceof Player) {
                    List<String> list = new ArrayList();
                    for (Material m : Material.values()) {
                        if (m.toString().toLowerCase().startsWith(args[3].toLowerCase())) {
                            list.add(m.toString().toLowerCase());
                        }
                    }
                    return list;
                }
            }
            if (args[0].equalsIgnoreCase("gui") && PluginControl.hasCommandPermission(sender, "Gui", false)) { // gui buy 
                if (args.length == 2) {
                    if (sender instanceof Player) {
                        if (args[1].toLowerCase().startsWith("s")) {
                            return Arrays.asList("sell");
                        } else if (args[1].toLowerCase().startsWith("b")) {
                            if (args[1].toLowerCase().startsWith("bu")) return Arrays.asList("buy");
                            if (args[1].toLowerCase().startsWith("bi")) return Arrays.asList("bid");
                            return Arrays.asList("buy", "bid");
                        }
                        return Arrays.asList("sell", "buy", "bid");
                    }
                } else if (args.length == 3 && PluginControl.hasCommandPermission(sender, "Gui-Others-Player", false)) {
                    if (sender instanceof Player) {
                        List<String> list = new ArrayList();
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (p.getName().toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
                                list.add(p.getName());
                            }
                        }
                        return list;
                    }
                }
            }
        }
        return new ArrayList();
    }
}
