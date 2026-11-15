package io.RPGCraft.FableCraft.Tasks;

import io.RPGCraft.FableCraft.Utils.ColorUtils;
import io.RPGCraft.FableCraft.commands.quest.questManager;
import io.RPGCraft.FableCraft.core.YAML.yamlManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.List;

import static io.RPGCraft.FableCraft.core.YAML.Placeholder.setPlaceholders;

public class Sidebar implements Runnable {

  private static final Sidebar instance = new Sidebar();

  public static Sidebar getInstance() {
    return instance;
  }

  @Override
  public void run() {
    if (!Boolean.valueOf(yamlManager.getInstance().getOption("config", "sidebar.enabled").toString())) {
      return;
    }

    for (Player p : Bukkit.getOnlinePlayers()) {
      updateSidebar(p);
    }
  }

  private void updateSidebar(Player p) {
    // Create a new scoreboard each time to avoid stale teams from previous updates
    Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

    // Create new sidebar objective
    String title = yamlManager.getInstance().getOption("config", "sidebar.title").toString();
    title = resolvePlaceholders(title, p);
    Objective objective = scoreboard.registerNewObjective("RPGCraftSidebar", "dummy", ColorUtils.colorize(title, '&'));
    objective.setDisplaySlot(DisplaySlot.SIDEBAR);

    // Build the lines (scoreboard is rendered highest score at top, so we assign descending scores)
    List<String> lines = buildSidebarLines(p);
    int score = lines.size();

    for (String line : lines) {
      String resolved = resolvePlaceholders(line, p);
      // Use ColorUtils.colorize to support hex colors (&#RRGGBB, &{#RRGGBB}, gradients)
      resolved = ColorUtils.colorize(resolved, '&');

      // Each line gets its own team; text goes in the suffix (no length limit)
      // The entry is a single space — unique because each line has its own team
      String teamName = "t" + score;
      org.bukkit.scoreboard.Team team = scoreboard.registerNewTeam(teamName);
      team.setPrefix(resolved);
      team.setSuffix("");
      // Entry name must be unique across the scoreboard — use score-based unique name
      String entryName = String.valueOf((char)(0x200B + score));
      team.addEntry(entryName);
      objective.getScore(entryName).setScore(score);
      score--;
    }

    p.setScoreboard(scoreboard);
  }

  private List<String> buildSidebarLines(Player p) {
    Object template = yamlManager.getInstance().getOption("config", "sidebar.lines");
    if (template instanceof List<?> list) {
      return list.stream().map(Object::toString).toList();
    }
    if (template instanceof String s) {
      // Single line config — wrap in list
      return List.of(s);
    }
    return List.of();
  }

  private String resolvePlaceholders(String text, Player p) {
    // First, resolve quest-specific placeholders manually (not in the registry)
    List<String> activeQuests = questManager.getActiveQuests(p);

    if (!activeQuests.isEmpty()) {
      String currentQuest = activeQuests.get(0);
      int currentStep = questManager.getStep(p, currentQuest);

      Object questNameObj = questManager.questOption(currentQuest, "name");
      String questName = questNameObj != null ? questNameObj.toString() : currentQuest;

      int totalSteps = questManager.toInt(questManager.questOption(currentQuest, "steps.amount"), 0);
      String stepInfo = currentStep + "/" + totalSteps;

      Location stepLoc = questManager.stepLocation(currentQuest, currentStep);
      String distance;
      if (stepLoc != null) {
        double dist = p.getLocation().distance(stepLoc);
        distance = String.valueOf((int) dist) + " blocks";
      } else {
        distance = "N/A";
      }

      text = text.replace("%quest_name%", questName);
      text = text.replace("%quest_step%", stepInfo);
      text = text.replace("%quest_distance%", distance);
    } else {
      text = text.replace("%quest_name%", "No active quest");
      text = text.replace("%quest_step%", "0/0");
      text = text.replace("%quest_distance%", "N/A");
    }

    // Use the existing placeholder system for %money%, %rankPrefix%, %currentHealth%, %maxHealth%, %currentMana%, %maxMana%, etc.
    text = setPlaceholders(text, false, p);

    // Handle legacy aliases for backwards compatibility with old config format
    text = text.replace("%coins%", String.valueOf(getMoneyStatic(p)));
    text = text.replace("%rank_prefix%", getRankPrefixStatic(p));
    text = text.replace("%player%", p.getName());

    return text;
  }

  private static int getMoneyStatic(Player p) {
    return io.RPGCraft.FableCraft.Utils.money.moneyManager.getMoney(p);
  }

  private static String getRankPrefixStatic(Player p) {
    return io.RPGCraft.FableCraft.Utils.Placeholders.PlaceholderTypes.PlayerPlaceholders.rankPrefix(p);
  }
}
