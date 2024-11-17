package com.maks.mycraftingplugin2;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.sql.*;

public class EditRecipeMenu {

    // Mapa przechowująca ID receptury dla każdego gracza
    private static HashMap<UUID, Integer> playerRecipeIds = new HashMap<>();

    public static void open(Player player, int recipeId) {
        Inventory inv = Bukkit.createInventory(null, 27, "Edit Recipe");

        // Zapamiętaj ID receptury dla gracza
        setRecipeId(player.getUniqueId(), recipeId);

        // Wypełniamy interfejs szkłem
        fillWithGlass(inv);

        // Pobierz dane receptury z bazy danych
        try {
            Connection conn = Main.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM recipes WHERE id = ?"
            );
            ps.setInt(1, recipeId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Ustaw wymagane przedmioty (sloty 0-9)
                for (int i = 0; i < 10; i++) {
                    String itemData = rs.getString("required_item_" + (i + 1));
                    if (itemData != null) {
                        ItemStack requiredItem = ItemStackSerializer
                                .deserialize(itemData);
                        inv.setItem(i, requiredItem);
                    } else {
                        inv.setItem(i, createMenuItem(
                                Material.GRAY_STAINED_GLASS_PANE,
                                ChatColor.YELLOW + "Required Item " + (i + 1)
                        ));
                    }
                }

                // Przedmiot wynikowy (slot 13)
                ItemStack resultItem = ItemStackSerializer.deserialize(
                        rs.getString("result_item")
                );
                inv.setItem(13, resultItem);

                // Szansa na sukces (slot 20)
                double successChance = rs.getDouble("success_chance");
                inv.setItem(20, createInfoItem(
                        "Success Chance", successChance + "%"
                ));
                TemporaryData.setSuccessChance(player.getUniqueId(),
                        successChance);

                // Koszt (slot 21)
                double cost = rs.getDouble("cost");
                inv.setItem(21, createInfoItem(
                        "Cost", formatCost(cost)
                ));
                TemporaryData.setCost(player.getUniqueId(), cost);

                // Przycisk "Save" (slot 22)
                inv.setItem(22, createMenuItem(
                        Material.EMERALD, ChatColor.GREEN + "Save"
                ));

                // Przycisk "Delete" (slot 23)
                inv.setItem(23, createMenuItem(
                        Material.BARRIER, ChatColor.RED + "Delete"
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        player.openInventory(inv);
    }

    private static ItemStack createMenuItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createInfoItem(String name, String value) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + name);
        meta.setLore(Arrays.asList(ChatColor.WHITE + value));
        item.setItemMeta(meta);
        return item;
    }

    private static void fillWithGlass(Inventory inv) {
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(" ");
        glass.setItemMeta(meta);

        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, glass);
        }
    }

    // Metody do zarządzania ID receptury
    public static void setRecipeId(UUID playerUUID, int recipeId) {
        playerRecipeIds.put(playerUUID, recipeId);
    }

    public static int getRecipeId(UUID playerUUID) {
        return playerRecipeIds.getOrDefault(playerUUID, -1);
    }

    public static void removeRecipeId(UUID playerUUID) {
        playerRecipeIds.remove(playerUUID);
    }

    private static String formatCost(double cost) {
        if (cost >= 1_000_000_000) {
            return (cost / 1_000_000_000) + "kkk";
        } else if (cost >= 1_000_000) {
            return (cost / 1_000_000) + "kk";
        } else if (cost >= 1_000) {
            return (cost / 1_000) + "k";
        } else {
            return String.valueOf(cost);
        }
    }
}
