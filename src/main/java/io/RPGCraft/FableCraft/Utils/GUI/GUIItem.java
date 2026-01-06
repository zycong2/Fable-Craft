package io.RPGCraft.FableCraft.Utils.GUI;

import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import io.papermc.paper.datacomponent.item.ItemEnchantments;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static io.RPGCraft.FableCraft.RPGCraft.MM;
import static org.bukkit.Bukkit.getServer;

@SuppressWarnings("UnstableApiUsage")
public class GUIItem {

    private Material material;
    private String name;
    private Integer amount;
    private List<String> lore = List.of();
    private List<Enchants> enchantments;
    private Map<DataComponentType, Object> data = new HashMap<>();
    private Integer customModelData;
    private Consumer<ClickContext> clickEvent;
    private ItemMeta meta;

    public GUIItem(Material material){
      this.material = material;
    }
    public GUIItem(){}

    public ItemStack toItemStack(){
      ItemStack item = ItemStack.of(this.material);
      item.setItemMeta(this.meta);
      ItemMeta meta = item.getItemMeta();
      ItemLore lore = ItemLore.lore(MM(this.lore));
      try {
        item.setData(DataComponentTypes.LORE, lore);
        enchantments.forEach(e -> {
          meta.addEnchant(e.getEnchantment(), e.getLevel(), e.getBoo());
        });
      } catch (NullPointerException ignored) {}
      meta.setCustomModelData(customModelData);
      item.setItemMeta(meta);
      for(Map.Entry<DataComponentType, Object> data : this.data.entrySet()){
        if(data.getKey() instanceof DataComponentType.Valued<?> valued) {
          if(data.getValue() == null) continue;
          applyComponent(item, valued, data.getValue());
        } else if (data.getKey() instanceof DataComponentType.NonValued nonValued) {
          applyComponent(item, nonValued);
        }else {
          getServer().getLogger().warning("I absolutely don't know how you actually got here if you're able because this likely is impossible");
        }
      }
      try {
        item.setAmount(amount);
      } catch (NullPointerException ignored) {}

        return item;
    }

  public <T> void applyComponent(ItemStack item, DataComponentType.Valued<?> type, T value) {
    item.setData((DataComponentType.Valued<T>) type, value);
  }
  public void applyComponent(ItemStack item, DataComponentType.NonValued type) {
    item.setData(type);
  }

  public Material material() {
    return material;
  }
  public GUIItem material(Material material) {
    this.material = material;
    return this;
  }

  public String name() {
    return data(DataComponentTypes.CUSTOM_NAME);
  }
  public GUIItem name(String name) {
    data(DataComponentTypes.CUSTOM_NAME, MM(name));
    return this;
  }
  public GUIItem name(Component name) {
    data(DataComponentTypes.CUSTOM_NAME, name);
    return this;
  }

  public Integer amount() {
    return amount;
  }
  public GUIItem amount(Integer amount) {
    this.amount = amount;
    return this;
  }

  public ItemLore lore() {
    return data(DataComponentTypes.LORE);
  }
  public GUIItem lore(ItemLore lore) {
      data(DataComponentTypes.LORE, lore);
      return this;
  }
  public GUIItem lore(List<String> lore) {
    ItemLore l = ItemLore.lore().lines(MM(lore)).build();
    data(DataComponentTypes.LORE, l);
    return this;
  }
  public GUIItem lore(String... lore) {
    ItemLore l = ItemLore.lore().lines(MM(Arrays.stream(lore).toList())).build();
    data(DataComponentTypes.LORE, l);
    return this;
  }

  public ItemEnchantments enchantments() {
    return data(DataComponentTypes.ENCHANTMENTS);
  }
  public GUIItem enchantments(ItemEnchantments enchantments) {
      data(DataComponentTypes.ENCHANTMENTS, enchantments);
      return this;
  }

  public CustomModelData customModelData() {
    return data(DataComponentTypes.CUSTOM_MODEL_DATA);
  }
  public GUIItem customModelData(CustomModelData CMD) {
      data(DataComponentTypes.CUSTOM_MODEL_DATA, CMD);
      return this;
  }

  public Consumer<ClickContext> clickEvent() {
    return clickEvent;
  }
  public GUIItem clickEvent(Consumer<ClickContext> clickEvent) {
    this.clickEvent = clickEvent;
    return this;
  }

  public ItemMeta meta() {
    return meta;
  }
  public GUIItem meta(ItemMeta meta) {
    this.meta = meta;
    return this;
  }

  /**
   * Settings data will take priority over other meaning
   * if you use the other method for enchantments/name
   * it WILL be overridden by the data you input here
   *
   * @param data the valued DataComponentType for the data
   * @param value the value of the data. what are you stupid?
   */
  public <T> GUIItem data(DataComponentType.Valued data, T value) {this.data.put(data, value);return this;}

  /**
   * Retrieving the value of the data from the DataMap
   * This will return a generic because I don't fucking know
   * what it returns duh that's literally why generic exist
   *
   * @param data the valued DataComponentType for the data
   */
  public <T> T data(DataComponentType.Valued data) {return (T) this.data.get(data);}

  /**
   * Settings data will take priority over other meaning
   * that's if you use the other method for enchantments/name
   * it WILL be overridden by the data you input here
   *
   * @param data the nonvalued DataComponentType for the data
   */
  public GUIItem data(DataComponentType.NonValued data) {this.data.put(data, null);return this;}

  public record ClickContext(Player player, ClickType clickType, GUI gui, GUIItem clickedItem) {}

  /**
   * put in a ItemStack, and it will be converted to GUIItem
   * to make it easier to change ItemStack to GUIItem
   *
   * @param item The ItemStack to be converted
   * @return A GUIItem with the same data as the input item
   */
  @SuppressWarnings({"deprecation"})
  public static GUIItem ItemStackToGUIItem(ItemStack item){
    ItemEnchantments enchant = item.getData(DataComponentTypes.ENCHANTMENTS);
    GUIItem output = new GUIItem(item.getType());
    ItemMeta meta = item.getItemMeta();
    if(meta != null) {
          output.material(item.getType() != null ? item.getType() : Material.DIRT)
            .name(item.getData(DataComponentTypes.CUSTOM_NAME))
            .lore(item.getData(DataComponentTypes.LORE))
            .customModelData(item.getData(DataComponentTypes.CUSTOM_MODEL_DATA))
            .meta(meta)
            .enchantments(enchant);
        }else{
      getServer().getLogger().warning("Meta is null!!!!!!");
    }
        item.getDataTypes().forEach(dt -> {
          if(dt instanceof DataComponentType.Valued<?> vct) {
            Object data = item.getData(vct);
            output.data(vct, data);
          } else if (dt instanceof DataComponentType.NonValued NCT) {
            output.data(NCT);
          }
        });
        return output;
  }

  /**
   * put in a ItemStack, and it will be converted to GUIItem
   * to make it easier to change ItemStack to GUIItem
   *
   * @param item The ItemStack to be converted
   * @return A GUIItem with the same data as the input item
   */
  @SuppressWarnings({"deprecation"})
  public static GUIItem IStoGUIItem(ItemStack item){
    ItemEnchantments enchant = item.getData(DataComponentTypes.ENCHANTMENTS);
        GUIItem output = new GUIItem(item.getType());
    ItemMeta meta = item.getItemMeta();
    if(meta != null) {
          output.material(item.getType() != null ? item.getType() : Material.DIRT)
            .name(item.getData(DataComponentTypes.CUSTOM_NAME))
            .lore(item.getData(DataComponentTypes.LORE))
            .customModelData(item.getData(DataComponentTypes.CUSTOM_MODEL_DATA))
            .meta(meta)
            .enchantments(enchant);
        }
        item.getDataTypes().forEach(dt -> {
          if(dt instanceof DataComponentType.Valued<?> vct) {
            Object data = item.getData(vct);
            output.data(vct, data);
          } else if (dt instanceof DataComponentType.NonValued NCT) {
            output.data(NCT);
          }
        });
        return output;
  }
}
