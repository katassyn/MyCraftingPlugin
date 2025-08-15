package com.maks.mycraftingplugin2;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /runes_crushing - Opens the rune crushing menu directly.
 */
public class RunesCrushingCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        RunesCrushingMenu.open(player);
        return true;
    }

    /**
     * Opens the rune crushing menu directly, bypassing permission checks.
     * Should only be used internally.
     */
    public static void openMenuWithoutPermissionCheck(Player player) {
        RunesCrushingMenu.open(player);
    }
}
