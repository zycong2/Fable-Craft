package io.RPGCraft.FableCraft.Utils.GUI;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;

public class GUIListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    void onInventoryClick(InventoryClickEvent e){
        if(e.getInventory() instanceof GUI eventGUI) {
            e.setCancelled(!eventGUI.getCanBeModified());
            Player player = (Player) e.getWhoClicked();
            int slot = e.getSlot();
            if (eventGUI.getItemMap().containsKey(slot)) {
                GUIItem guiItem = eventGUI.getItemMap().get(slot);
                Consumer<GUIItem.ClickContext> clickEvent = guiItem.clickEvent();
                clickEvent.accept(new GUIItem.ClickContext(
                        player,
                        e.getClick(),
                        eventGUI,
                        guiItem
                ));
            }
        }
    }
}
