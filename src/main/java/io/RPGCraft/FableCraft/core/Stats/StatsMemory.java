package io.RPGCraft.FableCraft.core.Stats;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.UUID;

public class StatsMemory {

    private final UUID uuid;

    private double Health = 20;
    private double health = 20;
    private double AttackDamage = 4;
    private double Mana = 4;
    private double Defense = 0;
    private double MovementSpeed = 1;

    public StatsMemory(UUID uuid1){
        uuid = uuid1;
    }

    public StatsMemory(Player uuid1){
        uuid = uuid1.getUniqueId();
    }

    public void updateAttributeStats(){
        Player player = Bukkit.getPlayer(uuid);
        if (player != null){
            player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(Health);
            player.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(MovementSpeed/10);
        }
    }

    public void stat(String name, Double value){
        try {
            Field field = this.getClass().getDeclaredField(name);
            field.set(this, value);
            updateAttributeStats();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public Object stat(String name){
        try {
            Field field = this.getClass().getDeclaredField(name);
            return field.get(this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Double statDouble(String name){
        if(stat(name) instanceof Number n){
          return n.doubleValue();
        }else{
          return null;
        }
    }

    public void addStat(String name, Double value){
        try {
            Field field = this.getClass().getDeclaredField(name);
            value += Double.parseDouble(field.get(this).toString());
            field.set(this, value);
            updateAttributeStats();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void addItemStats(ItemStats stats){
        Field[] statsFields = stats.getClass().getDeclaredFields();
        Field[] fields = this.getClass().getDeclaredFields();
        Arrays.stream(statsFields).forEach(s -> {
            for(Field field : fields){
                if(field.getName().equals(s.getName())){
                    field.setAccessible(true);
                    try {
                        double value = field.getDouble(this)+s.getDouble(stats);
                        field.set(this, value);
                    } catch (IllegalAccessException ignored) {}
                }
            }
        });
    }

    public void removeItemStats(ItemStats stats){
        Field[] statsFields = stats.getClass().getDeclaredFields();
        Field[] fields = this.getClass().getDeclaredFields();
        Arrays.stream(statsFields).forEach(s -> {
            for(Field field : fields){
                if(field.getName().equals(s.getName())){
                    field.setAccessible(true);
                    try {
                        double value = field.getDouble(this)-s.getDouble(stats);
                        field.set(this, value);
                    } catch (IllegalAccessException ignored) {}
                }
            }
        });
    }

    public void removeStat(String name, Double value){
        try {
            Field field = this.getClass().getDeclaredField(name);
            Double value2 = Double.parseDouble(field.get(this).toString())-value;
            if (value2 < 0){
                return;
            }
            field.set(this, value2);
            updateAttributeStats();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public Double getMaxHealth() {
        return Health;
    }
    public StatsMemory maxHealth(Double maxHealth) {
        Health = maxHealth;
        updateAttributeStats();
        return this;
    }
    public Double getAtttackDamage() {
        return AttackDamage;
    }
    public StatsMemory attackDamage(Double atkdmg) {
        AttackDamage = atkdmg;
        updateAttributeStats();
        return this;
    }
    public Double getDefense() {
        return Defense;
    }
    public StatsMemory defense(Double defense) {
        Defense = defense;
        updateAttributeStats();
        return this;
    }
    public Double getMovementSpeed() {
        return MovementSpeed;
    }
    public StatsMemory movementSpeed(Double movementSpeed) {
        MovementSpeed = movementSpeed;
        updateAttributeStats();
        return this;
    }

  public double getMana() {
    return Mana;
  }

  public StatsMemory mana(double mana) {
    Mana = mana;
    return this;
  }
}
