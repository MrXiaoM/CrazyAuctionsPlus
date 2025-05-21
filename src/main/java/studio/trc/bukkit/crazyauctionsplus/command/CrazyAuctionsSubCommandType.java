package studio.trc.bukkit.crazyauctionsplus.command;

import studio.trc.bukkit.crazyauctionsplus.command.subcommand.*;

public enum CrazyAuctionsSubCommandType
{
    HELP("help", new HelpCommand(), "Help"),
    
    RELOAD("reload", new ReloadCommand(), "Reload"),
    
    ADMIN("admin", new AdminCommand(), "Admin"),
    
    GUI("gui", new GUICommand(), "Gui"),
    
    VIEW("view", new ViewCommand(), "View"),
    
    MAIL("mail", new MailCommand(), "Mail"),
    
    LISTED("listed", new ListedCommand(), "Listed"),
    
    SELL("sell", new SellCommand(), "Sell"),
    
    BUY("buy", new BuyCommand(), "Buy"),
    
    BID("bid", new BidCommand(), "Bid");

    private final String subCommandName;
    private final CrazyAuctionsSubCommand subCommand;
    private final String commandPermissionPath;

    CrazyAuctionsSubCommandType(String subCommandName, CrazyAuctionsSubCommand subCommand, String commandPermissionPath) {
        this.subCommandName = subCommandName;
        this.subCommand = subCommand;
        this.commandPermissionPath = commandPermissionPath;
    }

    public String getSubCommandName() {
        return subCommandName;
    }

    public CrazyAuctionsSubCommand getSubCommand() {
        return subCommand;
    }

    public String getCommandPermissionPath() {
        return commandPermissionPath;
    }

    /**
     * 获取命令类型
     * @param subCommand 参数
     */
    public static CrazyAuctionsSubCommandType getCommandType(String subCommand) {
        for (CrazyAuctionsSubCommandType type : values()) {
            if (type.getSubCommandName().equalsIgnoreCase(subCommand)) {
                return type;
            }
        }
        return null;
    }
}
