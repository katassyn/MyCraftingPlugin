package com.maks.mycraftingplugin2.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * Event that is triggered when a player successfully crafts an item using the MyCraftingPlugin2 system.
 * This event can be used by other plugins (like QuestSystem) to track crafting progress.
 */
public class CustomCraftingEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final ItemStack craftedItem;

    /**
     * Creates a new CustomCraftingEvent
     * 
     * @param player The player who crafted the item
     * @param craftedItem The item that was crafted
     */
    public CustomCraftingEvent(Player player, ItemStack craftedItem) {
        this.player = player;
        this.craftedItem = craftedItem;
    }

    /**
     * Gets the player who crafted the item
     * 
     * @return The player who crafted the item
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the item that was crafted
     * 
     * @return The crafted item
     */
    public ItemStack getCraftedItem() {
        return craftedItem;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}