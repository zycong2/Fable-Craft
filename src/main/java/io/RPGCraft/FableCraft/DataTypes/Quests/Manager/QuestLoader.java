package io.RPGCraft.FableCraft.DataTypes.Quests.Manager;

import io.RPGCraft.FableCraft.DataTypes.Quests.Helper.Objective;
import io.RPGCraft.FableCraft.DataTypes.Quests.Helper.ObjectiveTypes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestLoader {
  private static final Map<String, List<Objective>> quests = new HashMap<>();

  static {
    quests.put("bandit_hunt", List.of(
      new Objective().name("Clark").types(ObjectiveTypes.TALK_TO_NPC),
      new Objective().name("Bandit").types(ObjectiveTypes.KILL_MOBS).count(5)
    ));
  }

  public static Objective getObjective(String questId, int step) {
    return quests.getOrDefault(questId, List.of()).get(step);
  }
}

