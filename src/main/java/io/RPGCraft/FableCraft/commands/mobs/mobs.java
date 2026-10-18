package io.RPGCraft.FableCraft.commands.mobs;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.Utils.commandHelper.CommandInterface;
import io.RPGCraft.FableCraft.core.Helpers.PDCHelper;
import io.RPGCraft.FableCraft.core.Helpers.lootTableHelper;
import io.RPGCraft.FableCraft.core.YAML.Placeholder;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;
import static io.RPGCraft.FableCraft.RPGCraft.customMobs;

/*
 * mobDB.yml example:
 *
 * zombie_king:
 *   type: ZOMBIE
 *   health: 200
 *   randomSpawns:
 *     frequency: 0.05          # chance per natural spawn attempt (0.0 - 1.0)
 *   spawnPoints:
 *     respawnSeconds: 60       # delay AFTER it dies before the point spawns again
 *     maxAlive: 1              # per spawn point
 *     activationRange: 48      # a player must be this close for the point to spawn
 *     locations:
 *       - "world, 100.5, 64, -200.5"
 *       - "world, 150.5, 70, -180.5, 90"   # optional 5th value = yaw
 */
public class mobs implements CommandInterface, Listener {

  private static final String DB = "mobDB";

  private record SpawnPoint(String key, String mob, Location location,
                            long respawnMillis, int maxAlive, double activationRange) {}

  private static final List<SpawnPoint> spawnPoints = new ArrayList<>();
  private static final Map<String, List<LivingEntity>> pointMobs = new HashMap<>();
  private static final Map<String, Long> nextSpawnAt = new HashMap<>();
  private static final Map<UUID, BossBar> bossBars = new HashMap<>();
  private static final Map<String, Double> spawnChances = new HashMap<>();
  private static BukkitTask spawnPointTask;

  // ------------------------------------------------------------------ helpers

  public static FileConfiguration db() {
    return yamlManager.getInstance().getFileConfig(DB);
  }

  /** Single place that writes to mobDB. If your setOption overloads differ, change it here. */
  static void setDb(String path, Object value) {
    yamlManager.getInstance().setOption(DB, path, value);
  }

  /** All mob IDs (top-level sections of mobDB.yml). */
  public static List<String> getMobNames() {
    FileConfiguration db = db();
    List<String> names = new ArrayList<>();
    for (String key : db.getKeys(false)) {
      if (db.isConfigurationSection(key)) names.add(key);
    }
    return names;
  }

  public static boolean isValidMobType(EntityType type) {
    Class<? extends Entity> cls = type.getEntityClass();
    return type.isSpawnable() && cls != null && LivingEntity.class.isAssignableFrom(cls);
  }

  private static void msg(CommandSender sender, String text) {
    sender.sendMessage(Colorize(text));
  }

  private static List<String> filter(List<String> options, String prefix) {
    String lower = prefix.toLowerCase(Locale.ROOT);
    return options.stream().filter(o -> o.toLowerCase(Locale.ROOT).startsWith(lower)).toList();
  }

  // ------------------------------------------------------------------ command

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    Player p = sender instanceof Player pl ? pl : null;

    if (!sender.hasPermission("FableCraft.mobs")) {
      sender.sendMessage(yamlGetter.getMessage("messages.error.noPermission", p, true));
      return true;
    }
    if (args.length == 0) {
      sender.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", null, true));
      return true;
    }

    switch (args[0].toLowerCase(Locale.ROOT)) {
      case "spawn" -> {
        if (p == null) { msg(sender, "&cOnly players can use this."); return true; }
        if (args.length < 2 || !getMobNames().contains(args[1])) {
          msg(sender, "&cUsage: /mobs spawn <mob>");
          return true;
        }
        getEntity(args[1], p.getLocation());
      }
      case "killall" -> {
        for (LivingEntity le : new ArrayList<>(customMobs)) { // copy: death event removes from the list
          if (le.isValid()) le.setHealth(0);
        }
        customMobs.clear();
        bossBars.values().forEach(BossBar::removeAll);
        bossBars.clear();
        msg(sender, "&aKilled all custom mobs.");
      }
      case "reload" -> {
        reloadSpawns();
        msg(sender, "&aReloaded random spawns and spawn points.");
      }
      case "editor" -> {
        if (p == null) { msg(sender, "&cOnly players can use this."); return true; }
        mobsEditor.openMobDB(p);
      }
      case "addspawn" -> {
        if (p == null) { msg(sender, "&cOnly players can use this."); return true; }
        if (args.length < 2 || !getMobNames().contains(args[1])) {
          msg(sender, "&cUsage: /mobs addspawn <mob>  (uses your current position)");
          return true;
        }
        int count = addSpawnPoint(args[1], p.getLocation());
        msg(sender, "&aAdded spawn point #" + count + " for &f" + args[1]);
      }
      case "removespawn" -> {
        if (args.length < 3 || !getMobNames().contains(args[1])) {
          msg(sender, "&cUsage: /mobs removespawn <mob> <number>  (see /mobs listspawns <mob>)");
          return true;
        }
        int index;
        try { index = Integer.parseInt(args[2]) - 1; }
        catch (NumberFormatException ex) { msg(sender, "&cThe number must be an integer."); return true; }
        msg(sender, removeSpawnPoint(args[1], index)
          ? "&aRemoved spawn point #" + args[2]
          : "&cNo spawn point with that number.");
      }
      case "listspawns" -> {
        if (args.length < 2 || !getMobNames().contains(args[1])) {
          msg(sender, "&cUsage: /mobs listspawns <mob>");
          return true;
        }
        List<String> points = getSpawnPoints(args[1]);
        if (points.isEmpty()) msg(sender, "&7No spawn points for &f" + args[1]);
        for (int i = 0; i < points.size(); i++) msg(sender, "&f" + (i + 1) + ". &7" + points.get(i));
      }
      default -> sender.sendMessage(yamlGetter.getMessage("messages.error.noValidArgument", null, true));
    }
    return true;
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (args.length == 1) {
      return filter(List.of("spawn", "killAll", "reload", "editor", "addSpawn", "removeSpawn", "listSpawns"), args[0]);
    }
    if (args.length == 2 && Set.of("spawn", "addspawn", "removespawn", "listspawns").contains(args[0].toLowerCase(Locale.ROOT))) {
      return filter(getMobNames(), args[1]);
    }
    return List.of();
  }

  // ------------------------------------------------------------------ spawning a mob

  public static LivingEntity getEntity(String name, Location loc) {
    FileConfiguration db = db();
    if (!db.isConfigurationSection(name)) {
      Bukkit.getLogger().severe("[mobs] Unknown mob '" + name + "'");
      return null;
    }
    String base = name + ".";

    EntityType entityType;
    try {
      entityType = EntityType.valueOf(db.getString(base + "type", "").toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ex) {
      Bukkit.getLogger().severe("[mobs] Invalid entity type for '" + name + "': " + db.getString(base + "type"));
      return null;
    }
    // check BEFORE spawning, otherwise a non-living type would be spawned and then fail the cast
    if (!isValidMobType(entityType)) {
      Bukkit.getLogger().severe("[mobs] '" + name + "' uses a type that can't be spawned as a mob: " + entityType);
      return null;
    }

    LivingEntity le = (LivingEntity) loc.getWorld().spawnEntity(loc, entityType);

    if (db.contains(base + "glowing")) le.setGlowing(db.getBoolean(base + "glowing"));
    if (db.contains(base + "invulnerable")) le.setInvulnerable(db.getBoolean(base + "invulnerable"));

    if (db.contains(base + "health")) {
      double health = Math.max(1, Math.min(db.getDouble(base + "health"), 1024));
      AttributeInstance maxHp = le.getAttribute(Attribute.MAX_HEALTH);
      if (maxHp != null) {
        maxHp.setBaseValue(health);
        le.setHealth(Math.min(health, maxHp.getValue()));
      }
    }
    if (db.contains(base + "damage")) setBase(le, Attribute.ATTACK_DAMAGE, db.getDouble(base + "damage"));
    if (db.contains(base + "speed")) setBase(le, Attribute.MOVEMENT_SPEED, db.getDouble(base + "speed"));

    String rawName = db.getString(base + "customName.name");
    if (rawName != null) {
      le.customName(MiniMessage.miniMessage().deserialize(Placeholder.setPlaceholders(rawName, true, le)));
    }
    le.setCustomNameVisible(db.getBoolean(base + "customName.visible", false));

    if (db.contains(base + "bossBar.color")) {
      try {
        BarColor color = BarColor.valueOf(db.getString(base + "bossBar.color", "RED").toUpperCase(Locale.ROOT));
        BarStyle style = BarStyle.valueOf(db.getString(base + "bossBar.barStyle", "SOLID").toUpperCase(Locale.ROOT));
        String title = le.customName() != null
          ? LegacyComponentSerializer.legacySection().serialize(le.customName())
          : name;
        BossBar bar = Bukkit.createBossBar(title, color, style);
        Bukkit.getOnlinePlayers().forEach(bar::addPlayer);
        bossBars.put(le.getUniqueId(), bar);
      } catch (IllegalArgumentException ex) {
        Bukkit.getLogger().warning("[mobs] Invalid bossBar color/barStyle for '" + name + "'");
      }
    }

    String lootTable = db.getString(base + "lootTable");
    if (lootTable != null) PDCHelper.setEntityPDC("lootTable", le, lootTable);
    PDCHelper.setEntityPDC("type", le, name);

    customMobs.add(le);
    return le;
  }

  private static void setBase(LivingEntity le, Attribute attribute, double value) {
    AttributeInstance inst = le.getAttribute(attribute); // null for mobs that lack it (passive mobs have no attack damage)
    if (inst == null) return;
    try {
      inst.setBaseValue(value);
    } catch (IllegalArgumentException ex) {
      Bukkit.getLogger().warning("[mobs] " + attribute + " value " + value + " is out of range");
    }
  }

  // ------------------------------------------------------------------ random (natural) spawns

  public static void reloadSpawns() {
    RPGCraft.spawns = new ArrayList<>();
    spawnChances.clear();
    FileConfiguration db = db();
    for (String mob : getMobNames()) {
      double frequency = db.getDouble(mob + ".randomSpawns.frequency", 0);
      if (frequency <= 0) continue;
      spawnChances.put(mob, Math.min(frequency, 1.0));
      RPGCraft.spawns.add(mob);
    }
    loadSpawnPoints();
    Bukkit.getLogger().info("[mobs] random spawns: " + RPGCraft.spawns + ", spawn points: " + spawnPoints.size());
  }

  @EventHandler(ignoreCancelled = true)
  void onSpawn(CreatureSpawnEvent event) {
    if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) return;
    // only ever cancel, never setCancelled(false) - that would undo other plugins (WorldGuard etc.)
    if (yamlManager.getInstance().getFileConfig("config").getBoolean("mobs.removeAllVanillaSpawning")) {
      event.setCancelled(true);
    }
    randomSpawn(event);
  }

  public void randomSpawn(CreatureSpawnEvent event) {
    if (RPGCraft.spawns.isEmpty()) return;
    List<String> candidates = new ArrayList<>(RPGCraft.spawns);
    Collections.shuffle(candidates);
    for (String mob : candidates) {
      // each mob rolls its own chance, so adding more mobs doesn't dilute the others
      if (Math.random() >= spawnChances.getOrDefault(mob, 0.0)) continue;
      if (!canSpawnAt(mob, event.getLocation())) continue;
      if (getEntity(mob, event.getLocation()) != null) {
        event.setCancelled(true); // replace the vanilla mob
        return;
      }
    }
  }

  private static boolean canSpawnAt(String mob, Location loc) {
    FileConfiguration db = db();

    List<String> spawnOn = db.getStringList(mob + ".randomSpawns.options.spawnOn");
    if (!spawnOn.isEmpty()) {
      String below = loc.clone().subtract(0, 1, 0).getBlock().getType().name();
      if (spawnOn.stream().noneMatch(below::equalsIgnoreCase)) return false;
    }

    List<String> biomes = db.getStringList(mob + ".randomSpawns.options.biomes");
    if (!biomes.isEmpty()) {
      // compare by key instead of Biome.valueOf(): no exception on typos, works with registry-based biomes
      String biome = loc.getBlock().getBiome().getKey().getKey();
      if (biomes.stream().noneMatch(b -> b.replace("minecraft:", "").equalsIgnoreCase(biome))) return false;
    }
    return true;
  }

  // ------------------------------------------------------------------ spawn points

  public static List<String> getSpawnPoints(String mob) {
    return db().getStringList(mob + ".spawnPoints.locations");
  }

  /** Appends the given location (centered on its block) to the mob's spawn points. Returns the new count. */
  public static int addSpawnPoint(String mob, Location at) {
    FileConfiguration db = db();
    String base = mob + ".spawnPoints.";
    // write the defaults once so they're easy to find and tweak in the yml
    if (!db.contains(base + "respawnSeconds")) setDb(base + "respawnSeconds", 60);
    if (!db.contains(base + "maxAlive")) setDb(base + "maxAlive", 1);
    if (!db.contains(base + "activationRange")) setDb(base + "activationRange", 48);

    List<String> points = new ArrayList<>(db.getStringList(base + "locations"));
    points.add(formatLocation(at));
    setDb(base + "locations", points);

    yamlManager.getInstance().saveData();
    loadSpawnPoints();
    return points.size();
  }

  /** index is 0-based. */
  public static boolean removeSpawnPoint(String mob, int index) {
    String path = mob + ".spawnPoints.locations";
    List<String> points = new ArrayList<>(db().getStringList(path));
    if (index < 0 || index >= points.size()) return false;
    points.remove(index);
    setDb(path, points);
    yamlManager.getInstance().saveData();
    loadSpawnPoints();
    return true;
  }

  public static void loadSpawnPoints() {
    spawnPoints.clear();
    FileConfiguration db = db();
    for (String mob : getMobNames()) {
      String base = mob + ".spawnPoints.";
      List<String> raw = db.getStringList(base + "locations");
      if (raw.isEmpty()) continue;

      long respawn = (long) (Math.max(0, db.getDouble(base + "respawnSeconds", 60)) * 1000);
      int maxAlive = Math.max(1, db.getInt(base + "maxAlive", 1));
      double range = Math.max(1, db.getDouble(base + "activationRange", 48));

      for (String line : raw) {
        Location loc = parseLocation(line);
        if (loc == null) {
          Bukkit.getLogger().warning("[mobs] Invalid spawn point for '" + mob + "': \"" + line + "\" (format: world, x, y, z)");
          continue;
        }
        spawnPoints.add(new SpawnPoint(mob + "@" + line.trim(), mob, loc, respawn, maxAlive, range));
      }
    }
    Set<String> valid = new HashSet<>();
    spawnPoints.forEach(sp -> valid.add(sp.key()));
    pointMobs.keySet().retainAll(valid);
    nextSpawnAt.keySet().retainAll(valid);
  }

  /** Call this once from onEnable (after your configs are loaded). Safe to call again. */
  public static void startSpawnPointTask() {
    if (spawnPointTask != null) spawnPointTask.cancel();
    loadSpawnPoints();
    spawnPointTask = new BukkitRunnable() {
      @Override
      public void run() { tickSpawnPoints(); }
    }.runTaskTimer(RPGCraft.getPlugin(), 100L, 100L); // every 5 seconds
  }

  private static void tickSpawnPoints() {
    customMobs.removeIf(LivingEntity::isDead); // drop references to mobs that vanished without a death event
    long now = System.currentTimeMillis();

    for (SpawnPoint sp : spawnPoints) {
      List<LivingEntity> alive = pointMobs.computeIfAbsent(sp.key(), k -> new ArrayList<>());
      // the respawn timer starts when we notice a mob from this point is gone
      if (alive.removeIf(LivingEntity::isDead)) nextSpawnAt.put(sp.key(), now + sp.respawnMillis());

      if (alive.size() >= sp.maxAlive()) continue;
      if (now < nextSpawnAt.getOrDefault(sp.key(), 0L)) continue;

      Location loc = sp.location();
      if (!loc.isChunkLoaded()) continue; // never force-load chunks
      if (loc.getWorld().getNearbyPlayers(loc, sp.activationRange()).isEmpty()) continue;

      LivingEntity le = getEntity(sp.mob(), loc.clone());
      if (le == null) continue;
      le.setPersistent(false); // not saved with the chunk -> no duplicates after unload/restart
      alive.add(le);
    }
  }

  private static Location parseLocation(String raw) {
    if (raw == null) return null;
    String[] parts = raw.trim().split("\\s*[,;]\\s*|\\s+");
    if (parts.length < 4) return null;
    World world = Bukkit.getWorld(parts[0]);
    if (world == null) return null;
    try {
      double x = Double.parseDouble(parts[1]);
      double y = Double.parseDouble(parts[2]);
      double z = Double.parseDouble(parts[3]);
      float yaw = parts.length > 4 ? Float.parseFloat(parts[4]) : 0f;
      return new Location(world, x, y, z, yaw, 0f);
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  private static String formatLocation(Location l) {
    return String.format(Locale.ROOT, "%s, %.1f, %.1f, %.1f, %.0f",
      l.getWorld().getName(), l.getBlockX() + 0.5, l.getY(), l.getBlockZ() + 0.5, l.getYaw());
  }

  // ------------------------------------------------------------------ events

  @EventHandler
  void onDeath(EntityDeathEvent event) {
    LivingEntity le = event.getEntity();

    String lootTable = PDCHelper.getEntityPDC("lootTable", le);
    if (lootTable != null) {
      event.getDrops().clear();
      event.getDrops().addAll(lootTableHelper.getLootTable(lootTable));
    }

    customMobs.remove(le);
    BossBar bar = bossBars.remove(le.getUniqueId());
    if (bar != null) bar.removeAll();
  }

  @EventHandler(ignoreCancelled = true)
  void onDamage(EntityDamageEvent event) {
    if (!(event.getEntity() instanceof LivingEntity le)) return;
    BossBar bar = bossBars.get(le.getUniqueId());
    if (bar == null) return;
    AttributeInstance max = le.getAttribute(Attribute.MAX_HEALTH);
    double maxHp = max != null ? max.getValue() : 20;
    bar.setProgress(Math.max(0, Math.min(1, (le.getHealth() - event.getFinalDamage()) / maxHp)));
  }

  @EventHandler
  void onJoin(PlayerJoinEvent event) {
    bossBars.values().forEach(bar -> bar.addPlayer(event.getPlayer()));
  }
}

