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

@SuppressWarnings("UnstableApiUsage")
public class GUIItem {

    private Material material;
    private String name;
    private Integer amount;
    private List<String> lore;
    private List<Enchants> enchantments;
    private Map<DataComponentType.Valued, Object> data = new HashMap<>();
    private Integer customModelData;
    private Consumer<ClickContext> clickEvent;;

    public ItemStack toItemStack(){
        ItemStack item = new ItemStack(this.material);
        ItemMeta meta = item.getItemMeta();
        ItemLore lore = ItemLore.lore(MM(this.lore));
        item.setData(DataComponentTypes.CUSTOM_NAME, MM(name));
        item.setData(DataComponentTypes.LORE, lore);
        enchantments.forEach(e -> {
            meta.addEnchant(e.getEnchantment(), e.getLevel(), e.getBoo());
        });
        for(Map.Entry<DataComponentType.Valued, Object> data : this.data.entrySet()){
            item.setData(data.getKey(), data.getValue());
        }
        meta.setCustomModelData(customModelData);
        item.setItemMeta(meta);
        item.setAmount(amount);

        return item;
    }

    public Material getMaterial() {return material;}

    public GUIItem setMaterial(Material material) {this.material = material;return this;}

    public String getName() {return name;}

    public GUIItem setName(String name) {this.name = name;return this;}

    public List<String> getLore() {return lore;}

    public GUIItem setLore(List<String> lore) {this.lore = lore;return this;}

    public List<Enchants> getEnchantments() {return enchantments;}
    public GUIItem setEnchantments(List<Enchants> enchantments) {this.enchantments = enchantments;return this;}

    public Integer getCustomModelData() {return customModelData;}

    public GUIItem setCustomModelData(Integer customModelData) {this.customModelData = customModelData;return this;}

    public Consumer<ClickContext> getClickEvent() {return clickEvent;}

    public GUIItem setClickEvent(Consumer<ClickContext> clickEvent) {this.clickEvent = clickEvent;return this;}

    public GUIItem setAdditionalData(DataComponentType.Valued data, Object value) {this.data.put(data, value);return this;}


    public record ClickContext(Player player, ClickType clickType, GUI gui) {}

    public static GUIItem ItemStackToGUIItem(ItemStack item){
        List<Enchants> list = new ArrayList<>();
        item.getEnchantments().forEach(
                (enchantment, integer) -> {
                    list.add(new Enchants().setEnchantment(enchantment).setLevel(integer));
                }
        );
        GUIItem output = new GUIItem()
                .setMaterial(item.getType() != null ? item.getType() : Material.DIRT)
                .setName(item.getItemMeta().getDisplayName())
                .setLore(item.getItemMeta().getLore())
                .setCustomModelData(item.getItemMeta().getCustomModelData())
                .setEnchantments(list);
        return output;
    }
}
