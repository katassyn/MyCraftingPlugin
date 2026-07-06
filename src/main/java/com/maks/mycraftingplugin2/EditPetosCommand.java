package com.maks.mycraftingplugin2;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /edit_petos — opens the PETOS category editor (like other mechanics),
 * even when there are no recipes yet.
 */
public class EditPetosCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("mycraftingplugin.editlayout")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to edit crafting layouts.");
            return true;
        }

        // Open standard editor menu for category 'petos' (works even with 0 recipes)
        CategoryMenu.openEditor(player, "petos", 0);
        return true;
    }
}
