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

import static io.RPGCraft.FableCraft.listeners.SecondaryListener.SweepEffect.SlashEffect.slashVertical;

public class SweepAttackListener implements Listener {

  private final Random random = new Random();

  @EventHandler
  public void onPlayerLeftClick(PlayerInteractEvent event) {
    if ((event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)
        && (event.getPlayer().getInventory().getItemInMainHand().getType().toString().contains("_SWORD") || event.getPlayer().getInventory().getItemInMainHand().getType().toString().contains("_AXE"))) {
      slashVertical(event.getPlayer(), Particle.DUST);
    }
  }

  @EventHandler
  public void onEntityHit(EntityDamageByEntityEvent event) {
    if (event.getDamager() instanceof Player player) {
      slashVertical(player, Particle.DUST);
    }
  }
}
