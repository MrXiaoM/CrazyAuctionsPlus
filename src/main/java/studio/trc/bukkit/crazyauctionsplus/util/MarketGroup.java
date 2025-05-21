package studio.trc.bukkit.crazyauctionsplus.util;

import studio.trc.bukkit.crazyauctionsplus.util.FileManager.Files;
import studio.trc.bukkit.crazyauctionsplus.util.FileManager.ProtectedConfiguration;

public class MarketGroup
{
    private final String groupName;
    
    private static final ProtectedConfiguration config = Files.CONFIG.getFile();
    
    public MarketGroup(String groupName) {
        this.groupName = groupName;
    }
    
    public int getSellLimit() {
        return config.getInt("Settings.Permissions.Market.Permission-Groups." + groupName + ".Sell-Limit");
    }
    
    public int getBuyLimit() {
        return config.getInt("Settings.Permissions.Market.Permission-Groups." + groupName + ".Buy-Limit");
    }
    
    public int getBidLimit() {
        return config.getInt("Settings.Permissions.Market.Permission-Groups." + groupName + ".Bid-Limit");
    }
    
    public double getSellTaxRate() {
        return config.getDouble("Settings.Permissions.Market.Permission-Groups." + groupName + ".Sell-Tax-Rate");
    }
    
    public double getBuyTaxRate() {
        return config.getDouble("Settings.Permissions.Market.Permission-Groups." + groupName + ".Buy-Tax-Rate");
    }
    
    public double getBidTaxRate() {
        return config.getInt("Settings.Permissions.Market.Permission-Groups." + groupName + ".Bid-Tax-Rate");
    }
    
    public String getGroupName() {
        return groupName;
    }
    
    public boolean exist() {
        return config.get("Settings.Permissions.Market.Permission-Groups." + groupName) != null;
    }
}
