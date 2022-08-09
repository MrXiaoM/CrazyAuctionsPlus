package studio.trc.bukkit.crazyauctionsplus.command.subcommand;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

import studio.trc.bukkit.crazyauctionsplus.command.CrazyAuctionsSubCommand;
import studio.trc.bukkit.crazyauctionsplus.command.CrazyAuctionsSubCommandType;
import studio.trc.bukkit.crazyauctionsplus.util.MessageUtil;
import studio.trc.bukkit.crazyauctionsplus.util.PluginControl;

public class HelpCommand
    implements CrazyAuctionsSubCommand
{
    @Override
    public void execute(CommandSender sender, String subCommand, String... args) {
        if (!PluginControl.hasCommandPermission(sender, "Help", true)) return;
        MessageUtil.sendMessage(sender, "Help-Menu");
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public CrazyAuctionsSubCommandType getCommandType() {
        return CrazyAuctionsSubCommandType.HELP;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String subCommand, String... args) {
        return new ArrayList();
    }
}
