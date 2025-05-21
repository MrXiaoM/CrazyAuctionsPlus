package studio.trc.bukkit.crazyauctionsplus.database.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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

import studio.trc.bukkit.crazyauctionsplus.util.ItemMail;
import studio.trc.bukkit.crazyauctionsplus.database.Storage;
import studio.trc.bukkit.crazyauctionsplus.util.PluginControl;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class YamlStorage
    implements Storage
{
    public static volatile Map<UUID, YamlStorage> cache = new HashMap<>();
    
    private final UUID uuid;
    private final YamlConfiguration config = new YamlConfiguration();
    private final List<ItemMail> mailBox = new ArrayList<>();
    
    public YamlStorage(Player player) {
        uuid = player.getUniqueId();
        
        File dataFolder = new File("plugins/CrazyAuctionsPlus/Players");
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }
        
        File dataFile = new File("plugins/CrazyAuctionsPlus/Players/" + player.getUniqueId() + ".yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException ex) {
                PluginControl.printStackTrace(ex);
            }
        }
        try (InputStreamReader Config = new InputStreamReader(Files.newInputStream(dataFile.toPath()), StandardCharsets.UTF_8)) {
            config.load(Config);
        } catch (IOException | InvalidConfigurationException ex) {
            dataFileRepair();
            PluginControl.printStackTrace(ex);
        }
        
        loadData();
    }
    
    public YamlStorage(UUID uuid) {
        this.uuid = uuid;
        
        File dataFolder = new File("plugins/CrazyAuctionsPlus/Players");
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }
        
        File dataFile = new File("plugins/CrazyAuctionsPlus/Players/" + uuid.toString() + ".yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException ex) {
                PluginControl.printStackTrace(ex);
            }
        }
        try (InputStreamReader Config = new InputStreamReader(Files.newInputStream(dataFile.toPath()), StandardCharsets.UTF_8)) {
            config.load(Config);
        } catch (IOException | InvalidConfigurationException ex) {
            dataFileRepair();
            PluginControl.printStackTrace(ex);
        }
        
        loadData();
    }
    
    private void loadData() {
        String name = config.getString("Name");
        if (name == null || !name.equals(Bukkit.getOfflinePlayer(uuid).getName())) {
            config.set("Name", Bukkit.getOfflinePlayer(uuid).getName());
            saveData();
        }

        ConfigurationSection section = config.getConfigurationSection("Items");
        if (section != null) for (String path : section.getKeys(false)) {
            if (config.get("Items." + path) != null) {
                ItemMail im;
                try {
                    im = new ItemMail(
                            config.get("Items." + path + ".UID") != null ? config.getLong("Items." + path + ".UID") : Long.parseLong(path),
                            uuid,
                            config.get("Items." + path + ".Item") != null ? config.getItemStack("Items." + path + ".Item") : new ItemStack(Material.AIR),
                            config.getLong("Items." + path + ".Full-Time"),
                            config.get("Items." + path + ".Added-Time") != null ? config.getLong("Items." + path + ".Added-Time") : -1,
                            config.getBoolean("Items." + path + ".Never-Expire")
                    );
                } catch (Exception ex) {
                    PluginControl.printStackTrace(ex);
                    continue;
                }
                mailBox.add(im);
            }
        }
    }
    
    private void dataFileRepair() {

        File dataFolder = new File("plugins/CrazyAuctionsPlus/Broken-Players");
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }
        File dataFile = new File("plugins/CrazyAuctionsPlus/Players/" + uuid.toString() + ".yml");
        dataFile.renameTo(new File("plugins/CrazyAuctionsPlus/Broken-Players/" + uuid + ".yml"));
        try {
            dataFile.createNewFile();
        } catch (IOException ex) {
            PluginControl.printStackTrace(ex);
        }
        try (InputStreamReader Config = new InputStreamReader(Files.newInputStream(dataFile.toPath()), StandardCharsets.UTF_8)) {
            config.load(Config);
        } catch (IOException | InvalidConfigurationException ex) {
            PluginControl.printStackTrace(ex);
        }
    }
    
    @Override
    public String getName() {
        return config.getString("Name");
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
    public int getMailNumber() {
        return mailBox.size();
    }
    
    @Override
    public YamlConfiguration getYamlData() {
        return config;
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
        config.set("Items", null);
        saveData();
    }
    
    @Override
    public long makeUID() {
        long id = 0;
        while (true) { //循环查找
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
    
    @Override
    public void saveData() {
        long i = 1;
        config.set("Items", null);
        for (ItemMail im : mailBox) {
            if (im.getItem() == null || im.getItem().getType().equals(Material.AIR)) continue;
            config.set("Items." + i + ".Item", im.getItem());
            config.set("Items." + i + ".Full-Time", im.getFullTime());
            config.set("Items." + i + ".Never-Expire", im.isNeverExpire());
            config.set("Items." + i + ".UID", i);
            i++;
        }
        try {
            config.save("plugins/CrazyAuctionsPlus/Players/" + uuid + ".yml");
        } catch (IOException ex) {
            PluginControl.printStackTrace(ex);
        }
    }
    
    public static YamlStorage getPlayerData(Player player) {
        YamlStorage data = cache.get(player.getUniqueId());
        if (data != null) {
            return data;
        }
        data = new YamlStorage(player);
        cache.put(player.getUniqueId(), data);
        return data;
    }
    
    public static YamlStorage getPlayerData(OfflinePlayer player) {
        YamlStorage data = cache.get(player.getUniqueId());
        if (data != null) {
            return data;
        }
        data = new YamlStorage(player.getUniqueId());
        cache.put(player.getUniqueId(), data);
        return data;
    }
    
    public static YamlStorage getPlayerData(UUID uuid) {
        YamlStorage data = cache.get(uuid);
        if (data != null) {
            return data;
        }
        data = new YamlStorage(uuid);
        cache.put(uuid, data);
        return data;
    }
}
