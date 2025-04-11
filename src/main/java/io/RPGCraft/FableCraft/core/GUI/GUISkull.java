package io.RPGCraft.FableCraft.core.GUI;

import lombok.Builder;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Getter
public class GUISkull extends GUIItem {
  private final String texture;
  private final String playerName;

  @Builder(builderMethodName = "skullBuilder")
  public GUISkull(
    final Component name,
    final List<Component> lore,
    final int customModelData,
    final int amount,
    final int maxStackSize,
    final Consumer<ClickContext> clickHandler,
    final String texture,
    final String playerName
  ) {
    super(Material.PLAYER_HEAD, name, lore, customModelData, amount, maxStackSize, 0, clickHandler);
    this.texture = texture;
    this.playerName = playerName;
  }

  @Override
  public ItemStack getItemStack() {
    ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
    skull.setAmount(Math.min(this.getAmount(), this.getMaxStackSize()));

    if (this.getCustomModelData() > 0) {
      skull.getItemMeta().setCustomModelData(this.getCustomModelData());
    }

    // Apply skull texture or player name
    try {
      if (this.texture != null && !this.texture.isEmpty()) {
        PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
        PlayerTextures textures = profile.getTextures();
        textures.setSkin(new URL(this.texture));
        profile.setTextures(textures);

        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setPlayerProfile((com.destroystokyo.paper.profile.PlayerProfile) profile);
        skull.setItemMeta(meta);
      } else if (this.playerName != null && !this.playerName.isEmpty()) {
        PlayerProfile profile = Bukkit.createPlayerProfile(this.playerName);
        if (profile != null) {
          SkullMeta meta = (SkullMeta) skull.getItemMeta();
          meta.setPlayerProfile((com.destroystokyo.paper.profile.PlayerProfile) profile);
          skull.setItemMeta(meta);
        }
      }
    } catch (Exception e) {
      // Log error but return basic skull if texture/profile fails
      System.err.println("Failed to apply skull texture/profile: " + e.getMessage());
    }

    return skull;
  }

  public static class GUISkullBuilder {
    private Material material = Material.PLAYER_HEAD;
    private Component name = Component.empty();
    private List<Component> lore = List.of();
    private int customModelData = 0;
    private int amount = 1;
    private int maxStackSize = 64;
    private Consumer<ClickContext> clickHandler = (context) -> {};
    private String texture;
    private String playerName;

    public GUISkullBuilder material(Material material) {
      // Force material to be PLAYER_HEAD
      this.material = Material.PLAYER_HEAD;
      return this;
    }
  }
}
