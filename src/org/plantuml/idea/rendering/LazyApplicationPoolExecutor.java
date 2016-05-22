package org.plantuml.idea.rendering;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Future;


/**
 * This Executor executes Runnables sequentially and is so lazy that it executes only last Runnable submitted while
 * previously scheduled Runnable is running. Useful when you want to submit a lot of cumulative Runnables without
 * performance impact.
 *
 * @author Eugene Steinberg
 * @author Vojtech Krasa
 */
public class LazyApplicationPoolExecutor {
    public static final Logger logger = Logger.getInstance(LazyApplicationPoolExecutor.class);
    protected static final int MILLION = 1000000;

    protected Runnable nextCommand;
    protected long startAfterNanos;

    protected Future<?> future;
    protected long delayNanos; // delay between command executions
    protected final Object POOL_THREAD_STICK = new Object();

    public LazyApplicationPoolExecutor(int delayMillis) {
        setDelay(delayMillis);
        startAfterNanos = 0;
    }

    public void setDelay(long delayMillis) {
        this.delayNanos = delayMillis * MILLION;
        logger.debug("settings delayNanos=", delayNanos);
        setStartAfter();
        synchronized (POOL_THREAD_STICK) {
            POOL_THREAD_STICK.notifyAll();
        }
    }

    /**
     * Lazily executes the Runnable. Command will be queued for execution, but can be swallowed by another command
     * if it will be submitted before this command will be scheduled for execution
     *
     * @param command command to be executed.
     * @param delay
     */
    public synchronized void execute(@NotNull final Runnable command, Delay delay) {
        logger.debug("#execute ", command, " delay=", delay);
        nextCommand = command;

        if (delay == Delay.RESET_PRE_DELAY) {
            setStartAfter();
        } else if (delay == Delay.NOW) {
            startAfterNanos = 0;
            synchronized (POOL_THREAD_STICK) {
                POOL_THREAD_STICK.notifyAll();
            }
        }

        if (future == null || future.isDone()) {
            scheduleNext();
        }
    }

    private synchronized void setStartAfter() {
        startAfterNanos = System.nanoTime() + this.delayNanos;
    }

    private synchronized long getRemainingDelayMillis() {
        return (startAfterNanos - System.nanoTime()) / MILLION;
    }

    private synchronized Runnable pollCommand() {
        Runnable next = LazyApplicationPoolExecutor.this.nextCommand;
        LazyApplicationPoolExecutor.this.nextCommand = null;
        return next;
    }

    private synchronized void scheduleNext() {
        logger.debug("scheduleNext");
        if (nextCommand != null) {
            future = ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        long delayRemaining = getRemainingDelayMillis();
                        while (delayRemaining - 5 > 0) {//tolerance
                            logger.debug("waiting ", delayRemaining, "ms");
                            synchronized (POOL_THREAD_STICK) {
                                POOL_THREAD_STICK.wait(delayRemaining);
                            }
                            delayRemaining = getRemainingDelayMillis();
                        }

                        Runnable command = pollCommand();
                        if (command != null) {
                            logger.debug("running command ", command);
                            command.run();
                            setStartAfter();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        if (!Thread.currentThread().isInterrupted())
                            scheduleNext(); //needed to execute the very last command
                    }
                }

            });
        }
    }

    public enum Delay {
        RESET_PRE_DELAY,
        NOW,
        POST_DELAY;
    }
}
