package com.maks.mycraftingplugin2;

import com.maks.mycraftingplugin2.integration.EventIntegrationHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.*;
import java.util.*;

/**
 * Menu for adding or editing items in Gnob shop.
 */
public class GnobAddItemMenu {

    private static Map<UUID, String> playerCategories = new HashMap<>();
    private static Map<UUID, ItemStack[]> guiStates = new HashMap<>();
    private static Map<UUID, Integer> editItemIds = new HashMap<>();

    /**
     * Opens the menu to add a new item to Gonb shop.
     */
    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Add Gonb Item");

        // Fill with glass
        fillWithGlass(inv);

        String category = "gonb";

        // Check if there's a saved GUI state
        if (guiStates.containsKey(player.getUniqueId())) {
            inv.setContents(guiStates.get(player.getUniqueId()));
        } else {
            // Required items (slots 0-9)
            for (int i = 0; i < 10; i++) {
                inv.setItem(i, createPlaceholderItem(
                        Material.GRAY_STAINED_GLASS_PANE,
                        ChatColor.YELLOW + "Required Item " + (i + 1)
                ));
            }

            // Result item (slot 13)
            inv.setItem(13, createPlaceholderItem(
                    Material.LIGHT_BLUE_STAINED_GLASS_PANE,
                    ChatColor.YELLOW + "Result Item"
            ));

            // Cost (11)
            inv.setItem(11, createInfoItem(
                    "Cost",
                    "0"
            ));
            TemporaryData.setCost(player.getUniqueId(), 0.0);

            // Event selection (12) - Gnob has event support for all offers
            // Check if event_id is already set in TemporaryData (from EventSelectMenu)
            Object tempEventObj = TemporaryData.getPlayerData(player.getUniqueId(), "gnob_event_id");
            String eventId = null;
            if (tempEventObj instanceof String) {
                eventId = (String) tempEventObj;
            }
            inv.setItem(12, createEventInfoItem(eventId));
            if (tempEventObj == null) {
                TemporaryData.setPlayerData(player.getUniqueId(), "gnob_event_id", null);
            }

            // Daily Limit (15)
            inv.setItem(15, createInfoItem(
                    "Daily Limit",
                    "0"
            ));
            TemporaryData.setPlayerData(player.getUniqueId(), "gnob_daily_limit", 0);

            // Save button (22)
            inv.setItem(22, createMenuItem(
                    Material.EMERALD, ChatColor.GREEN + "Save"
            ));

            // Back button (24)
            inv.setItem(24, createMenuItem(
                    Material.ARROW, ChatColor.YELLOW + "Back"
            ));
        }

        // Save category
        setCategory(player.getUniqueId(), category);

        player.openInventory(inv);
    }

    /**
     * Opens the menu to edit an existing item.
     */
    public static void openEdit(Player player, int itemId) {
        Inventory inv = Bukkit.createInventory(null, 27, "Edit Gonb Item");

        // Fill with glass
        fillWithGlass(inv);

        // Store item ID for editing
        editItemIds.put(player.getUniqueId(), itemId);

        try (Connection conn = Main.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM gnob_items WHERE id = ?")) {

            ps.setInt(1, itemId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String category = rs.getString("category");
                String itemData = rs.getString("item");
                double cost = rs.getDouble("cost");
                int dailyLimit = rs.getInt("daily_limit");
                String eventId = rs.getString("event_id");

                // Set required items (slots 0-9)
                for (int i = 0; i < 10; i++) {
                    String requiredItemData = rs.getString("required_item_" + (i + 1));
                    if (requiredItemData != null) {
                        ItemStack requiredItem = ItemStackSerializer.deserialize(requiredItemData);
                        inv.setItem(i, requiredItem);
                    } else {
                        inv.setItem(i, createPlaceholderItem(
                                Material.GRAY_STAINED_GLASS_PANE,
                                ChatColor.YELLOW + "Required Item " + (i + 1)
                        ));
                    }
                }

                // Set the result item (slot 13)
                ItemStack item = ItemStackSerializer.deserialize(itemData);
                inv.setItem(13, item);

                // Cost
                inv.setItem(11, createInfoItem(
                        "Cost",
                        String.valueOf(cost)
                ));
                TemporaryData.setCost(player.getUniqueId(), cost);

                // Event selection (12) - Gnob has event support for all offers
                // Check if event_id is already set in TemporaryData (from EventSelectMenu)
                Object tempEventObj = TemporaryData.getPlayerData(player.getUniqueId(), "gnob_event_id");
                String finalEventId = eventId;
                if (tempEventObj instanceof String) {
                    finalEventId = (String) tempEventObj;
                }
                inv.setItem(12, createEventInfoItem(finalEventId));
                if (tempEventObj == null) {
                    TemporaryData.setPlayerData(player.getUniqueId(), "gnob_event_id", eventId);
                }

                // Daily Limit
                inv.setItem(15, createInfoItem(
                        "Daily Limit",
                        String.valueOf(dailyLimit)
                ));
                TemporaryData.setPlayerData(player.getUniqueId(), "gnob_daily_limit", dailyLimit);

                // Save button
                inv.setItem(22, createMenuItem(
                        Material.EMERALD, ChatColor.GREEN + "Save"
                ));

                // Delete button
                inv.setItem(23, createMenuItem(
                        Material.BARRIER, ChatColor.RED + "Delete"
                ));

                // Back button
                inv.setItem(24, createMenuItem(
                        Material.ARROW, ChatColor.YELLOW + "Back"
                ));

                // Save category
                setCategory(player.getUniqueId(), category);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Error loading item data!");
            return;
        }

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

    private static ItemStack createEventInfoItem(String eventId) {
        ItemStack item = new ItemStack(Material.CLOCK);
        ItemMeta meta = item.getItemMeta();
        if(meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "Event");

            List<String> lore = new ArrayList<>();
            if (eventId == null || eventId.isEmpty()) {
                lore.add(ChatColor.GREEN + "Always Active");
                lore.add(ChatColor.GRAY + "This offer is always visible");
            } else {
                String eventName = EventIntegrationHelper.getEventName(eventId);
                boolean active = EventIntegrationHelper.isEventActive(eventId);

                lore.add(ChatColor.AQUA + eventName);
                if (active) {
                    lore.add(ChatColor.GREEN + "Currently ACTIVE");
                } else {
                    lore.add(ChatColor.RED + "Currently INACTIVE");
                }
                lore.add(ChatColor.GRAY + "Visible only when event is active");
            }
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click to change");

            meta.setLore(lore);
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

    // Category management methods
    public static void setCategory(UUID playerUUID, String category) {
        playerCategories.put(playerUUID, category);
    }

    public static String getCategory(UUID playerUUID) {
        return playerCategories.get(playerUUID);
    }

    public static void removeCategory(UUID playerUUID) {
        playerCategories.remove(playerUUID);
    }

    // GUI state management methods
    public static void saveGuiState(UUID playerUUID, ItemStack[] contents) {
        guiStates.put(playerUUID, contents);
    }

    public static void removeGuiState(UUID playerUUID) {
        guiStates.remove(playerUUID);
    }

    // Edit item ID management
    public static int getEditItemId(UUID playerUUID) {
        return editItemIds.getOrDefault(playerUUID, -1);
    }

    public static void removeEditItemId(UUID playerUUID) {
        editItemIds.remove(playerUUID);
    }
}
