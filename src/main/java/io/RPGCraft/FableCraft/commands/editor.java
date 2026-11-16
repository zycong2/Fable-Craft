package io.RPGCraft.FableCraft.commands;

import io.RPGCraft.FableCraft.Utils.commandHelper.CommandInterface;
import io.RPGCraft.FableCraft.core.Achievements;
import io.RPGCraft.FableCraft.core.CraftingStations;
import io.RPGCraft.FableCraft.core.ConfigMenu;
import io.RPGCraft.FableCraft.core.CustomEnchantments;
import io.RPGCraft.FableCraft.core.MainGUI;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
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
      sender.sendMessage("This command can only be used by players.");
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
        player.sendMessage("§cYou don't have permission to use the YAML editor!");
        return true;
      }
      yamlManager.getInstance().setEditing(!yamlManager.getInstance().getEditing());
      player.sendMessage("Set YAML editing to " + yamlManager.getInstance().getEditing());
      return true;
    }

    // All other editors require the generic editor permission
    if (!player.hasPermission("rpgcraft.editor")) {
      player.sendMessage("§cYou don't have permission to access editors!");
      return true;
    }

    // Also check the specific permission where applicable
    switch (editorName) {
      case "itemdb" -> {
        if (!player.hasPermission("rpgcraft.itemDB")) {
          player.sendMessage("§cYou don't have permission to access the ItemDB editor!");
          return true;
        }
        MainGUI.itemDBMenu(player).open(player);
      }
      case "config" -> {
        if (!player.hasPermission("rpgcraft.config")) {
          player.sendMessage("§cYou don't have permission to access the config editor!");
          return true;
        }
        ConfigMenu.openConfigMenu(player);
      }
      case "crafting" -> {
        if (!player.hasPermission("rpgcraft.crafting")) {
          player.sendMessage("§cYou don't have permission to access the crafting editor!");
          return true;
        }
        CraftingStations.openStationsBrowser(player);
      }
      case "enchantments" -> {
        if (!player.hasPermission("rpgcraft.enchantments")) {
          player.sendMessage("§cYou don't have permission to browse enchantments!");
          return true;
        }
        CustomEnchantments.openEnchantmentsGUI(player);
      }
      case "advancements" -> {
        if (!player.hasPermission("rpgcraft.advancements")) {
          player.sendMessage("§cYou don't have permission to view advancements!");
          return true;
        }
        Achievements.openAchievementsGUI(player);
      }
      case "mobs" -> {
        if (!player.hasPermission("rpgcraft.mobs")) {
          player.sendMessage("§cYou don't have permission to access the mobs editor!");
          return true;
        }
        mobsEditor.openMobDB(player);
      }
      case "quests" -> {
        if (!player.hasPermission("rpgcraft.quests")) {
          player.sendMessage("§cYou don't have permission to access the quests editor!");
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
        player.sendMessage(Colorize("&cUnknown editor: " + editorName));
        player.sendMessage(Colorize("&7Type &6/rpgcraft editor&7 for a list of available editors."));
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
    p.sendMessage(Colorize("&6&l=== RPGCraft Editors ==="));
    p.sendMessage(Colorize("&7Open an editor with: &6/rpgcraft editor <name>"));
    p.sendMessage(Colorize(""));
    p.sendMessage(Colorize("&7Available editors:"));
    p.sendMessage(Colorize("  &fitemdb &8- &7Browse and edit custom items"));
    p.sendMessage(Colorize("  &fconfig &8- &7In-game config menu"));
    p.sendMessage(Colorize("  &fcrafting &8- &7Browse crafting stations"));
    p.sendMessage(Colorize("  &fenchantments &8- &7Browse, create, edit, and delete enchantments"));
    p.sendMessage(Colorize("  &fadvancements &8- &7View achievements"));
    p.sendMessage(Colorize("  &fmobs &8- &7Browse and edit custom mobs"));
    p.sendMessage(Colorize("  &fquests &8- &7View, create, edit, and delete quests"));
    p.sendMessage(Colorize("  &fyamledit &8- &7Toggle YAML click-to-edit mode"));
  }
}
