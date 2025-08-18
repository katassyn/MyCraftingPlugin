package com.maks.mycraftingplugin2;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
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
        if (mobId == null) {
            Main.getInstance().getLogger().info("Entity killed without MythicMob metadata: " + entity.getType());
            return;
        }

        Main.getInstance().getLogger().info("Mythic mob killed: " + mobId);

        List<String> allowed = Main.getInstance().getConfig()
                .getStringList("conjurer.mobs").stream()
                .map(s -> s.replace(",", "").trim().toLowerCase(Locale.ROOT))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        boolean isAllowed = allowed.contains(mobId.toLowerCase(Locale.ROOT));
        Main.getInstance().getLogger().info("Mob in conjurer list: " + isAllowed);
        if (!isAllowed) return;

        double dropChance = Main.getInstance().getConfig().getDouble("conjurer.recipe_drop_chance", 0.001);
        double roll = random.nextDouble();
        Main.getInstance().getLogger().info("Rolled " + roll + " against drop chance " + dropChance);
        if (roll >= dropChance) {
            Main.getInstance().getLogger().info("Roll failed; no recipe drop.");
            return;
        }

        Main.getInstance().getLogger().info("Roll succeeded; attempting recipe drop.");

        UUID uuid = killer.getUniqueId();
        List<String> locked = ConjurerRecipeUnlockManager.getLockedRecipes(uuid);
        if (locked.isEmpty()) return;

        String recipe = locked.get(random.nextInt(locked.size()));
        Main.getInstance().getLogger().info("Unlocking recipe " + recipe + " for player " + killer.getName());
        ConjurerRecipeUnlockManager.unlockRecipe(killer, recipe);
        killer.sendMessage(ChatColor.AQUA + "You feel knowledge flow through you...");
    }
}
