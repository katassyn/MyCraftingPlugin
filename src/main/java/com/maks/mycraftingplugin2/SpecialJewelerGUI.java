package com.maks.mycraftingplugin2;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Special Jeweler GUI - displays special jewelry crafting recipes.
 * Similar to CategoryMenu but with a single page of recipes.
 */
public class SpecialJewelerGUI implements InventoryHolder {
    private final Inventory inventory;
    private final Player player;
    private final boolean isEditMode;
    private static final String CATEGORY = "special_jeweler";
    private static final int ITEMS_PER_PAGE = 45; // Slots 0-44 for items

    public SpecialJewelerGUI(Player player, boolean isEditMode) {
        this.player = player;
        this.isEditMode = isEditMode;

        String title = isEditMode
            ? ChatColor.GOLD + "Edit Special Jeweler"
            : ChatColor.DARK_PURPLE + "Special Jeweler";

        this.inventory = Bukkit.createInventory(this, 54, title);
        setupGUI();
    }

    private void setupGUI() {
        // Load recipes from database first
        try (Connection conn = Main.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT * FROM recipes WHERE category = ? ORDER BY id ASC")) {

            ps.setString(1, CATEGORY);
            ResultSet rs = ps.executeQuery();

            List<ItemStack> items = new ArrayList<>();

            while (rs.next()) {
                ItemStack resultItem = ItemStackSerializer.deserialize(rs.getString("result_item"));
                int recipeId = rs.getInt("id");

                // Store recipe ID in PersistentDataContainer
                ItemMeta meta = resultItem.getItemMeta();
                if (meta != null) {
                    // Add ID as persistent data
                    NamespacedKey key = new NamespacedKey(Main.getInstance(), "recipe_id");
                    meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, recipeId);

                    // Display ID in lore
                    List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
                    lore.add(ChatColor.GRAY + "Recipe ID: " + String.valueOf(recipeId));
                    meta.setLore(lore);
                    resultItem.setItemMeta(meta);
                }

                items.add(resultItem);
            }

            // Place items in slots 0-44
            int endIndex = Math.min(ITEMS_PER_PAGE, items.size());
            for (int i = 0; i < endIndex; i++) {
                inventory.setItem(i, items.get(i));
            }

            // Debug logging
            if (Main.getInstance().getConfig().getInt("debug", 0) == 1 || items.isEmpty()) {
                Bukkit.getLogger().info("[SpecialJewelerGUI] Loaded " + items.size() + " recipes from category 'special_jeweler'");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Error loading special jeweler recipes!");
        }

        // Back button - closes the GUI
        inventory.setItem(49, createMenuItem(Material.BARRIER, ChatColor.RED + "Back"));

        // Fill empty slots with glass panes (after loading recipes)
        fillWithGlass();
    }

    private void fillWithGlass() {
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            glass.setItemMeta(meta);
        }

        // Fill all empty slots with glass
        for (int i = 0; i < 54; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, glass);
            }
        }
    }

    private ItemStack createMenuItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public static String getCategory() {
        return CATEGORY;
    }
}
