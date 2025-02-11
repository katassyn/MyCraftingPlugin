package com.maks.mycraftingplugin2;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM recipes WHERE category = ? ORDER BY slot ASC")) {

            ps.setString(1, category);
            ResultSet rs = ps.executeQuery();

            List<ItemStack> items = new ArrayList<>();
            List<Integer> recipeIds = new ArrayList<>();

            while (rs.next()) {
                ItemStack resultItem = ItemStackSerializer.deserialize(rs.getString("result_item"));
                int recipeId = rs.getInt("id");

                // Dodaj id receptury do metadanych przedmiotu
                ItemMeta meta = resultItem.getItemMeta();
                if (meta != null) {
                    List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
                    lore.add(ChatColor.GRAY + "Recipe ID: " + recipeId);
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
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM recipes WHERE category = ? ORDER BY slot ASC")) {

            ps.setString(1, category);
            ResultSet rs = ps.executeQuery();

            List<ItemStack> items = new ArrayList<>();

            while (rs.next()) {
                ItemStack resultItem = ItemStackSerializer.deserialize(rs.getString("result_item"));
                int recipeId = rs.getInt("id");

                // Dodaj id receptury do metadanych przedmiotu
                ItemMeta meta = resultItem.getItemMeta();
                if (meta != null) {
                    List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
                    lore.add(ChatColor.GRAY + "Recipe ID: " + recipeId);
                    meta.setLore(lore);
                    resultItem.setItemMeta(meta);
                }

                items.add(resultItem);
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
        ItemStack glass = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
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