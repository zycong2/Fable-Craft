package io.RPGCraft.FableCraft.commands;

import io.RPGCraft.FableCraft.Utils.commandHelper.CommandInterface;
import io.RPGCraft.FableCraft.commands.NPC.NPChandler.setPDC;
import io.RPGCraft.FableCraft.commands.mobs.mobs;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class reload implements CommandInterface {

  @Override
  public boolean onCommand(
    @NotNull CommandSender sender,
    @NotNull Command command,
    @NotNull String label,
    @NotNull String[] args
  ) {
    if (!sender.hasPermission("rpgcraft.reload")) {
      if (sender instanceof Player player) {
        player.sendMessage(
          yamlGetter.getMessage(
            "messages.error.noPermission",
            player,
            true
          )
        );
      } else {
        sender.sendMessage("You don't have permission.");
      }
      return true;
    }

    yamlManager manager = yamlManager.getInstance();

    // Reload all YAML files from disk.
    if (!manager.loadData()) {
      sender.sendMessage("Failed to reload YAML files. Check console.");
      Bukkit.getLogger().severe(
        "FableCraft failed to reload YAML files."
      );
      return true;
    }

    try {
      // Rebuild custom items from the newly loaded YAML data.
      manager.getCustomItems();

      // Reinitialize systems that depend on configuration.
      mobs.reloadSpawns();
      setPDC.initializeNPCs();

      // Apply recipe configuration.
      if (yamlGetter.getConfig(
        "items.removeDefaultRecipes", null, false
      ).equals(true)) {
        Bukkit.clearRecipes();
      } else {
        Bukkit.resetRecipes();
      }

      sender.sendMessage("FableCraft configuration reloaded.");
      Bukkit.getLogger().info(
        "FableCraft configuration reloaded successfully."
      );
    } catch (Exception e) {
      sender.sendMessage(
        "An error occurred while reloading FableCraft. Check console."
      );
      Bukkit.getLogger().severe(
        "FableCraft reload failed: " + e.getMessage()
      );
      e.printStackTrace();
    }

    return true;
  }

  @Override
  public List<String> onTabComplete(
    CommandSender sender,
    Command command,
    String label,
    String[] args
  ) {
    return List.of();
  }
}
