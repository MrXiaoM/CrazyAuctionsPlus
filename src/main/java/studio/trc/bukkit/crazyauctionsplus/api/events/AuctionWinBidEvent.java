package studio.trc.bukkit.crazyauctionsplus.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import org.jetbrains.annotations.NotNull;
import studio.trc.bukkit.crazyauctionsplus.util.MarketGoods;

/**
 *
 * @author BadBones69
 *
 * This event is fired when a bidding item's time has run out and so a player wins the item.
 *
 */
public class AuctionWinBidEvent extends Event {
    
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final double price;
    private final MarketGoods mg;
    
    /**
     *
     */
    public AuctionWinBidEvent(Player player, MarketGoods mg, double price) {
        this.player = player;
        this.mg = mg;
        this.price = price;
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
    
    public MarketGoods getMarketGoods() {
        return mg;
    }
    
    public double getPrice() {
        return price;
    }
    
}