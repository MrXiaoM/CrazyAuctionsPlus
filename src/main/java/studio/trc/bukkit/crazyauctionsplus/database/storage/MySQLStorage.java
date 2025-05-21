package studio.trc.bukkit.crazyauctionsplus.database.storage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import studio.trc.bukkit.crazyauctionsplus.Main;
import studio.trc.bukkit.crazyauctionsplus.database.Storage;
import studio.trc.bukkit.crazyauctionsplus.database.engine.MySQLEngine;
import studio.trc.bukkit.crazyauctionsplus.util.ItemMail;
import studio.trc.bukkit.crazyauctionsplus.util.PluginControl;

public class MySQLStorage
    extends MySQLEngine
    implements Storage
{
    public static volatile Map<UUID, MySQLStorage> cache = new HashMap<>();
    
    private static long lastUpdateTime = System.currentTimeMillis();
    
    private final UUID uuid;
    private final YamlConfiguration yamlData = new YamlConfiguration();
    private final List<ItemMail> mailBox = new ArrayList<>();
    
    public MySQLStorage(UUID uuid) {
        this.uuid = uuid;
        
        try {
            ResultSet rs = super.executeQuery(super.getConnection().prepareStatement("SELECT * FROM " + getDatabaseName() + "." + getItemMailTable() + " WHERE UUID = '" + uuid + "'"));
            if (rs.next()) {
                yamlData.loadFromString(rs.getString("YamlData"));
            } else {
                register(uuid);
            }
        } catch (SQLException ex) {
            if (Main.language.get("MySQL-DataReadingError") != null) Main.getInstance().getServer().getConsoleSender().sendMessage(Main.language.getProperty("MySQL-DataReadingError").replace("{error}", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null").replace("{prefix}", PluginControl.getPrefix()).replace("&", "§"));
            try {
                if (super.getConnection().isClosed()) {
                    super.repairConnection();
                }
            } catch (SQLException ex1) {
                PluginControl.printStackTrace(ex1);
            }
            PluginControl.printStackTrace(ex);
        } catch (InvalidConfigurationException | NullPointerException ex) {
            if (Main.language.get("PlayerDataFailedToLoad") != null) {
                Player player = Bukkit.getPlayer(uuid);
                String localizedMessage = ex.getLocalizedMessage();
                String message = Main.language.getProperty("PlayerDataFailedToLoad")
                        .replace("{player}", player != null ? player.getName() : "null")
                        .replace("{error}", localizedMessage != null ? localizedMessage : "null")
                        .replace("{prefix}", PluginControl.getPrefix())
                        .replace("&", "§");
                Main.getInstance().getServer().getConsoleSender().sendMessage(message);
            }
            PluginControl.printStackTrace(ex);
        }
        
        loadData();
    }
    
    private void loadData() {
        if (yamlData.get("Name") == null || !yamlData.getString("Name", "").equals(Bukkit.getOfflinePlayer(uuid).getName())) {
            yamlData.set("Name", Bukkit.getOfflinePlayer(uuid).getName());
            saveData();
        }
        
        if (yamlData.get("Items") != null) {
            ConfigurationSection section = yamlData.getConfigurationSection("Items");
            if (section != null) for (String path : section.getKeys(false)) {
                if (yamlData.get("Items." + path) != null) {
                    ItemMail im;
                    try {
                        im = new ItemMail(
                            yamlData.get("Items." + path + ".UID") != null ? yamlData.getLong("Items." + path + ".UID") : Long.parseLong(path),
                            uuid,
                            yamlData.get("Items." + path + ".Item") != null ? yamlData.getItemStack("Items." + path + ".Item") : new ItemStack(Material.AIR),
                            yamlData.getLong("Items." + path + ".Full-Time"),
                            yamlData.get("Items." + path + ".Added-Time") != null ? yamlData.getLong("Items." + path + ".Added-Time") : -1,
                            yamlData.getBoolean("Items." + path + ".Never-Expire")
                        );
                    } catch (Exception ex) {
                        PluginControl.printStackTrace(ex);
                        continue;
                    }
                    mailBox.add(im);
                }
            }
        }
    }
    
    @Override
    public String getName() {
        return yamlData.getString("Name");
    }
    
    @Override
    public UUID getUUID() {
        return uuid;
    }
    
    @Override
    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    @Override
    public YamlConfiguration getYamlData() {
        return yamlData;
    }

    @Override
    public void saveData() {
        try {
            long i = 1;
            yamlData.set("Items", null);
            for (ItemMail im : mailBox) {
                if (im.getItem() == null || im.getItem().getType().equals(Material.AIR)) continue;
                yamlData.set("Items." + i + ".Item", im.getItem());
                yamlData.set("Items." + i + ".Full-Time", im.getFullTime());
                yamlData.set("Items." + i + ".Never-Expire", im.isNeverExpire());
                yamlData.set("Items." + i + ".UID", i);
                i++;
            }
            String yaml = yamlData.saveToString();
            PreparedStatement statement = getConnection().prepareStatement("UPDATE " + getDatabaseName() + "." + getItemMailTable() + " SET " +
                    "YamlData = ? " +
                    "WHERE UUID = '" + uuid + "'");
            statement.setString(1, yaml);
            executeUpdate(statement);
        } catch (SQLException ex) {
            PluginControl.printStackTrace(ex);
        }
    }

    @Override
    public List<ItemMail> getMailBox() {
        boolean save = false;
        for (int i = mailBox.size() - 1;i > -1;i--) {
            if (mailBox.get(i).getItem() == null || mailBox.get(i).getItem().getType().equals(Material.AIR)) {
                mailBox.remove(i);
                save = true;
            }
        }
        if (save) saveData();
        return mailBox;
    }
    
    @Override
    public ItemMail getMail(long uid) {
        for (ItemMail im : mailBox) {
            if (im.getUID() == uid) {
                return im;
            }
        }
        return null;
    }

    @Override
    public void addItem(ItemMail... is) {
        mailBox.addAll(Arrays.asList(is));
        saveData();
    }

    @Override
    public void removeItem(ItemMail... is) {
        mailBox.removeAll(Arrays.asList(is));
        saveData();
    }

    @Override
    public void clearMailBox() {
        mailBox.clear();
        yamlData.set("Items", null);
        saveData();
    }

    @Override
    public int getMailNumber() {
        return mailBox.size();
    }

    @Override
    public long makeUID() {
        long id = 0;
        while (true) {
            id++;
            boolean b = false;
            for (ItemMail im : mailBox) {
                if (im.getUID() == id) {
                    b = true;
                    break;
                }
            }
            if (b) continue;
            break;
        }
        return id;
    }
    
    private void register(UUID uuid) throws SQLException {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        String name = player != null ? player.getName() : null;
        if (name == null) {
            name = "Null";
        }
        PreparedStatement statement = getConnection().prepareStatement("INSERT INTO " + getDatabaseName() + "." + getItemMailTable()
                + "(UUID, Name, YamlData) "
                + "VALUES(?, ?, ?)");
        statement.setString(1, uuid.toString());
        statement.setString(2, name);
        statement.setString(3, "{}");
        executeUpdate(statement);
        yamlData.set("Name", name);
    }
    
    public static MySQLStorage getPlayerData(Player player) {
        if (isItemMailReacquisition() && getUpdateDelay() == 0) {
            return new MySQLStorage(player.getUniqueId());
        } else {
            MySQLStorage data = cache.get(player.getUniqueId());
            if (data != null && getUpdateDelay() != 0) {
                if (!isItemMailReacquisition() || System.currentTimeMillis() - lastUpdateTime <= getUpdateDelay() * 1000) {
                    return data;
                }
            }
            data = new MySQLStorage(player.getUniqueId());
            cache.put(player.getUniqueId(), data);
            lastUpdateTime = System.currentTimeMillis();
            return data;
        }
    }
    
    public static MySQLStorage getPlayerData(OfflinePlayer player) {
        if (isItemMailReacquisition() && getUpdateDelay() == 0) {
            return new MySQLStorage(player.getUniqueId());
        } else {
            MySQLStorage data = cache.get(player.getUniqueId());
            if (data != null && getUpdateDelay() != 0) {
                if (!isItemMailReacquisition() || System.currentTimeMillis() - lastUpdateTime <= getUpdateDelay() * 1000) {
                    return data;
                }
            }
            data = new MySQLStorage(player.getUniqueId());
            cache.put(player.getUniqueId(), data);
            lastUpdateTime = System.currentTimeMillis();
            return data;
        }
    }
    
    public static MySQLStorage getPlayerData(UUID uuid) {
        if (isItemMailReacquisition() && getUpdateDelay() == 0) {
            return new MySQLStorage(uuid);
        } else {
            MySQLStorage data = cache.get(uuid);
            if (data != null && getUpdateDelay() != 0) {
                if (!isItemMailReacquisition() || System.currentTimeMillis() - lastUpdateTime <= getUpdateDelay() * 1000) {
                    return data;
                }
            }
            data = new MySQLStorage(uuid);
            cache.put(uuid, data);
            lastUpdateTime = System.currentTimeMillis();
            return data;
        }
    }
}
