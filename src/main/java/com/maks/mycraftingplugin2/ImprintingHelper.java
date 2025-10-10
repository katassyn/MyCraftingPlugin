package com.maks.mycraftingplugin2;

import com.maks.mycraftingplugin2.integration.PouchIntegrationHelper;
import com.maks.mycraftingplugin2.integration.PouchItemMappings;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ImprintingHelper {
    
    /**
     * Check if player has enough crystals of specific type
     */
    public static boolean hasEnoughCrystals(Player player, String crystalType, int amount) {
        if (amount <= 0) return true;
        
        // Create the crystal item to check
        ItemStack crystalItem = createCrystalItem(crystalType);
        if (crystalItem == null) return false;
        
        // Check total amount (inventory + pouch)
        int totalAmount = PouchIntegrationHelper.getTotalItemAmount(player, crystalItem);
        return totalAmount >= amount;
    }
    
    /**
     * Remove crystals from player inventory/pouch
     */
    public static boolean removeCrystals(Player player, String crystalType, int amount) {
        if (amount <= 0) return true;
        
        // Create the crystal item to remove
        ItemStack crystalItem = createCrystalItem(crystalType);
        if (crystalItem == null) return false;
        
        // Set the amount we want to remove
        crystalItem.setAmount(amount);
        
        // Try to remove the items
        PouchIntegrationHelper.RemovalResult result = PouchIntegrationHelper.removeItems(player, crystalItem);
        return result.success;
    }
    
    /**
     * Create crystal ItemStack based on type
     */
    private static ItemStack createCrystalItem(String crystalType) {
        Material material;
        String displayName;
        String loreText;
        
        switch (crystalType.toLowerCase()) {
            case "essence_crystal":
                material = Material.QUARTZ;
                displayName = "&9Essence Crystal";
                loreText = "&7§oBasic crafting material";
                break;
            case "power_crystal":
                material = Material.RAW_GOLD;
                displayName = "&5Power Crystal";
                loreText = "&a§oRare crafting material";
                break;
            case "primordial_soul_crystal":
                material = Material.ECHO_SHARD;
                displayName = "&6Primordial Soul Crystal";
                loreText = "&c§oLegendary crafting material";
                break;
            default:
                return null;
        }
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName.replace("&", "§"));
            meta.setLore(Arrays.asList(loreText));
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Give crystals to player (inventory first, then pouch if no space)
     */
    public static boolean giveCrystalsToPlayer(Player player, String crystalType, int amount) {
        if (amount <= 0) return true;
        
        // Create the crystal item to give
        ItemStack crystalItem = createCrystalItem(crystalType);
        if (crystalItem == null) return false;
        
        crystalItem.setAmount(amount);
        
        // Try to add to IngredientPouch FIRST (for crushing rewards)
        boolean addedToPouch = PouchIntegrationHelper.addItemToPouch(player, crystalItem);
        
        if (!addedToPouch) {
            // If pouch is full or unavailable, try inventory
            int remainingItems = player.getInventory().addItem(crystalItem).values().stream()
                .mapToInt(ItemStack::getAmount)
                .sum();
            
            if (remainingItems > 0) {
                // If inventory is also full, drop on ground
                ItemStack remainingCrystals = createCrystalItem(crystalType);
                remainingCrystals.setAmount(remainingItems);
                player.getWorld().dropItem(player.getLocation(), remainingCrystals);
                player.sendMessage(ChatColor.YELLOW + "Some crystals were dropped on the ground (inventory and pouch full)!");
            }
        }
        
        return true;
    }
    
    /**
     * Get crystal display name for messages
     */
    public static String getCrystalDisplayName(String crystalType) {
        switch (crystalType.toLowerCase()) {
            case "essence_crystal":
                return "Essence Crystal";
            case "power_crystal":
                return "Power Crystal";
            case "primordial_soul_crystal":
                return "Primordial Soul Crystal";
            default:
                return crystalType;
        }
    }
}