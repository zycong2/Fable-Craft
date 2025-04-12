package io.RPGCraft.FableCraft.core;

import io.RPGCraft.FableCraft.RPGCraft;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class PDCHelper {
    public static void setPlayerPDC(String keyString, Player p, String data) {
        NamespacedKey key = new NamespacedKey(RPGCraft.getPlugin(), keyString);
        p.getPersistentDataContainer().set(key, PersistentDataType.STRING, data);
    }

    public static String getPlayerPDC(String keyString, Player p) {
        NamespacedKey key = new NamespacedKey(RPGCraft.getPlugin(), keyString);
        PersistentDataContainer container = p.getPersistentDataContainer();
        return container.get(key, PersistentDataType.STRING);
    }

    public static void setNPCPDC(String keyString, NPC npc, String data) {
        NamespacedKey key = new NamespacedKey(RPGCraft.getPlugin(), keyString);
        if (npc.getEntity().getType().equals(EntityType.PLAYER)){
          Player p = (Player) npc;
          p.getPersistentDataContainer().set(key, PersistentDataType.STRING, data);
        }
    }

    public static String getNPCPDC(String keyString, NPC npc) {
        NamespacedKey key = new NamespacedKey(RPGCraft.getPlugin(), keyString);
      if (npc.getEntity().getType().equals(EntityType.PLAYER)) {
        Player p = (Player) npc;
        PersistentDataContainer container = p.getPersistentDataContainer();
        return container.get(key, PersistentDataType.STRING);
      }
      return null;
    }


    public static void setItemPDC(String keyString, ItemStack i, Object data) {
        NamespacedKey key = new NamespacedKey(RPGCraft.getPlugin(), keyString);
        ItemMeta meta = i.getItemMeta();
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, data.toString());
        i.setItemMeta(meta);
    }

    public static String getItemPDC(String keyString, ItemStack i) {
        NamespacedKey key = new NamespacedKey(RPGCraft.getPlugin(), keyString);
        PersistentDataContainer container = i.getItemMeta().getPersistentDataContainer();
        return container.get(key, PersistentDataType.STRING);
    }


    public static void setEntityPDC(String keyString, Entity p, String data) {
        NamespacedKey key = new NamespacedKey(RPGCraft.getPlugin(), keyString);
        p.getPersistentDataContainer().set(key, PersistentDataType.STRING, data);
    }

    public static String getEntityPDC(String keyString, Entity p) {
        NamespacedKey key = new NamespacedKey(RPGCraft.getPlugin(), keyString);
        PersistentDataContainer container = p.getPersistentDataContainer();
        return container.get(key, PersistentDataType.STRING);
    }


    public static void setBlockPDC(String keyString, Block block, String data) {
        NamespacedKey key = new NamespacedKey(RPGCraft.getPlugin(), keyString);
        if (block.getState() instanceof Chest chest) {
            chest.getPersistentDataContainer().set(key, PersistentDataType.STRING, data);
            chest.update();
        }
    }

    public static String getBlockPDC(String keyString, Block block) {
        NamespacedKey key = new NamespacedKey(RPGCraft.getPlugin(), keyString);
        if (block.getState() instanceof Chest chest) {
            PersistentDataContainer container = chest.getPersistentDataContainer();
            return container.get(key, PersistentDataType.STRING);
        }
        return null;
    }
}
