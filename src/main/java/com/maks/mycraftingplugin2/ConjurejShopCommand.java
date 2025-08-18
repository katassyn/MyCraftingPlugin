package com.maks.mycraftingplugin2;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /conjurej_shop - Opens the Conjurej main menu.
 */
public class ConjurejShopCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        if (player.getLevel() < 80) {
            player.sendMessage(ChatColor.RED + "You must be at least level 80 to use this command.");
            return true;
        }

        ConjurejMainMenu.open(player);
        return true;
    }
}
