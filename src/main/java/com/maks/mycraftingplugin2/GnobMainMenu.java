package com.maks.mycraftingplugin2;

import com.maks.mycraftingplugin2.integration.EventIntegrationHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.sql.*;
import java.util.*;

/**
 * Main menu for Gonb - displays items directly.
 */
public class GnobMainMenu {
    private static final int ITEMS_PER_PAGE = 45; // Slots 0-44 for items

    public static void open(Player player) {
        open(player, 0);
    }

    public static void open(Player player, int page) {
        Inventory inv = Bukkit.createInventory(null, 54, "Gonb");

        // Fill with background glass
        fillWithGlass(inv);

        // Get items from database
        try (Connection conn = Main.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM gnob_items WHERE category = ? ORDER BY slot ASC")) {

            ps.setString(1, "gonb");
            ResultSet rs = ps.executeQuery();

            List<ItemStack> items = new ArrayList<>();

            while (rs.next()) {
                ItemStack resultItem = ItemStackSerializer.deserialize(rs.getString("item"));
                int itemId = rs.getInt("id");
                int dailyLimit = rs.getInt("daily_limit");
                double cost = rs.getDouble("cost");
                String eventId = rs.getString("event_id");

                // Filter by event status for players (not in edit mode)
                if (eventId != null && !eventId.isEmpty()) {
                    if (!EventIntegrationHelper.isEventActive(eventId)) {
                        continue; // Skip this item if its event is not active
                    }
                }

                // Store item ID in PersistentDataContainer
                ItemMeta meta = resultItem.getItemMeta();
                if (meta != null) {
                    NamespacedKey key = new NamespacedKey(Main.getInstance(), "item_id");
                    meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, itemId);

                    // Create lore with info
                    List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
                    lore.add(ChatColor.GRAY + "Cost: " + ChatColor.GOLD + formatCost(cost));

                    // Add daily limit info
                    if (dailyLimit > 0) {
                        int usedToday = GnobTransactionManager.getTransactionCount(player.getUniqueId(), itemId);
                        int remainingUses = dailyLimit - usedToday;
                        lore.add(ChatColor.GRAY + "Daily Limit: " + ChatColor.YELLOW + remainingUses + "/" + dailyLimit);

                        if (remainingUses == 0) {
                            lore.add(ChatColor.RED + "Limit reached for today!");
                        }
                    }

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

        // Save page data
        TemporaryData.setPage(player.getUniqueId(), "gonb", page);

        player.openInventory(inv);
    }

    public static void openEditor(Player player) {
        openEditor(player, 0);
    }

    public static void openEditor(Player player, int page) {
        Inventory inv = Bukkit.createInventory(null, 54, "Edit Gonb");

        // Fill with background glass
        fillWithGlass(inv);

        // Get items from database
        try (Connection conn = Main.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM gnob_items WHERE category = ? ORDER BY slot ASC")) {

            ps.setString(1, "gonb");
            ResultSet rs = ps.executeQuery();

            List<ItemStack> items = new ArrayList<>();

            while (rs.next()) {
                ItemStack resultItem = ItemStackSerializer.deserialize(rs.getString("item"));
                int itemId = rs.getInt("id");
                int dailyLimit = rs.getInt("daily_limit");
                double cost = rs.getDouble("cost");
                String eventId = rs.getString("event_id");

                // Show all items in edit mode, with event info in lore
                // Store item ID and other data
                ItemMeta meta = resultItem.getItemMeta();
                if (meta != null) {
                    NamespacedKey key = new NamespacedKey(Main.getInstance(), "item_id");
                    meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, itemId);

                    List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
                    lore.add(ChatColor.GRAY + "Cost: " + ChatColor.GOLD + formatCost(cost));
                    lore.add(ChatColor.GRAY + "Daily Limit: " + ChatColor.YELLOW + dailyLimit);
                    lore.add(ChatColor.GRAY + "ID: " + ChatColor.WHITE + itemId);

                    // Add event info in editor mode
                    if (eventId == null || eventId.isEmpty()) {
                        lore.add(ChatColor.AQUA + "Event: " + ChatColor.GREEN + "Always Active");
                    } else {
                        String eventName = EventIntegrationHelper.getEventName(eventId);
                        boolean active = EventIntegrationHelper.isEventActive(eventId);
                        lore.add(ChatColor.AQUA + "Event: " + ChatColor.WHITE + eventName +
                                (active ? ChatColor.GREEN + " (ON)" : ChatColor.RED + " (OFF)"));
                    }

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

        // Save page data
        TemporaryData.setPage(player.getUniqueId(), "gonb", page);

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
