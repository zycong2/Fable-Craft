package io.RPGCraft.FableCraft.listeners.SecondaryListener;

import io.RPGCraft.FableCraft.RPGCraft;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class SweepAttackListener implements Listener {

  private final Random random = new Random();

  @EventHandler
  public void onPlayerLeftClick(PlayerInteractEvent event) {
    if ((event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) && !event.getPlayer().getItemInHand().equals(new ItemStack(Material.AIR))) {
      Slash(event.getPlayer());
    }
  }

  @EventHandler
  public void onEntityHit(EntityDamageByEntityEvent event) {
    if (event.getDamager() instanceof Player player) {
      Slash(player);
    }
  }

  private void Slash( final Player p ) {
    for(int i = 0; i < 19; i++) {
      new BukkitRunnable() {
        double alpha = 0;

        public void run() {
          alpha += Math.PI / 16;

          Location loc = p.getLocation();
          Location firstLocation = loc.clone().add(Math.cos(alpha), Math.sin(alpha) + 1, Math.sin(alpha));
          p.spawnParticle(Particle.CRIT, firstLocation, 0, 0, 0, 0, 0);
        }
      }.runTaskTimer(RPGCraft.getPlugin(), 0, 1);
    }
  }
}
