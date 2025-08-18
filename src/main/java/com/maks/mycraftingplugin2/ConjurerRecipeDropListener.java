package com.maks.mycraftingplugin2;

import org.bukkit.ChatColor;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles dropping of Conjurer recipes from MythicMobs.
 * Relies on the MythicMobDeathEvent provided by the MythicMobs API
 * to obtain the mob's internal name for matching against configured drops.
 */
public class ConjurerRecipeDropListener implements Listener {

    private final Random random = new Random();

    @EventHandler
    public void onMythicMobDeath(MythicMobDeathEvent event) {
        Entity killerEntity = event.getKiller();
        if (!(killerEntity instanceof Player)) {
            Main.getInstance().getLogger().info("Mythic mob died without a player killer");
            return;
        }
        Player killer = (Player) killerEntity;

        String mobId = event.getMobType().getInternalName();
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
