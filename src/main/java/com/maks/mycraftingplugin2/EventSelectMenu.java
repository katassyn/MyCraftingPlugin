package com.maks.mycraftingplugin2;

import com.maks.mycraftingplugin2.integration.EventIntegrationHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Menu for selecting which event an offer should be linked to.
 * Similar to ConjurerRecipeSelectMenu but for events.
 */
public class EventSelectMenu {

    /**
     * Open the event selection menu for a player.
     * @param player The player viewing the menu
     */
    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "Select Event");

        // Fill with background glass
        fillWithGlass(inv);

        // Add "Always Active" option at slot 0
        inv.setItem(0, createAlwaysActiveItem());

        // Get all events from eventPlugin
        Map<String, EventIntegrationHelper.EventInfo> events = EventIntegrationHelper.getAllEvents();

        int slot = 1;
        for (EventIntegrationHelper.EventInfo eventInfo : events.values()) {
            if (slot >= 45) break; // Stop if we run out of slots

            ItemStack eventItem = createEventItem(eventInfo);
            inv.setItem(slot++, eventItem);
        }

        // Back button at slot 49
        inv.setItem(49, createMenuItem(Material.ARROW, ChatColor.YELLOW + "Back"));

        player.openInventory(inv);
    }

    /**
     * Create the "Always Active" item.
     */
    private static ItemStack createAlwaysActiveItem() {
        ItemStack item = new ItemStack(Material.LIME_DYE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Always Active");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "This offer will always be visible");
            lore.add(ChatColor.GRAY + "regardless of event status");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click to select");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Create an item representing an event.
     */
    private static ItemStack createEventItem(EventIntegrationHelper.EventInfo eventInfo) {
        // Choose material based on event status
        Material material;
        ChatColor statusColor;
        String statusText;

        if (eventInfo.isActive()) {
            material = Material.EMERALD;
            statusColor = ChatColor.GREEN;
            statusText = "ACTIVE";
        } else {
            material = Material.REDSTONE;
            statusColor = ChatColor.RED;
            statusText = "INACTIVE";
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + eventInfo.getName());

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Event ID: " + ChatColor.WHITE + eventInfo.getId());
            lore.add(ChatColor.GRAY + "Status: " + statusColor + statusText);
            lore.add("");
            lore.add(ChatColor.GRAY + "Offers linked to this event will");
            lore.add(ChatColor.GRAY + "only be visible when the event is active");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click to select");

            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Create a simple menu item.
     */
    private static ItemStack createMenuItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Fill empty slots with glass panes.
     */
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
     * Get the event ID from a clicked item.
     * Returns null for "Always Active" option.
     */
    public static String getEventIdFromItem(ItemStack item) {
        if (item == null || item.getType() == Material.BLACK_STAINED_GLASS_PANE) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || meta.getLore() == null) {
            return null;
        }

        // Check if it's the "Always Active" option
        if (item.getType() == Material.LIME_DYE) {
            return null; // null means always active
        }

        // Extract event ID from lore
        for (String line : meta.getLore()) {
            if (line.contains("Event ID:")) {
                String stripped = ChatColor.stripColor(line);
                String[] parts = stripped.split("Event ID: ");
                if (parts.length > 1) {
                    return parts[1].trim();
                }
            }
        }

        return null;
    }

    /**
     * Get the event name from a clicked item.
     */
    public static String getEventNameFromItem(ItemStack item) {
        if (item == null || item.getType() == Material.BLACK_STAINED_GLASS_PANE) {
            return null;
        }

        // Check if it's the "Always Active" option
        if (item.getType() == Material.LIME_DYE) {
            return "Always Active";
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return null;
        }

        return ChatColor.stripColor(meta.getDisplayName());
    }
}
