package io.RPGCraft.FableCraft.commands.NPC.NPChandler;

import io.RPGCraft.FableCraft.core.PDCHelper;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import net.citizensnpcs.api.npc.NPC;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import static io.RPGCraft.FableCraft.Utils.Utils.isCitizensNPC;

public class setPDC implements CommandExecutor, TabCompleter {
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
            PDCHelper.setPlayerPDC("NPCType", (Player) npc, null);
            yamlManager.getInstance().setOption("data", "NPCType." + npc.getUniqueId(), null);
            return true;
          }
        }
      }else if (args[0].equals("set")){
        if (args.length == 2) {
          for (Entity e : p.getNearbyEntities(10, 10, 10)) {
            if (isCitizensNPC(e)) {
              LivingEntity npc = (LivingEntity) e;
              PDCHelper.setPlayerPDC("NPCType", (Player) npc, args[1]);
              yamlManager.getInstance().setOption("data", "NPCType." + npc.getUniqueId(), args[1]);
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
    if (yamlManager.getInstance().getFileConfig("data").isSet("NPCType")) {
      List<Object> NPCsObject = yamlGetter.getNodes("data", "NPCType");
      List<String> NPCs = new java.util.ArrayList<>(List.of());
      for (Object o : NPCsObject) {
        NPCs.add((String) o);
      }

      for (String s : NPCs) {
        Entity target = Bukkit.getEntity(UUID.fromString(s));
        if (target != null) {
          if (isCitizensNPC(target)) {
            PDCHelper.setPlayerPDC("NPCType", (Player) target, yamlManager.getInstance().getOption("data", "NPCType." + s).toString());
          }
        }
      }
    }
  }
}
