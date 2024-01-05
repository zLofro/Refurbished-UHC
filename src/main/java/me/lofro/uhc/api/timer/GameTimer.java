package me.lofro.uhc.api.timer;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple and easy to use Timer using {@link BukkitRunnable}. Uses the {@link net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket} as output.
 * @author <a href="https://github.com/zLofro">Lofro</a>.
 * @author <a href="https://github.com/InfinityZ25">InfinityZ25</a>.
 */
public class GameTimer extends BukkitRunnable {

    private int seconds;

    private final @Getter List<Audience> audience = new ArrayList<>();

    private @Setter @Getter boolean isActive = false;

    public GameTimer() {
        this.seconds = 0;
    }

    @Override
    public void run() {
        if (!isActive)
            return;
        if (this.seconds >= 0) {
            audience.forEach(p -> p.sendActionBar(formatTime(this.seconds)));
            this.seconds--;
        } else {
            end();
        }
    }

    public void update(int seconds) {
        this.seconds = seconds;
    }

    public int getTime() {
        return seconds;
    }

    public void setPreStart(int time) {
        this.seconds = time;
        this.addPlayers();
    }

    public void start(int seconds) {
        setPreStart(seconds);
        this.isActive = true;
        addPlayers();
    }

    public void end() {
        this.isActive = false;
        this.seconds = 0;
        removePlayers();
    }

    public void addPlayer(Audience player) {
        if (audience.contains(player))
            return;
        audience.add(player);
    }

    public void addPlayers() {
        Bukkit.getOnlinePlayers().forEach(this::addPlayer);
    }

    public void removePlayer(Audience player) {
        audience.remove(player);
    }

    public void removePlayers() {
        Bukkit.getOnlinePlayers().forEach(this::removePlayer);
    }

    private static Component formatTime(final int time) {
        return Component.text(timeConvert(time));
    }

    private static String timeConvert(int t) {
        return getTimeString(t);
    }

    public static String getTimeString(int t) {
        int hours = t / 3600;

        int minutes = (t % 3600) / 60;
        int seconds = t % 60;

        return (hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds)
                : String.format("%02d:%02d", minutes, seconds));
    }

}
