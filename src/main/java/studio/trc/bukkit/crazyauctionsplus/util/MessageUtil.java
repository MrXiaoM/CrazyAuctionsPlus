package studio.trc.bukkit.crazyauctionsplus.util;

import studio.trc.bukkit.crazyauctionsplus.util.FileManager.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import org.bukkit.command.CommandSender;

public enum MessageUtil {
    
    @Deprecated PLAYERS_ONLY("Players-Only", "&cOnly players can use this command."),
    @Deprecated RELOAD("Reload", "&cYou have just reloaded the Crazy Auctions Files."),
    @Deprecated SYNCHRONIZE("Synchronize", "&aAll stored data has been synchronized to the latest version."),
    @Deprecated NEED_MORE_MONEY("Need-More-Money", "&cYou are in need of &a$%money_needed%&c."),
    @Deprecated INVENTORY_FULL("Inventory-Full", "&cYour inventory is too full. Please open up some space to buy that."),
    @Deprecated NO_PERMISSION("No-Permission", "&cYou do not have permission to use that command!"),
    @Deprecated NOT_ONLINE("Not-Online", "&cThat player is not online at this time."),
    @Deprecated DOSENT_HAVE_ITEM_IN_HAND("Doesnt-Have-Item-In-Hand", "&cYou must have an item in your hand."),
    @Deprecated NOT_A_NUMBER("Not-A-Valid-Number", "&c%arg% is not a number."),
    @Deprecated GOT_ITEM_BACK("Got-Item-Back", "&7Your item has been returned."),
    @Deprecated CANCELLED_ITEM("Cancelled-Item-On-Sale", "&7You have cancelled an item on the auction list, return your items with /Ca expired."),
    @Deprecated ITEM_HAS_EXPIRED("Item-Has-Expired", "&7An item you have in the Crazy Auctions has just expired."),
    @Deprecated ADMIN_FORCE_CENCELLED("Admin-Force-Cancelled", "&7You have just force cancelled a sale."),
    @Deprecated ADMIN_FORCE_CANCELLED_TO_PLAYER("Admin-Force-Cancelled-To-Player", "&cOne of your items was just force cancelled by an Admin."),
    @Deprecated ITEM_DOESNT_EXIST("Item-Doesnt-Exist", "&cThat item isnt in the crazy auctions any more."),
    @Deprecated MAX_ITEMS("Max-Selling-Items", "&cYou cant list any more items to the Crazy Auctions."),
    @Deprecated ITEM_BLACKLISTED("Item-BlackListed", "&cThat item is not allowed to be sold here."),
    @Deprecated ITEM_DAMAGED("Item-Damaged", "&cThat item is damaged and is not allowed to be sold here."),
    @Deprecated SOLD_MESSAGE("Sold-Msg", "&7Thank you for buying this item."),
    @Deprecated BID_MORE_MONEY("Bid-More-Money", "&cYour bid is to low, please bid more."),
    @Deprecated NOT_A_CURRENCY("Not-A-Currency", "&cThat is not a currency. Please use Money or Tokens ."),
    @Deprecated SELL_PRICE_TO_LOW("Sell-Price-To-Low", "&cYour sell price is to low the minimum is &a$10&c."),
    @Deprecated SELL_PRICE_TO_HIGH("Sell-Price-To-High", "&cYour sell price is to high the maximum is &a$1000000&c."),
    @Deprecated BID_PRICE_TO_LOW("Bid-Price-To-Low", "&cYour starting bid price is to low the minimum is &a$100&c."),
    @Deprecated BID_PRICE_TO_HIGH("Bid-Price-To-High", "&cYour starting bid price is to high the maximum is &a$1000000&c."),
    @Deprecated BOUGHT_ITEM("Bought-Item", "&7You have just bought a item for &a$%price%&7."),
    @Deprecated WIN_BIDDING("Win-Bidding", "&7You have just won a bid for &a$%price%&7. Do /Ca Collect to collect your winnings."),
    @Deprecated PLAYER_BOUGHT_ITEM("Player-Bought-Item", "&7%player% has bought your item for &a$%price%."),
    @Deprecated SOMEONE_WON_PLAYERS_BID("Someone-Won-Players-Bid", "&7%player% has won your item you from a bid for &a$%price%."),
    @Deprecated ADDED_ITEM_TO_AUCTION("Added-Item-To-Auction", "&7You have just added a item to the crazy auctions for &a$%price%&7."),
    @Deprecated BID_MESSAGE("Bid-Msg", "&7You have just bid &a$%Bid% &7on that item."),
    @Deprecated SELLING_DISABLED("Selling-Disabled", "&cThe selling option is disabled."),
    @Deprecated BIDDING_DISABLED("Bidding-Disabled", "&cThe bidding option is disabled."),
    @Deprecated CRAZYAUCTIONS_HELP("CrazyAuctions-Help", "&c/Ca help"),
    @Deprecated CRAZYAUCTIONS_VIEW("CrazyAuctions-View", "&c/Ca view <player>"),
    @Deprecated CRAZYAUCTIONS_SELL_BID("CrazyAuctions-Sell-Bid", "&c/Ca sell/bid <price> [amount of items]");
    
    @Deprecated
    private final String path;
    @Deprecated
    private String defaultMessage;
    @Deprecated
    private List<String> defaultListMessage;
    
    @Deprecated
    MessageUtil(String path, String defaultMessage) {
        this.path = path;
        this.defaultMessage = defaultMessage;
    }
    
    @Deprecated
    MessageUtil(String path, List<String> defaultListMessage) {
        this.path = path;
        this.defaultListMessage = defaultListMessage;
    }
    
    @Deprecated
    public static String convertList(List<String> list) {
        StringBuilder message = new StringBuilder();
        for (String m : list) {
            message.append(PluginControl.color(m)).append("\n");
        }
        return message.toString();
    }
    
    @Deprecated
    public static String convertList(List<String> list, HashMap<String, String> placeholders) {
        StringBuilder message = new StringBuilder();
        for (String m : list) {
            message.append(PluginControl.color(m)).append("\n");
        }
        for (String ph : placeholders.keySet()) {
            message = new StringBuilder(PluginControl.color(message.toString().replaceAll(ph, placeholders.get(ph))).replaceAll(ph, placeholders.get(ph).toLowerCase()));
        }
        return message.toString();
    }
    
    @Deprecated
    public static void addMissingMessages() {
        ProtectedConfiguration messages = Files.MESSAGES.getFile();
        boolean saveFile = false;
        for (MessageUtil message : values()) {
            if (!messages.contains("Messages." + message.getPath())) {
                saveFile = true;
                if (message.getDefaultMessage() != null) {
                    messages.set("Messages." + message.getPath(), message.getDefaultMessage());
                } else {
                    messages.set("Messages." + message.getPath(), message.getDefaultListMessage());
                }
            }
        }
        if (saveFile) {
            Files.MESSAGES.saveFile();
        }
    }
    
    @Deprecated
    public String getMessage() {
        if (isList()) {
            if (exists()) {
                return PluginControl.color(convertList(Files.MESSAGES.getFile().getStringList("Messages." + path)));
            } else {
                return PluginControl.color(convertList(getDefaultListMessage()));
            }
        } else {
            if (exists()) {
                return PluginControl.getPrefix(Files.MESSAGES.getFile().getString("Messages." + path));
            } else {
                return PluginControl.getPrefix(getDefaultMessage());
            }
        }
    }
    
    /**
     * Send message to command sender.
     * @param sender Command sender.
     * @param path MessageUtil.yml's path
     */
    public static void sendMessage(CommandSender sender, String path) {
        if (sender == null) return;
        List<String> messages = Files.MESSAGES.getFile().getStringList(Files.CONFIG.getFile().getString("Settings.Language") + "." + path);
        if (messages.isEmpty()) {
            sender.sendMessage(PluginControl.color(Files.MESSAGES.getFile().getString(Files.CONFIG.getFile().getString("Settings.Language") + "." + path).replace("{prefix}", PluginControl.getPrefix()).replace("/n", "\n")));
        } else {
            for (String message : messages) {
                sender.sendMessage(PluginControl.color(message.replace("{prefix}", PluginControl.getPrefix()).replace("/n", "\n")));
            }
        }
    }
    
    /**
     * Send message to command sender.
     * @param sender Command sender.
     * @param path MessageUtil.yml's path
     * @param placeholders If the text contains a placeholder,
     *                      The placeholder will be replaced with the specified text.
     */
    public static void sendMessage(CommandSender sender, String path, Map<String, String> placeholders) {
        if (sender == null) return;
        List<String> messages = Files.MESSAGES.getFile().getStringList(Files.CONFIG.getFile().getString("Settings.Language") + "." + path);
        if (messages.isEmpty()) {
            String message = PluginControl.color(Files.MESSAGES.getFile().getString(Files.CONFIG.getFile().getString("Settings.Language") + "." + path));
            for (String ph : placeholders.keySet()) {
                message = PluginControl.color(message.replaceAll(ph, placeholders.get(ph))).replaceAll(ph, placeholders.get(ph).toLowerCase());
            }
            sender.sendMessage(PluginControl.color(message.replace("{prefix}", PluginControl.getPrefix())).replace("/n", "\n"));
        } else {
            for (String message : messages) {
                for (String ph : placeholders.keySet()) {
                    message = PluginControl.color(message.replace(ph, placeholders.get(ph)).replace("{prefix}", PluginControl.getPrefix())).replace("/n", "\n");
                }
                sender.sendMessage(message);
            }
        }
    }
    
    /**
     * 
     * @param sender Command sender.
     * @param path MessageUtil.yml's path
     * @param placeholders If the text contains a placeholder,
     *                      The placeholder will be replaced with the specified text.
     * @param visible If the text contains a placeholder,
     *                 whether the entire line is visible or not will be
     *                 determined by the Boolean value corresponding to the placeholder.
     */
    public static void sendMessage(CommandSender sender, String path, Map<String, String> placeholders, Map<String, Boolean> visible) {
        if (sender == null) return;
        List<String> messages = Files.MESSAGES.getFile().getStringList(Files.CONFIG.getFile().getString("Settings.Language") + "." + path);
        if (messages.isEmpty()) {
            String message = PluginControl.color(Files.MESSAGES.getFile().getString(Files.CONFIG.getFile().getString("Settings.Language") + "." + path));
            for (String v : visible.keySet()) {
                if (message.contains(v)) {
                    if (!visible.get(v)) {
                        return;
                    }
                }
            }
            for (String ph : placeholders.keySet()) {
                message = PluginControl.color(message.replaceAll(ph, placeholders.get(ph))).replaceAll(ph, placeholders.get(ph).toLowerCase());
            }
            sender.sendMessage(PluginControl.color(message.replace("{prefix}", PluginControl.getPrefix())).replace("/n", "\n"));
        } else {
            for (String message : messages) {
                boolean isVisible = true;
                for (String v : visible.keySet()) {
                    if (message.contains(v)) {
                        if (!visible.get(v)) {
                            isVisible = false;
                            break;
                        } else {
                            message = message.replace(v, "");
                        }
                    }
                }
                if (!isVisible) {
                    continue;
                }
                for (String ph : placeholders.keySet()) {
                    message = PluginControl.color(message.replace(ph, placeholders.get(ph)).replace("{prefix}", PluginControl.getPrefix())).replace("/n", "\n");
                }
                sender.sendMessage(message);
            }
        }
    }
    
    public static String getValue(String path) {
        return PluginControl.color(Files.MESSAGES.getFile().getString(Files.CONFIG.getFile().getString("Settings.Language") + "." + path).replace("{prefix}", PluginControl.getPrefix()).replace("/n", "\n"));
    }
    
    public static List<String> getValueList(String path) {
        List<String> list = new ArrayList<>();
        ProtectedConfiguration config = Files.CONFIG.getFile();
        for (String message : Files.MESSAGES.getFile().getStringList(config.getString("Settings.Language") + "." + path)) {
            list.add(PluginControl.color(message.replace("{prefix}", config.getString("Settings.Prefix"))));
        }
        return list;
    }
    
    @Deprecated
    public String getMessage(HashMap<String, String> placeholders) {
        String message;
        if (isList()) {
            if (exists()) {
                message = PluginControl.color(convertList(Files.MESSAGES.getFile().getStringList("Messages." + path), placeholders));
            } else {
                message = PluginControl.color(convertList(getDefaultListMessage(), placeholders));
            }
        } else {
            if (exists()) {
                message = PluginControl.getPrefix(Files.MESSAGES.getFile().getString("Messages." + path));
            } else {
                message = PluginControl.getPrefix(getDefaultMessage());
            }
            for (String ph : placeholders.keySet()) {
                if (message.contains(ph)) {
                    message = message.replaceAll(ph, placeholders.get(ph)).replaceAll(ph, placeholders.get(ph).toLowerCase());
                }
            }
        }
        return message;
    }
    
    @Deprecated
    public String getMessageNoPrefix() {
        if (isList()) {
            if (exists()) {
                return PluginControl.color(convertList(Files.MESSAGES.getFile().getStringList("Messages." + path)));
            } else {
                return PluginControl.color(convertList(getDefaultListMessage()));
            }
        } else {
            if (exists()) {
                return PluginControl.color(Files.MESSAGES.getFile().getString("Messages." + path));
            } else {
                return PluginControl.color(getDefaultMessage());
            }
        }
    }
    
    @Deprecated
    public String getMessageNoPrefix(HashMap<String, String> placeholders) {
        String message;
        if (isList()) {
            if (exists()) {
                message = PluginControl.color(convertList(Files.MESSAGES.getFile().getStringList("Messages." + path), placeholders));
            } else {
                message = PluginControl.color(convertList(getDefaultListMessage(), placeholders));
            }
        } else {
            if (exists()) {
                message = PluginControl.color(Files.MESSAGES.getFile().getString("Messages." + path));
            } else {
                message = PluginControl.color(getDefaultMessage());
            }
            for (String ph : placeholders.keySet()) {
                if (message.contains(ph)) {
                    message = message.replaceAll(ph, placeholders.get(ph)).replaceAll(ph, placeholders.get(ph).toLowerCase());
                }
            }
        }
        return message;
    }
    
    @Deprecated
    private Boolean exists() {
        return Files.MESSAGES.getFile().contains("Messages." + path);
    }
    
    @Deprecated
    private Boolean isList() {
        if (Files.MESSAGES.getFile().contains("Messages." + path)) {
            return !Files.MESSAGES.getFile().getStringList("Messages." + path).isEmpty();
        } else {
            return defaultMessage == null;
        }
    }
    
    @Deprecated
    private String getPath() {
        return path;
    }
    
    @Deprecated
    private String getDefaultMessage() {
        return defaultMessage;
    }
    
    @Deprecated
    private List<String> getDefaultListMessage() {
        return defaultListMessage;
    }
}