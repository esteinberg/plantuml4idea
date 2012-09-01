package org.plantuml.idea.util;

import com.intellij.openapi.application.ApplicationManager;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;

/**
 * This Executor executes Runnables sequentially and is so lazy that it executes only last Runnable submitted while
 * previously scheduled Runnable is running. Useful when you want to submit a lot of cumulative Runnables without
 * performance impact.
 *
 * @author Eugene Steinberg
 */
public class LazyApplicationPoolExecutor implements Executor {

    private Runnable next;

    private Future<?> future;

    public LazyApplicationPoolExecutor() {
    }

    /**
     * Lazily executes the Runnable. Command will be queued for execution, but can be swallowed by another command
     * if it will be submitted before this command will be scheduled for execution
     *
     * @param command command to be executed.
     */
    public synchronized void execute(final Runnable command) {

        next = new Runnable() {
            public void run() {
                try {
                    command.run();
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    if (!Thread.currentThread().isInterrupted())
                        scheduleNext(); //needed to execute the very last command
                }
            }
        };
        if (future == null || future.isDone()) {
            scheduleNext();
        }
    }

    private synchronized Future<?> scheduleNext() {
        final Runnable toSchedule = next;
        next = null;
        if (toSchedule != null) {
            future = ApplicationManager.getApplication().executeOnPooledThread(toSchedule);
            return future;
        }
        return null;
    }
}
