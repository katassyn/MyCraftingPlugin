package com.maks.mycraftingplugin2;

import com.maks.mycraftingplugin2.integration.PouchIntegrationHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import com.maks.mycraftingplugin2.events.CustomCraftEvent;
import org.bukkit.permissions.PermissionAttachment;

import java.sql.*;
import java.util.*;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class MenuListener implements Listener {

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();

        // Debugging flag
        int debuggingFlag = 0; // Set to 0 to disable debug

        if (debuggingFlag == 1) {
            Bukkit.getLogger().info("[MenuListener] InventoryCloseEvent - Title: " + title + 
                              ", Player: " + player.getName());
        }

        // Handle Jewels Crushing menu closure
        if (title.equals("Jewels Crushing")) {
            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[MenuListener] Handling Jewels Crushing menu close for: " + player.getName());
            }

            // Return all items from crushing slots to player
            JewelsCrushingMenu.returnItemsToPlayer(player, event.getInventory());
        }
        // Handle Gem Crushing menu closure
        else if (title.equals("Gem Crushing")) {
            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[MenuListener] Handling Gem Crushing menu close for: " + player.getName());
            }

            GemCrushingMenu.returnItemsToPlayer(player, event.getInventory());
        }
        // Handle Rune Crushing menu closure
        else if (title.equals("Rune Crushing")) {
            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[MenuListener] Handling Rune Crushing menu close for: " + player.getName());
            }

            RunesCrushingMenu.returnItemsToPlayer(player, event.getInventory());
        }

        // Handle other menus that might need item protection
        else if (title.equals("Add New Recipe") || title.equals("Edit Recipe")) {
            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[MenuListener] Handling Recipe menu close for: " + player.getName());
            }

            String state = ChatListener.getPlayerState(player.getUniqueId());
            if ("entering_success_chance".equals(state) || "entering_cost".equals(state)) {
                // Save GUI state only when expecting chat input
                AddRecipeMenu.saveGuiState(player.getUniqueId(), event.getInventory().getContents());
            } else {
                // Otherwise clear saved state to avoid carrying over items
                AddRecipeMenu.removeGuiState(player.getUniqueId());
            }
        }

        else if (title.equals("Add Emilia Item") || title.equals("Edit Emilia Item")) {
            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[MenuListener] Handling Emilia item menu close for: " + player.getName());
            }

            String state = ChatListener.getPlayerState(player.getUniqueId());
            if ("entering_cost".equals(state) || "entering_emilia_daily_limit".equals(state)) {
                EmiliaAddItemMenu.saveGuiState(player.getUniqueId(), event.getInventory().getContents());
            } else {
                EmiliaAddItemMenu.removeGuiState(player.getUniqueId());
            }
        }

        else if (title.equals("Add Zumpe Item") || title.equals("Edit Zumpe Item")) {
            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[MenuListener] Handling Zumpe item menu close for: " + player.getName());
            }

            String state = ChatListener.getPlayerState(player.getUniqueId());
            if ("entering_cost".equals(state) || "entering_zumpe_daily_limit".equals(state)) {
                ZumpeAddItemMenu.saveGuiState(player.getUniqueId(), event.getInventory().getContents());
            } else {
                ZumpeAddItemMenu.removeGuiState(player.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {

        Inventory clickedInventory = event.getClickedInventory();
        Inventory topInventory = event.getView().getTopInventory();
        String title = event.getView().getTitle();
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        // Check if the top inventory is null
        if (topInventory == null || title == null) {
            return;
        }

        // Check if the clicked inventory is null
        if (clickedInventory == null) {
            return;
        }

        // Check if the clicked inventory is one of our plugin's inventories
        boolean isOurInventory = isOurPluginInventory(title);

        // If the inventory is not ours, do not process the event
        if (!isOurInventory) {
            return;
        }

        // From here on, we are dealing with our plugin's inventory
        event.setCancelled(true); // Prevent default behavior

        // Now process the event based on the inventory title
        String itemName = null;
        if (clickedItem != null && clickedItem.hasItemMeta()) {
            itemName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
        }

        // Handle different inventories based on the title
        if (title.equals("Add New Recipe") || title.equals("Edit Recipe")) {
            handleAddEditRecipe(event, player, clickedItem, itemName, title);
        } else if (title.equals("Crafting Categories")) {
            handleCraftingCategories(event, player, clickedItem, itemName);
        } else if (title.equals("Upgrade Levels") || title.equals("Edit Upgrade Levels")) {
            handleUpgradeLevels(event, player, clickedItem, itemName, title);
        } else if (title.startsWith("Difficulty for ") || title.startsWith("Edit Difficulty for ")) {
            handleDifficultyMenu(event, player, clickedItem, itemName, title);
        } else if (title.startsWith("Category: ") || title.startsWith("Edit Category: ")) {
            handleCategoryMenu(event, player, clickedItem, itemName, title);
        } else if (title.equals("Edit Categories")) {
            handleEditCategories(event, player, clickedItem, itemName);
        } else if (title.equals("Crafting Scheme")) {
            handleCraftingScheme(event, player, clickedItem, itemName);
        } else if (title.equals("Alchemy Menu")) {
            handleAlchemyMenuClick(player, itemName);
        } else if (title.equals("Edit Alchemy Menu")) {
            handleEditAlchemyMenuClick(player, itemName);
        } else if (title.equals("Jeweler Menu")) {
            handleJewelerMenuClick(player, itemName);
        } else if (title.equals("Edit Jeweler Menu")) {
            handleEditJewelerMenuClick(player, itemName);
        } else if (title.equals("Jewels Crushing")) {
            handleJewelsCrushingMenuClick(event, player, clickedItem, itemName);
        } else if (title.equals("Gemologist Menu")) {
            handleGemologistMenuClick(player, itemName);
        } else if (title.equals("Edit Gemologist Menu")) {
            handleEditGemologistMenuClick(player, itemName);
        } else if (title.equals("Gem Crushing")) {
            handleGemCrushingMenuClick(event, player, clickedItem, itemName);
        } else if (title.equals("Runemaster Menu")) {
            handleRunemasterMenuClick(player, itemName);
        } else if (title.equals("Edit Runemaster Menu")) {
            handleEditRunemasterMenuClick(player, itemName);
        } else if (title.equals("Rune Crushing")) {
            handleRuneCrushingMenuClick(event, player, clickedItem, itemName);
        } else if (title.equals("Emilia Shop")) {
            handleEmiliaMainMenu(player, clickedItem, itemName);
        } else if (title.equals("Edit Emilia Shop")) {
            handleEditEmiliaMainMenu(player, clickedItem, itemName);
        } else if (title.equals("Emilia - Shop")) {
            handleEmiliaShopMenu(player, clickedItem, itemName);
        } else if (title.equals("Edit Emilia - Shop")) {
            handleEditEmiliaShopMenu(player, clickedItem, itemName);
        } else if (title.equals("Emilia - Event Shop")) {
            handleEmiliaEventShopMenu(player, clickedItem, itemName);
        } else if (title.equals("Edit Emilia - Event Shop")) {
            handleEditEmiliaEventShopMenu(player, clickedItem, itemName);
        } else if (title.startsWith("Emilia: Shop") || title.startsWith("Emilia: Event Shop")) {
            handleEmiliaItemsMenu(event, player, clickedItem, itemName, title);
        } else if (title.startsWith("Edit Emilia: Shop") || title.startsWith("Edit Emilia: Event Shop")) {
            handleEditEmiliaItemsMenu(event, player, clickedItem, itemName, title);
        } else if (title.equals("Add Emilia Item") || title.equals("Edit Emilia Item")) {
            handleEmiliaAddEditItemMenu(event, player, clickedItem, itemName, title);
        } else if (title.equals("Zumpe Shop")) {
            handleZumpeShopMenu(event, player, clickedItem, itemName);
        } else if (title.equals("Edit Zumpe Shop")) {
            handleEditZumpeShopMenu(event, player, clickedItem, itemName);
        } else if (title.equals("Add Zumpe Item") || title.equals("Edit Zumpe Item")) {
            handleZumpeAddEditItemMenu(event, player, clickedItem, itemName, title);
        } else if (title.equals("Emilia Exchange")) {
            handleEmiliaConfirmationMenu(event, player, clickedItem, itemName);
        } else if (title.equals("Zumpe Exchange")) {
            handleZumpeConfirmationMenu(event, player, clickedItem, itemName);
        }

    }

    // Method to check if the inventory title matches our plugin's inventories
    private boolean isOurPluginInventory(String title) {
        return title.equals("Add New Recipe")
                || title.equals("Edit Recipe")
                || title.equals("Crafting Categories")
                || title.equals("Upgrade Levels")
                || title.startsWith("Difficulty for ")
                || title.startsWith("Category: ")
                || title.equals("Edit Categories")
                || title.equals("Edit Upgrade Levels")
                || title.startsWith("Edit Difficulty for ")
                || title.startsWith("Edit Category: ")
                || title.equals("Crafting Scheme")
                || title.equals("Alchemy Menu")
                || title.equals("Edit Alchemy Menu")
                || title.equals("Jeweler Menu")
                || title.equals("Edit Jeweler Menu")
                || title.equals("Jewels Crushing")
                || title.equals("Gemologist Menu")
                || title.equals("Edit Gemologist Menu")
                || title.equals("Gem Crushing")
                || title.equals("Runemaster Menu")
                || title.equals("Edit Runemaster Menu")
                || title.equals("Rune Crushing")
                || title.equals("Emilia Shop")
                || title.equals("Edit Emilia Shop")
                || title.equals("Emilia - Shop")
                || title.equals("Edit Emilia - Shop")
                || title.equals("Emilia - Event Shop")
                || title.equals("Edit Emilia - Event Shop")
                || title.startsWith("Emilia: Shop")
                || title.startsWith("Emilia: Event Shop")
                || title.startsWith("Edit Emilia: Shop")
                || title.startsWith("Edit Emilia: Event Shop")
                || title.equals("Add Emilia Item")
                || title.equals("Edit Emilia Item")
                || title.equals("Zumpe Shop")
                || title.equals("Edit Zumpe Shop")
                || title.equals("Add Zumpe Item")
                || title.equals("Edit Zumpe Item")
                || title.equals("Emilia Exchange")
                || title.equals("Zumpe Exchange");
    }
    // Method to handle "Add New Recipe" and "Edit Recipe" inventories
// Klasa: MenuListener
// Fragment kodu z metodą handleAddEditRecipe
    private void handleAddEditRecipe(InventoryClickEvent event, Player player,
                                     ItemStack clickedItem, String itemName, String title) {
        Inventory inv = event.getInventory();
        int slot = event.getRawSlot();
        boolean isTopInventory = (event.getView().getTopInventory() == event.getClickedInventory());

        if (isTopInventory) {
            // Jeśli gracz kliknął w górną część GUI
            // 1. Sprawdzamy, czy gracz kliknął placeholder "Required Item X" lub "Result Item".
            if (clickedItem != null
                    && itemName != null
                    && (itemName.startsWith("Required Item") || itemName.equalsIgnoreCase("Result Item"))) {

                // Dodatkowo sprawdzamy, czy to rodzaj szkła placeholdera
                // (dla slotu 13 może być LIGHT_BLUE_STAINED_GLASS_PANE,
                // dla slotów 0–9 GRAY_STAINED_GLASS_PANE, itp.)
                Material mat = clickedItem.getType();
                if (mat == Material.GRAY_STAINED_GLASS_PANE || mat == Material.LIGHT_BLUE_STAINED_GLASS_PANE) {
                    // Usuwamy placeholder ze slotu
                    inv.setItem(slot, null);
                    // Nie pozwalamy przenieść do eq
                    event.setCancelled(true);
                    return;
                }
            }

            // 2. Jeśli to sloty 0–9 (wymagane przedmioty) lub slot 13 (przedmiot wynikowy),
            //    pozwalamy wstawić tam dowolny item z eq gracza.
            if ((slot >= 0 && slot <= 9) || slot == 13) {
                event.setCancelled(false);
            } else {
                // 3. Obsługa przycisków (success_chance, cost, save, delete, back, itd.)
                if (itemName == null) return;

                switch (slot) {
                    case 20:
                        AddRecipeMenu.saveGuiState(player.getUniqueId(), inv.getContents());
                        ChatListener.setPlayerState(player.getUniqueId(), "entering_success_chance");
                        player.sendMessage(ChatColor.YELLOW + "Please enter the success chance (e.g. 55% or 0.5%).");
                        player.closeInventory();
                        break;

                    case 21:
                        AddRecipeMenu.saveGuiState(player.getUniqueId(), inv.getContents());
                        ChatListener.setPlayerState(player.getUniqueId(), "entering_cost");
                        player.sendMessage(ChatColor.YELLOW + "Please enter the cost (e.g. 500, 500k, 1kk).");
                        player.closeInventory();
                        break;


                    case 22:
                        // "Save"
                        if (itemName.equals("Save")) {
                            if (title.equals("Add New Recipe")) {
                                saveRecipe(player, inv);
                                AddRecipeMenu.removeCategory(player.getUniqueId());
                            } else if (title.equals("Edit Recipe")) {
                                updateRecipe(player, inv);
                                EditRecipeMenu.removeRecipeId(player.getUniqueId());
                            }
                        }
                        break;

                    case 23:
                        // "Delete" (tylko w Edit Recipe)
                        if (title.equals("Edit Recipe") && itemName.equals("Delete")) {
                            deleteRecipe(player);
                            EditRecipeMenu.removeRecipeId(player.getUniqueId());
                        }
                        break;

                    case 24:
                        // "Back"
                        String category = AddRecipeMenu.getCategory(player.getUniqueId());
                        if (category != null) {
                            if (player.hasPermission("mycraftingplugin.edit")) {
                                CategoryMenu.openEditor(player, category, 0);
                            } else {
                                CategoryMenu.open(player, category, 0);
                            }
                            AddRecipeMenu.removeCategory(player.getUniqueId());
                        } else {
                            player.closeInventory();
                        }
                        break;

                    default:
                        break;
                }
            }
        } else {
            // Kliknięto w ekwipunek gracza
            event.setCancelled(false);
        }
    }
    // Method to handle "Crafting Categories" inventory
    private void handleCraftingCategories(InventoryClickEvent event, Player player, ItemStack clickedItem, String itemName) {
        if (itemName == null) return;

        switch (itemName) {
            case "Upgrading":
                UpgradeMenu.open(player);
                break;
            case "Keys":
            case "Lootboxes":
                CategoryMenu.open(player, itemName, 0);
                break;
            default:
                break;
        }
    }
    // Method to handle "Upgrade Levels" inventories
    private void handleUpgradeLevels(InventoryClickEvent event, Player player, ItemStack clickedItem, String itemName, String title) {
        if (itemName == null) return;

        if (itemName.equals("Back")) {
            if (title.equals("Upgrade Levels")) {
                MainMenu.open(player);
            } else if (title.equals("Edit Upgrade Levels")) {
                MainMenu.openEditor(player);
            }
        } else if (itemName.startsWith("q")) {
            if (title.equals("Upgrade Levels")) {
                DifficultyMenu.open(player, itemName); // itemName is "q1" to "q10"
            } else if (title.equals("Edit Upgrade Levels")) {
                DifficultyMenu.openEditor(player, itemName);
            }
        }
    }
    // Method to handle "Difficulty" menus
    private void handleDifficultyMenu(InventoryClickEvent event, Player player, ItemStack clickedItem, String itemName, String title) {
        if (itemName == null) return;

        if (itemName.equals("Back")) {
            if (title.startsWith("Difficulty for ")) {
                UpgradeMenu.open(player);
            } else if (title.startsWith("Edit Difficulty for ")) {
                UpgradeMenu.openEditor(player);
            }
        } else if (itemName.equals("Infernal") || itemName.equals("Hell") || itemName.equals("Blood")) {
            String qLevel = title.substring(title.lastIndexOf(" ") + 1);

            // Create unique category name
            String category = qLevel + "_" + itemName.toLowerCase();

            // Store last category for the player
            TemporaryData.setLastCategory(player.getUniqueId(), category);

            // Open category menu
            if (title.startsWith("Difficulty for ")) {
                CategoryMenu.open(player, category, 0);
            } else if (title.startsWith("Edit Difficulty for ")) {
                CategoryMenu.openEditor(player, category, 0);
            }
        }
    }
    // Klasa: MenuListener
// Fragment kodu z metodą handleCategoryMenu
    private void handleCategoryMenu(InventoryClickEvent event, Player player,
                                    ItemStack clickedItem, String itemName, String title) {
        Inventory inv = event.getInventory();
        int slot = event.getRawSlot();
        boolean isTopInventory = (event.getView().getTopInventory() == event.getClickedInventory());

        if (itemName == null) return;
        // Wydobywamy nazwę kategorii z tytułu "Category: ..." lub "Edit Category: ..."
        String category = title.contains(": ") ? title.split(": ")[1] : "";

        // Sprawdzamy, czy jesteśmy w trybie edycji
        boolean isEditMode = title.startsWith("Edit Category: ");

        if (isTopInventory) {
            // W trybie edycji pozwalamy przenosić itemy w slotach 0–44 (layout).
            if (isEditMode && slot >= 0 && slot <= 44) {
                event.setCancelled(false);
            }

            if (itemName.equals("Back")) {
                // Jeżeli to kategoria Alchemii
                if (category.equalsIgnoreCase("alchemy_shop")
                        || category.equalsIgnoreCase("tonics")
                        || category.equalsIgnoreCase("potions")
                        || category.equalsIgnoreCase("physic")) {

                    // W zależności, czy to tryb edycji czy normalny
                    if (isEditMode) {
                        AlchemyMainMenu.openEditor(player);
                    } else {
                        AlchemyMainMenu.open(player);
                    }
                }
                // Jeżeli to kategoria "q" (np. q1_blood itp.)
                else if (category.startsWith("q")) {
                    String qLevel = category.split("_")[0];
                    if (isEditMode) {
                        DifficultyMenu.openEditor(player, qLevel);
                    } else {
                        DifficultyMenu.open(player, qLevel);
                    }
                }
                // Jeżeli to kategoria Jewelera
                else if (category.equalsIgnoreCase("jewels_upgrading")) {
                    if (isEditMode) {
                        JewelerMainMenu.openEditor(player);
                    } else {
                        JewelerMainMenu.open(player);
                    }
                }
                // Jeżeli to kategoria Gemologa
                else if (category.equalsIgnoreCase("gems_crafting")) {
                    if (isEditMode) {
                        GemologistMainMenu.openEditor(player);
                    } else {
                        GemologistMainMenu.open(player);
                    }
                }
                // Jeżeli to kategoria Runemastera
                else if (category.equalsIgnoreCase("runes_upgrading")) {
                    if (isEditMode) {
                        RunemasterMainMenu.openEditor(player);
                    } else {
                        RunemasterMainMenu.open(player);
                    }
                }
                // Jeżeli to kategoria Mine Shop
                else if (category.equalsIgnoreCase("mine_shop")) {
                    player.closeInventory();
                    boolean hadPerm = player.hasPermission("minesystemplugin.mine");
                    if (hadPerm) {
                        player.performCommand("mine");
                    } else {
                        PermissionAttachment attachment = player.addAttachment(Main.getInstance());
                        attachment.setPermission("minesystemplugin.mine", true);
                        player.performCommand("mine");
                        player.removeAttachment(attachment);
                    }
                }
                // Jeżeli to kategoria Fisherman Shop
                else if (category.equalsIgnoreCase("fisherman_shop")) {
                    player.closeInventory();
                    boolean hadPerm = player.hasPermission("fishing.use");
                    if (hadPerm) {
                        player.performCommand("fishing");
                    } else {
                        PermissionAttachment attachment = player.addAttachment(Main.getInstance());
                        attachment.setPermission("fishing.use", true);
                        player.performCommand("fishing");
                        player.removeAttachment(attachment);
                    }
                }

                // Inne kategorie (keys, lootboxes, itd.)
                else {
                    if (isEditMode) {
                        MainMenu.openEditor(player);
                    } else {
                        MainMenu.open(player);
                    }
                }
            }
            else if (itemName.equals("Next Page")) {
                int currentPage = TemporaryData.getPage(player.getUniqueId(), category);
                TemporaryData.setPage(player.getUniqueId(), category, currentPage + 1);
                if (isEditMode) {
                    CategoryMenu.openEditor(player, category, currentPage + 1);
                } else {
                    CategoryMenu.open(player, category, currentPage + 1);
                }
            }
            else if (itemName.equals("Previous Page")) {
                int currentPage = TemporaryData.getPage(player.getUniqueId(), category);
                TemporaryData.setPage(player.getUniqueId(), category, currentPage - 1);
                if (isEditMode) {
                    CategoryMenu.openEditor(player, category, currentPage - 1);
                } else {
                    CategoryMenu.open(player, category, currentPage - 1);
                }
            }
            else if (isEditMode && itemName.equals("Add Recipe")) {
                // Tylko w trybie edycji
                if (!player.hasPermission("mycraftingplugin.add")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to add recipes.");
                    return;
                }
                AddRecipeMenu.open(player, category);
            }
            else {
                // Kliknięto w przedmiot (recepturę)
                ItemMeta meta = clickedItem.getItemMeta();
                if (meta == null || !meta.hasLore()) {
                    player.sendMessage(ChatColor.RED + "Invalid recipe.");
                    return;
                }

                // Szukamy ID receptury w lore
                int recipeId = -1;
// First try to get ID from persistent data
                NamespacedKey key = new NamespacedKey(Main.getInstance(), "recipe_id");
                if (meta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
                    recipeId = meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
                } else {
                    // Fall back to lore parsing for backward compatibility
                    List<String> lore = meta.getLore();
                    for (String line : lore) {
                        String clean = ChatColor.stripColor(line);
                        if (clean.startsWith("Recipe ID: ")) {
                            String idStr = clean.replace("Recipe ID: ", "").trim();
                            recipeId = Integer.parseInt(idStr);
                            break;
                        }
                    }
                }

                if (recipeId == -1) {
                    player.sendMessage(ChatColor.RED + "Invalid recipe.");
                    return;
                }

                // W zależności czy edytujemy, czy oglądamy
                if (isEditMode) {
                    // Otwórz menu edycji receptury
                    EditRecipeMenu.open(player, recipeId);
                } else {
                    // Otwórz UnifiedConfirmationMenu
                    TemporaryData.setLastCategory(player.getUniqueId(), category);
                    UnifiedConfirmationMenu.open(player, recipeId, UnifiedConfirmationMenu.ShopType.CRAFTING);
                }
            }

            // Zapisujemy layout tylko w trybie edycji (po drobnej zwłoce)
            if (isEditMode) {
                Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                    saveCategoryLayout(inv, category);
                }, 1L);
            }
        } else {
            // Kliknięto w ekwipunek gracza
            if (isEditMode) {
                // Zezwalamy na przesuwanie w ekwipunku gracza
                event.setCancelled(false);
            }
        }
    }
    // Method to handle "Edit Categories" inventory
    private void handleEditCategories(InventoryClickEvent event, Player player, ItemStack clickedItem, String itemName) {
        if (itemName == null) return;

        switch (itemName) {
            case "Upgrading":
                UpgradeMenu.openEditor(player);
                break;
            case "Keys":
            case "Lootboxes":
                CategoryMenu.openEditor(player, itemName, 0);
                break;
            default:
                break;
        }
    }
    private void handleCraftingScheme(InventoryClickEvent event, Player player, ItemStack clickedItem, String itemName) {
        if (itemName == null) return;

        if (itemName.equals("Back")) {
            // Back button logic
            String category = TemporaryData.getLastCategory(player.getUniqueId());
            int page = TemporaryData.getPage(player.getUniqueId(), category);
            CategoryMenu.open(player, category, page);
        } else if (itemName.equals("Craft")) {
            // Perform crafting
            performCrafting(player, event.getInventory());
        }
    }

    // Metoda performCrafting
    private void performCrafting(Player player, Inventory inv) {
        try {
            // Pobierz przedmiot wynikowy ze slotu 22
            ItemStack resultItem = inv.getItem(22);
            if (resultItem == null || resultItem.getType() == Material.AIR) {
                player.sendMessage(ChatColor.RED + "Invalid recipe.");
                return;
            }

            ItemMeta meta = resultItem.getItemMeta();
            if (meta == null) {
                player.sendMessage(ChatColor.RED + "Recipe not found.");
                return;
            }

            // Pobierz recipe ID z PersistentDataContainer
            NamespacedKey key = new NamespacedKey(Main.getInstance(), "recipe_id");
            Integer recipeId = meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);

            if (recipeId == null) {
                player.sendMessage(ChatColor.RED + "Recipe not found.");
                return;
            }

            if (Main.getInstance().getConfig().getInt("debug", 0) == 1) {
                Bukkit.getLogger().info("[Crafting] Processing recipe ID: " + recipeId);
            }

            try (Connection conn = Main.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM recipes WHERE id = ?")) {

                ps.setInt(1, recipeId);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    // Sprawdź czy gracz ma wymagane przedmioty
                    boolean hasItems = true;
                    Map<Integer, ItemStack> requiredItems = new HashMap<>();

                    for (int i = 0; i < 10; i++) {
                        String itemData = rs.getString("required_item_" + (i + 1));
                        if (itemData != null) {
                            ItemStack requiredItem = ItemStackSerializer.deserialize(itemData);
                            if (requiredItem != null) {
                                requiredItems.put(i, requiredItem);

                                // Użyj PouchIntegrationHelper
                                int totalAmount = PouchIntegrationHelper.getTotalItemAmount(player, requiredItem);
                                int neededAmount = PouchIntegrationHelper.getActualItemAmount(requiredItem);

                                if (Main.getInstance().getConfig().getInt("debug", 0) == 1) {
                                    Bukkit.getLogger().info("[Crafting] Checking item: " + 
                                        PouchIntegrationHelper.getItemName(requiredItem) + 
                                        " - Has: " + totalAmount + ", Needs: " + neededAmount);
                                }

                                if (totalAmount < neededAmount) {
                                    hasItems = false;
                                    player.sendMessage(ChatColor.RED + "Missing: " + 
                                        PouchIntegrationHelper.getItemName(requiredItem) + 
                                        " x" + (neededAmount - totalAmount));
                                    break;
                                }
                            }
                        }
                    }

                    if (!hasItems) {
                        player.sendMessage(ChatColor.RED + "You don't have all the required items.");
                        return;
                    }

                    // Sprawdź czy gracz ma wystarczająco pieniędzy
                    double cost = rs.getDouble("cost");

                    // Sprawdź zniżkę Steam Sale
                    double discountMultiplier = 1.0;
                    try {
                        Class<?> jewelAPIClass = Class.forName("com.maks.trinketsplugin.JewelAPI");
                        Method getCraftingDiscountMethod = jewelAPIClass.getMethod("getCraftingDiscount", Player.class);
                        Object result = getCraftingDiscountMethod.invoke(null, player);
                        if (result instanceof Double) {
                            discountMultiplier = (Double) result;
                        }
                    } catch (Exception e) {
                        // API niedostępne, używamy domyślnego mnożnika
                    }

                    double finalCost = cost * discountMultiplier;

                    if (Main.getEconomy().getBalance(player) < finalCost) {
                        player.sendMessage(ChatColor.RED + "You don't have enough money.");
                        return;
                    }

                    // Pobierz przedmiot wynikowy z bazy danych
                    String resultItemData = rs.getString("result_item");
                    ItemStack craftedItem = ItemStackSerializer.deserialize(resultItemData);
                    if (craftedItem == null) {
                        player.sendMessage(ChatColor.RED + "Error loading result item!");
                        return;
                    }

                    // Sprawdź czy gracz ma miejsce w ekwipunku
                    if (player.getInventory().firstEmpty() == -1) {
                        player.sendMessage(ChatColor.RED + "Your inventory is full!");
                        return;
                    }

                    // Usuń przedmioty od gracza - ATOMOWO!
                    boolean allItemsRemoved = true;
                    List<ItemStack> removedItems = new ArrayList<>(); // Lista usuniętych przedmiotów dla rollbacku

                    // Group required items by base name and calculate total units needed
                    Map<String, Integer> totalUnitsNeeded = new HashMap<>();
                    Map<String, ItemStack> baseItemSamples = new HashMap<>();

                    for (ItemStack requiredItem : requiredItems.values()) {
                        String baseName = PouchIntegrationHelper.getBaseItemName(requiredItem);
                        int unitsNeeded = PouchIntegrationHelper.getActualItemAmount(requiredItem);

                        // Add to total units needed for this base item
                        totalUnitsNeeded.put(baseName, totalUnitsNeeded.getOrDefault(baseName, 0) + unitsNeeded);

                        // Keep a sample of this item for later use
                        if (!baseItemSamples.containsKey(baseName)) {
                            baseItemSamples.put(baseName, requiredItem.clone());
                        }

                        if (Main.getInstance().getConfig().getInt("debug", 0) == 1) {
                            Bukkit.getLogger().info("[Crafting] Grouped: " + baseName + 
                                " - Added " + unitsNeeded + " units, total now: " + totalUnitsNeeded.get(baseName));
                        }
                    }

                    // Remove items in consolidated operations
                    for (Map.Entry<String, Integer> entry : totalUnitsNeeded.entrySet()) {
                        String baseName = entry.getKey();
                        int unitsNeeded = entry.getValue();
                        ItemStack sampleItem = baseItemSamples.get(baseName);

                        // Create a new item with the total units needed
                        ItemStack consolidatedItem = sampleItem.clone();
                        consolidatedItem.setAmount(1); // Will be multiplied by stack multiplier

                        // Set the appropriate multiplier in the name
                        ItemMeta itemMeta = consolidatedItem.getItemMeta();
                        if (unitsNeeded >= 1000 && unitsNeeded % 1000 == 0) {
                            itemMeta.setDisplayName(baseName + " x1000");
                            consolidatedItem.setAmount(unitsNeeded / 1000);
                        } else if (unitsNeeded >= 100 && unitsNeeded % 100 == 0) {
                            itemMeta.setDisplayName(baseName + " x100");
                            consolidatedItem.setAmount(unitsNeeded / 100);
                        } else {
                            itemMeta.setDisplayName(baseName);
                            consolidatedItem.setAmount(unitsNeeded);
                        }
                        consolidatedItem.setItemMeta(itemMeta);

                        if (Main.getInstance().getConfig().getInt("debug", 0) == 1) {
                            Bukkit.getLogger().info("[Crafting] Removing consolidated: " + 
                                PouchIntegrationHelper.getItemName(consolidatedItem) + " x" + 
                                consolidatedItem.getAmount() + " (" + unitsNeeded + " units)");
                        }

                        // Remove the consolidated item
                        PouchIntegrationHelper.RemovalResult result = PouchIntegrationHelper.removeItems(player, consolidatedItem);
                        if (!result.success) {
                            allItemsRemoved = false;

                            if (result.errorMessage != null && result.errorMessage.startsWith("Not enough")) {
                                player.sendMessage(ChatColor.RED + "You don't have enough " + 
                                    PouchIntegrationHelper.getItemName(consolidatedItem) + 
                                    "! Required: " + result.required + ", Available: " + result.available);
                            } else {
                                player.sendMessage(ChatColor.RED + "Error removing items! Transaction cancelled.");
                            }

                            if (Main.getInstance().getConfig().getInt("debug", 0) == 1) {
                                Bukkit.getLogger().warning("[Crafting] Failed to remove: " + 
                                    PouchIntegrationHelper.getItemName(consolidatedItem) + " x" + 
                                    PouchIntegrationHelper.getActualItemAmount(consolidatedItem) + 
                                    (result.errorMessage != null ? " - " + result.errorMessage : ""));
                            }

                            // Przywróć wcześniej usunięte przedmioty
                            for (ItemStack removedItem : removedItems) {
                                player.getInventory().addItem(removedItem.clone());

                                if (Main.getInstance().getConfig().getInt("debug", 0) == 1) {
                                    Bukkit.getLogger().info("[Crafting] Rollback: Returned " + 
                                        PouchIntegrationHelper.getItemName(removedItem) + " x" + 
                                        PouchIntegrationHelper.getActualItemAmount(removedItem));
                                }
                            }

                            break;
                        }

                        // Zapisz usunięty przedmiot dla ewentualnego rollbacku
                        removedItems.add(consolidatedItem.clone());

                        if (Main.getInstance().getConfig().getInt("debug", 0) == 1) {
                            Bukkit.getLogger().info("[Crafting] Removed: " + 
                                PouchIntegrationHelper.getItemName(consolidatedItem) + " x" + 
                                PouchIntegrationHelper.getActualItemAmount(consolidatedItem));
                        }
                    }

                    if (!allItemsRemoved) {
                        player.sendMessage(ChatColor.RED + "Transaction failed! All items have been returned to your inventory.");
                        return;
                    }

                    // Pobierz pieniądze
                    if (finalCost > 0) {
                        Main.getEconomy().withdrawPlayer(player, finalCost);
                        if (discountMultiplier < 1.0) {
                            player.sendMessage(ChatColor.GOLD + "Your Merchant Jewel gave you a " + 
                                    (int)((1.0 - discountMultiplier) * 100) + "% discount!");
                        }
                    }

                    // Oblicz szansę na sukces
                    double successChance = rs.getDouble("success_chance");
                    if (Math.random() * 100 <= successChance) {
                        // Sukces - daj przedmiot graczowi
                        player.getInventory().addItem(craftedItem);
                        player.sendMessage(ChatColor.GREEN + "Crafting successful!");

                        // Call custom crafting event for integration with other plugins
                        CustomCraftEvent event = new CustomCraftEvent(player, craftedItem, 1, String.valueOf(recipeId));
                        Bukkit.getPluginManager().callEvent(event);

                        if (Main.getInstance().getConfig().getInt("debug", 0) == 1) {
                            Bukkit.getLogger().info("[Crafting] Success - crafted: " + 
                                PouchIntegrationHelper.getItemName(craftedItem));
                            Bukkit.getLogger().info("[Crafting] CustomCraftEvent fired for player: " + player.getName());
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "Crafting failed.");

                        if (Main.getInstance().getConfig().getInt("debug", 0) == 1) {
                            Bukkit.getLogger().info("[Crafting] Failed - success chance was: " + successChance + "%");
                        }
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Recipe not found.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "An error occurred during crafting.");
        }
    }


    // Implement the removeItems method
    private void removeItems(Player player, ItemStack item) {
        int amountToRemove = item.getAmount();
        ItemStack[] contents = player.getInventory().getContents();

        for (int i = 0; i < contents.length; i++) {
            ItemStack invItem = contents[i];
            if (areItemsEqual(invItem, item)) {
                if (invItem.getAmount() > amountToRemove) {
                    invItem.setAmount(invItem.getAmount() - amountToRemove);
                    break;
                } else {
                    amountToRemove -= invItem.getAmount();
                    contents[i] = null;
                    if (amountToRemove == 0) {
                        break;
                    }
                }
            }
        }

        player.getInventory().setContents(contents);
    }

    // Implement the areItemsEqual method
    private boolean areItemsEqual(ItemStack item1, ItemStack item2) {
        if (item1 == null || item2 == null) {
            // System.out.println("One of the items is null");
            return false;
        }
        if (item1.getType() != item2.getType()) {
            // System.out.println("Different types: " + item1.getType() + " vs " + item2.getType());
            return false;
        }
        if (item1.getDurability() != item2.getDurability()) {
            // System.out.println("Different durability: " + item1.getDurability() + " vs " + item2.getDurability());
            return false;
        }

        ItemMeta meta1 = item1.getItemMeta();
        ItemMeta meta2 = item2.getItemMeta();

        if ((meta1 == null) != (meta2 == null)) {
            // System.out.println("One of the items has meta, the other doesn't");
            return false;
        }
        if (meta1 == null && meta2 == null) {
            // System.out.println("Both items have no meta, items are equal");
            return true;
        }

        // Compare display names
        if (meta1.hasDisplayName() != meta2.hasDisplayName()) {
            // System.out.println("One of the items has a display name, the other doesn't");
            return false;
        }
        if (meta1.hasDisplayName() && !meta1.getDisplayName().equals(meta2.getDisplayName())) {
            // System.out.println("Different display names: " + meta1.getDisplayName() + " vs " + meta2.getDisplayName());
            return false;
        }

        // Compare lore
        if (meta1.hasLore() != meta2.hasLore()) {
            // System.out.println("One of the items has lore, the other doesn't");
            return false;
        }
        if (meta1.hasLore() && !meta1.getLore().equals(meta2.getLore())) {
            // System.out.println("Different lore");
            return false;
        }

        // Compare enchants
        if (meta1.hasEnchants() != meta2.hasEnchants()) {
            // System.out.println("One of the items has enchants, the other doesn't");
            return false;
        }
        if (meta1.hasEnchants() && !meta1.getEnchants().equals(meta2.getEnchants())) {
            // System.out.println("Different enchants");
            return false;
        }

        // Items are equal
        // System.out.println("Items are equal");
        return true;
    }



    // Implement the getTotalItemAmount method
    private int getTotalItemAmount(Player player, ItemStack item) {
        int total = 0;
        for (ItemStack invItem : player.getInventory().getContents()) {
            if (areItemsEqual(invItem, item)) {
                total += invItem.getAmount();
            }
        }
        return total;
    }

    // Metoda saveRecipe
    private void saveRecipe(Player player, Inventory inv) {
        String category = AddRecipeMenu.getCategory(player.getUniqueId());
        if (category == null) {
            player.sendMessage(ChatColor.RED + "Category not found.");
            return;
        }

        // Get highest ID to set proper slot
        int nextSlot = 0;
        try (Connection conn = Main.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT MAX(id) FROM recipes WHERE category = ?")) {
            ps.setString(1, category);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getObject(1) != null) {
                nextSlot = rs.getInt(1) + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Error determining slot position.");
            return;
        }

        try (Connection conn = Main.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO recipes (category, slot, required_item_1, required_item_2, required_item_3, " +
                             "required_item_4, required_item_5, required_item_6, required_item_7, required_item_8, " +
                             "required_item_9, required_item_10, result_item, success_chance, cost) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
             )) {

            ps.setString(1, category);
            ps.setInt(2, nextSlot);

            // Wymagane przedmioty (sloty 0-9)
            for (int i = 0; i < 10; i++) {
                ItemStack item = inv.getItem(i);
                if (item != null && item.getType() != Material.GRAY_STAINED_GLASS_PANE) {
                    ps.setString(i + 3, ItemStackSerializer.serialize(item));
                } else {
                    ps.setString(i + 3, null);
                }
            }

            // Przedmiot wynikowy (slot 13)
            ItemStack resultItem = inv.getItem(13);
            if (resultItem != null && resultItem.getType() != Material.LIGHT_BLUE_STAINED_GLASS_PANE) {
                ps.setString(13, ItemStackSerializer.serialize(resultItem));
            } else {
                player.sendMessage(ChatColor.RED + "You must set a result item!");
                return;
            }

            // Szansa na sukces
            ps.setDouble(14, TemporaryData.getSuccessChance(player.getUniqueId()));

            // Koszt
            ps.setDouble(15, TemporaryData.getCost(player.getUniqueId()));

            ps.executeUpdate();
            player.sendMessage(ChatColor.GREEN + "Recipe has been saved.");

            // Wyczyść dane tymczasowe
            TemporaryData.removeSuccessChance(player.getUniqueId());
            TemporaryData.removeCost(player.getUniqueId());
            AddRecipeMenu.removeGuiState(player.getUniqueId());

            // Zamiast zamykać GUI, otwórz z powrotem menu kategorii
            int page = TemporaryData.getPage(player.getUniqueId(), category);
            CategoryMenu.openEditor(player, category, page);

        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "An error occurred while saving the recipe.");
        }
    }
    // Metoda updateRecipe
    private void updateRecipe(Player player, Inventory inv) {
        int recipeId = EditRecipeMenu.getRecipeId(player.getUniqueId());
        if (recipeId == -1) {
            player.sendMessage(ChatColor.RED + "Recipe ID not found.");
            return;
        }

        try (Connection conn = Main.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE recipes SET required_item_1=?, required_item_2=?, required_item_3=?, " +
                             "required_item_4=?, required_item_5=?, required_item_6=?, required_item_7=?, " +
                             "required_item_8=?, required_item_9=?, required_item_10=?, result_item=?, " +
                             "success_chance=?, cost=? WHERE id=?"
             )) {

            // Wymagane przedmioty (sloty 0-9)
            for (int i = 0; i < 10; i++) {
                ItemStack item = inv.getItem(i);
                if (item != null && item.getType() != Material.GRAY_STAINED_GLASS_PANE) {
                    ps.setString(i + 1, ItemStackSerializer.serialize(item));
                } else {
                    ps.setString(i + 1, null);
                }
            }

            // Przedmiot wynikowy (slot 13)
            ItemStack resultItem = inv.getItem(13);
            if (resultItem != null && resultItem.getType() != Material.LIGHT_BLUE_STAINED_GLASS_PANE) {
                ps.setString(11, ItemStackSerializer.serialize(resultItem));
            } else {
                player.sendMessage(ChatColor.RED + "You must set a result item!");
                return;
            }

            // Szansa na sukces i koszt
            ps.setDouble(12, TemporaryData.getSuccessChance(player.getUniqueId()));
            ps.setDouble(13, TemporaryData.getCost(player.getUniqueId()));
            ps.setInt(14, recipeId);

            int updatedRows = ps.executeUpdate();
            if (updatedRows > 0) {
                player.sendMessage(ChatColor.GREEN + "Recipe has been updated.");
            } else {
                player.sendMessage(ChatColor.RED + "No recipe was updated. The recipe might have been deleted.");
            }

            // Wyczyść dane tymczasowe
            TemporaryData.removeSuccessChance(player.getUniqueId());
            TemporaryData.removeCost(player.getUniqueId());
            AddRecipeMenu.removeGuiState(player.getUniqueId());

            // Instead of player.closeInventory();
            String category = TemporaryData.getLastCategory(player.getUniqueId());
            int page = TemporaryData.getPage(player.getUniqueId(), category);
            CategoryMenu.openEditor(player, category, page);

        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "An error occurred while updating the recipe.");
        }
    }

    // Metoda deleteRecipe
    private void deleteRecipe(Player player) {
        int recipeId = EditRecipeMenu.getRecipeId(player.getUniqueId());
        if (recipeId == -1) {
            player.sendMessage(ChatColor.RED + "Recipe ID not found.");
            return;
        }

        try (Connection conn = Main.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM recipes WHERE id = ?")) {

            ps.setInt(1, recipeId);
            int deletedRows = ps.executeUpdate();

            if (deletedRows > 0) {
                player.sendMessage(ChatColor.GREEN + "Recipe has been deleted.");
            } else {
                player.sendMessage(ChatColor.RED + "No recipe was deleted. The recipe might have already been removed.");
            }

            // Wyczyść dane tymczasowe
            TemporaryData.removeSuccessChance(player.getUniqueId());
            TemporaryData.removeCost(player.getUniqueId());
            AddRecipeMenu.removeGuiState(player.getUniqueId());

            // Instead of player.closeInventory();
            String category = TemporaryData.getLastCategory(player.getUniqueId());
            int page = TemporaryData.getPage(player.getUniqueId(), category);
            CategoryMenu.openEditor(player, category, page);

        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "An error occurred while deleting the recipe.");
        }
    }


    // Metoda saveCategoryLayout
    private void saveCategoryLayout(Inventory inv, String category) {
        try (Connection conn = Main.getConnection()) {
            // Najpierw pobierz wszystkie receptury z tej kategorii
            try (PreparedStatement selectPs = conn.prepareStatement("SELECT id, result_item FROM recipes WHERE category = ?")) {
                selectPs.setString(1, category);
                ResultSet rs = selectPs.executeQuery();

                Map<String, Integer> recipeIdMap = new HashMap<>();
                while (rs.next()) {
                    recipeIdMap.put(rs.getString("result_item"), rs.getInt("id"));
                }

                // Następnie aktualizuj pozycje
                try (PreparedStatement updatePs = conn.prepareStatement("UPDATE recipes SET slot = ? WHERE id = ?")) {
                    for (int i = 0; i < 45; i++) {
                        ItemStack item = inv.getItem(i);
                        if (item != null && item.getType() != Material.BLACK_STAINED_GLASS_PANE) {
                            String itemData = ItemStackSerializer.serialize(item);
                            Integer recipeId = recipeIdMap.get(itemData);
                            if (recipeId != null) {
                                updatePs.setInt(1, i);
                                updatePs.setInt(2, recipeId);
                                updatePs.addBatch();
                            }
                        }
                    }
                    updatePs.executeBatch();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void handleAlchemyMenuClick(Player player, String itemName) {
        switch (itemName) {
            case "Alchemy Shop":
                // Otwórz CategoryMenu z kategorią "alchemy_shop"
                CategoryMenu.open(player, "alchemy_shop", 0);
                break;
            case "Tonics Crafting":
                CategoryMenu.open(player, "tonics", 0);
                break;
            case "Potions Crafting":
                CategoryMenu.open(player, "potions", 0);
                break;
            case "Physic Crafting":
                CategoryMenu.open(player, "physic", 0);
                break;
            default:
                break;
        }
    }

    private void handleEditAlchemyMenuClick(Player player, String itemName) {
        switch (itemName) {
            case "Alchemy Shop":
                CategoryMenu.openEditor(player, "alchemy_shop", 0);
                break;
            case "Tonics Crafting":
                CategoryMenu.openEditor(player, "tonics", 0);
                break;
            case "Potions Crafting":
                CategoryMenu.openEditor(player, "potions", 0);
                break;
            case "Physic Crafting":
                CategoryMenu.openEditor(player, "physic", 0);
                break;
            default:
                break;
        }
    }

    private void handleJewelerMenuClick(Player player, String itemName) {
        if (itemName == null) return;

        switch (itemName) {
            case "Jewels Upgrading":
                CategoryMenu.open(player, "jewels_upgrading", 0);
                break;
            case "Jewels Crushing":
                // Use the method that bypasses permission checks when opening from jeweler menu
                JewelsCrushingCommand.openMenuWithoutPermissionCheck(player);
                break;
            case "Back":
                // Go back to crafting menu instead of jeweler
                MainMenu.open(player);
                break;
            default:
                break;
        }
    }

    private void handleEditJewelerMenuClick(Player player, String itemName) {
        if (itemName == null) return;

        switch (itemName) {
            case "Jewels Upgrading":
                CategoryMenu.openEditor(player, "jewels_upgrading", 0);
                break;
            case "Jewels Crushing":
                // No edit mode for crushing, just open normal crushing menu
                // Use the method that bypasses permission checks when opening from jeweler edit menu
                JewelsCrushingCommand.openMenuWithoutPermissionCheck(player);
                break;
            case "Back":
                // Go back to edit crafting menu
                MainMenu.openEditor(player);
                break;
            default:
                break;
        }
    }

    private void handleGemologistMenuClick(Player player, String itemName) {
        if (itemName == null) return;

        switch (itemName) {
            case "Gem Crafting":
                CategoryMenu.open(player, "gems_crafting", 0);
                break;
            case "Gem Actions":
                PermissionAttachment attachment = null;
                if (!player.hasPermission("mycraftingplugin.use")) {
                    attachment = player.addAttachment(Main.getInstance(), "mycraftingplugin.use", true);
                }
                player.performCommand("gem_actions");
                if (attachment != null) {
                    player.removeAttachment(attachment);
                }
                break;
            case "Gem Crushing":
                GemCrushingCommand.openMenuWithoutPermissionCheck(player);
                break;
            case "Back":
                MainMenu.open(player);
                break;
            default:
                break;
        }
    }

    private void handleEditGemologistMenuClick(Player player, String itemName) {
        if (itemName == null) return;

        switch (itemName) {
            case "Gem Crafting":
                CategoryMenu.openEditor(player, "gems_crafting", 0);
                break;
            case "Gem Actions":
                PermissionAttachment attachment = null;
                if (!player.hasPermission("mycraftingplugin.use")) {
                    attachment = player.addAttachment(Main.getInstance(), "mycraftingplugin.use", true);
                }
                player.performCommand("gem_actions");
                if (attachment != null) {
                    player.removeAttachment(attachment);
                }
                break;
            case "Gem Crushing":
                GemCrushingCommand.openMenuWithoutPermissionCheck(player);
                break;
            case "Back":
                MainMenu.openEditor(player);
                break;
            default:
                break;
        }
    }

    private void handleRunemasterMenuClick(Player player, String itemName) {
        if (itemName == null) return;

        switch (itemName) {
            case "Runes Upgrading":
                CategoryMenu.open(player, "runes_upgrading", 0);
                break;
            case "Rune Crushing":
                RunesCrushingCommand.openMenuWithoutPermissionCheck(player);
                break;
            case "Back":
                MainMenu.open(player);
                break;
            default:
                break;
        }
    }

    private void handleEditRunemasterMenuClick(Player player, String itemName) {
        if (itemName == null) return;

        switch (itemName) {
            case "Runes Upgrading":
                CategoryMenu.openEditor(player, "runes_upgrading", 0);
                break;
            case "Rune Crushing":
                RunesCrushingCommand.openMenuWithoutPermissionCheck(player);
                break;
            case "Back":
                MainMenu.openEditor(player);
                break;
            default:
                break;
        }
    }

    private void handleJewelsCrushingMenuClick(InventoryClickEvent event, Player player, ItemStack clickedItem, String itemName) {
        int slot = event.getRawSlot();
        Inventory inv = event.getInventory();

        // Debugging flag
        int debuggingFlag = 1; // Set to 0 to disable debug

        if (debuggingFlag == 1) {
            Bukkit.getLogger().info("[MenuListener] JewelsCrushing click - Slot: " + slot + 
                                  ", Action: " + event.getAction() + 
                                  ", Click: " + event.getClick() + 
                                  ", Player: " + player.getName());
        }

        // Check if the clicked inventory is null
        if (event.getClickedInventory() == null) {
            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[MenuListener] Clicked inventory is null");
            }
            return;
        }

        // Allow placing/removing items in first two rows (slots 0-17)
        if (slot < 18 && event.getClickedInventory().equals(event.getView().getTopInventory())) {
            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[MenuListener] Click in jewel placement area, slot: " + slot);
            }

            // Handle different click types
            if (event.getAction().name().contains("PLACE")) {
                ItemStack cursor = event.getCursor();
                if (cursor != null && cursor.getType() != Material.AIR) {
                    if (debuggingFlag == 1) {
                        Bukkit.getLogger().info("[MenuListener] Placing item: " + cursor.getType() + 
                                              " with name: " + (cursor.hasItemMeta() && cursor.getItemMeta().hasDisplayName() ? 
                                              cursor.getItemMeta().getDisplayName() : "No name"));
                    }

                    // Check if it's a valid jewel
                    if (!JewelsCrushingMenu.isJewel(cursor)) {
                        player.sendMessage(ChatColor.RED + "You can only place jewels here!");
                        event.setCancelled(true);
                        return;
                    }
                }
            }

            // Handle shift-click from player inventory
            else if (event.getAction().name().contains("SHIFT") && 
                    event.getClickedInventory().equals(event.getView().getBottomInventory())) {
                ItemStack currentItem = event.getCurrentItem();
                if (currentItem != null && currentItem.getType() != Material.AIR) {
                    if (debuggingFlag == 1) {
                        Bukkit.getLogger().info("[MenuListener] Shift-clicking from player inventory: " + 
                                              currentItem.getType());
                    }

                    // Check if it's a valid jewel
                    if (!JewelsCrushingMenu.isJewel(currentItem)) {
                        player.sendMessage(ChatColor.RED + "You can only place jewels in the crushing menu!");
                        event.setCancelled(true);
                        return;
                    }

                    // Find empty slot in crushing area (0-17)
                    boolean placed = false;
                    for (int i = 0; i < 18; i++) {
                        if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) {
                            ItemStack toPlace = currentItem.clone();
                            inv.setItem(i, toPlace);
                            currentItem.setAmount(0); // Remove from player inventory
                            placed = true;

                            if (debuggingFlag == 1) {
                                Bukkit.getLogger().info("[MenuListener] Placed jewel in slot: " + i);
                            }
                            break;
                        }
                    }

                    if (!placed) {
                        player.sendMessage(ChatColor.RED + "No space available in the crushing area!");
                    }

                    event.setCancelled(true);
                    return;
                }
            }

            // Allow the action (placing/removing)
            event.setCancelled(false);
            return;
        }

        // If clicking in player inventory, allow it
        if (event.getClickedInventory().equals(event.getView().getBottomInventory())) {
            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[MenuListener] Click in player inventory allowed");
            }
            event.setCancelled(false);
            return;
        }

        // Handle confirm button
        if (slot == 22 && itemName != null && itemName.equals("Confirm Crushing")) {
            if (debuggingFlag == 1) {
                Bukkit.getLogger().info("[MenuListener] Confirm crushing button clicked");
            }

            JewelsCrushingMenu.processJewels(player, inv);
            event.setCancelled(true);
            return;
        }

        // Cancel click on bottom row glass panes and other UI elements
        if (debuggingFlag == 1) {
            Bukkit.getLogger().info("[MenuListener] Cancelling click on UI element, slot: " + slot);
        }
        event.setCancelled(true);
    }

    private void handleGemCrushingMenuClick(InventoryClickEvent event, Player player, ItemStack clickedItem, String itemName) {
        int slot = event.getRawSlot();
        Inventory inv = event.getInventory();

        if (event.getClickedInventory() == null) {
            return;
        }

        if (slot < 18 && event.getClickedInventory().equals(event.getView().getTopInventory())) {
            if (event.getAction().name().contains("PLACE")) {
                ItemStack cursor = event.getCursor();
                if (cursor != null && cursor.getType() != Material.AIR) {
                    if (!GemCrushingMenu.isGem(cursor)) {
                        player.sendMessage(ChatColor.RED + "You can only place gems here!");
                        event.setCancelled(true);
                        return;
                    }
                }
            } else if (event.getAction().name().contains("SHIFT") &&
                    event.getClickedInventory().equals(event.getView().getBottomInventory())) {
                ItemStack currentItem = event.getCurrentItem();
                if (currentItem != null && currentItem.getType() != Material.AIR) {
                    if (!GemCrushingMenu.isGem(currentItem)) {
                        player.sendMessage(ChatColor.RED + "You can only place gems in the crushing menu!");
                        event.setCancelled(true);
                        return;
                    }

                    boolean placed = false;
                    for (int i = 0; i < 18; i++) {
                        if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) {
                            ItemStack toPlace = currentItem.clone();
                            inv.setItem(i, toPlace);
                            currentItem.setAmount(0);
                            placed = true;
                            break;
                        }
                    }

                    if (!placed) {
                        player.sendMessage(ChatColor.RED + "No space available in the crushing area!");
                    }

                    event.setCancelled(true);
                    return;
                }
            }

            event.setCancelled(false);
            return;
        }

        if (event.getClickedInventory().equals(event.getView().getBottomInventory())) {
            event.setCancelled(false);
            return;
        }

        if (slot == 22 && itemName != null && itemName.equals("Confirm Crushing")) {
            GemCrushingMenu.processGems(player, inv);
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
    }

    private void handleRuneCrushingMenuClick(InventoryClickEvent event, Player player, ItemStack clickedItem, String itemName) {
        int slot = event.getRawSlot();
        Inventory inv = event.getInventory();

        if (event.getClickedInventory() == null) {
            return;
        }

        if (slot < 18 && event.getClickedInventory().equals(event.getView().getTopInventory())) {
            if (event.getAction().name().contains("PLACE")) {
                ItemStack cursor = event.getCursor();
                if (cursor != null && cursor.getType() != Material.AIR) {
                    if (!RunesCrushingMenu.isRune(cursor)) {
                        player.sendMessage(ChatColor.RED + "You can only place runes here!");
                        event.setCancelled(true);
                        return;
                    }
                }
            } else if (event.getAction().name().contains("SHIFT") &&
                    event.getClickedInventory().equals(event.getView().getBottomInventory())) {
                ItemStack currentItem = event.getCurrentItem();
                if (currentItem != null && currentItem.getType() != Material.AIR) {
                    if (!RunesCrushingMenu.isRune(currentItem)) {
                        player.sendMessage(ChatColor.RED + "You can only place runes in the crushing menu!");
                        event.setCancelled(true);
                        return;
                    }

                    boolean placed = false;
                    for (int i = 0; i < 18; i++) {
                        if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) {
                            ItemStack toPlace = currentItem.clone();
                            inv.setItem(i, toPlace);
                            currentItem.setAmount(0);
                            placed = true;
                            break;
                        }
                    }

                    if (!placed) {
                        player.sendMessage(ChatColor.RED + "No space available in the crushing area!");
                    }

                    event.setCancelled(true);
                    return;
                }
            }

            event.setCancelled(false);
            return;
        }

        if (event.getClickedInventory().equals(event.getView().getBottomInventory())) {
            event.setCancelled(false);
            return;
        }

        if (slot == 22 && itemName != null && itemName.equals("Confirm Crushing")) {
            RunesCrushingMenu.processRunes(player, inv);
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
    }

    private void handleEmiliaMainMenu(Player player, ItemStack clickedItem, String itemName) {
        if (itemName == null) return;

        switch (itemName) {
            case "Shop":
                EmiliaShopMenu.open(player);
                break;
            case "Event Shop":
                EmiliaEventShopMenu.open(player);
                break;
            default:
                break;
        }
    }

    private void handleEditEmiliaMainMenu(Player player, ItemStack clickedItem, String itemName) {
        if (itemName == null) return;

        switch (itemName) {
            case "Shop":
                EmiliaShopMenu.openEditor(player);
                break;
            case "Event Shop":
                EmiliaEventShopMenu.openEditor(player);
                break;
            default:
                break;
        }
    }

    private void handleEmiliaShopMenu(Player player, ItemStack clickedItem, String itemName) {
        if (itemName == null) return;

        if (itemName.equals("Back")) {
            EmiliaMainMenu.open(player);
            return;
        }

        // Check if item is locked (contains "Locked")
        if (itemName.contains("Locked")) {
            player.sendMessage(ChatColor.RED + "You don't have access to this tier!");
            return;
        }

        String tierType = ChatColor.stripColor(itemName);
        if (tierType.equals("Basic") || tierType.equals("Premium") || tierType.equals("Deluxe")) {
            EmiliaShopItemsMenu.open(player, "Shop", tierType, 0);
        }
    }

    private void handleEditEmiliaShopMenu(Player player, ItemStack clickedItem, String itemName) {
        if (itemName == null) return;

        if (itemName.equals("Back")) {
            EmiliaMainMenu.openEditor(player);
            return;
        }

        String tierType = ChatColor.stripColor(itemName);
        if (tierType.equals("Basic") || tierType.equals("Premium") || tierType.equals("Deluxe")) {
            EmiliaShopItemsMenu.openEditor(player, "Shop", tierType, 0);
        }
    }

    private void handleEmiliaEventShopMenu(Player player, ItemStack clickedItem, String itemName) {
        if (itemName == null) return;

        if (itemName.equals("Back")) {
            EmiliaMainMenu.open(player);
            return;
        }

        // Check if item is locked (contains "Locked")
        if (itemName.contains("Locked")) {
            player.sendMessage(ChatColor.RED + "You don't have access to this tier!");
            return;
        }

        String tierType = ChatColor.stripColor(itemName);
        if (tierType.equals("Basic") || tierType.equals("Premium") || tierType.equals("Deluxe")) {
            EmiliaShopItemsMenu.open(player, "Event Shop", tierType, 0);
        }
    }

    private void handleEditEmiliaEventShopMenu(Player player, ItemStack clickedItem, String itemName) {
        if (itemName == null) return;

        if (itemName.equals("Back")) {
            EmiliaMainMenu.openEditor(player);
            return;
        }

        String tierType = ChatColor.stripColor(itemName);
        if (tierType.equals("Basic") || tierType.equals("Premium") || tierType.equals("Deluxe")) {
            EmiliaShopItemsMenu.openEditor(player, "Event Shop", tierType, 0);
        }
    }

    private void handleEmiliaItemsMenu(InventoryClickEvent event, Player player, ItemStack clickedItem, String itemName, String title) {
        if (itemName == null) return;

        if (itemName.equals("Back")) {
            String shopType = (String) TemporaryData.getPlayerData(player.getUniqueId(), "emilia_shop_type");
            if (shopType != null) {
                if (shopType.equals("Shop")) {
                    EmiliaShopMenu.open(player);
                } else if (shopType.equals("Event Shop")) {
                    EmiliaEventShopMenu.open(player);
                }
            }
            return;
        }

        if (itemName.equals("Next Page")) {
            String shopType = (String) TemporaryData.getPlayerData(player.getUniqueId(), "emilia_shop_type");
            String tierType = (String) TemporaryData.getPlayerData(player.getUniqueId(), "emilia_tier_type");

            if (shopType != null && tierType != null) {
                String category = "emilia_" + shopType.toLowerCase().replace(" ", "_") + "_" + tierType.toLowerCase();
                int currentPage = TemporaryData.getPage(player.getUniqueId(), category);
                EmiliaShopItemsMenu.open(player, shopType, tierType, currentPage + 1);
            }
            return;
        }

        if (itemName.equals("Previous Page")) {
            String shopType = (String) TemporaryData.getPlayerData(player.getUniqueId(), "emilia_shop_type");
            String tierType = (String) TemporaryData.getPlayerData(player.getUniqueId(), "emilia_tier_type");

            if (shopType != null && tierType != null) {
                String category = "emilia_" + shopType.toLowerCase().replace(" ", "_") + "_" + tierType.toLowerCase();
                int currentPage = TemporaryData.getPage(player.getUniqueId(), category);
                if (currentPage > 0) {
                    EmiliaShopItemsMenu.open(player, shopType, tierType, currentPage - 1);
                }
            }
            return;
        }

        // Clicked on an item - use unified confirmation menu
        if (clickedItem != null && clickedItem.getType() != Material.BLACK_STAINED_GLASS_PANE) {
            ItemMeta meta = clickedItem.getItemMeta();
            if (meta != null) {
                NamespacedKey key = new NamespacedKey(Main.getInstance(), "item_id");
                if (meta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
                    int itemId = meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);

                    // Use UnifiedConfirmationMenu
                    UnifiedConfirmationMenu.open(player, itemId, UnifiedConfirmationMenu.ShopType.EMILIA);

                    if (Main.getInstance().getConfig().getInt("debug", 0) == 1) {
                        Bukkit.getLogger().info("[MenuListener] Opening unified confirmation for Emilia item: " + itemId);
                    }
                }
            }
        }
    }

    private void handleEditEmiliaItemsMenu(InventoryClickEvent event, Player player, ItemStack clickedItem, String itemName, String title) {
        if (itemName == null) return;

        if (itemName.equals("Back")) {
            String shopType = (String) TemporaryData.getPlayerData(player.getUniqueId(), "emilia_shop_type");
            if (shopType != null) {
                if (shopType.equals("Shop")) {
                    EmiliaShopMenu.openEditor(player);
                } else if (shopType.equals("Event Shop")) {
                    EmiliaEventShopMenu.openEditor(player);
                }
            }
            return;
        }

        if (itemName.equals("Next Page")) {
            String shopType = (String) TemporaryData.getPlayerData(player.getUniqueId(), "emilia_shop_type");
            String tierType = (String) TemporaryData.getPlayerData(player.getUniqueId(), "emilia_tier_type");

            if (shopType != null && tierType != null) {
                String category = "emilia_" + shopType.toLowerCase().replace(" ", "_") + "_" + tierType.toLowerCase();
                int currentPage = TemporaryData.getPage(player.getUniqueId(), category);
                EmiliaShopItemsMenu.openEditor(player, shopType, tierType, currentPage + 1);
            }
            return;
        }

        if (itemName.equals("Previous Page")) {
            String shopType = (String) TemporaryData.getPlayerData(player.getUniqueId(), "emilia_shop_type");
            String tierType = (String) TemporaryData.getPlayerData(player.getUniqueId(), "emilia_tier_type");

            if (shopType != null && tierType != null) {
                String category = "emilia_" + shopType.toLowerCase().replace(" ", "_") + "_" + tierType.toLowerCase();
                int currentPage = TemporaryData.getPage(player.getUniqueId(), category);
                if (currentPage > 0) {
                    EmiliaShopItemsMenu.openEditor(player, shopType, tierType, currentPage - 1);
                }
            }
            return;
        }

        if (itemName.equals("Add Item")) {
            String shopType = (String) TemporaryData.getPlayerData(player.getUniqueId(), "emilia_shop_type");
            String tierType = (String) TemporaryData.getPlayerData(player.getUniqueId(), "emilia_tier_type");

            if (shopType != null && tierType != null) {
                EmiliaAddItemMenu.open(player, shopType, tierType);
            }
            return;
        }

        // Clicked on an item to edit
        if (clickedItem != null && clickedItem.getType() != Material.BLACK_STAINED_GLASS_PANE) {
            ItemMeta meta = clickedItem.getItemMeta();
            if (meta != null) {
                NamespacedKey key = new NamespacedKey(Main.getInstance(), "item_id");
                if (meta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
                    int itemId = meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
                    EmiliaAddItemMenu.openEdit(player, itemId);
                }
            }
        }
    }

    private void handleEmiliaAddEditItemMenu(InventoryClickEvent event, Player player, ItemStack clickedItem, String itemName, String title) {
        Inventory inv = event.getInventory();
        int slot = event.getRawSlot();
        boolean isTopInventory = (event.getView().getTopInventory() == event.getClickedInventory());
        boolean isEditMode = title.equals("Edit Emilia Item");

        if (isTopInventory) {
            // Allow placing items in slots 0-9 (required items) and slot 13 (result item)
            if ((slot >= 0 && slot <= 9) || slot == 13) {
                // Handle placeholder removal
                if (clickedItem != null && itemName != null) {
                    Material mat = clickedItem.getType();
                    if (mat == Material.GRAY_STAINED_GLASS_PANE || mat == Material.LIGHT_BLUE_STAINED_GLASS_PANE) {
                        // Remove placeholder
                        inv.setItem(slot, null);
                        event.setCancelled(true);
                        return;
                    }
                }
                // Allow item placement/removal
                event.setCancelled(false);
                return;
            }

            // Handle cost and daily limit buttons
            if (itemName == null) return;

            switch (slot) {
                case 11: // Cost
                    EmiliaAddItemMenu.saveGuiState(player.getUniqueId(), inv.getContents());
                    ChatListener.setPlayerState(player.getUniqueId(), "entering_cost");
                    player.sendMessage(ChatColor.YELLOW + "Please enter the cost (e.g. 500, 500k, 1m).");
                    player.closeInventory();
                    break;

                case 15: // Daily Limit
                    EmiliaAddItemMenu.saveGuiState(player.getUniqueId(), inv.getContents());
                    ChatListener.setPlayerState(player.getUniqueId(), "entering_emilia_daily_limit");
                    player.sendMessage(ChatColor.YELLOW + "Please enter the daily limit (0 = no limit).");
                    player.closeInventory();
                    break;

                case 22: // Save
                    if (isEditMode) {
                        updateEmiliaItem(player, inv);
                    } else {
                        saveEmiliaItem(player, inv);
                    }
                    break;

                case 23: // Delete (only in edit mode)
                    if (isEditMode && itemName.equals("Delete")) {
                        deleteEmiliaItem(player);
                    }
                    break;

                case 24: // Back
                    goBackFromEmiliaAdd(player);
                    break;
            }
        } else {
            // Clicking in player inventory
            event.setCancelled(false);
        }
    }

    private void saveEmiliaItem(Player player, Inventory inv) {
        String category = EmiliaAddItemMenu.getCategory(player.getUniqueId());
        if (category == null) {
            player.sendMessage(ChatColor.RED + "Category not found.");
            return;
        }

        ItemStack item = inv.getItem(13);
        if (item == null || item.getType() == Material.LIGHT_BLUE_STAINED_GLASS_PANE) {
            player.sendMessage(ChatColor.RED + "You must set an item!");
            return;
        }

        // Get next available slot
        int nextSlot = 0;
        try (Connection conn = Main.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT MAX(slot) FROM emilia_items WHERE category = ?")) {
            ps.setString(1, category);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getObject(1) != null) {
                nextSlot = rs.getInt(1) + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Error determining slot position.");
            return;
        }

        double cost = TemporaryData.getCost(player.getUniqueId());
        int dailyLimit = 0;
        Object limitObj = TemporaryData.getPlayerData(player.getUniqueId(), "emilia_daily_limit");
        if (limitObj instanceof Integer) {
            dailyLimit = (Integer) limitObj;
        }

        try (Connection conn = Main.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO emilia_items (category, slot, required_item_1, required_item_2, required_item_3, " +
                     "required_item_4, required_item_5, required_item_6, required_item_7, required_item_8, " +
                     "required_item_9, required_item_10, item, cost, daily_limit) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

            ps.setString(1, category);
            ps.setInt(2, nextSlot);

            // Required items (slots 0-9)
            for (int i = 0; i < 10; i++) {
                ItemStack requiredItem = inv.getItem(i);
                if (requiredItem != null && requiredItem.getType() != Material.GRAY_STAINED_GLASS_PANE) {
                    ps.setString(i + 3, ItemStackSerializer.serialize(requiredItem));
                } else {
                    ps.setString(i + 3, null);
                }
            }

            ps.setString(13, ItemStackSerializer.serialize(item));
            ps.setDouble(14, cost);
            ps.setInt(15, dailyLimit);

            ps.executeUpdate();
            player.sendMessage(ChatColor.GREEN + "Item has been added to the shop.");

            // Clear temporary data
            TemporaryData.removeCost(player.getUniqueId());
            TemporaryData.removePlayerData(player.getUniqueId(), "emilia_daily_limit");
            EmiliaAddItemMenu.removeGuiState(player.getUniqueId());

            // Return to shop items menu
            goBackFromEmiliaAdd(player);

        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Error saving item to database.");
        }
    }

    private void updateEmiliaItem(Player player, Inventory inv) {
        int itemId = EmiliaAddItemMenu.getEditItemId(player.getUniqueId());
        if (itemId == -1) {
            player.sendMessage(ChatColor.RED + "Item ID not found.");
            return;
        }

        ItemStack item = inv.getItem(13);
        if (item == null || item.getType() == Material.LIGHT_BLUE_STAINED_GLASS_PANE) {
            player.sendMessage(ChatColor.RED + "You must set an item!");
            return;
        }

        double cost = TemporaryData.getCost(player.getUniqueId());
        int dailyLimit = 0;
        Object limitObj = TemporaryData.getPlayerData(player.getUniqueId(), "emilia_daily_limit");
        if (limitObj instanceof Integer) {
            dailyLimit = (Integer) limitObj;
        }

        try (Connection conn = Main.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE emilia_items SET required_item_1 = ?, required_item_2 = ?, required_item_3 = ?, " +
                     "required_item_4 = ?, required_item_5 = ?, required_item_6 = ?, required_item_7 = ?, " +
                     "required_item_8 = ?, required_item_9 = ?, required_item_10 = ?, " +
                     "item = ?, cost = ?, daily_limit = ? WHERE id = ?")) {

            // Required items (slots 0-9)
            for (int i = 0; i < 10; i++) {
                ItemStack requiredItem = inv.getItem(i);
                if (requiredItem != null && requiredItem.getType() != Material.GRAY_STAINED_GLASS_PANE) {
                    ps.setString(i + 1, ItemStackSerializer.serialize(requiredItem));
                } else {
                    ps.setString(i + 1, null);
                }
            }

            ps.setString(11, ItemStackSerializer.serialize(item));
            ps.setDouble(12, cost);
            ps.setInt(13, dailyLimit);
            ps.setInt(14, itemId);

            int updated = ps.executeUpdate();
            if (updated > 0) {
                player.sendMessage(ChatColor.GREEN + "Item has been updated.");
            } else {
                player.sendMessage(ChatColor.RED + "Item not found in database.");
            }

            // Clear temporary data
            TemporaryData.removeCost(player.getUniqueId());
            TemporaryData.removePlayerData(player.getUniqueId(), "emilia_daily_limit");
            EmiliaAddItemMenu.removeGuiState(player.getUniqueId());
            EmiliaAddItemMenu.removeEditItemId(player.getUniqueId());

            // Return to shop items menu
            goBackFromEmiliaAdd(player);

        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Error updating item in database.");
        }
    }

    private void deleteEmiliaItem(Player player) {
        int itemId = EmiliaAddItemMenu.getEditItemId(player.getUniqueId());
        if (itemId == -1) {
            player.sendMessage(ChatColor.RED + "Item ID not found.");
            return;
        }

        try (Connection conn = Main.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM emilia_items WHERE id = ?")) {

            ps.setInt(1, itemId);
            int deleted = ps.executeUpdate();

            if (deleted > 0) {
                player.sendMessage(ChatColor.GREEN + "Item has been deleted.");
            } else {
                player.sendMessage(ChatColor.RED + "Item not found in database.");
            }

            // Clear temporary data
            TemporaryData.removeCost(player.getUniqueId());
            TemporaryData.removePlayerData(player.getUniqueId(), "emilia_daily_limit");
            EmiliaAddItemMenu.removeGuiState(player.getUniqueId());
            EmiliaAddItemMenu.removeEditItemId(player.getUniqueId());

            // Return to shop items menu
            goBackFromEmiliaAdd(player);

        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Error deleting item from database.");
        }
    }

    private void goBackFromEmiliaAdd(Player player) {
        String category = EmiliaAddItemMenu.getCategory(player.getUniqueId());
        if (category != null) {
            String[] parts = category.split("_");
            if (parts.length >= 3) {
                String shopType = parts[1].equals("shop") ? "Shop" : "Event Shop";
                String tierType = capitalizeFirstLetter(parts[parts.length - 1]);

                EmiliaShopItemsMenu.openEditor(player, shopType, tierType, 0);
                EmiliaAddItemMenu.removeCategory(player.getUniqueId());
            } else {
                player.closeInventory();
            }
        } else {
            player.closeInventory();
        }
    }

    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    // Zumpe Shop handlers

    private void handleZumpeShopMenu(InventoryClickEvent event, Player player, ItemStack clickedItem, String itemName) {
        if (itemName == null) return;

        // Handle clicks in the Zumpe shop menu
        // Since Zumpe shop opens directly without categories, we can handle item purchases here

        // Check if the clicked item is a shop item (not a glass pane)
        if (clickedItem != null && clickedItem.getType() != Material.BLACK_STAINED_GLASS_PANE) {
            ItemMeta meta = clickedItem.getItemMeta();
            if (meta != null) {
                NamespacedKey key = new NamespacedKey(Main.getInstance(), "item_id");
                if (meta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
                    int itemId = meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);

                    // Use UnifiedConfirmationMenu
                    UnifiedConfirmationMenu.open(player, itemId, UnifiedConfirmationMenu.ShopType.ZUMPE);

                    if (Main.getInstance().getConfig().getInt("debug", 0) == 1) {
                        Bukkit.getLogger().info("[MenuListener] Opening unified confirmation for Zumpe item: " + itemId);
                    }
                }
            }
        }
    }

    private void handleEditZumpeShopMenu(InventoryClickEvent event, Player player, ItemStack clickedItem, String itemName) {
        if (itemName == null) return;

        // Handle clicks in the edit mode of Zumpe shop

        if (itemName.equals("Add Item")) {
            // Open add item menu
            ZumpeAddItemMenu.open(player);
            return;
        }

        // Check if the clicked item is a shop item (not a glass pane)
        if (clickedItem != null && clickedItem.getType() != Material.BLACK_STAINED_GLASS_PANE) {
            ItemMeta meta = clickedItem.getItemMeta();
            if (meta != null) {
                NamespacedKey key = new NamespacedKey(Main.getInstance(), "item_id");
                if (meta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
                    int itemId = meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
                    // Open edit item menu
                    ZumpeAddItemMenu.openEdit(player, itemId);
                }
            }
        }
    }

    private void handleZumpeAddEditItemMenu(InventoryClickEvent event, Player player, ItemStack clickedItem, String itemName, String title) {
        Inventory inv = event.getInventory();
        int slot = event.getRawSlot();
        boolean isTopInventory = (event.getView().getTopInventory() == event.getClickedInventory());
        boolean isEditMode = title.equals("Edit Zumpe Item");

        if (isTopInventory) {
            // Allow placing items in slots 0-9 (required items) and slot 13 (result item)
            if ((slot >= 0 && slot <= 9) || slot == 13) {
                // Handle placeholder removal
                if (clickedItem != null && itemName != null) {
                    Material mat = clickedItem.getType();
                    if (mat == Material.GRAY_STAINED_GLASS_PANE || mat == Material.LIGHT_BLUE_STAINED_GLASS_PANE) {
                        // Remove placeholder
                        inv.setItem(slot, null);
                        event.setCancelled(true);
                        return;
                    }
                }
                // Allow item placement/removal
                event.setCancelled(false);
                return;
            }

            // Handle cost and daily limit buttons
            if (itemName == null) return;

            switch (slot) {
                case 11: // Cost
                    ZumpeAddItemMenu.saveGuiState(player.getUniqueId(), inv.getContents());
                    ChatListener.setPlayerState(player.getUniqueId(), "entering_cost");
                    player.sendMessage(ChatColor.YELLOW + "Please enter the cost (e.g. 500, 500k, 1m).");
                    player.closeInventory();
                    break;

                case 15: // Daily Limit
                    ZumpeAddItemMenu.saveGuiState(player.getUniqueId(), inv.getContents());
                    ChatListener.setPlayerState(player.getUniqueId(), "entering_zumpe_daily_limit");
                    player.sendMessage(ChatColor.YELLOW + "Please enter the daily limit (0 = no limit).");
                    player.closeInventory();
                    break;

                case 22: // Save
                    if (isEditMode) {
                        updateZumpeItem(player, inv);
                    } else {
                        saveZumpeItem(player, inv);
                    }
                    break;

                case 23: // Delete (only in edit mode)
                    if (isEditMode && itemName.equals("Delete")) {
                        deleteZumpeItem(player);
                    }
                    break;

                case 24: // Back
                    player.closeInventory();
                    ZumpeMainMenu.openEditor(player);
                    break;
            }
        } else {
            // Clicking in player inventory
            event.setCancelled(false);
        }
    }

    private void saveZumpeItem(Player player, Inventory inv) {
        ItemStack item = inv.getItem(13);
        if (item == null || item.getType() == Material.LIGHT_BLUE_STAINED_GLASS_PANE) {
            player.sendMessage(ChatColor.RED + "You must set an item!");
            return;
        }

        // Get next available slot
        int nextSlot = 0;
        try (Connection conn = Main.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT MAX(slot) FROM zumpe_items")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getObject(1) != null) {
                nextSlot = rs.getInt(1) + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Error determining slot position.");
            return;
        }

        double cost = TemporaryData.getCost(player.getUniqueId());
        int dailyLimit = 0;
        Object limitObj = TemporaryData.getPlayerData(player.getUniqueId(), "zumpe_daily_limit");
        if (limitObj instanceof Integer) {
            dailyLimit = (Integer) limitObj;
        }

        try (Connection conn = Main.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO zumpe_items (slot, required_item_1, required_item_2, required_item_3, " +
                     "required_item_4, required_item_5, required_item_6, required_item_7, required_item_8, " +
                     "required_item_9, required_item_10, item, cost, daily_limit) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

            ps.setInt(1, nextSlot);

            // Required items (slots 0-9)
            for (int i = 0; i < 10; i++) {
                ItemStack requiredItem = inv.getItem(i);
                if (requiredItem != null && requiredItem.getType() != Material.GRAY_STAINED_GLASS_PANE) {
                    ps.setString(i + 2, ItemStackSerializer.serialize(requiredItem));
                } else {
                    ps.setString(i + 2, null);
                }
            }

            ps.setString(12, ItemStackSerializer.serialize(item));
            ps.setDouble(13, cost);
            ps.setInt(14, dailyLimit);

            ps.executeUpdate();
            player.sendMessage(ChatColor.GREEN + "Item has been added to the shop.");

            // Clear temporary data
            TemporaryData.removeCost(player.getUniqueId());
            TemporaryData.removePlayerData(player.getUniqueId(), "zumpe_daily_limit");
            ZumpeAddItemMenu.removeGuiState(player.getUniqueId());

            // Return to shop menu
            player.closeInventory();
            ZumpeMainMenu.openEditor(player);

        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Error saving item to database.");
        }
    }

    private void updateZumpeItem(Player player, Inventory inv) {
        int itemId = ZumpeAddItemMenu.getEditItemId(player.getUniqueId());
        if (itemId == -1) {
            player.sendMessage(ChatColor.RED + "Item ID not found.");
            return;
        }

        ItemStack item = inv.getItem(13);
        if (item == null || item.getType() == Material.LIGHT_BLUE_STAINED_GLASS_PANE) {
            player.sendMessage(ChatColor.RED + "You must set an item!");
            return;
        }

        double cost = TemporaryData.getCost(player.getUniqueId());
        int dailyLimit = 0;
        Object limitObj = TemporaryData.getPlayerData(player.getUniqueId(), "zumpe_daily_limit");
        if (limitObj instanceof Integer) {
            dailyLimit = (Integer) limitObj;
        }

        try (Connection conn = Main.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE zumpe_items SET required_item_1 = ?, required_item_2 = ?, required_item_3 = ?, " +
                     "required_item_4 = ?, required_item_5 = ?, required_item_6 = ?, required_item_7 = ?, " +
                     "required_item_8 = ?, required_item_9 = ?, required_item_10 = ?, " +
                     "item = ?, cost = ?, daily_limit = ? WHERE id = ?")) {

            // Required items (slots 0-9)
            for (int i = 0; i < 10; i++) {
                ItemStack requiredItem = inv.getItem(i);
                if (requiredItem != null && requiredItem.getType() != Material.GRAY_STAINED_GLASS_PANE) {
                    ps.setString(i + 1, ItemStackSerializer.serialize(requiredItem));
                } else {
                    ps.setString(i + 1, null);
                }
            }

            ps.setString(11, ItemStackSerializer.serialize(item));
            ps.setDouble(12, cost);
            ps.setInt(13, dailyLimit);
            ps.setInt(14, itemId);

            int updated = ps.executeUpdate();
            if (updated > 0) {
                player.sendMessage(ChatColor.GREEN + "Item has been updated.");
            } else {
                player.sendMessage(ChatColor.RED + "Item not found in database.");
            }

            // Clear temporary data
            TemporaryData.removeCost(player.getUniqueId());
            TemporaryData.removePlayerData(player.getUniqueId(), "zumpe_daily_limit");
            ZumpeAddItemMenu.removeGuiState(player.getUniqueId());
            ZumpeAddItemMenu.removeEditItemId(player.getUniqueId());

            // Return to shop menu
            player.closeInventory();
            ZumpeMainMenu.openEditor(player);

        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Error updating item in database.");
        }
    }

    private void deleteZumpeItem(Player player) {
        int itemId = ZumpeAddItemMenu.getEditItemId(player.getUniqueId());
        if (itemId == -1) {
            player.sendMessage(ChatColor.RED + "Item ID not found.");
            return;
        }

        try (Connection conn = Main.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM zumpe_items WHERE id = ?")) {

            ps.setInt(1, itemId);
            int deleted = ps.executeUpdate();

            if (deleted > 0) {
                player.sendMessage(ChatColor.GREEN + "Item has been deleted.");
            } else {
                player.sendMessage(ChatColor.RED + "Item not found in database.");
            }

            // Clear temporary data
            TemporaryData.removeCost(player.getUniqueId());
            TemporaryData.removePlayerData(player.getUniqueId(), "zumpe_daily_limit");
            ZumpeAddItemMenu.removeGuiState(player.getUniqueId());
            ZumpeAddItemMenu.removeEditItemId(player.getUniqueId());

            // Return to shop menu
            player.closeInventory();
            ZumpeMainMenu.openEditor(player);

        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Error deleting item from database.");
        }
    }

    private void handleEmiliaConfirmationMenu(InventoryClickEvent event, Player player, ItemStack clickedItem, String itemName) {
        if (itemName == null) return;

        if (itemName.equals("Back")) {
            // Return to shop items menu
            String shopType = (String) TemporaryData.getPlayerData(player.getUniqueId(), "emilia_shop_type");
            String tierType = (String) TemporaryData.getPlayerData(player.getUniqueId(), "emilia_tier_type");
            String category = "emilia_" + shopType.toLowerCase().replace(" ", "_") + "_" + tierType.toLowerCase();
            int currentPage = TemporaryData.getPage(player.getUniqueId(), category);
            EmiliaShopItemsMenu.open(player, shopType, tierType, currentPage);
        } else if (itemName.equals("Exchange") || itemName.equals("Craft")) { // Handle both button types
            // Perform exchange with improved transaction manager
            performEmiliaExchangeImproved(player, event.getInventory());
        }
    }

    private void handleZumpeConfirmationMenu(InventoryClickEvent event, Player player, ItemStack clickedItem, String itemName) {
        if (itemName == null) return;

        if (itemName.equals("Back")) {
            // Return to Zumpe shop
            ZumpeMainMenu.open(player);
        } else if (itemName.equals("Exchange") || itemName.equals("Craft")) { // Handle both button types
            // Perform exchange with improved transaction manager
            performZumpeExchangeImproved(player, event.getInventory());
        }
    }

    private void performEmiliaExchange(Player player, Inventory inv) {
        ItemStack resultItem = inv.getItem(22);
        if (resultItem == null || resultItem.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "Invalid item.");
            return;
        }

        ItemMeta meta = resultItem.getItemMeta();
        if (meta == null) {
            player.sendMessage(ChatColor.RED + "Item not found.");
            return;
        }

        NamespacedKey key = new NamespacedKey(Main.getInstance(), "emilia_item_id");
        Integer itemId = meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);

        if (itemId == null) {
            player.sendMessage(ChatColor.RED + "Item not found.");
            return;
        }

        // Process the purchase
        boolean success = EmiliaTransactionManager.processPurchase(player, itemId);
        // Don't close inventory to allow multiple transactions
    }

    private void performEmiliaExchangeImproved(Player player, Inventory inv) {
        ItemStack resultItem = inv.getItem(22);
        if (resultItem == null || resultItem.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "Invalid item.");
            return;
        }

        ItemMeta meta = resultItem.getItemMeta();
        if (meta == null) {
            player.sendMessage(ChatColor.RED + "Item not found.");
            return;
        }

        NamespacedKey key = new NamespacedKey(Main.getInstance(), "emilia_item_id");
        Integer itemId = meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);

        if (itemId == null) {
            player.sendMessage(ChatColor.RED + "Item not found.");
            return;
        }

        // Debug mode
        if (Main.getInstance().getConfig().getInt("debug", 0) == 1) {
            Bukkit.getLogger().info("[MenuListener] Starting Emilia exchange for player: " + player.getName() + 
                                  ", itemId: " + itemId);
            TransactionManager.testDailyLimit(player, itemId, "emilia");
        }

        // Process the purchase
        boolean success = EmiliaTransactionManager.processPurchase(player, itemId);

        // Debug message instead of closing inventory
        if (success) {
            if (Main.getInstance().getConfig().getInt("debug", 0) == 1) {
                Bukkit.getLogger().info("[Emilia] Exchange success - keeping menu open");
            }

            // Refresh the menu to show updated limits
            UnifiedConfirmationMenu.refresh(player, itemId, UnifiedConfirmationMenu.ShopType.EMILIA);
        }
    }

    private void performZumpeExchange(Player player, Inventory inv) {
        ItemStack resultItem = inv.getItem(22);
        if (resultItem == null || resultItem.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "Invalid item.");
            return;
        }

        ItemMeta meta = resultItem.getItemMeta();
        if (meta == null) {
            player.sendMessage(ChatColor.RED + "Item not found.");
            return;
        }

        NamespacedKey key = new NamespacedKey(Main.getInstance(), "zumpe_item_id");
        Integer itemId = meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);

        if (itemId == null) {
            player.sendMessage(ChatColor.RED + "Item not found.");
            return;
        }

        // Process the purchase
        boolean success = ZumpeTransactionManager.processPurchase(player, itemId);
        // Don't close inventory to allow multiple transactions
    }

    private void performZumpeExchangeImproved(Player player, Inventory inv) {
        ItemStack resultItem = inv.getItem(22);
        if (resultItem == null || resultItem.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "Invalid item.");
            return;
        }

        ItemMeta meta = resultItem.getItemMeta();
        if (meta == null) {
            player.sendMessage(ChatColor.RED + "Item not found.");
            return;
        }

        NamespacedKey key = new NamespacedKey(Main.getInstance(), "zumpe_item_id");
        Integer itemId = meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);

        if (itemId == null) {
            player.sendMessage(ChatColor.RED + "Item not found.");
            return;
        }

        // Debug mode
        if (Main.getInstance().getConfig().getInt("debug", 0) == 1) {
            Bukkit.getLogger().info("[MenuListener] Starting Zumpe exchange for player: " + player.getName() + 
                                  ", itemId: " + itemId);
            TransactionManager.testDailyLimit(player, itemId, "zumpe");
        }

        // Process the purchase
        boolean success = ZumpeTransactionManager.processPurchase(player, itemId);

        // Debug message instead of closing inventory
        if (success) {
            if (Main.getInstance().getConfig().getInt("debug", 0) == 1) {
                Bukkit.getLogger().info("[Zumpe] Exchange success - keeping menu open");
            }

            // Refresh the menu to show updated limits
            UnifiedConfirmationMenu.refresh(player, itemId, UnifiedConfirmationMenu.ShopType.ZUMPE);
        }
    }
}
