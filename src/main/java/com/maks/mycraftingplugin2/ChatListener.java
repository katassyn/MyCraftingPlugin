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

        if (playerStates.containsKey(uuid)) {
            event.setCancelled(true);
            String state = playerStates.get(uuid);
            String message = event.getMessage();

            Bukkit.getLogger().info("[DEBUG] onPlayerChat: state=" + state
                    + ", rawMessage='" + message + "'");

            // Przekazujemy do wątku głównego
            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                if (state.equals("entering_success_chance")) {
                    // Gracz ustawia % szansy
                    try {
                        double successChance = parsePercentage(message);
                        Bukkit.getLogger().info("[DEBUG] onPlayerChat (successChance): after parse -> " + successChance);

                        // Ustawiamy WYŁĄCZNIE successChance
                        TemporaryData.setSuccessChance(uuid, successChance);

                        player.sendMessage(ChatColor.GREEN + "Success chance set to " + successChance + "%.");
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED
                                + "Invalid input. Please enter a valid percentage (like 50% or 50).");
                    }
                    removePlayerState(uuid);
                    reopenRecipeMenu(player);

                } else if (state.equals("entering_cost")) {
                    // Gracz ustawia koszt
                    try {
                        double cost = parseCost(message);
                        Bukkit.getLogger().info("[DEBUG] onPlayerChat (cost): after parse -> " + cost);

                        // Ustawiamy WYŁĄCZNIE cost
                        TemporaryData.setCost(uuid, cost);

                        player.sendMessage(ChatColor.GREEN + "Cost set to " + cost + ".");
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED
                                + "Invalid input. Please enter a valid cost (like 500, 1k, 2m).");
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

        // Sprawdzamy, czy mamy zapisaną kategorię (AddRecipeMenu)
        String category = AddRecipeMenu.getCategory(uuid);
        if (category != null) {
            // Nowa receptura
            AddRecipeMenu.open(player, category);
        } else {
            // Edycja już istniejącej
            int recipeId = EditRecipeMenu.getRecipeId(uuid);
            if (recipeId != -1) {
                EditRecipeMenu.open(player, recipeId);
            }
        }
    }

    /**
     * Parsujemy koszt (np. 500, 1k, 2m).
     */
    private double parseCost(String input) throws NumberFormatException {
        Bukkit.getLogger().info("[DEBUG] parseCost: input BEFORE replace='" + input + "'");
        input = input.replace(",", "")
                .replace("k", "000")
                .replace("m", "000000")
                .trim();
        Bukkit.getLogger().info("[DEBUG] parseCost: input AFTER replace='" + input + "'");
        return Double.parseDouble(input);
    }

    /**
     * Parsujemy procent (np. 50%, 0.5%).
     */
    private double parsePercentage(String input) throws NumberFormatException {
        Bukkit.getLogger().info("[DEBUG] parsePercentage: raw='" + input + "'");
        input = input.replace("%", "").trim();
        Bukkit.getLogger().info("[DEBUG] parsePercentage: after replace='" + input + "'");
        return Double.parseDouble(input);
    }
}
