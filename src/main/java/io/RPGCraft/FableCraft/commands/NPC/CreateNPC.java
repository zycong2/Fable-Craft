package io.RPGCraft.FableCraft.commands.NPC;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;

public class CreateNPC implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

      if(commandSender instanceof Player) {
        if (args[0].isBlank() || args[0].isEmpty()) {
          commandSender.sendMessage(Colorize("Please enter the name"));
          return false;
        }

        Player player = (Player) commandSender;

        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, args[0]);
        npc.spawn(player.getLocation());

        return false;
      }
      return false;
    }
}
