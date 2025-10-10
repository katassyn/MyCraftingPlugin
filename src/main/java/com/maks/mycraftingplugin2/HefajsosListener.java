package com.maks.mycraftingplugin2;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.permissions.PermissionAttachment;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HefajsosListener implements Listener {
    
    private static final Map<UUID, PermissionAttachment> tempPermissions = new HashMap<>();
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        
        if (!(inventory.getHolder() instanceof HefajsosGUI)) return;
        
        event.setCancelled(true); // Cancel all clicks in this GUI
        
        int slot = event.getRawSlot();
        
        // Handle button clicks
        if (slot == 10) { // Dungeon Shop
            player.closeInventory();
            
            // Give temporary permission and execute command
            giveTemporaryPermission(player, "mycraftingplugin.use");
            
            // Execute dungeon_shop command
            player.performCommand("dungeon_shop");
            
            // Remove permission after short delay
            Main.getInstance().getServer().getScheduler().runTaskLater(Main.getInstance(), () -> {
                removeTemporaryPermission(player);
            }, 20L); // Remove after 1 second
            
        } else if (slot == 13) { // Set Imprinting
            player.closeInventory();
            
            // Give temporary permission and execute command
            giveTemporaryPermission(player, "mycraftingplugin.use");
            
            // Execute imprinting command
            player.performCommand("imprinting");
            
            // Remove permission after short delay
            Main.getInstance().getServer().getScheduler().runTaskLater(Main.getInstance(), () -> {
                removeTemporaryPermission(player);
            }, 20L); // Remove after 1 second
            
        } else if (slot == 16) { // Set Crushing
            player.closeInventory();
            
            // Give temporary permission and execute command
            giveTemporaryPermission(player, "mycraftingplugin.use");
            
            // Execute set_crushing command
            player.performCommand("set_crushing");
            
            // Remove permission after short delay
            Main.getInstance().getServer().getScheduler().runTaskLater(Main.getInstance(), () -> {
                removeTemporaryPermission(player);
            }, 20L); // Remove after 1 second
        }
    }
    
    private void giveTemporaryPermission(Player player, String permission) {
        // Remove existing temporary permission if any
        removeTemporaryPermission(player);
        
        // Add new temporary permission
        PermissionAttachment attachment = player.addAttachment(Main.getInstance());
        attachment.setPermission(permission, true);
        tempPermissions.put(player.getUniqueId(), attachment);
    }
    
    private void removeTemporaryPermission(Player player) {
        PermissionAttachment attachment = tempPermissions.remove(player.getUniqueId());
        if (attachment != null) {
            attachment.remove();
        }
    }
    
    // Clean up permissions on player quit (should be added to a PlayerQuitEvent listener)
    public static void cleanupPlayer(UUID playerId) {
        PermissionAttachment attachment = tempPermissions.remove(playerId);
        if (attachment != null) {
            attachment.remove();
        }
    }
}