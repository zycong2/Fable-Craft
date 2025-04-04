package org.zycong.fableCraft.core;

import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.Particle;

import javax.xml.stream.Location;

public class particles {

  public void makeParticle(Location loc, int amount, String particle, int a, int r, int g, int b){
    ParticleBuilder
    .color((int) a, (int) r,(int)  g,(int)  b)
            .locatoin(loc)
            .count(100)
            .particle(Particle.valueOf(particle))
            .spawn();;
  }
}