package com.maks.mycraftingplugin2.integration;

import com.maks.mycraftingplugin2.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class for integrating with IngredientPouchPlugin.
 * Allows checking and removing items from both player inventory and ingredient pouch.
 * 
 * GŁÓWNE POPRAWKI:
 * 1. Bezpieczne usuwanie z ekwipunku - nie próbuje usunąć więcej niż gracz ma
 * 2. Atomowe transakcje - wszystko albo nic, z rollbackiem w razie błędu
 * 3. Lepsze zliczanie przedmiotów z różnymi mnożnikami (x100, x1000)
 * 4. Więcej debugowania dla łatwiejszego śledzenia problemów
 */
public class PouchIntegrationHelper {
    // Static API references
    private static Class<?> pouchAPIClass;
    private static Method getItemAmountMethod;
    private static Method removeItemMethod;
    private static Method updatePouchGUIMethod;
    private static Object pouchAPI;

    // Patterns for stack multipliers
    private static final Pattern STACK_PATTERN_100 = Pattern.compile("\\sx100\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern STACK_PATTERN_1000 = Pattern.compile("\\sx1000\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern STACK_PATTERN_X = Pattern.compile("\\sx(\\d+)\\b", Pattern.CASE_INSENSITIVE);

    // Debug flag
    private static final boolean debugFlag = true; // Ustaw na true dla debugowania

    // Cache for pouch item amounts
    private static final Map<String, Integer> pouchAmountCache = new HashMap<>();
    private static final Map<String, Long> cacheTimestamps = new HashMap<>();
    private static final long CACHE_DURATION = 100; // 100ms cache

    // Static initialization
    static {
        initializeAPI();
    }

    /**
     * Initialize the API by finding the IngredientPouchPlugin class and methods
     */
    private static void initializeAPI() {
        try {
            // Poprawna ścieżka do API
            pouchAPIClass = Class.forName("com.maks.ingredientpouchplugin.api.PouchAPI");

            // Pobierz instancję pluginu
            org.bukkit.plugin.Plugin pouchPlugin = Bukkit.getPluginManager().getPlugin("IngredientPouchPlugin");
            if (pouchPlugin == null) {
                if (debugFlag) {
                    Bukkit.getLogger().warning("[PouchIntegration] IngredientPouchPlugin not found!");
                }
                return;
            }

            // Pobierz metodę getAPI() z głównej klasy pluginu
            Class<?> pluginClass = Class.forName("com.maks.ingredientpouchplugin.IngredientPouchPlugin");
            Method getAPIMethod = pluginClass.getMethod("getAPI");
            Object apiInstance = getAPIMethod.invoke(pouchPlugin);

            // Pobierz metody z API
            getItemAmountMethod = pouchAPIClass.getMethod("getItemQuantity", String.class, String.class);
            removeItemMethod = pouchAPIClass.getMethod("updateItemQuantity", String.class, String.class, int.class);
            updatePouchGUIMethod = pouchAPIClass.getMethod("updatePouchGUI", org.bukkit.entity.Player.class, String.class);

            // Zapisz instancję API
            pouchAPI = apiInstance;

            if (debugFlag) {
                Bukkit.getLogger().info("[PouchIntegration] Successfully initialized IngredientPouch API");
            }
        } catch (Exception e) {
            if (debugFlag) {
                Bukkit.getLogger().warning("[PouchIntegration] Failed to initialize IngredientPouch API: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Check if the IngredientPouch API is available
     * @return true if the API is available
     */
    public static boolean isAPIAvailable() {
        return pouchAPIClass != null && getItemAmountMethod != null && 
               removeItemMethod != null && pouchAPI != null;
    }

    /**
     * Get the total amount of an item in a player's inventory and pouch
     * @param player The player
     * @param item The item to check
     * @return The total amount of the item
     */
    public static int getTotalItemAmount(Player player, ItemStack item) {
        // Get the amount in inventory
        int inventoryAmount = getInventoryItemAmount(player, item);

        // If API is not available, return only inventory amount
        if (!isAPIAvailable()) {
            return inventoryAmount;
        }

        // Get the pouch item ID
        String pouchItemId = getPouchItemId(item);
        if (pouchItemId == null) {
            return inventoryAmount;
        }

        // Get the amount in pouch
        int pouchAmount = getPouchItemAmount(player, pouchItemId);

        if (debugFlag) {
            Bukkit.getLogger().info("[PouchIntegration] Pouch has " + pouchAmount + " units of " + pouchItemId);
            Bukkit.getLogger().info("[PouchIntegration] Total: " + inventoryAmount + " (inv) + " + pouchAmount + " (pouch) = " + (inventoryAmount + pouchAmount));
            Bukkit.getLogger().info("[PouchIntegration] ======================");
        }

        return inventoryAmount + pouchAmount;
    }

    /**
     * Remove items from player (inventory first, then pouch)
     * POPRAWKA: Zwraca nadwyżkę do sakwy
     * @param player The player
     * @param item The item to remove
     * @return RemovalResult containing success status and error details if failed
     */
    public static RemovalResult removeItems(Player player, ItemStack item) {
        int amountToRemove = item.getAmount(); // Ilość stacków do usunięcia
        int stackMultiplier = getStackMultiplier(item);
        int totalUnitsToRemove = amountToRemove * stackMultiplier; // Całkowita ilość pojedynczych jednostek

        if (debugFlag) {
            Bukkit.getLogger().info("[PouchIntegration] Need to remove " + amountToRemove + " stacks (x" + stackMultiplier + ") = " + totalUnitsToRemove + " units of " + getItemName(item));
        }

        // Najpierw sprawdź czy gracz ma wystarczająco
        int totalAvailable = getTotalItemAmount(player, item);
        if (totalAvailable < totalUnitsToRemove) {
            if (debugFlag) {
                Bukkit.getLogger().info("[PouchIntegration] Not enough items! Has: " + totalAvailable + ", needs: " + totalUnitsToRemove);
            }
            return new RemovalResult(false, "Not enough items", totalAvailable, totalUnitsToRemove, item.clone());
        }

        // Zapisz stan ekwipunku przed usuwaniem (dla rollbacku)
        ItemStack[] inventoryBackup = player.getInventory().getContents().clone();

        // Usuń z ekwipunku i zbierz nadwyżkę
        RemovalResult removalResult = removeFromInventoryWithExcess(player, item, totalUnitsToRemove);
        int removedFromInventory = removalResult.removed;
        int excessUnits = removalResult.excess;
        int remainingToRemove = totalUnitsToRemove - removedFromInventory;

        if (debugFlag) {
            Bukkit.getLogger().info("[PouchIntegration] Removed " + removedFromInventory + " from inventory, excess: " + excessUnits + ", still need: " + remainingToRemove);
        }

        // Jeśli trzeba usunąć więcej, usuń z poucha
        if (remainingToRemove > 0 && isAPIAvailable()) {
            String pouchItemId = getPouchItemId(item);
            if (pouchItemId == null) {
                if (debugFlag) {
                    Bukkit.getLogger().warning("[PouchIntegration] Cannot remove from pouch - no item ID found! Rolling back...");
                }
                // Rollback inventory changes
                player.getInventory().setContents(inventoryBackup);
                return new RemovalResult(false, "No pouch item ID found", 0, totalUnitsToRemove, item.clone());
            }

            try {
                // Sprawdź czy pouch ma wystarczająco
                int pouchAmount = getPouchItemAmount(player, pouchItemId);
                if (pouchAmount < remainingToRemove) {
                    if (debugFlag) {
                        Bukkit.getLogger().warning("[PouchIntegration] Pouch doesn't have enough! Has: " + pouchAmount + ", needs: " + remainingToRemove);
                    }
                    // Rollback inventory changes
                    player.getInventory().setContents(inventoryBackup);
                    int totalHas = totalAvailable - removedFromInventory + pouchAmount; // Inventory amount was already removed
                    return new RemovalResult(false, "Not enough items in pouch", totalHas, totalUnitsToRemove, item.clone());
                }

                // Usuń z poucha
                Object result = removeItemMethod.invoke(pouchAPI, player.getUniqueId().toString(), pouchItemId, -remainingToRemove);
                boolean removed = result instanceof Boolean && (Boolean) result;

                if (removed) {
                    // Update cache
                    updatePouchCache(player, pouchItemId, -remainingToRemove);

                    // Zaktualizuj GUI poucha jeśli jest otwarte
                    updatePouchGUI(player);

                    if (debugFlag) {
                        Bukkit.getLogger().info("[PouchIntegration] Successfully removed " + remainingToRemove + " " + pouchItemId + " from pouch");
                    }

                    // Dodaj nadwyżkę do poucha
                    if (excessUnits > 0) {
                        returnExcessToPouch(player, item, excessUnits);
                    }

                    return new RemovalResult(true, null, totalAvailable, totalUnitsToRemove, item.clone());
                } else {
                    if (debugFlag) {
                        Bukkit.getLogger().warning("[PouchIntegration] Failed to remove items from pouch! Rolling back...");
                    }
                    // Rollback inventory changes
                    player.getInventory().setContents(inventoryBackup);
                    return new RemovalResult(false, "Failed to remove items from pouch", totalAvailable, totalUnitsToRemove, item.clone());
                }
            } catch (Exception e) {
                if (debugFlag) {
                    Bukkit.getLogger().warning("[PouchIntegration] Error removing items from pouch: " + e.getMessage());
                    e.printStackTrace();
                }
                // Rollback inventory changes
                player.getInventory().setContents(inventoryBackup);
                return new RemovalResult(false, "Error: " + e.getMessage(), totalAvailable, totalUnitsToRemove, item.clone());
            }
        }

        // Jeśli usunęliśmy wszystko z ekwipunku, dodaj nadwyżkę do poucha
        if (excessUnits > 0) {
            returnExcessToPouch(player, item, excessUnits);
        }

        return new RemovalResult(remainingToRemove <= 0, remainingToRemove > 0 ? "Failed to remove all items" : null, 
                                totalAvailable, totalUnitsToRemove, item.clone());
    }

    /**
     * Get the amount of an item in a player's inventory
     * POPRAWKA: Lepsze zliczanie z uwzględnieniem różnych mnożników
     * @param player The player
     * @param targetItem The item to check
     * @return The amount of the item in the player's inventory
     */
    public static int getInventoryItemAmount(Player player, ItemStack targetItem) {
        int total = 0;
        String baseItemName = getBaseItemName(targetItem);

        if (debugFlag) {
            Bukkit.getLogger().info("[PouchIntegration] === INVENTORY AMOUNT CHECK ===");
            Bukkit.getLogger().info("[PouchIntegration] Looking for: " + baseItemName);
        }

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && areItemsSimilar(item, targetItem)) {
                int stackMultiplier = getStackMultiplier(item);
                int amount = item.getAmount() * stackMultiplier;
                total += amount;

                if (debugFlag) {
                    Bukkit.getLogger().info("[PouchIntegration] Found stack: " + getItemName(item) + " x" + 
                                          item.getAmount() + " (x" + stackMultiplier + " = " + amount + " units)");
                }
            }
        }

        if (debugFlag) {
            Bukkit.getLogger().info("[PouchIntegration] Total in inventory for " + baseItemName + ": " + total + " units");
        }

        return total;
    }

    /**
     * Helper class to return both removed amount and excess, as well as error information
     */
    public static class RemovalResult {
        public final int removed;
        public final int excess;
        public final boolean success;
        public final String errorMessage;
        public final int available;
        public final int required;
        public final ItemStack item;

        public RemovalResult(int removed, int excess) {
            this.removed = removed;
            this.excess = excess;
            this.success = true;
            this.errorMessage = null;
            this.available = 0;
            this.required = 0;
            this.item = null;
        }

        public RemovalResult(boolean success, String errorMessage, int available, int required, ItemStack item) {
            this.removed = 0;
            this.excess = 0;
            this.success = success;
            this.errorMessage = errorMessage;
            this.available = available;
            this.required = required;
            this.item = item;
        }
    }

    /**
     * Remove items from inventory with tracking of excess units
     */
    private static RemovalResult removeFromInventoryWithExcess(Player player, ItemStack targetItem, int unitsToRemove) {
        int totalRemoved = 0;
        int totalActuallyRemoved = 0; // Ile faktycznie usunęliśmy (włącznie z nadwyżką)
        ItemStack[] contents = player.getInventory().getContents();

        for (int i = 0; i < contents.length && totalRemoved < unitsToRemove; i++) {
            ItemStack item = contents[i];

            if (item != null && areItemsSimilar(item, targetItem)) {
                int stackMultiplier = getStackMultiplier(item);
                int unitsInStack = item.getAmount() * stackMultiplier;
                int unitsNeeded = unitsToRemove - totalRemoved;

                if (unitsInStack <= unitsNeeded) {
                    // Usuń cały stack
                    contents[i] = null;
                    totalRemoved += unitsInStack;
                    totalActuallyRemoved += unitsInStack;

                    if (debugFlag) {
                        Bukkit.getLogger().info("[PouchIntegration] Removed entire stack from slot " + i + " (" + unitsInStack + " units)");
                    }
                } else {
                    // Musimy usunąć tylko część, ale w MC nie możemy dzielić stacków z mnożnikami
                    // więc usuwamy cały stack i zwracamy nadwyżkę
                    contents[i] = null;
                    totalRemoved += unitsNeeded; // Tyle potrzebowaliśmy
                    totalActuallyRemoved += unitsInStack; // Tyle faktycznie usunęliśmy

                    if (debugFlag) {
                        Bukkit.getLogger().info("[PouchIntegration] Removed stack from slot " + i + 
                                              " (needed " + unitsNeeded + " but removed " + unitsInStack + " units)");
                    }
                    break; // Mamy już wszystko czego potrzebujemy
                }
            }
        }

        player.getInventory().setContents(contents);
        player.updateInventory();

        // Oblicz rzeczywistą nadwyżkę
        int totalExcess = totalActuallyRemoved - unitsToRemove;

        if (debugFlag) {
            Bukkit.getLogger().info("[PouchIntegration] Summary: needed " + unitsToRemove + 
                                  ", actually removed " + totalActuallyRemoved + 
                                  ", excess: " + totalExcess);
        }

        return new RemovalResult(totalRemoved, totalExcess);
    }

    /**
     * Return excess units to player's pouch
     */
    private static void returnExcessToPouch(Player player, ItemStack item, int excessUnits) {
        if (!isAPIAvailable() || excessUnits <= 0) {
            return;
        }

        String pouchItemId = getPouchItemId(item);
        if (pouchItemId == null) {
            // Jeśli nie można dodać do poucha, zwróć do ekwipunku
            returnExcessToInventory(player, item, excessUnits);
            return;
        }

        try {
            // Dodaj do poucha
            Object result = removeItemMethod.invoke(pouchAPI, player.getUniqueId().toString(), pouchItemId, excessUnits);
            boolean added = result instanceof Boolean && (Boolean) result;

            if (added) {
                // Update cache
                updatePouchCache(player, pouchItemId, excessUnits);

                updatePouchGUI(player);
                player.sendMessage(ChatColor.GRAY + "Returned " + excessUnits + " " + getBaseItemName(item) + " to your pouch.");

                if (debugFlag) {
                    Bukkit.getLogger().info("[PouchIntegration] Returned " + excessUnits + " units to pouch");
                }
            } else {
                // Jeśli nie udało się dodać do poucha, zwróć do ekwipunku
                returnExcessToInventory(player, item, excessUnits);
            }
        } catch (Exception e) {
            if (debugFlag) {
                Bukkit.getLogger().warning("[PouchIntegration] Error returning excess to pouch: " + e.getMessage());
            }
            // Fallback - zwróć do ekwipunku
            returnExcessToInventory(player, item, excessUnits);
        }
    }

    /**
     * Return excess units to player's inventory
     */
    private static void returnExcessToInventory(Player player, ItemStack item, int excessUnits) {
        if (excessUnits <= 0) {
            return;
        }

        // Stwórz nowy przedmiot z odpowiednią ilością
        ItemStack excessItem = item.clone();
        int stackMultiplier = getStackMultiplier(item);

        // Oblicz ile stacków musimy zwrócić
        int stacksToReturn;
        if (stackMultiplier > 1) {
            // Jeśli to przedmiot z mnożnikiem, musimy zwrócić odpowiednią ilość stacków
            stacksToReturn = excessUnits / stackMultiplier;
            if (excessUnits % stackMultiplier > 0) {
                stacksToReturn++; // Zaokrąglamy w górę
            }
        } else {
            // Dla zwykłych przedmiotów po prostu zwracamy tyle jednostek
            stacksToReturn = excessUnits;
        }

        excessItem.setAmount(stacksToReturn);
        player.getInventory().addItem(excessItem);
        player.sendMessage(ChatColor.GRAY + "Returned " + excessUnits + " " + getBaseItemName(item) + " to your inventory.");

        if (debugFlag) {
            Bukkit.getLogger().info("[PouchIntegration] Returned " + excessUnits + " units (" + 
                                  stacksToReturn + " stacks) to inventory");
        }
    }

    /**
     * Remove items from inventory
     * @param player The player
     * @param targetItem The item to remove
     * @param unitsToRemove The number of units to remove
     * @return The number of units actually removed
     */
    public static int removeFromInventory(Player player, ItemStack targetItem, int unitsToRemove) {
        int totalRemoved = 0;
        ItemStack[] contents = player.getInventory().getContents();

        for (int i = 0; i < contents.length && totalRemoved < unitsToRemove; i++) {
            ItemStack item = contents[i];

            if (item != null && areItemsSimilar(item, targetItem)) {
                int stackMultiplier = getStackMultiplier(item);
                int unitsInStack = item.getAmount() * stackMultiplier;
                int unitsNeeded = unitsToRemove - totalRemoved;

                if (unitsInStack <= unitsNeeded) {
                    // Usuń cały stack
                    contents[i] = null;
                    totalRemoved += unitsInStack;

                    if (debugFlag) {
                        Bukkit.getLogger().info("[PouchIntegration] Removed entire stack from slot " + i + " (" + unitsInStack + " units)");
                    }
                } else {
                    // Usuń tylko część stacka
                    int itemsToRemove = (int) Math.ceil((double) unitsNeeded / stackMultiplier);
                    if (itemsToRemove > item.getAmount()) {
                        itemsToRemove = item.getAmount();
                    }

                    int unitsRemoved = itemsToRemove * stackMultiplier;
                    if (unitsRemoved > unitsNeeded) {
                        unitsRemoved = unitsNeeded; // Nie usuwamy więcej niż potrzeba
                    }

                    item.setAmount(item.getAmount() - itemsToRemove);
                    totalRemoved += unitsRemoved;

                    if (debugFlag) {
                        Bukkit.getLogger().info("[PouchIntegration] Removed " + itemsToRemove + " items from slot " + i + 
                                              " (" + unitsRemoved + " units)");
                    }
                }
            }
        }

        player.getInventory().setContents(contents);
        player.updateInventory();

        return totalRemoved;
    }

    /**
     * Get the amount of an item in a player's pouch
     * @param player The player
     * @param itemId The pouch item ID
     * @return The amount of the item in the pouch
     */
    public static int getPouchItemAmount(Player player, String itemId) {
        if (!isAPIAvailable() || itemId == null) {
            return 0;
        }

        // Check cache first
        String cacheKey = player.getUniqueId().toString() + ":" + itemId;
        Long timestamp = cacheTimestamps.get(cacheKey);
        if (timestamp != null && System.currentTimeMillis() - timestamp < CACHE_DURATION) {
            Integer cachedAmount = pouchAmountCache.get(cacheKey);
            if (cachedAmount != null) {
                if (debugFlag) {
                    Bukkit.getLogger().info("[IngredientPouchPlugin] [API-DEBUG] Using cached quantity: " + cachedAmount);
                }
                return cachedAmount;
            }
        }

        try {
            if (debugFlag) {
                Bukkit.getLogger().info("[IngredientPouchPlugin] [API-DEBUG] Getting quantity of " + itemId + " for player " + player.getUniqueId());
            }

            Object result = getItemAmountMethod.invoke(pouchAPI, player.getUniqueId().toString(), itemId);
            int amount = 0;
            if (result instanceof Integer) {
                amount = (Integer) result;
            }

            if (debugFlag) {
                Bukkit.getLogger().info("[IngredientPouchPlugin] [API-DEBUG] Quantity: " + amount);
            }

            // Update cache
            pouchAmountCache.put(cacheKey, amount);
            cacheTimestamps.put(cacheKey, System.currentTimeMillis());

            return amount;
        } catch (Exception e) {
            if (debugFlag) {
                Bukkit.getLogger().warning("[PouchIntegration] Error getting pouch item amount: " + e.getMessage());
            }
            return 0;
        }
    }

    /**
     * Update the cache for a pouch item
     * @param player The player
     * @param itemId The pouch item ID
     * @param change The change in amount
     */
    private static void updatePouchCache(Player player, String itemId, int change) {
        if (itemId == null) {
            return;
        }

        String cacheKey = player.getUniqueId().toString() + ":" + itemId;
        Integer currentAmount = pouchAmountCache.get(cacheKey);
        if (currentAmount != null) {
            int newAmount = currentAmount + change;
            if (newAmount < 0) newAmount = 0;
            pouchAmountCache.put(cacheKey, newAmount);
            cacheTimestamps.put(cacheKey, System.currentTimeMillis());
        }
    }

    /**
     * Clear the cache for a player
     * @param player The player
     */
    public static void clearPouchCache(Player player) {
        String prefix = player.getUniqueId().toString() + ":";
        List<String> keysToRemove = new ArrayList<>();

        for (String key : pouchAmountCache.keySet()) {
            if (key.startsWith(prefix)) {
                keysToRemove.add(key);
            }
        }

        for (String key : keysToRemove) {
            pouchAmountCache.remove(key);
            cacheTimestamps.remove(key);
        }
    }

    /**
     * Update the pouch GUI for a player
     * @param player The player
     */
    private static void updatePouchGUI(Player player) {
        if (!isAPIAvailable()) {
            return;
        }

        try {
            // Sprawdź czy gracz ma otwarte GUI poucha
            if (player.getOpenInventory() != null && 
                player.getOpenInventory().getTitle() != null && 
                player.getOpenInventory().getTitle().contains("Ingredient Pouch")) {

                // Zaktualizuj GUI
                updatePouchGUIMethod.invoke(pouchAPI, player, null);

                if (debugFlag) {
                    Bukkit.getLogger().info("[PouchIntegration] Updated pouch GUI for " + player.getName());
                }
            }
        } catch (Exception e) {
            if (debugFlag) {
                Bukkit.getLogger().warning("[PouchIntegration] Error updating pouch GUI: " + e.getMessage());
            }
        }
    }

    /**
     * Get the pouch item ID for an item
     * @param item The item
     * @return The pouch item ID
     */
    public static String getPouchItemId(ItemStack item) {
        if (item == null) {
            return null;
        }

        String baseName = getBaseItemName(item);
        if (baseName == null) {
            return null;
        }

        // Sprawdź czy mamy mapowanie dla tego przedmiotu
        String pouchId = PouchItemMappings.getPouchItemId(baseName);

        if (pouchId != null) {
            if (debugFlag) {
                Bukkit.getLogger().info("[PouchIntegration] Found mapping: " + baseName + " -> " + pouchId);
            }
            return pouchId;
        }

        // Jeśli nie znaleziono mapowania, zwróć null
        return null;
    }

    /**
     * Get the base name of an item (without stack multiplier)
     * @param item The item
     * @return The base name
     */
    public static String getBaseItemName(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return null;
        }

        String displayName = item.getItemMeta().getDisplayName();

        // Usuń mnożniki ze stacków
        displayName = STACK_PATTERN_100.matcher(displayName).replaceAll("");
        displayName = STACK_PATTERN_1000.matcher(displayName).replaceAll("");
        displayName = STACK_PATTERN_X.matcher(displayName).replaceAll("");

        return displayName.trim();
    }

    /**
     * Check if an item has a stack multiplier
     * @param item The item
     * @return true if the item has a stack multiplier
     */
    public static boolean hasStackMultiplier(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }

        String displayName = item.getItemMeta().getDisplayName();

        // Sprawdź czy nazwa zawiera mnożnik
        Matcher matcher100 = STACK_PATTERN_100.matcher(displayName);
        Matcher matcher1000 = STACK_PATTERN_1000.matcher(displayName);
        Matcher matcherX = STACK_PATTERN_X.matcher(displayName);

        return matcher100.find() || matcher1000.find() || matcherX.find();
    }

    /**
     * Get the stack multiplier for an item
     * @param item The item
     * @return The stack multiplier (1, 100, 1000, etc.)
     */
    public static int getStackMultiplier(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return 1;
        }

        String displayName = item.getItemMeta().getDisplayName();

        // Sprawdź mnożniki
        Matcher matcher100 = STACK_PATTERN_100.matcher(displayName);
        if (matcher100.find()) {
            return 100;
        }

        Matcher matcher1000 = STACK_PATTERN_1000.matcher(displayName);
        if (matcher1000.find()) {
            return 1000;
        }

        Matcher matcherX = STACK_PATTERN_X.matcher(displayName);
        if (matcherX.find()) {
            try {
                return Integer.parseInt(matcherX.group(1));
            } catch (NumberFormatException e) {
                return 1;
            }
        }

        return 1;
    }

    /**
     * Get the actual amount of an item (amount * stack multiplier)
     * @param item The item
     * @return The actual amount
     */
    public static int getActualItemAmount(ItemStack item) {
        if (item == null) {
            return 0;
        }

        int stackMultiplier = getStackMultiplier(item);
        int amount = item.getAmount();

        return amount * stackMultiplier;
    }

    /**
     * Get the name of an item
     * @param item The item
     * @return The name of the item
     */
    public static String getItemName(ItemStack item) {
        if (item == null) {
            return "null";
        }

        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }

        return item.getType().toString();
    }

    /**
     * Check if two items are similar (same base item, ignoring stack multiplier)
     * @param item1 The first item
     * @param item2 The second item
     * @return true if the items are similar
     */
    public static boolean areItemsSimilar(ItemStack item1, ItemStack item2) {
        if (item1 == null || item2 == null) {
            return false;
        }

        // Sprawdź typ
        if (item1.getType() != item2.getType()) {
            return false;
        }

        // Jeśli oba nie mają meta, są podobne
        if (!item1.hasItemMeta() && !item2.hasItemMeta()) {
            return true;
        }

        // Jeśli tylko jeden ma meta, nie są podobne
        if (!item1.hasItemMeta() || !item2.hasItemMeta()) {
            return false;
        }

        ItemMeta meta1 = item1.getItemMeta();
        ItemMeta meta2 = item2.getItemMeta();

        // Sprawdź czy oba mają nazwę
        if (!meta1.hasDisplayName() && !meta2.hasDisplayName()) {
            return true;
        }

        // Jeśli tylko jeden ma nazwę, nie są podobne
        if (!meta1.hasDisplayName() || !meta2.hasDisplayName()) {
            return false;
        }

        // Porównaj bazowe nazwy (bez mnożników)
        String baseName1 = getBaseItemName(item1);
        String baseName2 = getBaseItemName(item2);

        return baseName1 != null && baseName2 != null && baseName1.equals(baseName2);
    }

    /**
     * Send debug information about an item to a player
     * @param player The player
     * @param item The item
     */
    public static void sendDebugInfo(Player player, ItemStack item) {
        if (item == null) {
            player.sendMessage(ChatColor.RED + "Item is null");
            return;
        }

        player.sendMessage(ChatColor.YELLOW + "=== Item Debug Info ===");
        player.sendMessage(ChatColor.YELLOW + "Type: " + item.getType());
        player.sendMessage(ChatColor.YELLOW + "Amount: " + item.getAmount());

        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            player.sendMessage(ChatColor.YELLOW + "Has Meta: Yes");

            if (meta.hasDisplayName()) {
                player.sendMessage(ChatColor.YELLOW + "Display Name: " + meta.getDisplayName());
            } else {
                player.sendMessage(ChatColor.YELLOW + "Display Name: None");
            }

            if (meta.hasLore()) {
                player.sendMessage(ChatColor.YELLOW + "Lore:");
                for (String line : meta.getLore()) {
                    player.sendMessage(ChatColor.GRAY + " - " + line);
                }
            } else {
                player.sendMessage(ChatColor.YELLOW + "Lore: None");
            }
        } else {
            player.sendMessage(ChatColor.YELLOW + "Has Meta: No");
        }

        player.sendMessage(ChatColor.YELLOW + "Base Name: " + getBaseItemName(item));
        player.sendMessage(ChatColor.YELLOW + "Stack Multiplier: " + getStackMultiplier(item));
        player.sendMessage(ChatColor.YELLOW + "Actual Amount: " + getActualItemAmount(item));
        player.sendMessage(ChatColor.YELLOW + "Pouch Item ID: " + getPouchItemId(item));
        player.sendMessage(ChatColor.YELLOW + "====================");
    }
}
