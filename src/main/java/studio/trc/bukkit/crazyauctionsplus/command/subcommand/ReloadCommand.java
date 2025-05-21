package studio.trc.bukkit.crazyauctionsplus.command.subcommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import studio.trc.bukkit.crazyauctionsplus.command.CrazyAuctionsSubCommand;
import studio.trc.bukkit.crazyauctionsplus.command.CrazyAuctionsSubCommandType;
import studio.trc.bukkit.crazyauctionsplus.util.GUI;
import studio.trc.bukkit.crazyauctionsplus.util.MessageUtil;
import studio.trc.bukkit.crazyauctionsplus.util.PluginControl;

public class ReloadCommand
    implements CrazyAuctionsSubCommand
{

    @Override
    public void execute(CommandSender sender, String subCommand, String... args) {
        if (!PluginControl.hasCommandPermission(sender, "Reload", true)) return;
        if (args.length == 1) {
            PluginControl.reload(PluginControl.ReloadType.ALL);
            Bukkit.getOnlinePlayers().stream().filter(player -> GUI.openingGUI.containsKey(player.getUniqueId())).forEach(player -> {
                player.closeInventory();
            });
            MessageUtil.sendMessage(sender, "Reload");
        } else if (args.length >= 2) {
            if (args[1].equalsIgnoreCase("database")) {
                if (!PluginControl.hasCommandPermission(sender, "Reload.SubCommands.Database", true)) return;
                Bukkit.getOnlinePlayers().stream().filter(player -> GUI.openingGUI.containsKey(player.getUniqueId())).forEach(Player::closeInventory);
                PluginControl.reload(PluginControl.ReloadType.DATABASE);
                MessageUtil.sendMessage(sender, "Reload-Database");
            } else if (args[1].equalsIgnoreCase("config")) {
                if (!PluginControl.hasCommandPermission(sender, "Reload.SubCommands.Config", true)) return;
                Bukkit.getOnlinePlayers().stream().filter(player -> GUI.openingGUI.containsKey(player.getUniqueId())).forEach(Player::closeInventory);
                PluginControl.reload(PluginControl.ReloadType.CONFIG);
                MessageUtil.sendMessage(sender, "Reload-Config");
            } else if (args[1].equalsIgnoreCase("market")) {
                if (!PluginControl.hasCommandPermission(sender, "Reload.SubCommands.Market", true)) return;
                Bukkit.getOnlinePlayers().stream().filter(player -> GUI.openingGUI.containsKey(player.getUniqueId())).forEach(Player::closeInventory);
                PluginControl.reload(PluginControl.ReloadType.MARKET);
                MessageUtil.sendMessage(sender, "Reload-Market");
            } else if (args[1].equalsIgnoreCase("messages")) {
                if (!PluginControl.hasCommandPermission(sender, "Reload.SubCommands.Messages", true)) return;
                Bukkit.getOnlinePlayers().stream().filter(player -> GUI.openingGUI.containsKey(player.getUniqueId())).forEach(Player::closeInventory);
                PluginControl.reload(PluginControl.ReloadType.MESSAGES);
                MessageUtil.sendMessage(sender, "Reload-Messages");
            } else if (args[1].equalsIgnoreCase("playerdata")) {
                if (!PluginControl.hasCommandPermission(sender, "Reload.SubCommands.PlayerData", true)) return;
                Bukkit.getOnlinePlayers().stream().filter(player -> GUI.openingGUI.containsKey(player.getUniqueId())).forEach(Player::closeInventory);
                PluginControl.reload(PluginControl.ReloadType.PLAYERDATA);
                MessageUtil.sendMessage(sender, "Reload-PlayerData");
            } else if (args[1].equalsIgnoreCase("category")) {
                if (!PluginControl.hasCommandPermission(sender, "Reload.SubCommands.Category", true)) return;
                Bukkit.getOnlinePlayers().stream().filter(player -> GUI.openingGUI.containsKey(player.getUniqueId())).forEach(Player::closeInventory);
                PluginControl.reload(PluginControl.ReloadType.CATEGORY);
                MessageUtil.sendMessage(sender, "Reload-Category");
            } else if (args[1].equalsIgnoreCase("itemcollection")) {
                if (!PluginControl.hasCommandPermission(sender, "Reload.SubCommands.ItemCollection", true)) return;
                Bukkit.getOnlinePlayers().stream().filter(player -> GUI.openingGUI.containsKey(player.getUniqueId())).forEach(Player::closeInventory);
                PluginControl.reload(PluginControl.ReloadType.ITEMCOLLECTION);
                MessageUtil.sendMessage(sender, "Reload-ItemCollection");
            } else {
                PluginControl.reload(PluginControl.ReloadType.ALL);
                MessageUtil.sendMessage(sender, "Reload");
            }
        }
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String subCommand, String... args) {
        if (args.length == 2) {
            return getTabElements(args, 2, Arrays.asList("database", "config", "market", "messages", "playerdata", "category", "itemcollection", "all"));
        }
        return new ArrayList();
    }

    @Override
    public CrazyAuctionsSubCommandType getCommandType() {
        return CrazyAuctionsSubCommandType.RELOAD;
    }
}
