package io.RPGCraft.FableCraft.core.Helpers;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
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

    /** Returns the PDC value parsed as an int, or {@code fallback} if missing or invalid. */
    public static int getIntPDC(Player p, String keyString, int fallback) {
        String value = getPlayerPDC(keyString, p);
        if (value == null) return fallback;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /** Returns the PDC value parsed as a double, or {@code fallback} if missing or invalid. */
    public static double getDoublePDC(Player p, String keyString, double fallback) {
        String value = getPlayerPDC(keyString, p);
        if (value == null) return fallback;
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static void setNPCPDC(String keyString, Entity npc, Object data) {
      yamlManager.getInstance().setOption("data", "npc." + npc.getUniqueId() + "." + keyString, data);
    }

    public static Object getNPCPDC(String keyString, Entity npc) {
      return yamlManager.getInstance().getOption("data", "npc." + npc.getUniqueId() + "." + keyString);
    }


    public static ItemStack setItemPDC(String keyString, ItemStack i, Object data) {
        NamespacedKey key = new NamespacedKey(RPGCraft.getPlugin(), keyString);
        ItemMeta meta = i.getItemMeta();
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, data.toString());
        i.setItemMeta(meta);
        return i;
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
