package com.maks.mycraftingplugin2;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class ImprintingGUI implements InventoryHolder {
    private final Inventory inventory;
    private final Player player;

    public ImprintingGUI(Player player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 27, ChatColor.DARK_PURPLE + "Set Imprinting");
        setupGUI();
    }

    private void setupGUI() {
        // Fill with glass panes
        ItemStack grayPane = createGlassPane(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            if (i != 10 && i != 12 && i != 16) {
                inventory.setItem(i, grayPane);
            }
        }

        // Target item slot (non-set item)
        ItemStack targetSlot = createGlassPane(Material.ORANGE_STAINED_GLASS_PANE, 
            ChatColor.GOLD + "Target Item Slot",
            ChatColor.GRAY + "Place non-set item here");
        inventory.setItem(10, targetSlot);

        // Set item slot (set item to copy from)
        ItemStack setSlot = createGlassPane(Material.BLUE_STAINED_GLASS_PANE,
            ChatColor.AQUA + "Set Item Slot", 
            ChatColor.GRAY + "Place set item here (T1-T5)");
        inventory.setItem(12, setSlot);

        // Accept button
        ItemStack acceptButton = new ItemStack(Material.EMERALD);
        ItemMeta acceptMeta = acceptButton.getItemMeta();
        acceptMeta.setDisplayName(ChatColor.GREEN + "Accept Imprinting");
        acceptMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Click to imprint set bonus",
            ChatColor.YELLOW + "Costs will be displayed when",
            ChatColor.YELLOW + "both items are placed"
        ));
        acceptButton.setItemMeta(acceptMeta);
        inventory.setItem(16, acceptButton);
    }

    private ItemStack createGlassPane(Material material, String name, String... lore) {
        ItemStack pane = new ItemStack(material);
        ItemMeta meta = pane.getItemMeta();
        meta.setDisplayName(name);
        if (lore.length > 0) {
            meta.setLore(Arrays.asList(lore));
        }
        pane.setItemMeta(meta);
        return pane;
    }

    public void updateAcceptButton() {
        ItemStack targetItem = inventory.getItem(10);
        ItemStack setItem = inventory.getItem(12);
        
        ItemStack acceptButton = new ItemStack(Material.EMERALD);
        ItemMeta acceptMeta = acceptButton.getItemMeta();
        
        if (targetItem != null && setItem != null && 
            !isGlassPane(targetItem) && !isGlassPane(setItem)) {
            
            // Get set tier and calculate costs
            String setTier = getSetTier(setItem);
            if (setTier != null && !hasImprint(targetItem) && !isSetItem(targetItem) && areCompatibleTypes(targetItem, setItem)) {
                acceptMeta.setDisplayName(ChatColor.GREEN + "Accept Imprinting");
                List<String> costs = getCostLore(setTier);
                acceptMeta.setLore(costs);
                acceptButton.setItemMeta(acceptMeta);
                inventory.setItem(16, acceptButton);
                return;
            }
        }
        
        // Default disabled state
        acceptButton = new ItemStack(Material.BARRIER);
        acceptMeta = acceptButton.getItemMeta();
        acceptMeta.setDisplayName(ChatColor.RED + "Cannot Imprint");
        
        if (targetItem == null || isGlassPane(targetItem)) {
            acceptMeta.setLore(Arrays.asList(ChatColor.GRAY + "Place target item first"));
        } else if (setItem == null || isGlassPane(setItem)) {
            acceptMeta.setLore(Arrays.asList(ChatColor.GRAY + "Place set item second"));
        } else if (hasImprint(targetItem)) {
            acceptMeta.setLore(Arrays.asList(ChatColor.RED + "Target already has imprint"));
        } else if (isSetItem(targetItem)) {
            acceptMeta.setLore(Arrays.asList(ChatColor.RED + "Cannot imprint onto set items"));
        } else if (!areCompatibleTypes(targetItem, setItem)) {
            acceptMeta.setLore(Arrays.asList(ChatColor.RED + "Items must be same type"));
        } else if (getSetTier(setItem) == null) {
            acceptMeta.setLore(Arrays.asList(ChatColor.RED + "Set item tier not found"));
        }
        
        acceptButton.setItemMeta(acceptMeta);
        inventory.setItem(16, acceptButton);
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

    private List<String> getCostLore(String tier) {
        Main plugin = Main.getInstance();
        
        int essenceCrystal = plugin.getConfig().getInt("imprinting.costs." + tier + ".essence_crystal", 0);
        int powerCrystal = plugin.getConfig().getInt("imprinting.costs." + tier + ".power_crystal", 0);
        int primordialCrystal = plugin.getConfig().getInt("imprinting.costs." + tier + ".primordial_soul_crystal", 0);
        double vaultMoney = plugin.getConfig().getDouble("imprinting.costs." + tier + ".vault_money", 0);
        
        return Arrays.asList(
            ChatColor.GRAY + "Costs:",
            ChatColor.AQUA + "Essence Crystal: " + ChatColor.WHITE + essenceCrystal,
            ChatColor.LIGHT_PURPLE + "Power Crystal: " + ChatColor.WHITE + powerCrystal,
            ChatColor.GOLD + "Primordial Soul Crystal: " + ChatColor.WHITE + primordialCrystal,
            ChatColor.GREEN + "Money: " + ChatColor.WHITE + "$" + formatMoney(vaultMoney)
        );
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public Player getPlayer() {
        return player;
    }
    
    /**
     * Format money with K, M, B suffixes
     * @param money The money amount
     * @return Formatted string with appropriate suffix
     */
    private String formatMoney(double money) {
        if (money == 0) return "0";
        
        if (money >= 1_000_000_000) {
            // Billions
            double billions = money / 1_000_000_000;
            if (billions == (long) billions) {
                return String.format("%.0fB", billions);
            } else {
                return String.format("%.1fB", billions);
            }
        } else if (money >= 1_000_000) {
            // Millions
            double millions = money / 1_000_000;
            if (millions == (long) millions) {
                return String.format("%.0fM", millions);
            } else {
                return String.format("%.1fM", millions);
            }
        } else if (money >= 1_000) {
            // Thousands
            double thousands = money / 1_000;
            if (thousands == (long) thousands) {
                return String.format("%.0fK", thousands);
            } else {
                return String.format("%.1fK", thousands);
            }
        } else {
            // Less than 1000
            if (money == (long) money) {
                return String.format("%.0f", money);
            } else {
                return String.format("%.2f", money);
            }
        }
    }
}