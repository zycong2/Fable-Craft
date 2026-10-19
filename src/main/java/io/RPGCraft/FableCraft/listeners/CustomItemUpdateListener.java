
package io.RPGCraft.FableCraft.listeners;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.core.Helpers.PDCHelper;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public class CustomItemUpdateListener implements Listener {

  private final RPGCraft plugin;

  public CustomItemUpdateListener(RPGCraft plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    plugin.getServer().getScheduler().runTask(plugin, () -> {
      if (!player.isOnline()) {
        return;
      }

      updateInventory(player);
    });
  }

  private void updateInventory(Player player) {
    ItemStack[] contents = player.getInventory().getContents();

    for (int slot = 0; slot < contents.length; slot++) {
      ItemStack oldItem = contents[slot];
      if (oldItem == null || oldItem.getType().isAir()) {
        continue;
      }

      String customName = getCustomItemName(oldItem);

      if (customName == null || customName.isBlank()) {
        continue;
      }

      ItemStack updated = yamlManager.getItem(customName);

      if (updated == null) {
        player.getInventory().setItem(slot, null);

        plugin.getLogger().info(
          "[CustomItemUpdate] Removed obsolete custom item '"
            + customName + "' from " + player.getName()
        );

        continue;
      }
      updated.setAmount(oldItem.getAmount());

      if (oldItem.isSimilar(updated)) {
        continue;
      }

      player.getInventory().setItem(slot, updated);
    }
  }

  private String getCustomItemName(ItemStack item) {
    if (!item.hasItemMeta()) {
      return null;
    }

    NamespacedKey key = new NamespacedKey(plugin, "customItemName");

    return item.getItemMeta()
      .getPersistentDataContainer()
      .get(key, PersistentDataType.STRING);
  }
}
