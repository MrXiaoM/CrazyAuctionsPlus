package studio.trc.bukkit.crazyauctionsplus.api.events;

import org.jetbrains.annotations.NotNull;
import studio.trc.bukkit.crazyauctionsplus.util.enums.ShopType;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author BadBones69
 *
 * This event is fired when a new item is listed onto the auction house.
 *
 */
public class AuctionListEvent extends Event {
    
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final double money;
    private final double tax;
    private final ShopType shop;
    private final ItemStack item;
    
    public AuctionListEvent(Player player, ShopType shop, ItemStack item, double money, double tax) {
        this.player = player;
        this.shop = shop;
        this.item = item;
        this.money = money;
        this.tax = tax;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public ShopType getShopType() {
        return shop;
    }
    
    public ItemStack getItem() {
        return item;
    }
    
    public double getMoney() {
        return money;
    }
    
    public double getTax() {
        return tax;
    }
}