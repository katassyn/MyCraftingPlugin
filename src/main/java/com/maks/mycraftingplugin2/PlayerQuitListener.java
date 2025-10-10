package com.maks.mycraftingplugin2;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Clean up temporary permissions from HefajsosListener
        HefajsosListener.cleanupPlayer(event.getPlayer().getUniqueId());
    }
}