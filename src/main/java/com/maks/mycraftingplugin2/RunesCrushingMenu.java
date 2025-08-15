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
 * Menu for crushing runes into Rune Dust.
 */
public class RunesCrushingMenu {

    private static final List<String> RUNE_NAMES = List.of(
            "Uruz",
            "Algiz",
            "Shield",
            "Thurisaz",
            "Wunjo",
            "Laguz",
            "Gebo",
            "Ehwaz",
            "Berkano"
    );

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Rune Crushing");

        for (int i = 0; i < inv.getSize(); i++) {
            if (i < 18) {
                continue;
            } else if (i == 22) {
                ItemStack confirmButton = new ItemStack(Material.EMERALD);
                ItemMeta meta = confirmButton.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.GREEN + "Confirm Crushing");
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GRAY + "Click to crush all runes");
                    lore.add(ChatColor.GRAY + "and receive Rune Dust");
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

    public static boolean isRune(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        String displayName = ChatColor.stripColor(meta.getDisplayName());

        boolean hasTier = displayName.contains("[ I ]") || displayName.contains("[ II ]") || displayName.contains("[ III ]");
        if (!hasTier) {
            return false;
        }

        boolean isKnownRune = false;
        for (String name : RUNE_NAMES) {
            if (displayName.contains(name)) {
                isKnownRune = true;
                break;
            }
        }
        if (!isKnownRune) {
            return false;
        }

        return meta.hasEnchant(Enchantment.DURABILITY);
    }

    public static int getRuneTier(ItemStack item) {
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

    public static void processRunes(Player player, Inventory inv) {
        int totalDust = 0;
        List<Integer> slotsToEmpty = new ArrayList<>();

        for (int i = 0; i < 18; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && isRune(item)) {
                int tier = getRuneTier(item);
                int amount = item.getAmount();
                if (tier > 0) {
                    totalDust += tier * amount;
                    slotsToEmpty.add(i);
                }
            }
        }

        if (totalDust > 0) {
            ItemStack dust = createRuneDust(totalDust);
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(dust);
                for (int slot : slotsToEmpty) {
                    inv.setItem(slot, null);
                }
                player.sendMessage(ChatColor.GREEN + "You crushed runes and received " + totalDust + " Rune Dust!");
            } else {
                player.sendMessage(ChatColor.RED + "Your inventory is full! Make space before crushing runes.");
            }
        } else {
            player.sendMessage(ChatColor.RED + "No valid runes to crush!");
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

    private static ItemStack createRuneDust(int amount) {
        try {
            Class<?> itemManagerClass = Class.forName("com.maks.trinketsplugin.ItemManager");
            java.lang.reflect.Method getItemMethod = itemManagerClass.getMethod("getItem", String.class, int.class);
            Object result = getItemMethod.invoke(null, "rune_dust", amount);
            if (result instanceof ItemStack) {
                return (ItemStack) result;
            }
        } catch (Exception ignored) {
        }

        ItemStack dust = new ItemStack(Material.CLAY_BALL, amount);
        ItemMeta meta = dust.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§cRune Dust");
            List<String> lore = new ArrayList<>();
            // Ensure color code comes before italics so formatting is preserved
            lore.add("§7§oUsed to upgrade runes");

            meta.setLore(lore);
            meta.addEnchant(Enchantment.DURABILITY, 10, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
            meta.setUnbreakable(true);
            dust.setItemMeta(meta);
        }
        return dust;
    }
}
