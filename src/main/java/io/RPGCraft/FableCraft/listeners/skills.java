package io.RPGCraft.FableCraft.listeners;

import io.RPGCraft.FableCraft.core.PDCHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;


public class skills implements Listener {
    @EventHandler
    void combat(EntityDamageByEntityEvent event){
        if (event.getDamager() instanceof Player p){
            PDCHelper.getPlayerPDC("combat", p);
        }
    }
}
