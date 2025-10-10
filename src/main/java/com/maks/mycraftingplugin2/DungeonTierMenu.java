package com.maks.mycraftingplugin2;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DungeonTierMenu {

    public static void open(Player player, String dungeonType) {
        Inventory inv = Bukkit.createInventory(null, 27, dungeonType + " - Tier Selection");

        inv.setItem(9, createMenuItem(Material.IRON_SWORD, ChatColor.WHITE + "Tier 1"));
        inv.setItem(11, createMenuItem(Material.IRON_AXE, ChatColor.GREEN + "Tier 2"));
        inv.setItem(13, createMenuItem(Material.DIAMOND_SWORD, ChatColor.BLUE + "Tier 3"));
        inv.setItem(15, createMenuItem(Material.NETHERITE_SWORD, ChatColor.DARK_PURPLE + "Tier 4"));
        inv.setItem(17, createMenuItem(Material.TRIDENT, ChatColor.GOLD + "Tier 5"));

        inv.setItem(22, createMenuItem(Material.BARRIER, ChatColor.RED + "Back"));

        fillWithGlass(inv);
        player.openInventory(inv);
    }

    public static void openEditor(Player player, String dungeonType) {
        Inventory inv = Bukkit.createInventory(null, 27, "Edit " + dungeonType + " - Tier Selection");

        inv.setItem(9, createMenuItem(Material.IRON_SWORD, ChatColor.WHITE + "Tier 1"));
        inv.setItem(11, createMenuItem(Material.IRON_AXE, ChatColor.GREEN + "Tier 2"));
        inv.setItem(13, createMenuItem(Material.DIAMOND_SWORD, ChatColor.BLUE + "Tier 3"));
        inv.setItem(15, createMenuItem(Material.NETHERITE_SWORD, ChatColor.DARK_PURPLE + "Tier 4"));
        inv.setItem(17, createMenuItem(Material.TRIDENT, ChatColor.GOLD + "Tier 5"));

        inv.setItem(22, createMenuItem(Material.BARRIER, ChatColor.RED + "Back"));

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