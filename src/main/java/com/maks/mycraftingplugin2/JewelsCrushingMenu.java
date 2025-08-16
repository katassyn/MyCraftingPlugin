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

    private static final int debuggingFlag = 1; // Set to 0 to disable debug

    /**
     * List of all jewel names from jewele.yml (without tier indicators)
     */
    private static final List<String> JEWEL_NAMES = List.of(
        "Emberfang Jewel",           // DMG jewel
        "Windstep Sapphire",         // MOVE SPEED jewel
        "Whirlwind Opal",            // ATTACK SPEED jewel
        "Heartroot Ruby",            // HEALTH jewel
        "Stonehide Garnet",          // ARMOR TOUGHNESS jewel
        "Lasting Healing Jewel",     // LASTING HEALING jewel
        "Amplified Healing Jewel",   // AMPLIFIED HEALING jewel
        "Jewel of Focus",            // JEWEL OF FOCUS
        "Jewel of Rage",             // JEWEL OF RAGE
        "Steam Sale",                // STEAM SALE
        "Phoenix Egg",               // PHOENIX jewel
        "Shadowvein Crystal",        // ANDER jewel
        "Sunspire Amber",            // CLOVER jewel
        "Melonbane Prism",           // DRAKENMELON jewel
        "Deathcut Garnet",           // COLLECTOR jewel
        "Shadowpick Onyx",           // LOCKPICK jewel
        "Ingredient Jewel",          // INGREDIENT jewel
        "Golden Fish Jewel"          // GOLDEN FISH jewel
    );

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

        if (debuggingFlag == 1) {
            Bukkit.getLogger().info("[JewelsCrushing] Opening crushing menu for player: " + player.getName());
        }

        player.openInventory(inv);
    }

    /**
     * Enhanced jewel detection that works with all jewels from jewele.yml
     * @param item The item to check.
     * @return True if the item is a jewel, false otherwise.
     */
    public static boolean isJewel(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        String displayName = meta.getDisplayName();
        String cleanDisplayName = ChatColor.stripColor(displayName);

        if (debuggingFlag == 1) {
            Bukkit.getLogger().info("[JewelsCrushing] Checking item: " + displayName);
            Bukkit.getLogger().info("[JewelsCrushing] Clean name: " + cleanDisplayName);
        }

        // Check for tier indicators [ I ], [ II ], or [ III ]
        boolean hasTierIndicator = cleanDisplayName.contains("[ I ]") || 
                                  cleanDisplayName.contains("[ II ]") || 
                                  cleanDisplayName.contains("[ III ]");

        if (!hasTierIndicator) {
            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[JewelsCrushing] No tier indicator found in: " + cleanDisplayName);
            }
            return false;
        }

        // Check if the clean name contains any of the known jewel names
        boolean isKnownJewel = false;
        String matchedJewelName = "";

        for (String jewelName : JEWEL_NAMES) {
            if (cleanDisplayName.contains(jewelName)) {
                isKnownJewel = true;
                matchedJewelName = jewelName;
                break;
            }
        }

        if (!isKnownJewel) {
            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[JewelsCrushing] Item name does not match any known jewel names");
            }
            return false;
        }

        // Additional verification: check for DURABILITY enchantment (all jewels have this)
        if (meta.hasEnchants() && meta.getEnchants().containsKey(Enchantment.DURABILITY)) {
            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[JewelsCrushing] Valid jewel detected: " + matchedJewelName + " (tier " + getJewelTier(item) + ")");
            }
            return true;
        }

        if (debuggingFlag == 1) {
            Bukkit.getLogger().info("[JewelsCrushing] Item matches jewel name but has no DURABILITY enchantment: " + cleanDisplayName);
        }
        return false;
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

        if (debuggingFlag == 1) {
            Bukkit.getLogger().info("[JewelsCrushing] Processing jewels for player: " + player.getName());
        }

        // Process the first two rows (0-17) where jewels can be placed
        for (int i = 0; i < 18; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && isJewel(item)) {
                int tier = getJewelTier(item);
                int amount = item.getAmount();

                if (debuggingFlag == 1) {
                    Bukkit.getLogger().info("[JewelsCrushing] Found jewel in slot " + i + 
                                          ", tier: " + tier + ", amount: " + amount);
                }

                // Calculate dust based on tier
                if (tier > 0) {
                    totalDust += tier * amount;
                    slotsToEmpty.add(i);
                }
            }
        }

        if (debuggingFlag == 1) {
            Bukkit.getLogger().info("[JewelsCrushing] Total dust to give: " + totalDust);
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

                if (debuggingFlag == 1) {
                    Bukkit.getLogger().info("[JewelsCrushing] Successfully gave " + totalDust + 
                                          " jewel dust to " + player.getName());
                }
            } else {
                player.sendMessage(ChatColor.RED + "Your inventory is full! Make space before crushing jewels.");

                if (debuggingFlag == 1) {
                    Bukkit.getLogger().info("[JewelsCrushing] Player inventory full: " + player.getName());
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "No valid jewels to crush!");

            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[JewelsCrushing] No valid jewels found for: " + player.getName());
            }
        }
    }

    /**
     * Returns all items from crushing slots to player's inventory
     * @param player The player
     * @param inv The crushing inventory
     */
    public static void returnItemsToPlayer(Player player, Inventory inv) {
        if (debuggingFlag == 1) {
            Bukkit.getLogger().info("[JewelsCrushing] Returning items to player: " + player.getName());
        }

        List<ItemStack> itemsToReturn = new ArrayList<>();

        // Collect items from first two rows (0-17)
        for (int i = 0; i < 18; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                itemsToReturn.add(item.clone());
                inv.setItem(i, null);

                if (debuggingFlag == 1) {
                    Bukkit.getLogger().info("[JewelsCrushing] Found item in slot " + i + ": " + 
                                          item.getType() + " x" + item.getAmount());
                }
            }
        }

        // Return items to player
        for (ItemStack item : itemsToReturn) {
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(item);
            } else {
                // Drop on ground if inventory full
                player.getWorld().dropItemNaturally(player.getLocation(), item);
                player.sendMessage(ChatColor.YELLOW + "Some items were dropped on the ground due to full inventory!");
            }
        }

        if (!itemsToReturn.isEmpty()) {
            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[JewelsCrushing] Returned " + itemsToReturn.size() + 
                                      " items to " + player.getName());
            }
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
                if (debuggingFlag == 1) {
                    Bukkit.getLogger().info("[JewelsCrushing] Got jewel_dust from ItemManager");
                }
                return (ItemStack) result;
            }
        } catch (Exception e) {
            // If the above fails, create a basic jewel dust item
            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[JewelsCrushing] Could not get jewel_dust from ItemManager, creating basic item: " + e.getMessage());
            }
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

        if (debuggingFlag == 1) {
            Bukkit.getLogger().info("[JewelsCrushing] Created basic jewel dust item");
        }

        return dust;
    }

    /**
     * Test method to check if jewel detection works properly
     * @param player The player to send test results to
     */
    public static void testJewelDetection(Player player) {
        if (debuggingFlag != 1) {
            player.sendMessage(ChatColor.RED + "Debug mode is disabled. Enable debuggingFlag to use this test.");
            return;
        }

        player.sendMessage(ChatColor.YELLOW + "=== JEWEL DETECTION TEST ===");

        // Test all items in player's inventory
        int jewelCount = 0;
        int totalItems = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                totalItems++;
                boolean isDetectedAsJewel = isJewel(item);

                if (isDetectedAsJewel) {
                    jewelCount++;
                    int tier = getJewelTier(item);
                    String name = item.hasItemMeta() && item.getItemMeta().hasDisplayName() 
                        ? ChatColor.stripColor(item.getItemMeta().getDisplayName()) 
                        : item.getType().toString();

                    player.sendMessage(ChatColor.GREEN + "✓ JEWEL: " + name + " (Tier " + tier + ")");
                } else {
                    String name = item.hasItemMeta() && item.getItemMeta().hasDisplayName() 
                        ? ChatColor.stripColor(item.getItemMeta().getDisplayName()) 
                        : item.getType().toString();

                    // Only show non-jewels if they have tier indicators (might be false negatives)
                    if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                        String displayName = item.getItemMeta().getDisplayName();
                        if (displayName.contains("[ I ]") || displayName.contains("[ II ]") || displayName.contains("[ III ]")) {
                            player.sendMessage(ChatColor.RED + "✗ NOT JEWEL: " + name + " (has tier indicator!)");
                        }
                    }
                }
            }
        }

        player.sendMessage(ChatColor.YELLOW + "=== TEST RESULTS ===");
        player.sendMessage(ChatColor.WHITE + "Total items checked: " + totalItems);
        player.sendMessage(ChatColor.GREEN + "Jewels detected: " + jewelCount);
        player.sendMessage(ChatColor.YELLOW + "========================");

        Bukkit.getLogger().info("[JewelsCrushing] Test completed for " + player.getName() + 
                              " - " + jewelCount + "/" + totalItems + " items detected as jewels");
    }
}
