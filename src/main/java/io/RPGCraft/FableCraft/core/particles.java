package io.RPGCraft.FableCraft.core;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

public class particles {

  public void makeParticle(Location loc, int amount, String particle) {
    loc.getWorld().spawnParticle(Particle.valueOf(particle), loc, amount);
  }

  public void particleLine(Location loc, int amount, String particle, int length){
    int loops = amount/length;
    for (int i = 0; i < length; i+=loops){
      Vector direction = loc.getDirection().normalize();
      double x = direction.getX() * i;
      double y = direction.getY() * i + 1.5;
      double z = direction.getZ() * i;
      loc.add(x, y, z);
      makeParticle(loc, 1, particle);
      loc.subtract(x,y,z);
    }
  }
  public void particleSph(Location loc, int amount, String particle, int diameter, boolean hollow){
    for(double phi=0; phi<=Math.PI; phi+=Math.PI/15) {
      for(double theta=0; theta<=2*Math.PI; theta+=Math.PI/30) {
        double r = 1.5;
        double x = r*Math.cos(theta)*Math.sin(phi);
        double y = r*Math.cos(phi) + 1.5;
        double z = r*Math.sin(theta)*Math.sin(phi);

        loc.add(x,y,z);
        makeParticle(loc, amount, particle);
        loc.subtract(x, y, z);
      }
    }
  }
}
