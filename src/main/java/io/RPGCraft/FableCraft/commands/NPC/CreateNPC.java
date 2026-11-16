package io.RPGCraft.FableCraft.commands.NPC;

import io.RPGCraft.FableCraft.Utils.commandHelper.CommandInterface;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;
import static io.RPGCraft.FableCraft.RPGCraft.IsCitizen;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.setNPCPDC;

public class CreateNPC implements CommandInterface {
  private List<String> ValidNPCType = List.of(
    "shop",
    "quest",
    "banker",
    "none"
  );

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      if (!sender.hasPermission("rpgcraft.npc")) {
        if (sender instanceof Player p) {
          p.sendMessage(yamlGetter.getMessage("messages.error.noPermission", p, true));
        } else {
          sender.sendMessage(yamlGetter.getMessage("messages.general.consoleNoPermission", null, true));
        }
        return true;
      }
      if (!(sender instanceof Player player)) {
        sender.sendMessage(yamlGetter.getMessage("messages.general.onlyPlayers", null, true));
        return true;
      }
      if (args.length < 3) {
        sender.sendMessage(yamlGetter.getMessage("messages.npc.create.usage", null, true));
        return true;
      }
      for (int i = 0; i < 3; i++) {
        if (args[i].isBlank()) {
          sender.sendMessage(yamlGetter.getMessage("messages.npc.create.usage", null, true));
          return true;
        }
      }

      if (!ValidNPCType.contains(args[2].toLowerCase())) {
        sender.sendMessage(yamlGetter.getMessage("messages.npc.create.invalidType", null, true));
        return true;
      }

      if (!IsCitizen) {
        Bukkit.getLogger().severe("Citizens is not installed, please install it to use this command");
        sender.sendMessage(yamlGetter.getMessage("messages.npc.create.noCitizens", null, true));
        return true;
      }

      NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, args[0]);
      npc.getOrAddTrait(SkinTrait.class).setSkinName(args[1]);
      npc.spawn(player.getLocation());
      setNPCPDC("NPCType", npc.getEntity(), args[2]);
      return true;
    }

  @Override
  public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
      if (args.length == 3){
        return List.of("shop", "quest", "banker", "none");
      }

    return null;

  }
}
