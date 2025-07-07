package com.maks.mycraftingplugin2;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Transaction manager with better daily limit handling
 * and timezone support
 */
public class TransactionManager {

    private static final int debuggingFlag = 1; // Set to 0 to disable debug
    private static final ZoneId SERVER_TIMEZONE = ZoneId.systemDefault(); // Or set specific like ZoneId.of("Europe/Warsaw")

    /**
     * Gets transaction count for today with timezone awareness
     */
    public static int getTransactionCount(UUID playerUUID, int itemId, String tableName) {
        String todayDate = getCurrentDate();

        if (debuggingFlag == 1) {
            Bukkit.getLogger().info("[TransactionManager] Checking transactions for player: " + playerUUID + 
                                  ", itemId: " + itemId + ", date: " + todayDate);
        }

        try (Connection conn = Main.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM " + tableName + "_transactions " +
                     "WHERE player_uuid = ? AND item_id = ? AND transaction_date = ?")) {

            ps.setString(1, playerUUID.toString());
            ps.setInt(2, itemId);
            ps.setString(3, todayDate);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                if (debuggingFlag == 1) {
                    Bukkit.getLogger().info("[TransactionManager] Found " + count + " transactions today");
                }
                return count;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().severe("[TransactionManager] Error getting transaction count: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Records a transaction with proper timestamp
     */
    public static boolean recordTransaction(UUID playerUUID, int itemId, String tableName) {
        String todayDate = getCurrentDate();
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());

        if (debuggingFlag == 1) {
            Bukkit.getLogger().info("[TransactionManager] Recording transaction - Player: " + playerUUID + 
                                  ", ItemId: " + itemId + ", Date: " + todayDate + ", Time: " + currentTime);
        }

        try (Connection conn = Main.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO " + tableName + "_transactions " +
                     "(player_uuid, item_id, transaction_date, transaction_time) VALUES (?, ?, ?, ?)")) {

            ps.setString(1, playerUUID.toString());
            ps.setInt(2, itemId);
            ps.setString(3, todayDate);
            ps.setTimestamp(4, currentTime);

            int affected = ps.executeUpdate();

            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[TransactionManager] Transaction recorded: " + (affected > 0));
            }

            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().severe("[TransactionManager] Error recording transaction: " + e.getMessage());
        }
        return false;
    }

    /**
     * Enhanced cleanup that shows what's being deleted
     */
    public static void cleanupOldTransactions(String tableName) {
        String todayDate = getCurrentDate();

        if (debuggingFlag == 1) {
            Bukkit.getLogger().info("[TransactionManager] Starting cleanup for " + tableName + 
                                  "_transactions. Keeping only: " + todayDate);
        }

        try (Connection conn = Main.getConnection()) {
            // First, let's see what we're about to delete
            if (debuggingFlag == 1) {
                try (PreparedStatement countPs = conn.prepareStatement(
                        "SELECT transaction_date, COUNT(*) as cnt FROM " + tableName + 
                        "_transactions WHERE transaction_date != ? GROUP BY transaction_date")) {

                    countPs.setString(1, todayDate);
                    ResultSet rs = countPs.executeQuery();

                    while (rs.next()) {
                        Bukkit.getLogger().info("[TransactionManager] Will delete " + rs.getInt("cnt") + 
                                              " transactions from " + rs.getString("transaction_date"));
                    }
                }
            }

            // Now delete old transactions
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM " + tableName + "_transactions WHERE transaction_date != ?")) {

                ps.setString(1, todayDate);
                int deleted = ps.executeUpdate();

                Bukkit.getLogger().info("[TransactionManager] Cleaned up " + deleted + 
                                      " old transactions from " + tableName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().severe("[TransactionManager] Error cleaning up transactions: " + e.getMessage());
        }
    }

    /**
     * Check if player can make purchase (with detailed debug info)
     */
    public static boolean canMakePurchase(Player player, int itemId, int dailyLimit, String shopType) {
        if (dailyLimit <= 0) {
            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[TransactionManager] No daily limit set for item " + itemId);
            }
            return true;
        }

        String tableName = shopType.toLowerCase();
        int currentCount = getTransactionCount(player.getUniqueId(), itemId, tableName);

        boolean canPurchase = currentCount < dailyLimit;

        if (debuggingFlag == 1) {
            Bukkit.getLogger().info("[TransactionManager] Daily limit check - Player: " + player.getName() + 
                                  ", Current: " + currentCount + "/" + dailyLimit + 
                                  ", Can purchase: " + canPurchase);
        }

        if (!canPurchase) {
            player.sendMessage(ChatColor.RED + "You have reached your daily limit (" + 
                             currentCount + "/" + dailyLimit + ") for this item!");
        }

        return canPurchase;
    }

    /**
     * Gets current date with timezone consideration
     */
    private static String getCurrentDate() {
        LocalDate today = LocalDate.now(SERVER_TIMEZONE);
        String dateStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE);

        if (debuggingFlag == 1) {
            LocalDateTime now = LocalDateTime.now(SERVER_TIMEZONE);
            Bukkit.getLogger().info("[TransactionManager] Current server time: " + now + 
                                  " (Timezone: " + SERVER_TIMEZONE + ")");
        }

        return dateStr;
    }

    /**
     * Test method to verify daily limit is working
     */
    public static void testDailyLimit(Player player, int itemId, String shopType) {
        if (debuggingFlag != 1) return;

        Bukkit.getLogger().info("=== DAILY LIMIT TEST START ===");
        Bukkit.getLogger().info("Player: " + player.getName());
        Bukkit.getLogger().info("ItemId: " + itemId);
        Bukkit.getLogger().info("Shop: " + shopType);

        String tableName = shopType.toLowerCase();

        // Check current count
        int count = getTransactionCount(player.getUniqueId(), itemId, tableName);
        Bukkit.getLogger().info("Current transaction count: " + count);

        // Check date
        Bukkit.getLogger().info("Current date: " + getCurrentDate());

        // Check database directly
        try (Connection conn = Main.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM " + tableName + "_transactions WHERE player_uuid = ? AND item_id = ?")) {

            ps.setString(1, player.getUniqueId().toString());
            ps.setInt(2, itemId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Bukkit.getLogger().info("Transaction found - Date: " + rs.getString("transaction_date") + 
                                      ", Time: " + rs.getTimestamp("transaction_time"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Bukkit.getLogger().info("=== DAILY LIMIT TEST END ===");
    }

    /**
     * Gets remaining uses for today
     */
    public static int getRemainingUses(UUID playerUUID, int itemId, int dailyLimit, String tableName) {
        if (dailyLimit <= 0) return -1; // No limit

        int used = getTransactionCount(playerUUID, itemId, tableName);
        return Math.max(0, dailyLimit - used);
    }

    /**
     * Gets appropriate color for limit display based on remaining uses
     */
    public static ChatColor getLimitColor(int remaining, int total) {
        if (remaining == 0) return ChatColor.RED;
        if (remaining <= total / 3) return ChatColor.GOLD; // Less than 1/3
        return ChatColor.GREEN; // More than 1/3
    }
}