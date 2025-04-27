package io.RPGCraft.FableCraft.commands.quest;
import java.util.List;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.core.PDCHelper;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.core.lootTableHelper;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;
import static io.RPGCraft.FableCraft.commands.quest.questManager.startQuest;
import static io.RPGCraft.FableCraft.core.PDCHelper.getPlayerPDC;

public class quests implements CommandExecutor, TabCompleter, Listener {
  @Override
  public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
    Player p = (Player) commandSender;
    if (args.length == 0){
      p.sendMessage((TextComponent) yamlGetter.getConfig("messages.error.noValidArgument", null, true));
      return true;
    }

    switch (args[0]) {
      case "startNew":
        startQuest(p, args[1]);
      case "disband":
        String quests = getPlayerPDC("quests", p);
        if (quests.contains(args[1])) {
          PDCHelper.setPlayerPDC("quests", p, quests.replace(";" + args[1], ""));
          PDCHelper.setPlayerPDC(args[1] + ".step", p, null);
          PDCHelper.setPlayerPDC(args[1] + ".progress", p, null);
          p.sendMessage(Colorize((String) yamlGetter.getConfig("messages.info.quests.disband", p, true)));
        } else {
          p.sendMessage(Colorize((String) yamlGetter.getConfig("messages.error.questNotStarted", null, true)));
        }
    }

    return true;
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
    return List.of("startNew", "disband");
  }

}
