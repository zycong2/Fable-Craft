package io.RPGCraft.FableCraft.commands;

import io.RPGCraft.FableCraft.core.GUI;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import io.RPGCraft.FableCraft.listeners.mainListeners;

public class itemDB implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player p = (Player)commandSender;
        if (!p.hasPermission("FableCraft.itemDB")) {
            p.sendMessage( yamlGetter.getMessage("messages.error.noPermission", p, true));
        } else {
            GUI.itemDBMenu(p);
            p.openInventory(GUI.itemDB);
        }
        return true;
    }
}
