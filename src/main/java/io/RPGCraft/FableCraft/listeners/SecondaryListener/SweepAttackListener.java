package io.RPGCraft.FableCraft.listeners.SecondaryListener;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Random;

public class SweepAttackListener implements Listener {

  private final Random random = new Random();

  @EventHandler
  public void onPlayerLeftClick(PlayerInteractEvent event) {
    if ((event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) && !event.getPlayer().getItemInHand().equals(new ItemStack(Material.AIR))) {
      playRandomSlash(event.getPlayer());
    }
  }

  @EventHandler
  public void onEntityHit(EntityDamageByEntityEvent event) {
    if (event.getDamager() instanceof Player player) {
      playRandomSlash(player);
    }
  }

  public void playRandomSlash(Player player) {
    Location origin = player.getEyeLocation().clone().add(0, -0.3, 0);
    Vector direction = origin.getDirection().normalize();

    // Slash pattern options
    Vector[] patterns = new Vector[] {
      new Vector(-1, 0, 0),    // left ➝ right
      new Vector(1, 0, 0),     // right ➝ left
      new Vector(0, -1, 0),    // top ➝ bottom
      new Vector(-1, -1, 0),   // top-left ➝ bottom-right
      new Vector(1, -1, 0),    // top-right ➝ bottom-left
    };

    Vector slashDir = patterns[random.nextInt(patterns.length)].normalize();
    Vector rotated = rotateVector(slashDir, direction);

    // Slash particles
    int steps = 10;
    for (int i = 0; i < steps; i++) {
      Vector step = rotated.clone().multiply(i * 0.3);
      Location loc = origin.clone().add(step);
      player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, loc, 0);
    }

    // Optional sound
    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1f);
  }

  public Vector rotateVector(Vector v, Vector direction) {
    Location tempLoc = new Location(null, 0, 0, 0);
    tempLoc.setDirection(direction);
    float yaw = tempLoc.getYaw();
    float pitch = tempLoc.getPitch();

    double yawRad = Math.toRadians(-yaw);
    double pitchRad = Math.toRadians(-pitch);

    double x1 = v.getX() * Math.cos(yawRad) - v.getZ() * Math.sin(yawRad);
    double z1 = v.getX() * Math.sin(yawRad) + v.getZ() * Math.cos(yawRad);
    double y1 = v.getY();

    double y2 = y1 * Math.cos(pitchRad) - z1 * Math.sin(pitchRad);
    double z2 = y1 * Math.sin(pitchRad) + z1 * Math.cos(pitchRad);

    return new Vector(x1, y2, z2);
  }
}
