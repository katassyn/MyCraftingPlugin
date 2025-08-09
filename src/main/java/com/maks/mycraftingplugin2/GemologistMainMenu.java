package com.maks.mycraftingplugin2;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Main menu for the Gemologist (normal and edit mode).
 */
public class GemologistMainMenu {

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Gemologist Menu");

        inv.setItem(11, createMenuItem(Material.CRAFTING_TABLE, ChatColor.GREEN + "Gem Crafting"));
        inv.setItem(13, createMenuItem(Material.AMETHYST_SHARD, ChatColor.GREEN + "Gem Actions"));
        inv.setItem(15, createMenuItem(Material.GRINDSTONE, ChatColor.GREEN + "Gem Crushing"));

        fillWithGlass(inv);
        player.openInventory(inv);
    }

    public static void openEditor(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Edit Gemologist Menu");

        inv.setItem(11, createMenuItem(Material.CRAFTING_TABLE, ChatColor.GREEN + "Gem Crafting"));
        inv.setItem(13, createMenuItem(Material.AMETHYST_SHARD, ChatColor.GREEN + "Gem Actions"));
        inv.setItem(15, createMenuItem(Material.GRINDSTONE, ChatColor.GREEN + "Gem Crushing"));

        fillWithGlass(inv);
        player.openInventory(inv);
    }

    private static ItemStack createMenuItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static void fillWithGlass(Inventory inv) {
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            glass.setItemMeta(meta);
        }
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, glass);
            }
        }
    }
}
