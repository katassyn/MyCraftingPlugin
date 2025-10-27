package com.maks.mycraftingplugin2;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Shop submenu for Gnob with tier access controls.
 */
public class GnobShopMenu {

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Gnob - Shop");

        // Three tier options
        inv.setItem(10, createMenuItem(Material.IRON_INGOT, ChatColor.WHITE + "Basic"));

        // Premium tier - check permissions
        if (hasPermission(player, "premium")) {
            inv.setItem(13, createMenuItem(Material.GOLD_INGOT, ChatColor.GOLD + "Premium"));
        } else {
            inv.setItem(13, createLockedMenuItem(Material.GOLD_INGOT, ChatColor.GOLD + "Premium",
                    "Requires Premium or Deluxe rank"));
        }

        // Deluxe tier - check permissions
        if (hasPermission(player, "deluxe")) {
            inv.setItem(16, createMenuItem(Material.DIAMOND, ChatColor.AQUA + "Deluxe"));
        } else {
            inv.setItem(16, createLockedMenuItem(Material.DIAMOND, ChatColor.AQUA + "Deluxe",
                    "Requires Deluxe rank"));
        }

        // Back button
        inv.setItem(22, createMenuItem(Material.ARROW, ChatColor.YELLOW + "Back"));

        fillWithGlass(inv);
        player.openInventory(inv);
    }

    public static void openEditor(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Edit Gnob - Shop");

        // Three tier options (in edit mode, all are accessible)
        inv.setItem(10, createMenuItem(Material.IRON_INGOT, ChatColor.WHITE + "Basic"));
        inv.setItem(13, createMenuItem(Material.GOLD_INGOT, ChatColor.GOLD + "Premium"));
        inv.setItem(16, createMenuItem(Material.DIAMOND, ChatColor.AQUA + "Deluxe"));

        // Back button
        inv.setItem(22, createMenuItem(Material.ARROW, ChatColor.YELLOW + "Back"));

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

    private static ItemStack createLockedMenuItem(Material material, String name, String lockReason) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name + ChatColor.RED + " (Locked)");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.RED + lockReason);
            meta.setLore(lore);
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

    /**
     * Checks if a player has the required rank or higher.
     * @param player The player to check
     * @param requiredRank The minimum rank required ('premium' or 'deluxe')
     * @return True if player has the required rank or higher
     */
    private static boolean hasPermission(Player player, String requiredRank) {
        if (requiredRank.equals("premium")) {
            return player.hasPermission("mycraftingplugin.premium") ||
                   player.hasPermission("mycraftingplugin.deluxe");
        } else if (requiredRank.equals("deluxe")) {
            return player.hasPermission("mycraftingplugin.deluxe");
        }
        return false;
    }
}
