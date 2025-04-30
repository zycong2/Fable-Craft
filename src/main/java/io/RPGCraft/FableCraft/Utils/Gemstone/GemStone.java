package io.RPGCraft.FableCraft.Utils.Gemstone;

import io.RPGCraft.FableCraft.Utils.Placeholders.PlaceholderTypes.PlayerPlaceholders;
import io.RPGCraft.FableCraft.Utils.Placeholders.PlaceholderUtils.Placeholder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.RPGCraft.FableCraft.RPGCraft.ColorizeList;
import static io.RPGCraft.FableCraft.RPGCraft.ColorizeReString;

public class GemStone {
  private static final Map<ItemStack, Method> Redstone = new HashMap<>();
  private static final Map<ItemStack, Method> Bluestone = new HashMap<>();
  private static final Map<ItemStack, Method> Pinkstone = new HashMap<>();

  public GemStone() {
    registerPlaceholders(Gemstones.class);
  }

  public static <T> void registerPlaceholders(Class<?> c) {
    for (Method m : c.getDeclaredMethods()) {
      if (m.isAnnotationPresent(GemstoneAnnotation.class)) {
        GemstoneAnnotation ann = m.getAnnotation(GemstoneAnnotation.class);
        switch (ann.type()){
          case RED -> {
            ItemStack i = new ItemStack(Material.RED_DYE);
            ItemMeta meta = i.getItemMeta();
            meta.setDisplayName(ColorizeReString(ann.name()));
            meta.setLore(ColorizeList(List.of(
                ann.name() + " Gemstone", "&7 ", "&8Drag me onto an item to apply the effect", "&8Right click to view the effect"
            )));
            i.setItemMeta(meta);
            Redstone.put(i, m);
          }
          case BLUE -> {
            ItemStack i = new ItemStack(Material.BLUE_DYE);
            ItemMeta meta = i.getItemMeta();
            meta.setDisplayName(ColorizeReString(ann.name()));
            meta.setLore(ColorizeList(List.of(
                ann.name() + " Gemstone", "&7 ", "&8Drag me onto an item to apply the effect", "&8Right click to view the effect"
            )));
            i.setItemMeta(meta);
            Bluestone.put(i, m);
          }
          case PINK -> {
            ItemStack i = new ItemStack(Material.PINK_DYE);
            ItemMeta meta = i.getItemMeta();
            meta.setDisplayName(ColorizeReString(ann.name()));
            meta.setLore(ColorizeList(List.of(
                ann.name() + " Gemstone", "&7 ", "&8Drag me onto an item to apply the effect", "&8Right click to view the effect"
            )));
            i.setItemMeta(meta);
            Pinkstone.put(i, m);
          }
        }
      }
    }
  }

}
