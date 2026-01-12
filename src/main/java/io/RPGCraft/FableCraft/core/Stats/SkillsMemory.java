package io.RPGCraft.FableCraft.core.Stats;

import io.RPGCraft.FableCraft.core.YAML.yamlGetter;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class SkillsMemory {

  @Getter
  private final UUID uuid;

  @Getter @Setter
  private double Combat = Double.parseDouble((yamlGetter.getConfig("skills.Combat.default") != null ? yamlGetter.getConfig("skills.Combat.default") : yamlGetter.getConfig("skills.general.default")).toString());
  @Getter @Setter
  private double Mining = Double.parseDouble((yamlGetter.getConfig("skills.Mining.default") != null ? yamlGetter.getConfig("skills.Mining.default") : yamlGetter.getConfig("skills.general.default")).toString());
  @Getter @Setter
  private double Foraging = Double.parseDouble((yamlGetter.getConfig("skills.Foraging.default") != null ? yamlGetter.getConfig("skills.Foraging.default") : yamlGetter.getConfig("skills.general.default")).toString());

  public SkillsMemory(UUID uuid){
    this.uuid = uuid;
  }

}
