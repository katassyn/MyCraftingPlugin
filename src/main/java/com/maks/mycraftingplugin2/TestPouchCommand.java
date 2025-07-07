package com.maks.mycraftingplugin2;

import com.maks.mycraftingplugin2.integration.PouchIntegrationHelper;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Test command to check pouch integration
 * Usage: /testpouch
 */
public class TestPouchCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("mycraftingplugin.testpouch")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        player.sendMessage(ChatColor.GOLD + "=== Pouch Integration Test ===");
        player.sendMessage(ChatColor.YELLOW + "API Available: " + 
                          (PouchIntegrationHelper.isAPIAvailable() ? ChatColor.GREEN + "YES" : ChatColor.RED + "NO"));

        if (args.length == 0) {
            player.sendMessage(ChatColor.GRAY + "Usage:");
            player.sendMessage(ChatColor.GRAY + "/testpouch - Show this help");
            player.sendMessage(ChatColor.GRAY + "/testpouch check - Check item in main hand");
            player.sendMessage(ChatColor.GRAY + "/testpouch inventory - Check all inventory items");
            return true;
        }

        if (args[0].equalsIgnoreCase("check")) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item == null || item.getType().isAir()) {
                player.sendMessage(ChatColor.RED + "Hold an item in your main hand!");
                return true;
            }

            // Send debug info
            PouchIntegrationHelper.sendDebugInfo(player, item);
            
        } else if (args[0].equalsIgnoreCase("inventory")) {
            player.sendMessage(ChatColor.YELLOW + "=== Inventory Check ===");
            
            int slot = 0;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && !item.getType().isAir()) {
                    int total = PouchIntegrationHelper.getTotalItemAmount(player, item);
                    if (total > 0) {
                        player.sendMessage(ChatColor.GRAY + "Slot " + slot + ": " + 
                                         ChatColor.WHITE + item.getType() + 
                                         ChatColor.GRAY + " - Total: " + ChatColor.GREEN + total);
                    }
                }
                slot++;
            }
        }

        return true;
    }
}