package io.RPGCraft.FableCraft.Utils.GUI;

import org.bukkit.enchantments.Enchantment;

public class Enchants {
    private Enchantment enchantment;
    private Integer level;
    private Boolean Boo = true;

    public Boolean getBoo() {
        return Boo;
    }

    public Enchants setBoo(Boolean boo) {
        Boo = boo;
        return this;
    }

    public Integer getLevel() {
        return level;
    }

    public Enchants setLevel(Integer level) {
        this.level = level;
        return this;
    }

    public Enchantment getEnchantment() {
        return enchantment;
    }

    public Enchants setEnchantment(Enchantment enchantment) {
        this.enchantment = enchantment;
        return this;
    }
}
