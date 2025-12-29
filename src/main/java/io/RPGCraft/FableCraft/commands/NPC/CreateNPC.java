package io.RPGCraft.FableCraft.commands.NPC;

import io.RPGCraft.FableCraft.Utils.commandHelper.CommandInterface;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;
import static io.RPGCraft.FableCraft.RPGCraft.IsCitizen;
import static io.RPGCraft.FableCraft.core.PDCHelper.setNPCPDC;

public class CreateNPC implements CommandInterface {
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

        if (!IsCitizen){Bukkit.getLogger().warning("Citizens is not installed, please install it to use this command");
          commandSender.sendMessage(Colorize("&cCitizens is not installed, please install it to use this command"));}

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

  @Override
  public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
    return List.of();
  }
}
