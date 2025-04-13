package io.RPGCraft.FableCraft.commands.NPC.NPChandler;

import io.RPGCraft.FableCraft.core.PDCHelper;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import net.citizensnpcs.api.npc.NPC;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static io.RPGCraft.FableCraft.Utils.Utils.isCitizensNPC;

public class setPDC implements CommandExecutor, TabCompleter {
  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
    Player p = (Player) sender;
    if (!p.hasPermission("FableCraft.npc")) {
      p.sendMessage((TextComponent)  yamlGetter.getConfig("messages.error.noPermission", p, true));
    }
    if (args.length != 0){
      if (args[0].equals("remove")){
        for(Entity e : p.getNearbyEntities(10, 10, 10)){
          if(isCitizensNPC(e)){
            LivingEntity npc = (LivingEntity) e;
            PDCHelper.setPlayerPDC("NPCType", (Player) npc, null);
            return true;
          }
        }
      }else if (args[0].equals("set")){
        if (args.length == 2) {
          for (Entity e : p.getNearbyEntities(10, 10, 10)) {
            if (isCitizensNPC(e)) {
              LivingEntity npc = (LivingEntity) e;
              PDCHelper.setPlayerPDC("NPCType", (Player) npc, args[1]);
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
    if (args.length == 1){
      return List.of("set", "remove");
    }
    else {
      if (args[0].equals("set")) {
        return List.of("shop", "quest");
      } else {
        return List.of();
      }
    }
  }
}
