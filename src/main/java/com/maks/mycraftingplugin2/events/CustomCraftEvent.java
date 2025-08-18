package com.maks.mycraftingplugin2.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Event called when a player crafts an item using MyCraftingPlugin2
 */
public class CustomCraftEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final ItemStack result;
    private final int amount;
    private final String recipeName;
    private boolean cancelled = false;

    /**
     * Constructor for CustomCraftEvent
     * 
     * @param player The player who crafted the item
     * @param result The item that was crafted
     * @param amount The amount of items crafted
     * @param recipeName The name of the recipe used
     */
    public CustomCraftEvent(Player player, ItemStack result, int amount, String recipeName) {
        super(player);
        this.result = result;
        this.amount = amount;
        this.recipeName = recipeName;
    }

    /**
     * Get the crafted item
     * 
     * @return The crafted item
     */
    public ItemStack getResult() {
        return result;
    }

    /**
     * Get the amount of items crafted
     * 
     * @return The amount
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Get the recipe name
     * 
     * @return The recipe name
     */
    public String getRecipeName() {
        return recipeName;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}