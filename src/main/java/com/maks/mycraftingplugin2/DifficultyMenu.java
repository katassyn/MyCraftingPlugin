package com.maks.mycraftingplugin2;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

public class DifficultyMenu {

    public static void open(Player player, String qLevel) {
        Inventory inv = Bukkit.createInventory(null, 27, "Difficulty for " + qLevel);

        inv.setItem(11, createMenuItem(Material.REDSTONE, ChatColor.RED + "Infernal"));
        inv.setItem(13, createMenuItem(Material.BLAZE_POWDER, ChatColor.GOLD + "Hell"));
        inv.setItem(15, createMenuItem(Material.NETHER_STAR, ChatColor.DARK_RED + "Blood"));

        fillWithGlass(inv);

        // Przycisk "Back"
        inv.setItem(22, createMenuItem(Material.BARRIER, ChatColor.RED + "Back"));

        // Przechowaj qLevel dla gracza
        TemporaryData.setPlayerData(player.getUniqueId(), "qLevel", qLevel);

        player.openInventory(inv);
    }

    private static ItemStack createMenuItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if(meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static void fillWithGlass(Inventory inv) {
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        if(meta != null) {
            meta.setDisplayName(" ");
            glass.setItemMeta(meta);
        }

        for (int i = 0; i < inv.getSize(); i++) {
            if(inv.getItem(i) == null)
                inv.setItem(i, glass);
        }
    }
    public static void openEditor(Player player, String qLevel) {
        Inventory inv = Bukkit.createInventory(null, 27, "Edit Difficulty for " + qLevel);

        inv.setItem(11, createMenuItem(Material.REDSTONE, ChatColor.RED + "Infernal"));
        inv.setItem(13, createMenuItem(Material.BLAZE_POWDER, ChatColor.GOLD + "Hell"));
        inv.setItem(15, createMenuItem(Material.NETHER_STAR, ChatColor.DARK_RED + "Blood"));

        fillWithGlass(inv);

        // Przycisk "Back"
        inv.setItem(22, createMenuItem(Material.BARRIER, ChatColor.RED + "Back"));

        // Przechowaj qLevel dla gracza
        TemporaryData.setPlayerData(player.getUniqueId(), "qLevel", qLevel);

        player.openInventory(inv);
    }

}
