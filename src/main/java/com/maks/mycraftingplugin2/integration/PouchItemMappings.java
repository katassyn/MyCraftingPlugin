package com.maks.mycraftingplugin2.integration;

import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Set;

/**
 * Complete mappings between display names and pouch item IDs
 * Based on actual ItemManager.java from IngredientPouchPlugin
 */
public class PouchItemMappings {

    private static final Map<String, String> itemMappings = new HashMap<>();
    private static final Set<String> mineItems = new HashSet<>(Arrays.asList(
        "Hematite",
        "Black Spinel",
        "Black Diamond",
        "Magnetite",
        "Silver",
        "Osmium",
        "Azurite",
        "Tanzanite",
        "Blue Sapphire",
        "Carnelian",
        "Red Spinel",
        "Pigeon Blood Ruby",
        "Pyrite",
        "Yellow Topaz",
        "Yellow Sapphire",
        "Malachite",
        "Peridot",
        "Tropiche Emerald",
        "Danburite",
        "Goshenite",
        "Cerussite"
    ));

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

        // MINE category items
        itemMappings.put("Hematite", "hematite");
        itemMappings.put("Black Spinel", "black_spinel");
        itemMappings.put("Black Diamond", "black_diamond");
        itemMappings.put("Magnetite", "magnetite");
        itemMappings.put("Silver", "silver");
        itemMappings.put("Osmium", "osmium");
        itemMappings.put("Azurite", "azurite");
        itemMappings.put("Tanzanite", "tanzanite");
        itemMappings.put("Blue Sapphire", "blue_sapphire");
        itemMappings.put("Carnelian", "carnelian");
        itemMappings.put("Red Spinel", "red_spinel");
        itemMappings.put("Pigeon Blood Ruby", "pigeon_blood_ruby");
        itemMappings.put("Pyrite", "pyrite");
        itemMappings.put("Yellow Topaz", "yellow_topaz");
        itemMappings.put("Yellow Sapphire", "yellow_sapphire");
        itemMappings.put("Malachite", "malachite");
        itemMappings.put("Peridot", "peridot");
        itemMappings.put("Tropiche Emerald", "tropiche_emerald");
        itemMappings.put("Danburite", "danburite");
        itemMappings.put("Goshenite", "goshenite");
        itemMappings.put("Cerussite", "cerussite");

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

        // FARMING category items
        itemMappings.put("[ I ] Plant Fiber", "farmer_plant_fiber_I");
        itemMappings.put("[ II ] Plant Fiber", "farmer_plant_fiber_II");
        itemMappings.put("[ III ] Plant Fiber", "farmer_plant_fiber_III");
        itemMappings.put("[ I ] Seed Pouch", "farmer_seed_pouch_I");
        itemMappings.put("[ II ] Seed Pouch", "farmer_seed_pouch_II");
        itemMappings.put("[ III ] Seed Pouch", "farmer_seed_pouch_III");
        itemMappings.put("[ I ] Compost Dust", "farmer_compost_dust_I");
        itemMappings.put("[ II ] Compost Dust", "farmer_compost_dust_II");
        itemMappings.put("[ III ] Compost Dust", "farmer_compost_dust_III");
        itemMappings.put("[ I ] Herbal Extract", "farmer_herb_extract_I");
        itemMappings.put("[ II ] Herbal Extract", "farmer_herb_extract_II");
        itemMappings.put("[ III ] Herbal Extract", "farmer_herb_extract_III");
        itemMappings.put("[ I ] Mushroom Spores", "farmer_mushroom_spores_I");
        itemMappings.put("[ II ] Mushroom Spores", "farmer_mushroom_spores_II");
        itemMappings.put("[ III ] Mushroom Spores", "farmer_mushroom_spores_III");
        itemMappings.put("[ I ] Beeswax Chunk", "farmer_beeswax_chunk_I");
        itemMappings.put("[ II ] Beeswax Chunk", "farmer_beeswax_chunk_II");
        itemMappings.put("[ III ] Beeswax Chunk", "farmer_beeswax_chunk_III");
        itemMappings.put("[ I ] Druidic Essence", "farmer_druidic_essence_I");
        itemMappings.put("[ II ] Druidic Essence", "farmer_druidic_essence_II");
        itemMappings.put("[ III ] Druidic Essence", "farmer_druidic_essence_III");
        itemMappings.put("[ I ] Golden Truffle", "farmer_golden_truffle_I");
        itemMappings.put("[ II ] Golden Truffle", "farmer_golden_truffle_II");
        itemMappings.put("[ III ] Golden Truffle", "farmer_golden_truffle_III");
        itemMappings.put("[ I ] Ancient Grain Sheaf", "farmer_ancient_grain_I");
        itemMappings.put("[ II ] Ancient Grain Sheaf", "farmer_ancient_grain_II");
        itemMappings.put("[ III ] Ancient Grain Sheaf", "farmer_ancient_grain_III");

        // BEES category items
        itemMappings.put("[ I ] Honey Bottle", "honey_quality_basic");
        itemMappings.put("[ II ] Honey Bottle", "honey_quality_rare");
        itemMappings.put("[ III ] Honey Bottle", "honey_quality_legendary");
        itemMappings.put("[ I ] Queen Bee", "queen_bee_I");
        itemMappings.put("[ II ] Queen Bee", "queen_bee_II");
        itemMappings.put("[ III ] Queen Bee", "queen_bee_III");
        itemMappings.put("[ I ] Worker Bee", "worker_bee_I");
        itemMappings.put("[ II ] Worker Bee", "worker_bee_II");
        itemMappings.put("[ III ] Worker Bee", "worker_bee_III");
        itemMappings.put("[ I ] Drone Bee", "drone_bee_I");
        itemMappings.put("[ II ] Drone Bee", "drone_bee_II");
        itemMappings.put("[ III ] Drone Bee", "drone_bee_III");
        itemMappings.put("[ I ] Bee Larva", "larva_I");
        itemMappings.put("[ II ] Bee Larva", "larva_II");
        itemMappings.put("[ III ] Bee Larva", "larva_III");
        // Updated MythicMobs display names (aliases for API lookups)
        itemMappings.put("[ I ] Grimmor`s Cindered Cape", "grimmag_frag_I");
        itemMappings.put("[ II ] Grimmor`s Cindered Cape", "grimmag_frag_II");
        itemMappings.put("[ III ] Grimmor`s Cindered Cape", "grimmag_frag_III");
        itemMappings.put("[ I ] Araksha`s Venom Husk", "arachna_frag_I");
        itemMappings.put("[ II ] Araksha`s Venom Husk", "arachna_frag_II");
        itemMappings.put("[ III ] Araksha`s Venom Husk", "arachna_frag_III");
        itemMappings.put("[ I ] Heredorn`s Glacial Armor", "heredur_frag_I");
        itemMappings.put("[ II ] Heredorn`s Glacial Armor", "heredur_frag_II");
        itemMappings.put("[ III ] Heredorn`s Glacial Armor", "heredur_frag_III");
        itemMappings.put("[ I ] Bearok Honey Hide", "bearach_frag_I");
        itemMappings.put("[ II ] Bearok Honey Hide", "bearach_frag_II");
        itemMappings.put("[ III ] Bearok Honey Hide", "bearach_frag_III");
        itemMappings.put("[ I ] Kalith`s Ritual Robe", "khalys_frag_I");
        itemMappings.put("[ II ] Kalith`s Ritual Robe", "khalys_frag_II");
        itemMappings.put("[ III ] Kalith`s Ritual Robe", "khalys_frag_III");
        itemMappings.put("[ I ] Harbinger`s Dragon Skin", "heralds_frag_I");
        itemMappings.put("[ II ] Harbinger`s Dragon Skin", "heralds_frag_II");
        itemMappings.put("[ III ] Harbinger`s Dragon Skin", "heralds_frag_III");
        itemMappings.put("[ I ] Sigrosmar`s Eternal Ice", "sigrismar_frag_I");
        itemMappings.put("[ II ] Sigrosmar`s Eternal Ice", "sigrismar_frag_II");
        itemMappings.put("[ III ] Sigrosmar`s Eternal Ice", "sigrismar_frag_III");
        itemMappings.put("[ I ] M`Edara Stone Scales", "medusa_frag_I");
        itemMappings.put("[ II ] M`Edara Stone Scales", "medusa_frag_II");
        itemMappings.put("[ III ] M`Edara Stone Scales", "medusa_frag_III");
        itemMappings.put("[ I ] Gorgra`s Broken Tooth", "gorga_frag_I");
        itemMappings.put("[ II ] Gorgra`s Broken Tooth", "gorga_frag_II");
        itemMappings.put("[ III ] Gorgra`s Broken Tooth", "gorga_frag_III");
        itemMappings.put("[ I ] Mortrix Sacrificial Bones", "mortis_frag_I");
        itemMappings.put("[ II ] Mortrix Sacrificial Bones", "mortis_frag_II");
        itemMappings.put("[ III ] Mortrix Sacrificial Bones", "mortis_frag_III");
        itemMappings.put("Anderium", "andermant");
        // CURRENCY category items
        itemMappings.put("DrakenMelon", "draken");
        itemMappings.put("Glided Sunflower", "clover");
        itemMappings.put("Andermant", "andermant");
        itemMappings.put("Lockpick", "lockpick");
        itemMappings.put("Jewel Dust", "jewel_dust");
        itemMappings.put("Shiny Dust", "shiny_dust");
        itemMappings.put("Rune Dust", "rune_dust");
        itemMappings.put("Crystal", "crystal");
        
        // Imprinting crystals
        itemMappings.put("Essence Crystal", "essence_crystal");
        itemMappings.put("Power Crystal", "power_crystal");
        itemMappings.put("Primordial Soul Crystal", "primordial_soul_crystal");
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
     * Check if a display name belongs to the MINE category
     */
    public static boolean isMineCategoryItem(String displayName) {
        displayName = org.bukkit.ChatColor.stripColor(displayName);
        return mineItems.contains(displayName);
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

