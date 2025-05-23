package com.maks.mycraftingplugin2;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Helper class for comparing items in a more flexible way
 */
public class ItemComparisonHelper {
    
    private static final int debuggingFlag = 1;
    
    /**
     * Improved item comparison that's less strict about metadata
     */
    public static boolean areItemsSimilar(ItemStack item1, ItemStack item2) {
        if (item1 == null || item2 == null) {
            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[ItemComparison] One of the items is null");
            }
            return false;
        }
        
        // Check material type
        if (item1.getType() != item2.getType()) {
            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[ItemComparison] Different types: " + item1.getType() + " vs " + item2.getType());
            }
            return false;
        }
        
        // Check durability
        if (item1.getDurability() != item2.getDurability()) {
            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[ItemComparison] Different durability: " + item1.getDurability() + " vs " + item2.getDurability());
            }
            return false;
        }
        
        ItemMeta meta1 = item1.getItemMeta();
        ItemMeta meta2 = item2.getItemMeta();
        
        // If both have no metadata, they're equal
        if (meta1 == null && meta2 == null) {
            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[ItemComparison] Both items have no meta - MATCH");
            }
            return true;
        }
        
        // If one has metadata and the other doesn't
        if ((meta1 == null) != (meta2 == null)) {
            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[ItemComparison] One has meta, other doesn't - NO MATCH");
            }
            return false;
        }
        
        // Compare display names
        boolean sameName = Objects.equals(meta1.getDisplayName(), meta2.getDisplayName());
        if (!sameName) {
            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[ItemComparison] Different names: '" + meta1.getDisplayName() + "' vs '" + meta2.getDisplayName() + "'");
            }
            return false;
        }
        
        // Compare enchantments
        boolean sameEnchants = meta1.getEnchants().equals(meta2.getEnchants());
        if (!sameEnchants) {
            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[ItemComparison] Different enchantments");
            }
            return false;
        }
        
        // For lore, we'll be more flexible - check if key parts match
        // This helps when items have dynamic lore (like "Daily Limit: 2/3")
        if (meta1.hasLore() && meta2.hasLore()) {
            // For shop items, we might want to ignore certain lore lines
            // that contain dynamic data like costs or limits
            boolean loreMatches = compareLoreFlexible(meta1.getLore(), meta2.getLore());
            if (!loreMatches) {
                if (debuggingFlag == 1) {
                    Bukkit.getLogger().info("[ItemComparison] Lore doesn't match");
                }
                return false;
            }
        } else if (meta1.hasLore() != meta2.hasLore()) {
            // One has lore, other doesn't
            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[ItemComparison] One has lore, other doesn't");
            }
            return false;
        }
        
        if (debuggingFlag == 1) {
            Bukkit.getLogger().info("[ItemComparison] Items MATCH!");
        }
        
        return true;
    }
    
    /**
     * Flexible lore comparison that ignores dynamic lines
     */
    private static boolean compareLoreFlexible(java.util.List<String> lore1, java.util.List<String> lore2) {
        // If sizes are very different, they're probably not the same item
        if (Math.abs(lore1.size() - lore2.size()) > 3) {
            return false;
        }
        
        // Compare non-dynamic lines
        for (int i = 0; i < Math.min(lore1.size(), lore2.size()); i++) {
            String line1 = lore1.get(i);
            String line2 = lore2.get(i);
            
            // Skip lines that contain dynamic data
            if (isDynamicLoreLine(line1) || isDynamicLoreLine(line2)) {
                continue;
            }
            
            // Compare static lines
            if (!line1.equals(line2)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Check if a lore line contains dynamic data that changes
     */
    private static boolean isDynamicLoreLine(String line) {
        String stripped = org.bukkit.ChatColor.stripColor(line).toLowerCase();
        return stripped.contains("cost:") || 
               stripped.contains("daily limit:") || 
               stripped.contains("recipe id:") ||
               stripped.contains("id:") ||
               stripped.contains("/");  // For ratios like 2/3
    }
    
    /**
     * Get total amount of similar items in player's inventory
     */
    public static int getTotalSimilarItemAmount(Player player, ItemStack targetItem) {
        int total = 0;
        
        if (debuggingFlag == 1) {
            Bukkit.getLogger().info("[ItemComparison] Checking inventory for: " + targetItem.getType() + 
                                  " x" + targetItem.getAmount());
        }
        
        for (ItemStack invItem : player.getInventory().getContents()) {
            if (invItem != null && areItemsSimilar(invItem, targetItem)) {
                total += invItem.getAmount();
                if (debuggingFlag == 1) {
                    Bukkit.getLogger().info("[ItemComparison] Found matching stack with " + invItem.getAmount() + " items");
                }
            }
        }
        
        if (debuggingFlag == 1) {
            Bukkit.getLogger().info("[ItemComparison] Total found: " + total);
        }
        
        return total;
    }
    
    /**
     * Remove similar items from player's inventory
     */
    public static void removeSimilarItems(Player player, ItemStack targetItem) {
        int amountToRemove = targetItem.getAmount();
        ItemStack[] contents = player.getInventory().getContents();
        
        if (debuggingFlag == 1) {
            Bukkit.getLogger().info("[ItemComparison] Removing " + amountToRemove + " of " + targetItem.getType());
        }
        
        for (int i = 0; i < contents.length; i++) {
            ItemStack invItem = contents[i];
            if (invItem != null && areItemsSimilar(invItem, targetItem)) {
                if (invItem.getAmount() > amountToRemove) {
                    invItem.setAmount(invItem.getAmount() - amountToRemove);
                    if (debuggingFlag == 1) {
                        Bukkit.getLogger().info("[ItemComparison] Reduced stack at slot " + i + " by " + amountToRemove);
                    }
                    break;
                } else {
                    amountToRemove -= invItem.getAmount();
                    contents[i] = null;
                    if (debuggingFlag == 1) {
                        Bukkit.getLogger().info("[ItemComparison] Removed entire stack at slot " + i);
                    }
                    if (amountToRemove == 0) {
                        break;
                    }
                }
            }
        }
        
        player.getInventory().setContents(contents);
    }
}