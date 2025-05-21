package studio.trc.bukkit.crazyauctionsplus.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public interface CrazyAuctionsSubCommand
{
    public void execute(CommandSender sender, String subCommand, String... args);
    
    public String getName();
    
    public List<String> tabComplete(CommandSender sender, String subCommand, String... args);
    
    public CrazyAuctionsSubCommandType getCommandType();
    
    default List<String> getTabPlayersName(String[] args, int length) {
        if (args.length == length) {
            List<String> onlines = Bukkit.getOnlinePlayers().stream().map(player -> player.getName()).collect(Collectors.toList());
            List<String> names = new ArrayList();
            onlines.stream().filter(command -> command.toLowerCase().startsWith(args[length - 1].toLowerCase())).forEach(command -> {
                names.add(command);
            });
            return names;
        }
        return new ArrayList();
    }
    
    default List<String> getTabElements(String[] args, int length, Collection<String> elements) {
        if (args.length == length) {
            List<String> names = new ArrayList();
            elements.stream().filter(command -> command.toLowerCase().startsWith(args[length - 1].toLowerCase())).forEach(command -> {
                names.add(command);
            });
            return names;
        }
        return new ArrayList();
    }
}
