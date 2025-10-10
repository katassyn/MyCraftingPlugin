package com.maks.mycraftingplugin2;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class HefajsosGUI implements InventoryHolder {
    private final Inventory inventory;
    private final Player player;

    public HefajsosGUI(Player player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 27, ChatColor.DARK_RED + "Hefajsos Services");
        setupGUI();
    }

    private void setupGUI() {
        // Fill with gray glass panes
        ItemStack grayPane = createGlassPane(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            if (i != 10 && i != 13 && i != 16) {
                inventory.setItem(i, grayPane);
            }
        }

        // Dungeon Shop button
        ItemStack dungeonShop = new ItemStack(Material.NETHER_BRICK);
        ItemMeta dungeonMeta = dungeonShop.getItemMeta();
        dungeonMeta.setDisplayName(ChatColor.RED + "Dungeon Shop");
        dungeonMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Access the mysterious dungeon",
            ChatColor.GRAY + "trading post with rare items",
            "",
            ChatColor.YELLOW + "Click to open!"
        ));
        dungeonShop.setItemMeta(dungeonMeta);
        inventory.setItem(10, dungeonShop);

        // Set Imprinting button
        ItemStack imprinting = new ItemStack(Material.ENCHANTING_TABLE);
        ItemMeta imprintMeta = imprinting.getItemMeta();
        imprintMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Set Imprinting");
        imprintMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Imprint set bonuses onto",
            ChatColor.GRAY + "your equipment using crystals",
            "",
            ChatColor.YELLOW + "Click to open!"
        ));
        imprinting.setItemMeta(imprintMeta);
        inventory.setItem(13, imprinting);

        // Set Crushing button
        ItemStack crushing = new ItemStack(Material.ANVIL);
        ItemMeta crushMeta = crushing.getItemMeta();
        crushMeta.setDisplayName(ChatColor.GOLD + "Set Crushing");
        crushMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Crush set items and imprinted",
            ChatColor.GRAY + "items to recover 20% of crystals",
            "",
            ChatColor.YELLOW + "Click to open!"
        ));
        crushing.setItemMeta(crushMeta);
        inventory.setItem(16, crushing);
    }

    private ItemStack createGlassPane(Material material, String name) {
        ItemStack pane = new ItemStack(material);
        ItemMeta meta = pane.getItemMeta();
        meta.setDisplayName(name);
        pane.setItemMeta(meta);
        return pane;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public Player getPlayer() {
        return player;
    }
}