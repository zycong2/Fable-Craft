package io.RPGCraft.FableCraft.Utils.GUI;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static io.RPGCraft.FableCraft.RPGCraft.MM;
import static io.RPGCraft.FableCraft.RPGCraft.getPlugin;

public class GUI implements InventoryHolder {

    private String Title;
    private Rows Row;
    private Inventory inventory;
    private boolean canModified = false;

    private Map<Integer, GUIItem> itemMap;

    Map<Integer, GUIItem> getItemMap(){
        return itemMap;
    }

    public GUI(String title, Rows rows) {
        this.Title = title;
        this.Row = rows;

        buildInventory(this.Title, this.Row);
    }

    public GUI(String title) {
        this.Title = title;
        this.Row = Rows.ONE;

        buildInventory(this.Title, this.Row);
    }

    public void setItem(int slot, GUIItem item){
        inventory.setItem(slot, item.toItemStack());
        itemMap.put(slot, item);
    }

    public void canBeModified(){
      canModified = !canModified;
    }

    public boolean getCanBeModified(){
      return canModified;
    }

    public void setItem(int slot, ItemStack item) {
        inventory.setItem(slot, item);
        itemMap.put(slot, GUIItem.ItemStackToGUIItem(item));
    }

    public void open(Player player){
        player.openInventory(inventory);
    }

    public void open(Player player, Integer delay){
        Bukkit.getScheduler().runTaskLater(getPlugin(), new BukkitRunnable() {
            @Override
            public void run() {
                player.openInventory(inventory);
            }
        }, delay);
    }

    private void buildInventory(String Title, Rows Rows){
        Component colorized = MM(Title);
        Inventory inv = Bukkit.createInventory(this, Rows.getValue()*9, colorized);

        this.inventory = inv;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }

    public enum Rows{
        ONE(1),
        TWO(2),
        THREE(3),
        FOUR(4),
        FIVE(5),
        SIX(6);

        private Integer value;

        Rows(Integer i){
            this.value = i;
        }

        Integer getValue(){
            return value;
        }
    }
}
