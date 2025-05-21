package studio.trc.bukkit.crazyauctionsplus.util;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class ItemOwner
{
    private final UUID uuid;
    private String name;
    private Player player;
    private OfflinePlayer offlineplayer;
    
    public ItemOwner(UUID uuid) {
        this.uuid = uuid;
    }
    
    public ItemOwner(UUID uuid, String playerName) {
        this.uuid = uuid;
        name = playerName;
    }
    
    public ItemOwner(UUID uuid, Player player) {
        this.uuid = uuid;
        this.player = player;
    }
    
    public ItemOwner(UUID uuid, OfflinePlayer offlineplayer) {
        this.uuid = uuid;
        this.offlineplayer = offlineplayer;
    }
    
    public UUID getUUID() {
        return uuid;
    }
    
    public String getName() {
        return name != null ? name : Bukkit.getOfflinePlayer(uuid).getName();
    }
    
    public Player getPlayer() {
        return player != null ? player : Bukkit.getPlayer(uuid);
    }
    
    public OfflinePlayer getOfflinePlayer() {
        return offlineplayer != null ? offlineplayer : Bukkit.getOfflinePlayer(uuid);
    }
    
    @Override
    public String toString() {
        return name != null ? name + ":" + uuid : 
                player != null ? player.getName() + ":" + uuid :
                offlineplayer != null ? offlineplayer.getName() + ":" + uuid : Bukkit.getOfflinePlayer(uuid).getName() + ":" + uuid;
    }
}
