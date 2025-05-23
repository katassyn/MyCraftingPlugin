package com.maks.mycraftingplugin2;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.sql.*;
import java.util.*;

public class CategoryMenu {
    private static final int ITEMS_PER_PAGE = 45; // Sloty 0-44 na przedmioty

    public static void open(Player player, String category, int page) {
        Inventory inv = Bukkit.createInventory(null, 54, "Category: " + category);

        // Wypełnij pustą przestrzeń białymi szybami
        fillWithGlass(inv);

        // Pobierz receptury z bazy danych
        try (Connection conn = Main.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM recipes WHERE category = ? ORDER BY id ASC")) {

            ps.setString(1, category);
            ResultSet rs = ps.executeQuery();

            List<ItemStack> items = new ArrayList<>();
            List<Integer> recipeIds = new ArrayList<>();

            while (rs.next()) {
                ItemStack resultItem = ItemStackSerializer.deserialize(rs.getString("result_item"));
                int recipeId = rs.getInt("id");

                // Store recipe ID in PersistentDataContainer
                ItemMeta meta = resultItem.getItemMeta();
                if (meta != null) {
                    // Add ID as persistent data
                    NamespacedKey key = new NamespacedKey(Main.getInstance(), "recipe_id");
                    meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, recipeId);

                    // Display ID safely in lore
                    List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
                    // Format as String to avoid byte conversion issues
                    lore.add(ChatColor.GRAY + "Recipe ID: " + String.valueOf(recipeId));
                    meta.setLore(lore);
                    resultItem.setItemMeta(meta);
                }

                items.add(resultItem);
                recipeIds.add(recipeId);
            }

            int startIndex = page * ITEMS_PER_PAGE;
            int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, items.size());

            for (int i = startIndex; i < endIndex; i++) {
                int slot = i - startIndex;
                ItemStack item = items.get(i);
                inv.setItem(slot, item);
            }

            // Przyciski nawigacji
            if (page > 0) {
                inv.setItem(45, createMenuItem(Material.ARROW, ChatColor.GREEN + "Previous Page"));
            }
            if (endIndex < items.size()) {
                inv.setItem(53, createMenuItem(Material.ARROW, ChatColor.GREEN + "Next Page"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Error loading recipes!");
        }

        // Przycisk "Back"
        inv.setItem(49, createMenuItem(Material.BARRIER, ChatColor.RED + "Back"));

        player.openInventory(inv);
    }

    public static void openEditor(Player player, String category, int page) {
        Inventory inv = Bukkit.createInventory(null, 54, "Edit Category: " + category);

        // Wypełnij pustą przestrzeń białymi szybami
        fillWithGlass(inv);

        // Pobierz receptury z bazy danych
        try (Connection conn = Main.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM recipes WHERE category = ? ORDER BY id ASC"))     {

            ps.setString(1, category);
            ResultSet rs = ps.executeQuery();

            List<ItemStack> items = new ArrayList<>();

            while (rs.next()) {
                ItemStack resultItem = ItemStackSerializer.deserialize(rs.getString("result_item"));
                int recipeId = rs.getInt("id");

                // Store recipe ID in PersistentDataContainer
                ItemMeta meta = resultItem.getItemMeta();
                if (meta != null) {
                    // Add ID as persistent data
                    NamespacedKey key = new NamespacedKey(Main.getInstance(), "recipe_id");
                    meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, recipeId);

                    // Display ID safely in lore
                    List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
                    // Format as String to avoid byte conversion issues
                    lore.add(ChatColor.GRAY + "Recipe ID: " + String.valueOf(recipeId));
                    meta.setLore(lore);
                    resultItem.setItemMeta(meta);
                }

                items.add(resultItem);
                // No recipeIds.add() here since the list doesn't exist in this method
            }

            int startIndex = page * ITEMS_PER_PAGE;
            int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, items.size());

            for (int i = startIndex; i < endIndex; i++) {
                int slot = i - startIndex;
                ItemStack item = items.get(i);
                inv.setItem(slot, item);
            }

            // Przyciski nawigacji
            if (page > 0) {
                inv.setItem(45, createMenuItem(Material.ARROW, ChatColor.GREEN + "Previous Page"));
            }
            if (endIndex < items.size()) {
                inv.setItem(53, createMenuItem(Material.ARROW, ChatColor.GREEN + "Next Page"));
            }

            // Przycisk "Add Recipe"
            inv.setItem(48, createMenuItem(Material.WRITABLE_BOOK, ChatColor.GREEN + "Add Recipe"));

            // Przycisk "Back"
            inv.setItem(49, createMenuItem(Material.BARRIER, ChatColor.RED + "Back"));

        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Error loading recipes!");
        }

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

        for (int i = 0; i < 54; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, glass);
            }
        }
    }
}
