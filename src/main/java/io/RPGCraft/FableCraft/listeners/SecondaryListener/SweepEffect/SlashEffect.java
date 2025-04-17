package io.RPGCraft.FableCraft.listeners.SecondaryListener.SweepEffect;

import io.RPGCraft.FableCraft.RPGCraft;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class SlashEffect {

  /*
   * This code was copied from hidden1nin on GitHub.
   * Modified to fit the needs of RPGCraft.
   * Repository link: https://github.com/hidden1nin/slashes
   * No formal license specified.
   */

  public static void slashVertical(Player player, Particle particle){

    Location playerlocation = player.getLocation().clone();
    //clamp the players
    if(playerlocation.getPitch()>60||playerlocation.getPitch()< -60) playerlocation.setPitch(0);

    playerlocation.add(0,1,0);

    int direction = 1;

    int count = 0;

    int timer = 0;
      for (double i = -1.5; i < 1.5; i += .05) {
        if (count > 10) {

          timer++;
          double finalI = i;
          int finalDirection = direction;
          new BukkitRunnable() {
            @Override
            public void run() {
              Location location = playerlocation.clone().add(playerlocation.getDirection().clone().multiply(2).rotateAroundY(finalI / 2 * finalDirection)).clone().add(0, finalI, 0);
              player.spawnParticle(particle, location, 0, 0, 0, 0, new Particle.DustOptions(Color.AQUA, 1));
              player.getLocation().getWorld().spawnParticle(particle, location, 0, 0, 0, 0, new Particle.DustOptions(Color.AQUA, 1));
            }
          }.runTaskLater(RPGCraft.getPlugin(), timer);
          count = 0;
        } else {
          double finalI = i;
          int finalDirection = direction;
          new BukkitRunnable() {
            @Override
            public void run() {
              Location location = playerlocation.clone().add(playerlocation.getDirection().clone().multiply(2).rotateAroundY(finalI / 2 * finalDirection)).clone().add(0, finalI, 0);
              player.spawnParticle(particle, location, 0, 0, 0, 0, new Particle.DustOptions(Color.AQUA, 1));
            }
          }.runTaskLater(RPGCraft.getPlugin(), timer);
        }
        count++;
      }
  }
}

