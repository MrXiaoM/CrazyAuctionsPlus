package studio.trc.bukkit.crazyauctionsplus.command.subcommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import studio.trc.bukkit.crazyauctionsplus.util.FileManager;
import studio.trc.bukkit.crazyauctionsplus.util.ItemOwner;
import studio.trc.bukkit.crazyauctionsplus.util.MarketGoods;
import studio.trc.bukkit.crazyauctionsplus.util.MessageUtil;
import studio.trc.bukkit.crazyauctionsplus.util.PluginControl;
import studio.trc.bukkit.crazyauctionsplus.util.enums.ShopType;
import studio.trc.bukkit.crazyauctionsplus.util.enums.Version;

public class BidCommand
    implements CrazyAuctionsSubCommand
{
    @Override
    public void execute(CommandSender sender, String subCommand, String... args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, "Players-Only");
            return;
        }
        if (args.length >= 2) {
            Player player = (Player) sender;
            if (PluginControl.isWorldDisabled(player)) {
                MessageUtil.sendMessage(sender, "World-Disabled");
                return;
            }
            ShopType type = ShopType.BID;
            if (!CrazyAuctionsCommand.getCrazyAuctions().isBiddingEnabled()) {
                MessageUtil.sendMessage(player, "Bidding-Disabled");
                return;
            }
            if (!PluginControl.hasCommandPermission(player, "Bid", true)) {
                MessageUtil.sendMessage(player, "No-Permission");
                return;
            }
            ItemStack item = PluginControl.getItemInHand(player);
            int amount = item.getAmount();
            if (args.length >= 3) {
                if (!PluginControl.isInt(args[2])) {
                    Map<String, String> placeholders = new HashMap();
                    placeholders.put("%arg%", args[2]);
                    MessageUtil.sendMessage(player, "Not-A-Valid-Number", placeholders);
                    return;
                }
                amount = Integer.parseInt(args[2]);
                if (amount <= 0) amount = 1;
                if (amount > item.getAmount()) amount = item.getAmount();
            }
            if (PluginControl.getItemInHand(player).getType() == Material.AIR) {
                MessageUtil.sendMessage(player, "Doesnt-Have-Item-In-Hand");
                return;
            }
            if (!PluginControl.isNumber(args[1])) {
                Map<String, String> placeholders = new HashMap();
                placeholders.put("%arg%", args[1]);
                MessageUtil.sendMessage(player, "Not-A-Valid-Number", placeholders);
                return;
            }
            double price = Double.valueOf(args[1]);
            double tax = 0;
            if (price < FileManager.Files.CONFIG.getFile().getDouble("Settings.Minimum-Bid-Price")) {
                Map<String, String> placeholders = new HashMap();
                placeholders.put("%price%", String.valueOf(FileManager.Files.CONFIG.getFile().getDouble("Settings.Minimum-Bid-Price")));
                MessageUtil.sendMessage(player, "Bid-Price-To-Low", placeholders);
                return;
            }
            if (price > FileManager.Files.CONFIG.getFile().getDouble("Settings.Max-Beginning-Bid-Price")) {
                Map<String, String> placeholders = new HashMap();
                placeholders.put("%price%", String.valueOf(FileManager.Files.CONFIG.getFile().getDouble("Settings.Max-Beginning-Bid-Price")));
                MessageUtil.sendMessage(player, "Bid-Price-To-High", placeholders);
                return;
            }
            if (!PluginControl.bypassLimit(player, ShopType.BID)) {
                int limit = PluginControl.getLimit(player, ShopType.BID);
                if (limit > -1) {
                    if (CrazyAuctionsCommand.getCrazyAuctions().getNumberOfPlayerItems(player, ShopType.BID) >= limit) {
                        Map<String, String> placeholders = new HashMap();
                        placeholders.put("%number%", String.valueOf(limit));
                        MessageUtil.sendMessage(player, "Max-Bidding-Items", placeholders);
                        return;
                    }
                }
            }
            if (!PluginControl.bypassTaxRate(player, ShopType.BID)) {
                tax = price * PluginControl.getTaxRate(player, ShopType.BID);
                if (CurrencyManager.getMoney(player) < tax) { 
                    HashMap<String, String> placeholders = new HashMap();
                    placeholders.put("%Money_Needed%", String.valueOf(tax - CurrencyManager.getMoney(player)));
                    placeholders.put("%money_needed%", String.valueOf(tax - CurrencyManager.getMoney(player)));
                    MessageUtil.sendMessage(player, "Need-More-Money", placeholders);
                    return;
                }
            }
            if (PluginControl.isItemBlacklisted(item)) {
                MessageUtil.sendMessage(player, "Item-BlackListed");
                return;
            }
            if (PluginControl.isItemLoreBlacklisted(item)) {
                MessageUtil.sendMessage(player, "Item-LoreBlackListed");
                return;
            }
            if (!FileManager.Files.CONFIG.getFile().getBoolean("Settings.Allow-Damaged-Items")) {
                for (Material i : getDamageableItems()) {
                    if (item.getType() == i) {
                        if (item.getDurability() > 0) {
                            MessageUtil.sendMessage(player, "Item-Damaged");
                            return;
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
                PluginControl.convertToMill(FileManager.Files.CONFIG.getFile().getString("Settings.Bid-Time")),
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
            placeholders.put("%tax%", String.valueOf(tax));
            MessageUtil.sendMessage(player, "Added-Item-For-Bid", placeholders);
            if (item.getAmount() <= 1 || (item.getAmount() - amount) <= 0) {
                PluginControl.setItemInHand(player, new ItemStack(Material.AIR));
            } else {
                item.setAmount(item.getAmount() - amount);
            }
            return;
        }
        MessageUtil.sendMessage(sender, "CrazyAuctions-Bid");
    }

    @Override
    public String getName() {
        return "bid";
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String subCommand, String... args) {
        return new ArrayList();
    }

    @Override
    public CrazyAuctionsSubCommandType getCommandType() {
        return CrazyAuctionsSubCommandType.BID;
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
}
