package com.maks.mycraftingplugin2;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.sql.*;
import java.util.*;

public class MenuListener implements Listener {


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
        }else if (title.equals("Alchemy Menu")) {
            handleAlchemyMenuClick(player, itemName);
        }
        else if (title.equals("Edit Alchemy Menu")) {
            handleEditAlchemyMenuClick(player, itemName);
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
                ||title.equals("Alchemy Menu")
                || title.equals("Edit Alchemy Menu");
    }
    // Method to handle "Add New Recipe" and "Edit Recipe" inventories
    private void handleAddEditRecipe(InventoryClickEvent event, Player player, ItemStack clickedItem, String itemName, String title) {
        Inventory inv = event.getInventory();
        int slot = event.getRawSlot();
        boolean isTopInventory = event.getView().getTopInventory() == event.getClickedInventory();

        if (isTopInventory) {
            // Allow interactions in specific slots
            if ((slot >= 0 && slot <= 9) || slot == 13) {
                event.setCancelled(false); // Allow item movement
            } else {
                // Handle buttons and other interactions
                if (itemName == null) return;

                if (slot == 20) {
                    // Set success chance
                    // Save GUI state before closing
                    AddRecipeMenu.saveGuiState(player.getUniqueId(), inv.getContents());

                    ChatListener.setPlayerState(player.getUniqueId(), "entering_success_chance");
                    player.sendMessage(ChatColor.YELLOW + "Please enter the success chance (e.g., 55% or 0.5%):");
                    player.closeInventory();
                } else if (slot == 21) {
                    // Set cost
                    // Save GUI state before closing
                    AddRecipeMenu.saveGuiState(player.getUniqueId(), inv.getContents());

                    ChatListener.setPlayerState(player.getUniqueId(), "entering_cost");
                    player.sendMessage(ChatColor.YELLOW + "Please enter the cost (e.g., 500, 500k, 1kk):");
                    player.closeInventory();
                } else if (slot == 22) {
                    // Save recipe
                    if (itemName.equals("Save")) {
                        if (title.equals("Add New Recipe")) {
                            saveRecipe(player, inv);
                            AddRecipeMenu.removeCategory(player.getUniqueId());
                        } else if (title.equals("Edit Recipe")) {
                            updateRecipe(player, inv);
                            EditRecipeMenu.removeRecipeId(player.getUniqueId());
                        }
                    }
                } else if (slot == 23 && title.equals("Edit Recipe")) {
                    // Delete recipe
                    deleteRecipe(player);
                    EditRecipeMenu.removeRecipeId(player.getUniqueId());
                } else if (slot == 24) {
                    // Back button
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
                }
            }
        } else {
            // Clicked in the player's own inventory
            event.setCancelled(false); // Allow interaction
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
    private void handleCategoryMenu(InventoryClickEvent event, Player player, ItemStack clickedItem, String itemName, String title) {
        Inventory inv = event.getInventory();
        int slot = event.getRawSlot();
        boolean isTopInventory = event.getView().getTopInventory() == event.getClickedInventory();

        if (itemName == null) return;

        String category = title.contains(": ") ? title.split(": ")[1] : "";

        if (title.startsWith("Edit Category: ")) {
            // Editing category
            if (isTopInventory) {
                // Allow moving items in slots 0-44
                if (slot >= 0 && slot <= 44) {
                    event.setCancelled(false);
                }

                if (itemName.equals("Back")) {
                    // Back button logic
                    if (category.startsWith("q")) {
                        String qLevel = category.split("_")[0];
                        DifficultyMenu.openEditor(player, qLevel);
                    } else {
                        MainMenu.openEditor(player);
                    }
                } else if (itemName.equals("Next Page")) {
                    int currentPage = TemporaryData.getPage(player.getUniqueId(), category);
                    TemporaryData.setPage(player.getUniqueId(), category, currentPage + 1);
                    CategoryMenu.openEditor(player, category, currentPage + 1);
                } else if (itemName.equals("Previous Page")) {
                    int currentPage = TemporaryData.getPage(player.getUniqueId(), category);
                    TemporaryData.setPage(player.getUniqueId(), category, currentPage - 1);
                    CategoryMenu.openEditor(player, category, currentPage - 1);
                } else if (itemName.equals("Add Recipe")) {
                    if (!player.hasPermission("mycraftingplugin.add")) {
                        player.sendMessage(ChatColor.RED + "You don't have permission to add recipes.");
                        return;
                    }
                    AddRecipeMenu.open(player, category);
                } else {
                    // Clicked on a recipe - open edit interface
                    ItemMeta meta = clickedItem.getItemMeta();
                    if (meta == null || !meta.hasLore()) {
                        player.sendMessage(ChatColor.RED + "Invalid recipe.");
                        return;
                    }

                    List<String> lore = meta.getLore();
                    int recipeId = -1;
                    for (String line : lore) {
                        if (ChatColor.stripColor(line).startsWith("Recipe ID: ")) {
                            String idString = ChatColor.stripColor(line).replace("Recipe ID: ", "").trim();
                            recipeId = Integer.parseInt(idString);
                            break;
                        }
                    }

                    if (recipeId == -1) {
                        player.sendMessage(ChatColor.RED + "Invalid recipe.");
                        return;
                    }

                    // Open edit recipe menu
                    EditRecipeMenu.open(player, recipeId);
                }

                // Save new layout after moving items
                Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                    saveCategoryLayout(inv, category);
                }, 1L);
            } else {
                event.setCancelled(false); // Allow interaction with player's inventory
            }
        } else {
            // Viewing category
            if (itemName.equals("Back")) {
                if (category.startsWith("q")) {
                    String qLevel = category.split("_")[0];
                    DifficultyMenu.open(player, qLevel);
                } else {
                    MainMenu.open(player);
                }
            } else if (itemName.equals("Next Page")) {
                int currentPage = TemporaryData.getPage(player.getUniqueId(), category);
                TemporaryData.setPage(player.getUniqueId(), category, currentPage + 1);
                CategoryMenu.open(player, category, currentPage + 1);
            } else if (itemName.equals("Previous Page")) {
                int currentPage = TemporaryData.getPage(player.getUniqueId(), category);
                TemporaryData.setPage(player.getUniqueId(), category, currentPage - 1);
                CategoryMenu.open(player, category, currentPage - 1);
            } else {
                // Clicked on a recipe - open crafting scheme
                ItemMeta meta = clickedItem.getItemMeta();
                if (meta == null || !meta.hasLore()) {
                    player.sendMessage(ChatColor.RED + "Invalid recipe.");
                    return;
                }

                List<String> lore = meta.getLore();
                int recipeId = -1;
                for (String line : lore) {
                    if (ChatColor.stripColor(line).startsWith("Recipe ID: ")) {
                        String idString = ChatColor.stripColor(line).replace("Recipe ID: ", "").trim();
                        recipeId = Integer.parseInt(idString);
                        break;
                    }
                }

                if (recipeId == -1) {
                    player.sendMessage(ChatColor.RED + "Invalid recipe.");
                    return;
                }

                // Open crafting scheme
                TemporaryData.setLastCategory(player.getUniqueId(), category);
                CraftingSchemeMenu.open(player, clickedItem, recipeId);
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
            ItemStack resultItem = inv.getItem(22); // Assuming the result item is in slot 22

            if (resultItem == null || resultItem.getType() == Material.AIR) {
                player.sendMessage(ChatColor.RED + "Invalid recipe.");
                return;
            }

            // Retrieve the Recipe ID from the item's PersistentDataContainer
            ItemMeta meta = resultItem.getItemMeta();
            if (meta == null) {
                player.sendMessage(ChatColor.RED + "Recipe not found.");
                return;
            }

            NamespacedKey key = new NamespacedKey(Main.getInstance(), "recipe_id");
            Integer recipeId = meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);

            if (recipeId == null) {
                player.sendMessage(ChatColor.RED + "Recipe not found.");
                return;
            }

            // Obtain database connection
            Connection conn = Main.getConnection();
            if (conn == null) {
                player.sendMessage(ChatColor.RED + "Database connection error.");
                return;
            }

            // Fetch recipe details from the database
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM recipes WHERE id = ?");
            ps.setInt(1, recipeId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Check if the player has the required items
                boolean hasItems = true;
                Map<Integer, ItemStack> requiredItems = new HashMap<>();
                for (int i = 0; i < 10; i++) {
                    String itemData = rs.getString("required_item_" + (i + 1));
                    if (itemData != null) {
                        ItemStack requiredItem = ItemStackSerializer.deserialize(itemData);
                        requiredItems.put(i, requiredItem);
                        int requiredAmount = requiredItem.getAmount();

                        int playerAmount = getTotalItemAmount(player, requiredItem);

                        if (playerAmount < requiredAmount) {
                            hasItems = false;
                            break;
                        }
                    }
                }

                if (!hasItems) {
                    player.sendMessage(ChatColor.RED + "You don't have all the required items.");
                    return;
                }

                // Check if the player has enough money
                double cost = rs.getDouble("cost");
                if (Main.getEconomy().getBalance(player) < cost) {
                    player.sendMessage(ChatColor.RED + "You don't have enough money.");
                    return;
                }

                // Remove items from the player
                for (ItemStack requiredItem : requiredItems.values()) {
                    removeItems(player, requiredItem);
                }

                // Deduct the cost
                Main.getEconomy().withdrawPlayer(player, cost);

                // Calculate success chance
                double successChance = rs.getDouble("success_chance");
                if (Math.random() * 100 <= successChance) {
                    // Success - give the item to the player
                    player.getInventory().addItem(resultItem);
                    player.sendMessage(ChatColor.GREEN + "Crafting successful!");
                    player.closeInventory();
                } else {
                    // Failure
                    player.sendMessage(ChatColor.RED + "Crafting failed.");
                    // Keep the GUI open
                }

            } else {
                player.sendMessage(ChatColor.RED + "Recipe not found.");
                // Keep the GUI open
            }

        } catch (Exception e) {
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
        try {
            Connection conn = Main.getConnection();
            if (conn == null) {
                player.sendMessage(ChatColor.RED + "Database connection error.");
                return;
            }

            String category = AddRecipeMenu.getCategory(player.getUniqueId());

            if (category == null) {
                player.sendMessage(ChatColor.RED + "Category not found.");
                return;
            }

            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO recipes (category, required_item_1, required_item_2, required_item_3, required_item_4, required_item_5, required_item_6, required_item_7, required_item_8, required_item_9, required_item_10, result_item, success_chance, cost) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );

            ps.setString(1, category);

            // Wymagane przedmioty (sloty 0-9)
            for (int i = 0; i < 10; i++) {
                ItemStack item = inv.getItem(i);
                if (item != null && item.getType() != Material.GRAY_STAINED_GLASS_PANE) {
                    ps.setString(i + 2, ItemStackSerializer.serialize(item));
                } else {
                    ps.setString(i + 2, null);
                }
            }

            // Przedmiot wynikowy (slot 13)
            ItemStack resultItem = inv.getItem(13);
            if (resultItem != null && resultItem.getType() != Material.LIGHT_BLUE_STAINED_GLASS_PANE) {
                ps.setString(12, ItemStackSerializer.serialize(resultItem));
            } else {
                player.sendMessage(ChatColor.RED + "You must set a result item!");
                return;
            }

            // Szansa na sukces
            double successChance = TemporaryData.getSuccessChance(player.getUniqueId());
            ps.setDouble(13, successChance);

            // Koszt
            double cost = TemporaryData.getCost(player.getUniqueId());
            ps.setDouble(14, cost);

            ps.executeUpdate();
            player.sendMessage(ChatColor.GREEN + "Recipe has been saved.");

            // Wyczyść dane tymczasowe
            TemporaryData.removeSuccessChance(player.getUniqueId());
            TemporaryData.removeCost(player.getUniqueId());

            // Usuń zapisany stan GUI
            AddRecipeMenu.removeGuiState(player.getUniqueId());

            player.closeInventory();

        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "An error occurred while saving the recipe.");
        }
    }


    // Metoda updateRecipe
    private void updateRecipe(Player player, Inventory inv) {
        try {
            Connection conn = Main.getConnection();
            if (conn == null) {
                player.sendMessage(ChatColor.RED + "Database connection error.");
                return;
            }

            int recipeId = EditRecipeMenu.getRecipeId(player.getUniqueId());

            if (recipeId == -1) {
                player.sendMessage(ChatColor.RED + "Recipe ID not found.");
                return;
            }

            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE recipes SET required_item_1 = ?, required_item_2 = ?, required_item_3 = ?, required_item_4 = ?, required_item_5 = ?, required_item_6 = ?, required_item_7 = ?, required_item_8 = ?, required_item_9 = ?, required_item_10 = ?, result_item = ?, success_chance = ?, cost = ? WHERE id = ?"
            );

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

            // Szansa na sukces
            double successChance = TemporaryData.getSuccessChance(player.getUniqueId());
            ps.setDouble(12, successChance);

            // Koszt
            double cost = TemporaryData.getCost(player.getUniqueId());
            ps.setDouble(13, cost);

            // ID receptury
            ps.setInt(14, recipeId);

            ps.executeUpdate();
            player.sendMessage(ChatColor.GREEN + "Recipe has been updated.");

            // Wyczyść dane tymczasowe
            TemporaryData.removeSuccessChance(player.getUniqueId());
            TemporaryData.removeCost(player.getUniqueId());

            // Usuń zapisany stan GUI
            AddRecipeMenu.removeGuiState(player.getUniqueId());

            player.closeInventory();

        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "An error occurred while updating the recipe.");
        }
    }

    // Metoda deleteRecipe
    private void deleteRecipe(Player player) {
        try {
            Connection conn = Main.getConnection();
            if (conn == null) {
                player.sendMessage(ChatColor.RED + "Database connection error.");
                return;
            }

            int recipeId = EditRecipeMenu.getRecipeId(player.getUniqueId());

            if (recipeId == -1) {
                player.sendMessage(ChatColor.RED + "Recipe ID not found.");
                return;
            }

            PreparedStatement ps = conn.prepareStatement("DELETE FROM recipes WHERE id = ?");
            ps.setInt(1, recipeId);
            ps.executeUpdate();

            player.sendMessage(ChatColor.GREEN + "Recipe has been deleted.");

            // Wyczyść dane tymczasowe
            TemporaryData.removeSuccessChance(player.getUniqueId());
            TemporaryData.removeCost(player.getUniqueId());

            // Usuń zapisany stan GUI
            AddRecipeMenu.removeGuiState(player.getUniqueId());

            player.closeInventory();

        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "An error occurred while deleting the recipe.");
        }
    }

    // Metoda saveCategoryLayout
    private void saveCategoryLayout(Inventory inv, String category) {
        try {
            Connection conn = Main.getConnection();
            if (conn == null) {
                return;
            }

            // Najpierw pobierz wszystkie receptury z tej kategorii
            PreparedStatement selectPs = conn.prepareStatement("SELECT id, result_item FROM recipes WHERE category = ?");
            selectPs.setString(1, category);
            ResultSet rs = selectPs.executeQuery();

            Map<String, Integer> recipeIdMap = new HashMap<>();

            while (rs.next()) {
                int id = rs.getInt("id");
                String resultItemData = rs.getString("result_item");
                recipeIdMap.put(resultItemData, id);
            }

            PreparedStatement updatePs = conn.prepareStatement("UPDATE recipes SET slot = ? WHERE id = ?");

            for (int i = 0; i < 45; i++) {
                ItemStack item = inv.getItem(i);
                if (item != null && item.getType() != Material.WHITE_STAINED_GLASS_PANE) {
                    String itemData = ItemStackSerializer.serialize(item);
                    Integer recipeId = recipeIdMap.get(itemData);

                    if (recipeId != null) {
                        updatePs.setInt(1, i); // slot
                        updatePs.setInt(2, recipeId); // id
                        updatePs.addBatch();
                    }
                }
            }

            updatePs.executeBatch();

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
}
