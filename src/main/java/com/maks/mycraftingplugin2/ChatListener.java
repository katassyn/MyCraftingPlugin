package com.maks.mycraftingplugin2;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatListener implements Listener {

    // Mapowanie stanu gracza
    private static Map<UUID, String> playerStates = new HashMap<>();

    public static void setPlayerState(UUID uuid, String state) {
        playerStates.put(uuid, state);
    }

    public static void removePlayerState(UUID uuid) {
        playerStates.remove(uuid);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (playerStates.containsKey(uuid)) {
            event.setCancelled(true);
            String state = playerStates.get(uuid);
            String message = event.getMessage();

            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                if (state.equals("entering_success_chance")) {
                    try {
                        double successChance = parsePercentage(message);
                        TemporaryData.setSuccessChance(uuid, successChance);
                        player.sendMessage(ChatColor.GREEN + "Success chance set to " + successChance + "%.");
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "Invalid input. Please enter a valid percentage.");
                    }
                    removePlayerState(uuid);
                    reopenRecipeMenu(player);
                } else if (state.equals("entering_cost")) {
                    try {
                        double cost = parseCost(message);
                        TemporaryData.setCost(uuid, cost);
                        player.sendMessage(ChatColor.GREEN + "Cost set to " + cost + ".");
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "Invalid input. Please enter a valid cost.");
                    }
                    removePlayerState(uuid);
                    reopenRecipeMenu(player);
                }
            });
        }
    }

    private void reopenRecipeMenu(Player player) {
        UUID uuid = player.getUniqueId();
        String category = AddRecipeMenu.getCategory(uuid);
        if (category != null) {
            AddRecipeMenu.open(player, category);
        } else {
            int recipeId = EditRecipeMenu.getRecipeId(uuid);
            if (recipeId != -1) {
                // Implementuj metodę EditRecipeMenu.open(player, recipeId);
                // Otwórz interfejs edycji receptury
            }
        }
    }

    private double parsePercentage(String input) throws NumberFormatException {
        input = input.replace("%", "").trim();
        return Double.parseDouble(input);
    }

    private double parseCost(String input) throws NumberFormatException {
        // Możesz zaimplementować bardziej zaawansowany parser kosztów (np. 1k = 1000)
        input = input.replace(",", "").replace("k", "000").replace("m", "000000").trim();
        return Double.parseDouble(input);
    }
}
