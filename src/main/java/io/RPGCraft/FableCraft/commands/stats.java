package io.RPGCraft.FableCraft.commands;

import java.util.List;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.core.PDCHelper;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class stats implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player p = (Player) commandSender;
        if (!p.hasPermission("FableCraft.resetStats")) {
            p.sendMessage(yamlGetter.getMessage("messages.error.noPermission", p, true));
            return true;
        }if (args.length == 0 || Bukkit.getPlayer(args[0]) == null){
            p.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", null, true));
            return true;
        }

        String[] skills = yamlGetter.getNodes("config", "stats").toArray(new String[0]);
        for(String skill : skills) {
            PDCHelper.setPlayerPDC(skill, Bukkit.getPlayer(args[0]), String.valueOf(yamlGetter.getConfig("stats." + skill + ".default", Bukkit.getPlayer(args[0]), true)));
        }
        stats.checkCurrentStats(Bukkit.getPlayer(args[0]));
        p.sendMessage(yamlGetter.getMessage("messages.info.resetSuccess", Bukkit.getPlayer(args[0]), true));
        return true;
    }


    public static void checkCurrentStats(Player p){
        if (p.getMetadata("currentHealth").getFirst() != null) {
          if (p.getMetadata("currentHealth").getFirst().asDouble() > Double.parseDouble(PDCHelper.getPlayerPDC("Health", p))) {
            p.setMetadata("currentHealth", new FixedMetadataValue(RPGCraft.getPlugin(), Double.parseDouble(PDCHelper.getPlayerPDC("Health", p))));
          }
        } else {
          p.setMetadata("currentHealth", new FixedMetadataValue(RPGCraft.getPlugin(), Double.parseDouble(PDCHelper.getPlayerPDC("Health", p))));
        }
        if (p.getMetadata("currentMana").getFirst() != null){
          if (p.getMetadata("currentMana").getFirst().asDouble() > Double.parseDouble(PDCHelper.getPlayerPDC("Mana", p))) {
            p.setMetadata("currentMana", new FixedMetadataValue(RPGCraft.getPlugin(), Double.parseDouble(PDCHelper.getPlayerPDC("Mana", p))));
          }
        } else {
          p.setMetadata("currentMana", new FixedMetadataValue(RPGCraft.getPlugin(), Double.parseDouble(PDCHelper.getPlayerPDC("Mana", p))));
        }
    }
    @Override
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
