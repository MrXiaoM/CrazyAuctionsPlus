package studio.trc.bukkit.crazyauctionsplus.currency;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import studio.trc.bukkit.crazyauctionsplus.util.PluginControl;

public class Vault {
    
    public static Economy econ = null;
    
    public static boolean hasVault() {
        return Bukkit.getServer().getPluginManager().getPlugin("Vault") != null;
    }
    
    public static boolean setupEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
    
    public static double getMoney(Player player) {
        if (player != null) {
            try {
                return econ.getBalance(player);
            } catch (NullPointerException ex) {
                PluginControl.printStackTrace(ex);
            }
        }
        return 0L;
    }
    
    public static double getMoney(OfflinePlayer player) {
        if (player != null) {
            try {
                return econ.getBalance(player);
            } catch (NullPointerException ex) {
                PluginControl.printStackTrace(ex);
            }
        }
        return 0L;
    }
    
    public static void removeMoney(Player player, double amount) {
        econ.withdrawPlayer(player, amount);
    }
    
    public static void removeMoney(OfflinePlayer player, double amount) {
        econ.withdrawPlayer(player, amount);
    }
    
    public static void addMoney(Player player, double amount) {
        econ.depositPlayer(player, amount);
    }
    
    public static void addMoney(OfflinePlayer player, double amount) {
        econ.depositPlayer(player, amount);
    }
}