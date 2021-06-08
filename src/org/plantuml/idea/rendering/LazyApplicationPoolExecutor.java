package org.plantuml.idea.rendering;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.ConcurrencyUtil;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.preview.ExecutionStatusPanel;

import java.util.Comparator;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * This Executor executes Runnables sequentially and is so lazy that it executes only last RenderCommand submitted while
 * previously scheduled RenderCommand is running. Useful when you want to submit a lot of cumulative Runnables without
 * performance impact.
 *
 * @author Eugene Steinberg
 * @author Vojtech Krasa
 */
public class LazyApplicationPoolExecutor {
    public static final Logger logger = Logger.getInstance(LazyApplicationPoolExecutor.class);

    private TreeSet<RenderCommand> queue = new TreeSet<>(new Comparator<RenderCommand>() {
        @Override
        public int compare(RenderCommand t0, RenderCommand t1) {
            int compare = Long.compare(t0.getStartAtNanos(), t1.startAtNanos);
            if (compare == 0) {
                compare = t0.version - t1.version;
            }
            return compare;
        }
    });
    private ExecutorService executor = new ThreadPoolExecutor(1, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), ConcurrencyUtil.newNamedThreadFactory("PlantUML integration plugin executor", true, Thread.NORM_PRIORITY));

    protected final Object POOL_THREAD_STICK = new Object();

    @NotNull
    public static LazyApplicationPoolExecutor getInstance() {
        return ServiceManager.getService(LazyApplicationPoolExecutor.class);
    }


    /**
     * Lazily executes the RenderCommand. Command will be queued for execution, but can be swallowed by another command
     * if it will be submitted before this command will be scheduled for execution
     *
     * @param newCommand command to be executed.
     */
    public synchronized void submit(@NotNull final RenderCommand newCommand) {
        logger.debug("#submit ", newCommand);
        for (RenderCommand command : queue) {
            if (
//                    newCommand.reason != RenderCommand.Reason.REFRESH && newCommand.reason != RenderCommand.Reason.INCLUDES &&
                    command.isSame(newCommand)) {
                if (command.containsTargets(newCommand.getTargets())) {
                    logger.debug("skipping duplicate ", command);
                    return;
                } else if (command.addTargetsIfPossible_blocking(newCommand)) {
                    logger.debug("targets added to ", command);
                } else {
                    addToQueue(new RenderCommand.DisplayExisting(newCommand, command));
                }
                return;
            } else if (command.containsTargets(newCommand.getTargets())) {
                logger.debug("replacing command ", command);
                newCommand.addTargets(command.getTargets());
                queue.remove(command);//todo there could be more of them
                addToQueue(newCommand);
                return;
            }
        }
        addToQueue(newCommand);
    }

    protected void addToQueue(@NotNull RenderCommand newCommand) {
        logger.debug("adding to queue ", newCommand);
        boolean add = queue.add(newCommand);
        newCommand.updateState(ExecutionStatusPanel.State.WAITING);
        synchronized (POOL_THREAD_STICK) {
            POOL_THREAD_STICK.notifyAll();
        }
        scheduleNext();
    }

    private synchronized RenderCommand next() {
        Thread.interrupted(); //clear flag
        return queue.first();
    }

    protected synchronized void scheduleNext() {
        logger.debug("scheduleNext");
        executor.submit(new MyRunnable());
    }

    public enum Delay {
        RESET_DELAY,
        NOW,
        MAYBE_WITH_DELAY;

    }

    private class MyRunnable implements Runnable {

        @Override
        public void run() {
            RenderCommand command = next();
            while (command != null) {
                try {
                    long delayRemaining = command.getRemainingDelayMillis();
                    if (delayRemaining - 5 > 0) {//tolerance
                        logger.debug("waiting ", delayRemaining, "ms");
                        synchronized (POOL_THREAD_STICK) {
                            POOL_THREAD_STICK.wait(delayRemaining);
                        }
                        command = next();
                        continue;
                    }
                } catch (InterruptedException e) {
                    command = next();
                    continue;
                }
                try {
                    logger.debug("running command ", command);
                    long start = System.currentTimeMillis();
                    command.render();
                    command.displayResult();
                    removeFromQueue(command);
                    logger.debug("command executed in ", System.currentTimeMillis() - start, "ms");
                } catch (Throwable e) {
                    logger.error(e);
                } finally {
                    queue.remove(command);
                    command = next();
                }
            }
        }


    }

    private synchronized void removeFromQueue(RenderCommand command) {
        logger.debug("removing from queue ", command);
        queue.remove(command);
    }
}
