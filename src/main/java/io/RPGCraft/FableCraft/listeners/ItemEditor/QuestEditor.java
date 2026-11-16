package io.RPGCraft.FableCraft.listeners.ItemEditor;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.Utils.ChatInputManager;
import io.RPGCraft.FableCraft.Utils.GUI.GUI;
import io.RPGCraft.FableCraft.Utils.GUI.GUIItem;
import io.RPGCraft.FableCraft.commands.quest.questManager;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static io.RPGCraft.FableCraft.RPGCraft.*;

/**
 * In-game editor for quests in the quests config file.
 * Allows creating, editing, and deleting quest definitions via GUI.
 *
 * YAML structure:
 *   NPCName:
 *     quest:
 *       questId:
 *         name: ...
 *         order: ...
 *         min-level: ...
 *         steps:
 *           amount: ...
 *           1:
 *             type: kill/get/talkToNPC
 *             ...
 *         reward/rewards: ...
 */
public class QuestEditor {

  // ------------------------------------------------------------------
  // Browser GUI — list all quests grouped by NPC
  // ------------------------------------------------------------------

  public static GUI makeQuestBrowser() {
    GUI gui = new GUI("&6&lQuest Editor", GUI.Rows.SIX);

    YamlConfiguration questsFile = yamlManager.getInstance().getFileConfig("quests");
    if (questsFile == null) return gui;

    // Build NPC -> quest list map
    Map<String, List<String>> npcQuests = new HashMap<>();
    for (String npcKey : questsFile.getKeys(false)) {
      // Skip non-NPC keys
      if (!questsFile.contains(npcKey + ".quest")) continue;
      String questBase = npcKey + ".quest";
      List<String> qIds = new ArrayList<>(questsFile.getConfigurationSection(questBase).getKeys(false));
      if (!qIds.isEmpty()) {
        npcQuests.put(npcKey, qIds);
      }
    }

    int slot = 0;

    // NPC headers + quest items
    for (Map.Entry<String, List<String>> entry : npcQuests.entrySet()) {
      String npc = entry.getKey();

      // NPC header (pane)
      if (slot >= 53) break;
      gui.setItem(slot, new GUIItem(Material.GRAY_STAINED_GLASS_PANE)
        .name("&8── &7" + npc + " &8──")
        .lore("&7NPC: &f" + npc, "&7Quests: &f" + entry.getValue().size()));
      slot++;

      for (String qId : entry.getValue()) {
        if (slot >= 50) break;

        String fullPath = npc + ".quest." + qId;
        String name = questsFile.getString(fullPath + ".name", qId);
        int steps = questsFile.getInt(fullPath + ".steps.amount", 0);
        int minLevel = questsFile.getInt(fullPath + ".min-level", 0);
        int order = questsFile.getInt(fullPath + ".order", 0);

        List<String> lore = new ArrayList<>();
        lore.add("&7ID: &f" + qId);
        lore.add("&7Name: " + name);
        lore.add("&7Steps: &f" + steps);
        if (minLevel > 0) lore.add("&7Min Level: &f" + minLevel);
        if (order > 0) lore.add("&7Order: &f" + order);
        lore.add("");
        lore.add("&eLeft-click to edit");
        lore.add("&cRight-click to delete");

        final String fNpc = npc;
        final String fQId = qId;
        gui.setItem(slot, new GUIItem(Material.WRITABLE_BOOK)
          .name(name)
          .lore(lore)
          .clickEvent(ce -> {
            if (ce.isLeftClick()) {
              openQuestEditor(ce.player(), fNpc, fQId);
            } else if (ce.isRightClick()) {
              deleteQuest(ce.player(), fNpc, fQId);
            }
          }));
        slot++;
      }
    }

    // Create new quest button
    gui.setItem(45, new GUIItem(Material.LIME_DYE)
      .name("&a&lCreate New Quest")
      .lore("&7Click to create a new quest under an NPC")
      .clickEvent(ce -> createNewQuest(ce.player())));

    // Reload button
    gui.setItem(49, new GUIItem(Material.BOOK)
      .name("&e&lReload Quests")
      .lore("&7Reload quests from disk")
      .clickEvent(ce -> {
        yamlManager.getInstance().setEditing(true);
        yamlManager.getInstance().loadData();
        questManager.reloadQuests();
        ce.player().sendMessage(Colorize("&aQuests reloaded!"));
        ce.player().closeInventory();
        makeQuestBrowser().open(ce.player());
      }));

    // Close button
    gui.setItem(53, new GUIItem(Material.BARRIER)
      .name("&cClose Menu")
      .clickEvent(ce -> ce.player().closeInventory()));

    return gui;
  }

  // ------------------------------------------------------------------
  // Editor GUI — edit a single quest
  // ------------------------------------------------------------------

  public static void openQuestEditor(Player p, String npc, String qId) {
    YamlConfiguration file = yamlManager.getInstance().getFileConfig("quests");
    if (file == null) {
      p.sendMessage(Colorize("&cQuests file not found!"));
      return;
    }

    String base = npc + ".quest." + qId;
    if (!file.contains(base + ".name") && !file.contains(base + ".steps")) {
      p.sendMessage(Colorize("&cQuest not found!"));
      return;
    }

    String name = file.getString(base + ".name", qId);
    GUI gui = new GUI("&eEdit: " + name, GUI.Rows.SIX);

    // Preview
    int steps = file.getInt(base + ".steps.amount", 0);
    int minLevel = file.getInt(base + ".min-level", 0);
    int order = file.getInt(base + ".order", 0);

    List<String> previewLore = new ArrayList<>();
    previewLore.add("&7NPC: &f" + npc);
    previewLore.add("&7ID: &f" + qId);
    previewLore.add("&7Steps: &f" + steps);
    if (minLevel > 0) previewLore.add("&7Min Level: &f" + minLevel);
    if (order > 0) previewLore.add("&7Order: &f" + order);

    gui.setItem(4, new GUIItem(Material.WRITABLE_BOOK)
      .name(name)
      .lore(previewLore));

    // Row 1: Basic properties
    gui.setItem(9, button("&aDisplay Name", Material.NAME_TAG, "Rename the quest.",
      player -> startInput(player, "Enter the quest name (color codes allowed):", npc, qId, "name",
        (pl, n, q, in) -> setAndReopen(pl, n, q, "name", deMM(in), "Display name"))));

    gui.setItem(10, button("&7NPC", Material.PLAYER_HEAD, "Change which NPC offers this quest.",
      player -> startInput(player, "Enter the NPC name (no dots):", npc, qId, "npc",
        (pl, n, q, in) -> renameQuestNpc(pl, n, q, in))));

    gui.setItem(11, button("&bOrder", Material.CLOCK, "Set the quest order (lower = first).",
      player -> startInput(player, "Enter the order number (0 = file order):", npc, qId, "order",
        (pl, n, q, in) -> {
          try {
            int val = parseInt(plaintext(in).trim(), 0);
            setAndReopen(pl, n, q, "order", val, "Order");
          } catch (NumberFormatException e) {
            pl.sendMessage(Colorize("&cInvalid number!"));
          }
        })));

    gui.setItem(12, button("&6Min Level", Material.EXPERIENCE_BOTTLE, "Minimum player level to start.",
      player -> startInput(player, "Enter the minimum level (0 = no requirement):", npc, qId, "min-level",
        (pl, n, q, in) -> {
          try {
            int val = parseInt(plaintext(in).trim(), 0);
            setAndReopen(pl, n, q, "min-level", Math.max(0, val), "Min level");
          } catch (NumberFormatException e) {
            pl.sendMessage(Colorize("&cInvalid number!"));
          }
        })));

    gui.setItem(13, button("&eSteps Amount", Material.KNOWLEDGE_BOOK, "Total number of steps.",
      player -> startInput(player, "Enter the total number of steps (1-20):", npc, qId, "steps.amount",
        (pl, n, q, in) -> {
          try {
            int val = parseInt(plaintext(in).trim(), 0);
            val = Math.max(1, Math.min(20, val));
            setAndReopen(pl, n, q, "steps.amount", val, "Steps amount");
          } catch (NumberFormatException e) {
            pl.sendMessage(Colorize("&cInvalid number!"));
          }
        })));

    // Row 2: Step editing
    gui.setItem(18, button("&aEdit Step 1", Material.PAPER, "Edit step 1 properties.",
      player -> openStepEditor(player, npc, qId, 1)));
    gui.setItem(19, button("&aEdit Step 2", Material.PAPER, "Edit step 2 properties.",
      player -> openStepEditor(player, npc, qId, 2)));
    gui.setItem(20, button("&aEdit Step 3", Material.PAPER, "Edit step 3 properties.",
      player -> openStepEditor(player, npc, qId, 3)));
    gui.setItem(21, button("&aEdit Step 4", Material.PAPER, "Edit step 4 properties.",
      player -> openStepEditor(player, npc, qId, 4)));
    gui.setItem(22, button("&aEdit Step 5", Material.PAPER, "Edit step 5 properties.",
      player -> openStepEditor(player, npc, qId, 5)));

    // Row 3: Rewards
    gui.setItem(27, button("&6Rewards", Material.CHEST, "Set quest completion rewards.",
      player -> startListInput(player, npc, qId, "reward", "Rewards",
        "Enter #ADD <reward>, #REMOVE <n>, #SET <n> <reward>, or #CLEAR",
        "Enter rewards as ID:amount (e.g. DIAMOND:5, money:100)")));

    // Row 4: Location (quest-level default)
    gui.setItem(36, button("&3Quest Location", Material.MAP, "Default location for steps without one.",
      player -> startInput(player, "Enter location: world x y z", npc, qId, "location",
        (pl, n, q, in) -> setLocation(pl, n, q, in, "location", "Quest location"))));

    // Back button
    gui.setItem(45, new GUIItem(Material.ARROW)
      .name("&e&lBack")
      .lore("&7Return to quest list")
      .clickEvent(ce -> makeQuestBrowser().open(ce.player())));

    // Close button
    gui.setItem(53, new GUIItem(Material.BARRIER)
      .name("&cClose Menu")
      .clickEvent(ce -> ce.player().closeInventory()));

    gui.open(p);
  }

  // ------------------------------------------------------------------
  // Step Editor GUI
  // ------------------------------------------------------------------

  public static void openStepEditor(Player p, String npc, String qId, int step) {
    YamlConfiguration file = yamlManager.getInstance().getFileConfig("quests");
    if (file == null) return;

    String base = npc + ".quest." + qId;
    int totalSteps = file.getInt(base + ".steps.amount", 0);

    if (step < 1 || step > totalSteps) {
      p.sendMessage(Colorize("&cStep " + step + " does not exist. Steps amount: " + totalSteps));
      return;
    }

    String stepBase = base + ".steps." + step;
    String type = file.getString(stepBase + ".type", "none");

    GUI gui = new GUI("&eStep " + step + " (" + type + ")", GUI.Rows.FIVE);

    // Preview
    List<String> previewLore = new ArrayList<>();
    previewLore.add("&7Type: &f" + type);
    previewLore.add("&7Value: &f" + file.getString(stepBase + ".value", "not set"));

    String entity = file.getString(stepBase + ".entity");
    if (entity != null) previewLore.add("&7Entity: &f" + entity);
    String item = file.getString(stepBase + ".item");
    if (item != null) previewLore.add("&7Item: &f" + item);
    String npcName = file.getString(stepBase + ".NPCName");
    if (npcName != null) previewLore.add("&7NPC: &f" + npcName);

    gui.setItem(4, new GUIItem(Material.PAPER)
      .name("&eStep " + step)
      .lore(previewLore));

    // Row 1: Step type and value
    gui.setItem(9, button("&aStep Type", Material.KNOWLEDGE_BOOK, "kill, get, or talkToNPC.",
      player -> startInput(player, "Enter step type: kill, get, or talkToNPC:", npc, qId, "steps." + step + ".type",
        (pl, n, q, in) -> {
          String t = plaintext(in).trim().toLowerCase();
          if (!t.equals("kill") && !t.equals("get") && !t.equals("talktonpc")) {
            pl.sendMessage(Colorize("&cUse 'kill', 'get', or 'talkToNPC'!"));
            return;
          }
          setAndReopen(pl, n, q, "steps." + step + ".type", t, "Step type");
        })));

    gui.setItem(10, button("&bValue", Material.TOTEM_OF_UNDYING, "Required amount for this step.",
      player -> startInput(player, "Enter the required value (amount):", npc, qId, "steps." + step + ".value",
        (pl, n, q, in) -> {
          try {
            int val = parseInt(plaintext(in).trim(), 0);
            setAndReopen(pl, n, q, "steps." + step + ".value", Math.max(0, val), "Value");
          } catch (NumberFormatException e) {
            pl.sendMessage(Colorize("&cInvalid number!"));
          }
        })));

    // Row 2: Type-specific fields
    gui.setItem(18, button("&cEntity", Material.SPIDER_EYE, "Entity to kill (EntityType or NPC name).",
      player -> startInput(player, "Enter entity type (e.g. spider, zombie) or NPC name:", npc, qId, "steps." + step + ".entity",
        (pl, n, q, in) -> setAndReopen(pl, n, q, "steps." + step + ".entity", plaintext(in).trim(), "Entity"))));

    gui.setItem(19, button("&fItem", Material.IRON_INGOT, "Item to collect (material name).",
      player -> startInput(player, "Enter item material (e.g. STRING, IRON_INGOT):", npc, qId, "steps." + step + ".item",
        (pl, n, q, in) -> setAndReopen(pl, n, q, "steps." + step + ".item", plaintext(in).trim().toUpperCase(), "Item"))));

    gui.setItem(20, button("&7NPC Name", Material.PLAYER_HEAD, "NPC to talk to.",
      player -> startInput(player, "Enter the NPC name:", npc, qId, "steps." + step + ".NPCName",
        (pl, n, q, in) -> setAndReopen(pl, n, q, "steps." + step + ".NPCName", plaintext(in).trim(), "NPC Name"))));

    // Row 3: Actions (for talkToNPC steps)
    gui.setItem(27, button("&eDialogue", Material.BOOK, "Talk lines for this step.",
      player -> startListInput(player, npc, qId, "steps." + step + ".actions.talk", "Dialogue",
        "Enter #ADD <line>, #REMOVE <n>, #SET <n> <line>, or #CLEAR",
        "Dialogue lines with color codes (e.g. &e[NPC] &fHello!)")));

    gui.setItem(28, button("&cRemove Items", Material.BARRIER, "Items taken from player.",
      player -> startListInput(player, npc, qId, "steps." + step + ".actions.removeItems", "Remove Items",
        "Enter #ADD <ID:amount>, #REMOVE <n>, #SET <n> <ID:amount>, or #CLEAR",
        "Items taken from player (e.g. STRING:10)")));

    gui.setItem(29, button("&aGive Items", Material.CHEST, "Items given to player.",
      player -> startListInput(player, npc, qId, "steps." + step + ".actions.giveItems", "Give Items",
        "Enter #ADD <ID:amount>, #REMOVE <n>, #SET <n> <ID:amount>, or #CLEAR",
        "Items given to player (e.g. DIAMOND:1, money:100)")));

    // Row 4: Step location
    gui.setItem(36, button("&3Step Location", Material.MAP, "Location for this step.",
      player -> startInput(player, "Enter location: world x y z (or 'remove'):", npc, qId, "steps." + step + ".location",
        (pl, n, q, in) -> setLocation(pl, n, q, in, "steps." + step + ".location", "Step location"))));

    // Back button
    gui.setItem(39, new GUIItem(Material.ARROW)
      .name("&e&lBack")
      .lore("&7Return to quest editor")
      .clickEvent(ce -> openQuestEditor(ce.player(), npc, qId)));

    // Close button
    gui.setItem(44, new GUIItem(Material.BARRIER)
      .name("&cClose Menu")
      .clickEvent(ce -> ce.player().closeInventory()));

    gui.open(p);
  }

  // ------------------------------------------------------------------
  // Create new quest
  // ------------------------------------------------------------------

  private static void createNewQuest(Player p) {
    p.closeInventory();
    p.sendMessage(Colorize("&7Enter the NPC name that will offer this quest (no dots):"));
    ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
      String npc = plaintext(msg).trim();
      if (npc.isEmpty() || npc.contains(".")) {
        pl.sendMessage(Colorize("&cInvalid NPC name. Use only letters, numbers, '_' and '-'."));
        return;
      }

      // Normalize NPC name (capitalize first letter for consistency)
      YamlConfiguration file = yamlManager.getInstance().getFileConfig("quests");

      // Check if NPC exists; if not, create the section
      if (!file.contains(npc + ".quest")) {
        yamlManager.getInstance().setEditing(true);
        file.set(npc + ".quest", new Object());
      }

      pl.sendMessage(Colorize("&7Enter the quest ID (letters, numbers, underscores, hyphens only):"));
      ChatInputManager.waitForNextMessage(pl, (player, qMsg) -> {
        String qId = plaintext(qMsg).trim();
        if (qId.isEmpty() || !qId.matches("[A-Za-z0-9_\\-]+")) {
          player.sendMessage(Colorize("&cInvalid ID. Use only letters, numbers, '_' and '-'."));
          return;
        }

        // Check for duplicates
        if (file.contains(npc + ".quest." + qId + ".name")) {
          player.sendMessage(Colorize("&cA quest with that ID already exists under this NPC."));
          return;
        }

        yamlManager.getInstance().setEditing(true);
        String base = npc + ".quest." + qId;
        file.set(base + ".name", "&f" + qId);
        file.set(base + ".steps.amount", 1);
        file.set(base + ".steps.1.type", "talkToNPC");
        file.set(base + ".steps.1.NPCName", npc);
        file.set(base + ".steps.1.actions.talk", List.of("&e[" + npc + "] &fWelcome!"));
        yamlManager.getInstance().saveData();
        questManager.reloadQuests();

        player.sendMessage(Colorize("&aQuest created! Opening editor..."));
        RPGCraft.wait(1, new BukkitRunnable() {
          @Override
          public void run() {
            openQuestEditor(player, npc, qId);
          }
        });
      });
    });
  }

  // ------------------------------------------------------------------
  // Delete quest
  // ------------------------------------------------------------------

  private static void deleteQuest(Player p, String npc, String qId) {
    p.sendMessage(Colorize("&cAre you sure you want to delete quest '" + qId + "' under NPC '" + npc + "'?"));
    p.sendMessage(Colorize("&7Type &ayes &7to confirm."));
    ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
      if (plaintext(msg).trim().equalsIgnoreCase("yes")) {
        yamlManager.getInstance().setEditing(true);
        YamlConfiguration file = yamlManager.getInstance().getFileConfig("quests");
        file.set(npc + ".quest." + qId, null);
        yamlManager.getInstance().saveData();
        questManager.reloadQuests();
        pl.sendMessage(Colorize("&aQuest '" + qId + "' deleted!"));
        makeQuestBrowser().open(pl);
      } else {
        pl.sendMessage(Colorize("&7Deletion cancelled."));
        makeQuestBrowser().open(pl);
      }
    });
  }

  // ------------------------------------------------------------------
  // Rename quest NPC (move quest to a different NPC)
  // ------------------------------------------------------------------

  private static void renameQuestNpc(Player p, String oldNpc, String qId, Component input) {
    String newNpc = plaintext(input).trim();
    if (newNpc.isEmpty() || newNpc.contains(".")) {
      p.sendMessage(Colorize("&cInvalid NPC name!"));
      return;
    }

    YamlConfiguration file = yamlManager.getInstance().getFileConfig("quests");
    String oldBase = oldNpc + ".quest." + qId;
    String newBase = newNpc + ".quest." + qId;

    // Check for conflicts
    if (file.contains(newBase + ".name")) {
      p.sendMessage(Colorize("&cA quest with that ID already exists under NPC '" + newNpc + "'!"));
      return;
    }

    // Move all data
    yamlManager.getInstance().setEditing(true);
    Object questData = file.get(oldBase);
    file.set(oldBase, null);
    if (!file.contains(newNpc + ".quest")) {
      file.set(newNpc + ".quest", new Object());
    }
    file.set(newBase, questData);
    yamlManager.getInstance().saveData();
    questManager.reloadQuests();

    p.sendMessage(Colorize("&aQuest moved to NPC '" + newNpc + "'!"));
    RPGCraft.wait(1, new BukkitRunnable() {
      @Override
      public void run() {
        openQuestEditor(p, newNpc, qId);
      }
    });
  }

  // ------------------------------------------------------------------
  // Set location helper
  // ------------------------------------------------------------------

  private static void setLocation(Player p, String npc, String qId, Component input, String path, String label) {
    String raw = plaintext(input).trim();
    if (raw.equalsIgnoreCase("remove")) {
      yamlManager.getInstance().setEditing(true);
      YamlConfiguration file = yamlManager.getInstance().getFileConfig("quests");
      String base = npc + ".quest." + qId;
      file.set(base + "." + path, null);
      yamlManager.getInstance().saveData();
      p.sendMessage(Colorize("&a" + label + " removed!"));
      reopenLater(p, npc, qId);
      return;
    }

    String[] parts = raw.split("\\s+");
    if (parts.length != 4) {
      p.sendMessage(Colorize("&cFormat: world x y z"));
      return;
    }

    try {
      double x = Double.parseDouble(parts[1]);
      double y = Double.parseDouble(parts[2]);
      double z = Double.parseDouble(parts[3]);

      yamlManager.getInstance().setEditing(true);
      YamlConfiguration file = yamlManager.getInstance().getFileConfig("quests");
      String base = npc + ".quest." + qId;
      file.set(base + "." + path + ".world", parts[0]);
      file.set(base + "." + path + ".x", x);
      file.set(base + "." + path + ".y", y);
      file.set(base + "." + path + ".z", z);
      yamlManager.getInstance().saveData();
      p.sendMessage(Colorize("&a" + label + " set!"));
      reopenLater(p, npc, qId);
    } catch (NumberFormatException e) {
      p.sendMessage(Colorize("&cInvalid coordinates!"));
    }
  }

  // ------------------------------------------------------------------
  // Input helpers
  // ------------------------------------------------------------------

  @FunctionalInterface
  interface QuestInputHandler {
    void handle(Player p, String npc, String qId, Component input);
  }

  private static void startInput(Player p, String prompt, String npc, String qId, String field, QuestInputHandler handler) {
    p.closeInventory();
    p.sendMessage(Colorize(prompt));

    YamlConfiguration file = yamlManager.getInstance().getFileConfig("quests");
    if (file != null) {
      String base = npc + ".quest." + qId;
      Object val = file.get(base + "." + field);
      if (val != null) {
        p.sendMessage(Colorize("&7Current: &f" + val + "   (type cancel to go back)"));
      }
    }

    final String fNpc = npc;
    final String fQId = qId;
    ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
      if (plaintext(msg).trim().equalsIgnoreCase("cancel")) {
        reopenLater(pl, fNpc, fQId);
        return;
      }
      handler.handle(p, fNpc, fQId, msg);
    });
  }

  private static void setAndReopen(Player p, String npc, String qId, String path, Object value, String label) {
    yamlManager.getInstance().setEditing(true);
    YamlConfiguration file = yamlManager.getInstance().getFileConfig("quests");
    if (file == null) {
      p.sendMessage(Colorize("&cQuests file not found!"));
      return;
    }
    String base = npc + ".quest." + qId;
    file.set(base + "." + path, value);
    yamlManager.getInstance().saveData();
    questManager.reloadQuests();
    p.sendMessage(Colorize("&a" + label + " updated!"));
    reopenLater(p, npc, qId);
  }

  private static void startListInput(Player p, String npc, String qId, String path, String label, String hint, String formatHint) {
    p.closeInventory();
    p.sendMessage(Colorize("&7Editing " + label + " for " + qId + ":"));
    p.sendMessage(Colorize("&7" + hint));
    p.sendMessage(Colorize("&7Format: &f" + formatHint));

    YamlConfiguration file = yamlManager.getInstance().getFileConfig("quests");
    if (file == null) {
      p.sendMessage(Colorize("&cQuests file not found!"));
      return;
    }

    String base = npc + ".quest." + qId;
    List<String> currentList = file.getStringList(base + "." + path);
    if (!currentList.isEmpty()) {
      p.sendMessage(Colorize("&7Current: &f" + String.join(", ", currentList)));
    } else {
      p.sendMessage(Colorize("&7Current: &7(not set)"));
    }
    p.sendMessage(Colorize("&7Type &fcancel &7to go back."));

    final String fNpc = npc;
    final String fQId = qId;
    final YamlConfiguration fFile = file;
    ChatInputManager.waitForNextMessage(p, (pl, msg) -> {
      if (plaintext(msg).trim().equalsIgnoreCase("cancel")) {
        reopenLater(pl, fNpc, fQId);
        return;
      }
      String raw = plaintext(msg).trim();
      String[] parts = raw.split("\\s+", 3);
      String command = parts[0].toUpperCase();

      List<String> list = new ArrayList<>(fFile.getStringList(base + "." + path));

      switch (command) {
        case "#ADD" -> {
          if (parts.length < 2) {
            pl.sendMessage(Colorize("&cUsage: #ADD <value>"));
            return;
          }
          list.add(parts[1].trim());
        }
        case "#REMOVE" -> {
          if (parts.length < 2) {
            pl.sendMessage(Colorize("&cUsage: #REMOVE <n>"));
            return;
          }
          int idx = parseInt(parts[1], -1);
          if (idx < 1 || idx > list.size()) {
            pl.sendMessage(Colorize("&cInvalid index!"));
            return;
          }
          list.remove(idx - 1);
        }
        case "#SET" -> {
          if (parts.length < 3) {
            pl.sendMessage(Colorize("&cUsage: #SET <n> <value>"));
            return;
          }
          int idx = parseInt(parts[1], -1);
          if (idx < 1 || idx > list.size()) {
            pl.sendMessage(Colorize("&cInvalid index!"));
            return;
          }
          list.set(idx - 1, parts[2].trim());
        }
        case "#CLEAR" -> list.clear();
        default -> {
          pl.sendMessage(Colorize("&cUnknown command. Use #ADD, #REMOVE, #SET, or #CLEAR."));
          return;
        }
      }

      yamlManager.getInstance().setEditing(true);
      fFile.set(base + "." + path, list.isEmpty() ? null : list);
      yamlManager.getInstance().saveData();
      questManager.reloadQuests();
      pl.sendMessage(Colorize("&a" + label + " updated!"));
      reopenLater(pl, fNpc, fQId);
    });
  }

  private static GUIItem button(String name, Material mat, String description, Consumer<Player> onClick) {
    return GUIItem.IStoGUIItem(ItemEditor.createButton(name, mat, "&f" + description, "&8Click to edit"))
      .clickEvent(ce -> onClick.accept(ce.player()));
  }

  private static void reopenLater(Player p, String npc, String qId) {
    RPGCraft.wait(1, new BukkitRunnable() {
      @Override
      public void run() {
        openQuestEditor(p, npc, qId);
      }
    });
  }

  private static int parseInt(String s, int fallback) {
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return fallback;
    }
  }
}
