package org.zycong.fableCraft.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zycong.fableCraft.FableCraft;
import org.zycong.fableCraft.core.PDCHelper;
import org.zycong.fableCraft.core.yamlManager;

import java.util.List;

public class stats implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender p, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!p.hasPermission("FableCraft.resetStats")) {
            p.sendMessage((String) yamlManager.getConfig("messages.error.noPermission", (Player) p, true));
            return true;
        }if (args.length == 0 || Bukkit.getPlayer(args[0]) == null){
            p.sendMessage(yamlManager.getConfig("messages.error.noValidArgument", null, true).toString());
            return true;
        }

        String[] skills = yamlManager.getNodes("config", "stats").toArray(new String[0]);
        for(String skill : skills) {
            PDCHelper.setPlayerPDC(skill, Bukkit.getPlayer(args[0]), String.valueOf(yamlManager.getConfig("stats." + skill + ".default", Bukkit.getPlayer(args[0]), true)));
        }
        stats.checkCurrentStats(Bukkit.getPlayer(args[0]));
        p.sendMessage((String)yamlManager.getConfig("messages.info.resetSuccess", Bukkit.getPlayer(args[0]), true));
        return true;
    }


    public static void checkCurrentStats(Player p){
        if (p.getMetadata("currentHealth").getFirst().asDouble() > Double.parseDouble(PDCHelper.getPlayerPDC("Health", p))){
            p.setMetadata("currentHealth", new FixedMetadataValue(FableCraft.getPlugin(), Double.parseDouble(PDCHelper.getPlayerPDC("Health", p))));
        } if (p.getMetadata("currentMana").getFirst().asDouble() > Double.parseDouble(PDCHelper.getPlayerPDC("Mana", p))){
            p.setMetadata("currentMana", new FixedMetadataValue(FableCraft.getPlugin(), Double.parseDouble(PDCHelper.getPlayerPDC("Mana", p))));
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
}
