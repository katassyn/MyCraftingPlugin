package com.maks.mycraftingplugin2;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Główne menu Alchemii (oraz jego tryb edycji).
 */
public class AlchemyMainMenu {

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Alchemy Menu");

        // Cztery kategorie do przeglądania
        inv.setItem(10, createMenuItem(Material.BREWING_STAND, ChatColor.GREEN + "Alchemy Shop"));
        inv.setItem(12, createMenuItem(Material.BEEF,     ChatColor.GREEN + "Tonics Crafting"));
        inv.setItem(14, createMenuItem(Material.MUTTON,    ChatColor.GREEN + "Potions Crafting"));
        inv.setItem(16, createMenuItem(Material.CHICKEN,   ChatColor.GREEN + "Physic Crafting"));

        fillWithGlass(inv);
        player.openInventory(inv);
    }

    public static void openEditor(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Edit Alchemy Menu");

        // Cztery kategorie w trybie edycji
        inv.setItem(10, createMenuItem(Material.BREWING_STAND, ChatColor.GREEN + "Alchemy Shop"));
        inv.setItem(12, createMenuItem(Material.BEEF,     ChatColor.GREEN + "Tonics Crafting"));
        inv.setItem(14, createMenuItem(Material.MUTTON,    ChatColor.GREEN + "Potions Crafting"));
        inv.setItem(16, createMenuItem(Material.CHICKEN,   ChatColor.GREEN + "Physic Crafting"));

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
