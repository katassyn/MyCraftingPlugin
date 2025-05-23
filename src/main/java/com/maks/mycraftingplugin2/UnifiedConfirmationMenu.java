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

/**
 * Unified confirmation menu that can be used for all shop systems
 * to maintain consistency across Crafting, Emilia, and Zumpe shops.
 */
public class UnifiedConfirmationMenu {

    private static final int debuggingFlag = 1; // Set to 0 to disable debug messages

    public enum ShopType {
        CRAFTING("recipes", "recipe_id", "Crafting Scheme"),
        EMILIA("emilia_items", "emilia_item_id", "Emilia Exchange"),
        ZUMPE("zumpe_items", "zumpe_item_id", "Zumpe Exchange");

        private final String tableName;
        private final String itemIdKey;
        private final String menuTitle;

        ShopType(String tableName, String itemIdKey, String menuTitle) {
            this.tableName = tableName;
            this.itemIdKey = itemIdKey;
            this.menuTitle = menuTitle;
        }
    }

    /**
     * Opens unified confirmation menu for any shop type
     */
    public static void open(Player player, int itemId, ShopType shopType) {
        if (debuggingFlag == 1) {
            Bukkit.getLogger().info("[UnifiedConfirmation] Opening " + shopType.menuTitle + " for itemId: " + itemId);
        }

        Inventory inv = Bukkit.createInventory(null, 45, shopType.menuTitle);

        // Fill with consistent glass type (matching old system)
        fillWithGlass(inv);

        // Set required items
        setRequiredItems(inv, itemId, shopType);

        // Set result item (center slot 22)
        ItemStack resultItem = getResultItem(itemId, shopType);
        if (resultItem != null) {
            inv.setItem(22, resultItem);
        }

        // Set action button (slot 40) - unified naming
        ItemStack actionButton = createActionButton(itemId, shopType);
        if (actionButton != null) {
            inv.setItem(40, actionButton);
        }

        // Back button (slot 36)
        inv.setItem(36, createMenuItem(Material.ARROW, ChatColor.YELLOW + "Back"));

        player.openInventory(inv);
    }

    private static void setRequiredItems(Inventory inv, int itemId, ShopType shopType) {
        String query = "";

        switch (shopType) {
            case CRAFTING:
                query = "SELECT * FROM recipes WHERE id = ?";
                break;
            case EMILIA:
                query = "SELECT * FROM emilia_items WHERE id = ?";
                break;
            case ZUMPE:
                query = "SELECT * FROM zumpe_items WHERE id = ?";
                break;
        }

        try (Connection conn = Main.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Set required items in slots 10-19 (consistent with CraftingScheme)
                    for (int i = 0; i < 10; i++) {
                        String itemData = rs.getString("required_item_" + (i + 1));
                        if (itemData != null) {
                            ItemStack requiredItem = ItemStackSerializer.deserialize(itemData);
                            inv.setItem(10 + i, requiredItem);

                            if (debuggingFlag == 1) {
                                Bukkit.getLogger().info("[UnifiedConfirmation] Set required item " + (i+1) + " in slot " + (10+i));
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().severe("[UnifiedConfirmation] Error loading required items for " + shopType.name());
        }
    }

    private static ItemStack getResultItem(int itemId, ShopType shopType) {
        String query = "";
        String itemColumn = "";

        switch (shopType) {
            case CRAFTING:
                query = "SELECT result_item FROM recipes WHERE id = ?";
                itemColumn = "result_item";
                break;
            case EMILIA:
            case ZUMPE:
                query = "SELECT item FROM " + shopType.tableName + " WHERE id = ?";
                itemColumn = "item";
                break;
        }

        try (Connection conn = Main.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String itemData = rs.getString(itemColumn);
                    ItemStack resultItem = ItemStackSerializer.deserialize(itemData);

                    // Store item ID in PersistentDataContainer
                    ItemMeta meta = resultItem.getItemMeta();
                    if (meta != null) {
                        NamespacedKey key = new NamespacedKey(Main.getInstance(), shopType.itemIdKey);
                        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, itemId);
                        resultItem.setItemMeta(meta);
                    }

                    if (debuggingFlag == 1) {
                        Bukkit.getLogger().info("[UnifiedConfirmation] Set result item for " + shopType.name());
                    }

                    return resultItem;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ItemStack createActionButton(int itemId, ShopType shopType) {
        String query = "";

        switch (shopType) {
            case CRAFTING:
                query = "SELECT cost, success_chance FROM recipes WHERE id = ?";
                break;
            case EMILIA:
            case ZUMPE:
                query = "SELECT cost, daily_limit FROM " + shopType.tableName + " WHERE id = ?";
                break;
        }

        try (Connection conn = Main.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double cost = rs.getDouble("cost");

                    ItemStack actionButton = new ItemStack(Material.EMERALD);
                    ItemMeta meta = actionButton.getItemMeta();
                    if (meta != null) {
                        // Unified button name
                        String buttonName = (shopType == ShopType.CRAFTING) ? "Craft" : "Exchange";
                        meta.setDisplayName(ChatColor.GREEN + buttonName);

                        List<String> lore = new ArrayList<>();

                        // Cost (all systems have this)
                        if (cost > 0) {
                            lore.add(ChatColor.YELLOW + "Cost: " + ChatColor.GOLD + formatCost(cost));
                        }

                        // System-specific info
                        if (shopType == ShopType.CRAFTING) {
                            double successChance = rs.getDouble("success_chance");
                            lore.add(ChatColor.YELLOW + "Success Chance: " + ChatColor.GOLD + successChance + "%");
                        } else {
                            int dailyLimit = rs.getInt("daily_limit");
                            if (dailyLimit > 0) {
                                // Pobierz aktualny stan użycia
                                int usedToday = 0;
                                Player viewingPlayer = null;

                                // Znajdź gracza który ogląda to menu
                                for (Player p : Bukkit.getOnlinePlayers()) {
                                    if (p.getOpenInventory() != null && 
                                        p.getOpenInventory().getTitle().equals(shopType.menuTitle)) {
                                        viewingPlayer = p;
                                        break;
                                    }
                                }

                                if (viewingPlayer != null) {
                                    if (shopType == ShopType.EMILIA) {
                                        usedToday = EmiliaTransactionManager.getTransactionCount(
                                                viewingPlayer.getUniqueId(), itemId);
                                    } else {
                                        usedToday = ZumpeTransactionManager.getTransactionCount(
                                                viewingPlayer.getUniqueId(), itemId);
                                    }
                                }

                                int remainingUses = dailyLimit - usedToday;
                                lore.add(ChatColor.YELLOW + "Daily Limit: " + ChatColor.GOLD + 
                                        remainingUses + "/" + dailyLimit);
                            }
                        }

                        meta.setLore(lore);
                        actionButton.setItemMeta(meta);
                    }

                    return actionButton;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
        // Use consistent glass type across all menus
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

    /**
     * Refreshes the confirmation menu after a successful transaction
     * Updates button state and daily limit display
     */
    public static void refresh(Player player, int itemId, ShopType shopType) {
        if (debuggingFlag == 1) {
            Bukkit.getLogger().info("[UnifiedConfirmation] Refreshing menu for " + shopType.name() + ", itemId: " + itemId);
        }

        Inventory currentInv = player.getOpenInventory().getTopInventory();
        if (currentInv == null || !player.getOpenInventory().getTitle().equals(shopType.menuTitle)) {
            return; // Not in the right menu
        }

        // Refresh the action button
        ItemStack newButton = createActionButton(itemId, shopType);
        if (newButton != null) {
            // Check if we need to disable the button
            if (shopType != ShopType.CRAFTING) {
                // For Emilia/Zumpe check daily limit
                if (!canPerformExchange(player, itemId, shopType)) {
                    ItemMeta meta = newButton.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(ChatColor.RED + "Exchange (Limit Reached)");
                        newButton.setItemMeta(meta);
                    }
                    newButton.setType(Material.BARRIER);
                }
            }

            currentInv.setItem(40, newButton);
        }

        // Update player's view
        player.updateInventory();
    }

    /**
     * Check if player can still perform exchange (for daily limits)
     */
    private static boolean canPerformExchange(Player player, int itemId, ShopType shopType) {
        if (shopType == ShopType.CRAFTING) {
            return true; // No daily limits for crafting
        }

        try (Connection conn = Main.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT daily_limit FROM " + shopType.tableName + " WHERE id = ?")) {

            ps.setInt(1, itemId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int dailyLimit = rs.getInt("daily_limit");
                if (dailyLimit <= 0) {
                    return true; // No limit
                }

                int usedToday = 0;
                if (shopType == ShopType.EMILIA) {
                    usedToday = EmiliaTransactionManager.getTransactionCount(player.getUniqueId(), itemId);
                } else if (shopType == ShopType.ZUMPE) {
                    usedToday = ZumpeTransactionManager.getTransactionCount(player.getUniqueId(), itemId);
                }

                boolean canExchange = usedToday < dailyLimit;

                if (debuggingFlag == 1) {
                    Bukkit.getLogger().info("[UnifiedConfirmation] Daily limit check: " + 
                                          usedToday + "/" + dailyLimit + " = " + canExchange);
                }

                return canExchange;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }
}
