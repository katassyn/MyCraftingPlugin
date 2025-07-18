package com.maks.mycraftingplugin2;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class CraftingCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label,
                             String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        MainMenu.open(player);
        return true;
    }
}
