package io.RPGCraft.FableCraft.Utils.GUI;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;

import static org.bukkit.Bukkit.getServer;

public class GUIListener implements Listener {

    @EventHandler
    void onInventoryClick(InventoryClickEvent e){
        if(e.getInventory().getHolder() instanceof GUI eventGUI) {
            e.setCancelled(!eventGUI.getCanBeModified());
            Player player = (Player) e.getWhoClicked();
            int slot = e.getSlot();
            if (eventGUI.getItemMap().containsKey(slot)) {
                GUIItem guiItem = eventGUI.getItemMap().get(slot);
                Consumer<GUIItem.ClickContext> clickEvent = guiItem.clickEvent();
                if(clickEvent == null) return;
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
