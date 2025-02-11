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
import java.text.DecimalFormat;

public class CraftingSchemeMenu {

    public static void open(Player player, ItemStack displayItem, int recipeId) {
        Inventory inv = Bukkit.createInventory(null, 45, "Crafting Scheme");

        // Fill inventory with glass panes
        fillWithGlass(inv);

        // Set required items in the appropriate slots
        setRequiredItems(inv, recipeId);

        // Set the result item in the center slot (slot 22)
        ItemStack resultItem = getResultItem(recipeId);
        if (resultItem != null) {
            inv.setItem(22, resultItem);
        }

        // Set the "Craft" button (slot 40)
        ItemStack craftButton = createCraftButton(recipeId);
        if (craftButton != null) {
            inv.setItem(40, craftButton);
        }

        // Set the "Back" button (slot 36)
        inv.setItem(36, createMenuItem(Material.ARROW, ChatColor.YELLOW + "Back"));

        player.openInventory(inv);
    }

    private static void setRequiredItems(Inventory inv, int recipeId) {
        try (Connection conn = Main.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM recipes WHERE id = ?")) {

            ps.setInt(1, recipeId);
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
            Bukkit.getLogger().severe("Error setting required items for recipe " + recipeId);
        }
    }

    private static ItemStack getResultItem(int recipeId) {
        try (Connection conn = Main.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT result_item FROM recipes WHERE id = ?")) {

            ps.setInt(1, recipeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String itemData = rs.getString("result_item");
                    ItemStack resultItem = ItemStackSerializer.deserialize(itemData);

                    // Store the recipe ID in the item's PersistentDataContainer
                    ItemMeta meta = resultItem.getItemMeta();
                    if (meta != null) {
                        NamespacedKey key = new NamespacedKey(Main.getInstance(), "recipe_id");
                        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, recipeId);
                        resultItem.setItemMeta(meta);
                    }

                    return resultItem;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().severe("Error getting result item for recipe " + recipeId);
        }
        return null;
    }

    private static ItemStack createCraftButton(int recipeId) {
        try (Connection conn = Main.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT cost, success_chance FROM recipes WHERE id = ?")) {

            ps.setInt(1, recipeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double cost = rs.getDouble("cost");
                    double successChance = rs.getDouble("success_chance");

                    ItemStack craftButton = new ItemStack(Material.EMERALD);
                    ItemMeta meta = craftButton.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(ChatColor.GREEN + "Craft");

                        List<String> lore = new ArrayList<>();
                        lore.add(ChatColor.YELLOW + "Cost: " + ChatColor.GOLD + formatCost(cost));
                        lore.add(ChatColor.YELLOW + "Success Chance: " + ChatColor.GOLD + successChance + "%");

                        meta.setLore(lore);
                        craftButton.setItemMeta(meta);
                    }

                    return craftButton;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().severe("Error creating craft button for recipe " + recipeId);
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