package studio.trc.bukkit.crazyauctionsplus.util;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import studio.trc.bukkit.crazyauctionsplus.database.GlobalMarket;
import studio.trc.bukkit.crazyauctionsplus.util.FileManager.Files;
import studio.trc.bukkit.crazyauctionsplus.util.enums.ShopType;

/**
 * For processing goods on the market.
 */
public class MarketGoods
{   
    private String topBidder;
    private long timeTillExpire;
    private double price = 0;
    private double reward = 0;
    
    private final ShopType shoptype;
    private final long addedTime;
    private final long fullTime;
    private final ItemOwner owner;
    private final ItemStack item;
    private final long uid;
    
    @Override
    public String toString() {
        return "[MarketGoods] -> [UID=" + uid + ", Owner=" + owner + ", FullTime=" + fullTime + ", TimeTillExpire=" + timeTillExpire + ", AddedTime=" + addedTime + ", ShopType=" + shoptype.getName() + ", Price=" + price + ", Reward=" + reward + ", TopBidder=" + topBidder + ", Item=" + item + "]";
    }
    
    public MarketGoods(long uid, ShopType shoptype, ItemOwner owner, ItemStack item, long timeTillExpire, long fullTime, long addedTime) {
        this.shoptype = shoptype;
        this.owner = owner;
        this.item = item;
        this.timeTillExpire = timeTillExpire;
        this.fullTime = fullTime;
        this.addedTime = addedTime;
        this.uid = uid;
    }

    public MarketGoods(long uid, ShopType shoptype, ItemOwner owner, ItemStack item, long timeTillExpire, long fullTime, long addedTime, double money) {
        this.shoptype = shoptype;
        this.owner = owner;
        this.item = item;
        this.timeTillExpire = timeTillExpire;
        this.fullTime = fullTime;
        this.addedTime = addedTime;
        this.uid = uid;
        if (shoptype.equals(ShopType.SELL) || shoptype.equals(ShopType.BID)) {
            price = money;
        } else if (shoptype.equals(ShopType.BUY)) {
            reward = money;
        }
    }

    public MarketGoods(long uid, ShopType shoptype, ItemOwner owner, ItemStack item, long timeTillExpire, long fullTime, long addedTime, double price, String topBidder) {
        this.shoptype = shoptype;
        this.owner = owner;
        this.item = item;
        this.timeTillExpire = timeTillExpire;
        this.fullTime = fullTime;
        this.addedTime = addedTime;
        this.price = price;
        this.topBidder = topBidder;
        this.uid = uid;
    }

    public MarketGoods(long uid, ShopType shoptype, ItemOwner owner, ItemStack item, long timeTillExpire, long fullTime, long addedTime, double price, OfflinePlayer topBidder) {
        this.shoptype = shoptype;
        this.owner = owner;
        this.item = item;
        this.timeTillExpire = timeTillExpire;
        this.fullTime = fullTime;
        this.addedTime = addedTime;
        this.price = price;
        this.topBidder = topBidder.getName() + ":" + topBidder.getUniqueId();
        this.uid = uid;
    }
    
    /**
     * Get the OfflinePlayer instance of the Top Bidder.
     */
    public String getTopBidder() {
        return topBidder;
    }
    
    /**
     * Get product type
     */
    public ShopType getShopType() {
        return shoptype;
    }
    
    /**
     * Get the price of an item
     */
    public double getPrice() {
        return price;
    }
    
    /**
     * Get paid for goods
     */
    public double getReward() {
        return reward;
    }
    
    /**
     * Get Time Till Expire.
     */
    public long getTimeTillExpire() {
        return timeTillExpire;
    }
    
    /**
     * Get Full Time
     */
    public long getFullTime() {
        return fullTime;
    }
    
    /**
     * Get added time
     */
    public long getAddedTime() {
        if (addedTime == -1) {
            return fullTime - (PluginControl.convertToMill(Files.CONFIG.getFile().getString("Settings.Full-Expire-Time")) - System.currentTimeMillis());
        } else {
            return addedTime;
        }
    }
    
    /**
     * Get the UID of an item.
     */
    public long getUID() {
        return uid;
    }
    
    /**
     * Whether the product has expired.
     * This method is usually called by automatic update detection.
     */
    public boolean expired() {
        return System.currentTimeMillis() >= timeTillExpire;
    }
    
    /**
     * Get item.
     */
    public ItemStack getItem() {
        return item;
    }
    
    /**
     * Get Item's owner.
     */
    public ItemOwner getItemOwner() {
        return owner;
    }
    
    /**
     * Repricing.
     */
    public void repricing(double money) {
        switch (shoptype) {
            case BUY: {
                reward = money;
                break;
            }
            case SELL: {
                price = money;
                break;
            }
        }
    }
    
    /**
     */
    public void setTopBidder(String topBidder) {
        this.topBidder = topBidder;
        GlobalMarket.getMarket().saveData();
    }
    
    /**
     */
    public void setTopBidder(OfflinePlayer player) {
        topBidder = player.getName() + ":" + player.getUniqueId();
        GlobalMarket.getMarket().saveData();
    }
    
    /**
     */
    public void setPrice(double price) {
        this.price = price;
        this.reward = price;
        GlobalMarket.getMarket().saveData();
    }
    
    /**
     */
    public void setTimeTillExpire(long timeTillExpire) {
        this.timeTillExpire = timeTillExpire;
        GlobalMarket.getMarket().saveData();
    }
}
