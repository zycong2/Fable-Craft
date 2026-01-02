package io.RPGCraft.FableCraft.commands.NPC.NPChandler;

import io.RPGCraft.FableCraft.Utils.commandHelper.CommandInterface;
import io.RPGCraft.FableCraft.core.Helpers.PDCHelper;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import static io.RPGCraft.FableCraft.Utils.Utils.isCitizensNPC;

public class setPDC implements CommandInterface {
  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
    Player p = (Player) sender;
    if (!p.hasPermission("FableCraft.npc")) {
      p.sendMessage(yamlGetter.getMessage("messages.error.noPermission", p, true));
      return true;
    }
    if (args.length != 0){
      if (args[0].equals("remove")){
        for(Entity e : p.getNearbyEntities(10, 10, 10)){
          if(isCitizensNPC(e)){
            LivingEntity npc = (LivingEntity) e;
            PDCHelper.setNPCPDC("NPCType", npc, null);
            return true;
          }
        }
      }else if (args[0].equals("set")){
        if (args.length == 2) {
          for (Entity e : p.getNearbyEntities(10, 10, 10)) {
            if (isCitizensNPC(e)) {
              LivingEntity npc = (LivingEntity) e;
              PDCHelper.setNPCPDC("NPCType", npc, args[1]);
              return true;
            }
          }
        }
      }
    }
    return true;
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

  public static void initializeNPCs(){
    /*if (yamlManager.getInstance().getFileConfig("data").isSet("NPCType")) {
      List<Object> NPCsObject = yamlGetter.getNodes("data", "NPCType");
      List<String> NPCs = new java.util.ArrayList<>(List.of());
      for (Object o : NPCsObject) {
        NPCs.add((String) o);
      }

      for (String s : NPCs) {
        Entity target = Bukkit.getEntity(UUID.fromString(s));
        if (target != null) {
          if (isCitizensNPC(target)) {
            PDCHelper.setNPCPDC("NPCType", target, get);
          }
        }
      }
    }*/
  }
}
