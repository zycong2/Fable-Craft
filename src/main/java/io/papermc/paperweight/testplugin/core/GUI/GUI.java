package io.papermc.paperweight.testplugin.core.GUI;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
public class GUI implements InventoryHolder {
    private final Component title;
    private final int rows;
    private final Inventory inventory;
    private final Map<Integer, GUIItem> items;
    private GUIAnimation currentAnimation;
    
    public GUI(final Component title, final int rows) {
        this.title = title;
        this.rows = rows;
        this.inventory = Bukkit.createInventory(this, rows * 9, title);
        this.items = new HashMap<>();
    }
    
    public void setItem(final int slot, final GUIItem item) {
        if (slot >= 0 && slot < this.inventory.getSize()) {
            this.items.put(slot, item);
            this.inventory.setItem(slot, item.getItemStack());
        }
    }
    
    public Optional<GUIItem> getItem(final int slot) {
        return Optional.ofNullable(this.items.get(slot));
    }
    
    public void open(final Player player) {
        player.openInventory(this.inventory);
    }
    
    public void startAnimation(final GUIAnimation animation) {
        if (this.currentAnimation != null) {
            this.currentAnimation.stop();
        }
        this.currentAnimation = animation;
        this.currentAnimation.start(this);
    }
    
    public void stopAnimation() {
        if (this.currentAnimation != null) {
            this.currentAnimation.stop();
            this.currentAnimation = null;
        }
    }
    
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}
