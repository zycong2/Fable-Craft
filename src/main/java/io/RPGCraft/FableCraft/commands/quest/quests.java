package io.RPGCraft.FableCraft.commands.quest;

import io.RPGCraft.FableCraft.Utils.GUI.GUI;
import io.RPGCraft.FableCraft.Utils.GUI.GUIItem;
import io.RPGCraft.FableCraft.Utils.commandHelper.CommandInterface;
import io.RPGCraft.FableCraft.core.Helpers.PDCHelper;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;
import static io.RPGCraft.FableCraft.RPGCraft.MM;
import static io.RPGCraft.FableCraft.commands.quest.questManager.*;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.getPlayerPDC;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.setPlayerPDC;

public class quests implements CommandInterface, Listener {

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (!(sender instanceof Player p)) {
      sender.sendMessage(yamlGetter.getMessage("messages.general.onlyPlayers", null, true));
      return true;
    }

    if (args.length == 0) {
      // Default: open the quests GUI
      if (p.hasPermission("rpgcraft.quests")) {
        openQuestsGUI(p);
      } else {
        p.sendMessage(yamlGetter.getMessage("messages.error.noPermission", p, true));
      }
      return true;
    }

    switch (args[0]) {
      case "gui" -> {
        if (p.hasPermission("rpgcraft.quests")) {
          openQuestsGUI(p);
        } else {
          p.sendMessage(yamlGetter.getMessage("messages.error.noPermission", p, true));
        }
      }
      case "startNew" -> {
        if (!p.hasPermission("rpgcraft.quests")) {
          p.sendMessage(yamlGetter.getMessage("messages.error.noPermission", p, true));
          return true;
        }
        if (args.length < 2) {
          p.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", null, true));
          return true;
        }
        startQuest(p, args[1]);
      }
      case "disband" -> {
        if (!p.hasPermission("rpgcraft.quests")) {
          p.sendMessage(yamlGetter.getMessage("messages.error.noPermission", p, true));
          return true;
        }
        if (args.length < 2) {
          p.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", null, true));
          return true;
        }
        String activeQuests = getPlayerPDC("quests", p);
        if (activeQuests != null && activeQuests.contains(args[1])) {
          PDCHelper.setPlayerPDC("quests", p, activeQuests.replace(";" + args[1], ""));
          PDCHelper.setPlayerPDC(args[1] + ".step", p, null);
          PDCHelper.setPlayerPDC(args[1] + ".progress", p, null);
          p.sendMessage(yamlGetter.getMessage("messages.info.quests.disband", p, true));
        } else {
          p.sendMessage(yamlGetter.getMessage("messages.error.questNotStarted", null, true));
        }
      }
      case "resetAllPlayersQuests" -> {
        if (!p.hasPermission("rpgcraft.resetQuests")) {
          p.sendMessage(yamlGetter.getMessage("messages.error.noPermission", p, true));
          return true;
        }
        for (Player pla : Bukkit.getOnlinePlayers()) {
          setPlayerPDC("quests", pla, "");
          setPlayerPDC("completed_quests", pla, "");
        }
        p.sendMessage(yamlGetter.getMessage("messages.quests.resetAll", p, true));
      }
      default -> p.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", null, true));
    }

    return true;
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (!(sender instanceof Player p)) return List.of();

    if (args.length == 1) {
      List<String> subs = new ArrayList<>(List.of("gui", "startNew", "disband"));
      if (p.hasPermission("rpgcraft.resetQuests")) {
        subs.add("resetAllPlayersQuests");
      }
      return subs;
    } else if (args.length == 2) {
      if (args[0].equalsIgnoreCase("startNew")) {
        List<String> arguments = new ArrayList<>();
        for (Object o : yamlGetter.getAllNodesInDB("quests", "")) {
          if (o instanceof String st) arguments.add(st);
        }
        return arguments;
      } else if (args[0].equalsIgnoreCase("disband")) {
        String activeQuests = getPlayerPDC("quests", p);
        if (activeQuests == null) return List.of();
        return List.of(activeQuests.split(";"));
      }
    }
    return List.of();
  }

  // ------------------------------------------------------------------
  // GUI — Quests browser
  // ------------------------------------------------------------------

  /** Opens a GUI showing only active quests with scalable size. */
  public static void openQuestsGUI(Player p) {
    List<String> active = getActiveQuests(p);
    List<String> completed = getCompletedQuests(p);

    // Calculate inventory size based on active quests
    // Minimum 2 rows: bottom row is always the button bar, quests go above
    int questRows = (int) Math.ceil((double) active.size() / 9);
    int rowCount = Math.max(2, questRows + 1); // +1 for the button bar row
    if (rowCount > 6) rowCount = 6;
    GUI.Rows rows;
    if (rowCount == 2) rows = GUI.Rows.TWO;
    else if (rowCount == 3) rows = GUI.Rows.THREE;
    else if (rowCount == 4) rows = GUI.Rows.FOUR;
    else if (rowCount == 5) rows = GUI.Rows.FIVE;
    else rows = GUI.Rows.SIX;

    GUI gui = new GUI("&6&lActive Quests", rows);
    int totalSlots = rows.getValue() * 9;

    int slot = 0;
    for (String questId : active) {
      String name = (String) questOption(questId, "name");
      if (name == null) name = questId;

      int step = getStep(p, questId);
      int total = toInt(questOption(questId, "steps.amount"), 0);
      int progress = toInt(getPlayerPDC(questId + ".progress", p), 0);
      int required = toInt(stepOption(questId, step, "value"), 0);

      List<String> lore = new ArrayList<>();
      lore.add(Colorize(name));
      lore.add("");
      lore.add("&a&lACTIVE");
      lore.add("&7Step: &f" + step + "/" + total);
      if (required > 0) {
        lore.add("&7Progress: &f" + progress + "/" + required);
      }
      lore.add("");
      lore.add("&7Click to disband this quest");

      final String fqId = questId;
      gui.setItem(slot, new GUIItem(Material.EMERALD)
        .name(lore.remove(0))
        .lore(lore)
        .clickEvent(ce -> {
          // Disband the quest
          String activeQuests = getPlayerPDC("quests", ce.player());
          if (activeQuests != null) {
            PDCHelper.setPlayerPDC("quests", ce.player(), activeQuests.replace(";" + fqId, ""));
          }
          PDCHelper.setPlayerPDC(fqId + ".step", ce.player(), null);
          PDCHelper.setPlayerPDC(fqId + ".progress", ce.player(), null);
          ce.player().closeInventory();
          openQuestsGUI(ce.player());
        }));
      slot++;
    }

    // Completed quests button (bottom left)
    int completedButtonSlot = totalSlots - 9; // First slot of bottom row
    gui.setItem(completedButtonSlot, new GUIItem(Material.LIME_DYE)
      .name("&a&lCompleted Quests")
      .lore("&7View your completed quests",
            "&7Completed: &6" + completed.size())
      .clickEvent(ce -> {
        ce.player().closeInventory();
        openCompletedQuestsGUI(ce.player());
      }));

    // Close button (bottom right)
    gui.setItem(totalSlots - 1, new GUIItem(Material.BARRIER)
      .name("&cClose Menu")
      .clickEvent(ce -> ce.player().closeInventory()));

    gui.open(p);
  }

  /** Opens a GUI showing completed quests. */
  public static void openCompletedQuestsGUI(Player p) {
    List<String> completed = getCompletedQuests(p);

    // Calculate inventory size based on completed quests
    // Minimum 2 rows: bottom row is always the button bar, quests go above
    int questRows = (int) Math.ceil((double) completed.size() / 9);
    int rowCount = Math.max(2, questRows + 1); // +1 for the button bar row
    if (rowCount > 6) rowCount = 6;
    GUI.Rows rows;
    if (rowCount == 2) rows = GUI.Rows.TWO;
    else if (rowCount == 3) rows = GUI.Rows.THREE;
    else if (rowCount == 4) rows = GUI.Rows.FOUR;
    else if (rowCount == 5) rows = GUI.Rows.FIVE;
    else rows = GUI.Rows.SIX;

    GUI gui = new GUI("&a&lCompleted Quests", rows);
    int totalSlots = rows.getValue() * 9;

    int slot = 0;
    for (String questId : completed) {
      String name = (String) questOption(questId, "name");
      if (name == null) name = questId;

      List<String> lore = new ArrayList<>();
      lore.add(Colorize(name));
      lore.add("");
      lore.add("&a&lCOMPLETED");

      gui.setItem(slot, new GUIItem(Material.LIME_DYE)
        .name(lore.remove(0))
        .lore(lore));
      slot++;
    }

    // Back button (bottom left)
    int backButtonSlot = totalSlots - 9;
    gui.setItem(backButtonSlot, new GUIItem(Material.ARROW)
      .name("&eBack to Active Quests")
      .clickEvent(ce -> {
        ce.player().closeInventory();
        openQuestsGUI(ce.player());
      }));

    // Close button (bottom right)
    gui.setItem(totalSlots - 1, new GUIItem(Material.BARRIER)
      .name("&cClose Menu")
      .clickEvent(ce -> ce.player().closeInventory()));

    gui.open(p);
  }
}
