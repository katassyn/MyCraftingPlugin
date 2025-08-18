package com.maks.mycraftingplugin2;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class AddRecipeMenu {

    // Mapa przechowująca kategorię dla każdego gracza
    private static Map<UUID, String> playerCategories = new HashMap<>();

    // Mapa przechowująca stan GUI dla każdego gracza
    private static Map<UUID, ItemStack[]> guiStates = new HashMap<>();

    public static void open(Player player, String category) {
        Inventory inv = Bukkit.createInventory(null, 27, "Add New Recipe");

        // Wypełniamy interfejs szkłem
        fillWithGlass(inv);

        // Jeśli gracz ma zapisany stan GUI, przywróć go
        if (guiStates.containsKey(player.getUniqueId())) {
            inv.setContents(guiStates.get(player.getUniqueId()));
            if ("conjurer_shop".equalsIgnoreCase(category)) {
                String required = TemporaryData.getRequiredRecipe(player.getUniqueId());
                String display = (required != null) ? required : "None";
                inv.setItem(25, createInfoItem("Required Recipe", display));
            }
        } else {
            // Ustaw pola na wymagane przedmioty (sloty 0-9)
            for (int i = 0; i < 10; i++) {
                inv.setItem(i, createPlaceholderItem(
                        Material.GRAY_STAINED_GLASS_PANE,
                        ChatColor.YELLOW + "Required Item " + (i + 1)
                ));
            }

            // Przedmiot wynikowy (slot 13)
            inv.setItem(13, createPlaceholderItem(
                    Material.LIGHT_BLUE_STAINED_GLASS_PANE,
                    ChatColor.YELLOW + "Result Item"
            ));

            // Szansa na sukces (slot 20)
            inv.setItem(20, createInfoItem(
                    "Success Chance",
                    "100%"
            ));
            TemporaryData.setSuccessChance(player.getUniqueId(), 100.0);

            // Koszt (slot 21)
            inv.setItem(21, createInfoItem(
                    "Cost",
                    "0"
            ));
            TemporaryData.setCost(player.getUniqueId(), 0.0);

            // Przycisk "Save" (slot 22)
            inv.setItem(22, createMenuItem(
                    Material.EMERALD, ChatColor.GREEN + "Save"
            ));

            // Required recipe selector for Conjurer shop
            if ("conjurer_shop".equalsIgnoreCase(category)) {
                String required = TemporaryData.getRequiredRecipe(player.getUniqueId());
                String display = (required != null) ? required : "None";
                inv.setItem(25, createInfoItem("Required Recipe", display));
            } else {
                TemporaryData.removeRequiredRecipe(player.getUniqueId());
            }

            // Przycisk "Back" (slot 24)
            inv.setItem(24, createMenuItem(
                    Material.ARROW, ChatColor.YELLOW + "Back"
            ));
        }

        // Zapamiętaj kategorię dla gracza
        setCategory(player.getUniqueId(), category);

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

    private static ItemStack createPlaceholderItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if(meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Collections.singletonList(ChatColor.GRAY + "Click to set"));
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack createInfoItem(String name, String value) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if(meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + name);
            meta.setLore(Collections.singletonList(ChatColor.WHITE + value));
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
            inv.setItem(i, glass);
        }
    }

    // Metody do zarządzania kategorią dla gracza
    public static void setCategory(UUID playerUUID, String category) {
        playerCategories.put(playerUUID, category);
    }

    public static String getCategory(UUID playerUUID) {
        return playerCategories.get(playerUUID);
    }

    public static void removeCategory(UUID playerUUID) {
        playerCategories.remove(playerUUID);
    }

    // Metody do zarządzania stanem GUI
    public static void saveGuiState(UUID playerUUID, ItemStack[] contents) {
        guiStates.put(playerUUID, contents);
    }

    public static ItemStack[] getGuiState(UUID playerUUID) {
        return guiStates.get(playerUUID);
    }

    public static void removeGuiState(UUID playerUUID) {
        guiStates.remove(playerUUID);
    }
}
