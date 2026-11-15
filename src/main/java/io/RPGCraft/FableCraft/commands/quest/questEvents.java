
package io.RPGCraft.FableCraft.commands.quest;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.core.Helpers.PDCHelper;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static io.RPGCraft.FableCraft.Utils.Utils.isCitizensNPC;
import static io.RPGCraft.FableCraft.commands.quest.questManager.*;
import static io.RPGCraft.FableCraft.commands.quest.questManager.addProgress;
import static io.RPGCraft.FableCraft.commands.quest.questManager.debug;
import static io.RPGCraft.FableCraft.commands.quest.questManager.getActiveQuests;
import static io.RPGCraft.FableCraft.commands.quest.questManager.getStep;
import static io.RPGCraft.FableCraft.commands.quest.questManager.stepOption;

public class questEvents implements Listener {

  // ------------------------------------------------------------
  // KILL QUESTS
  // ------------------------------------------------------------

  @EventHandler
  public void onEntityDeath(EntityDeathEvent event) {
    LivingEntity entity = event.getEntity();
    Player killer = resolveKiller(entity);
    boolean npc = isNPC(entity);

    if (killer != null || npc) {
      debug("death: " + entity.getType()
        + " name=" + entity.getName()
        + " npc=" + npc
        + " killer=" + (killer == null
        ? "none" : killer.getName()));
    }

    if (killer == null) {
      return;
    }

    for (String quest : getActiveQuests(killer)) {
      int step = getStep(killer, quest);

      Object type = stepOption(quest, step, "type");
      if (type != null
        && !type.toString().equalsIgnoreCase("kill")) {
        continue;
      }

      Object wanted = stepOption(quest, step, "entity");
      if (wanted == null) {
        continue;
      }

      String target = wanted.toString();
      boolean matches;

      if (npc) {
        matches = target.equalsIgnoreCase("NPC")
          || entity.getName().equalsIgnoreCase(target);
      } else {
        matches = !target.equalsIgnoreCase("NPC")
          && entity.getType().name()
          .equalsIgnoreCase(target);
      }

      if (matches) {
        addProgress(killer, quest, 1);
      }
    }
  }

  private static Player resolveKiller(LivingEntity entity) {
    Player killer = entity.getKiller();

    if (killer != null) {
      return killer;
    }

    if (entity.getLastDamageCause()
      instanceof EntityDamageByEntityEvent byEntity) {

      Entity damager = byEntity.getDamager();

      if (damager instanceof Player direct) {
        return direct;
      }

      if (damager instanceof Projectile projectile
        && projectile.getShooter() instanceof Player shooter) {
        return shooter;
      }
    }

    return null;
  }

  private static boolean isNPC(Entity entity) {
    if (isCitizensNPC(entity) || entity.hasMetadata("NPC")) {
      return true;
    }

    return entity instanceof Player player
      && Bukkit.getPlayer(player.getUniqueId()) == null;
  }

  // ------------------------------------------------------------
  // GET ITEM QUESTS
  // ------------------------------------------------------------

  @EventHandler(ignoreCancelled = true)
  public void onPlayerPickUp(EntityPickupItemEvent event) {
    if (!(event.getEntity() instanceof Player player)) {
      return;
    }

    ItemStack pickedStack = event.getItem().getItemStack();

    // EntityPickupItemEvent#getRemaining() is the amount left
    // in the dropped entity after the pickup.
    int picked = pickedStack.getAmount() - event.getRemaining();

    if (picked <= 0) {
      return;
    }

    for (String quest : getActiveQuests(player)) {
      int step = getStep(player, quest);

      Object type = stepOption(quest, step, "type");
      if (type == null
        || !type.toString().equalsIgnoreCase("get")) {
        continue;
      }

      Object wanted = stepOption(quest, step, "item");
      if (wanted == null) {
        continue;
      }

      if (matchesQuestItem(pickedStack, wanted.toString())) {
        addProgress(player, quest, picked);

        debug("get quest: player=" + player.getName()
          + " quest=" + quest
          + " item=" + wanted
          + " amount=" + picked);
      }
    }
  }

  /**
   * Supports:
   *
   * Vanilla material: STRING
   * Custom item config key: woodenSword
   * Custom ItemID: just_a_sword
   *
   * Custom items are matched using the actual ItemStack metadata
   * created by yamlManager, including its PDC data.
   */
  private static boolean matchesQuestItem(
    ItemStack actual,
    String wanted
  ) {
    if (actual == null || actual.getType().isAir()) {
      return false;
    }

    String target = wanted.trim();

    if (target.isEmpty()) {
      return false;
    }

    // First, try matching a custom item by its database key.
    ItemStack expected = yamlManager.getItem(target);

    if (expected != null) {
      if (actual.isSimilar(expected)) {
        return true;
      }

      // The item may have changed durability or other
      // mutable metadata. Compare its persistent custom ID
      // against the database item's ID as a fallback.
      if (matchesCustomItemId(actual, target)) {
        return true;
      }
    }

    // Next, accept the configured ItemID, which may differ
    // from the custom item's database key.
    for (YamlConfiguration config :
      getItemDatabaseFiles()) {

      if (config == null) {
        continue;
      }

      for (String key : config.getKeys(false)) {
        String itemId = config.getString(key + ".ItemID");

        if (itemId == null
          || !itemId.equalsIgnoreCase(target)) {
          continue;
        }

        ItemStack customItem = yamlManager.getItem(key);

        if (customItem != null
          && actual.isSimilar(customItem)) {
          return true;
        }

        if (matchesCustomItemId(actual, itemId)) {
          return true;
        }
      }
    }

    // Finally, allow normal vanilla materials.
    // This check is deliberately last so custom-item
    // identifiers can be resolved before material matching.
    return actual.getType().name().equalsIgnoreCase(target);
  }

  /**
   * Looks up all item database configurations.
   */
  private static List<YamlConfiguration> getItemDatabaseFiles() {
    return RPGCraft.DBFileConfiguration.getOrDefault(
      "itemDB", List.of()
    );
  }

  /**
   * Checks whether an item has the requested custom identity.
   *
   * This uses the database-generated item as the source of truth
   * rather than assuming how PDCHelper names its PDC keys.
   */
  private static boolean matchesCustomItemId(
    ItemStack actual,
    String wantedId
  ) {
    if (actual == null || wantedId == null) {
      return false;
    }

    for (YamlConfiguration config : getItemDatabaseFiles()) {
      if (config == null) {
        continue;
      }

      for (String key : config.getKeys(false)) {
        String itemId = config.getString(key + ".ItemID");

        if (itemId == null
          || !itemId.equalsIgnoreCase(wantedId)) {
          continue;
        }

        ItemStack expected = yamlManager.getItem(key);

        if (expected != null && actual.isSimilar(expected)) {
          return true;
        }
      }
    }

    return false;
  }

  // ------------------------------------------------------------
  // Auto-start quests: tracked quest IDs per player so each
  // auto-start fires only once, even after a server restart.
  // ------------------------------------------------------------

  private static Set<String> getAutoStartedQuests(Player p) {
    String raw = PDCHelper.getPlayerPDC("auto_started_quests", p);
    Set<String> set = new HashSet<>();
    if (raw != null && !raw.isEmpty()) {
      for (String id : raw.split(";")) {
        if (!id.isEmpty()) set.add(id);
      }
    }
    return set;
  }

  private static void markAutoStarted(Player p, String quest) {
    Set<String> set = getAutoStartedQuests(p);
    set.add(quest);
    PDCHelper.setPlayerPDC("auto_started_quests", p, String.join(";", set));
  }

  // ------------------------------------------------------------
  // AUTO-START TRIGGERS
  // These start a quest automatically when the configured event
  // occurs.  Each quest may declare an auto-start section:
  //
  //   quests.<npc>.quest.<questId>.autoStart:
  //     type: firstJoin | rightClick | leftClick | blockPlace
  //         | blockBreak | command | playerDeath
  //         | worldChange | entityInteract
  //     # optional: only start if the player has this permission
  //     permission: my.permission
  //     # optional: delay in ticks before starting (default 0)
  //     delay: 20
  //     # optional: only start once per player (default true)
  //     once: true
  //
  // Type-specific options:
  //   rightClick / leftClick:
  //     item: DIAMOND_SWORD          # material or custom item key
  //     hand: main | off             # which hand (default: main)
  //   blockPlace / blockBreak:
  //     block: STONE                 # material to match
  //   command:
  //     command: join                # command name (without /)
  //   worldChange:
  //     world: world_nether          # world name to match
  //   entityInteract:
  //     entity: COW                  # entity type to match
  // ------------------------------------------------------------

  /** Check whether any quest with an auto-start trigger should fire. */
  private static void checkAutoStart(Player p, String triggerType, String extra) {
    if (p == null || !p.isOnline()) return;

    // Gather all quests that have an auto-start section matching this trigger.
    for (String npcKey : childKeysInDB("")) {
      for (String id : childKeysInDB(npcKey + ".quest")) {
        String path = npcKey + ".quest." + id;
        String node = path + ".autoStart";

        Object type = yamlGetter.getPathInDB("quests", node + ".type");
        if (type == null)
          continue;
        if (!type.toString().equalsIgnoreCase(triggerType))
          continue;

        // Check type-specific conditions
        if (!matchesAutoStartCondition(p, path + ".autoStart", triggerType, extra))
          continue;

        // Check permission requirement
        Object perm = yamlGetter.getPathInDB("quests", node + ".permission");
        if (perm != null && !p.hasPermission(perm.toString()))
          continue;

        // Check once-per-player flag
        String questId = normalizeId(id);
        Object once = yamlGetter.getPathInDB("quests", node + ".once");
        boolean oncePerPlayer = once == null || Boolean.parseBoolean(once.toString());

        if (oncePerPlayer && getAutoStartedQuests(p).contains(questId))
          continue;

        // Check level requirement
        if (!meetsLevelRequirement(p, questId)) {
          int minLevel = getMinLevel(questId);
          sendLevelRequirementMessage(p, minLevel);
          continue;
        }

        // Don't start if already active or completed
        if (getActiveQuests(p).contains(questId))
          continue;
        if (getCompletedQuests(p).contains(questId))
          continue;

        // Apply optional delay
        Object delayObj = yamlGetter.getPathInDB("quests", node + ".delay");
        int delay = toInt(delayObj, 0);

        if (delay > 0) {
          final String q = questId;
          Bukkit.getScheduler().runTaskLater(RPGCraft.getPlugin(), () -> {
            if (p.isOnline() && !getActiveQuests(p).contains(q)) {
              startQuest(p, q);
              if (oncePerPlayer) markAutoStarted(p, q);
              debug("auto-start (delayed): " + q + " for " + p.getName() + " (trigger: " + triggerType + ")");
            }
          }, delay);
        } else {
          startQuest(p, questId);
          if (oncePerPlayer) markAutoStarted(p, questId);
          debug("auto-start: " + questId + " for " + p.getName() + " (trigger: " + triggerType + ")");
        }
      }
    }
  }

  /**
   * Returns the direct children of a path in the quests DB.
   * Mirrors questManager.childKeys() but made package-private for use here.
   */
  private static List<String> childKeysInDB(String path) {
    return questManager.childKeys(path);
  }

  /**
   * Checks type-specific conditions for an auto-start trigger.
   * The {@code extra} parameter carries event-specific data:
   *   - rightClick/leftClick: "ITEM:DIAMOND_SWORD;HAND:main"
   *   - blockPlace/blockBreak: "STONE"
   *   - command: "join"
   *   - worldChange: "world_nether"
   *   - entityInteract: "COW"
   */
  private static boolean matchesAutoStartCondition(
    Player p, String node, String triggerType, String extra
  ) {
    switch (triggerType.toLowerCase(Locale.ROOT)) {
      case "rightclick", "leftclick" -> {
        // Check item requirement
        Object item = yamlGetter.getPathInDB("quests", node + ".item");
        if (item != null) {
          String wanted = item.toString();
          ItemStack held = null;

          // Check hand preference
          Object hand = yamlGetter.getPathInDB("quests", node + ".hand");
          if (hand != null && hand.toString().equalsIgnoreCase("off")) {
            held = p.getInventory().getItemInOffHand();
          } else {
            held = p.getInventory().getItemInMainHand();
          }

          if (held == null || held.getType().isAir())
            return false;
          if (!matchesQuestItem(held, wanted))
            return false;
        }
        return true;
      }
      case "blockplace", "blockbreak" -> {
        Object block = yamlGetter.getPathInDB("quests", node + ".block");
        if (block != null) {
          Material wanted = Material.matchMaterial(block.toString());
          if (wanted != null && !wanted.name().equalsIgnoreCase(extra))
            return false;
        }
        return true;
      }
      case "command" -> {
        Object cmd = yamlGetter.getPathInDB("quests", node + ".command");
        if (cmd != null && !cmd.toString().equalsIgnoreCase(extra))
          return false;
        return true;
      }
      case "worldchange" -> {
        Object world = yamlGetter.getPathInDB("quests", node + ".world");
        if (world != null && !world.toString().equalsIgnoreCase(extra))
          return false;
        return true;
      }
      case "entityinteract" -> {
        Object entity = yamlGetter.getPathInDB("quests", node + ".entity");
        if (entity != null && !entity.toString().equalsIgnoreCase(extra))
          return false;
        return true;
      }
      default -> {
        // firstJoin, playerDeath have no extra conditions
        return true;
      }
    }
  }

  // ------------------------------------------------------------
  // EVENT HANDLERS
  // ------------------------------------------------------------

  /** Player first join — starts quests with autoStart.type: firstJoin */
  @EventHandler
  public void onPlayerFirstJoin(PlayerJoinEvent event) {
    Player p = event.getPlayer();
    if (!p.hasPlayedBefore()) {
      checkAutoStart(p, "firstJoin", null);
    }
  }

  /** Player right/left click — starts quests with autoStart.type: rightClick / leftClick */
  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    Player p = event.getPlayer();
    Action action = event.getAction();

    // Only trigger on actual right/left click actions
    if (action != Action.RIGHT_CLICK_AIR
        && action != Action.RIGHT_CLICK_BLOCK
        && action != Action.LEFT_CLICK_AIR
        && action != Action.LEFT_CLICK_BLOCK) {
      return;
    }

    String triggerType;
    if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
      triggerType = "rightClick";
    } else {
      triggerType = "leftClick";
    }

    // Build extra info: item held
    String extra = null;
    checkAutoStart(p, triggerType, extra);
  }

  /** Player places a block — starts quests with autoStart.type: blockPlace */
  @EventHandler
  public void onBlockPlace(BlockPlaceEvent event) {
    Player p = event.getPlayer();
    Block block = event.getBlockPlaced();
    checkAutoStart(p, "blockPlace", block.getType().name());
  }

  /** Player breaks a block — starts quests with autoStart.type: blockBreak */
  @EventHandler
  public void onBlockBreak(BlockBreakEvent event) {
    Player p = event.getPlayer();
    checkAutoStart(p, "blockBreak", event.getBlock().getType().name());
  }

  /** Player runs a command — starts quests with autoStart.type: command */
  @EventHandler
  public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
    Player p = event.getPlayer();
    String[] parts = event.getMessage().substring(1).split(" ");
    if (parts.length > 0) {
      checkAutoStart(p, "command", parts[0]);
    }
  }

  /** Player dies — starts quests with autoStart.type: playerDeath */
  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    Player p = event.getEntity();
    checkAutoStart(p, "playerDeath", null);
  }

  /** Player changes world — starts quests with autoStart.type: worldChange */
  @EventHandler
  public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
    Player p = event.getPlayer();
    checkAutoStart(p, "worldChange", p.getWorld().getName());
  }

  /** Player interacts with an entity — starts quests with autoStart.type: entityInteract */
  @EventHandler
  public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    Player p = event.getPlayer();
    String entityType = event.getRightClicked().getType().name();
    checkAutoStart(p, "entityInteract", entityType);
  }
}
