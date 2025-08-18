package com.maks.mycraftingplugin2;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Menu for selecting which Conjurej recipe is required for a crafting recipe.
 */
public class ConjurejRecipeSelectMenu {

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Select Required Recipe");

        List<String> recipes = ConjurejRecipeUnlockManager.getAllRecipes();
        int index = 0;
        for (String recipe : recipes) {
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.YELLOW + recipe);
                item.setItemMeta(meta);
            }
            inv.setItem(index++, item);
        }

        inv.setItem(26, createMenuItem(Material.ARROW, ChatColor.YELLOW + "Back"));

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
