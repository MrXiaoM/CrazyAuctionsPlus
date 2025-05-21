package studio.trc.bukkit.crazyauctionsplus.command.subcommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import studio.trc.bukkit.crazyauctionsplus.command.CrazyAuctionsSubCommand;
import studio.trc.bukkit.crazyauctionsplus.command.CrazyAuctionsSubCommandType;
import studio.trc.bukkit.crazyauctionsplus.event.GUIAction;
import studio.trc.bukkit.crazyauctionsplus.util.Category;
import studio.trc.bukkit.crazyauctionsplus.util.FileManager.Files;
import studio.trc.bukkit.crazyauctionsplus.util.MessageUtil;
import studio.trc.bukkit.crazyauctionsplus.util.PluginControl;
import studio.trc.bukkit.crazyauctionsplus.util.enums.ShopType;

public class GUICommand
    implements CrazyAuctionsSubCommand
{
    @Override
    public void execute(CommandSender sender, String subCommand, String... args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, "Players-Only");
            return;
        }
        if (!PluginControl.hasCommandPermission(sender, "Gui", true)) return;
        Player player = (Player) sender;
        if (PluginControl.isWorldDisabled(player)) {
            MessageUtil.sendMessage(sender, "World-Disabled");
            return;
        }
        if (args.length == 1) {
            if (Files.CONFIG.getFile().getBoolean("Settings.Category-Page-Opens-First")) {
                GUIAction.setShopType(player, ShopType.ANY);
                GUIAction.setCategory(player, Category.getDefaultCategory());
                GUIAction.openCategories(player, ShopType.ANY);
            } else {
                GUIAction.openShop(player, ShopType.ANY, Category.getDefaultCategory(), 1);
            }
        } else if (args.length == 2) {
            if (args[1].equalsIgnoreCase("sell")) {
                GUIAction.openShop(player, ShopType.SELL, Category.getDefaultCategory(), 1);
            } else if (args[1].equalsIgnoreCase("buy")) {
                GUIAction.openShop(player, ShopType.BUY, Category.getDefaultCategory(), 1);
            } else if (args[1].equalsIgnoreCase("bid")) {
                GUIAction.openShop(player, ShopType.BID, Category.getDefaultCategory(), 1);
            } else {
                GUIAction.openShop(player, ShopType.ANY, Category.getDefaultCategory(), 1);
            }
        } else if (args.length >= 3) {
            if (!PluginControl.hasCommandPermission(sender, "Gui-Others-Player", true)) return;
            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                MessageUtil.sendMessage(sender, "Not-Online");
                return;
            }
            if (args[1].equalsIgnoreCase("sell")) {
                GUIAction.openShop(target, ShopType.SELL, Category.getDefaultCategory(), 1);
            } else if (args[1].equalsIgnoreCase("buy")) {
                GUIAction.openShop(target, ShopType.BUY, Category.getDefaultCategory(), 1);
            } else if (args[1].equalsIgnoreCase("bid")) {
                GUIAction.openShop(target, ShopType.BID, Category.getDefaultCategory(), 1);
            } else {
                GUIAction.openShop(target, ShopType.ANY, Category.getDefaultCategory(), 1);
            }
        }
    }

    @Override
    public String getName() {
        return "gui";
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String subCommand, String... args) {
        if (args.length == 2) {
            return getTabElements(args, 2, Arrays.asList("sell", "buy", "bid"));
        }
        if (args.length == 3) {
            return getTabPlayersName(args, 3);
        }
        return new ArrayList<>();
    }

    @Override
    public CrazyAuctionsSubCommandType getCommandType() {
        return CrazyAuctionsSubCommandType.GUI;
    }
}
