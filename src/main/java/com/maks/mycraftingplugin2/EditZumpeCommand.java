package com.maks.mycraftingplugin2;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /edit_zumpe - Opens the Zumpe shop menu in edit mode.
 */
public class EditZumpeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        
        if (!player.hasPermission("mycraftingplugin.editlayout")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to edit the Zumpe shop layout.");
            return true;
        }

        // Open the Zumpe menu in edit mode
        ZumpeMainMenu.openEditor(player);
        return true;
    }
}