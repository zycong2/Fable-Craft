package io.RPGCraft.FableCraft.listeners;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.*;

public class buildingBreaking implements Listener {
  @EventHandler
  void blockBreak(BlockBreakEvent e) {
    if (Boolean.parseBoolean(yamlGetter.getConfig("breaking.enabled").toString())) {
      Player p = e.getPlayer();
      Block blo = e.getBlock();
      Location loc = blo.getLocation();
      Material originalType = blo.getType();
      BlockData originalData = blo.getBlockData().clone();

      Map<String, Object> blockData = new HashMap<>();
      blockData.put("action", "break");
      blockData.put("world", loc.getWorld().getName());
      blockData.put("x", loc.getBlockX());
      blockData.put("y", loc.getBlockY());
      blockData.put("z", loc.getBlockZ());
      blockData.put("type", originalType.toString());
      blockData.put("data", originalData.getAsString());
      blockData.put("expireTime",
        System.currentTimeMillis() +
          (Integer.parseInt(yamlGetter.getConfig("breaking.removeTime").toString()) * 1000L)
      );

      neededToPlace.add(blo);

      addBlockToPersistentStorage(blockData);

      if (!p.getGameMode().equals(GameMode.CREATIVE)) {
        int delayTicks = Integer.parseInt(yamlGetter.getConfig("breaking.removeTime").toString()) * 20;

        Bukkit.getScheduler().runTaskLater(RPGCraft.getPlugin(), () -> {
          Block targetBlock = loc.getWorld().getBlockAt(loc);
          targetBlock.setType(originalType);
          targetBlock.setBlockData(originalData);
          neededToPlace.remove(blo);

          removeBlockFromPersistentStorage(loc, "break");
        }, delayTicks);
      }
    }
  }

  @EventHandler
  void blockPlace(BlockPlaceEvent e) {
    if (Boolean.parseBoolean(yamlGetter.getConfig("placing.enabled").toString())) {
      Player p = e.getPlayer();
      Block blo = e.getBlock();
      Location loc = blo.getLocation();
      Material placedType = blo.getType();
      BlockData placedData = blo.getBlockData().clone();

      Map<String, Object> blockData = new HashMap<>();
      blockData.put("action", "place");
      blockData.put("world", loc.getWorld().getName());
      blockData.put("x", loc.getBlockX());
      blockData.put("y", loc.getBlockY());
      blockData.put("z", loc.getBlockZ());
      blockData.put("type", placedType.toString());
      blockData.put("data", placedData.getAsString());
      blockData.put("expireTime",
        System.currentTimeMillis() +
          (Integer.parseInt(yamlGetter.getConfig("placing.removeTime").toString()) * 1000L)
      );

      placedBlocks.add(blo);

      addBlockToPersistentStorage(blockData);

      if (!p.getGameMode().equals(GameMode.CREATIVE)) {
        int delayTicks = Integer.parseInt(yamlGetter.getConfig("placing.removeTime").toString()) * 20;

        Bukkit.getScheduler().runTaskLater(RPGCraft.getPlugin(), () -> {
          Block targetBlock = loc.getWorld().getBlockAt(loc);
          targetBlock.setType(Material.AIR);
          placedBlocks.remove(blo);

          removeBlockFromPersistentStorage(loc, "place");
        }, delayTicks);
      }
    }
  }

  private Set<Block> neededToPlace = new HashSet<>();
  private Set<Block> placedBlocks = new HashSet<>();

  private void addBlockToPersistentStorage(Map<String, Object> blockData) {
    try {
      List<Map<String, Object>> blocks = new ArrayList<>();
      Object existing = yamlManager.getInstance().getOption("data", "pendingBlocks");

      if (existing instanceof List) {
        blocks = (List<Map<String, Object>>) existing;
      }

      blocks.add(blockData);

      yamlManager.getInstance().setOption("data", "pendingBlocks", blocks);

    } catch (Exception ex) {
      Bukkit.getLogger().severe("Failed to save block data: " + ex.getMessage());
    }
  }


  private static void removeBlockFromPersistentStorage(Location loc, String action) {
    try {
      List<Map<String, Object>> blocks = new ArrayList<>();
      Object existing = yamlManager.getInstance().getOption("data", "pendingBlocks");

      if (existing instanceof List) {
        blocks = (List<Map<String, Object>>) existing;

        blocks.removeIf(data ->
          data.get("world").equals(loc.getWorld().getName()) &&
            (int) data.get("x") == loc.getBlockX() &&
            (int) data.get("y") == loc.getBlockY() &&
            (int) data.get("z") == loc.getBlockZ() &&
            data.get("action").equals(action)
        );

        yamlManager.getInstance().setOption("data", "pendingBlocks", blocks);
      }

    } catch (Exception ex) {
      Bukkit.getLogger().severe("Failed to remove block data: " + ex.getMessage());
    }
  }

  public static void restorePendingBlocks() {
    try {
      Object existing = yamlManager.getInstance().getOption("data", "pendingBlocks");

      if (!(existing instanceof List)) {
        return;
      }

      List<Map<String, Object>> blocks = (List<Map<String, Object>>) existing;
      long currentTime = System.currentTimeMillis();

      for (Map<String, Object> blockData : new ArrayList<>(blocks)) {
        String action = (String) blockData.get("action");
        long expireTime = (long) blockData.get("expireTime");

        if (expireTime > currentTime) {
          long remainingTime = (expireTime - currentTime) / 1000L;

          if (remainingTime > 0) {
            scheduleDelayedAction(blockData, remainingTime);
          } else {
            processBlockAction(blockData);
            blocks.remove(blockData);
          }
        } else {
          processBlockAction(blockData);
          blocks.remove(blockData);
        }
      }

      yamlManager.getInstance().setOption("data", "pendingBlocks", blocks);

    } catch (Exception ex) {
      Bukkit.getLogger().severe("Failed to restore pending blocks: " + ex.getMessage());
    }
  }

  private static void scheduleDelayedAction(Map<String, Object> blockData, long seconds) {
    String action = (String) blockData.get("action");
    String worldName = (String) blockData.get("world");
    int x = (int) blockData.get("x");
    int y = (int) blockData.get("y");
    int z = (int) blockData.get("z");
    String typeStr = (String) blockData.get("type");
    String dataStr = (String) blockData.get("data");

    Bukkit.getScheduler().runTaskLater(RPGCraft.getPlugin(), () -> {
      World world = Bukkit.getWorld(worldName);
      if (world == null) return;

      Location loc = new Location(world, x, y, z);
      Block block = loc.getBlock();

      try {
        if (action.equals("break")) {
          Material type = Material.valueOf(typeStr);
          BlockData data = Bukkit.createBlockData(dataStr);
          block.setType(type);
          block.setBlockData(data);
        } else if (action.equals("place")) {
          block.setType(Material.AIR);
        }

        removeBlockFromPersistentStorage(loc, action);

      } catch (IllegalArgumentException ex) {
        Bukkit.getLogger().warning("Invalid block data for " + action + " at " + loc);
      }

    }, seconds * 20L);
  }

  private static void processBlockAction(Map<String, Object> blockData) {
    String action = (String) blockData.get("action");
    String worldName = (String) blockData.get("world");
    int x = (int) blockData.get("x");
    int y = (int) blockData.get("y");
    int z = (int) blockData.get("z");
    String typeStr = (String) blockData.get("type");
    String dataStr = (String) blockData.get("data");

    World world = Bukkit.getWorld(worldName);
    if (world == null) return;

    Location loc = new Location(world, x, y, z);
    Block block = loc.getBlock();

    try {
      if (action.equals("break")) {
        Material type = Material.valueOf(typeStr);
        BlockData data = Bukkit.createBlockData(dataStr);
        block.setType(type);
        block.setBlockData(data);
      } else if (action.equals("place")) {
        block.setType(Material.AIR);
      }

    } catch (IllegalArgumentException ex) {
      Bukkit.getLogger().warning("Invalid block data for " + action + " at " + loc);
    }
  }
}
