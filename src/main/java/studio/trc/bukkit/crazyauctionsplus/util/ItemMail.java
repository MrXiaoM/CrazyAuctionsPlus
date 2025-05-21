package studio.trc.bukkit.crazyauctionsplus.util;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import studio.trc.bukkit.crazyauctionsplus.util.FileManager.Files;

/**
 * Used to process player item mail.
 */
public class ItemMail
{
    private final UUID uuid;
    private final ItemStack is;
    private final long addedTime;
    private final long fullTime;
    private final boolean neverExpire;
    private final long uid;
    
    public ItemMail(long uid, UUID uuid, ItemStack is, long fullTime, long addedTime, boolean neverExpire) {
        this.uid = uid;
        this.uuid = uuid;
        this.is = is;
        this.fullTime = fullTime;
        this.addedTime = addedTime;
        this.neverExpire = neverExpire;
    }
    
    public ItemMail(long uid, Player owner, ItemStack is, long fullTime, long addedTime, boolean neverExpire) {
        this.uid = uid;
        this.uuid = owner.getUniqueId();
        this.is = is;
        this.fullTime = fullTime;
        this.addedTime = addedTime;
        this.neverExpire = neverExpire;
    }
    
    public ItemMail(long uid, OfflinePlayer owner, ItemStack is, long fullTime, long addedTime, boolean neverExpire) {
        this.uid = uid;
        this.uuid = owner.getUniqueId();
        this.is = is;
        this.fullTime = fullTime;
        this.addedTime = addedTime;
        this.neverExpire = neverExpire;
    }
    
    /**
     * Get ItemStack instance.
     */
    public ItemStack getItem() {
        return is;
    }
    
    /**
     * Get full time.
     */
    public long getFullTime() {
        return fullTime;
    }
    
    /**
     * Whether the mail has expired.
     * This method is usually called by automatic update detection.
     */
    public boolean expired() {
        return System.currentTimeMillis() >= fullTime;
    }
    
    /**
     * Get creation time.
     */
    public long getAddedTime() {
        if (addedTime == -1) {
            return fullTime - (PluginControl.convertToMill(Files.CONFIG.getFile().getString("Settings.Full-Expire-Time")) - System.currentTimeMillis());
        } else {
            return addedTime;
        }
    }
    
    /**
     * Does the item mail expire?
     */
    public boolean isNeverExpire() {
        return neverExpire;
    }
    
    /**
     * Get Player's instance
     */
    public Player getOwner() {
        return Bukkit.getPlayer(uuid);
    }
    
    /**
     * Get UUID.
     */
    public UUID getOwnerUUID() {
        return uuid;
    }
    
    /**
     * Get Item Mail's UID
     */
    public long getUID() {
        return uid;
    }
    
    /**
     * Give Item and delete this item mail.
     */
    public void giveItem() {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(is.clone());
            } else {
                player.getWorld().dropItem(player.getLocation(), is.clone());
            }
            is.setType(Material.AIR);
        }
    }
    
    @Override
    public String toString() {
        return "[ItemMail] -> [OwnerUUID=" + uuid.toString() + ",ItemStack=" + is.toString() + ",FullTime=" + fullTime + ",NeverExpire=" + neverExpire + ",UID=" + uid + "]";
    }
}
