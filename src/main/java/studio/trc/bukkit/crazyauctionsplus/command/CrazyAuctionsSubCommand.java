package studio.trc.bukkit.crazyauctionsplus.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface CrazyAuctionsSubCommand
{
    void execute(CommandSender sender, String subCommand, String... args);
    
    String getName();
    
    List<String> tabComplete(CommandSender sender, String subCommand, String... args);
    
    CrazyAuctionsSubCommandType getCommandType();
    
    default List<String> getTabPlayersName(String[] args, int length) {
        if (args.length == length) {
            List<String> onlinePlayers = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            List<String> names = new ArrayList<>();
            onlinePlayers.stream().filter(command -> command.toLowerCase().startsWith(args[length - 1].toLowerCase())).forEach(names::add);
            return names;
        }
        return new ArrayList<>();
    }
    
    default List<String> getTabElements(String[] args, int length, Collection<String> elements) {
        if (args.length == length) {
            List<String> names = new ArrayList<>();
            elements.stream().filter(command -> command.toLowerCase().startsWith(args[length - 1].toLowerCase())).forEach(names::add);
            return names;
        }
        return new ArrayList<>();
    }
}
