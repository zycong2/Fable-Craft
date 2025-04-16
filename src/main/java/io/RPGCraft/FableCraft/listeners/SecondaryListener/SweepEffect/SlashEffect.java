package io.RPGCraft.FableCraft.listeners.SecondaryListener.SweepEffect;

import io.RPGCraft.FableCraft.RPGCraft;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

public class SlashEffect {

  /*
  *
  * Added a temporary slash effect
  *
  * Thank you code from hidden1nin
  * For the particle system
  * https://github.com/hidden1nin/slashes
  *
  * I'll replace this with a code that I made myself later
  *
   */

  public static void slashHorizontal(Player player) {

    boolean topOrBottom = ThreadLocalRandom.current().nextBoolean();

    boolean leftOrRight = ThreadLocalRandom.current().nextBoolean();

    slashHorizontal(player,topOrBottom,leftOrRight, Particle.CRIT,.05f);

  }

  public static void slashVertical(Player player) {

    boolean topOrBottom = ThreadLocalRandom.current().nextBoolean();

    boolean leftOrRight = ThreadLocalRandom.current().nextBoolean();
    slashVertical(player,topOrBottom,leftOrRight,Particle.CRIT,.05f);
  }

  public static void slashHorizontal(Player player,Particle particle) {

    boolean topOrBottom = ThreadLocalRandom.current().nextBoolean();

    boolean leftOrRight = ThreadLocalRandom.current().nextBoolean();
    slashHorizontal(player,topOrBottom,leftOrRight,particle,.05f);
  }

  public static void slashVertical(Player player,Particle particle) {

    boolean topOrBottom = ThreadLocalRandom.current().nextBoolean();

    boolean leftOrRight = ThreadLocalRandom.current().nextBoolean();

    slashVertical(player,topOrBottom,leftOrRight,particle,.05f);
  }

  public static void slashHorizontal(Player player,Particle particle,float density) {
    boolean topOrBottom =ThreadLocalRandom.current().nextBoolean();

    boolean leftOrRight = ThreadLocalRandom.current().nextBoolean();
    slashVertical(player,topOrBottom,leftOrRight,particle,density);
  }

  public static void slashVertical(Player player,Particle particle,float density) {
    boolean topOrBottom =ThreadLocalRandom.current().nextBoolean();

    boolean leftOrRight = ThreadLocalRandom.current().nextBoolean();
    slashVertical(player,topOrBottom,leftOrRight,particle,density);
  }
  /**
   * draws a slash particle in front of the player
   * @param player player to draw the particles for
   * @param topOrBottom if true slash top to bottom
   * @param leftOrRight if true slashes from left to right
   * @param particle the particle to display
   * @param density space between particles
   */
  public static void slashVertical(Player player,boolean topOrBottom,boolean leftOrRight,Particle particle,float density){

    Location playercopy = player.getLocation().clone();
    //clamp the players
    if(playercopy.getPitch()>60||playercopy.getPitch()< -60) playercopy.setPitch(0);

    playercopy.add(0,1,0);

    int direction = 1;

    if(topOrBottom)direction=-1;

    int count = 0;

    int timer = 0;

    if(leftOrRight) {
      for (double i = -1.5; i < 1.5; i += .05) {
        if (count > 10) {

          timer++;
          double finalI = i;
          int finalDirection = direction;
          new BukkitRunnable() {
            @Override
            public void run() {
              Location location = playercopy.clone().add(playercopy.getDirection().clone().multiply(2).rotateAroundY(finalI / 2 * finalDirection)).clone().add(0, finalI, 0);
              player.spawnParticle(particle, location, 0, 0, 0, 0);
              player.getLocation().getWorld().spawnParticle(particle, location, 0, 0, 0, 0);
            }
          }.runTaskLater(RPGCraft.getPlugin(), timer);
          count = 0;
        } else {
          double finalI = i;
          int finalDirection = direction;
          new BukkitRunnable() {
            @Override
            public void run() {
              Location location = playercopy.clone().add(playercopy.getDirection().clone().multiply(2).rotateAroundY(finalI / 2 * finalDirection)).clone().add(0, finalI, 0);
              player.spawnParticle(particle, location, 0, 0, 0, 0);
            }
          }.runTaskLater(RPGCraft.getPlugin(), timer);
        }
        count++;
      }
    }else {
      for (double i = 1.5; i > -1.5; i -= .05) {
        if (count > 10) {
          timer++;
          double finalI = i;
          int finalDirection = direction;
          new BukkitRunnable() {
            @Override
            public void run() {
              Location location = playercopy.clone().add(playercopy.getDirection().clone().multiply(2).rotateAroundY(finalI / 2 * finalDirection)).clone().add(0, finalI, 0);
              player.spawnParticle(particle, location, 0, 0, 0, 0);
              player.getLocation().getWorld().spawnParticle(particle, location, 0, 0, 0, 0);
            }
          }.runTaskLater(RPGCraft.getPlugin(), timer);
          count = 0;
        } else {
          double finalI = i;
          int finalDirection = direction;
          new BukkitRunnable() {
            @Override
            public void run() {
              Location location = playercopy.clone().add(playercopy.getDirection().clone().multiply(2).rotateAroundY(finalI / 2 * finalDirection)).clone().add(0, finalI, 0);
              player.spawnParticle(particle, location, 0, 0, 0, 0);
            }
          }.runTaskLater(RPGCraft.getPlugin(), timer);
        }
        count++;
      }
    }

  }
  /**
   * draws a slash particle in front of the player
   * @param player player to draw the particles for
   * @param topOrBottom if true slash top to bottom
   * @param leftOrRight if true slashes from left to right
   * @param particle the particle to display
   * @param density space between particles
   */
  public static void slashHorizontal(Player player, boolean topOrBottom, boolean leftOrRight, Particle particle, float density) {

    Location playercopy = player.getLocation().clone();
    if(playercopy.getPitch()>60||playercopy.getPitch()< -60) playercopy.setPitch(0);
    playercopy.add(0,1,0);
    int direction = 1;
    if(topOrBottom)direction=-1;
    int count = 0;
    int timer = 0;
    if(leftOrRight) {
      for (double i = -1; i < 1; i += density) {
        if (count > 10) {
          timer++;
          double finalI = i;
          int finalDirection = direction;
          new BukkitRunnable() {
            @Override
            public void run() {
              Location location = playercopy.clone().add(playercopy.getDirection().clone().multiply(2).rotateAroundY(finalI)).clone().add(0, finalI / 2 * finalDirection, 0);
              player.spawnParticle(particle, location, 0, 0, 0, 0);
            }
          }.runTaskLater(RPGCraft.getPlugin(), timer);
          count = 0;
        } else {
          double finalI = i;
          int finalDirection = direction;
          new BukkitRunnable() {
            @Override
            public void run() {
              Location location = playercopy.clone().add(playercopy.getDirection().clone().multiply(2).rotateAroundY(finalI)).clone().add(0, finalI / 2 * finalDirection, 0);
              player.spawnParticle(particle, location, 0, 0, 0, 0);
            }
          }.runTaskLater(RPGCraft.getPlugin(), timer);
        }
        count++;
      }
    }else {
      for (double i = 1; i > -1; i -= density) {
        if (count > 10) {
          timer++;
          double finalI = i;
          int finalDirection = direction;
          new BukkitRunnable() {
            @Override
            public void run() {
              Location location = playercopy.clone().add(playercopy.getDirection().clone().multiply(2).rotateAroundY(finalI)).clone().add(0, finalI / 2 * finalDirection, 0);
              player.spawnParticle(particle, location, 0, 0, 0, 0);
            }
          }.runTaskLater(RPGCraft.getPlugin(), timer);
          count = 0;
        } else {
          double finalI = i;
          int finalDirection = direction;
          new BukkitRunnable() {
            @Override
            public void run() {
              Location location = playercopy.clone().add(playercopy.getDirection().clone().multiply(2).rotateAroundY(finalI)).clone().add(0, finalI / 2 * finalDirection, 0);
              player.spawnParticle(particle, location, 0, 0, 0, 0);
            }
          }.runTaskLater(RPGCraft.getPlugin(), timer);
        }
        count++;
      }
    }
  }
}

