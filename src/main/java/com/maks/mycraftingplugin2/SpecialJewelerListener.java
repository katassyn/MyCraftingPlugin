package com.maks.mycraftingplugin2;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/**
 * Listener for Special Jeweler GUI interactions.
 */
public class SpecialJewelerListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();

        if (!(inventory.getHolder() instanceof SpecialJewelerGUI)) return;

        SpecialJewelerGUI gui = (SpecialJewelerGUI) inventory.getHolder();
        int slot = event.getRawSlot();

        // Cancel all clicks in the GUI
        event.setCancelled(true);

        // If player clicked in bottom inventory (their own inventory), ignore
        if (slot >= 54) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;

        String itemName = null;
        if (clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName()) {
            itemName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
        }

        // Handle Back button (slot 49)
        if (slot == 49 && "Back".equals(itemName)) {
            player.closeInventory();
            return;
        }

        // Handle recipe clicks (slots 0-44)
        if (slot >= 0 && slot <= 44) {
            boolean isEditMode = gui.isEditMode();

            // In edit mode, allow moving items in slots 0-44
            if (isEditMode) {
                event.setCancelled(false);
                return;
            }

            // In normal mode, open the crafting scheme
            if (clickedItem.hasItemMeta()) {
                ItemMeta meta = clickedItem.getItemMeta();
                NamespacedKey key = new NamespacedKey(Main.getInstance(), "recipe_id");
                Integer recipeId = meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);

                if (recipeId != null) {
                    // Store the category for crafting
                    TemporaryData.setLastCategory(player.getUniqueId(), SpecialJewelerGUI.getCategory());
                    TemporaryData.setPage(player.getUniqueId(), SpecialJewelerGUI.getCategory(), 0);

                    // Open crafting scheme with this item
                    CraftingSchemeMenu.open(player, clickedItem, recipeId);
                }
            }
        }
    }
}
