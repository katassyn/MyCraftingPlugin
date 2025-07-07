package com.maks.mycraftingplugin2;

import com.maks.mycraftingplugin2.integration.PouchIntegrationHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Manages Zumpe shop transactions and daily limits.
 */
public class ZumpeTransactionManager {

    /**
     * Gets the number of transactions for a player for a specific item today.
     * @param playerUUID The player's UUID
     * @param itemId The item ID
     * @return The number of transactions today
     */
    public static int getTransactionCount(UUID playerUUID, int itemId) {
        // Use TransactionManager for better timezone handling
        return TransactionManager.getTransactionCount(playerUUID, itemId, "zumpe");
    }

    /**
     * Records a transaction.
     * @param playerUUID The player's UUID
     * @param itemId The item ID
     * @return True if the transaction was recorded successfully
     */
    public static boolean recordTransaction(UUID playerUUID, int itemId) {
        // Use TransactionManager for better timezone handling
        return TransactionManager.recordTransaction(playerUUID, itemId, "zumpe");
    }

    /**
     * Processes a purchase of an item.
     * @param player The player making the purchase
     * @param itemId The item ID being purchased
     * @return True if the purchase was successful
     */
    public static boolean processPurchase(Player player, int itemId) {
        try (Connection conn = Main.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM zumpe_items WHERE id = ?")) {

            ps.setInt(1, itemId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                double cost = rs.getDouble("cost");
                int dailyLimit = rs.getInt("daily_limit");
                String itemData = rs.getString("item");

                // Check daily limit with better messaging
                if (dailyLimit > 0) {
                    int currentCount = getTransactionCount(player.getUniqueId(), itemId);
                    if (currentCount >= dailyLimit) {
                        int remainingUses = 0;
                        player.sendMessage(ChatColor.RED + "You have reached your daily exchange limit for this item!");
                        player.sendMessage(ChatColor.YELLOW + "Remaining uses today: " + ChatColor.RED + 
                                         remainingUses + "/" + dailyLimit);

                        if (Main.getInstance().getConfig().getInt("debug", 0) == 1) {
                            Bukkit.getLogger().info("[ZumpeTransaction] Player " + player.getName() + 
                                                  " hit daily limit: " + currentCount + "/" + dailyLimit);
                        }
                        return false;
                    }
                }

                // Check required items using PouchIntegrationHelper (MODIFIED)
                Map<Integer, ItemStack> requiredItems = new HashMap<>();
                for (int i = 0; i < 10; i++) {
                    String requiredItemData = rs.getString("required_item_" + (i + 1));
                    if (requiredItemData != null) {
                        ItemStack requiredItem = ItemStackSerializer.deserialize(requiredItemData);
                        requiredItems.put(i, requiredItem);

                        // Use PouchIntegrationHelper instead of ItemComparisonHelper
                        int totalAmount = PouchIntegrationHelper.getTotalItemAmount(player, requiredItem);
                        int neededAmount = requiredItem.getAmount();

                        if (totalAmount < neededAmount) {
                            if (Main.getInstance().getConfig().getInt("debug", 0) == 1) {
                                Bukkit.getLogger().info("[ZumpeTransaction] Player missing: " + 
                                                      requiredItem.getType() + " x" + neededAmount + 
                                                      " (has: " + totalAmount + ")");
                            }
                            player.sendMessage(ChatColor.RED + "You don't have all the required items for this exchange!");
                            player.sendMessage(ChatColor.YELLOW + "Missing: " + 
                                             requiredItem.getItemMeta().getDisplayName() + 
                                             " x" + (neededAmount - totalAmount));
                            return false;
                        }
                    }
                }

                // Check if player has enough money
                if (cost > 0 && Main.getEconomy().getBalance(player) < cost) {
                    player.sendMessage(ChatColor.RED + "You don't have enough money for this exchange!");
                    player.sendMessage(ChatColor.YELLOW + "Required: " + ChatColor.GOLD + formatCost(cost));
                    return false;
                }

                // Deserialize result item
                ItemStack item = ItemStackSerializer.deserialize(itemData);
                if (item == null) {
                    player.sendMessage(ChatColor.RED + "Item could not be loaded!");
                    return false;
                }

                // Check if player has inventory space
                if (player.getInventory().firstEmpty() == -1) {
                    player.sendMessage(ChatColor.RED + "Your inventory is full!");
                    return false;
                }

                // Remove required items from player using PouchIntegrationHelper (MODIFIED)
                for (ItemStack requiredItem : requiredItems.values()) {
                    PouchIntegrationHelper.RemovalResult result = PouchIntegrationHelper.removeItems(player, requiredItem);
                    if (!result.success) {
                        if (result.errorMessage != null && result.errorMessage.startsWith("Not enough")) {
                            player.sendMessage(ChatColor.RED + "You don't have enough " + 
                                PouchIntegrationHelper.getItemName(requiredItem) + 
                                "! Required: " + result.required + ", Available: " + result.available);
                        } else {
                            player.sendMessage(ChatColor.RED + "Error removing items! Contact administrator.");
                        }
                        return false;
                    }

                    if (Main.getInstance().getConfig().getInt("debug", 0) == 1) {
                        Bukkit.getLogger().info("[ZumpeTransaction] Removed: " + 
                                              PouchIntegrationHelper.getItemName(requiredItem) + " x" + 
                                              PouchIntegrationHelper.getActualItemAmount(requiredItem));
                    }
                }

                // Charge player money if needed
                if (cost > 0) {
                    Main.getEconomy().withdrawPlayer(player, cost);
                }

                // Give result item
                player.getInventory().addItem(item);

                // Record transaction
                recordTransaction(player.getUniqueId(), itemId);

                // Show remaining uses if there's a limit
                if (dailyLimit > 0) {
                    int newCount = getTransactionCount(player.getUniqueId(), itemId);
                    int remainingUses = dailyLimit - newCount;
                    player.sendMessage(ChatColor.GREEN + "Exchange successful!");
                    player.sendMessage(ChatColor.GRAY + "Remaining uses today: " + 
                                     ChatColor.YELLOW + remainingUses + "/" + dailyLimit);
                } else {
                    player.sendMessage(ChatColor.GREEN + "Exchange successful!");
                }

                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Error processing purchase!");
        }
        return false;
    }

    /**
     * Helper method to format cost for display
     */
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

    /**
     * Deletes old transactions (maintains the database).
     * Can be run daily at server startup or via scheduler.
     */
    public static void cleanupOldTransactions() {
        // Use TransactionManager for better timezone handling
        TransactionManager.cleanupOldTransactions("zumpe");
    }

    // getCurrentDate method removed - now using ImprovedTransactionManager

    // Helper methods removed - now using ItemComparisonHelper
}
