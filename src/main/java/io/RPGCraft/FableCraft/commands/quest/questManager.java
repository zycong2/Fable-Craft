package io.RPGCraft.FableCraft.commands.quest;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.Utils.skript.onQuestComplete;
import io.RPGCraft.FableCraft.Utils.skript.onQuestStart;
import io.RPGCraft.FableCraft.Utils.skript.onQuestUpdate;
import io.RPGCraft.FableCraft.core.Helpers.PDCHelper;
import io.RPGCraft.FableCraft.core.Helpers.lootTableHelper;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.RPGCraft.FableCraft.Utils.money.moneyManager.addMoney;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.getPlayerPDC;
import static io.RPGCraft.FableCraft.core.YAML.yamlGetter.getAllNodesInDB;

public class questManager {
  private static List<String> readList(String key, Player p) {
    List<String> out = new ArrayList<>();
    String raw = getPlayerPDC(key, p);
    if (raw == null) return out;
    for (String s : raw.split(";")) {
      if (!s.isEmpty()) out.add(s);
    }
    return out;
  }

  private static void writeList(String key, Player p, List<String> list) {
    PDCHelper.setPlayerPDC(key, p, list.isEmpty() ? "" : ";" + String.join(";", list));
  }

  public static List<String> getActiveQuests(Player p) {
    return readList("quests", p);
  }

  public static List<String> getCompletedQuests(Player p) {
    return readList("completed_quests", p);
  }

  public static int getStep(Player p, String quest) {
    return toInt(getPlayerPDC(quest + ".step", p), 0);
  }

  static Object stepOption(String quest, int step, String field) {
    return yamlGetter.getPathInDB("quests", quest + ".steps." + step + "." + field);
  }

  static int toInt(Object o, int fallback) {
    if (o instanceof Number n) return n.intValue();
    if (o == null) return fallback;
    try {
      return Integer.parseInt(o.toString().trim());
    } catch (NumberFormatException e) {
      return fallback;
    }
  }

  static String color(String s) {
    return ChatColor.translateAlternateColorCodes('&', s);
  }


  public static void startQuest(Player p, String quest) {
    quest = quest.replace(",", "").replace(";", "").replace(" ", "");

    List<String> active = getActiveQuests(p);
    if (active.contains(quest)) { // exact match, not substring ("quest1" vs "quest10")
      p.sendMessage(yamlGetter.getMessage("messages.error.questAlreadyStarted", null, true));
      return;
    }

    active.add(quest);
    writeList("quests", p, active);
    PDCHelper.setPlayerPDC(quest + ".step", p, "1");
    PDCHelper.setPlayerPDC(quest + ".progress", p, "0");
    p.sendMessage(yamlGetter.getMessage("messages.info.quests.start", p, true));

    Bukkit.getPluginManager().callEvent(new onQuestStart(p, quest));
  }

  public static void advanceStep(Player p, String quest) {
    int next = getStep(p, quest) + 1;
    int total = toInt(yamlGetter.getPathInDB("quests", quest + ".steps.amount"), 0);

    if (next > total) {
      finishedQuest(p, quest);
      return;
    }

    PDCHelper.setPlayerPDC(quest + ".step", p, String.valueOf(next));
    PDCHelper.setPlayerPDC(quest + ".progress", p, "0");

    Location loc = parseLocation(quest + ".steps." + next + ".location");
    Bukkit.getPluginManager().callEvent(new onQuestUpdate(p, quest, loc));
  }

  public static void addProgress(Player p, String quest, int amount) {
    int step = getStep(p, quest);
    int required = toInt(stepOption(quest, step, "value"), 1);
    int progress = toInt(getPlayerPDC(quest + ".progress", p), 0) + amount;

    if (progress >= required) {
      advanceStep(p, quest);
    } else {
      PDCHelper.setPlayerPDC(quest + ".progress", p, String.valueOf(progress));
    }
  }

  public static void finishedQuest(Player p, String quest) {
    p.sendMessage(yamlGetter.getMessage("messages.info.quests.completed", p, true));

    Object rewards = yamlGetter.getPathInDB("quests", quest + ".rewards");
    if (rewards instanceof String table) {
      List<ItemStack> items = lootTableHelper.getLootTable(table);
      if (items != null) {
        for (ItemStack item : items) giveOrDrop(p, item);
      }
    } else if (rewards instanceof List<?> list) {
      for (Object entry : list) giveReward(p, entry.toString());
    }

    List<String> active = getActiveQuests(p);
    active.remove(quest);
    writeList("quests", p, active);

    List<String> done = getCompletedQuests(p);
    if (!done.contains(quest)) {
      done.add(quest);
      writeList("completed_quests", p, done);
    }

    Bukkit.getPluginManager().callEvent(new onQuestComplete(p, quest));
  }


  public static void talkedNPC(Player p, String NPC) {
    List<String> active = getActiveQuests(p);
    boolean handled = false;

    for (String quest : active) {
      int step = getStep(p, quest);
      Object type = stepOption(quest, step, "type");
      Object npcName = stepOption(quest, step, "NPCName");

      if (type == null || npcName == null) continue;
      if (!type.toString().equalsIgnoreCase("talkToNPC")) continue;
      if (!NPC.equalsIgnoreCase(npcName.toString())) continue;

      handled = true;

      if (!hasRequiredItems(p, quest, step)) {
        p.sendMessage(color("&cYou don't have the required items."));
        continue;
      }

      runActions(p, quest, step);
      advanceStep(p, quest);
    }

    if (!handled) {
      tryNewQuest(active, p, NPC);
    }
  }

  private static boolean hasRequiredItems(Player p, String quest, int step) {
    Object value = yamlGetter.getPathInDB("quests", quest + ".steps." + step + ".actions.removeItems");
    if (!(value instanceof List<?> list)) return true;

    for (Object raw : list) {
      Entry e = Entry.parse(raw);
      ItemStack probe = buildStack(e.id(), 1);
      if (probe == null) {
        Bukkit.getLogger().warning("[Quests] Unknown item '" + e.id() + "' in " + quest + " step " + step);
        return false;
      }
      if (!p.getInventory().containsAtLeast(probe, e.amount())) return false;
    }
    return true;
  }

  private static void runActions(Player p, String quest, int step) {
    String base = quest + ".steps." + step + ".actions";
    var nodes = getAllNodesInDB("quests", base);
    if (nodes == null) return;

    int talkDelay = toInt(yamlGetter.getConfig("quests.npc.talk.delay"), 20);
    int delay = 0;

    for (Object node : nodes) {
      Object value = yamlGetter.getPathInDB("quests", base + "." + node);
      if (!(value instanceof List<?> list)) continue;

      switch (node.toString()) {
        case "removeItems" -> {
          for (Object raw : list) {
            Entry e = Entry.parse(raw);
            ItemStack stack = buildStack(e.id(), e.amount());
            if (stack != null) p.getInventory().removeItem(stack);
          }
        }
        case "talk" -> {
          for (Object line : list) {
            String text = color(line.toString());
            Bukkit.getScheduler().runTaskLater(RPGCraft.getPlugin(), () -> p.sendMessage(text), delay);
            delay += talkDelay;
          }
        }
        case "giveItems" -> {
          for (Object raw : list) {
            Bukkit.getScheduler().runTaskLater(RPGCraft.getPlugin(), () -> giveReward(p, raw.toString()), delay);
          }
        }
        default -> { }
      }
    }
  }

  static void tryNewQuest(List<String> activeQuests, Player p, String NPC) {
    List<String> done = getCompletedQuests(p);
    var all = getAllNodesInDB("quests", "");
    if (all == null) return;

    for (Object node : all) {
      for (String id : node.toString().replace("[", "").replace("]", "").split(", ")) {
        if (id.isEmpty() || activeQuests.contains(id) || done.contains(id)) continue;

        Object starter = yamlGetter.getPathInDB("quests", id + ".npcStarter");
        if (starter != null && starter.toString().equalsIgnoreCase(NPC)) {
          startQuest(p, id);
        }
      }
    }
  }

  // ------------------------------------------------------------------
  // Items / rewards
  // ----------------------------------------------------------------

  private record Entry(String id, int amount) {
    static Entry parse(Object raw) {
      String[] parts = raw.toString().replace("[", "").replace("]", "").trim().split(":");
      int amount = 1;
      if (parts.length > 1) {
        try {
          amount = Math.max(1, Integer.parseInt(parts[1].trim()));
        } catch (NumberFormatException ignored) { }
      }
      return new Entry(parts[0].trim(), amount);
    }
  }

  private static ItemStack buildStack(String id, int amount) {
    Material mat = Material.matchMaterial(id);
    if (mat != null) return new ItemStack(mat, amount);

    ItemStack custom = yamlManager.getItem(id);
    if (custom == null) return null;

    ItemStack copy = custom.clone();
    copy.setAmount(amount);
    return copy;
  }

  static void giveReward(Player p, String raw) {
    Entry e = Entry.parse(raw);

    if (e.id().equalsIgnoreCase("money")) {
      addMoney(p, e.amount());
      p.sendMessage(color("&aYou have received " + e.amount() + " money!"));
      return;
    }

    ItemStack stack = buildStack(e.id(), e.amount());
    if (stack == null) {
      Bukkit.getLogger().warning("[Quests] Unknown reward '" + e.id() + "'");
      return;
    }
    giveOrDrop(p, stack);
  }

  static void giveOrDrop(Player p, ItemStack item) {
    Map<Integer, ItemStack> leftover = p.getInventory().addItem(item);
    for (ItemStack rest : leftover.values()) {
      p.getWorld().dropItemNaturally(p.getLocation(), rest);
    }
  }

  static Location parseLocation(String path) {
    Object world = yamlGetter.getPathInDB("quests", path + ".world");
    Double x = num(yamlGetter.getPathInDB("quests", path + ".x"));
    Double y = num(yamlGetter.getPathInDB("quests", path + ".y"));
    Double z = num(yamlGetter.getPathInDB("quests", path + ".z"));
    if (world == null || x == null || y == null || z == null) return null;

    World w = Bukkit.getWorld(world.toString());
    if (w == null) return null;
    return new Location(w, x, y, z);
  }

  private static Double num(Object o) {
    return o instanceof Number n ? Double.valueOf(n.doubleValue()) : null;
  }
}

