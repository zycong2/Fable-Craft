package io.RPGCraft.FableCraft.commands;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.Utils.commandHelper.CommandInterface;
import io.RPGCraft.FableCraft.core.Helpers.PDCHelper;
import io.RPGCraft.FableCraft.core.Stats.StatsMemory;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class stats implements CommandInterface {
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player p = (Player) commandSender;
        if (!p.hasPermission("FableCraft.resetStats")) {
            p.sendMessage(yamlGetter.getMessage("messages.error.noPermission", p, true));
            return true;
        }
        Player player = Bukkit.getPlayer(args[0]);
        if (args.length == 0 || player == null){
            p.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", null, true));
            return true;
        }

        String[] skills = yamlGetter.getNodes("config", "stats").toArray(new String[0]);
        StatsMemory statsMem = player.getStatsMemory();
        for(String skill : skills) {
          statsMem.stat(skill, Double.valueOf(yamlGetter.getConfig("stats." + skill + ".default", player, true).toString()));
        }
        stats.checkCurrentStats(player);
        p.sendMessage(yamlGetter.getMessage("messages.info.resetSuccess", player, true));
        return true;
    }


    public static void checkCurrentStats(Player p){
      StatsMemory stats = p.getStatsMemory();
      if (p.getMetadata("currentHealth").getFirst() != null) {
          if (p.getMetadata("currentHealth").getFirst().asDouble() > stats.statDouble("Health")) {
            p.setMetadata("currentHealth", new FixedMetadataValue(RPGCraft.getPlugin(), stats.statDouble("Health")));
          }
        } else {
          p.setMetadata("currentHealth", new FixedMetadataValue(RPGCraft.getPlugin(), stats.statDouble("Health")));
        }
        if (p.getMetadata("currentMana").getFirst() != null){
          if (p.getMetadata("currentMana").getFirst().asDouble() > stats.statDouble("Mana")) {
            p.setMetadata("currentMana", new FixedMetadataValue(RPGCraft.getPlugin(), stats.statDouble("Mana")));
          }
        } else {
          p.setMetadata("currentMana", new FixedMetadataValue(RPGCraft.getPlugin(), stats.statDouble("Mana")));
        }
    }
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<Player> players = (List<Player>) Bukkit.getOnlinePlayers();
        List<String> args = new java.util.ArrayList<>(List.of());
        for (Player p : players){
            args.add(p.getName());
        }
        return args;
    }

    public void setStat(Player p, String stat, Double amount){
      PDCHelper.setPlayerPDC(stat, p, amount.toString());
    }
}
