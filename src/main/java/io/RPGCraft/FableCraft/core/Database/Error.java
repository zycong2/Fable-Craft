package io.RPGCraft.FableCraft.core.Database;

import io.RPGCraft.FableCraft.RPGCraft;

import java.util.logging.Level;

public class Error {
  public static void execute(RPGCraft plugin, Exception ex){
    plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
  }
  public static void close(RPGCraft plugin, Exception ex){
    plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
  }
}
