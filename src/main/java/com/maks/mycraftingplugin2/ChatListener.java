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

    /**
     * Ustawiamy stan danego gracza (np. "entering_success_chance", "entering_cost").
     */
    public static void setPlayerState(UUID uuid, String state) {
        playerStates.put(uuid, state);
    }

    /**
     * Usuwamy stan gracza po zakończeniu wprowadzania danych.
     */
    public static void removePlayerState(UUID uuid) {
        playerStates.remove(uuid);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Sprawdzamy, czy gracz jest w jednym z naszych "stanów" (wprowadzania % lub kosztu).
        if (playerStates.containsKey(uuid)) {
            event.setCancelled(true);  // Anulujemy "normalne" chatowanie
            String state = playerStates.get(uuid);
            String message = event.getMessage();

            // Za pomocą schedulera przekazujemy logikę do wątku głównego
            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                if (state.equals("entering_success_chance")) {
                    try {
                        double successChance = parsePercentage(message);
                        TemporaryData.setSuccessChance(uuid, successChance);
                        player.sendMessage(ChatColor.GREEN + "Success chance set to " + successChance + "%.");
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "Invalid input. Please enter a valid percentage (like 50% or 50).");
                    }
                    removePlayerState(uuid);
                    reopenRecipeMenu(player);

                } else if (state.equals("entering_cost")) {
                    try {
                        double cost = parseCost(message);
                        TemporaryData.setCost(uuid, cost);
                        player.sendMessage(ChatColor.GREEN + "Cost set to " + cost + ".");
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "Invalid input. Please enter a valid cost (like 500, 1k, 2m).");
                    }
                    removePlayerState(uuid);
                    reopenRecipeMenu(player);
                }
            });
        }
    }

    /**
     * Po wpisaniu liczby w czacie wracamy do okna receptury (nowej lub edytowanej).
     */
    private void reopenRecipeMenu(Player player) {
        UUID uuid = player.getUniqueId();
        // Sprawdzamy, czy mamy zapisaną kategorię AddRecipeMenu (nowa receptura)
        String category = AddRecipeMenu.getCategory(uuid);
        if (category != null) {
            // Jeśli tak, otwieramy ponownie AddRecipeMenu
            AddRecipeMenu.open(player, category);
        } else {
            // W innym wypadku sprawdzamy, czy gracz edytuje już istniejącą recepturę
            int recipeId = EditRecipeMenu.getRecipeId(uuid);
            if (recipeId != -1) {
                // Jeśli mamy ID recepty, otwieramy EditRecipeMenu
                EditRecipeMenu.open(player, recipeId);
            }
        }
    }

    /**
     * Parsujemy wpisany przez gracza procent (np. "55%", "0.5%").
     */
    private double parsePercentage(String input) throws NumberFormatException {
        input = input.replace("%", "").trim(); // Usuwamy znak %
        return Double.parseDouble(input);       // Zamieniamy na double
    }

    /**
     * Parsujemy wpisany przez gracza koszt (np. "500", "1k", "2m").
     */
    private double parseCost(String input) throws NumberFormatException {
        // Zamień np. 1k -> 1000, 1m -> 1000000
        input = input.replace(",", "")
                .replace("k", "000")
                .replace("m", "000000")
                .trim();
        return Double.parseDouble(input);
    }
}
