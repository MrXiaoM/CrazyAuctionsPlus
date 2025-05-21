package studio.trc.bukkit.crazyauctionsplus.currency;

import java.util.UUID;

import studio.trc.bukkit.crazyauctionsplus.util.FileManager.Files;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public enum CurrencyManager {
    
    VAULT("Vault", "Money");
    
    private final String pluginName, name;
    
    /**
     * @param pluginName
     *            name of the Plugin.
     * @param name
     *            name of the Currency.
     */
    CurrencyManager(String pluginName, String name) {
        this.pluginName = pluginName;
        this.name = name;
    }
    
    /**
     * @param name
     *            name of the Type you want.
     * @return Returns the Currency as an Enum.
     */
    public static CurrencyManager getFromName(String name) {
        for (CurrencyManager type : CurrencyManager.values()) {
            if (type.getPluginName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
    
    /**
     *
     * @param player
     *            Player you want the currency from.
     * @return Returns the amount they have of the currency
     */
    public static double getMoney(Player player) {
        return Vault.getMoney(player);
    }
    
    public static double getMoney(UUID uuid) {
        return Vault.getMoney(Bukkit.getOfflinePlayer(uuid));
    }
    
    /**
     *
     * @param player
     *            Player you want the currency from.
     * @param amount
     *            The amount you want to take.
     */
    public static void removeMoney(Player player, double amount) {
        Vault.removeMoney(player, amount);
    }
    
    /**
     *
     * @param player
     *            Player you want the currency from.
     * @param amount
     *            The amount you want to take.
     */
    public static void removeMoney(OfflinePlayer player, double amount) {
        Vault.removeMoney(player, amount);
    }
    
    /**
     *
     * @param player
     *            Player you want the currency from.
     * @param amount
     *            The amount you want to add.
     */
    public static void addMoney(Player player, double amount) {
        Vault.addMoney(player, amount);
    }
    
    /**
     *
     * @param player
     *            Player you want the currency from.
     * @param amount
     *            The amount you want to add.
     */
    public static void addMoney(OfflinePlayer player, double amount) {
        Vault.addMoney(player, amount);
    }
    
    /**
     * @return Returns the Currency name as a string.
     */
    public String getName() {
        return name;
    }
    
    /**
     * @return Returns the Currency name as a string.
     */
    public String getPluginName() {
        return pluginName;
    }
    
    /**
     *
     * @return Returns true if the server has the plugin.
     */
    public Boolean hasPlugin() {
        if (Bukkit.getServer().getPluginManager().getPlugin(pluginName) != null) {
            return Files.CONFIG.getFile().getBoolean("Settings.Currencies." + pluginName + ".Enabled");
        }
        return false;
    }
}