package com.maks.mycraftingplugin2;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TemporaryData {

    // Mapa przechowująca aktualne strony dla graczy w danych kategoriach
    private static Map<UUID, Map<String, Integer>> playerPages = new HashMap<>();

    public static void setPage(UUID playerUUID, String category, int page) {
        playerPages.computeIfAbsent(playerUUID, k -> new HashMap<>()).put(category, page);
    }

    public static int getPage(UUID playerUUID, String category) {
        return playerPages.getOrDefault(playerUUID, new HashMap<>()).getOrDefault(category, 0);
    }

    public static void removePage(UUID playerUUID, String category) {
        if (playerPages.containsKey(playerUUID)) {
            playerPages.get(playerUUID).remove(category);
        }
    }
    private static Map<UUID, Map<String, Object>> playerData = new HashMap<>();

    public static void setPlayerData(UUID uuid, String key, Object value) {
        playerData.computeIfAbsent(uuid, k -> new HashMap<>()).put(key, value);
    }

    public static Object getPlayerData(UUID uuid, String key) {
        if (playerData.containsKey(uuid)) {
            return playerData.get(uuid).get(key);
        }
        return null;
    }

    public static void removePlayerData(UUID uuid, String key) {
        if (playerData.containsKey(uuid)) {
            playerData.get(uuid).remove(key);
        }
    }
    // Dane dotyczące szansy na sukces i kosztu
    private static Map<UUID, Double> successChances = new HashMap<>();
    private static Map<UUID, Double> costs = new HashMap<>();

    public static void setSuccessChance(UUID playerUUID, double value) {
        successChances.put(playerUUID, value);
    }

    public static double getSuccessChance(UUID playerUUID) {
        return successChances.getOrDefault(playerUUID, 100.0);
    }

    public static void removeSuccessChance(UUID playerUUID) {
        successChances.remove(playerUUID);
    }

    public static void setCost(UUID playerUUID, double value) {
        costs.put(playerUUID, value);
    }

    public static double getCost(UUID playerUUID) {
        return costs.getOrDefault(playerUUID, 0.0);
    }

    public static void removeCost(UUID playerUUID) {
        costs.remove(playerUUID);
    }

    // Required recipe for Conjurej shop crafting
    private static Map<UUID, String> requiredRecipes = new HashMap<>();

    public static void setRequiredRecipe(UUID playerUUID, String recipe) {
        if (recipe == null) {
            requiredRecipes.remove(playerUUID);
        } else {
            requiredRecipes.put(playerUUID, recipe);
        }
    }

    public static String getRequiredRecipe(UUID playerUUID) {
        return requiredRecipes.get(playerUUID);
    }

    public static void removeRequiredRecipe(UUID playerUUID) {
        requiredRecipes.remove(playerUUID);
    }

    // Przechowywanie ostatnio odwiedzonej kategorii przez gracza
    private static Map<UUID, String> lastCategory = new HashMap<>();

    public static void setLastCategory(UUID playerUUID, String category) {
        lastCategory.put(playerUUID, category);
    }

    public static String getLastCategory(UUID playerUUID) {
        return lastCategory.getOrDefault(playerUUID, "");
    }

    public static void removeLastCategory(UUID playerUUID) {
        lastCategory.remove(playerUUID);
    }

    // Inne metody i zmienne używane w pluginie
}
