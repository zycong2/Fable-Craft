package io.RPGCraft.FableCraft.Utils.GUI;

import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.function.Consumer;

import static io.RPGCraft.FableCraft.RPGCraft.MM;
import static org.bukkit.Bukkit.getServer;

@SuppressWarnings("UnstableApiUsage")
public class GUIItem {

    private Material material;
    private String name;
    private Integer amount;
    private List<String> lore;
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
        ItemStack item = new ItemStack(this.material);
        item.setItemMeta(this.meta);
        ItemMeta meta = item.getItemMeta();
        ItemLore lore = ItemLore.lore(MM(this.lore).stream().toList());
        item.setData(DataComponentTypes.CUSTOM_NAME, MM(name));
        item.setData(DataComponentTypes.LORE, lore);
        enchantments.forEach(e -> {
            meta.addEnchant(e.getEnchantment(), e.getLevel(), e.getBoo());
        });
        meta.setCustomModelData(customModelData);
        item.setItemMeta(meta);
      for(Map.Entry<DataComponentType, Object> data : this.data.entrySet()){
        if(data.getKey() instanceof DataComponentType.Valued<?> valued) {
          applyComponent(item, valued, data.getValue());
        } else if (data.getKey() instanceof DataComponentType.NonValued nonValued) {
          applyComponent(item, nonValued);
        }else {
          getServer().getLogger().warning("I absolutely don't know how you actually got here if you're able because this likely is impossible");
        }
      }
        item.setAmount(amount);

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
    return name;
  }
  public GUIItem name(String name) {
    this.name = name;
    return this;
  }

  public Integer amount() {
    return amount;
  }
  public GUIItem amount(Integer amount) {
    this.amount = amount;
    return this;
  }

  public List<String> lore() {
    return lore;
  }
  public GUIItem lore(List<String> lore) {
    this.lore = lore;
    return this;
  }
  public GUIItem lore(String... lore) {
    List<String> process = Arrays.stream(lore).toList();
    this.lore = process;
    return this;
  }

  public List<Enchants> enchantments() {
    return enchantments;
  }
  public GUIItem enchantments(List<Enchants> enchantments) {
    this.enchantments = enchantments;
    return this;
  }

  public Integer customModelData() {
    return customModelData;
  }
  public GUIItem customModelData(Integer customModelData) {
    this.customModelData = customModelData;
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
   * that's if you use the other method for enchantments/name
   * it WILL be overridden by the data you input here
   *
   * @param data the valued DataComponentType for the data
   * @param value the value of the data. what are you stupid?
   */
  public <T> GUIItem data(DataComponentType.Valued data, T value) {this.data.put(data, value);return this;}

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
        List<Enchants> list = new ArrayList<>();
        item.getEnchantments().forEach(
                (enchantment, integer) -> {
                    list.add(new Enchants().setEnchantment(enchantment).setLevel(integer));
                }
        );
        GUIItem output = new GUIItem(item.getType());
    ItemMeta meta = item.getItemMeta();
    if(meta != null) {
          output.material(item.getType() != null ? item.getType() : Material.DIRT)
            .name(meta.getDisplayName())
            .lore(meta.getLore())
            .meta(meta)
            .enchantments(list);
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
        List<Enchants> list = new ArrayList<>();
        item.getEnchantments().forEach(
                (enchantment, integer) -> {
                    list.add(new Enchants().setEnchantment(enchantment).setLevel(integer));
                }
        );
        GUIItem output = new GUIItem(item.getType());
    ItemMeta meta = item.getItemMeta();
    if(meta != null) {
          output.material(item.getType() != null ? item.getType() : Material.DIRT)
            .name(meta.getDisplayName())
            .lore(meta.getLore())
            .customModelData(meta.getCustomModelData())
            .meta(meta)
            .enchantments(list);
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
