package com.maks.mycraftingplugin2;

import com.maks.mycraftingplugin2.integration.PouchIntegrationHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Menu for crushing set items into crystals.
 */
public class SetCrushingMenu {

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Set Crushing");

        // Set up the inventory with black glass panes (like gem crushing)
        for (int i = 0; i < inv.getSize(); i++) {
            if (i < 18) {
                // First two rows are for set item insertion - leave empty
                continue;
            } else if (i == 22) {
                // Confirm button in the middle of the bottom row
                ItemStack confirmButton = new ItemStack(Material.EMERALD);
                ItemMeta meta = confirmButton.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.GREEN + "Confirm Crushing");
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GRAY + "Click to crush all set items");
                    lore.add(ChatColor.GRAY + "and receive crystals (20% of cost)");
                    lore.add(ChatColor.RED + "Warning: This action is irreversible!");
                    meta.setLore(lore);
                    confirmButton.setItemMeta(meta);
                }
                inv.setItem(i, confirmButton);
            } else {
                // Fill the rest with black glass panes
                ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
                ItemMeta meta = glass.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(" ");
                    glass.setItemMeta(meta);
                }
                inv.setItem(i, glass);
            }
        }

        player.openInventory(inv);
    }

    /**
     * Check if an item can be crushed (set item or imprinted set item)
     * @param item The item to check
     * @return True if the item can be crushed
     */
    public static boolean isCrushableSetItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) return false;
        
        List<String> lore = meta.getLore();
        
        // Check for "Set:" (original set items) or "Imprinted Set:" (imprinted items)
        for (String line : lore) {
            String stripped = ChatColor.stripColor(line);
            if (stripped.contains("Set:")) {
                return true; // Either "Set:" or "Imprinted Set:"
            }
        }
        
        return false;
    }

    /**
     * Get the tier of a set item for calculating returns
     * For imprinted items, check the tier in "Imprinted Set" lore line
     * For original set items, check the tier in display name
     * @param item The item to check
     * @return The tier (1-5) or 0 if no tier found
     */
    public static int getSetTier(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        
        // Check if this is an imprinted item
        if (isImprintedSetItem(item)) {
            // For imprinted items, look for tier in the "Imprinted Set" lore line
            List<String> lore = item.getItemMeta().getLore();
            if (lore != null) {
                for (String line : lore) {
                    String stripped = ChatColor.stripColor(line);
                    if (stripped.contains("Imprinted Set:")) {
                        // Extract tier from this line - format: "[ T1 ] Imprinted Set: ..."
                        if (stripped.contains("[ T1 ]")) return 1;
                        if (stripped.contains("[ T2 ]")) return 2;
                        if (stripped.contains("[ T3 ]")) return 3;
                        if (stripped.contains("[ T4 ]")) return 4;
                        if (stripped.contains("[ T5 ]")) return 5;
                    }
                }
            }
        } else {
            // For original set items, check display name
            String displayName = item.getItemMeta().getDisplayName();
            if (displayName != null) {
                String stripped = ChatColor.stripColor(displayName);
                if (stripped.contains("[ T1 ]")) return 1;
                if (stripped.contains("[ T2 ]")) return 2;
                if (stripped.contains("[ T3 ]")) return 3;
                if (stripped.contains("[ T4 ]")) return 4;
                if (stripped.contains("[ T5 ]")) return 5;
            }
        }
        
        return 0;
    }
    
    /**
     * Check if an item is an original set item (has "Set:" but not "Imprinted Set:")
     * @param item The item to check
     * @return True if it's an original set item
     */
    public static boolean isOriginalSetItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        
        List<String> lore = item.getItemMeta().getLore();
        if (lore == null) return false;
        
        for (String line : lore) {
            String stripped = ChatColor.stripColor(line);
            // Must contain "Set:" but NOT "Imprinted Set:"
            if (stripped.contains("Set:") && !stripped.contains("Imprinted Set:")) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if an item is an imprinted set item (has "Imprinted Set:")
     * @param item The item to check
     * @return True if it's an imprinted set item
     */
    public static boolean isImprintedSetItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        
        List<String> lore = item.getItemMeta().getLore();
        if (lore == null) return false;
        
        for (String line : lore) {
            String stripped = ChatColor.stripColor(line);
            if (stripped.contains("Imprinted Set:")) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Process all set items in the crushing GUI and give crystals (20% return)
     * @param player The player
     * @param inv The crushing inventory
     */
    public static void processSets(Player player, Inventory inv) {
        int totalEssenceCrystals = 0;
        int totalPowerCrystals = 0;
        int totalPrimordialCrystals = 0;
        List<Integer> slotsToEmpty = new ArrayList<>();

        // Calculate returns for all crushable items
        for (int i = 0; i < 18; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && isCrushableSetItem(item)) {
                int tier = getSetTier(item);
                int amount = item.getAmount();
                
                if (tier > 0) {
                    // Get costs from config for this tier
                    Main plugin = Main.getInstance();
                    String tierKey = "t" + tier;
                    
                    int essenceCost = plugin.getConfig().getInt("imprinting.costs." + tierKey + ".essence_crystal", 0);
                    int powerCost = plugin.getConfig().getInt("imprinting.costs." + tierKey + ".power_crystal", 0);
                    int primordialCost = plugin.getConfig().getInt("imprinting.costs." + tierKey + ".primordial_soul_crystal", 0);
                    
                    // Calculate 20% return for each item (always round up, minimum 1 if cost > 0)
                    int essenceReturn = essenceCost > 0 ? Math.max(1, (int) Math.ceil(essenceCost * 0.2 * amount)) : 0;
                    int powerReturn = powerCost > 0 ? Math.max(1, (int) Math.ceil(powerCost * 0.2 * amount)) : 0;
                    int primordialReturn = primordialCost > 0 ? Math.max(1, (int) Math.ceil(primordialCost * 0.2 * amount)) : 0;
                    
                    totalEssenceCrystals += essenceReturn;
                    totalPowerCrystals += powerReturn;
                    totalPrimordialCrystals += primordialReturn;
                    
                    slotsToEmpty.add(i);
                }
            }
        }

        if (!slotsToEmpty.isEmpty()) {
            List<String> rewardsGiven = new ArrayList<>();
            
            // Give essence crystals if any
            if (totalEssenceCrystals > 0) {
                if (ImprintingHelper.giveCrystalsToPlayer(player, "essence_crystal", totalEssenceCrystals)) {
                    rewardsGiven.add(totalEssenceCrystals + " Essence Crystal" + (totalEssenceCrystals > 1 ? "s" : ""));
                }
            }
            
            // Give power crystals if any
            if (totalPowerCrystals > 0) {
                if (ImprintingHelper.giveCrystalsToPlayer(player, "power_crystal", totalPowerCrystals)) {
                    rewardsGiven.add(totalPowerCrystals + " Power Crystal" + (totalPowerCrystals > 1 ? "s" : ""));
                }
            }
            
            // Give primordial crystals if any
            if (totalPrimordialCrystals > 0) {
                if (ImprintingHelper.giveCrystalsToPlayer(player, "primordial_soul_crystal", totalPrimordialCrystals)) {
                    rewardsGiven.add(totalPrimordialCrystals + " Primordial Soul Crystal" + (totalPrimordialCrystals > 1 ? "s" : ""));
                }
            }
            
            // Clear the crushed items
            for (int slot : slotsToEmpty) {
                inv.setItem(slot, null);
            }
            
            // Send success message
            if (!rewardsGiven.isEmpty()) {
                player.sendMessage(ChatColor.GREEN + "You crushed set items and received:");
                for (String reward : rewardsGiven) {
                    player.sendMessage(ChatColor.GRAY + "- " + reward);
                }
            } else {
                player.sendMessage(ChatColor.YELLOW + "Items crushed but no crystals were returned!");
            }
        } else {
            player.sendMessage(ChatColor.RED + "No valid set items to crush!");
        }
    }
    
    /**
     * Return all items from crushing GUI to player (try inventory first, then pouch)
     * @param player The player
     * @param inv The crushing inventory
     */
    public static void returnItemsToPlayer(Player player, Inventory inv) {
        List<ItemStack> itemsToReturn = new ArrayList<>();
        for (int i = 0; i < 18; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                itemsToReturn.add(item.clone());
                inv.setItem(i, null);
            }
        }

        // Try to return items to inventory first, then to pouch if inventory is full
        for (ItemStack item : itemsToReturn) {
            // Try inventory first
            int remainingItems = player.getInventory().addItem(item).values().stream()
                .mapToInt(ItemStack::getAmount)
                .sum();
            
            if (remainingItems > 0) {
                // If inventory is full, try to add to pouch
                ItemStack remainingItem = item.clone();
                remainingItem.setAmount(remainingItems);
                
                // Use PouchIntegrationHelper to add to pouch
                boolean addedToPouch = PouchIntegrationHelper.addItemToPouch(player, remainingItem);
                
                if (!addedToPouch) {
                    // If pouch is also full, drop on ground
                    player.getWorld().dropItem(player.getLocation(), remainingItem);
                    player.sendMessage(ChatColor.YELLOW + "Some items were dropped on the ground (inventory and pouch full)!");
                }
            }
        }
    }
}