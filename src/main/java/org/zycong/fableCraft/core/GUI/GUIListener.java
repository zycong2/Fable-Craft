package org.zycong.fableCraft.core.GUI;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class GUIListener implements Listener {
    
    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof GUI gui)) {
            return;
        }
        
        event.setCancelled(true);
        
        final int slot = event.getRawSlot();
        gui.getItem(slot).ifPresent(item -> {
            if (item.getClickHandler() != null) {
                item.getClickHandler().accept(new GUIItem.ClickContext(
                    (Player) event.getWhoClicked(),
                    event.getClick(),
                    gui
                ));
            }
        });
    }
    
    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof GUI gui) {
            gui.stopAnimation();
        }
    }
}