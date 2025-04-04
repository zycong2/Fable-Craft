package io.papermc.paperweight.testplugin.commands;

import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import io.papermc.paperweight.testplugin.core.yamlManager;
import io.papermc.paperweight.testplugin.listeners.mainListeners;

public class itemDB implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player p = (Player)commandSender;
        if (!p.hasPermission("FableCraft.itemDB")) {
            p.sendMessage((TextComponent)  yamlManager.getConfig("messages.error.noPermission", p, true));
        } else {
            mainListeners.itemDBMenu(p);
            p.openInventory(mainListeners.itemDB);
        }
        return true;
    }
}
