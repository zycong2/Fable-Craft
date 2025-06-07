package io.RPGCraft.FableCraft.commands;

import io.RPGCraft.FableCraft.commands.DONOTTOUCH.command;
import io.RPGCraft.FableCraft.core.GUI;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class itemDB {

    @command(name = "itemDB", playerOnly = true, permission = "RPGCraft.itemDB")
    public boolean onCommand(CommandSender sender, String[] args) {
        Player p = (Player)sender;
        if (!p.hasPermission("FableCraft.itemDB")) {
            p.sendMessage( yamlGetter.getMessage("messages.error.noPermission", p, true));
        } else {
            GUI.itemDBMenu(p);
            p.openInventory(GUI.itemDB);
        }
        return true;
    }
}
