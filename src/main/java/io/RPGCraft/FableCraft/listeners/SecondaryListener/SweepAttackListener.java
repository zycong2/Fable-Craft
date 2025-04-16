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
    if ((event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)
        && event.getPlayer().getInventory().getItemInMainHand().getType().toString().contains("_SWORD")) {
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
    Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(139, 232, 240), 1);
    double alpha = 0;
    for(int i = 0; i < 19; i++) {
      alpha += Math.PI / 16;

      Location loc = p.getLocation();
      Location firstLocation = loc.clone().add(Math.cos(alpha), Math.sin(alpha) + 1, Math.sin(alpha));
      p.getLocation().getWorld().spawnParticle(Particle.ASH, firstLocation.getX(), firstLocation.getY(), firstLocation.getZ(), 1, 0.001, 1, 0, 1, dustOptions);
    }
  }
}
