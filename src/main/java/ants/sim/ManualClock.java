package ants.sim;

import io.jbotsim.core.Clock;
import io.jbotsim.core.ClockManager;

/**
 * JBotSim clock that advances only on explicit {@link #tick()} calls, on the caller's thread.
 * Topology.step() is not usable for captures: it resumes the timer thread asynchronously.
 */
public class ManualClock extends Clock {

    // JBotSim instantiates the clock by reflection and exposes no getter for it.
    private static volatile ManualClock last;

    private boolean running;
    private int timeUnit = 10;

    public ManualClock(ClockManager manager) {
        super(manager);
        last = this;
    }

    /**
     * The clock created by the latest Topology.start() using this model. Assumes manual
     * simulations are started one at a time on a single thread.
     */
    public static ManualClock last() {
        return last;
    }

    public void tick() {
        manager.onClock();
    }

    @Override
    public int getTimeUnit() {
        return timeUnit;
    }

    @Override
    public void setTimeUnit(int timeUnit) {
        this.timeUnit = timeUnit;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void start() {
        running = true;
    }

    @Override
    public void pause() {
        running = false;
    }

    @Override
    public void resume() {
        running = true;
    }
}
