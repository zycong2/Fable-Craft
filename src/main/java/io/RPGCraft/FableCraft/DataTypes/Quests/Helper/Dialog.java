package io.RPGCraft.FableCraft.DataTypes.Quests.Helper;

import io.RPGCraft.FableCraft.RPGCraft;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class Dialog {

  private List<String> dialogs;
  private Sound sound;
  private int delay;

  public int getDelay() {
    return delay;
  }

  public Dialog delay(int delay) {
    this.delay = delay;
    return this;
  }

  public Sound getSound() {
    return sound;
  }

  public Dialog sound(Sound sound) {
    this.sound = sound;
    return this;
  }

  public List<String> getDialogs() {
    return dialogs;
  }

  public Dialog dialogs(List<String> dialogs) {
    this.dialogs = dialogs;
    return this;
  }

  public void play(Player p){
    if (sound == null) {return;}
    for (String dialog : dialogs) {
      p.sendMessage(dialog);
      p.playSound(p, sound, 1, 1);
      RPGCraft.wait(delay, new BukkitRunnable() {@Override public void run() {}});
    }
  }
}
