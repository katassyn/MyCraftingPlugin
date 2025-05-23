package com.maks.mycraftingplugin2;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /jewels_crushing - Opens the jewels crushing menu directly.
 */
public class JewelsCrushingCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        // Open the jewels crushing menu
        JewelsCrushingMenu.open(player);
        return true;
    }

    /**
     * Opens the jewels crushing menu directly, bypassing permission checks.
     * This should only be called from within the plugin, not from external commands.
     * 
     * @param player The player to open the menu for
     */
    public static void openMenuWithoutPermissionCheck(Player player) {
        JewelsCrushingMenu.open(player);
    }
}
