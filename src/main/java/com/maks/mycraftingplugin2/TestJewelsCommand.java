package com.maks.mycraftingplugin2;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /testjewels - Test command for jewel detection system
 */
public class TestJewelsCommand implements CommandExecutor {

    private static final int debuggingFlag = 1; // Set to 0 to disable debug

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("mycraftingplugin.testjewels")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (debuggingFlag != 1) {
            player.sendMessage(ChatColor.RED + "Debug mode is disabled. This command requires debug mode to be enabled.");
            return true;
        }

        if (args.length == 0) {
            // Run jewel detection test
            JewelsCrushingMenu.testJewelDetection(player);
        } else if (args[0].equalsIgnoreCase("help")) {
            sendHelpMessage(player);
        } else {
            player.sendMessage(ChatColor.RED + "Unknown argument. Use '/testjewels help' for usage.");
        }

        return true;
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Test Jewels Command Help ===");
        player.sendMessage(ChatColor.YELLOW + "/testjewels" + ChatColor.WHITE + " - Test jewel detection on your inventory");
        player.sendMessage(ChatColor.YELLOW + "/testjewels help" + ChatColor.WHITE + " - Show this help message");
        player.sendMessage(ChatColor.GRAY + "This command helps test the improved jewel crushing system.");
        player.sendMessage(ChatColor.GRAY + "It will check all items in your inventory and report which ones");
        player.sendMessage(ChatColor.GRAY + "are detected as jewels and their tiers.");
    }
}