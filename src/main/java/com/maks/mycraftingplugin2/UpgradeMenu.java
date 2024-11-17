package com.maks.mycraftingplugin2;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

public class UpgradeMenu {

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Upgrade Levels");

        // Wypełnij pozostałe sloty szkłem
        fillWithGlass(inv);

        // Dodaj q1 do q10 w uporządkowany sposób
        int[] slots = {10, 11, 12, 13, 14, 15, 16}; // Sloty w drugim rzędzie
        for (int i = 1; i <= 7; i++) {
            inv.setItem(slots[i - 1], createMenuItem(Material.PAPER, ChatColor.GREEN + "q" + i));
        }

        // Trzeci rząd dla q8, q9, q10
        inv.setItem(19, createMenuItem(Material.PAPER, ChatColor.GREEN + "q8"));
        inv.setItem(20, createMenuItem(Material.PAPER, ChatColor.GREEN + "q9"));
        inv.setItem(21, createMenuItem(Material.PAPER, ChatColor.GREEN + "q10"));

        // Przycisk "Back"
        inv.setItem(22, createMenuItem(Material.BARRIER, ChatColor.RED + "Back"));

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

    public static void openEditor(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Edit Upgrade Levels");

        // Wypełnij pozostałe sloty szkłem
        fillWithGlass(inv);

        // Dodaj q1 do q10 w uporządkowany sposób
        int[] slots = {10, 11, 12, 13, 14, 15, 16}; // Sloty w drugim rzędzie
        for (int i = 1; i <= 7; i++) {
            inv.setItem(slots[i - 1], createMenuItem(Material.PAPER, ChatColor.GREEN + "q" + i));
        }

        // Trzeci rząd dla q8, q9, q10
        inv.setItem(19, createMenuItem(Material.PAPER, ChatColor.GREEN + "q8"));
        inv.setItem(20, createMenuItem(Material.PAPER, ChatColor.GREEN + "q9"));
        inv.setItem(21, createMenuItem(Material.PAPER, ChatColor.GREEN + "q10"));

        // Przycisk "Back"
        inv.setItem(22, createMenuItem(Material.BARRIER, ChatColor.RED + "Back"));

        player.openInventory(inv);
    }

}
