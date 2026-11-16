package io.RPGCraft.FableCraft.core;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.core.Helpers.PDCHelper;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Manages mob skills defined in the mobSkills database folder.
 *
 * YAML format (mobSkills/Default.yml):
 *
 * fireAttack:
 *   name: "Fire Attack"
 *   description: "Deals extra fire damage on hit"
 *   effects:
 *     attackDamageBonus: 5.0
 *     setOnFire: true
 *     fireTicks: 100
 *   cooldown: 40
 *   trigger: "onAttack"
 *
 * healOverTime:
 *   name: "Regeneration"
 *   description: "Heals over time"
 *   effects:
 *     healAmount: 1.0
 *   cooldown: 100
 *   trigger: "periodic"
 *   interval: 200
 *
 * speedBoost:
 *   name: "Haste"
 *   description: "Moves faster"
 *   effects:
 *     movementSpeedBonus: 0.3
 *   cooldown: 0
 *   trigger: "onSpawn"
 *
 * Commands via /npc set skill <skillName> (on nearest NPC)
 * Commands via /npc remove skill (removes all skill tags from nearest NPC)
 */
public class MobSkillManager implements Listener {

  private static final String DB = "mobSkills";
  private static final Map<String, MobSkill> loadedSkills = new HashMap<>();

  /** Tracks active periodic skill tasks */
  private static final Map<UUID, BukkitRunnable> periodicTasks = new HashMap<>();

  /** Tracks cooldowns per entity per skill: key = entity UUID, value = skill -> nextAllowedTick */
  private static final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

  public static class MobSkill {
    public final String id;
    public final String name;
    public final String description;
    public final Map<String, Object> effects;
    public final int cooldown;
    public final String trigger; // "onSpawn", "onAttack", "periodic"
    public final int interval;   // ticks between periodic triggers

    public MobSkill(String id, String name, String description, Map<String, Object> effects,
                    int cooldown, String trigger, int interval) {
      this.id = id;
      this.name = name;
      this.description = description;
      this.effects = effects;
      this.cooldown = cooldown;
      this.trigger = trigger;
      this.interval = interval;
    }
  }

  // ------------------------------------------------------------------ loading

  /**
   * Call once during onEnable to load all mob skill definitions.
   */
  public static void loadSkills() {
    loadedSkills.clear();
    var files = RPGCraft.DBFileConfiguration.get(DB);
    if (files == null) {
      Bukkit.getLogger().info("[MobSkills] No mobSkills folder found. Create mobSkills/Default.yml to define skills.");
      return;
    }

    for (YamlConfiguration config : files) {
      if (config == null) continue;
      for (String key : config.getKeys(false)) {
        if (config.isConfigurationSection(key)) {
          MobSkill skill = parseSkill(key, config);
          if (skill != null) {
            loadedSkills.put(key.toLowerCase(Locale.ROOT), skill);
          }
        }
      }
    }
    Bukkit.getLogger().info("[MobSkills] Loaded " + loadedSkills.size() + " skill definitions.");
  }

  private static MobSkill parseSkill(String id, YamlConfiguration config) {
    String base = id + ".";
    String name = config.getString(base + "name", id);
    String description = config.getString(base + "description", "");
    Map<String, Object> effects = new HashMap<>();
    if (config.isConfigurationSection(base + "effects")) {
      effects.putAll(config.getConfigurationSection(base + "effects").getValues(false));
    }
    int cooldown = config.getInt(base + "cooldown", 0);
    String trigger = config.getString(base + "trigger", "onSpawn");
    int interval = config.getInt(base + "interval", 200);

    return new MobSkill(id, name, description, effects, cooldown, trigger, interval);
  }

  /** Returns all available skill IDs. */
  public static List<String> getSkillNames() {
    return new ArrayList<>(loadedSkills.keySet());
  }

  /** Returns a skill definition by ID (case-insensitive). */
  public static MobSkill getSkill(String id) {
    return loadedSkills.get(id.toLowerCase(Locale.ROOT));
  }

  // ------------------------------------------------------------------ entity skill tags

  /**
   * Returns the list of skill IDs assigned to an entity via PDC tags.
   */
  public static List<String> getEntitySkills(Entity entity) {
    String tag = PDCHelper.getEntityPDC("mobSkills", entity);
    if (tag == null || tag.isEmpty()) return Collections.emptyList();
    return Arrays.asList(tag.split(","));
  }

  /**
   * Adds a skill tag to an entity. If the entity already has skill tags, appends to the list.
   */
  public static void addSkillToEntity(Entity entity, String skillId) {
    MobSkill skill = getSkill(skillId);
    if (skill == null) {
      Bukkit.getLogger().warning("[MobSkills] Unknown skill: " + skillId);
      return;
    }

    String current = PDCHelper.getEntityPDC("mobSkills", entity);
    List<String> skills = new ArrayList<>();
    if (current != null && !current.isEmpty()) {
      skills.addAll(Arrays.asList(current.split(",")));
    }
    if (!skills.contains(skillId)) {
      skills.add(skillId);
      PDCHelper.setEntityPDC("mobSkills", entity, String.join(",", skills));
    }

    // Apply onSpawn effects immediately if adding to a live entity
    if (skill.trigger.equals("onSpawn") && entity instanceof LivingEntity) {
      applySkillEffects((LivingEntity) entity, skill);
    }
    if (skill.trigger.equals("periodic")) {
      startPeriodicTask(entity, skill);
    }
  }

  /**
   * Removes a skill tag from an entity.
   */
  public static void removeSkillFromEntity(Entity entity, String skillId) {
    String current = PDCHelper.getEntityPDC("mobSkills", entity);
    if (current == null || current.isEmpty()) return;

    List<String> skills = new ArrayList<>(Arrays.asList(current.split(",")));
    skills.remove(skillId);

    if (skills.isEmpty()) {
      PDCHelper.setEntityPDC("mobSkills", entity, null);
    } else {
      PDCHelper.setEntityPDC("mobSkills", entity, String.join(",", skills));
    }

    // Stop periodic task if it was using this skill
    stopPeriodicTask(entity);
    // Restart periodic task if other skills still use it
    for (String remainingSkill : skills) {
      MobSkill skill = getSkill(remainingSkill);
      if (skill != null && skill.trigger.equals("periodic")) {
        startPeriodicTask(entity, skill);
        break;
      }
    }
  }

  /**
   * Removes all skill tags from an entity.
   */
  public static void removeAllSkillsFromEntity(Entity entity) {
    PDCHelper.setEntityPDC("mobSkills", entity, null);
    stopPeriodicTask(entity);
  }

  // ------------------------------------------------------------------ apply on spawn

  /**
   * Called when a mob is spawned. Applies all onSpawn and starts periodic tasks.
   * Pass the mob ID (from mobDB) so we can check for auto-assigned skills.
   */
  public static void applySpawnSkills(LivingEntity entity, String mobDbId) {
    // Check if the mobDB entry has auto-assigned skills
    if (mobDbId != null) {
      var mobDb = RPGCraft.DBFileConfiguration.get("mobDB");
      if (mobDb != null) {
        for (YamlConfiguration config : mobDb) {
          if (config == null) continue;
          String skillsPath = mobDbId + ".skills";
          if (config.isList(skillsPath)) {
            for (Object obj : config.getStringList(skillsPath)) {
              String skillId = (String) obj;
              MobSkill skill = getSkill(skillId);
              if (skill != null) {
                addSkillToEntity(entity, skillId);
              }
            }
          }
        }
      }
    }

    // Apply skills from PDC tags
    for (String skillId : getEntitySkills(entity)) {
      MobSkill skill = getSkill(skillId);
      if (skill == null) continue;

      if (skill.trigger.equals("onSpawn")) {
        applySkillEffects(entity, skill);
      } else if (skill.trigger.equals("periodic")) {
        startPeriodicTask(entity, skill);
      }
    }
  }

  // ------------------------------------------------------------------ apply on attack

  /**
   * Called when an entity attacks. Checks cooldown and applies onAttack effects.
   */
  public static void applyAttackSkills(LivingEntity entity) {
    long currentTick = Bukkit.getCurrentTick();

    for (String skillId : getEntitySkills(entity)) {
      MobSkill skill = getSkill(skillId);
      if (skill == null || !skill.trigger.equals("onAttack")) continue;

      Map<String, Long> entityCooldowns = cooldowns.computeIfAbsent(entity.getUniqueId(), k -> new HashMap<>());
      long nextAllowed = entityCooldowns.getOrDefault(skillId, 0L);

      if (currentTick < nextAllowed) continue;

      applySkillEffects(entity, skill);
      if (skill.cooldown > 0) {
        entityCooldowns.put(skillId, currentTick + skill.cooldown);
      }
    }
  }

  // ------------------------------------------------------------------ effect application

  private static void applySkillEffects(LivingEntity entity, MobSkill skill) {
    Map<String, Object> effects = skill.effects;

    // Attack damage bonus
    if (effects.containsKey("attackDamageBonus")) {
      double bonus = Double.parseDouble(effects.get("attackDamageBonus").toString());
      var attr = entity.getAttribute(org.bukkit.attribute.Attribute.ATTACK_DAMAGE);
      if (attr != null) {
        attr.setBaseValue(attr.getBaseValue() + bonus);
      }
    }

    // Movement speed bonus
    if (effects.containsKey("movementSpeedBonus")) {
      double bonus = Double.parseDouble(effects.get("movementSpeedBonus").toString());
      var attr = entity.getAttribute(org.bukkit.attribute.Attribute.MOVEMENT_SPEED);
      if (attr != null) {
        attr.setBaseValue(attr.getBaseValue() + bonus);
      }
    }

    // Health bonus
    if (effects.containsKey("healthBonus")) {
      double bonus = Double.parseDouble(effects.get("healthBonus").toString());
      var attr = entity.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
      if (attr != null) {
        attr.setBaseValue(attr.getBaseValue() + bonus);
        entity.setHealth(Math.min(entity.getHealth() + bonus, attr.getValue()));
      }
    }

    // Set target on fire
    if (effects.containsKey("setOnFire") && Boolean.parseBoolean(effects.get("setOnFire").toString())) {
      int ticks = Integer.parseInt(effects.getOrDefault("fireTicks", "100").toString());
      entity.setFireTicks(ticks);
    }

    // Heal amount (for periodic heal skills)
    if (effects.containsKey("healAmount")) {
      double amount = Double.parseDouble(effects.get("healAmount").toString());
      var maxAttr = entity.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
      double max = maxAttr != null ? maxAttr.getValue() : 20;
      entity.setHealth(Math.min(max, entity.getHealth() + amount));
    }

    // Knockback on attack
    if (effects.containsKey("knockback")) {
      // Stored for use during attack events
      PDCHelper.setEntityPDC("skillKnockback", entity, effects.get("knockback").toString());
    }

    // Glow effect
    if (effects.containsKey("glowing")) {
      entity.setGlowing(Boolean.parseBoolean(effects.get("glowing").toString()));
    }
  }

  // ------------------------------------------------------------------ periodic tasks

  private static void startPeriodicTask(Entity entity, MobSkill skill) {
    if (!entity.isValid()) return;
    stopPeriodicTask(entity);

    BukkitRunnable task = new BukkitRunnable() {
      @Override
      public void run() {
        if (!entity.isValid() || entity.isDead()) {
          cancel();
          periodicTasks.remove(entity.getUniqueId());
          return;
        }
        if (entity instanceof LivingEntity le) {
          long currentTick = Bukkit.getCurrentTick();
          Map<String, Long> entityCooldowns = cooldowns.computeIfAbsent(entity.getUniqueId(), k -> new HashMap<>());
          long nextAllowed = entityCooldowns.getOrDefault(skill.id, 0L);
          if (currentTick >= nextAllowed) {
            applySkillEffects(le, skill);
            if (skill.cooldown > 0) {
              entityCooldowns.put(skill.id, currentTick + skill.cooldown);
            }
          }
        }
      }
    };
    task.runTaskTimer(RPGCraft.getPlugin(), skill.interval, skill.interval);
    periodicTasks.put(entity.getUniqueId(), task);
  }

  private static void stopPeriodicTask(Entity entity) {
    BukkitRunnable task = periodicTasks.remove(entity.getUniqueId());
    if (task != null) task.cancel();
    cooldowns.remove(entity.getUniqueId());
  }

  // ------------------------------------------------------------------ event listeners

  @EventHandler
  public void onEntitySpawn(EntitySpawnEvent event) {
    if (event.getEntity() instanceof LivingEntity le) {
      String mobType = PDCHelper.getEntityPDC("type", le);
      applySpawnSkills(le, mobType);
    }
  }

  /**
   * Cleanup when entity dies.
   */
  @EventHandler
  public void onEntityDeath(org.bukkit.event.entity.EntityDeathEvent event) {
    Entity entity = event.getEntity();
    stopPeriodicTask(entity);
    cooldowns.remove(entity.getUniqueId());
  }
}
