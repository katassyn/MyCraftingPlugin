package com.maks.mycraftingplugin2;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScientistRecipeSelectMenu {
    private static final NamespacedKey KEY = new NamespacedKey(Main.getInstance(), "scientist_recipe_key");
    private static final Pattern BONE_KEY = Pattern.compile("^unlock_([a-z0-9]+)_([IV]+)$");
    private static final List<String> BONE_ORDER = Arrays.asList(
            "weapon", "helm", "chest", "legs", "boots",
            "ring1", "ring2", "necklace", "adornment",
            "cloak", "shield", "belt", "gloves"
    );
    private static final Map<String, Material> CATEGORY_ICONS = Map.ofEntries(
            Map.entry("weapon", Material.NETHERITE_SWORD),
            Map.entry("helm", Material.NETHERITE_HELMET),
            Map.entry("chest", Material.NETHERITE_CHESTPLATE),
            Map.entry("legs", Material.NETHERITE_LEGGINGS),
            Map.entry("boots", Material.NETHERITE_BOOTS),
            Map.entry("ring1", Material.GOLD_NUGGET),
            Map.entry("ring2", Material.IRON_NUGGET),
            Map.entry("necklace", Material.CHAIN),
            Map.entry("adornment", Material.AMETHYST_SHARD),
            Map.entry("cloak", Material.PURPLE_WOOL),
            Map.entry("shield", Material.SHIELD),
            Map.entry("belt", Material.RABBIT_HIDE),
            Map.entry("gloves", Material.LEATHER)
    );

    private record BoneRecipe(String key, String title, String category, int tier) {}

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "Select Scientist Recipe");
        fillWithGlass(inv);

        Map<String, String> titles = ScientistResearchUnlockHelper.getRecipeTitles();
        if (titles.isEmpty()) {
            inv.setItem(22, createMenuItem(Material.BARRIER, ChatColor.RED + "No scientist recipes found"));
            inv.setItem(49, createMenuItem(Material.ARROW, ChatColor.YELLOW + "Back"));
            player.openInventory(inv);
            return;
        }

        List<BoneRecipe> boneRecipes = new ArrayList<>();
        List<Map.Entry<String, String>> otherRecipes = new ArrayList<>();
        for (Map.Entry<String, String> entry : titles.entrySet()) {
            Matcher matcher = BONE_KEY.matcher(entry.getKey());
            if (matcher.matches()) {
                String category = matcher.group(1).toLowerCase(Locale.ROOT);
                int tier = romanToTier(matcher.group(2));
                if (tier >= 1 && tier <= 3 && BONE_ORDER.contains(category)) {
                    boneRecipes.add(new BoneRecipe(entry.getKey(), entry.getValue(), category, tier));
                } else {
                    otherRecipes.add(entry);
                }
            } else {
                otherRecipes.add(entry);
            }
        }

        placeBoneRecipes(inv, boneRecipes);
        placeOtherRecipes(inv, otherRecipes, Collections.singleton(49));

        inv.setItem(49, createMenuItem(Material.ARROW, ChatColor.YELLOW + "Back"));
        player.openInventory(inv);
    }

    private static void placeBoneRecipes(Inventory inv, List<BoneRecipe> recipes) {
        for (BoneRecipe recipe : recipes) {
            int orderIndex = BONE_ORDER.indexOf(recipe.category);
            if (orderIndex < 0) continue;
            int block = orderIndex / 9;
            int column = orderIndex % 9;
            int baseRow = block * 3;
            int tier = Math.min(Math.max(recipe.tier, 1), 3) - 1;
            int slot = (baseRow + tier) * 9 + column;
            if (slot >= inv.getSize()) {
                continue;
            }
            inv.setItem(slot, createBoneRecipeItem(recipe));
        }
    }

    private static void placeOtherRecipes(Inventory inv, List<Map.Entry<String, String>> others, Collection<Integer> reservedSlots) {
        List<Integer> freeSlots = new ArrayList<>();
        for (int i = 0; i < inv.getSize(); i++) {
            if (reservedSlots != null && reservedSlots.contains(i)) {
                continue;
            }
            ItemStack existing = inv.getItem(i);
            if (existing == null || existing.getType() == Material.BLACK_STAINED_GLASS_PANE) {
                freeSlots.add(i);
            }
        }
        int index = 0;
        for (Map.Entry<String, String> entry : others) {
            if (index >= freeSlots.size()) {
                break;
            }
            int slot = freeSlots.get(index++);
            inv.setItem(slot, createGenericRecipeItem(entry.getKey(), entry.getValue()));
        }
    }

    private static ItemStack createBoneRecipeItem(BoneRecipe recipe) {
        Material icon = CATEGORY_ICONS.getOrDefault(recipe.category, Material.PAPER);
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String tierRoman = switch (recipe.tier) {
                case 2 -> "II";
                case 3 -> "III";
                default -> "I";
            };
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + recipe.title);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Key: " + recipe.key);
            lore.add(ChatColor.DARK_GRAY + "Category: " + ChatColor.WHITE + capitalize(recipe.category));
            lore.add(ChatColor.DARK_GRAY + "Tier: " + ChatColor.WHITE + tierRoman);
            meta.setLore(lore);
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(KEY, PersistentDataType.STRING, recipe.key);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack createGenericRecipeItem(String key, String title) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + title);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + key);
            meta.setLore(lore);
            meta.getPersistentDataContainer().set(KEY, PersistentDataType.STRING, key);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static String readKey(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (pdc.has(KEY, PersistentDataType.STRING)) {
            return pdc.get(KEY, PersistentDataType.STRING);
        }
        List<String> lore = meta.getLore();
        if (lore != null && !lore.isEmpty()) {
            return ChatColor.stripColor(lore.get(0));
        }
        return null;
    }

    private static ItemStack createMenuItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static void fillWithGlass(Inventory inv) {
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("");
            glass.setItemMeta(meta);
        }
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, glass);
            }
        }
    }

    private static int romanToTier(String roman) {
        if (roman == null) return 0;
        return switch (roman.toUpperCase(Locale.ROOT)) {
            case "I" -> 1;
            case "II" -> 2;
            case "III" -> 3;
            default -> 0;
        };
    }

    private static String capitalize(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        String lower = input.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + (lower.length() > 1 ? lower.substring(1) : "");
    }
}
