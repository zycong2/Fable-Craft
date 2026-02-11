package io.RPGCraft.FableCraft.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationUtil {
  public static String locationToString(Location loc) {
    if (loc == null) return "";
    return loc.getWorld().getName() + "," +
      loc.getX() + "," +
      loc.getY() + "," +
      loc.getZ() + "," +
      loc.getYaw() + "," +
      loc.getPitch();
  }

  // 2. Convert String back to Location
  public static Location stringToLocation(String str) {
    if (str == null || str.trim().isEmpty()) return null;

    String[] parts = str.split(",");
    if (parts.length < 4) return null; // Needs at least world, x, y, z

    World world = Bukkit.getWorld(parts[0]);
    if (world == null) return null; // World must be loaded on the server

    double x = Double.parseDouble(parts[1]);
    double y = Double.parseDouble(parts[2]);
    double z = Double.parseDouble(parts[3]);

    float yaw = 0;
    float pitch = 0;

    if (parts.length >= 6) {
      yaw = Float.parseFloat(parts[4]);
      pitch = Float.parseFloat(parts[5]);
    }

    return new Location(world, x, y, z, yaw, pitch);
  }
}
