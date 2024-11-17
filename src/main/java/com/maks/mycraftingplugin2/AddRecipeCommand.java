package com.maks.mycraftingplugin2;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class AddRecipeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Ta komenda może być użyta tylko przez gracza.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Poprawne użycie: /addrecipe <category>");
            return true;
        }

        String category = args[0];
        Player player = (Player) sender;

        // Otwórz interfejs dodawania receptury
        AddRecipeMenu.open(player, category);

        return true;
    }
}
