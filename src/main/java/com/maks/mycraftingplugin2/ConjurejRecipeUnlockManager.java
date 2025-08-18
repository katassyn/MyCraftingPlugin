package com.maks.mycraftingplugin2;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;

/**
 * Manages unlocking and tracking of Conjurej recipes for players.
 */
public class ConjurejRecipeUnlockManager {

    private static final List<String> ALL_RECIPES = Arrays.asList(
            "Runic Tether",
            "Surgical Sever",
            "Blessing Theft",
            "Hunter's Mark",
            "Rhythmic Displacement",
            "Crosshair Rattle",
            "Whiplash Sprint",
            "Mischief",
            "Overextension"
    );

    public static List<String> getAllRecipes() {
        return ALL_RECIPES;
    }

    public static boolean hasRecipe(UUID uuid, String recipe) {
        if (recipe == null || recipe.isEmpty()) return true;
        try (Connection conn = Main.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT 1 FROM conjurej_recipes WHERE player_uuid=? AND recipe=?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, recipe);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void unlockRecipe(Player player, String recipe) {
        try (Connection conn = Main.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT IGNORE INTO conjurej_recipes (player_uuid, recipe) VALUES (?, ?)")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, recipe);
            ps.executeUpdate();
            player.sendMessage(ChatColor.GOLD + "You have unlocked the " + recipe + " recipe!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getLockedRecipes(UUID uuid) {
        List<String> locked = new ArrayList<>(ALL_RECIPES);
        try (Connection conn = Main.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT recipe FROM conjurej_recipes WHERE player_uuid=?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    locked.remove(rs.getString("recipe"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return locked;
    }
}
