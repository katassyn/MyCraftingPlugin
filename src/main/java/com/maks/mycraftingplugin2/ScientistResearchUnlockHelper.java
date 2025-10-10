package com.maks.mycraftingplugin2;

import org.bukkit.Bukkit;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Reflection-based bridge to ScientistPlugin research unlocks.
 * Looks for: pl.yourserver.scientistPlugin.api.PublicAPI#isRecipeUnlocked(UUID, String)
 */
public class ScientistResearchUnlockHelper {
    private static final String API_CLASS = "pl.yourserver.scientistPlugin.api.PublicAPI";

    public static boolean isUnlocked(UUID uuid, String key) {
        try {
            Class<?> apiClass = Class.forName(API_CLASS);
            Method m = apiClass.getMethod("isRecipeUnlocked", UUID.class, String.class);
            Object res = m.invoke(null, uuid, key);
            if (res instanceof Boolean) return (Boolean) res;
        } catch (ClassNotFoundException e) {
            // Scientist plugin not present
            return false;
        } catch (Throwable t) {
            Bukkit.getLogger().warning("ScientistResearchUnlockHelper error: " + t.getMessage());
        }
        return false;
    }

    public static Map<String, String> getRecipeTitles() {
        Map<String, String> titles = new LinkedHashMap<>();
        try {
            Class<?> apiClass = Class.forName(API_CLASS);
            Method m = apiClass.getMethod("getResearchTitles");
            Object res = m.invoke(null);
            if (res instanceof Map<?, ?> map) {
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (entry.getKey() == null || entry.getValue() == null) {
                        continue;
                    }
                    titles.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
                }
            }
        } catch (ClassNotFoundException e) {
            // Scientist plugin not present
            return titles;
        } catch (Throwable t) {
            Bukkit.getLogger().warning("ScientistResearchUnlockHelper error: " + t.getMessage());
        }
        return titles;
    }

    public static String getRecipeTitle(String key) {
        if (key == null || key.isEmpty()) {
            return "";
        }
        try {
            Class<?> apiClass = Class.forName(API_CLASS);
            Method m = apiClass.getMethod("getResearchTitle", String.class);
            Object res = m.invoke(null, key);
            if (res != null) {
                return String.valueOf(res);
            }
        } catch (ClassNotFoundException e) {
            return key;
        } catch (Throwable t) {
            Bukkit.getLogger().warning("ScientistResearchUnlockHelper error: " + t.getMessage());
        }
        return key;
    }
}
