package io.RPGCraft.FableCraft.core.GUI;

import lombok.Builder;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Getter
@Builder
public class GUIItem {
    private final Material material;
    private final Component name;
    private final List<Component> lore;
    private final int customModelData;
    private final int amount;
    private final int maxStackSize;
    private final int durability;
    private final Consumer<ClickContext> clickHandler;
    
    public ItemStack getItemStack() {
        ItemStack item = new ItemStack(this.material);
        item.setAmount(Math.min(this.amount, this.maxStackSize));
        
        // Set custom properties directly on ItemStack
        // Avoiding ItemMeta as per requirements
        if (this.customModelData > 0) {
            item.editMeta(imeta ->{
               imeta.setCustomModelData(this.customModelData);
            });
        }

        if (this.durability > 0 && item.getType().getMaxDurability() > 0) {
            item.setDurability((short) this.durability);
        }
        
        return item;
    }
    
    public static class GUIItemBuilder {
        private Material material = Material.STONE;
        private Component name = Component.empty();
        private List<Component> lore = new ArrayList<>();
        private int customModelData = 0;
        private int amount = 1;
        private int maxStackSize = 64;
        private int durability = 0;
        private Consumer<ClickContext> clickHandler = (context) -> {};
    }
    
    public record ClickContext(Player player, ClickType clickType, GUI gui) {}
}
