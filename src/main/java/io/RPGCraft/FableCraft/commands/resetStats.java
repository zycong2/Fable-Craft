package io.RPGCraft.FableCraft.commands;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.Utils.commandHelper.CommandInterface;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.getPlayerPDC;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.setPlayerPDC;

public class resetStats implements CommandInterface {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        if (!p.hasPermission("rpgcraft.resetStats")) {
            p.sendMessage(yamlGetter.getMessage("messages.error.noPermission", p, true));
            return true;
        }

        if (args.length == 0) {
            p.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", null, true));
            return true;
        }
        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            p.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", null, true));
            return true;
        }

        String[] skills = yamlGetter.getNodes("config", "stats").toArray(new String[0]);

        for (String skill : skills) {
            setPlayerPDC(skill, player, String.valueOf(Double.parseDouble(yamlGetter.getConfig("stats." + skill + ".default", player, true).toString())));
        }

        for (String st : List.of("Health", "Mana")) {
            player.setMetadata("current" + st, new FixedMetadataValue(RPGCraft.getPlugin(), getPlayerPDC("current" + st, player)));
        }

        resetStats.checkCurrentStats(player);
        p.sendMessage(yamlGetter.getMessage("messages.info.resetSuccess", player, true));
        return true;
    }


    public static void checkCurrentStats(Player p) {
        String healthStr = getPlayerPDC("Health", p);
        String manaStr = getPlayerPDC("Mana", p);
        if (healthStr != null) {
            try {
                double health = Double.parseDouble(healthStr);
                var healthMeta = p.getMetadata("currentHealth");
                if (healthMeta.isEmpty() || healthMeta.getFirst().asDouble() > health) {
                    p.setMetadata("currentHealth", new FixedMetadataValue(RPGCraft.getPlugin(), health));
                }
            } catch (NumberFormatException ignored) {
                p.setMetadata("currentHealth", new FixedMetadataValue(RPGCraft.getPlugin(), 0.0));
            }
        }
        if (manaStr != null) {
            try {
                double mana = Double.parseDouble(manaStr);
                var manaMeta = p.getMetadata("currentMana");
                if (manaMeta.isEmpty() || manaMeta.getFirst().asDouble() > mana) {
                    p.setMetadata("currentMana", new FixedMetadataValue(RPGCraft.getPlugin(), mana));
                }
            } catch (NumberFormatException ignored) {
                p.setMetadata("currentMana", new FixedMetadataValue(RPGCraft.getPlugin(), 0.0));
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> players = new java.util.ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            players.add(p.getName());
        }
        return players;
    }

    public void setStat(Player p, String stat, Double amount) {
        setPlayerPDC(stat, p, amount.toString());
    }
}
