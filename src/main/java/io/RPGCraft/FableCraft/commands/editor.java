package io.RPGCraft.FableCraft.commands;

import io.RPGCraft.FableCraft.Utils.commandHelper.CommandInterface;
import io.RPGCraft.FableCraft.core.Achievements;
import io.RPGCraft.FableCraft.core.CraftingStations;
import io.RPGCraft.FableCraft.core.ConfigMenu;
import io.RPGCraft.FableCraft.core.CustomEnchantments;
import io.RPGCraft.FableCraft.core.MainGUI;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.commands.mobs.mobsEditor;
import io.RPGCraft.FableCraft.commands.quest.quests;
import io.RPGCraft.FableCraft.listeners.ItemEditor.QuestEditor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;
import static io.RPGCraft.FableCraft.RPGCraft.MM;

public class editor implements CommandInterface {

  // Editor name -> description for the help list
  static final List<String> EDITOR_NAMES = Arrays.asList(
    "itemdb",
    "config",
    "crafting",
    "enchantments",
    "advancements",
    "mobs",
    "quests",
    "yamledit"
  );

  @Override
  public boolean onCommand(
    @NotNull CommandSender sender,
    @NotNull Command command,
    @NotNull String label,
    @NotNull String[] args
  ) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(yamlGetter.getMessage("messages.general.onlyPlayers", null, true));
      return true;
    }

    // No args — show help list
    if (args.length == 0) {
      showHelp(player);
      return true;
    }

    String editorName = args[0].toLowerCase(Locale.ROOT);

    // yamlEdit is special — it's a toggle, not a GUI
    if (editorName.equals("yamledit")) {
      if (!player.hasPermission("rpgcraft.yamlEdit")) {
        player.sendMessage(yamlGetter.getMessage("messages.editor.yamlEdit.noPermission", player, true));
        return true;
      }
      yamlManager.getInstance().setEditing(!yamlManager.getInstance().getEditing());
      player.sendMessage(MM(yamlManager.getInstance().getFileConfig("messages").getString("messages.editor.yamlEdit.toggled", "").replace("%value%", String.valueOf(yamlManager.getInstance().getEditing()))));
      return true;
    }

    // All other editors require the generic editor permission
    if (!player.hasPermission("rpgcraft.editor")) {
      player.sendMessage(yamlGetter.getMessage("messages.editor.noPermission", player, true));
      return true;
    }

    // Also check the specific permission where applicable
    switch (editorName) {
      case "itemdb" -> {
        if (!player.hasPermission("rpgcraft.itemDB")) {
          player.sendMessage(yamlGetter.getMessage("messages.editor.itemdb.noPermission", player, true));
          return true;
        }
        MainGUI.itemDBMenu(player).open(player);
      }
      case "config" -> {
        if (!player.hasPermission("rpgcraft.config")) {
          player.sendMessage(yamlGetter.getMessage("messages.editor.config.noPermission", player, true));
          return true;
        }
        ConfigMenu.openConfigMenu(player);
      }
      case "crafting" -> {
        if (!player.hasPermission("rpgcraft.crafting")) {
          player.sendMessage(yamlGetter.getMessage("messages.editor.crafting.noPermission", player, true));
          return true;
        }
        CraftingStations.openStationsBrowser(player);
      }
      case "enchantments" -> {
        if (!player.hasPermission("rpgcraft.enchantments")) {
          player.sendMessage(yamlGetter.getMessage("messages.editor.enchantments.noPermission", player, true));
          return true;
        }
        CustomEnchantments.openEnchantmentsGUI(player);
      }
      case "advancements" -> {
        if (!player.hasPermission("rpgcraft.advancements")) {
          player.sendMessage(yamlGetter.getMessage("messages.editor.advancements.noPermission", player, true));
          return true;
        }
        Achievements.openAchievementsGUI(player);
      }
      case "mobs" -> {
        if (!player.hasPermission("rpgcraft.mobs")) {
          player.sendMessage(yamlGetter.getMessage("messages.editor.mobs.noPermission", player, true));
          return true;
        }
        mobsEditor.openMobDB(player);
      }
      case "quests" -> {
        if (!player.hasPermission("rpgcraft.quests")) {
          player.sendMessage(yamlGetter.getMessage("messages.editor.quests.noPermission", player, true));
          return true;
        }
        // Route to the full editor if the player has edit permission
        if (player.hasPermission("rpgcraft.questEdit")) {
          QuestEditor.makeQuestBrowser().open(player);
        } else {
          quests.openQuestsGUI(player);
        }
      }
      default -> {
        player.sendMessage(MM(yamlManager.getInstance().getFileConfig("messages").getString("messages.editor.unknown", "").replace("%name%", editorName)));
        player.sendMessage(yamlGetter.getMessage("messages.editor.unknown.help", player, true));
      }
    }

    return true;
  }

  @Override
  public List<String> onTabComplete(
    @NotNull CommandSender sender,
    @NotNull Command command,
    @NotNull String label,
    @NotNull String[] args
  ) {
    if (args.length == 1) {
      String prefix = args[0].toLowerCase(Locale.ROOT);
      List<String> completions = new ArrayList<>();
      for (String name : EDITOR_NAMES) {
        if (name.startsWith(prefix)) {
          completions.add(name);
        }
      }
      return completions;
    }
    return List.of();
  }

  private static void showHelp(Player p) {
    p.sendMessage(yamlGetter.getMessage("messages.editor.help.title", p, true));
    p.sendMessage(yamlGetter.getMessage("messages.editor.help.usage", p, true));
    p.sendMessage(MM(""));
    p.sendMessage(yamlGetter.getMessage("messages.editor.help.available", p, true));
    p.sendMessage(yamlGetter.getMessage("messages.editor.help.itemdb", p, true));
    p.sendMessage(yamlGetter.getMessage("messages.editor.help.config", p, true));
    p.sendMessage(yamlGetter.getMessage("messages.editor.help.crafting", p, true));
    p.sendMessage(yamlGetter.getMessage("messages.editor.help.enchantments", p, true));
    p.sendMessage(yamlGetter.getMessage("messages.editor.help.advancements", p, true));
    p.sendMessage(yamlGetter.getMessage("messages.editor.help.mobs", p, true));
    p.sendMessage(yamlGetter.getMessage("messages.editor.help.quests", p, true));
    p.sendMessage(yamlGetter.getMessage("messages.editor.help.yamledit", p, true));
  }
}
