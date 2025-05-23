package com.maks.mycraftingplugin2;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /edit_emilia - Opens the Emilia shop menu in edit mode.
 */
public class EditEmiliaCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        
        if (!player.hasPermission("mycraftingplugin.editlayout")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to edit the Emilia shop layout.");
            return true;
        }

        // Open the Emilia menu in edit mode
        EmiliaMainMenu.openEditor(player);
        return true;
    }
}