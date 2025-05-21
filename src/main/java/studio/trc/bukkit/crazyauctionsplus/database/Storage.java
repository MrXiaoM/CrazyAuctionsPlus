package studio.trc.bukkit.crazyauctionsplus.database;

import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import studio.trc.bukkit.crazyauctionsplus.database.storage.MySQLStorage;
import studio.trc.bukkit.crazyauctionsplus.database.storage.SQLiteStorage;
import studio.trc.bukkit.crazyauctionsplus.database.storage.YamlStorage;
import studio.trc.bukkit.crazyauctionsplus.util.PluginControl;

public interface Storage
    extends ItemMailBox
{
    /**
     * Get player name from configuration file.
     */
    String getName();
    
    /**
     * Get player's uuid.
     */
    UUID getUUID();
    
    /**
     * Get Yaml Configuration Data.
     */
    YamlConfiguration getYamlData();
    
    /**
     * Get Player's instance.
     */
    Player getPlayer();
    
    /**
     * Save cached data to configuration file.
     */
    void saveData();
    
    static Storage getPlayer(Player player) {
        if (PluginControl.useSplitDatabase()) {
            switch (PluginControl.getItemMailStorageMethod()) {
                case MySQL: {
                    if (PluginControl.useMySQLStorage()) {
                        return MySQLStorage.getPlayerData(player.getUniqueId());
                    } else {
                        return YamlStorage.getPlayerData(player);
                    }
                }
                case SQLite: {
                    if (PluginControl.useSQLiteStorage()) {
                        return SQLiteStorage.getPlayerData(player.getUniqueId());
                    } else {
                        return YamlStorage.getPlayerData(player);
                    }
                }
                case YAML: {
                    return YamlStorage.getPlayerData(player);
                }
            }
        } else if (PluginControl.useMySQLStorage()) {
            return MySQLStorage.getPlayerData(player);
        } else if (PluginControl.useSQLiteStorage()) {
            return SQLiteStorage.getPlayerData(player);
        }
        return YamlStorage.getPlayerData(player);
    }
    
    static Storage getPlayer(OfflinePlayer player) {
        if (PluginControl.useSplitDatabase()) {
            switch (PluginControl.getItemMailStorageMethod()) {
                case MySQL: {
                    if (PluginControl.useMySQLStorage()) {
                         return MySQLStorage.getPlayerData(player.getUniqueId());
                    } else {
                        return YamlStorage.getPlayerData(player);
                    }
                }
                case SQLite: {
                    if (PluginControl.useSQLiteStorage()) {
                        return SQLiteStorage.getPlayerData(player.getUniqueId());
                    } else {
                        return YamlStorage.getPlayerData(player);
                    }
                }
                case YAML: {
                    return YamlStorage.getPlayerData(player);
                }
            }
        } else if (PluginControl.useMySQLStorage()) {
            return MySQLStorage.getPlayerData(player);
        } else if (PluginControl.useSQLiteStorage()) {
            return SQLiteStorage.getPlayerData(player);
        }
        return YamlStorage.getPlayerData(player);
    }
    
    static Storage getPlayer(UUID uuid) {
        if (PluginControl.useSplitDatabase()) {
            switch (PluginControl.getItemMailStorageMethod()) {
                case MySQL: {
                    if (PluginControl.useMySQLStorage()) {
                         return MySQLStorage.getPlayerData(uuid);
                    } else {
                        return YamlStorage.getPlayerData(uuid);
                    }
                }
                case SQLite: {
                    if (PluginControl.useSQLiteStorage()) {
                        return SQLiteStorage.getPlayerData(uuid);
                    } else {
                        return YamlStorage.getPlayerData(uuid);
                    }
                }
                case YAML: {
                    return YamlStorage.getPlayerData(uuid);
                }
            }
        } else if (PluginControl.useMySQLStorage()) {
            return MySQLStorage.getPlayerData(uuid);
        } else if (PluginControl.useSQLiteStorage()) {
            return SQLiteStorage.getPlayerData(uuid);
        }
        return YamlStorage.getPlayerData(uuid);
    }
}
