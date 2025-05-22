package studio.trc.bukkit.crazyauctionsplus.util;

import com.google.common.collect.Lists;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTType;
import de.tr7zw.changeme.nbtapi.handler.NBTHandlers;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBTCompoundList;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBTList;
import de.tr7zw.changeme.nbtapi.iface.ReadableNBT;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static studio.trc.bukkit.crazyauctionsplus.util.AdventureUtil.miniMessage;

public class AdventureItemStack {
    private static boolean textUseComponent;
    private static boolean itemNbtUseComponentsFormat;
    private static boolean componentUseNBT;
    protected static void init() {
        itemNbtUseComponentsFormat = MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4);
        ItemStack item = new ItemStack(Material.STONE);
        ItemMeta meta = item.getItemMeta();
        String testDisplayName = "§a§l测试§e§l文本";
        if (meta == null) { // 预料之外的情况
            textUseComponent = false;
            componentUseNBT = false;
        } else {
            meta.setDisplayName(testDisplayName);
            item.setItemMeta(meta);
            if (itemNbtUseComponentsFormat) {
                textUseComponent = true;
                componentUseNBT = NBT.getComponents(item, nbt -> { // 1.21.5 开始，文本组件从 JSON 字符串改为了 NBT 组件
                    NBTType type = nbt.getType("minecraft:custom_name");
                    return !type.equals(NBTType.NBTTagString);
                });
            } else {
                // 测试物品是否支持使用 component
                NBT.get(item, nbt -> {
                    ReadableNBT display = nbt.getCompound("display");
                    if (display == null) {
                        textUseComponent = false;
                        return;
                    }
                    String name = display.getString("Name");
                    // 旧版本文本组件不支持 JSON 字符串，设置旧版颜色符之后，物品名会跟之前一样
                    textUseComponent = !name.equals(testDisplayName);
                });
                componentUseNBT = false;
            }
        }
    }
    public static ComponentSerializer<Component, ?, String> serializer() {
        if (textUseComponent) {
            return GsonComponentSerializer.gson();
        } else {
            return LegacyComponentSerializer.legacySection();
        }
    }


    public static ItemStack buildItem(Material material, String name, String... lore) {
        return buildItem(material, null, name, Lists.newArrayList(lore));
    }
    public static ItemStack buildItem(Material material, String name, List<String> lore) {
        return buildItem(material, null, name, lore);
    }
    public static ItemStack buildItem(Material material, Integer customModelData, String name, String... lore) {
        return buildItem(material, customModelData, name, Lists.newArrayList(lore));
    }
    public static ItemStack buildItem(Material material, Integer customModelData, String name, List<String> lore) {
        if (material.equals(Material.AIR)) return new ItemStack(material);
        ItemStack item = new ItemStack(material, 1);
        setItemDisplayName(item, name);
        setItemLoreMiniMessage(item, lore);
        if (customModelData != null) setCustomModelData(item, customModelData);
        return item;
    }

    public static void setItemDisplayName(ItemStack item, String name) {
        if (isEmpty(item)) return;
        Component displayName = miniMessage(name);
        setItemDisplayNameByJson(item, serializer().serialize(displayName));
    }

    public static void setItemDisplayName(ItemStack item, Component name) {
        if (isEmpty(item)) return;
        setItemDisplayNameByJson(item, serializer().serialize(name));
    }

    @Nullable
    public String getItemDisplayNameAsMiniMessage(ItemStack item) {
        Component component = getItemDisplayName(item);
        if (component == null) return null;
        return miniMessage(component);
    }

    @Nullable
    public Component getItemDisplayName(ItemStack item) {
        String nameAsJson = getItemDisplayNameAsJson(item);
        if (nameAsJson == null) return null;
        return serializer().deserialize(nameAsJson);
    }

    @Nullable
    public static String getItemDisplayNameAsJson(ItemStack item) {
        if (isEmpty(item)) return null;
        if (itemNbtUseComponentsFormat) {
            ReadWriteNBT nbtItem = NBT.itemStackToNBT(item);
            ReadWriteNBT nbt = nbtItem.getCompound("components");
            if (nbt == null) return null;
            if (componentUseNBT) {
                ReadWriteNBT component = nbt.hasTag("minecraft:custom_name")
                        ? nbt.getCompound("minecraft:custom_name")
                        : null;
                return component != null
                        ? component.toString()
                        : null;
            } else {
                return nbt.hasTag("minecraft:custom_name", NBTType.NBTTagString)
                        ? nbt.getString("minecraft:custom_name")
                        : null;
            }
        } else {
            return NBT.get(item, nbt -> {
                ReadableNBT display = nbt.getCompound("display");
                return display != null && display.hasTag("Name", NBTType.NBTTagString)
                        ? display.getString("Name")
                        : null;
            });
        }
    }

    public static void setItemDisplayNameByJson(ItemStack item, String json) {
        if (itemNbtUseComponentsFormat) {
            NBT.modifyComponents(item, nbt -> {
                if (componentUseNBT) {
                    ReadWriteNBT component = NBT.parseNBT(json);
                    nbt.set("minecraft:custom_name", component, NBTHandlers.STORE_READWRITE_TAG);
                } else {
                    nbt.setString("minecraft:custom_name", json);
                }
            });
        } else {
            NBT.modify(item, nbt -> {
                ReadWriteNBT display = nbt.getOrCreateCompound("display");
                display.setString("Name", json);
            });
        }
    }

    public static void setItemLoreMiniMessage(ItemStack item, String... lore) {
        setItemLoreMiniMessage(item, Lists.newArrayList(lore));
    }

    public static void setItemLoreMiniMessage(ItemStack item, List<String> lore) {
        if (isEmpty(item)) return;
        List<String> json = new ArrayList<>();
        for (String s : lore) {
            Component line = miniMessage(s);
            json.add(serializer().serialize(line));
        }
        setItemLoreByJson(item, json);
    }

    public static void setItemLore(ItemStack item, List<Component> lore) {
        if (isEmpty(item)) return;
        List<String> json = new ArrayList<>();
        for (Component component : lore) {
            json.add(serializer().serialize(component));
        }
        setItemLoreByJson(item, json);
    }

    public static List<String> getItemLoreAsMiniMessage(ItemStack item) {
        List<Component> components = getItemLore(item);
        if (components == null) return new ArrayList<>();
        List<String> lore = new ArrayList<>();
        for (Component component : components) {
            String s = miniMessage(component);
            lore.add(s);
        }
        return lore;
    }

    public static List<Component> getItemLore(ItemStack item) {
        List<String> loreAsJson = getItemLoreAsJson(item);
        if (loreAsJson == null) return new ArrayList<>();
        List<Component> lore = new ArrayList<>();
        for (String line : loreAsJson) {
            Component component = serializer().deserialize(line);
            lore.add(component);
        }
        return lore;
    }

    @Nullable
    public static List<String> getItemLoreAsJson(ItemStack item) {
        if (isEmpty(item)) return null;
        if (itemNbtUseComponentsFormat) {
            ReadWriteNBT nbtItem = NBT.itemStackToNBT(item);
            ReadWriteNBT nbt = nbtItem.getCompound("components");
            if (nbt == null) return null;
            if (componentUseNBT) {
                ReadWriteNBTCompoundList components = nbt.hasTag("minecraft:custom_name")
                        ? nbt.getCompoundList("minecraft:custom_name")
                        : null;
                if (components == null) return null;
                List<String> list = new ArrayList<>();
                for (ReadWriteNBT component : components) {
                    list.add(component.toString());
                }
                return list;
            } else {
                return nbt.hasTag("minecraft:custom_name", NBTType.NBTTagList)
                        ? nbt.getStringList("minecraft:lore").toListCopy()
                        : null;
            }
        } else {
            return NBT.get(item, nbt -> {
                ReadableNBT display = nbt.getCompound("display");
                return display != null && display.hasTag("Lore", NBTType.NBTTagList)
                        ? display.getStringList("Lore").toListCopy()
                        : null;
            });
        }
    }

    public static void setItemLoreByJson(ItemStack item, List<String> json) {
        if (itemNbtUseComponentsFormat) {
            NBT.modifyComponents(item, nbt -> {
                if (componentUseNBT) {
                    ReadWriteNBTCompoundList list = nbt.getCompoundList("minecraft:lore");
                    if (!list.isEmpty()) list.clear();
                    for (String s : json) {
                        ReadWriteNBT component = NBT.parseNBT(s);
                        list.addCompound(component);
                    }
                } else {
                    ReadWriteNBTList<String> list = nbt.getStringList("minecraft:lore");
                    if (!list.isEmpty()) list.clear();
                    list.addAll(json);
                }
            });
        } else {
            NBT.modify(item, nbt -> {
                ReadWriteNBT display = nbt.getOrCreateCompound("display");
                ReadWriteNBTList<String> list = display.getStringList("Lore");
                if (!list.isEmpty()) list.clear();
                list.addAll(json);
            });
        }
    }

    public static void setCustomModelData(ItemStack item, Integer customModelData) {
        if (!MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_14_R1)) return;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(customModelData);
            item.setItemMeta(meta);
        }
    }

    @Contract("null -> true")
    public static boolean isEmpty(ItemStack item) {
        return item == null || item.getType().equals(Material.AIR);
    }
}
