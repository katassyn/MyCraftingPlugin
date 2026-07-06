package com.maks.mycraftingplugin2;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

public class DemonTowerMenu {

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Demon Tower");

        inv.setItem(10, createMenuItem(Material.BLAZE_ROD, ChatColor.RED + "I Floor"));
        inv.setItem(12, createMenuItem(Material.BLAZE_ROD, ChatColor.GOLD + "II Floor"));
        inv.setItem(14, createMenuItem(Material.BLAZE_ROD, ChatColor.YELLOW + "III Floor"));
        inv.setItem(16, createMenuItem(Material.BLAZE_ROD, ChatColor.DARK_RED + "IV Floor"));

        fillWithGlass(inv);

        // Przycisk "Back"
        inv.setItem(22, createMenuItem(Material.BARRIER, ChatColor.RED + "Back"));

        player.openInventory(inv);
    }

    public static void openEditor(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Edit Demon Tower");

        inv.setItem(10, createMenuItem(Material.BLAZE_ROD, ChatColor.RED + "I Floor"));
        inv.setItem(12, createMenuItem(Material.BLAZE_ROD, ChatColor.GOLD + "II Floor"));
        inv.setItem(14, createMenuItem(Material.BLAZE_ROD, ChatColor.YELLOW + "III Floor"));
        inv.setItem(16, createMenuItem(Material.BLAZE_ROD, ChatColor.DARK_RED + "IV Floor"));

        fillWithGlass(inv);

        // Przycisk "Back"
        inv.setItem(22, createMenuItem(Material.BARRIER, ChatColor.RED + "Back"));

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
            if (inv.getItem(i) == null)
                inv.setItem(i, glass);
        }
    }
}
