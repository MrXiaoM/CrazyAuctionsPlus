package studio.trc.bukkit.crazyauctionsplus.database;

import java.util.List;

import studio.trc.bukkit.crazyauctionsplus.util.ItemMail;

public interface ItemMailBox
{
    /**
     * Get the player's item mailbox
     */
    List<ItemMail> getMailBox();
    
    /**
     * Get Item Mail on mailbox.
     * @param uid item mail's uid
     */
    ItemMail getMail(long uid);
    
    /**
     * Add new item mail to player's mailbox
     */
    void addItem(ItemMail... im);
    
    /**
     * Remove the specified item email from the player's item mailbox
     */
    void removeItem(ItemMail... im);
    
    /**
     * Empty player's item mailbox
     */
    void clearMailBox();
    
    /*
      Upload cached item mailbox data to the database.
     */
//    public void uploadMailBox();
    
    /**
     * Get the player's item email count
     */
    int getMailNumber();
    
    /**
     * Make a new UID.
     */
    long makeUID();
}
