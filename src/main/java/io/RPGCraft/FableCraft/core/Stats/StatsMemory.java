package io.RPGCraft.FableCraft.core.Stats;

import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.UUID;

public class StatsMemory {

    private final UUID uuid;

    private double Health = Double.valueOf((yamlGetter.getConfig("stats.Health.default")).toString());
    private double AttackDamage = Double.valueOf(yamlGetter.getConfig("stats.AttackDamage.default").toString());
    private double Regeneration = Double.valueOf(yamlGetter.getConfig("stats.Regeneration.default").toString());
    private double ManaRegeneration = Double.valueOf(yamlGetter.getConfig("stats.ManaRegeneration.default").toString());
    private double Mana = Double.valueOf(yamlGetter.getConfig("stats.Mana.default").toString());
    private double Defense = Double.valueOf(yamlGetter.getConfig("stats.Defense.default").toString());
    private double MovementSpeed = Double.valueOf(yamlGetter.getConfig("stats.MovementSpeed.default").toString());

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

  public double getRegeneration() {
    return Regeneration;
  }

  public StatsMemory regeneration(double regeneration) {
    Regeneration = regeneration;
    return this;
  }

  public double getManaRegeneration() {
    return ManaRegeneration;
  }

  public StatsMemory manaRegeneration(double manaRegeneration) {
    ManaRegeneration = manaRegeneration;
    return this;
  }
}
