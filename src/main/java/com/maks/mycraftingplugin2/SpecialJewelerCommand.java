package com.maks.mycraftingplugin2;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /special_jeweler - Opens the special jeweler GUI for browsing.
 */
public class SpecialJewelerCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        // Open the special jeweler GUI
        SpecialJewelerGUI gui = new SpecialJewelerGUI(player, false);
        player.openInventory(gui.getInventory());
        return true;
    }
}
