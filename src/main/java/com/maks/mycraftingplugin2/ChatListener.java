package com.maks.mycraftingplugin2;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

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

    /**
     * Zwraca aktualny stan gracza lub null jeśli żaden nie jest ustawiony.
     */
    public static String getPlayerState(UUID uuid) {
        return playerStates.get(uuid);
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
                    // Gracz ustawia koszt - sprawdzamy kontekst
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

                    // Sprawdzamy kontekst i otwieramy odpowiednie menu
                    reopenAppropriateMenu(player);
                } else if (state.equals("entering_emilia_daily_limit")) {
                    try {
                        int dailyLimit = Integer.parseInt(message.trim());
                        if (dailyLimit < 0) {
                            player.sendMessage(ChatColor.RED + "Daily limit cannot be negative. Please enter a valid number.");
                        } else {
                            TemporaryData.setPlayerData(uuid, "emilia_daily_limit", dailyLimit);
                            player.sendMessage(ChatColor.GREEN + "Daily limit set to " + dailyLimit + ".");
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "Invalid input. Please enter a valid number.");
                    }
                    removePlayerState(uuid);
                    reopenEmiliaMenu(player);
                } else if (state.equals("entering_zumpe_daily_limit")) {
                    try {
                        int dailyLimit = Integer.parseInt(message.trim());
                        if (dailyLimit < 0) {
                            player.sendMessage(ChatColor.RED + "Daily limit cannot be negative. Please enter a valid number.");
                        } else {
                            TemporaryData.setPlayerData(uuid, "zumpe_daily_limit", dailyLimit);
                            player.sendMessage(ChatColor.GREEN + "Daily limit set to " + dailyLimit + ".");
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "Invalid input. Please enter a valid number.");
                    }
                    removePlayerState(uuid);
                    reopenZumpeMenu(player);
                } else if (state.equals("entering_gnob_daily_limit")) {
                    try {
                        int dailyLimit = Integer.parseInt(message.trim());
                        if (dailyLimit < 0) {
                            player.sendMessage(ChatColor.RED + "Daily limit cannot be negative. Please enter a valid number.");
                        } else {
                            TemporaryData.setPlayerData(uuid, "gnob_daily_limit", dailyLimit);
                            player.sendMessage(ChatColor.GREEN + "Daily limit set to " + dailyLimit + ".");
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "Invalid input. Please enter a valid number.");
                    }
                    removePlayerState(uuid);
                    reopenGnobMenu(player);
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

    /**
     * Re-opens the Emilia item menu after input from chat.
     */
    private void reopenEmiliaMenu(Player player) {
        UUID uuid = player.getUniqueId();

        // Check if editing an existing item
        int itemId = EmiliaAddItemMenu.getEditItemId(uuid);
        if (itemId != -1) {
            EmiliaAddItemMenu.openEdit(player, itemId);
            return;
        }

        // Check if adding a new item
        String category = EmiliaAddItemMenu.getCategory(uuid);
        if (category != null) {
            String[] parts = category.split("_");
            if (parts.length >= 3) {
                String shopType = parts[1].equals("shop") ? "Shop" : "Event Shop";
                String tierType = capitalizeFirstLetter(parts[parts.length - 1]);

                EmiliaAddItemMenu.open(player, shopType, tierType);
            }
        }
    }

    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    /**
     * Sprawdza kontekst i otwiera odpowiednie menu po ustawieniu kosztu.
     */
    private void reopenAppropriateMenu(Player player) {
        UUID uuid = player.getUniqueId();

        if (Main.getInstance().getConfig().getInt("debug", 0) == 1) {
            Bukkit.getLogger().info("[ChatListener] Reopening appropriate menu for player: " + player.getName());
        }

        // Sprawdzamy, czy to Emilia
        String emiliaCategory = EmiliaAddItemMenu.getCategory(uuid);
        int emiliaItemId = EmiliaAddItemMenu.getEditItemId(uuid);

        if (emiliaCategory != null || emiliaItemId != -1) {
            if (Main.getInstance().getConfig().getInt("debug", 0) == 1) {
                Bukkit.getLogger().info("[ChatListener] Detected Emilia context");
            }
            reopenEmiliaMenu(player);
            return;
        }

        // Sprawdzamy, czy to Zumpe - POPRAWKA: sprawdź też czy jest zapisany stan GUI
        int zumpeItemId = ZumpeAddItemMenu.getEditItemId(uuid);
        ItemStack[] zumpeGuiState = ZumpeAddItemMenu.getGuiState(uuid); // Dodaj getter w ZumpeAddItemMenu

        if (zumpeItemId != -1 || zumpeGuiState != null) {
            if (Main.getInstance().getConfig().getInt("debug", 0) == 1) {
                Bukkit.getLogger().info("[ChatListener] Detected Zumpe context - itemId: " + zumpeItemId);
            }
            reopenZumpeMenu(player);
            return;
        }

        // W przeciwnym razie to normalne receptury
        if (Main.getInstance().getConfig().getInt("debug", 0) == 1) {
            Bukkit.getLogger().info("[ChatListener] Detected normal recipe context");
        }
        reopenRecipeMenu(player);
    }

    /**
     * Re-opens the Zumpe item menu after input from chat.
     */
    private void reopenZumpeMenu(Player player) {
        UUID uuid = player.getUniqueId();

        // Check if editing an existing item
        int itemId = ZumpeAddItemMenu.getEditItemId(uuid);
        if (itemId != -1) {
            ZumpeAddItemMenu.openEdit(player, itemId);
            return;
        }

        // If adding a new item
        ZumpeAddItemMenu.open(player);
    }

    private void reopenGnobMenu(Player player) {
        UUID uuid = player.getUniqueId();

        // Check if editing an existing item
        int itemId = GnobAddItemMenu.getEditItemId(uuid);
        if (itemId != -1) {
            GnobAddItemMenu.openEdit(player, itemId);
            return;
        }

        // Check if adding a new item
        String category = GnobAddItemMenu.getCategory(uuid);
        if (category != null) {
            GnobAddItemMenu.open(player);
        }
    }
}
