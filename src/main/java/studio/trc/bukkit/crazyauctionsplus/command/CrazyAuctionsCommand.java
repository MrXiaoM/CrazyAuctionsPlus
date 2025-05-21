package studio.trc.bukkit.crazyauctionsplus.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import studio.trc.bukkit.crazyauctionsplus.Main;
import studio.trc.bukkit.crazyauctionsplus.util.CrazyAuctions;
import studio.trc.bukkit.crazyauctionsplus.util.FileManager;
import studio.trc.bukkit.crazyauctionsplus.util.MessageUtil;
import studio.trc.bukkit.crazyauctionsplus.util.PluginControl;

public class CrazyAuctionsCommand
    implements CommandExecutor, TabCompleter
{
    @Getter
    private static final Map<String, CrazyAuctionsSubCommand> subCommands = new HashMap();
    @Getter
    private static final FileManager fileManager = FileManager.getInstance();
    @Getter
    private static final CrazyAuctions crazyAuctions = CrazyAuctions.getInstance();
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        PluginControl.checkUpdate();
        if (args.length == 0) {
            if (!PluginControl.hasCommandPermission(sender, "Access", true)) return true;
            Map<String, String> placeholders = new HashMap();
            placeholders.put("%version%", Main.getInstance().getDescription().getVersion());
            MessageUtil.sendMessage(sender, "CrazyAuctions-Main", placeholders);
        } else if (args.length >= 1) {
            if (FileManager.isBackingUp()) {
                MessageUtil.sendMessage(sender, "Admin-Command.Backup.BackingUp");
                return true;
            }
            if (FileManager.isRollingBack()) {
                MessageUtil.sendMessage(sender, "Admin-Command.RollBack.RollingBack");
                return true;
            }
            callSubCommand(sender, args);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return getNormallyTabComplete(sender, args[0]);
        } else if (args.length > 1) {
            return tabComplete(sender, args);
        } else {
            return new ArrayList();
        }
    }
    
    private List<String> tabComplete(CommandSender sender, String[] args) {
        String subCommand = args[0].toLowerCase();
        if (subCommands.get(subCommand) == null) {
            return new ArrayList();
        }
        CrazyAuctionsSubCommand command = subCommands.get(subCommand);
        return PluginControl.hasCommandPermission(sender, command.getCommandType().getCommandPermissionPath(), false) ? subCommands.get(subCommand).tabComplete(sender, subCommand, args) : new ArrayList();
    }
    
    private List<String> getCommands(CommandSender sender) {
        List<String> commands = new ArrayList();
        subCommands.values().stream().filter(command -> PluginControl.hasCommandPermission(sender, command.getCommandType().getCommandPermissionPath(), false)).forEach(command -> {
            commands.add(command.getName());
        });
        return commands;
    }
    
    private List<String> getNormallyTabComplete(CommandSender sender, String args) {
        List<String> commands = getCommands(sender);
        if (args != null) {
            List<String> names = new ArrayList();
            commands.stream().filter(command -> command.toLowerCase().startsWith(args.toLowerCase())).forEach(command -> {
                names.add(command);
            });
            return names;
        }
        return commands;
    }
    
    private void callSubCommand(CommandSender sender, String[] args) {
        String subCommand = args[0].toLowerCase();
        if (subCommands.get(subCommand) == null) {
            MessageUtil.sendMessage(sender, "Help-Menu");
            return;
        }
        subCommands.get(subCommand).execute(sender, subCommand, args);
    }
}
