package studio.trc.bukkit.crazyauctionsplus.command.subcommand;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import studio.trc.bukkit.crazyauctionsplus.Main;
import studio.trc.bukkit.crazyauctionsplus.command.CrazyAuctionsSubCommand;
import studio.trc.bukkit.crazyauctionsplus.command.CrazyAuctionsSubCommandType;
import studio.trc.bukkit.crazyauctionsplus.event.GUIAction;
import studio.trc.bukkit.crazyauctionsplus.util.MessageUtil;
import studio.trc.bukkit.crazyauctionsplus.util.PluginControl;

public class ViewCommand
    implements CrazyAuctionsSubCommand
{
    @Override
    public void execute(CommandSender sender, String subCommand, String... args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, "Players-Only");
            return;
        }
        Player player = (Player) sender;
        if (PluginControl.isWorldDisabled(player)) {
            MessageUtil.sendMessage(sender, "World-Disabled");
            return;
        }
        if (args.length == 1) {
            if (!PluginControl.hasCommandPermission(sender, "View", true)) return;
            GUIAction.openViewer(player, player.getUniqueId(), 0);
            return;
        }
        if (args.length >= 2) {
            if (!PluginControl.hasCommandPermission(sender, "View-Others-Player", true)) return;
            Player target = Bukkit.getPlayer(args[1]);
            if (target != null) {
                GUIAction.openViewer(player, target.getUniqueId(), 1);
            } else {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        GUIAction.openViewer(player, Bukkit.getOfflinePlayer(args[1]).getUniqueId(), 1);
                    }
                }.runTaskLater(Main.getInstance(), 1);
            }
            return;
        }
        MessageUtil.sendMessage(sender, "CrazyAuctions-View");
    }

    @Override
    public String getName() {
        return "view";
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String subCommand, String... args) {
        if (args.length >= 2) {
            if (PluginControl.hasCommandPermission(sender, "View-Others-Player", false)) {
                return getTabPlayersName(args, args.length);
            }
        }
        return new ArrayList<>();
    }

    @Override
    public CrazyAuctionsSubCommandType getCommandType() {
        return CrazyAuctionsSubCommandType.VIEW;
    }
}
