package io.RPGCraft.FableCraft.commands.quest;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import static io.RPGCraft.FableCraft.Utils.Utils.isCitizensNPC;
import static io.RPGCraft.FableCraft.commands.quest.questManager.addProgress;
import static io.RPGCraft.FableCraft.commands.quest.questManager.debug;
import static io.RPGCraft.FableCraft.commands.quest.questManager.getActiveQuests;
import static io.RPGCraft.FableCraft.commands.quest.questManager.getStep;
import static io.RPGCraft.FableCraft.commands.quest.questManager.stepOption;

public class questEvents implements Listener {

  @EventHandler
  public void onEntityDeath(EntityDeathEvent event) {
    LivingEntity entity = event.getEntity();
    Player killer = resolveKiller(entity);

    if (killer != null || isNPC(entity)) {
      debug("death: " + entity.getType() + " name=" + entity.getName()
        + " npc=" + isNPC(entity)
        + " killer=" + (killer == null ? "none" : killer.getName()));
    }
    if (killer == null) return;

    for (String quest : getActiveQuests(killer)) {
      int step = getStep(killer, quest);

      Object wanted = stepOption(quest, step, "entity");
      if (wanted == null) continue;

      String target = wanted.toString();
      boolean matches;

      if (isNPC(entity)) {
        // Citizens NPC: match its name, or allow "NPC" for any NPC.
        matches = target.equalsIgnoreCase("NPC")
          || entity.getName().equalsIgnoreCase(target);
      } else {
        // Normal Minecraft entity: match its EntityType.
        matches = !target.equalsIgnoreCase("NPC")
          && entity.getType().name().equalsIgnoreCase(target);
      }


      if (!matches) continue;

      addProgress(killer, quest, 1);

    }
  }

  /**
   * The player responsible for this death. getKiller() is null when damage was
   * dealt through a custom damage system, so fall back to the last damage cause.
   */
  private static Player resolveKiller(LivingEntity entity) {
    Player killer = entity.getKiller();
    if (killer != null) return killer;

    if (entity.getLastDamageCause() instanceof EntityDamageByEntityEvent byEntity) {
      Entity damager = byEntity.getDamager();
      if (damager instanceof Player direct) return direct;
      if (damager instanceof Projectile proj && proj.getShooter() instanceof Player shooter) return shooter;
    }
    return null;
  }

  private static boolean isNPC(Entity entity) {
    if (isCitizensNPC(entity) || entity.hasMetadata("NPC")) return true;

    // Citizens can despawn the NPC (and strip its marker) before this listener runs.
    // A PLAYER-type entity that isn't an online player can only be an NPC.
    return entity instanceof Player pl && Bukkit.getPlayer(pl.getUniqueId()) == null;
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerPickUp(EntityPickupItemEvent event) {
    if (!(event.getEntity() instanceof Player p)) return;

    ItemStack stack = event.getItem().getItemStack();
    int picked = stack.getAmount() - event.getRemaining(); // what actually fit in the inventory
    if (picked <= 0) return;

    for (String quest : getActiveQuests(p)) {
      int step = getStep(p, quest);

      Object type = stepOption(quest, step, "type");
      if (type == null || !type.toString().equalsIgnoreCase("get")) continue;

      Object wanted = stepOption(quest, step, "item");
      if (wanted == null || !stack.getType().name().equalsIgnoreCase(wanted.toString())) continue;

      addProgress(p, quest, picked);
    }
  }
}

