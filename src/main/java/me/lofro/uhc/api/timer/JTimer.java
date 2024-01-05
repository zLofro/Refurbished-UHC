package me.lofro.uhc.api.timer;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Simple and easy to use Java timer.
 * @author <a href="https://github.com/zLofro">Lofro</a>.
 * @author <a href="https://github.com/InfinityZ25">InfinityZ25</a>.
 */
public class JTimer implements Runnable {
    private @Setter int time;
    private @Getter final int initialTime;
    protected final CompletableFuture<JTimer> future;
    protected @Getter Thread thread;

    public JTimer(int time) {
        this.time = time;
        this.initialTime = time;
        this.future = new CompletableFuture<>();
    }

    protected int tick() {
        if (time >= 0) {
            return time--;
        }
        onComplete();
        return 0;
    }

    protected void onComplete() {
        // Complete the future
        future.complete(this);
    }

    @Override
    public void run() {
        do {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (tick() >= 0);
    }

    /**
     * @return The percentage of the time left.
     */
    public float progress() {
        return Math.max(Math.min((float) time / initialTime, 0), 1);
    }

    /**
     * @return The total percentage of completion so far, inverse of progress().
     */
    public float completion() {
        return 1 - progress();
    }

    protected int time() {
        return this.time;
    }

    public CompletableFuture<JTimer> start() {
        this.thread = new Thread(this, "Timer@" + UUID.randomUUID());
        this.thread.start();
        return future;
    }

    public void end() {
        time = 0;
    }

    public static BukkitTimer bTimer(int seconds) {
        return new BukkitTimer(seconds);
    }

}