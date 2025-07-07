package com.maks.mycraftingplugin2.integration;

import java.util.HashMap;
import java.util.Map;

/**
 * Complete mappings between display names and pouch item IDs
 * Based on actual ItemManager.java from IngredientPouchPlugin
 */
public class PouchItemMappings {

    private static final Map<String, String> itemMappings = new HashMap<>();

    static {
        initializeMappings();
    }

    private static void initializeMappings() {
        // EXPO category items - poprawione ID zgodnie z ItemManager
        itemMappings.put("Fragment of Infernal Passage", "ips");
        itemMappings.put("Broken Armor Piece", "broken_armor_piece");
        itemMappings.put("Tousled Priest Robe", "tousled_priest_robe");
        itemMappings.put("Black Fur", "fur_black");
        itemMappings.put("Dragon Scale", "dragon_scale");
        itemMappings.put("Chain Fragment", "chain_fragment");
        itemMappings.put("Satyr`s Horn", "satyrs_horn");
        itemMappings.put("Gorgon`s Poison", "gorgons_poison");
        itemMappings.put("Dragon`s Gold", "dragon_gold");
        itemMappings.put("Protector`s Heart", "protectors_heart");
        itemMappings.put("Dead Bush", "deaddd_bush");
        itemMappings.put("Demon Blood", "demon_blood");
        itemMappings.put("Sticky Mucus", "sticky_mucus");
        itemMappings.put("Soul of an Acient Spartan", "soul_of_an_acient_spartan");
        itemMappings.put("Shadow Rose", "shadow_rose");
        itemMappings.put("Throphy of the Long Forgotten Bone Dragon", "Throphy_of_the_long_forgotten_bone_dragon");

        // MONSTER_FRAGMENTS category items
        itemMappings.put("[ I ] Monster Soul Fragment", "mob_soul_I");
        itemMappings.put("[ II ] Monster Soul Fragment", "mob_soul_II");
        itemMappings.put("[ III ] Monster Soul Fragment", "mob_soul_III");
        itemMappings.put("[ I ] Monster Heart Fragment", "elite_heart_I");
        itemMappings.put("[ II ] Monster Heart Fragment", "elite_heart_II");
        itemMappings.put("[ III ] Monster Heart Fragment", "elite_heart_III");

        // Q (boss) category items
        itemMappings.put("[ I ] Grimmage Burned Cape", "grimmag_frag_I");
        itemMappings.put("[ II ] Grimmage Burned Cape", "grimmag_frag_II");
        itemMappings.put("[ III ] Grimmage Burned Cape", "grimmag_frag_III");
        itemMappings.put("[ I ] Arachna Poisonous Skeleton", "arachna_frag_I");
        itemMappings.put("[ II ] Arachna Poisonous Skeleton", "arachna_frag_II");
        itemMappings.put("[ III ] Arachna Poisonous Skeleton", "arachna_frag_III");
        itemMappings.put("[ I ] Heredur's Glacial Armor", "heredur_frag_I");
        itemMappings.put("[ II ] Heredur's Glacial Armor", "heredur_frag_II");
        itemMappings.put("[ III ] Heredur's Glacial Armor", "heredur_frag_III");
        itemMappings.put("[ I ] Bearach Honey Hide", "bearach_frag_I");
        itemMappings.put("[ II ] Bearach Honey Hide", "bearach_frag_II");
        itemMappings.put("[ III ] Bearach Honey Hide", "bearach_frag_III");
        itemMappings.put("[ I ] Khalys Magic Robe", "khalys_frag_I");
        itemMappings.put("[ II ] Khalys Magic Robe", "khalys_frag_II");
        itemMappings.put("[ III ] Khalys Magic Robe", "khalys_frag_III");
        itemMappings.put("[ I ] Herald's Dragon Skin", "heralds_frag_I");
        itemMappings.put("[ II ] Herald's Dragon Skin", "heralds_frag_II");
        itemMappings.put("[ III ] Herald's Dragon Skin", "heralds_frag_III");
        itemMappings.put("[ I ] Sigrismarr's Eternal Ice", "sigrismar_frag_I");
        itemMappings.put("[ II ] Sigrismarr's Eternal Ice", "sigrismar_frag_II");
        itemMappings.put("[ III ] Sigrismarr's Eternal Ice", "sigrismar_frag_III");
        itemMappings.put("[ I ] Medusa Stone Scales", "medusa_frag_I");
        itemMappings.put("[ II ] Medusa Stone Scales", "medusa_frag_II");
        itemMappings.put("[ III ] Medusa Stone Scales", "medusa_frag_III");
        itemMappings.put("[ I ] Gorga's Broken Tooth", "gorga_frag_I");
        itemMappings.put("[ II ] Gorga's Broken Tooth", "gorga_frag_II");
        itemMappings.put("[ III ] Gorga's Broken Tooth", "gorga_frag_III");
        itemMappings.put("[ I ] Mortis Sacrificial Bones", "mortis_frag_I");
        itemMappings.put("[ II ] Mortis Sacrificial Bones", "mortis_frag_II");
        itemMappings.put("[ III ] Mortis Sacrificial Bones", "mortis_frag_III");

        // KOPALNIA (mine) category items
        itemMappings.put("[ I ] Ore", "ore_I");
        itemMappings.put("[ II ] Ore", "ore_II");
        itemMappings.put("[ III ] Ore", "ore_III");
        itemMappings.put("[ I ] Cursed Blood", "blood_I");
        itemMappings.put("[ II ] Cursed Blood", "blood_II");
        itemMappings.put("[ III ] Cursed Blood", "blood_III");
        itemMappings.put("[ I ] Shattered Bone", "bone_I");
        itemMappings.put("[ II ] Shattered Bone", "bone_II");
        itemMappings.put("[ III ] Shattered Bone", "bone_III");
        itemMappings.put("[ I ] Leaf", "leaf_I");
        itemMappings.put("[ II ] Leaf", "leaf_II");
        itemMappings.put("[ III ] Leaf", "leaf_III");

        // LOWISKO (fishing) category items - POPRAWIONE ID!
        itemMappings.put("[ I ] Algal", "alga_I");
        itemMappings.put("[ II ] Algal", "alga_II");
        itemMappings.put("[ III ] Algal", "alga_III");
        itemMappings.put("[ I ] Shiny Pearl", "pearl_I");  // było shiny_pearl_I, teraz pearl_I
        itemMappings.put("[ II ] Shiny Pearl", "pearl_II");
        itemMappings.put("[ III ] Shiny Pearl", "pearl_III");
        itemMappings.put("[ I ] Heart of the Ocean", "ocean_heart_I");
        itemMappings.put("[ II ] Heart of the Ocean", "ocean_heart_II");
        itemMappings.put("[ III ] Heart of the Ocean", "ocean_heart_III");

        // CURRENCY category items
        itemMappings.put("DrakenMelon", "draken");
        itemMappings.put("Glided Sunflower", "clover");
        itemMappings.put("Andermant", "andermant");
        itemMappings.put("Lockpick", "lockpick");
        itemMappings.put("Jewel Dust", "jewel_dust");
        itemMappings.put("Shiny Dust", "shiny_dust");
        itemMappings.put("Rune Dust", "rune_dust");
    }

    /**
     * Get pouch item ID from display name
     */
    public static String getPouchItemId(String displayName) {
        // Remove color codes if present
        displayName = org.bukkit.ChatColor.stripColor(displayName);

        // First try exact match
        String id = itemMappings.get(displayName);
        if (id != null) {
            return id;
        }

        // Try without stack size (x100, x1000)
        if (displayName.contains(" x")) {
            String baseDisplay = displayName.substring(0, displayName.lastIndexOf(" x"));
            id = itemMappings.get(baseDisplay);
            if (id != null) {
                return id;
            }
        }

        return null;
    }

    /**
     * Add custom mapping
     */
    public static void addMapping(String displayName, String pouchId) {
        itemMappings.put(displayName, pouchId);
    }

    /**
     * Get all mappings for debugging
     */
    public static Map<String, String> getAllMappings() {
        return new HashMap<>(itemMappings);
    }
}
