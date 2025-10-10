package com.maks.mycraftingplugin2;

import com.maks.mycraftingplugin2.integration.PouchIntegrationHelper;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ImprintingListener implements Listener {
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        
        if (!(inventory.getHolder() instanceof ImprintingGUI)) return;
        
        ImprintingGUI gui = (ImprintingGUI) inventory.getHolder();
        int slot = event.getRawSlot();
        
        // Cancel clicks on glass panes and other non-interactive slots
        if (slot < 27 && slot != 10 && slot != 12 && slot != 16) {
            event.setCancelled(true);
            return;
        }
        
        // Handle accept button click
        if (slot == 16) {
            event.setCancelled(true);
            handleAcceptClick(gui, player);
            return;
        }
        
        // Handle item placement in target or set slots
        if (slot == 10 || slot == 12) {
            ItemStack currentItem = inventory.getItem(slot);
            ItemStack cursorItem = event.getCursor();
            
            // If slot contains glass pane, handle special interactions
            if (currentItem != null && isGlassPane(currentItem)) {
                event.setCancelled(true); // Cancel glass pane interactions
                
                // If player has item on cursor, replace glass pane with item
                if (cursorItem != null && cursorItem.getType() != Material.AIR) {
                    inventory.setItem(slot, cursorItem.clone());
                    event.setCursor(null);
                    
                    // Update the accept button after a short delay
                    Main.getInstance().getServer().getScheduler().runTaskLater(Main.getInstance(), () -> {
                        gui.updateAcceptButton();
                    }, 1L);
                }
                // If clicking glass pane with empty cursor, it just disappears (do nothing)
                return;
            }
            
            // If slot contains real item, allow normal interactions
            if (currentItem != null && !isGlassPane(currentItem)) {
                // Allow normal item interactions but update accept button afterwards
                Main.getInstance().getServer().getScheduler().runTaskLater(Main.getInstance(), () -> {
                    // Check if item was removed and restore glass pane if needed
                    ItemStack newItem = inventory.getItem(slot);
                    if (newItem == null || newItem.getType() == Material.AIR) {
                        if (slot == 10) {
                            inventory.setItem(slot, createGlassPane(Material.ORANGE_STAINED_GLASS_PANE, 
                                ChatColor.GOLD + "Target Item Slot", ChatColor.GRAY + "Place non-set item here"));
                        } else if (slot == 12) {
                            inventory.setItem(slot, createGlassPane(Material.BLUE_STAINED_GLASS_PANE,
                                ChatColor.AQUA + "Set Item Slot", ChatColor.GRAY + "Place set item here (T1-T5)"));
                        }
                    }
                    gui.updateAcceptButton();
                }, 1L);
                return;
            }
            
            // If slot is empty and player has item, place it
            if ((currentItem == null || currentItem.getType() == Material.AIR) && 
                cursorItem != null && cursorItem.getType() != Material.AIR) {
                // Allow normal placement
                Main.getInstance().getServer().getScheduler().runTaskLater(Main.getInstance(), () -> {
                    gui.updateAcceptButton();
                }, 1L);
            }
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof ImprintingGUI)) return;
        
        Player player = (Player) event.getPlayer();
        Inventory inventory = event.getInventory();
        
        // Return items to player
        ItemStack targetItem = inventory.getItem(10);
        ItemStack setItem = inventory.getItem(12);
        
        if (targetItem != null && !isGlassPane(targetItem)) {
            player.getInventory().addItem(targetItem);
        }
        
        if (setItem != null && !isGlassPane(setItem)) {
            player.getInventory().addItem(setItem);
        }
    }
    
    private void handleAcceptClick(ImprintingGUI gui, Player player) {
        Inventory inventory = gui.getInventory();
        ItemStack targetItem = inventory.getItem(10);
        ItemStack setItem = inventory.getItem(12);
        
        if (targetItem == null || setItem == null || 
            isGlassPane(targetItem) || isGlassPane(setItem)) {
            player.sendMessage(ChatColor.RED + "Both items must be placed first!");
            return;
        }
        
        String setTier = getSetTier(setItem);
        if (setTier == null) {
            player.sendMessage(ChatColor.RED + "Set item tier could not be determined!");
            return;
        }
        
        if (hasImprint(targetItem)) {
            player.sendMessage(ChatColor.RED + "Target item already has an imprint!");
            return;
        }
        
        if (isSetItem(targetItem)) {
            player.sendMessage(ChatColor.RED + "Cannot imprint onto set items!");
            return;
        }
        
        if (!areCompatibleTypes(targetItem, setItem)) {
            player.sendMessage(ChatColor.RED + "Items must be the same type!");
            return;
        }
        
        // Check and consume resources
        if (!checkAndConsumeResources(player, setTier)) {
            return; // Error message sent in method
        }
        
        // Perform the imprinting
        ItemStack imprintedItem = performImprinting(targetItem, setItem);
        if (imprintedItem != null) {
            // Clear the slots
            inventory.setItem(10, createGlassPane(Material.ORANGE_STAINED_GLASS_PANE, 
                ChatColor.GOLD + "Target Item Slot", ChatColor.GRAY + "Place non-set item here"));
            inventory.setItem(12, createGlassPane(Material.BLUE_STAINED_GLASS_PANE,
                ChatColor.AQUA + "Set Item Slot", ChatColor.GRAY + "Place set item here (T1-T5)"));
            
            // Give the imprinted item to player
            player.getInventory().addItem(imprintedItem);
            player.sendMessage(ChatColor.GREEN + "Successfully imprinted set bonus onto item!");
            
            // Update accept button
            gui.updateAcceptButton();
        }
    }
    
    private boolean checkAndConsumeResources(Player player, String tier) {
        Main plugin = Main.getInstance();
        
        int essenceCrystal = plugin.getConfig().getInt("imprinting.costs." + tier + ".essence_crystal", 0);
        int powerCrystal = plugin.getConfig().getInt("imprinting.costs." + tier + ".power_crystal", 0);
        int primordialCrystal = plugin.getConfig().getInt("imprinting.costs." + tier + ".primordial_soul_crystal", 0);
        double vaultMoney = plugin.getConfig().getDouble("imprinting.costs." + tier + ".vault_money", 0);
        
        // Check if player has enough money
        if (vaultMoney > 0 && Main.getEconomy() != null) {
            if (Main.getEconomy().getBalance(player) < vaultMoney) {
                player.sendMessage(ChatColor.RED + "Insufficient funds! Need $" + vaultMoney);
                return false;
            }
        }
        
        // Check crystals using ImprintingHelper
        if (essenceCrystal > 0) {
            if (!ImprintingHelper.hasEnoughCrystals(player, "essence_crystal", essenceCrystal)) {
                player.sendMessage(ChatColor.RED + "Insufficient Essence Crystals! Need " + essenceCrystal);
                return false;
            }
        }
        
        if (powerCrystal > 0) {
            if (!ImprintingHelper.hasEnoughCrystals(player, "power_crystal", powerCrystal)) {
                player.sendMessage(ChatColor.RED + "Insufficient Power Crystals! Need " + powerCrystal);
                return false;
            }
        }
        
        if (primordialCrystal > 0) {
            if (!ImprintingHelper.hasEnoughCrystals(player, "primordial_soul_crystal", primordialCrystal)) {
                player.sendMessage(ChatColor.RED + "Insufficient Primordial Soul Crystals! Need " + primordialCrystal);
                return false;
            }
        }
        
        // Consume resources
        if (vaultMoney > 0 && Main.getEconomy() != null) {
            Main.getEconomy().withdrawPlayer(player, vaultMoney);
        }
        
        if (essenceCrystal > 0) {
            if (!ImprintingHelper.removeCrystals(player, "essence_crystal", essenceCrystal)) {
                player.sendMessage(ChatColor.RED + "Failed to remove Essence Crystals!");
                return false;
            }
        }
        
        if (powerCrystal > 0) {
            if (!ImprintingHelper.removeCrystals(player, "power_crystal", powerCrystal)) {
                player.sendMessage(ChatColor.RED + "Failed to remove Power Crystals!");
                return false;
            }
        }
        
        if (primordialCrystal > 0) {
            if (!ImprintingHelper.removeCrystals(player, "primordial_soul_crystal", primordialCrystal)) {
                player.sendMessage(ChatColor.RED + "Failed to remove Primordial Soul Crystals!");
                return false;
            }
        }
        
        return true;
    }
    
    private ItemStack performImprinting(ItemStack targetItem, ItemStack setItem) {
        if (targetItem == null || setItem == null) return null;
        
        // Extract set information from set item
        String setName = extractSetName(setItem);
        List<String> setBonusLines = extractSetBonusLines(setItem);
        
        if (setName == null || setBonusLines.isEmpty()) {
            return null;
        }
        
        // Clone target item
        ItemStack result = targetItem.clone();
        ItemMeta meta = result.getItemMeta();
        if (meta == null) return null;
        
        // Get existing lore or create new
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        
        // Find insertion point (before Rarity line)
        int insertIndex = lore.size();
        for (int i = 0; i < lore.size(); i++) {
            String line = ChatColor.stripColor(lore.get(i));
            if (line.contains("Rarity:")) {
                insertIndex = i;
                break;
            }
        }
        
        // Get set tier for color formatting
        String setTier = getSetTier(setItem);
        String tierColor = getTierColor(setTier);
        String tierIndicator = getTierIndicator(setTier);
        
        // Insert imprinted set lines with tier indicator BEFORE "Imprinted"
        lore.add(insertIndex, ChatColor.GRAY + tierColor + tierIndicator + ChatColor.GRAY + " Imprinted Set: " + ChatColor.AQUA + setName);
        for (String bonusLine : setBonusLines) {
            lore.add(insertIndex + 1, bonusLine);
            insertIndex++;
        }
        
        // Normalize rarity line (based on TrinketsPlugin GemActionsListener)
        normalizeRarityLine(lore);
        
        meta.setLore(lore);
        result.setItemMeta(meta);
        
        return result;
    }
    
    private void normalizeRarityLine(List<String> lore) {
        boolean found = false;
        for (int i = 0; i < lore.size(); ) {
            String stripped = ChatColor.stripColor(lore.get(i)).trim().toLowerCase();
            if (stripped.startsWith("rarity:")) {
                if (!found) {
                    String line = lore.get(i);
                    int idx = line.toLowerCase().indexOf("rarity:");
                    String suffix = line.substring(idx + "rarity:".length());
                    if (suffix.startsWith(ChatColor.RESET.toString())) {
                        suffix = suffix.substring(ChatColor.RESET.toString().length());
                    }

                    lore.set(i, ChatColor.WHITE.toString() + ChatColor.BOLD + "Rarity:" + ChatColor.RESET + suffix);
                    found = true;
                    i++;
                } else {
                    lore.remove(i);
                }
            } else {
                i++;
            }
        }
    }
    
    private String extractSetName(ItemStack setItem) {
        if (setItem == null || !setItem.hasItemMeta()) return null;
        
        List<String> lore = setItem.getItemMeta().getLore();
        if (lore == null) return null;
        
        for (String line : lore) {
            String stripped = ChatColor.stripColor(line);
            if (stripped.contains("Set:")) {
                String[] parts = stripped.split("Set:", 2);
                if (parts.length > 1) {
                    return parts[1].trim();
                }
            }
        }
        return null;
    }
    
    private List<String> extractSetBonusLines(ItemStack setItem) {
        if (setItem == null || !setItem.hasItemMeta()) return new ArrayList<>();
        
        List<String> lore = setItem.getItemMeta().getLore();
        if (lore == null) return new ArrayList<>();
        
        List<String> bonusLines = new ArrayList<>();
        boolean foundSetLine = false;
        
        for (String line : lore) {
            String stripped = ChatColor.stripColor(line);
            
            if (stripped.contains("Set:")) {
                foundSetLine = true;
                continue;
            }
            
            if (foundSetLine) {
                // Look for bonus lines that start with [number]
                if (stripped.matches(".*\\[\\d+\\].*")) {
                    bonusLines.add(line);
                } else if (stripped.contains("Rarity:")) {
                    // Stop when we hit rarity line
                    break;
                }
            }
        }
        
        return bonusLines;
    }
    
    private boolean isGlassPane(ItemStack item) {
        if (item == null) return false;
        Material type = item.getType();
        return type == Material.GRAY_STAINED_GLASS_PANE || 
               type == Material.ORANGE_STAINED_GLASS_PANE ||
               type == Material.BLUE_STAINED_GLASS_PANE;
    }
    
    private String getSetTier(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        
        // First check if item has "Set:" in lore (must be a set item, not just tiered item)
        if (!isSetItem(item)) return null;
        
        String displayName = item.getItemMeta().getDisplayName();
        if (displayName == null) return null;
        
        String stripped = ChatColor.stripColor(displayName);
        if (stripped.contains("[ T1 ]")) return "t1";
        if (stripped.contains("[ T2 ]")) return "t2";
        if (stripped.contains("[ T3 ]")) return "t3";
        if (stripped.contains("[ T4 ]")) return "t4";
        if (stripped.contains("[ T5 ]")) return "t5";
        
        return null;
    }
    
    private boolean isSetItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        
        List<String> lore = item.getItemMeta().getLore();
        if (lore == null) return false;
        
        for (String line : lore) {
            String stripped = ChatColor.stripColor(line);
            // Must contain "Set:" but NOT "Imprinted Set:" (to prevent re-imprinting)
            if (stripped.contains("Set:") && !stripped.contains("Imprinted Set:")) {
                return true;
            }
        }
        return false;
    }
    
    private boolean hasImprint(ItemStack item) {
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
    
    private boolean areCompatibleTypes(ItemStack target, ItemStack setItem) {
        if (target == null || setItem == null) return false;
        
        Material targetType = target.getType();
        Material setType = setItem.getType();
        
        // Check if both are helmets
        if (isHelmet(targetType) && isHelmet(setType)) return true;
        // Check if both are chestplates
        if (isChestplate(targetType) && isChestplate(setType)) return true;
        // Check if both are leggings
        if (isLeggings(targetType) && isLeggings(setType)) return true;
        // Check if both are boots
        if (isBoots(targetType) && isBoots(setType)) return true;
        // Check if both are weapons
        if (isWeapon(targetType) && isWeapon(setType)) return true;
        // Check if both are tools
        if (isTool(targetType) && isTool(setType)) return true;
        // Check if both are accessories (from TrinketsPlugin)
        if (isAccessory(targetType) && isAccessory(setType)) return true;
        
        return false;
    }

    private boolean isHelmet(Material material) {
        return material.name().endsWith("_HELMET");
    }

    private boolean isChestplate(Material material) {
        return material.name().endsWith("_CHESTPLATE");
    }

    private boolean isLeggings(Material material) {
        return material.name().endsWith("_LEGGINGS");
    }

    private boolean isBoots(Material material) {
        return material.name().endsWith("_BOOTS");
    }

    private boolean isWeapon(Material material) {
        return material.name().endsWith("_SWORD") || 
               material.name().endsWith("_AXE") ||
               material == Material.BOW ||
               material == Material.CROSSBOW ||
               material == Material.TRIDENT;
    }

    private boolean isTool(Material material) {
        return material.name().endsWith("_PICKAXE") ||
               material.name().endsWith("_SHOVEL") ||
               material.name().endsWith("_HOE") ||
               material.name().endsWith("_AXE") && !isWeapon(material);
    }
    
    private boolean isAccessory(Material material) {
        // Based on AccessoryType enum from TrinketsPlugin
        return material == Material.TNT_MINECART ||      // RING_1
               material == Material.HOPPER_MINECART ||   // RING_2
               material == Material.CHEST_MINECART ||    // NECKLACE
               material == Material.FURNACE_MINECART ||  // ADORNMENT
               material == Material.WHITE_BANNER ||      // CLOAK
               material == Material.IRON_DOOR ||         // SHIELD
               material == Material.MINECART ||          // BELT
               material == Material.LEATHER_HORSE_ARMOR || // GLOVES
               material == Material.DRAGON_BREATH;       // BOSS_SOUL
    }
    
    private ItemStack createGlassPane(Material material, String name, String... lore) {
        ItemStack pane = new ItemStack(material);
        ItemMeta meta = pane.getItemMeta();
        meta.setDisplayName(name);
        if (lore.length > 0) {
            meta.setLore(List.of(lore));
        }
        pane.setItemMeta(meta);
        return pane;
    }
    
    /**
     * Get tier color based on tier string
     * T1: &9 (blue), T2: &a (green), T3: &e (yellow), T4: &5 (dark purple), T5: &c (red)
     */
    private String getTierColor(String tier) {
        if (tier == null) return ChatColor.GRAY.toString();
        
        switch (tier.toLowerCase()) {
            case "t1": return ChatColor.BLUE.toString();
            case "t2": return ChatColor.GREEN.toString();
            case "t3": return ChatColor.YELLOW.toString();
            case "t4": return ChatColor.DARK_PURPLE.toString();
            case "t5": return ChatColor.RED.toString();
            default: return ChatColor.GRAY.toString();
        }
    }
    
    /**
     * Get tier indicator with proper formatting
     */
    private String getTierIndicator(String tier) {
        if (tier == null) return "";
        
        switch (tier.toLowerCase()) {
            case "t1": return "[ T1 ]";
            case "t2": return "[ T2 ]";
            case "t3": return "[ T3 ]";
            case "t4": return "[ T4 ]";
            case "t5": return "[ T5 ]";
            default: return "";
        }
    }
}