package io.RPGCraft.FableCraft.core.GUI;

import io.RPGCraft.FableCraft.FableCraft;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class GUIAnimation {
    private final List<Frame> frames;
    private final long interval;
    private BukkitTask task;
    
    public void start(final GUI gui) {
        if (this.task != null) {
            this.stop();
        }
        
        final int[] currentFrame = {0};
        
        this.task = FableCraft.getInstance().getServer().getScheduler().runTaskTimer(
                FableCraft.getInstance(),
            () -> {
                Frame frame = this.frames.get(currentFrame[0]);
                frame.apply(gui);
                currentFrame[0] = (currentFrame[0] + 1) % this.frames.size();
            },
            0L,
            this.interval
        );
    }
    
    public void stop() {
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }
    }
    
    public record Frame(Map<Integer, GUIItem> items) {
        public void apply(final GUI gui) {
            this.items.forEach(gui::setItem);
        }
    }
}
