package io.RPGCraft.FableCraft.commands.NPC;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;
import static io.RPGCraft.FableCraft.core.PDCHelper.setNPCPDC;

public class CreateNPC implements CommandExecutor {
  private List<String> ValidNPCType = List.of(
    "shop",
    "quest",
    "none"
  );

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

      if(commandSender instanceof Player) {
        for (int i = 0; i <= 3; i++) {
          if (args[i].isBlank()) {
            commandSender.sendMessage(Colorize("&fPlease enter the values [/createnpc <name> <skin> <type>]"));
            return false;
          }
        }

        if (!ValidNPCType.contains(args[2].toLowerCase())){return false;}

        Player player = (Player) commandSender;

        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, args[0]);
        //npc.
        npc.getOrAddTrait(SkinTrait.class).setSkinName(args[1]);
        setNPCPDC("NPCType", npc, args[2]);
        npc.spawn(player.getLocation());

        return false;
      }
      return false;
    }
}
