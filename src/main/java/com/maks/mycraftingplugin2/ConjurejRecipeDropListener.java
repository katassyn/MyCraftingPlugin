package com.maks.mycraftingplugin2;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Handles dropping of Conjurej recipes from MythicMobs.
 * Uses metadata "MythicType" to identify MythicMobs without
 * requiring a direct dependency on the MythicMobs API.
 */
public class ConjurejRecipeDropListener implements Listener {

    private static final double DROP_CHANCE = 0.001; // 0.1%
    private final Random random = new Random();

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        Entity entity = event.getEntity();
        if (!entity.hasMetadata("MythicType")) return;
        String mobId = entity.getMetadata("MythicType").get(0).asString();

        List<String> allowed = Main.getInstance().getConfig().getStringList("conjurej.mob_ids");
        if (!allowed.contains(mobId)) return;

        if (random.nextDouble() >= DROP_CHANCE) return;

        UUID uuid = killer.getUniqueId();
        List<String> locked = ConjurejRecipeUnlockManager.getLockedRecipes(uuid);
        if (locked.isEmpty()) return;

        String recipe = locked.get(random.nextInt(locked.size()));
        ConjurejRecipeUnlockManager.unlockRecipe(killer, recipe);
        killer.sendMessage(ChatColor.AQUA + "You feel knowledge flow through you...");
    }
}
