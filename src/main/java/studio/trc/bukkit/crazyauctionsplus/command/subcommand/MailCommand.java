package studio.trc.bukkit.crazyauctionsplus.command.subcommand;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import studio.trc.bukkit.crazyauctionsplus.command.CrazyAuctionsSubCommand;
import studio.trc.bukkit.crazyauctionsplus.command.CrazyAuctionsSubCommandType;
import studio.trc.bukkit.crazyauctionsplus.event.GUIAction;
import studio.trc.bukkit.crazyauctionsplus.util.MessageUtil;
import studio.trc.bukkit.crazyauctionsplus.util.PluginControl;

public class MailCommand
    implements CrazyAuctionsSubCommand
{

    @Override
    public void execute(CommandSender sender, String subCommand, String... args) {
        if (!PluginControl.hasCommandPermission(sender, "Mail", true)) return;
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, "Players-Only");
            return;
        }
        Player player = (Player) sender;
        if (PluginControl.isWorldDisabled(player)) {
            MessageUtil.sendMessage(sender, "World-Disabled");
            return;
        }
        GUIAction.openPlayersMail(player, 1);
    }

    @Override
    public String getName() {
        return "mail";
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String subCommand, String... args) {
        return new ArrayList();
    }

    @Override
    public CrazyAuctionsSubCommandType getCommandType() {
        return CrazyAuctionsSubCommandType.MAIL;
    }
}
