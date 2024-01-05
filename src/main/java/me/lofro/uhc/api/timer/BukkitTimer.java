package me.lofro.uhc.api.timer;

import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static me.lofro.uhc.api.timer.GameTimer.getTimeString;

/**
 * Simple and easy to use {@link  JTimer} adapted to Bukkit. Uses {@link BossBar} as output.
 * @author <a href="https://github.com/zLofro">Lofro</a>.
 * @author <a href="https://github.com/InfinityZ25">InfinityZ25</a>.
 */
@Getter
public class BukkitTimer extends JTimer {

    private final BossBar bossBar;
    private final List<Audience> audience = new ArrayList<>();
    private @Getter boolean active = false;

    public BukkitTimer(final int time, final BossBar bossbar) {
        super(time);
        this.bossBar = bossbar;
    }

    public BukkitTimer(final int time) {
        super(time);
        this.bossBar = BossBar.bossBar(formatTime(time), 1, Color.WHITE, Overlay.PROGRESS);
    }

    @Override
    public CompletableFuture<JTimer> start() {
        if (active) throw new IllegalStateException("Timer is already running");
        this.active = true;
        bossBar.name(formatTime(time()));
        addAllViewers();
        return super.start();
    }

    @Override
    protected int tick() {
        // Progress the bar before the time is updated.
        bossBar.progress(progress());
        var tick = super.tick();
        // Update bar's name.
        bossBar.name(formatTime(tick));

        if (active) Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), "sfx.tic", 2f, 1f));

        return tick;
    }

    @Override
    protected void onComplete() {
        super.onComplete();
        // Remove bar from players
        audience.forEach(a -> a.hideBossBar(bossBar));
        this.active = false;
    }

    public void addViewer(Audience player) {
        if (!audience.contains(player)) {
            audience.add(player);
            player.showBossBar(bossBar);
        }
    }

    public void addAllViewers() {
        Bukkit.getOnlinePlayers().forEach(this::addViewer);
    }

    public void removeViewer(Audience player) {
        if (audience.contains(player)) {
            audience.remove(player);
            player.hideBossBar(bossBar);
        }
    }

    public void removeViewers() {
        Bukkit.getOnlinePlayers().forEach(this::removeViewer);
    }

    private static Component formatTime(final int time) {
        return Component.text(timeConvert(time));
    }

    private static String timeConvert(int t) {
        return getTimeString(t);
    }
}
