package io.RPGCraft.FableCraft.core.Stats;

import java.lang.reflect.Field;

public class ItemStats {

    private Double MaxHealth;
    private Double Strength;
    private Double Defense;
    private Double MovementSpeed;

    public ItemStats(){
        setDefault();
    }

    public ItemStats setDefault(){
        MaxHealth = 0.0;
        Strength = 0.0;
        Defense = 0.0;
        MovementSpeed = 0.0;
        return this;
    }

    public void stat(String name, Object value){
        try {
            Field field = this.getClass().getDeclaredField(name);
            field.set(this, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }



    public Double getMaxHealth() {
        return MaxHealth;
    }
    public ItemStats maxHealth(Double maxHealth) {
        MaxHealth = maxHealth;
        return this;
    }
    public Double getStrength() {
        return Strength;
    }
    public ItemStats strength(Double strength) {
        Strength = strength;
        return this;
    }
    public Double getDefense() {
        return Defense;
    }
    public ItemStats defense(Double defense) {
        Defense = defense;
        return this;
    }
    public Double getMovementSpeed() {
        return MovementSpeed;
    }
    public ItemStats movementSpeed(Double movementSpeed) {
        MovementSpeed = movementSpeed;
        return this;
    }
}
