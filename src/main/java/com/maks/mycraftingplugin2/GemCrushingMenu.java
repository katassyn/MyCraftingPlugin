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
 * Menu for crushing gems into Shiny Dust.
 */
public class GemCrushingMenu {

    private static final List<String> GEM_NAMES = List.of(
            "Ruby",
            "Amethyst",
            "Cyianite",
            "Zircon",
            "Diamond",
            "Rhodolite",
            "Onyx"
    );

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Gem Crushing");

        for (int i = 0; i < inv.getSize(); i++) {
            if (i < 18) {
                continue;
            } else if (i == 22) {
                ItemStack confirmButton = new ItemStack(Material.EMERALD);
                ItemMeta meta = confirmButton.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.GREEN + "Confirm Crushing");
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GRAY + "Click to crush all gems");
                    lore.add(ChatColor.GRAY + "and receive Shiny Dust");
                    meta.setLore(lore);
                    confirmButton.setItemMeta(meta);
                }
                inv.setItem(i, confirmButton);
            } else {
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

    public static boolean isGem(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        String displayName = ChatColor.stripColor(meta.getDisplayName());

        boolean hasTier = displayName.contains("[ I ]") || displayName.contains("[ II ]") || displayName.contains("[ III ]");
        if (!hasTier) {
            return false;
        }

        boolean isKnownGem = false;
        for (String name : GEM_NAMES) {
            if (displayName.contains(name)) {
                isKnownGem = true;
                break;
            }
        }
        if (!isKnownGem) {
            return false;
        }

        return meta.hasEnchant(Enchantment.DURABILITY);
    }

    public static int getGemTier(ItemStack item) {
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

    public static void processGems(Player player, Inventory inv) {
        int totalDust = 0;
        List<Integer> slotsToEmpty = new ArrayList<>();

        for (int i = 0; i < 18; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && isGem(item)) {
                int tier = getGemTier(item);
                int amount = item.getAmount();
                if (tier > 0) {
                    totalDust += tier * amount;
                    slotsToEmpty.add(i);
                }
            }
        }

        if (totalDust > 0) {
            ItemStack dust = createShinyDust(totalDust);
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(dust);
                for (int slot : slotsToEmpty) {
                    inv.setItem(slot, null);
                }
                player.sendMessage(ChatColor.GREEN + "You crushed gems and received " + totalDust + " Shiny Dust!");
            } else {
                player.sendMessage(ChatColor.RED + "Your inventory is full! Make space before crushing gems.");
            }
        } else {
            player.sendMessage(ChatColor.RED + "No valid gems to crush!");
        }
    }

    public static void returnItemsToPlayer(Player player, Inventory inv) {
        List<ItemStack> itemsToReturn = new ArrayList<>();
        for (int i = 0; i < 18; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                itemsToReturn.add(item.clone());
                inv.setItem(i, null);
            }
        }

        for (ItemStack item : itemsToReturn) {
            player.getInventory().addItem(item);
        }
    }

    private static ItemStack createShinyDust(int amount) {
        try {
            Class<?> itemManagerClass = Class.forName("com.maks.trinketsplugin.ItemManager");
            java.lang.reflect.Method getItemMethod = itemManagerClass.getMethod("getItem", String.class, int.class);
            Object result = getItemMethod.invoke(null, "shiny_dust", amount);
            if (result instanceof ItemStack) {
                return (ItemStack) result;
            }
        } catch (Exception ignored) {
        }

        ItemStack dust = new ItemStack(Material.GLOW_INK_SAC, amount);
        ItemMeta meta = dust.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§5Shiny Dust");
            List<String> lore = new ArrayList<>();
            lore.add("§7§oUsed to upgrade gems");
            meta.setLore(lore);
            meta.addEnchant(Enchantment.DURABILITY, 10, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
            meta.setUnbreakable(true);
            dust.setItemMeta(meta);
        }
        return dust;
    }
}
