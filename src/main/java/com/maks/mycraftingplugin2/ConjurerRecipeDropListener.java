package com.maks.mycraftingplugin2;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles dropping of Conjurer recipes from MythicMobs.
 * Checks common MythicMobs metadata keys to identify the mob
 * without requiring a direct dependency on the MythicMobs API.
 */
public class ConjurerRecipeDropListener implements Listener {

    private final Random random = new Random();

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        Entity entity = event.getEntity();

        String mobId = null;
        if (entity.hasMetadata("MythicType")) {
            mobId = entity.getMetadata("MythicType").get(0).asString();
        } else if (entity.hasMetadata("MythicMobType")) {
            mobId = entity.getMetadata("MythicMobType").get(0).asString();
        } else if (entity.hasMetadata("MythicMob")) {
            mobId = entity.getMetadata("MythicMob").get(0).asString();
        }
        if (mobId == null) return;

        String mobsRaw = Main.getInstance().getConfig().getString("conjurer.mobs", "");
        List<String> allowed = Arrays.stream(mobsRaw.split("\\R"))
                .map(line -> line.trim().toLowerCase(Locale.ROOT))
                .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                .collect(Collectors.toList());
        if (!allowed.contains(mobId.toLowerCase(Locale.ROOT))) return;

        double dropChance = Main.getInstance().getConfig().getDouble("conjurer.recipe_drop_chance", 0.001);
        if (random.nextDouble() >= dropChance) return;

        UUID uuid = killer.getUniqueId();
        List<String> locked = ConjurerRecipeUnlockManager.getLockedRecipes(uuid);
        if (locked.isEmpty()) return;

        String recipe = locked.get(random.nextInt(locked.size()));
        ConjurerRecipeUnlockManager.unlockRecipe(killer, recipe);
        killer.sendMessage(ChatColor.AQUA + "You feel knowledge flow through you...");
    }
}
