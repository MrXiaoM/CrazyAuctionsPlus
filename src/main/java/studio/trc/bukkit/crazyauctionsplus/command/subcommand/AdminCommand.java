package studio.trc.bukkit.crazyauctionsplus.command.subcommand;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
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
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import studio.trc.bukkit.crazyauctionsplus.command.CrazyAuctionsSubCommand;
import studio.trc.bukkit.crazyauctionsplus.command.CrazyAuctionsSubCommandType;
import studio.trc.bukkit.crazyauctionsplus.database.GlobalMarket;
import studio.trc.bukkit.crazyauctionsplus.database.Storage;
import studio.trc.bukkit.crazyauctionsplus.database.StorageMethod;
import studio.trc.bukkit.crazyauctionsplus.database.engine.MySQLEngine;
import studio.trc.bukkit.crazyauctionsplus.database.engine.SQLiteEngine;
import studio.trc.bukkit.crazyauctionsplus.util.*;
import studio.trc.bukkit.crazyauctionsplus.util.FileManager.Files;
import studio.trc.bukkit.crazyauctionsplus.util.enums.ShopType;

public class AdminCommand
    implements CrazyAuctionsSubCommand
{
    private final static Map<CommandSender, String> marketConfirm = new HashMap<>();
    private final static Map<CommandSender, String> itemMailConfirm = new HashMap<>();
    
    @Override
    public void execute(CommandSender sender, String subCommand, String... args) {
        if (!PluginControl.hasCommandPermission(sender, "Admin", true)) return;
        if (args.length == 1) {
            MessageUtil.sendMessage(sender, "Admin-Menu");
        } else if (args.length >= 2) {
            if (args[1].equalsIgnoreCase("backup")) {
                command_backup(sender, args);
            } else if (args[1].equalsIgnoreCase("rollback")) {
                command_rollback(sender, args);
            } else if (args[1].equalsIgnoreCase("info")) {
                command_info(sender, args);
            } else if (args[1].equalsIgnoreCase("synchronize")) {
                command_synchronize(sender, args);
            } else if (args[1].equalsIgnoreCase("printstacktrace")) {
                command_printstacktrace(sender, args);
            } else if (args[1].equalsIgnoreCase("market")) {
                command_market(sender, args);
            } else if (args[1].equalsIgnoreCase("player")) {
                command_player(sender, args);
            } else if (args[1].equalsIgnoreCase("itemcollection")) {
                command_itemcollection(sender, args);
            } else {
                MessageUtil.sendMessage(sender, "Admin-Menu");
            }
        }
    }

    @Override
    public String getName() {
        return "admin";
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String subCommand, String... args) {
        if (args.length == 2) {
            return getTabElements(args, 2, Arrays.asList("backup", "rollback", "info", "synchronize", "printstacktrace", "market", "player", "itemcollection"));
        } else {
            if (args[1].equalsIgnoreCase("rollback") && PluginControl.hasCommandPermission(sender, "Admin.SubCommands.RollBack", false)) {
                return getTabElements(args, args.length, PluginControl.getBackupFiles());
            }
            if (args[1].equalsIgnoreCase("info") && PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Info", false)) {
                return getTabPlayersName(args, args.length);
            }
            if (args[1].equalsIgnoreCase("market") && PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Market", false)) {
                return getTabElements(args, args.length, Arrays.asList("confirm", "clear", "list", "repricing", "delete", "download", "upload"));
            }
            if (args[1].equalsIgnoreCase("player") && PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Player", false)) {
                if (args.length == 3) {
                    List<String> list = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
                    if (PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Player.SubCommands.Confirm", false)) list.add("confirm");
                    return getTabElements(args, 3, list);
                }
                if (args.length == 4) {
                    return getTabElements(args, 4, Arrays.asList("clear", "list", "view", "delete", "download", "upload"));
                }
                if (args.length == 5) {
                    if (args[3].equalsIgnoreCase("clear") && PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Player.SubCommands.Clear", false)) {
                        return getTabElements(args, 5, Arrays.asList("market", "mail"));
                    }
                }
            }
            if (args[1].equalsIgnoreCase("itemcollection") && PluginControl.hasCommandPermission(sender, "Admin.SubCommands.ItemCollection", false)) {
                if (args.length == 3) {
                    return getTabElements(args, 3, Arrays.asList("help", "add", "delete", "give", "list"));
                }
                if (args.length == 4) {
                    if (args[2].equalsIgnoreCase("delete")) {
                        return getTabElements(args, 4, ItemCollection.getCollection().stream().map(ItemCollection::getDisplayName).collect(Collectors.toList()));
                    }
                    if (args[2].equalsIgnoreCase("give")) {
                        if (args.length == 4) {
                            return getTabElements(args, 4, ItemCollection.getCollection().stream().map(ItemCollection::getDisplayName).collect(Collectors.toList()));
                        }
                        if (args.length >= 5) {
                            return getTabPlayersName(args, args.length);
                        }
                    }
                }
            }
        }
        return new ArrayList<>();
    }

    @Override
    public CrazyAuctionsSubCommandType getCommandType() {
        return CrazyAuctionsSubCommandType.ADMIN;
    }

    private void command_backup(CommandSender sender, String[] args) {
        if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Backup", true)) return;
        if (FileManager.isBackingUp()) {
            MessageUtil.sendMessage(sender, "Admin-Command.Backup.BackingUp");
            return;
        }
        MessageUtil.sendMessage(sender, "Admin-Command.Backup.Starting");
        Bukkit.getOnlinePlayers().stream().filter(player -> GUI.openingGUI.containsKey(player.getUniqueId())).forEach(HumanEntity::closeInventory);
        FileManager.backup(sender);
    }

    private void command_rollback(CommandSender sender, String[] args) {
        if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.RollBack", true)) return;
        if (FileManager.isRollingBack()) {
            MessageUtil.sendMessage(sender, "Admin-Command.RollBack.RollingBack");
            return;
        }
        if (args.length == 2) {
            MessageUtil.sendMessage(sender, "Admin-Command.RollBack.Help");
        } else if (args.length >= 3) {
            File backupFile = new File("plugins/CrazyAuctionsPlus/Backup/" + args[2]);
            if (backupFile.exists()) {
                MessageUtil.sendMessage(sender, "Admin-Command.RollBack.Starting");
                Bukkit.getOnlinePlayers().stream().filter(player -> GUI.openingGUI.containsKey(player.getUniqueId())).forEach(HumanEntity::closeInventory);
                FileManager.rollBack(backupFile, sender);
            } else {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("%file%", args[2]);
                MessageUtil.sendMessage(sender, "Admin-Command.RollBack.Backup-Not-Exist", placeholders);
            }
        }
    }

    private void command_info(CommandSender sender, String[] args) {
        if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Info", true)) return;
        if (args.length == 2) {
            MessageUtil.sendMessage(sender, "Admin-Command.Info.Help");
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
                    items = GlobalMarket.getMarket().getItems().stream().filter(mg -> mg.getItemOwner().getUUID().equals(offlineplayer.getUniqueId())).map(item -> 1).reduce(items, Integer::sum);
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("%player%", offlineplayer.getName());
                    placeholders.put("%group%", MessageUtil.getValue("Admin-Command.Info.Unknown"));
                    placeholders.put("%items%", String.valueOf(items));
                    placeholders.put("%database%", database);
                    MessageUtil.sendMessage(sender, "Admin-Command.Info.Info-Messages", placeholders);
                } else {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("%player%", args[2]);
                    MessageUtil.sendMessage(sender, "Admin-Command.Info.Unknown-Player", placeholders);
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
                items = GlobalMarket.getMarket().getItems().stream().filter(mg -> mg.getItemOwner().getUUID().equals(player.getUniqueId())).map(item -> 1).reduce(items, Integer::sum);
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("%player%", player.getName());
                placeholders.put("%group%", group);
                placeholders.put("%items%", String.valueOf(items));
                placeholders.put("%database%", database);
                MessageUtil.sendMessage(sender, "Admin-Command.Info.Info-Messages", placeholders);
            }
        }
    }

    private void command_synchronize(CommandSender sender, String[] args) {
        if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Synchronize", true)) return;
        if (FileManager.isSyncing()) {
            MessageUtil.sendMessage(sender, "Admin-Command.Synchronize.Syncing");
            return;
        }
        MessageUtil.sendMessage(sender, "Admin-Command.Synchronize.Starting");
        FileManager.synchronize(sender);
    }

    private void command_printstacktrace(CommandSender sender, String[] args) {
        if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.PrintStackTrace", true)) return;
        if (PluginControl.stackTraceVisible.containsKey(sender)) {
            if (PluginControl.stackTraceVisible.get(sender)) {
                PluginControl.stackTraceVisible.put(sender, false);
                MessageUtil.sendMessage(sender, "Admin-Command.PrintStackTrace.Turn-Off");
            } else {
                PluginControl.stackTraceVisible.put(sender, true);
                MessageUtil.sendMessage(sender, "Admin-Command.PrintStackTrace.Turn-On");
            }
        } else {
            PluginControl.stackTraceVisible.put(sender, true);
            MessageUtil.sendMessage(sender, "Admin-Command.PrintStackTrace.Turn-On");
        }
    }

    private void command_market(CommandSender sender, String[] args) {
        if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Market", true)) return;
        if (args.length == 2) {
            MessageUtil.sendMessage(sender, "Admin-Command.Market.Help");
        } else if (args.length >= 3) {
            GlobalMarket market = GlobalMarket.getMarket();
            if (args[2].equalsIgnoreCase("confirm")) {
                command_market_confirm(sender, args);
            } else if (args[2].equalsIgnoreCase("list")) {
                command_market_list(sender, args, market);
            } else if (args[2].equalsIgnoreCase("clear")) {
                command_market_clear(sender, args, market);
            } else if (args[2].equalsIgnoreCase("repricing")) {
                command_market_repricing(sender, args, market);
            } else if (args[2].equalsIgnoreCase("delete")) {
                command_market_delete(sender, args, market);
            } else if (args[2].equalsIgnoreCase("download")) {
                command_market_download(sender, args, market);
            } else if (args[2].equalsIgnoreCase("upload")) {
                command_market_upload(sender, args, market);
            } else {
                MessageUtil.sendMessage(sender, "Admin-Command.Market.Help");
            }
        }
    }

    private void command_player(CommandSender sender, String[] args) {
        if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Player", true)) return;
        if (args.length == 2) {
            MessageUtil.sendMessage(sender, "Admin-Command.Player.Help");
        } else if (args.length == 3) {
            command_player_confirm(sender, args);
        } else if (args.length >= 4) {
            if (args[3].equalsIgnoreCase("list")) {
                command_player_list(sender, args);
            } else if (args[3].equalsIgnoreCase("clear")) {
                command_player_clear(sender, args);
            } else if (args[3].equalsIgnoreCase("delete")) {
                command_player_delete(sender, args);
            } else if (args[3].equalsIgnoreCase("view")) {
                command_player_view(sender, args);
            } else if (args[3].equalsIgnoreCase("download")) {
                command_player_download(sender, args);
            } else if (args[3].equalsIgnoreCase("upload")) {
                command_player_upload(sender, args);
            } else {
                MessageUtil.sendMessage(sender, "Admin-Command.Player.Help");
            }
        }
    }

    private void command_itemcollection(CommandSender sender, String[] args) {
        if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.ItemCollection", true)) return;
        if (args.length == 2) {
            MessageUtil.sendMessage(sender, "Admin-Command.ItemCollection.Help");
        } else if (args.length >= 3) {
            if (args[2].equalsIgnoreCase("add")) {
                if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.ItemCollection.SubCommands.Add", true)) return;
                if (args.length <= 3) {
                    MessageUtil.sendMessage(sender, "Admin-Command.ItemCollection.Add.Help");
                } else {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        if (player.getItemInHand() == null) {
                            MessageUtil.sendMessage(sender, "Admin-Command.ItemCollection.Add.Doesnt-Have-Item-In-Hand");
                            return;
                        }
                        if (ItemCollection.addItem(player.getItemInHand(), args[3])) {
                            Map<String, String> placeholders = new HashMap<>();
                            placeholders.put("%item%", args[3]);
                            MessageUtil.sendMessage(sender, "Admin-Command.ItemCollection.Add.Successfully", placeholders);
                        } else {
                            MessageUtil.sendMessage(sender, "Admin-Command.ItemCollection.Add.Already-Exist");
                        }
                    } else {
                        MessageUtil.sendMessage(sender, "Players-Only");
                    }
                }
            } else if (args[2].equalsIgnoreCase("delete") || args[2].equalsIgnoreCase("remove")) {
                if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.ItemCollection.SubCommands.Delete", true)) return;
                if (args.length <= 3) {
                    MessageUtil.sendMessage(sender, "Admin-Command.ItemCollection.Delete.Help");
                } else {
                    try {
                        long uid = Long.parseLong(args[3]);
                        for (ItemCollection ic : ItemCollection.getCollection()) {
                            if (ic.getUID() == uid) {
                                Map<String, String> placeholders = new HashMap<>();
                                placeholders.put("%item%", ic.getDisplayName());
                                ItemCollection.deleteItem(uid);
                                MessageUtil.sendMessage(sender, "Admin-Command.ItemCollection.Delete.Successfully", placeholders);
                                return;
                            }
                        }
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("%item%", args[3]);
                        MessageUtil.sendMessage(sender, "Admin-Command.ItemCollection.Delete.Item-Not-Exist", placeholders);
                    } catch (NumberFormatException ex) {
                        String displayName = args[3];
                        for (ItemCollection ic : ItemCollection.getCollection()) {
                            if (ic.getDisplayName().equalsIgnoreCase(displayName)) {
                                Map<String, String> placeholders = new HashMap<>();
                                placeholders.put("%item%", ic.getDisplayName());
                                ItemCollection.deleteItem(displayName);
                                MessageUtil.sendMessage(sender, "Admin-Command.ItemCollection.Delete.Successfully", placeholders);
                                return;
                            }
                        }
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("%item%", args[3]);
                        MessageUtil.sendMessage(sender, "Admin-Command.ItemCollection.Delete.Item-Not-Exist", placeholders);
                    }
                }
            } else if (args[2].equalsIgnoreCase("list")) {
                if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.ItemCollection.SubCommands.List", true)) return;
                if (ItemCollection.getCollection().isEmpty()) {
                    MessageUtil.sendMessage(sender, "Admin-Command.ItemCollection.List.Empty-Collection");
                } else {
                    String format = MessageUtil.getValue("Admin-Command.ItemCollection.List.List-Format");
                    List<String> list = ItemCollection.getCollection().stream().map(collection -> format.replace("%uid%", String.valueOf(collection.getUID())).replace("%item%", collection.getDisplayName())).collect(Collectors.toList());
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("%list%", list.toString().substring(1, list.toString().length() - 1));
                    MessageUtil.sendMessage(sender, "Admin-Command.ItemCollection.List.Messages", placeholders);
                }
            } else if (args[2].equalsIgnoreCase("give")) {
                if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.ItemCollection.SubCommands.Give", true)) return;
                if (args.length == 3) {
                    MessageUtil.sendMessage(sender, "Admin-Command.ItemCollection.Give.Help");
                } else if (args.length == 4) {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        try {
                            long uid = Long.parseLong(args[3]);
                            for (ItemCollection ic : ItemCollection.getCollection()) {
                                if (ic.getUID() == uid) {
                                    Map<String, String> placeholders = new HashMap<>();
                                    placeholders.put("%item%", ic.getDisplayName());
                                    placeholders.put("%player%", player.getName());
                                    player.getInventory().addItem(ic.getItem());
                                    MessageUtil.sendMessage(sender, "Admin-Command.ItemCollection.Give.Successfully", placeholders);
                                    return;
                                }
                            }
                            Map<String, String> placeholders = new HashMap<>();
                            placeholders.put("%item%", args[3]);
                            MessageUtil.sendMessage(sender, "Admin-Command.ItemCollection.Give.Item-Not-Exist", placeholders);
                        } catch (NumberFormatException ex) {
                            String displayName = args[3];
                            for (ItemCollection ic : ItemCollection.getCollection()) {
                                if (ic.getDisplayName().equalsIgnoreCase(displayName)) {
                                    Map<String, String> placeholders = new HashMap<>();
                                    placeholders.put("%item%", ic.getDisplayName());
                                    placeholders.put("%player%", player.getName());
                                    player.getInventory().addItem(ic.getItem());
                                    MessageUtil.sendMessage(sender, "Admin-Command.ItemCollection.Give.Successfully", placeholders);
                                    return;
                                }
                            }
                            Map<String, String> placeholders = new HashMap<>();
                            placeholders.put("%item%", args[3]);
                            MessageUtil.sendMessage(sender, "Admin-Command.ItemCollection.Give.Item-Not-Exist", placeholders);
                        }
                    } else {
                        MessageUtil.sendMessage(sender, "Admin-Command.ItemCollection.Give.Help");
                    }
                } else if (args.length >= 5) {
                    Player player = Bukkit.getPlayer(args[4]);
                    if (player == null) {
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("%player%", args[4]);
                        MessageUtil.sendMessage(sender, "Admin-Command.ItemCollection.Give.Player-Offline", placeholders);
                    } else {
                        try {
                            long uid = Long.parseLong(args[3]);
                            for (ItemCollection ic : ItemCollection.getCollection()) {
                                if (ic.getUID() == uid) {
                                    Map<String, String> placeholders = new HashMap<>();
                                    placeholders.put("%item%", ic.getDisplayName());
                                    placeholders.put("%player%", player.getName());
                                    player.getInventory().addItem(ic.getItem());
                                    MessageUtil.sendMessage(sender, "Admin-Command.ItemCollection.Give.Successfully", placeholders);
                                    return;
                                }
                            }
                            Map<String, String> placeholders = new HashMap<>();
                            placeholders.put("%item%", args[3]);
                            MessageUtil.sendMessage(sender, "Admin-Command.ItemCollection.Give.Item-Not-Exist", placeholders);
                        } catch (NumberFormatException ex) {
                            String displayName = args[3];
                            for (ItemCollection ic : ItemCollection.getCollection()) {
                                if (ic.getDisplayName().equalsIgnoreCase(displayName)) {
                                    Map<String, String> placeholders = new HashMap<>();
                                    placeholders.put("%item%", ic.getDisplayName());
                                    placeholders.put("%player%", player.getName());
                                    player.getInventory().addItem(ic.getItem());
                                    MessageUtil.sendMessage(sender, "Admin-Command.ItemCollection.Give.Successfully", placeholders);
                                    return;
                                }
                            }
                            Map<String, String> placeholders = new HashMap<>();
                            placeholders.put("%item%", args[3]);
                            MessageUtil.sendMessage(sender, "Admin-Command.ItemCollection.Give.Item-Not-Exist", placeholders);
                        }
                    }
                }
            } else {
                MessageUtil.sendMessage(sender, "Admin-Command.ItemCollection.Help");
            }
        }
    }

    private void command_market_confirm(CommandSender sender, String[] args) {
        if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Market.SubCommands.Confirm", true)) return;
        if (marketConfirm.containsKey(sender)) {
            Bukkit.dispatchCommand(sender, marketConfirm.get(sender));
        } else {
            MessageUtil.sendMessage(sender, "Admin-Command.Market.Confirm.Invalid");
        }
    }

    private void command_market_list(CommandSender sender, String[] args, GlobalMarket market) {
        if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Market.SubCommands.List", true)) return;
        if (args.length == 3) {
            List<MarketGoods> list = market.getItems();
            if (list.isEmpty()) {
                MessageUtil.sendMessage(sender, "Admin-Command.Market.List.Empty");
                return;
            }
            int page = 1;
            int nosp = 9;
            try {
                nosp = Integer.parseInt(MessageUtil.getValue("Admin-Command.Market.List.Number-Of-Single-Page"));
            } catch (NumberFormatException ignored) {}
            StringBuilder formatList = new StringBuilder();
            for (int i = page * nosp - nosp;i < list.size() && i < page * nosp;i++) {
                String format = MessageUtil.getValue("Admin-Command.Market.List.Format").replace("%uid%", String.valueOf(list.get(i).getUID())).replace("%money%", String.valueOf(list.get(i).getShopType().equals(ShopType.BUY) ? list.get(i).getReward() : list.get(i).getPrice())).replace("%owner%", list.get(i).getItemOwner().getName());
                format = format.replace("%item%", LangUtilsHook.getItemName(list.get(i).getItem()));
                formatList.append(format);
            }
            int maxpage = (list.size() / nosp) + 1;
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%format%", formatList.toString());
            placeholders.put("%page%", String.valueOf(page));
            placeholders.put("%maxpage%", String.valueOf(maxpage));
            placeholders.put("%nextpage%", String.valueOf(page + 1));
            Map<String, Boolean> visible = new HashMap<>();
            visible.put("{hasnext}", maxpage > page);
            MessageUtil.sendMessage(sender, "Admin-Command.Market.List.Messages", placeholders, visible);
        } else if (args.length >= 4) {
            List<MarketGoods> list = market.getItems();
            if (list.isEmpty()) {
                MessageUtil.sendMessage(sender, "Admin-Command.Market.List.Empty");
                return;
            }
            int page = 1;
            try {
                page = Integer.parseInt(args[3]);
            } catch (NumberFormatException ignored) {}
            int nosp = 9;
            try {
                nosp = Integer.parseInt(MessageUtil.getValue("Admin-Command.Market.List.Number-Of-Single-Page"));
            } catch (NumberFormatException ignored) {}
            StringBuilder formatList = new StringBuilder();
            int maxpage = (list.size() / nosp) + 1;
            if (maxpage < page) {
                page = maxpage;
            }
            for (int i = page * nosp - nosp;i < list.size() && i < page * nosp;i++) {
                String format = MessageUtil.getValue("Admin-Command.Market.List.Format").replace("%uid%", String.valueOf(list.get(i).getUID())).replace("%money%", String.valueOf(list.get(i).getShopType().equals(ShopType.BUY) ? list.get(i).getReward() : list.get(i).getPrice())).replace("%owner%", list.get(i).getItemOwner().getName());
                format = format.replace("%item%", LangUtilsHook.getItemName(list.get(i).getItem()));
                formatList.append(format);
            }
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%format%", formatList.toString());
            placeholders.put("%page%", String.valueOf(page));
            placeholders.put("%maxpage%", String.valueOf(maxpage));
            placeholders.put("%nextpage%", String.valueOf(page + 1));
            Map<String, Boolean> visible = new HashMap<>();
            visible.put("{hasnext}", maxpage > page);
            MessageUtil.sendMessage(sender, "Admin-Command.Market.List.Messages", placeholders, visible);
        }
    }

    private void command_market_clear(CommandSender sender, String[] args, GlobalMarket market) {
        if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Market.SubCommands.Clear", true)) return;
        if (marketConfirm.containsKey(sender) && marketConfirm.get(sender).equalsIgnoreCase("ca admin market clear")) {
            market.clearGlobalMarket();
            marketConfirm.remove(sender);
            MessageUtil.sendMessage(sender, "Admin-Command.Market.Clear");
        } else {
            MessageUtil.sendMessage(sender, "Admin-Command.Market.Confirm.Confirm");
            marketConfirm.put(sender, "ca admin market clear");
        }
    }

    private void command_market_repricing(CommandSender sender, String[] args, GlobalMarket market) {
        if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Market.SubCommands.Repricing", true)) return;
        if (args.length <= 4) {
            MessageUtil.sendMessage(sender, "Admin-Command.Market.Repricing.Help");
        } else if (args.length >= 5) {
            long uid;
            double money;
            try {
                uid = Long.parseLong(args[3]);
            } catch (NumberFormatException ex) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("%arg%", args[3]);
                MessageUtil.sendMessage(sender, "Admin-Command.Market.Repricing.Not-A-Valid-Number", placeholders);
                return;
            }
            try {
                money = Double.parseDouble(args[4]);
            } catch (NumberFormatException ex) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("%arg%", args[4]);
                MessageUtil.sendMessage(sender, "Admin-Command.Market.Repricing.Not-A-Valid-Number", placeholders);
                return;
            }
            MarketGoods goods = market.getMarketGoods(uid);
            if (goods == null) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("%uid%", String.valueOf(uid));
                MessageUtil.sendMessage(sender, "Admin-Command.Market.Repricing.Not-Exist", placeholders);
                return;
            }
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%item%", LangUtilsHook.getItemName(goods.getItem()));
            placeholders.put("%uid%", String.valueOf(uid));
            placeholders.put("%money%", String.valueOf(money));
            goods.setPrice(money);
            MessageUtil.sendMessage(sender, "Admin-Command.Market.Repricing.Succeeded", placeholders);
        }
    }

    private void command_market_delete(CommandSender sender, String[] args, GlobalMarket market) {
        if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Market.SubCommands.Delete", true)) return;
        if (args.length == 3) {
            MessageUtil.sendMessage(sender, "Admin-Command.Market.Delete.Help");
        } else if (args.length >= 4) {
            long uid;
            try {
                uid = Long.parseLong(args[3]);
            } catch (NumberFormatException ex) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("%arg%", args[3]);
                MessageUtil.sendMessage(sender, "Admin-Command.Market.Delete.Not-A-Valid-Number", placeholders);
                return;
            }
            MarketGoods goods = market.getMarketGoods(uid);
            if (goods == null) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("%uid%", String.valueOf(uid));
                MessageUtil.sendMessage(sender, "Admin-Command.Market.Delete.Not-Exist", placeholders);
                return;
            }
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%item%", LangUtilsHook.getItemName(goods.getItem()));
            placeholders.put("%uid%", String.valueOf(uid));
            market.removeGoods(uid);
            MessageUtil.sendMessage(sender, "Admin-Command.Market.Delete.Succeeded", placeholders);
        }
    }

    private void command_market_download(CommandSender sender, String[] args, GlobalMarket market) {
        if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Market.SubCommands.Download", true)) return;
        if (PluginControl.getMarketStorageMethod().equals(StorageMethod.YAML)) {
            MessageUtil.sendMessage(sender, "Admin-Command.Market.Download.Only-Database-Mode");
            return;
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
            try (OutputStream out = java.nio.file.Files.newOutputStream(file.toPath())) {
                out.write(market.getYamlData().saveToString().getBytes());
            } catch (IOException ex) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("%error%", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
                MessageUtil.sendMessage(sender, "Admin-Command.Market.Download.Failed", placeholders);
                marketConfirm.remove(sender);
                PluginControl.printStackTrace(ex);
                return;
            }
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%path%", fileName);
            MessageUtil.sendMessage(sender, "Admin-Command.Market.Download.Succeeded", placeholders);
            marketConfirm.remove(sender);
        } else {
            MessageUtil.sendMessage(sender, "Admin-Command.Market.Confirm.Confirm");
            marketConfirm.put(sender, "ca admin market download");
        }
    }

    private void command_market_upload(CommandSender sender, String[] args, GlobalMarket market) {
        if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Market.SubCommands.Upload", true)) return;
        if (PluginControl.getMarketStorageMethod().equals(StorageMethod.YAML)) {
            MessageUtil.sendMessage(sender, "Admin-Command.Market.Upload.Only-Database-Mode");
            return;
        }
        if (marketConfirm.containsKey(sender) && marketConfirm.get(sender).equalsIgnoreCase("ca admin market upload")) {
            String fileName = Files.CONFIG.getFile().getString("Settings.Upload.Market").replace("%date%", new SimpleDateFormat("yyyy-MM-hh-HH-mm-ss").format(new Date()));
            File file = new File(fileName);
            if (!file.exists()) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("%file%", fileName);
                MessageUtil.sendMessage(sender, "Admin-Command.Market.Upload.File-Not-Exist", placeholders);
                marketConfirm.remove(sender);
                return;
            }
            FileConfiguration config = new YamlConfiguration();
            try (Reader reader = new InputStreamReader(java.nio.file.Files.newInputStream(file.toPath()), StandardCharsets.UTF_8)) {
                config.load(reader);
            } catch (IOException | InvalidConfigurationException ex) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("%error%", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
                MessageUtil.sendMessage(sender, "Admin-Command.Market.Upload.Failed", placeholders);
                marketConfirm.remove(sender);
                PluginControl.printStackTrace(ex);
                return;
            }
            switch (PluginControl.getMarketStorageMethod()) {
                case MySQL: {
                    MySQLEngine engine = MySQLEngine.getInstance();
                    try (PreparedStatement statement = engine.getConnection().prepareStatement(
                            "UPDATE " + MySQLEngine.getDatabaseName() + "." + MySQLEngine.getMarketTable() + " SET YamlMarket = ?"
                    )){
                        statement.setString(1, config.saveToString());
                        statement.executeUpdate();
                    } catch (SQLException ex) {
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("%error%", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
                        MessageUtil.sendMessage(sender, "Admin-Command.Market.Upload.Failed", placeholders);
                        marketConfirm.remove(sender);
                        PluginControl.printStackTrace(ex);
                        return;
                    }
                    break;
                }
                case SQLite: {
                    SQLiteEngine engine = SQLiteEngine.getInstance();
                    try (PreparedStatement statement = engine.getConnection().prepareStatement(
                            "UPDATE " + SQLiteEngine.getMarketTable() + " SET YamlMarket = ?"
                    )){
                        statement.setString(1, config.saveToString());
                        statement.executeUpdate();
                    } catch (SQLException ex) {
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("%error%", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
                        MessageUtil.sendMessage(sender, "Admin-Command.Market.Upload.Failed", placeholders);
                        marketConfirm.remove(sender);
                        PluginControl.printStackTrace(ex);
                        return;
                    }
                    break;
                }
            }
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%file%", fileName);
            MessageUtil.sendMessage(sender, "Admin-Command.Market.Upload.Succeeded", placeholders);
            marketConfirm.remove(sender);
        } else {
            MessageUtil.sendMessage(sender, "Admin-Command.Market.Confirm.Confirm");
            marketConfirm.put(sender, "ca admin market upload");
        }
    }

    private void command_player_confirm(CommandSender sender, String[] args) {
        if (args[2].equalsIgnoreCase("confirm")) {
            if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Player.SubCommands.Confirm", true)) return;
            if (itemMailConfirm.containsKey(sender)) {
                Bukkit.dispatchCommand(sender, itemMailConfirm.get(sender));
            } else {
                MessageUtil.sendMessage(sender, "Admin-Command.Player.Confirm.Invalid");
            }
        } else {
            MessageUtil.sendMessage(sender, "Admin-Command.Player.Help");
        }
    }

    private void command_player_list(CommandSender sender, String[] args) {
        if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Player.SubCommands.List", true)) return;
        Player player = Bukkit.getPlayer(args[2]);
        UUID uuid;
        String name;
        if (player != null) {
            uuid = player.getUniqueId();
            name = player.getName();
        } else {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%player%", args[2]);
            MessageUtil.sendMessage(sender, "Admin-Command.Player.List.Please-Wait", placeholders);
            OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(args[2]);
            if (offlineplayer != null) {
                uuid = offlineplayer.getUniqueId();
                name = offlineplayer.getName();
            } else {
                MessageUtil.sendMessage(sender, "Admin-Command.Player.List.Player-Not-Exist", placeholders);
                return;
            }
        }
        if (args.length == 4) {
            List<ItemMail> list = Storage.getPlayer(uuid).getMailBox();
            if (list.isEmpty()) {
                MessageUtil.sendMessage(sender, "Admin-Command.Player.List.Empty");
                return;
            }
            int page = 1;
            int nosp = 9;
            try {
                nosp = Integer.parseInt(MessageUtil.getValue("Admin-Command.Player.List.Number-Of-Single-Page"));
            } catch (NumberFormatException ignored) {}
            StringBuilder formatList = new StringBuilder();
            for (int i = page * nosp - nosp;i < list.size() && i < page * nosp;i++) {
                String format = MessageUtil.getValue("Admin-Command.Player.List.Format").replace("%uid%", String.valueOf(list.get(i).getUID()));
                format = format.replace("%item%", LangUtilsHook.getItemName(list.get(i).getItem()));
                formatList.append(format);
            }
            int maxpage = (list.size() / nosp) + 1;
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%player%", name);
            placeholders.put("%format%", formatList.toString());
            placeholders.put("%page%", String.valueOf(page));
            placeholders.put("%maxpage%", String.valueOf(maxpage));
            placeholders.put("%nextpage%", String.valueOf(page + 1));
            Map<String, Boolean> visible = new HashMap<>();
            visible.put("{hasnext}", maxpage > page);
            MessageUtil.sendMessage(sender, "Admin-Command.Player.List.Messages", placeholders, visible);
        } else if (args.length >= 5) {
            List<ItemMail> list = Storage.getPlayer(uuid).getMailBox();
            if (list.isEmpty()) {
                MessageUtil.sendMessage(sender, "Admin-Command.Player.List.Empty");
                return;
            }
            int page = 1;
            try {
                page = Integer.parseInt(args[4]);
            } catch (NumberFormatException ignored) {}
            int nosp = 9;
            try {
                nosp = Integer.parseInt(MessageUtil.getValue("Admin-Command.Player.List.Number-Of-Single-Page"));
            } catch (NumberFormatException ignored) {}
            StringBuilder formatList = new StringBuilder();
            int maxpage = (list.size() / nosp) + 1;
            if (maxpage < page) {
                page = maxpage;
            }
            for (int i = page * nosp - nosp;i < list.size() && i < page * nosp;i++) {
                String format = MessageUtil.getValue("Admin-Command.Player.List.Format").replace("%uid%", String.valueOf(list.get(i).getUID()));
                format = format.replace("%item%", LangUtilsHook.getItemName(list.get(i).getItem()));
                formatList.append(format);
            }
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%player%", name);
            placeholders.put("%format%", formatList.toString());
            placeholders.put("%page%", String.valueOf(page));
            placeholders.put("%maxpage%", String.valueOf(maxpage));
            placeholders.put("%nextpage%", String.valueOf(page + 1));
            Map<String, Boolean> visible = new HashMap<>();
            visible.put("{hasnext}", maxpage > page);
            MessageUtil.sendMessage(sender, "Admin-Command.Player.List.Messages", placeholders, visible);
        }
    }

    private void command_player_clear(CommandSender sender, String[] args) {
        if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Player.SubCommands.Clear", true)) return;
        if (args.length == 4) {
            MessageUtil.sendMessage(sender, "Admin-Command.Player.Clear.Help");
            return;
        }
        Player player = Bukkit.getPlayer(args[2]);
        UUID uuid;
        String name;
        if (player != null) {
            uuid = player.getUniqueId();
            name = player.getName();
        } else {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%player%", args[2]);
            MessageUtil.sendMessage(sender, "Admin-Command.Player.Clear.Please-Wait", placeholders);
            OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(args[2]);
            if (offlineplayer != null) {
                uuid = offlineplayer.getUniqueId();
                name = offlineplayer.getName();
            } else {
                MessageUtil.sendMessage(sender, "Admin-Command.Player.Clear.Player-Not-Exist", placeholders);
                return;
            }
        }
        if (args[4].equalsIgnoreCase("market")) {
            GlobalMarket market = GlobalMarket.getMarket();
            if (itemMailConfirm.containsKey(sender) && itemMailConfirm.get(sender).equalsIgnoreCase("ca admin player " + name + " clear market")) {
                List<MarketGoods> marketGoods = market.getItems();
                for (int i = marketGoods.size() - 1;i > -1;i--) {
                    MarketGoods goods = marketGoods.get(i);
                    if (goods.getItemOwner().getUUID().equals(uuid)) {
                        market.removeGoodsFromCache(goods);
                    }
                }
                market.saveData();
                itemMailConfirm.remove(sender);
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("%player%", name);
                MessageUtil.sendMessage(sender, "Admin-Command.Player.Clear.Market", placeholders);
            } else {
                MessageUtil.sendMessage(sender, "Admin-Command.Player.Confirm.Confirm");
                itemMailConfirm.put(sender, "ca admin player " + name + " clear market");
            }
        } else if (args[4].equalsIgnoreCase("mail")) {
            if (itemMailConfirm.containsKey(sender) && itemMailConfirm.get(sender).equalsIgnoreCase("ca admin player " + name + " clear mail")) {
                Storage.getPlayer(uuid).clearMailBox();
                itemMailConfirm.remove(sender);
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("%player%", name);
                MessageUtil.sendMessage(sender, "Admin-Command.Player.Clear.ItemMail", placeholders);
            } else {
                MessageUtil.sendMessage(sender, "Admin-Command.Player.Confirm.Confirm");
                itemMailConfirm.put(sender, "ca admin player " + name + " clear mail");
            }
        } else {
            MessageUtil.sendMessage(sender, "Admin-Command.Player.Clear.Help");
        }
    }

    private void command_player_delete(CommandSender sender, String[] args) {
        if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Player.SubCommands.Delete", true)) return;
        if (args.length == 4) {
            MessageUtil.sendMessage(sender, "Admin-Command.Player.Delete.Help");
        } else if (args.length >= 5) {
            Player player = Bukkit.getPlayer(args[2]);
            UUID uuid;
            String name;
            if (player != null) {
                uuid = player.getUniqueId();
                name = player.getName();
            } else {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("%player%", args[2]);
                MessageUtil.sendMessage(sender, "Admin-Command.Player.Delete.Please-Wait", placeholders);
                OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(args[2]);
                if (offlineplayer != null) {
                    uuid = offlineplayer.getUniqueId();
                    name = offlineplayer.getName();
                } else {
                    MessageUtil.sendMessage(sender, "Admin-Command.Player.Delete.Player-Not-Exist", placeholders);
                    return;
                }
            }
            Storage playerData = Storage.getPlayer(uuid);
            long uid;
            try {
                uid = Long.parseLong(args[4]);
            } catch (NumberFormatException ex) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("%arg%", args[4]);
                MessageUtil.sendMessage(sender, "Admin-Command.Player.Delete.Not-A-Valid-Number", placeholders);
                return;
            }
            ItemMail mail = playerData.getMail(uid);
            if (mail == null) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("%uid%", String.valueOf(uid));
                MessageUtil.sendMessage(sender, "Admin-Command.Player.Delete.Not-Exist", placeholders);
                return;
            }
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%item%", LangUtilsHook.getItemName(mail.getItem()));
            placeholders.put("%uid%", String.valueOf(uid));
            placeholders.put("%player%", name);
            playerData.removeItem(mail);
            MessageUtil.sendMessage(sender, "Admin-Command.Player.Delete.Succeeded", placeholders);
        }
    }

    private void command_player_view(CommandSender sender, String[] args) {
        if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Player.SubCommands.View", true)) return;
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, "Admin-Command.Player.View.Player-Only");
            return;
        }
        Player player = Bukkit.getPlayer(args[2]);
        UUID uuid;
        String name;
        if (player != null) {
            uuid = player.getUniqueId();
            name = player.getName();
        } else {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%player%", args[2]);
            MessageUtil.sendMessage(sender, "Admin-Command.Player.View.Please-Wait", placeholders);
            OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(args[2]);
            if (offlineplayer != null) {
                uuid = offlineplayer.getUniqueId();
                name = offlineplayer.getName();
            } else {
                MessageUtil.sendMessage(sender, "Admin-Command.Player.View.Player-Not-Exist", placeholders);
                return;
            }
        }
        GUI.openPlayersMail((Player) sender, 1, uuid);
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("%player%", name);
        MessageUtil.sendMessage(sender, "Admin-Command.Player.View.Succeeded", placeholders);
    }

    private void command_player_download(CommandSender sender, String[] args) {
        if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Player.SubCommands.Download", true)) return;
        Player player = Bukkit.getPlayer(args[2]);
        UUID uuid;
        String name;
        if (player != null) {
            uuid = player.getUniqueId();
            name = player.getName();
        } else {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%player%", args[2]);
            MessageUtil.sendMessage(sender, "Admin-Command.Player.Download.Please-Wait", placeholders);
            OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(args[2]);
            if (offlineplayer != null) {
                uuid = offlineplayer.getUniqueId();
                name = offlineplayer.getName();
                name = name == null ? "null" : name;
            } else {
                MessageUtil.sendMessage(sender, "Admin-Command.Player.Download.Player-Not-Exist", placeholders);
                return;
            }
        }
        if (PluginControl.getItemMailStorageMethod().equals(StorageMethod.YAML)) {
            MessageUtil.sendMessage(sender, "Admin-Command.Player.Download.Only-Database-Mode");
            return;
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
            try (OutputStream out = java.nio.file.Files.newOutputStream(file.toPath())) {
                out.write(Storage.getPlayer(uuid).getYamlData().saveToString().getBytes());
            } catch (IOException ex) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("%error%", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
                MessageUtil.sendMessage(sender, "Admin-Command.Player.Download.Failed", placeholders);
                itemMailConfirm.remove(sender);
                PluginControl.printStackTrace(ex);
                return;
            }
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%path%", fileName);
            placeholders.put("%player%", name);
            MessageUtil.sendMessage(sender, "Admin-Command.Player.Download.Succeeded", placeholders);
            itemMailConfirm.remove(sender);
        } else {
            MessageUtil.sendMessage(sender, "Admin-Command.Player.Confirm.Confirm");
            itemMailConfirm.put(sender, "ca admin player " + name + " download");
        }
    }

    private void command_player_upload(CommandSender sender, String[] args) {
        if (!PluginControl.hasCommandPermission(sender, "Admin.SubCommands.Player.SubCommands.Upload", true)) return;
        Player player = Bukkit.getPlayer(args[2]);
        UUID uuid;
        String name;
        if (player != null) {
            uuid = player.getUniqueId();
            name = player.getName();
        } else {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%player%", args[2]);
            MessageUtil.sendMessage(sender, "Admin-Command.Player.Upload.Please-Wait", placeholders);
            OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(args[2]);
            if (offlineplayer != null) {
                uuid = offlineplayer.getUniqueId();
                name = offlineplayer.getName();
                if (name == null) name = "null";
            } else {
                MessageUtil.sendMessage(sender, "Admin-Command.Player.Upload.Player-Not-Exist", placeholders);
                return;
            }
        }
        if (PluginControl.getItemMailStorageMethod().equals(StorageMethod.YAML)) {
            MessageUtil.sendMessage(sender, "Admin-Command.Player.Download.Only-Database-Mode");
            return;
        }
        if (itemMailConfirm.containsKey(sender) && itemMailConfirm.get(sender).equalsIgnoreCase("ca admin player " + name + " upload")) {
            String fileName = Files.CONFIG.getFile().getString("Settings.Upload.PlayerData").replace("%player%", name).replace("%uuid%", uuid.toString()).replace("%date%", new SimpleDateFormat("yyyy-MM-hh-HH-mm-ss").format(new Date()));
            File file = new File(fileName);
            if (!file.exists()) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("%file%", fileName);
                MessageUtil.sendMessage(sender, "Admin-Command.Player.Upload.File-Not-Exist", placeholders);
                itemMailConfirm.remove(sender);
                return;
            }
            FileConfiguration config = new YamlConfiguration();
            try (Reader reader = new InputStreamReader(java.nio.file.Files.newInputStream(file.toPath()), StandardCharsets.UTF_8)) {
                config.load(reader);
            } catch (IOException | InvalidConfigurationException ex) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("%error%", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
                MessageUtil.sendMessage(sender, "Admin-Command.Player.Upload.Failed", placeholders);
                itemMailConfirm.remove(sender);
                PluginControl.printStackTrace(ex);
                return;
            }
            switch (PluginControl.getMarketStorageMethod()) {
                case MySQL: {
                    MySQLEngine engine = MySQLEngine.getInstance();
                    try (PreparedStatement statement = engine.getConnection().prepareStatement(
                            "UPDATE " + MySQLEngine.getDatabaseName() + "." + MySQLEngine.getItemMailTable() + " SET YamlData = ? WHERE UUID = ?"
                    )) {
                        statement.setString(1, config.saveToString());
                        statement.setString(2, uuid.toString());
                        statement.executeUpdate();
                    } catch (SQLException ex) {
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("%error%", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
                        MessageUtil.sendMessage(sender, "Admin-Command.Player.Upload.Failed", placeholders);
                        itemMailConfirm.remove(sender);
                        PluginControl.printStackTrace(ex);
                        return;
                    }
                    break;
                }
                case SQLite: {
                    SQLiteEngine engine = SQLiteEngine.getInstance();
                    try (PreparedStatement statement = engine.getConnection().prepareStatement(
                            "UPDATE " + SQLiteEngine.getItemMailTable() + " SET YamlMarket = ? WHERE UUID = ?"
                    )) {
                        statement.setString(1, config.saveToString());
                        statement.setString(2, uuid.toString());
                        statement.executeUpdate();
                    } catch (SQLException ex) {
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("%error%", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
                        MessageUtil.sendMessage(sender, "Admin-Command.Player.Upload.Failed", placeholders);
                        itemMailConfirm.remove(sender);
                        PluginControl.printStackTrace(ex);
                        return;
                    }
                    break;
                }
            }
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%file%", fileName);
            placeholders.put("%player%", name);
            MessageUtil.sendMessage(sender, "Admin-Command.Player.Upload.Succeeded", placeholders);
            itemMailConfirm.remove(sender);
        } else {
            MessageUtil.sendMessage(sender, "Admin-Command.Player.Confirm.Confirm");
            itemMailConfirm.put(sender, "ca admin player " + name + " upload");
        }
    }
}
