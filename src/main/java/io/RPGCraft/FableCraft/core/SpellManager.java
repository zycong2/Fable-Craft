package io.RPGCraft.FableCraft.core;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.commands.mobs.mobs;
import io.RPGCraft.FableCraft.core.Helpers.PDCHelper;
import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.RPGCraft.FableCraft.RPGCraft.MM;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.*;
import static io.RPGCraft.FableCraft.core.YAML.yamlGetter.getAllNodesInDB;
import static io.RPGCraft.FableCraft.core.YAML.yamlGetter.getPathInDB;

/**
 * Magic system that allows items to cast configurable spells.
 * Spells are defined in the spells/ database folder and can include:
 * - Damage (with damage types)
 * - Particle effects
 * - Fire effects
 * - Glow effects
 * - Attribute modifications
 * - Mob summoning
 * - Mana cost and cooldowns
 */
public class SpellManager implements Listener {

    private static SpellManager instance;
    private final Map<String, Long> activeCooldowns = new ConcurrentHashMap<>();
    private final Map<String, BukkitRunnable> activeAttributeModifiers = new ConcurrentHashMap<>();

    public SpellManager() {
        instance = this;
    }

    public static SpellManager getInstance() {
        return instance;
    }

    /**
     * Check if a player can cast a spell (has enough mana and is not on cooldown).
     */
    public boolean canCastSpell(Player p, String spellId) {
        // Check cooldown
        String cooldownKey = p.getUniqueId() + "_" + spellId;
        Long lastCast = activeCooldowns.get(cooldownKey);
        if (lastCast != null) {
            Object cooldownObj = getPathInDB("spells", spellId + ".cooldown");
            if (cooldownObj != null) {
                int cooldownTicks = (int) cooldownObj;
                if (cooldownTicks > 0) {
                    long elapsed = (System.currentTimeMillis() - lastCast) / 50L; // convert to ticks
                    if (elapsed < cooldownTicks) {
                        return false;
                    }
                    activeCooldowns.remove(cooldownKey);
                }
            }
        }

        // Check mana
        Object manaCostObj = getPathInDB("spells", spellId + ".manaCost");
        if (manaCostObj != null) {
            double manaCost = (double) manaCostObj;
            if (manaCost > 0) {
                double currentMana = getDoublePDC(p, "currentMana", 0);
                if (currentMana < manaCost) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Cast a spell for a player.
     */
    public void castSpell(Player p, String spellId, LivingEntity target) {
        if (!canCastSpell(p, spellId)) {
            p.sendMessage(RPGCraft.MM("<red>Cannot cast spell: on cooldown or not enough mana!</red>"));
            return;
        }

        // Deduct mana
        Object manaCostObj = getPathInDB("spells", spellId + ".manaCost");
        if (manaCostObj != null) {
            double manaCost = (double) manaCostObj;
            if (manaCost > 0) {
                double currentMana = getDoublePDC(p, "currentMana", 0);
                setPlayerPDC("currentMana", p, String.valueOf(currentMana - manaCost));
                p.setMetadata("currentMana", new FixedMetadataValue(RPGCraft.getPlugin(), currentMana - manaCost));
            }
        }

        // Set cooldown
        String cooldownKey = p.getUniqueId() + "_" + spellId;
        Object cooldownObj = getPathInDB("spells", spellId + ".cooldown");
        if (cooldownObj != null) {
            int cooldownTicks = (int) cooldownObj;
            if (cooldownTicks > 0) {
                activeCooldowns.put(cooldownKey, System.currentTimeMillis());
            }
        }

        // Get target location (use raycast if no target)
        Location targetLoc = target != null ? target.getLocation() : getRaycastLocation(p);

        // Execute spell effects
        List<Object> effects = getAllNodesInDB("spells", spellId + ".effects");
        if (effects == null) {
            Bukkit.getLogger().warning("Spell '" + spellId + "' has no effects defined!");
            return;
        }

        for (Object effect : effects) {
            String effectType = getPathInDB("spells", spellId + ".effects." + effect + ".type").toString().toLowerCase();
            switch (effectType) {
                case "damage" -> applyDamage(p, spellId, effect.toString(), target, targetLoc);
                case "particle" -> applyParticle(p, spellId, effect.toString(), targetLoc);
                case "fire" -> applyFire(p, spellId, effect.toString(), target);
                case "glow" -> applyGlow(p, spellId, effect.toString(), target);
                case "attribute" -> applyAttribute(p, spellId, effect.toString(), p);
                case "summon" -> applySummon(p, spellId, effect.toString(), targetLoc);
                case "potion" -> applyPotion(p, spellId, effect.toString(), target);
                case "sound" -> applySound(p, spellId, effect.toString(), targetLoc);
                case "knockback" -> applyKnockback(p, spellId, effect.toString(), target);
                default -> Bukkit.getLogger().warning("Unknown spell effect type: " + effectType);
            }
        }

        // Play cast sound if configured
        Object castSound = getPathInDB("spells", spellId + ".castSound");
        if (castSound != null) {
            try {
                Sound sound = Sound.valueOf(castSound.toString().toUpperCase());
                p.getWorld().playSound(p.getLocation(), sound, 1.0f, 1.0f);
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning("Invalid cast sound: " + castSound);
            }
        }
    }

    /**
     * Apply damage effect to a target.
     */
    private void applyDamage(Player caster, String spellId, String effectName, LivingEntity target, Location loc) {
        Object damageObj = getPathInDB("spells", spellId + ".effects." + effectName + ".amount");
        if (damageObj == null) return;

        double damage = (double) damageObj;

        if (target != null && !target.isDead()) {
            target.damage(damage);
        }

        // Apply area damage if configured
        Object radiusObj = getPathInDB("spells", spellId + ".effects." + effectName + ".radius");
        if (radiusObj != null) {
            double radius = (double) radiusObj;
            for (Entity entity : loc.getWorld().getEntities()) {
                if (entity instanceof LivingEntity living
                    && entity != caster
                    && entity.getLocation().distance(loc) <= radius) {
                    living.damage(damage);
                }
            }
        }
    }

    /**
     * Apply particle effect at a location.
     */
    private void applyParticle(Player caster, String spellId, String effectName, Location loc) {
        Object particleObj = getPathInDB("spells", spellId + ".effects." + effectName + ".particle");
        if (particleObj == null) return;

        try {
            Particle particle = Particle.valueOf(particleObj.toString().toUpperCase());

            Object amountObj = getPathInDB("spells", spellId + ".effects." + effectName + ".amount");
            int amount = amountObj != null ? (int) amountObj : 10;

            Object offsetXObj = getPathInDB("spells", spellId + ".effects." + effectName + ".offsetX");
            Object offsetYObj = getPathInDB("spells", spellId + ".effects." + effectName + ".offsetY");
            Object offsetZObj = getPathInDB("spells", spellId + ".effects." + effectName + ".offsetZ");
            double offsetX = offsetXObj != null ? (double) offsetXObj : 0;
            double offsetY = offsetYObj != null ? (double) offsetYObj : 0;
            double offsetZ = offsetZObj != null ? (double) offsetZObj : 0;

            Object speedObj = getPathInDB("spells", spellId + ".effects." + effectName + ".speed");
            double speed = speedObj != null ? (double) speedObj : 0.2;

            // Get particle color if configured
            Object colorObj = getPathInDB("spells", spellId + ".effects." + effectName + ".color");
            Object data = null;
            if (colorObj != null) {
                String[] rgb = colorObj.toString().split(",");
                if (rgb.length == 3) {
                    data = Color.fromRGB(
                        Integer.parseInt(rgb[0].trim()),
                        Integer.parseInt(rgb[1].trim()),
                        Integer.parseInt(rgb[2].trim())
                    );
                }
            }

            Location particleLoc = loc.clone().add(offsetX, offsetY, offsetZ);

            if (data != null) {
                particleLoc.getWorld().spawnParticle(particle, particleLoc, amount, 0.1, 0.1, 0.1, speed, data);
            } else {
                particleLoc.getWorld().spawnParticle(particle, particleLoc, amount, 0.1, 0.1, 0.1, speed);
            }

            // Ring effect
            Object ringObj = getPathInDB("spells", spellId + ".effects." + effectName + ".ring");
            if (ringObj != null) {
                double ringRadius = (double) ringObj;
                spawnParticleRing(particleLoc.getWorld(), particleLoc, particle, ringRadius, amount / 10, data, speed);
            }
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("Invalid particle type: " + particleObj);
        }
    }

    /**
     * Spawn particles in a ring pattern.
     */
    private void spawnParticleRing(World world, Location center, Particle particle, double radius, int amount, Object data, double speed) {
        int steps = Math.max(10, amount);
        for (int i = 0; i < steps; i++) {
            double angle = (2 * Math.PI * i) / steps;
            double x = center.getX() + Math.cos(angle) * radius;
            double z = center.getZ() + Math.sin(angle) * radius;
            Location loc = new Location(world, x, center.getY(), z);
            if (data != null) {
                world.spawnParticle(particle, loc, 1, 0, 0, 0, speed, data);
            } else {
                world.spawnParticle(particle, loc, 1, 0, 0, 0, speed);
            }
        }
    }

    /**
     * Apply fire effect to a target.
     */
    private void applyFire(Player caster, String spellId, String effectName, LivingEntity target) {
        if (target == null || target.isDead()) return;

        Object durationObj = getPathInDB("spells", spellId + ".effects." + effectName + ".duration");
        int durationTicks = durationObj != null ? (int) durationObj : 100; // 5 seconds default

        target.setFireTicks(durationTicks);

        // Apply fire aspect-like damage over time
        Object dotObj = getPathInDB("spells", spellId + ".effects." + effectName + ".damageOverTime");
        if (dotObj != null) {
            double dotAmount = (double) dotObj;
            int interval = 20; // every second
            new BukkitRunnable() {
                int ticks = 0;
                @Override
                public void run() {
                    if (target.isDead() || !target.isValid() || ticks >= durationTicks / interval) {
                        this.cancel();
                        return;
                    }
                    target.damage(dotAmount);
                    ticks++;
                }
            }.runTaskTimer(RPGCraft.getPlugin(), interval, interval);
        }
    }

    /**
     * Apply glow effect to a target.
     */
    private void applyGlow(Player caster, String spellId, String effectName, LivingEntity target) {
        if (target == null || target.isDead()) return;

        Object durationObj = getPathInDB("spells", spellId + ".effects." + effectName + ".duration");
        int durationTicks = durationObj != null ? (int) durationObj : 200; // 10 seconds default

        target.setGlowing(true);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (target.isValid()) {
                    target.setGlowing(false);
                }
                this.cancel();
            }
        }.runTaskLater(RPGCraft.getPlugin(), durationTicks);
    }

    /**
     * Apply attribute modification to an entity.
     */
    private void applyAttribute(Player caster, String spellId, String effectName, LivingEntity target) {
        if (target == null || target.isDead()) return;

        Object attributeObj = getPathInDB("spells", spellId + ".effects." + effectName + ".attribute");
        if (attributeObj == null) return;

        try {
            Attribute attribute = Attribute.valueOf(attributeObj.toString().toUpperCase());
            AttributeInstance attrInstance = target.getAttribute(attribute);
            if (attrInstance == null) return;

            Object amountObj = getPathInDB("spells", spellId + ".effects." + effectName + ".amount");
            double amount = amountObj != null ? (double) amountObj : 0;

            Object durationObj = getPathInDB("spells", spellId + ".effects." + effectName + ".duration");
            int durationTicks = durationObj != null ? (int) durationObj : 6000; // 5 minutes default

            String modifierKey = "spell_" + spellId + "_" + UUID.randomUUID();
            AttributeModifier modifier = new AttributeModifier(
                NamespacedKey.minecraft(modifierKey),
                amount,
                AttributeModifier.Operation.ADD_NUMBER
            );

            attrInstance.addModifier(modifier);

            // Remove modifier after duration
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (target.isValid()) {
                        attrInstance.removeModifier(modifier);
                    }
                    this.cancel();
                }
            }.runTaskLater(RPGCraft.getPlugin(), durationTicks);

        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("Invalid attribute: " + attributeObj);
        }
    }

    /**
     * Summon a mob at a location.
     */
    private void applySummon(Player caster, String spellId, String effectName, Location loc) {
        Object mobTypeObj = getPathInDB("spells", spellId + ".effects." + effectName + ".mobType");
        if (mobTypeObj == null) return;

        try {
            EntityType entityType = EntityType.valueOf(mobTypeObj.toString().toUpperCase());

            Object amountObj = getPathInDB("spells", spellId + ".effects." + effectName + ".amount");
            int amount = amountObj != null ? (int) amountObj : 1;

            Object customMobObj = getPathInDB("spells", spellId + ".effects." + effectName + ".customMobId");

            for (int i = 0; i < amount; i++) {
                Location spawnLoc = loc.clone().add(
                    (Math.random() - 0.5) * 2,
                    0,
                    (Math.random() - 0.5) * 2
                );

                if (customMobObj != null) {
                    // Try to spawn as custom mob via mobs system
                    String customMobId = customMobObj.toString();
                    LivingEntity mob = mobs.getEntity(customMobId, spawnLoc);
                    if (mob == null) {
                        mob = (LivingEntity) spawnLoc.getWorld().spawnEntity(spawnLoc, entityType);
                        if (mob != null) {
                            PDCHelper.setEntityPDC("type", mob, customMobId);
                        }
                    }
                } else {
                    LivingEntity mob = (LivingEntity) spawnLoc.getWorld().spawnEntity(spawnLoc, entityType);
                    if (mob != null) {
                        // Set owner for tamed behavior
                        Object ownerObj = getPathInDB("spells", spellId + ".effects." + effectName + ".owner");
                        if (ownerObj != null && "caster".equals(ownerObj.toString().toLowerCase())) {
                            mob.setCustomName(caster.getName());
                            mob.setCustomNameVisible(true);
                        }
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("Invalid mob type: " + mobTypeObj);
        }
    }

    /**
     * Apply potion effect to a target.
     */
    private void applyPotion(Player caster, String spellId, String effectName, LivingEntity target) {
        if (target == null || target.isDead()) return;

        Object effectTypeObj = getPathInDB("spells", spellId + ".effects." + effectName + ".effectType");
        if (effectTypeObj == null) return;

        try {
            PotionEffectType effectType = PotionEffectType.getByName(effectTypeObj.toString().toUpperCase());
            if (effectType == null) return;

            Object durationObj = getPathInDB("spells", spellId + ".effects." + effectName + ".duration");
            int durationTicks = durationObj != null ? (int) durationObj : 100;

            Object amplifierObj = getPathInDB("spells", spellId + ".effects." + effectName + ".amplifier");
            int amplifier = amplifierObj != null ? (int) amplifierObj : 0;

            Object ambientObj = getPathInDB("spells", spellId + ".effects." + effectName + ".ambient");
            boolean ambient = ambientObj != null && (boolean) ambientObj;

            Object particlesObj = getPathInDB("spells", spellId + ".effects." + effectName + ".particles");
            boolean showParticles = particlesObj != null && (boolean) particlesObj;

            target.addPotionEffect(new PotionEffect(
                effectType, durationTicks, amplifier, ambient, showParticles, true
            ));
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("Invalid potion effect type: " + effectTypeObj);
        }
    }

    /**
     * Play a sound at a location.
     */
    private void applySound(Player caster, String spellId, String effectName, Location loc) {
        Object soundObj = getPathInDB("spells", spellId + ".effects." + effectName + ".sound");
        if (soundObj == null) return;

        try {
            Sound sound = Sound.valueOf(soundObj.toString().toUpperCase());

            Object volumeObj = getPathInDB("spells", spellId + ".effects." + effectName + ".volume");
            float volume = volumeObj != null ? ((Number) volumeObj).floatValue() : 1.0f;

            Object pitchObj = getPathInDB("spells", spellId + ".effects." + effectName + ".pitch");
            float pitch = pitchObj != null ? ((Number) pitchObj).floatValue() : 1.0f;

            loc.getWorld().playSound(loc, sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("Invalid sound: " + soundObj);
        }
    }

    /**
     * Apply knockback to a target.
     */
    private void applyKnockback(Player caster, String spellId, String effectName, LivingEntity target) {
        if (target == null || target.isDead()) return;

        Object strengthObj = getPathInDB("spells", spellId + ".effects." + effectName + ".strength");
        double strength = strengthObj != null ? (double) strengthObj : 1.0;

        Object heightObj = getPathInDB("spells", spellId + ".effects." + effectName + ".height");
        double height = heightObj != null ? (double) heightObj : 0.5;

        Vector knockbackDir;
        if (caster != null) {
            knockbackDir = target.getLocation().toVector().subtract(caster.getLocation().toVector()).normalize();
        } else {
            knockbackDir = new Vector(0, 0, 1);
        }

        Vector velocity = knockbackDir.multiply(strength);
        velocity.setY(height);
        target.setVelocity(velocity);
    }

    /**
     * Get the location where the player is looking (raycast).
     */
    private Location getRaycastLocation(Player p) {
        RayTraceResult result = p.getWorld().rayTraceBlocks(
            p.getEyeLocation(),
            p.getEyeLocation().getDirection(),
            10.0,
            org.bukkit.FluidCollisionMode.NEVER,
            false
        );

        if (result != null) {
            return result.getHitPosition().toLocation(p.getWorld());
        }
        return p.getEyeLocation().add(p.getEyeLocation().getDirection().multiply(5));
    }

    /**
     * Event handler for right-clicking with a spell item.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player p = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || item.getType().isAir()) return;

        Object customItemName = PDCHelper.getItemPDC("customItemName", item);
        if (customItemName == null) return;

        // Check if this item has a spell assigned
        Object spellId = getPathInDB("itemDB", customItemName + ".spell");
        if (spellId == null) return;

        event.setCancelled(true);

        // Find target entity
        LivingEntity target = null;
        RayTraceResult result = p.getWorld().rayTraceEntities(
            p.getEyeLocation(),
            p.getEyeLocation().getDirection(),
            10.0
        );
        if (result != null && result.getHitEntity() instanceof LivingEntity) {
            target = (LivingEntity) result.getHitEntity();
        }

        castSpell(p, spellId.toString(), target);
    }
}
