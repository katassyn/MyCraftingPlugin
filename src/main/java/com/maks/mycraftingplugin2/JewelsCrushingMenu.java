package com.maks.mycraftingplugin2;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Menu for crushing jewels into Jewel Dust.
 */
public class JewelsCrushingMenu {

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Jewels Crushing");

        // Set up the inventory with dark glass panes
        for (int i = 0; i < inv.getSize(); i++) {
            if (i < 18) {
                // First two rows are for jewel insertion - leave empty
                continue;
            } else if (i == 22) {
                // Confirm button in the middle of the bottom row
                ItemStack confirmButton = new ItemStack(Material.EMERALD);
                ItemMeta meta = confirmButton.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.GREEN + "Confirm Crushing");
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GRAY + "Click to crush all jewels");
                    lore.add(ChatColor.GRAY + "and receive Jewel Dust");
                    meta.setLore(lore);
                    confirmButton.setItemMeta(meta);
                }
                inv.setItem(i, confirmButton);
            } else {
                // Fill the rest with dark glass panes
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
     * Checks if an item is a jewel based on its display name.
     * @param item The item to check.
     * @return True if the item is a jewel, false otherwise.
     */
    public static boolean isJewel(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }

        String displayName = item.getItemMeta().getDisplayName();
        // Check if it contains tier indicators [ I ], [ II ], or [ III ] and has "Jewel" in the name
        return (displayName.contains("[ I ]") || displayName.contains("[ II ]") || 
                displayName.contains("[ III ]")) && displayName.contains("Jewel");
    }

    /**
     * Determines jewel tier from the item's display name.
     * @param item The jewel item.
     * @return The tier (1, 2, or 3) or 0 if not a valid jewel.
     */
    public static int getJewelTier(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return 0;
        }

        String displayName = item.getItemMeta().getDisplayName();
        if (displayName.contains("[ I ]")) {
            return 1;
        } else if (displayName.contains("[ II ]")) {
            return 2;
        } else if (displayName.contains("[ III ]")) {
            return 3;
        }
        return 0;
    }

    /**
     * Processes jewels in the crushing inventory and gives appropriate dust.
     * @param player The player.
     * @param inv The crushing inventory.
     */
    public static void processJewels(Player player, Inventory inv) {
        int totalDust = 0;
        List<Integer> slotsToEmpty = new ArrayList<>();

        // Process the first two rows (0-17) where jewels can be placed
        for (int i = 0; i < 18; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && isJewel(item)) {
                int tier = getJewelTier(item);
                int amount = item.getAmount();

                // Calculate dust based on tier
                if (tier > 0) {
                    totalDust += tier * amount;
                    slotsToEmpty.add(i);
                }
            }
        }

        if (totalDust > 0) {
            // Create jewel dust item
            ItemStack jewelDust = createJewelDust(totalDust);

            // Give dust to player
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(jewelDust);

                // Clear the processed jewels
                for (int slot : slotsToEmpty) {
                    inv.setItem(slot, null);
                }

                player.sendMessage(ChatColor.GREEN + "You crushed jewels and received " + 
                                  totalDust + " Jewel Dust!");

                // NIE ZAMYKAMY GUI!
                // Gracz może chcieć dodać więcej jeweli do crushingu

                // Opcjonalnie odśwież inventory
                player.updateInventory();

            } else {
                player.sendMessage(ChatColor.RED + "Your inventory is full! Make space before crushing jewels.");
                // Również nie zamykamy GUI - gracz może zrobić miejsce i spróbować ponownie
            }
        } else {
            player.sendMessage(ChatColor.RED + "No valid jewels to crush!");
            // Nie zamykamy GUI - gracz może dodać jewele
        }
    }

    /**
     * Creates a Jewel Dust ItemStack.
     * @param amount The amount of dust.
     * @return The Jewel Dust ItemStack.
     */
    private static ItemStack createJewelDust(int amount) {
        // Try to get Jewel Dust from items.yml if available
        try {
            // This assumes there's a getItemFromConfig method or similar in your ItemManager class
            // If you don't have such a mechanism, you'd need to create a basic item here
            Class<?> itemManagerClass = Class.forName("com.maks.trinketsplugin.ItemManager");
            java.lang.reflect.Method getItemMethod = itemManagerClass.getMethod("getItem", String.class, int.class);
            Object result = getItemMethod.invoke(null, "jewel_dust", amount);
            if (result instanceof ItemStack) {
                return (ItemStack) result;
            }
        } catch (Exception e) {
            // If the above fails, create a basic jewel dust item
            Bukkit.getLogger().info("Could not get jewel_dust from ItemManager, creating basic item");
        }

        // Create a basic Jewel Dust item
        ItemStack dust = new ItemStack(Material.INK_SAC, amount);
        ItemMeta meta = dust.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.BLUE + "Jewel Dust");
            List<String> lore = new ArrayList<>();
            lore.add("§7§oUsed to upgrade jewels");
            meta.setLore(lore);

            // Add enchantments
            meta.addEnchant(Enchantment.DURABILITY, 10, true);

            // Add item flags
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

            // Set unbreakable
            meta.setUnbreakable(true);

            dust.setItemMeta(meta);
        }
        return dust;
    }
}
