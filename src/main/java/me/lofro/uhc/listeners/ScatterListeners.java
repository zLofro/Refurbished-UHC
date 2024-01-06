package me.lofro.uhc.listeners;

import me.lofro.uhc.UHC;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class ScatterListeners implements Listener {

     @EventHandler
    private void onMove(PlayerMoveEvent event) {
         if (event.hasChangedBlock()) {
             var gameManager = UHC.getInstance().getGameManager();

             if (gameManager.getGameData().isInScatter()) event.setCancelled(true);
         }
     }

}
