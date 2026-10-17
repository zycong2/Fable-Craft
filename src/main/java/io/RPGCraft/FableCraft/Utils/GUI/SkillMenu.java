
package io.RPGCraft.FableCraft.Utils.GUI;

import io.RPGCraft.FableCraft.RPGCraft;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.RPGCraft.FableCraft.RPGCraft.Colorize;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.getPlayerPDC;
import static io.RPGCraft.FableCraft.core.Helpers.PDCHelper.setPlayerPDC;
import static io.RPGCraft.FableCraft.Utils.GUI.GUIItem.ItemStackToGUIItem;

public final class SkillMenu {

  private static final int ITEMS_PER_PAGE = 45;
  private static final int LEVELS_PER_PAGE = 25;

  private SkillMenu() {}

  private static class Skill {
    final String id;
    final ConfigurationSection config;

    Skill(String id, ConfigurationSection config) {
      this.id = id;
      this.config = config;
    }
  }

  /**
   * Opens the skill-selection menu.
   */
  public static void openSkillsMenu(Player player, int page) {
    List<Skill> skills = getSkills();

    int maxPage = Math.max(
      0, (skills.size() - 1) / ITEMS_PER_PAGE
    );
    page = Math.max(0, Math.min(page, maxPage));

    GUI gui = new GUI("&8Skills", GUI.Rows.SIX);

    int start = page * ITEMS_PER_PAGE;
    int end = Math.min(start + ITEMS_PER_PAGE, skills.size());

    for (int i = start; i < end; i++) {
      Skill skill = skills.get(i);
      int slot = i - start;

      int level = getSkillLevel(player, skill.id);
      int maxLevel = Math.max(
        1, skill.config.getInt("max-level", 100)
      );

      Material icon = Material.matchMaterial(
        skill.config.getString("icon", "BOOK")
      );

      if (icon == null || !icon.isItem()) {
        icon = Material.BOOK;
      }

      List<String> lore = new ArrayList<>(
        skill.config.getStringList("description")
      );

      lore.add("");
      lore.add("&7Level: &e" + level + "&7/&e" + maxLevel);
      lore.add("&aClick to view skill path");

      ItemStack item = makeItem(
        icon,
        skill.config.getString("name", skill.id),
        lore
      );

      gui.setItem(
        slot,
        ItemStackToGUIItem(item).clickEvent(ce ->
          openSkillPath(ce.player(), skill.id, 0)
        )
      );
    }

    // Bottom navigation row.
    if (page > 0) {
      final int currentPage = page;
      gui.setItem(
        45,
        ItemStackToGUIItem(makeItem(
          Material.ARROW, "&aPrevious Page",
          List.of("&7Page " + page)
        )).clickEvent(ce ->
          openSkillsMenu(ce.player(), currentPage - 1)
        )
      );
    }

    gui.setItem(
      49,
      ItemStackToGUIItem(makeItem(
        Material.BARRIER, "&cClose",
        List.of("&7Close the skill menu")
      )).clickEvent(ce -> ce.player().closeInventory())
    );

    if (page < maxPage) {
      final int currentPage = page;
      gui.setItem(
        53,
        ItemStackToGUIItem(makeItem(
          Material.ARROW, "&aNext Page",
          List.of("&7Page " + (page + 2))
        )).clickEvent(ce ->
          openSkillsMenu(ce.player(), currentPage + 1)
        )
      );
    }

    gui.open(player);
  }

  /**
   * Opens a zigzag path for a particular skill.
   *
   * Each page shows 25 levels in a five-row zigzag.
   * The sixth row is reserved for navigation.
   */
  public static void openSkillPath(
    Player player,
    String skillId,
    int page
  ) {
    Skill skill = findSkill(skillId);

    if (skill == null) {
      player.sendMessage("§cThat skill does not exist.");
      return;
    }

    int maxLevel = Math.max(
      1, skill.config.getInt("max-level", 100)
    );

    int maxPage = (maxLevel - 1) / LEVELS_PER_PAGE;
    page = Math.max(0, Math.min(page, maxPage));

    int currentLevel = getSkillLevel(player, skill.id);

    GUI gui = new GUI(
      skill.config.getString("name", skill.id)
        + " &8- &7Path " + (page + 1),
      GUI.Rows.SIX
    );

    int startLevel = page * LEVELS_PER_PAGE + 1;
    int endLevel = Math.min(
      startLevel + LEVELS_PER_PAGE - 1, maxLevel
    );

    for (int level = startLevel; level <= endLevel; level++) {
      int index = level - startLevel;

      // Five columns, spaced two slots apart.
      // Each column alternates between top-to-bottom
      // and bottom-to-top.
      int column = index / 5;
      int offset = index % 5;
      int row = (column % 2 == 0) ? offset : 4 - offset;
      int col = column * 2;

      int slot = row * 9 + col;

      Material material;
      String status;

      if (level <= currentLevel) {
        material = Material.LIME_DYE;
        status = "&aCompleted";
      } else if (level == currentLevel + 1) {
        material = Material.GOLD_INGOT;
        status = "&eNext level";
      } else {
        material = Material.GRAY_DYE;
        status = "&7Locked";
      }

      List<String> lore = new ArrayList<>();
      lore.add("&7Level: &e" + level);
      lore.add("&7Status: " + status);

      ConfigurationSection rewards =
        skill.config.getConfigurationSection(
          "rewards." + level
        );

      if (rewards != null && !rewards.getKeys(false).isEmpty()) {
        lore.add("");
        lore.add("&6Rewards:");

        for (String stat : rewards.getKeys(false)) {
          lore.add("&7" + stat + ": &a+"
            + rewards.getString(stat));
        }
      }

      if (level > currentLevel) {
        lore.add("");
        lore.add("&8Earn this level through gameplay");
      }

      final int nodeLevel = level;

      ItemStack item = makeItem(
        material, "&eLevel " + level, lore
      );

      gui.setItem(
        slot,
        ItemStackToGUIItem(item).clickEvent(ce -> {
          Player p = ce.player();

          if (nodeLevel <= getSkillLevel(p, skill.id)) {
            p.sendMessage(
              "§aYou have already reached level "
                + nodeLevel + "!"
            );
          } else {
            p.sendMessage(
              "§eReach level " + nodeLevel
                + " in " + skill.id
                + " through gameplay."
            );
          }
        })
      );
    }

    // Previous path page.
    if (page > 0) {
      final int currentPage = page;
      gui.setItem(
        45,
        ItemStackToGUIItem(makeItem(
          Material.ARROW, "&aPrevious Page",
          List.of("&7Page " + page)
        )).clickEvent(ce ->
          openSkillPath(
            ce.player(), skill.id, currentPage - 1
          )
        )
      );
    }

    // Return to skill selection.
    gui.setItem(
      49,
      ItemStackToGUIItem(makeItem(
        Material.BARRIER, "&cBack to Skills",
        List.of("&7Return to the skill list")
      )).clickEvent(ce ->
        openSkillsMenu(ce.player(), 0)
      )
    );

    // Next path page.
    if (page < maxPage) {
      final int currentPage = page;
      gui.setItem(
        53,
        ItemStackToGUIItem(makeItem(
          Material.ARROW, "&aNext Page",
          List.of("&7Page " + (page + 2))
        )).clickEvent(ce ->
          openSkillPath(
            ce.player(), skill.id, currentPage + 1
          )
        )
      );
    }

    gui.open(player);
  }

  /**
   * Adds skill levels and grants configured rewards for each
   * newly reached level.
   *
   * Call this from your gameplay / XP system.
   */
  public static int addSkillLevel(
    Player player,
    String skillId,
    int amount
  ) {
    Skill skill = findSkill(skillId);

    if (skill == null || amount <= 0) {
      return getSkillLevel(player, skillId);
    }

    int current = getSkillLevel(player, skill.id);
    int max = Math.max(
      1, skill.config.getInt("max-level", 100)
    );

    int target = Math.min(max, current + amount);

    for (int level = current + 1; level <= target; level++) {
      applyLevelRewards(player, skill, level);
    }

    setPlayerPDC(skill.id, player, String.valueOf(target));

    return target;
  }

  /**
   * Returns the player's current level in a skill.
   * An unset skill starts at level zero.
   */
  public static int getSkillLevel(Player player, String skillId) {
    String value = getPlayerPDC(skillId, player);

    if (value == null) return 1;

    try {
      return Math.max(0, Integer.parseInt(value));
    } catch (NumberFormatException ignored) {
      return 0;
    }
  }

  private static void applyLevelRewards(
    Player player,
    Skill skill,
    int level
  ) {
    ConfigurationSection rewards =
      skill.config.getConfigurationSection(
        "rewards." + level
      );

    if (rewards == null) return;

    for (String stat : rewards.getKeys(false)) {
      if (!RPGCraft.itemStats.contains(stat)) {
        Bukkit.getLogger().warning(
          "[Skills] Invalid stat '" + stat
            + "' in skill " + skill.id
            + " at level " + level
        );
        continue;
      }

      double increase = rewards.getDouble(stat, 0);

      if (increase == 0) continue;

      String currentValue = getPlayerPDC(stat, player);
      double current = 0;

      if (currentValue != null) {
        try {
          current = Double.parseDouble(currentValue);
        } catch (NumberFormatException ignored) {
          Bukkit.getLogger().warning(
            "[Skills] Invalid player stat value for "
              + stat + " on " + player.getName()
          );
          continue;
        }
      }

      double updated = current + increase;

      String storedValue = updated == Math.rint(updated)
        ? String.valueOf((long) updated)
        : String.valueOf(updated);

      setPlayerPDC(stat, player, storedValue);
    }
  }

  private static List<Skill> getSkills() {
    List<Skill> skills = new ArrayList<>();
    Map<String, Skill> unique = new HashMap<>();

    List<YamlConfiguration> files =
      RPGCraft.DBFileConfiguration.get("skills");

    if (files == null) return skills;

    for (YamlConfiguration file : files) {
      if (file == null) continue;

      for (String id : file.getKeys(false)) {
        ConfigurationSection section =
          file.getConfigurationSection(id);

        if (section == null) continue;

        unique.putIfAbsent(
          id.toLowerCase(),
          new Skill(id, section)
        );
      }
    }

    skills.addAll(unique.values());
    return skills;
  }

  private static Skill findSkill(String skillId) {
    if (skillId == null) return null;

    for (Skill skill : getSkills()) {
      if (skill.id.equalsIgnoreCase(skillId)) {
        return skill;
      }
    }

    return null;
  }

  private static ItemStack makeItem(
    Material material,
    String name,
    List<String> lore
  ) {
    ItemStack item = new ItemStack(material);
    ItemMeta meta = item.getItemMeta();

    if (meta != null) {
      meta.setDisplayName(Colorize(name));

      List<String> coloredLore = new ArrayList<>();
      for (String line : lore) {
        coloredLore.add(Colorize(line));
      }

      meta.setLore(coloredLore);
      item.setItemMeta(meta);
    }

    return item;
  }
}
