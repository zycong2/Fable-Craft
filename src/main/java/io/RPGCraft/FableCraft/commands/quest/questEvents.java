package io.RPGCraft.FableCraft.commands.quest;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import static io.RPGCraft.FableCraft.Utils.Utils.isCitizensNPC;
import static io.RPGCraft.FableCraft.commands.quest.questManager.addProgress;
import static io.RPGCraft.FableCraft.commands.quest.questManager.getActiveQuests;
import static io.RPGCraft.FableCraft.commands.quest.questManager.getStep;
import static io.RPGCraft.FableCraft.commands.quest.questManager.stepOption;

public class questEvents implements Listener {

  @EventHandler
  public void onEntityDeath(EntityDeathEvent event) {
    LivingEntity entity = event.getEntity();
    Player killer = entity.getKiller();
    if (killer == null) return;

    for (String quest : getActiveQuests(killer)) {
      int step = getStep(killer, quest);

      Object type = stepOption(quest, step, "type");
      if (type == null || !type.toString().equalsIgnoreCase("kill")) continue;

      Object wanted = stepOption(quest, step, "entity");
      if (wanted == null) continue;

      boolean matches = wanted.toString().equalsIgnoreCase("NPC")
        ? isCitizensNPC(entity)
        : entity.getType().name().equalsIgnoreCase(wanted.toString());
      if (!matches) continue;

      addProgress(killer, quest, 1);
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerPickUp(EntityPickupItemEvent event) {
    if (!(event.getEntity() instanceof Player p)) return;

    ItemStack stack = event.getItem().getItemStack();
    int picked = stack.getAmount() - event.getRemaining();
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

