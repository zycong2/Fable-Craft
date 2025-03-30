package org.zycong.fableCraft.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.zycong.fableCraft.core.PDCHelper;

public class skills implements Listener {
    @EventHandler
    void combat(EntityDamageByEntityEvent event){
        if (event.getDamager() instanceof Player p){
            PDCHelper.getPlayerPDC("combat", p);
        }
    }
}
