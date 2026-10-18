package io.RPGCraft.FableCraft.commands.quest;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.Utils.skript.onQuestComplete;
import io.RPGCraft.FableCraft.Utils.skript.onQuestStart;
import io.RPGCraft.FableCraft.Utils.skript.onQuestUpdate;
import io.RPGCraft.FableCraft.core.Helpers.PDCHelper;
import io.RPGCraft.FableCraft.core.Helpers.lootTableHelper;
import io.RPGCraft.FableCraft.core.Stats.Stats;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static io.RPGCraft.FableCraft.Utils.money.moneyManager.addMoney;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.getPlayerPDC;
import static io.RPGCraft.FableCraft.core.YAML.yamlGetter.getAllNodesInDB;

/**
 * YAML layout (quests file):
 *
 * <pre>
 * John:                      <- NPC that offers the quests (no dots in the name)
 *   quest:
 *     quest1:                <- quest id, must be unique across ALL npcs
 *       name: ...
 *       order: 1             <- optional, default = position in the file
 *       steps: ...
 *       reward: ...
 *     quest2:                <- starts when John is clicked after quest1 is completed
 *       ...
 * </pre>
 */
public class questManager {

  /** The node between the NPC name and the quest id: npc.quest.questId */
  private static final String QUEST_NODE = "quest";

  // ------------------------------------------------------------------
  // Quest lookup: questId -> "NpcName.quest.questId"
  // ------------------------------------------------------------------

  private static volatile Map<String, String> questIndex = null;
  private static volatile long lastIndexBuild = 0;
  private static final long INDEX_REBUILD_COOLDOWN_MS = 2000;

  /** Lower-case, no ',' ';' or spaces. Player data (PDC keys) can't contain uppercase. */
  static String normalizeId(String id) {
    return id.replace(",", "").replace(";", "").replace(" ", "").toLowerCase(Locale.ROOT);
  }

  /** Call this after reloading the quests file so new/removed quests are picked up. */
  public static void reloadQuests() {
    questIndex = null;
  }

  /**
   * Direct children of a path in the quests file.
   * Works whether getAllNodesInDB returns plain keys or full paths.
   */
  private static List<String> childKeys(String path) {
    List<String> keys = new ArrayList<>();
    var all = getAllNodesInDB("quests", path);
    if (all == null) return keys;

    for (Object node : all) {
      for (String raw : node.toString().replace("[", "").replace("]", "").split(", ")) {
        String key = raw.trim();
        if (key.isEmpty()) continue;
        if (!path.isEmpty() && key.startsWith(path + ".")) key = key.substring(path.length() + 1);
        int dot = key.indexOf('.');
        if (dot > 0) key = key.substring(0, dot);
        if (!key.isEmpty() && !keys.contains(key)) keys.add(key);
      }
    }
    return keys;
  }

  private static Map<String, String> buildIndex() {
    Map<String, String> index = new HashMap<>();
    for (String npcKey : childKeys("")) {
      for (String id : childKeys(npcKey + "." + QUEST_NODE)) {
        String norm = normalizeId(id);
        String path = npcKey + "." + QUEST_NODE + "." + id;
        if (index.putIfAbsent(norm, path) != null) {
          Bukkit.getLogger().warning("[Quests] Duplicate quest id '" + id + "' under " + npcKey
            + " - quest ids must be unique across all NPCs. Ignoring this one.");
        }
      }
    }
    return index;
  }

  /** "John.quest.quest1" for quest id "quest1", or null if no such quest exists. */
  public static String questPath(String quest) {
    if (quest == null) return null;
    String key = normalizeId(quest);

    Map<String, String> index = questIndex;
    long now = System.currentTimeMillis();
    if (index == null || (!index.containsKey(key) && now - lastIndexBuild > INDEX_REBUILD_COOLDOWN_MS)) {
      index = buildIndex();
      questIndex = index;
      lastIndexBuild = now;
    }
    return index.get(key);
  }

  /** quests.<npc>.quest.<quest>.<field> (e.g. field "name" or "steps.amount"). Null if missing. */
  public static Object questOption(String quest, String field) {
    String root = questPath(quest);
    return root == null ? null : yamlGetter.getPathInDB("quests", root + "." + field);
  }

  /** Case/colour-insensitive lookup of the NPC's section in the quests file. */
  private static String findNpcKey(String npc) {
    if (npc == null) return null;
    String wanted = ChatColor.stripColor(npc).trim();
    for (String key : childKeys("")) {
      if (ChatColor.stripColor(key).trim().equalsIgnoreCase(wanted)) return key;
    }
    return null;
  }

  /** The quest ids an NPC offers, in the order they are handed out. */
  private static List<String> questsOf(String npcKey) {
    String base = npcKey + "." + QUEST_NODE;
    List<String> ids = childKeys(base);

    Map<String, Integer> rank = new HashMap<>();
    for (int i = 0; i < ids.size(); i++) {
      Object order = yamlGetter.getPathInDB("quests", base + "." + ids.get(i) + ".order");
      rank.put(ids.get(i), toInt(order, i + 1));
    }
    ids.sort(Comparator.comparingInt(rank::get)); // stable: ties keep file order
    return ids;
  }

  // ------------------------------------------------------------------
  // State helpers
  // ------------------------------------------------------------------

  /** ";quest1;quest2" -> ["quest1", "quest2"] (never contains empty strings). */
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
    // lowercase on purpose: NamespacedKey rejects uppercase characters
    return readList("completed_quests", p);
  }

  /** Current step as a real int (0 if the player has no step stored). */
  public static int getStep(Player p, String quest) {
    return toInt(getPlayerPDC(quest + ".step", p), 0);
  }

  /** quests.<npc>.quest.<quest>.steps.<step>.<field> from the quests YAML (null if missing). */
  static Object stepOption(String quest, int step, String field) {
    return questOption(quest, "steps." + step + "." + field);
  }

  /**
   * ...steps.<step>.actions.<key> as a list.
   * YAML allows both "removeItems: STRING:10" and a "- STRING:10" list - both work.
   */
  private static List<Object> actionList(String quest, int step, String key) {
    Object value = stepOption(quest, step, "actions." + key);
    if (value == null) return List.of();
    if (value instanceof List<?> list) return new ArrayList<Object>(list);
    return List.of(value);
  }

  /** YAML numbers arrive as Integer, PDC values as String - handle both. */
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

  /** Flip to false once quests behave. */
  static final boolean DEBUG = true;

  static void debug(String msg) {
    if (DEBUG) Bukkit.getLogger().info("[Quests] " + msg);
  }

  // ------------------------------------------------------------------
  // Start / progress / finish
  // ------------------------------------------------------------------

  public static void startQuest(Player p, String quest) {
    quest = normalizeId(quest);

    if (questPath(quest) == null) {
      Bukkit.getLogger().warning("[Quests] Tried to start unknown quest '" + quest + "'");
      return;
    }

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

  /** Move to the next step, or finish the quest if that was the last one. */
  public static void advanceStep(Player p, String quest) {
    int next = getStep(p, quest) + 1;
    int total = toInt(questOption(quest, "steps.amount"), 0);

    if (next > total) {
      finishedQuest(p, quest);
      return;
    }

    PDCHelper.setPlayerPDC(quest + ".step", p, String.valueOf(next));
    PDCHelper.setPlayerPDC(quest + ".progress", p, "0");

    Location loc = stepLocation(quest, next); // null if none
    Bukkit.getPluginManager().callEvent(new onQuestUpdate(p, quest, loc));
  }

  /** Used by kill / get steps. Advances the step once progress reaches "value". */
  public static void addProgress(Player p, String quest, int amount) {
    int step = getStep(p, quest);
    int required = toInt(stepOption(quest, step, "value"), 1);
    int progress = toInt(getPlayerPDC(quest + ".progress", p), 0) + amount;
    debug(quest + " step " + step + " progress " + progress + "/" + required);

    if (progress >= required) {
      advanceStep(p, quest);
    } else {
      PDCHelper.setPlayerPDC(quest + ".progress", p, String.valueOf(progress));

      // Refresh trackers / Skript on every kill or pickup, not only when the step changes
      Location loc = stepLocation(quest, step);
      Bukkit.getPluginManager().callEvent(new onQuestUpdate(p, quest, loc));
    }
  }

  public static void finishedQuest(Player p, String quest) {
    quest = normalizeId(quest);
    p.sendMessage(yamlGetter.getMessage("messages.info.quests.completed", p, true));

    // "reward" (loot table name or list) or "rewards" - accept both.
    Object rewards = questOption(quest, "rewards");
    if (rewards == null) rewards = questOption(quest, "reward");

    if (rewards instanceof String table) {
      List<ItemStack> items = lootTableHelper.getLootTable(table);
      if (items != null) {
        for (ItemStack item : items) giveOrDrop(p, item);
      }
    } else if (rewards instanceof List<?> list) {
      for (Object entry : list) giveReward(p, entry.toString());
    }

    // The quest is no longer active, but remember that it was done.
    List<String> active = getActiveQuests(p);
    active.remove(quest);
    writeList("quests", p, active);

    List<String> done = getCompletedQuests(p);
    if (!done.contains(quest)) {
      done.add(quest);
      writeList("completed_quests", p, done);
    }

    // Always fire - even for quests without rewards.
    Bukkit.getPluginManager().callEvent(new onQuestComplete(p, quest));
  }

  // ------------------------------------------------------------------
  // NPC interaction
  // ------------------------------------------------------------------

  private enum TalkResult {
    NO_MATCH, // this quest's current step has nothing to do with this NPC
    BLOCKED,  // it does, but the player is missing the required items
    STARTED   // dialogue is playing, the step will complete when it ends
  }

  /**
   * While an NPC is talking (and for a short moment after), further clicks are ignored.
   * This stops click spam and the main-hand/off-hand double interact event
   * from replaying the dialogue or starting things twice.
   */
  private static final long MIN_TALK_COOLDOWN_MS = 500;
  private static final Map<UUID, Long> talkLockUntil = new ConcurrentHashMap<>();

  private static boolean isTalkLocked(Player p) {
    Long until = talkLockUntil.get(p.getUniqueId());
    if (until == null) return false;
    if (System.currentTimeMillis() >= until) {
      talkLockUntil.remove(p.getUniqueId());
      return false;
    }
    return true;
  }

  private static void lockTalk(Player p, int dialogueTicks) {
    long until = System.currentTimeMillis() + dialogueTicks * 50L + MIN_TALK_COOLDOWN_MS;
    talkLockUntil.merge(p.getUniqueId(), until, Long::max);
  }

  private static void sendMissingItems(Player p) {
    p.sendMessage(color("&cYou don't have the required items."));
  }

  public static void talkedNPC(Player p, String NPC) {
    if (isTalkLocked(p)) return;

    List<String> active = getActiveQuests(p);
    boolean blocked = false;

    for (String quest : active) {
      TalkResult result = handleTalkStep(p, quest, NPC);

      if (result == TalkResult.STARTED) return; // one conversation per click
      if (result == TalkResult.BLOCKED) blocked = true;
    }

    // This NPC belongs to an active quest, but the player isn't ready yet.
    if (blocked) {
      sendMissingItems(p);
      lockTalk(p, 0);
      return;
    }

    // Only look for new quests if this NPC wasn't part of an active one.
    tryNewQuest(active, p, NPC);
  }

  /**
   * If the quest's CURRENT step is a talkToNPC step for this NPC and the player has
   * the required items: play the dialogue, then (once it is over) take the items,
   * hand out the rewards and advance the quest.
   */
  private static TalkResult handleTalkStep(Player p, String quest, String npc) {
    int step = getStep(p, quest);
    Object type = stepOption(quest, step, "type");
    Object npcName = stepOption(quest, step, "NPCName");

    if (type == null || npcName == null) return TalkResult.NO_MATCH;
    if (!type.toString().equalsIgnoreCase("talkToNPC")) return TalkResult.NO_MATCH;
    if (!npc.equalsIgnoreCase(npcName.toString())) return TalkResult.NO_MATCH;

    // Nothing is consumed yet - this only checks, so a failed check leaves the quest untouched.
    if (!hasRequiredItems(p, quest, step)) {
      debug(quest + " step " + step + ": " + p.getName() + " is missing required items");
      return TalkResult.BLOCKED;
    }

    int talkDelay = toInt(yamlGetter.getConfig("quests.npc.talk.delay"), 20);
    int delay = 0;

    for (Object line : actionList(quest, step, "talk")) {
      String text = color(line.toString());
      if (delay == 0) {
        p.sendMessage(text);
      } else {
        Bukkit.getScheduler().runTaskLater(RPGCraft.getPlugin(), () -> p.sendMessage(text), delay);
      }
      delay += talkDelay;
    }

    lockTalk(p, delay);
    debug(quest + " step " + step + ": talking to " + npc + " (" + delay + " ticks of dialogue)");

    // Finish after the last line so "quest completed" never shows up mid-conversation.
    if (delay == 0) {
      completeTalkStep(p, quest, step);
    } else {
      Bukkit.getScheduler().runTaskLater(RPGCraft.getPlugin(), () -> completeTalkStep(p, quest, step), delay);
    }
    return TalkResult.STARTED;
  }

  /** Runs when the dialogue is over: re-check, take items, give items, advance. */
  private static void completeTalkStep(Player p, String quest, int step) {
    if (!p.isOnline()) return; // nothing was consumed, they can simply talk again later

    // Quest state could have changed while the NPC was talking.
    if (!getActiveQuests(p).contains(quest) || getStep(p, quest) != step) return;

    // The player may have dropped the items during the dialogue.
    if (!hasRequiredItems(p, quest, step) || !removeItems(p, quest, step)) {
      p.sendMessage(color("&cYou no longer have the required items."));
      return;
    }

    for (Object raw : actionList(quest, step, "giveItems")) {
      giveReward(p, raw.toString());
    }

    advanceStep(p, quest); // moves on, or finishes the quest
  }

  /**
   * Hands out the NPC's next quest.
   *
   * The NPC's quests are checked in order (quest1, quest2, ...):
   *  - completed            -> look at the next one
   *  - active               -> stop, the player is still busy with it (don't start the next one yet)
   *  - not started          -> start this one
   *
   * So clicking an NPC after finishing quest1 starts quest2, and so on.
   */
  static void tryNewQuest(List<String> activeQuests, Player p, String NPC) {
    String npcKey = findNpcKey(NPC);
    if (npcKey == null) return; // this NPC has no quests

    List<String> done = getCompletedQuests(p);

    for (String id : questsOf(npcKey)) {
      String quest = normalizeId(id);

      if (done.contains(quest)) continue;
      if (activeQuests.contains(quest)) return;

      startQuest(p, quest);
      lockTalk(p, 0);

      // The quest giver usually opens with a talkToNPC step 1: play it right away,
      // so the click that starts the quest also gives the intro dialogue/items.
      if (handleTalkStep(p, quest, NPC) == TalkResult.BLOCKED) {
        sendMissingItems(p);
      }
      return; // one new quest per click
    }
  }

  // ------------------------------------------------------------------
  // Inventory checks
  // ------------------------------------------------------------------

  /**
   * Counts matching items across all storage slots.
   * Works with items spread across multiple stacks and custom item metadata.
   */
  private static int countMatching(Player p, ItemStack probe) {
    if (probe == null) return 0;

    int total = 0;

    for (ItemStack item : p.getInventory().getStorageContents()) {
      if (item != null && item.isSimilar(probe)) {
        total += item.getAmount();
      }
    }

    return total;
  }

  /**
   * Removes the requested amount across multiple slots.
   * Returns true only if the full amount was removed.
   */
  private static boolean removeMatching(Player p, ItemStack probe, int amount) {
    if (probe == null || amount <= 0) return false;

    // Check first so we never partially remove an item requirement.
    if (countMatching(p, probe) < amount) return false;

    int remaining = amount;
    ItemStack[] contents = p.getInventory().getStorageContents();

    for (int slot = 0; slot < contents.length && remaining > 0; slot++) {
      ItemStack item = contents[slot];

      if (item == null || !item.isSimilar(probe)) continue;

      int take = Math.min(item.getAmount(), remaining);
      remaining -= take;

      if (take == item.getAmount()) {
        contents[slot] = null;
      } else {
        item.setAmount(item.getAmount() - take);
      }
    }

    p.getInventory().setStorageContents(contents);
    return remaining == 0;
  }

  /**
   * Can this step's removeItems be paid?
   * If the step consumes items, the "get" steps right before it must also still be
   * satisfied - so a player can't collect the items, drop them, and still hand in.
   * Steps without removeItems have nothing to verify.
   */
  private static boolean hasRequiredItems(Player p, String quest, int step) {
    List<Object> toRemove = actionList(quest, step, "removeItems");
    if (toRemove.isEmpty()) return true;

    // The items this step will consume.
    for (Object raw : toRemove) {
      Entry e = Entry.parse(raw);
      ItemStack probe = buildStack(e.id(), 1);

      if (probe == null) {
        Bukkit.getLogger().warning(
          "[Quests] Unknown item '" + e.id() + "' in " + quest + " step " + step
        );
        return false;
      }

      if (countMatching(p, probe) < e.amount()) {
        return false;
      }
    }

    // Consecutive "get" steps directly before this one.
    for (int previous = step - 1; previous >= 1; previous--) {
      Object type = stepOption(quest, previous, "type");

      if (type == null || !type.toString().equalsIgnoreCase("get")) {
        break;
      }

      Object itemId = stepOption(quest, previous, "item");
      int required = toInt(stepOption(quest, previous, "value"), 1);

      if (itemId == null) return false;

      ItemStack probe = buildStack(itemId.toString(), 1);

      if (probe == null || countMatching(p, probe) < required) {
        return false;
      }
    }

    return true;
  }

  /** Takes every removeItems entry of the step. False if any of them can't be paid. */
  private static boolean removeItems(Player p, String quest, int step) {
    for (Object raw : actionList(quest, step, "removeItems")) {
      Entry e = Entry.parse(raw);
      ItemStack probe = buildStack(e.id(), 1);

      if (probe == null || !removeMatching(p, probe, e.amount())) {
        Bukkit.getLogger().warning(
          "[Quests] Could not remove " + e.amount() + "x " + e.id()
            + " for " + p.getName() + " in quest " + quest + " step " + step
        );
        return false;
      }
    }
    return true;
  }

  // ------------------------------------------------------------------
  // Items / rewards
  // ------------------------------------------------------------------

  /** "DIAMOND:5", "myCustomItem:2", "money:100" - amount defaults to 1. */
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

  /** Vanilla material (case-insensitive) or custom item. Always returns a fresh copy. */
  private static ItemStack buildStack(String id, int amount) {
    Material mat = Material.matchMaterial(id);
    if (mat != null) return new ItemStack(mat, amount);

    ItemStack custom = yamlManager.getItem(id);
    if (custom == null) return null;

    ItemStack copy = custom.clone(); // never mutate the registered template
    copy.setAmount(amount);
    return copy;
  }

  static void giveReward(Player p, String raw) {
    Entry e = Entry.parse(raw);

    if (e.id().equalsIgnoreCase("money")) { // check this first
      addMoney(p, e.amount());
      p.sendMessage(color("&aYou have received " + e.amount() + " coin!"));
      return;
    }

    ItemStack stack = buildStack(e.id(), e.amount());
    if (stack == null) {
      Bukkit.getLogger().warning("[Quests] Unknown reward '" + e.id() + "'");
      return;
    }
    giveOrDrop(p, stack);
  }

  /** addItem returns what didn't fit - drop it instead of silently losing it. */
  static void giveOrDrop(Player p, ItemStack item) {
    Map<Integer, ItemStack> leftover = p.getInventory().addItem(item);
    Stats.refreshNextTick(p); //update stats
    for (ItemStack rest : leftover.values()) {
      p.getWorld().dropItemNaturally(p.getLocation(), rest);
    }
  }

  // ------------------------------------------------------------------
  // Locations
  // ------------------------------------------------------------------

  /** The location of a quest step, or null if the step has none / it is incomplete. */
  static Location stepLocation(String quest, int step) {
    String root = questPath(quest);
    return root == null ? null : parseLocationAt(root + ".steps." + step + ".location");
  }

  /**
   * Accepts the old style "quest1.steps.2.location" as well as a full path
   * "John.quest.quest1.steps.2.location". Returns null if incomplete.
   */
  static Location parseLocation(String path) {
    int dot = path.indexOf('.');
    String root = dot > 0 ? questPath(path.substring(0, dot)) : null;
    return parseLocationAt(root != null ? root + path.substring(dot) : path);
  }

  private static Location parseLocationAt(String fullPath) {
    Object world = yamlGetter.getPathInDB("quests", fullPath + ".world");
    Double x = num(yamlGetter.getPathInDB("quests", fullPath + ".x"));
    Double y = num(yamlGetter.getPathInDB("quests", fullPath + ".y"));
    Double z = num(yamlGetter.getPathInDB("quests", fullPath + ".z"));
    if (world == null || x == null || y == null || z == null) return null;

    World w = Bukkit.getWorld(world.toString());
    if (w == null) return null;
    return new Location(w, x, y, z);
  }

  private static Double num(Object o) {
    return o instanceof Number n ? Double.valueOf(n.doubleValue()) : null;
  }
}
