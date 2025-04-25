package io.RPGCraft.FableCraft.commands;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.commands.NPC.NPChandler.setPDC;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;
import static io.RPGCraft.FableCraft.core.YAML.yamlManager.getFileConfig;

public class reload implements CommandExecutor {
  @Override
  public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
    Player p = (Player) commandSender;
    if (!p.hasPermission("FableCraft.reload")) {
      p.sendMessage(Colorize(yamlGetter.getConfig("messages.error.noPermission", p, true).toString()));
      return true;
    }
    getFileConfig("data").set("customMobs", RPGCraft.customMobs);
    if (!yamlManager.saveData()) {
      Bukkit.getLogger().severe("Failed to save data!");
    }


    if (!yamlManager.loadData()) { //don't ever put code in the line before this one otherwise you WILL get errors
      Bukkit.getLogger().severe("Failed to load config!");
    }
    if (!yamlManager.loadPlayerData()){
      Bukkit.getLogger().severe("Failed to load data!");
    }
    if (yamlGetter.getConfig("items.removeDefaultRecipes", null, false).equals(true)) {Bukkit.clearRecipes();} else {Bukkit.resetRecipes();}
    yamlManager.getCustomItems();
    mobs.reloadSpawns();
    setPDC.initializeNPCs();

    return true;
  }
}
