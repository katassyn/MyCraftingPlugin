package com.maks.mycraftingplugin2;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;
import org.bukkit.persistence.PersistentDataType;

import java.sql.*;
import java.util.*;

/**
 * Menu to display and handle shop items for Gnob's shop.
 */
public class GnobShopItemsMenu {
    private static final int ITEMS_PER_PAGE = 45; // Slots 0-44 for items

    /**
     * Opens the shop items menu for a specific category.
     * @param player The player viewing the menu.
     * @param shopType The shop type ("Shop" or "Event Shop").
     * @param tierType The tier type ("Basic", "Premium", "Deluxe").
     * @param page The page number to display.
     */
    public static void open(Player player, String shopType, String tierType, int page) {
        String category = "gnob_" + shopType.toLowerCase().replace(" ", "_") + "_" + tierType.toLowerCase();
        Inventory inv = Bukkit.createInventory(null, 54, "Gnob: " + shopType + " - " + tierType);

        // Fill with background glass
        fillWithGlass(inv);

        // Get items from database
        try (Connection conn = Main.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM gnob_items WHERE category = ? ORDER BY slot ASC")) {

            ps.setString(1, category);
            ResultSet rs = ps.executeQuery();

            List<ItemStack> items = new ArrayList<>();
            List<Integer> itemIds = new ArrayList<>();
            List<Integer> dailyLimits = new ArrayList<>();
            List<Double> costs = new ArrayList<>();

            while (rs.next()) {
                ItemStack resultItem = ItemStackSerializer.deserialize(rs.getString("item"));
                int itemId = rs.getInt("id");
                int dailyLimit = rs.getInt("daily_limit");
                double cost = rs.getDouble("cost");

                // Store item ID in PersistentDataContainer
                ItemMeta meta = resultItem.getItemMeta();
                if (meta != null) {
                    // Add ID as persistent data
                    NamespacedKey key = new NamespacedKey(Main.getInstance(), "item_id");
                    meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, itemId);

                    // Create lore with info
                    List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
                    lore.add(ChatColor.GRAY + "Cost: " + ChatColor.GOLD + formatCost(cost));

                    // Add daily limit info
                    if (dailyLimit > 0) {
                        int usedToday = GnobTransactionManager.getTransactionCount(player.getUniqueId(), itemId);
                        int remainingUses = dailyLimit - usedToday;
                        lore.add(ChatColor.GRAY + "Daily Limit: " + ChatColor.YELLOW +
                                 remainingUses + "/" + dailyLimit);

                        // Optionally add color in depending on remaining uses
                        if (remainingUses == 0) {
                            lore.add(ChatColor.RED + "Limit reached for today!");
                        }
                    }

                    meta.setLore(lore);
                    resultItem.setItemMeta(meta);
                }

                items.add(resultItem);
                itemIds.add(itemId);
                dailyLimits.add(dailyLimit);
                costs.add(cost);
            }

            int startIndex = page * ITEMS_PER_PAGE;
            int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, items.size());

            for (int i = startIndex; i < endIndex; i++) {
                int slot = i - startIndex;
                ItemStack item = items.get(i);
                inv.setItem(slot, item);
            }

            // Navigation buttons
            if (page > 0) {
                inv.setItem(45, createMenuItem(Material.ARROW, ChatColor.GREEN + "Previous Page"));
            }
            if (endIndex < items.size()) {
                inv.setItem(53, createMenuItem(Material.ARROW, ChatColor.GREEN + "Next Page"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Error loading items!");
        }

        // Back button
        inv.setItem(49, createMenuItem(Material.BARRIER, ChatColor.RED + "Back"));

        // Save data for navigation
        TemporaryData.setPlayerData(player.getUniqueId(), "gnob_shop_type", shopType);
        TemporaryData.setPlayerData(player.getUniqueId(), "gnob_tier_type", tierType);
        TemporaryData.setPage(player.getUniqueId(), category, page);

        player.openInventory(inv);
    }

    /**
     * Opens the shop items menu in editor mode.
     */
    public static void openEditor(Player player, String shopType, String tierType, int page) {
        String category = "gnob_" + shopType.toLowerCase().replace(" ", "_") + "_" + tierType.toLowerCase();
        Inventory inv = Bukkit.createInventory(null, 54, "Edit Gnob: " + shopType + " - " + tierType);

        // Fill with background glass
        fillWithGlass(inv);

        // Get items from database
        try (Connection conn = Main.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM gnob_items WHERE category = ? ORDER BY slot ASC")) {

            ps.setString(1, category);
            ResultSet rs = ps.executeQuery();

            List<ItemStack> items = new ArrayList<>();
            List<Integer> itemIds = new ArrayList<>();

            while (rs.next()) {
                ItemStack resultItem = ItemStackSerializer.deserialize(rs.getString("item"));
                int itemId = rs.getInt("id");
                int dailyLimit = rs.getInt("daily_limit");
                double cost = rs.getDouble("cost");

                // Store item ID and other data
                ItemMeta meta = resultItem.getItemMeta();
                if (meta != null) {
                    NamespacedKey key = new NamespacedKey(Main.getInstance(), "item_id");
                    meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, itemId);

                    List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
                    lore.add(ChatColor.GRAY + "Cost: " + ChatColor.GOLD + formatCost(cost));
                    lore.add(ChatColor.GRAY + "Daily Limit: " + ChatColor.YELLOW + dailyLimit);
                    lore.add(ChatColor.GRAY + "ID: " + ChatColor.WHITE + itemId);

                    meta.setLore(lore);
                    resultItem.setItemMeta(meta);
                }

                items.add(resultItem);
                itemIds.add(itemId);
            }

            int startIndex = page * ITEMS_PER_PAGE;
            int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, items.size());

            for (int i = startIndex; i < endIndex; i++) {
                int slot = i - startIndex;
                ItemStack item = items.get(i);
                inv.setItem(slot, item);
            }

            // Navigation buttons
            if (page > 0) {
                inv.setItem(45, createMenuItem(Material.ARROW, ChatColor.GREEN + "Previous Page"));
            }
            if (endIndex < items.size()) {
                inv.setItem(53, createMenuItem(Material.ARROW, ChatColor.GREEN + "Next Page"));
            }

            // Add Item button
            inv.setItem(48, createMenuItem(Material.EMERALD, ChatColor.GREEN + "Add Item"));

            // Back button
            inv.setItem(49, createMenuItem(Material.BARRIER, ChatColor.RED + "Back"));

        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Error loading items!");
        }

        // Save data for navigation
        TemporaryData.setPlayerData(player.getUniqueId(), "gnob_shop_type", shopType);
        TemporaryData.setPlayerData(player.getUniqueId(), "gnob_tier_type", tierType);
        TemporaryData.setPage(player.getUniqueId(), category, page);

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
        // Changed to BLACK_STAINED_GLASS_PANE for consistency
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

    private static String formatCost(double cost) {
        if (cost >= 1_000_000_000) {
            return String.format("%.2fB", cost / 1_000_000_000);
        } else if (cost >= 1_000_000) {
            return String.format("%.2fM", cost / 1_000_000);
        } else if (cost >= 1_000) {
            return String.format("%.2fk", cost / 1_000);
        } else {
            return String.format("%.0f", cost);
        }
    }
}
