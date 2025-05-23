package com.maks.mycraftingplugin2;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.sql.*;
import java.util.*;

public class ZumpeConfirmationMenu {

    public static void open(Player player, int itemId) {
        Inventory inv = Bukkit.createInventory(null, 45, "Zumpe Exchange");

        // Fill inventory with glass panes
        fillWithGlass(inv);

        // Set required items in the appropriate slots
        setRequiredItems(inv, itemId);

        // Set the result item in the center slot (slot 22)
        ItemStack resultItem = getResultItem(itemId);
        if (resultItem != null) {
            inv.setItem(22, resultItem);
        }

        // Set the "Exchange" button (slot 40)
        ItemStack exchangeButton = createExchangeButton(itemId);
        if (exchangeButton != null) {
            inv.setItem(40, exchangeButton);
        }

        // Set the "Back" button (slot 36)
        inv.setItem(36, createMenuItem(Material.ARROW, ChatColor.YELLOW + "Back"));

        player.openInventory(inv);
    }

    private static void setRequiredItems(Inventory inv, int itemId) {
        try (Connection conn = Main.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM zumpe_items WHERE id = ?")) {

            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Retrieve required items from the database and set them in the inventory
                    for (int i = 0; i < 10; i++) {
                        String itemData = rs.getString("required_item_" + (i + 1));
                        if (itemData != null) {
                            ItemStack requiredItem = ItemStackSerializer.deserialize(itemData);
                            // Set the required item in the appropriate slot
                            inv.setItem(10 + i, requiredItem);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().severe("Error setting required items for item " + itemId);
        }
    }

    private static ItemStack getResultItem(int itemId) {
        try (Connection conn = Main.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT item FROM zumpe_items WHERE id = ?")) {

            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String itemData = rs.getString("item");
                    ItemStack resultItem = ItemStackSerializer.deserialize(itemData);

                    // Store the item ID in the item's PersistentDataContainer
                    ItemMeta meta = resultItem.getItemMeta();
                    if (meta != null) {
                        NamespacedKey key = new NamespacedKey(Main.getInstance(), "zumpe_item_id");
                        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, itemId);
                        resultItem.setItemMeta(meta);
                    }

                    return resultItem;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().severe("Error getting result item for item " + itemId);
        }
        return null;
    }

    private static ItemStack createExchangeButton(int itemId) {
        try (Connection conn = Main.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT cost, daily_limit FROM zumpe_items WHERE id = ?")) {

            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double cost = rs.getDouble("cost");
                    int dailyLimit = rs.getInt("daily_limit");

                    ItemStack exchangeButton = new ItemStack(Material.EMERALD);
                    ItemMeta meta = exchangeButton.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(ChatColor.GREEN + "Exchange");

                        List<String> lore = new ArrayList<>();
                        if (cost > 0) {
                            lore.add(ChatColor.YELLOW + "Cost: " + ChatColor.GOLD + formatCost(cost));
                        }
                        if (dailyLimit > 0) {
                            lore.add(ChatColor.YELLOW + "Daily Limit: " + ChatColor.GOLD + dailyLimit);
                        }

                        meta.setLore(lore);
                        exchangeButton.setItemMeta(meta);
                    }

                    return exchangeButton;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().severe("Error creating exchange button for item " + itemId);
        }
        return null;
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
            inv.setItem(i, glass);
        }
    }
}