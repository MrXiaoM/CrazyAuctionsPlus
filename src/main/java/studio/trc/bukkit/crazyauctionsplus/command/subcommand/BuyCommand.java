package studio.trc.bukkit.crazyauctionsplus.command.subcommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import studio.trc.bukkit.crazyauctionsplus.api.events.AuctionListEvent;
import studio.trc.bukkit.crazyauctionsplus.command.CrazyAuctionsCommand;
import studio.trc.bukkit.crazyauctionsplus.command.CrazyAuctionsSubCommand;
import studio.trc.bukkit.crazyauctionsplus.command.CrazyAuctionsSubCommandType;
import studio.trc.bukkit.crazyauctionsplus.currency.CurrencyManager;
import studio.trc.bukkit.crazyauctionsplus.database.GlobalMarket;
import studio.trc.bukkit.crazyauctionsplus.util.*;
import studio.trc.bukkit.crazyauctionsplus.util.enums.ShopType;

public class BuyCommand
    implements CrazyAuctionsSubCommand
{
    @Override
    public void execute(CommandSender sender, String subCommand, String... args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, "Players-Only");
            return;
        }
        if (args.length == 1) {
            MessageUtil.sendMessage(sender, "CrazyAuctions-Buy");
            return;
        }
        if (args.length >= 2) {
            Player player = (Player) sender;
            if (PluginControl.isWorldDisabled(player)) {
                MessageUtil.sendMessage(sender, "World-Disabled");
                return;
            }
            if (!CrazyAuctionsCommand.getCrazyAuctions().isBuyingEnabled()) {
                MessageUtil.sendMessage(player, "Buying-Disabled");
                return;
            }
            if (!PluginControl.hasCommandPermission(player, "Buy", true)) return;
            if (!PluginControl.isNumber(args[1])) {
                Map<String, String> placeholders = new HashMap();
                placeholders.put("%arg%", args[1]);
                MessageUtil.sendMessage(player, "Not-A-Valid-Number", placeholders);
                return;
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
                MessageUtil.sendMessage(player, "Need-More-Money", placeholders);
                return;
            }
            if (reward < FileManager.Files.CONFIG.getFile().getDouble("Settings.Minimum-Buy-Reward")) {
                Map<String, String> placeholders = new HashMap();
                placeholders.put("%reward%", String.valueOf(FileManager.Files.CONFIG.getFile().getDouble("Settings.Minimum-Buy-Reward")));
                MessageUtil.sendMessage(player, "Buy-Reward-To-Low", placeholders);
                return;
            }
            if (reward > FileManager.Files.CONFIG.getFile().getDouble("Settings.Max-Beginning-Buy-Reward")) {
                Map<String, String> placeholders = new HashMap();
                placeholders.put("%reward%", String.valueOf(FileManager.Files.CONFIG.getFile().getDouble("Settings.Max-Beginning-Buy-Reward")));
                MessageUtil.sendMessage(player, "Buy-Reward-To-High", placeholders);
                return;
            }
            if (!PluginControl.bypassLimit(player, ShopType.BUY)) {
                int limit = PluginControl.getLimit(player, ShopType.BUY);
                if (limit > -1) {
                    if (CrazyAuctionsCommand.getCrazyAuctions().getNumberOfPlayerItems(player, ShopType.BUY) >= limit) {
                        Map<String, String> placeholders = new HashMap();
                        placeholders.put("%number%", String.valueOf(limit));
                        MessageUtil.sendMessage(player, "Max-Buying-Items", placeholders);
                        return;
                    }
                }
            }
            int amount = 1;
            if (args.length >= 3) {
                if (!PluginControl.isInt(args[2])) {
                    Map<String, String> placeholders = new HashMap();
                    placeholders.put("%arg%", args[1]);
                    MessageUtil.sendMessage(player, "Not-A-Valid-Number", placeholders);
                    return;
                } else {
                    amount = Integer.valueOf(args[2]);
                }
            }
            if (amount > 64) {
                MessageUtil.sendMessage(player, "Too-Many-Items");
                return;
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
                    MessageUtil.sendMessage(sender, "Unknown-Item", placeholders);
                    return;
                }
                if (item.getType().equals(Material.AIR)) {
                    Map<String, String> placeholders = new HashMap();
                    placeholders.put("%item%", args[3]);
                    MessageUtil.sendMessage(sender, "Unknown-Item", placeholders);
                    return;
                }
            } else if (PluginControl.getItemInHand(player).getType() != Material.AIR) {
                item = PluginControl.getItemInHand(player).clone();
            } else {
                MessageUtil.sendMessage(sender, "CrazyAuctions-Buy");
                return;
            }
            if (PluginControl.isItemBlacklisted(item)) {
                MessageUtil.sendMessage(player, "Item-BlackListed");
                return;
            }
            if (PluginControl.isItemLoreBlacklisted(item)) {
                MessageUtil.sendMessage(player, "Item-LoreBlackListed");
                return;
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
            placeholders.put("%item%", LangUtilsHook.getItemName(item));
            MessageUtil.sendMessage(player, "Added-Item-For-Acquisition", placeholders);
        }
    }

    @Override
    public String getName() {
        return "buy";
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String subCommand, String... args) {
        if (args.length == 4) {
            return getTabElements(args, 4, Arrays.stream(Material.values()).map(material -> material.name()).collect(Collectors.toList()));
        }
        return new ArrayList();
    }

    @Override
    public CrazyAuctionsSubCommandType getCommandType() {
        return CrazyAuctionsSubCommandType.BUY;
    }
}
