package studio.trc.bukkit.crazyauctionsplus.util;

import org.bukkit.inventory.ItemStack;

import static studio.trc.bukkit.crazyauctionsplus.util.GUI.putFlag;

public class MenuIcon {
    public final long uid;
    public final ItemStack item;

    public MenuIcon(long uid, ItemStack item) {
        this.uid = uid;
        this.item = item;
        putFlag(item, null);
    }

    public static MenuIcon icon(long uid, ItemStack item) {
        return new MenuIcon(uid, item);
    }
}
