package org.plantuml.idea.util;

import com.intellij.openapi.application.ApplicationManager;
import org.apache.commons.lang.time.StopWatch;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: eugene
 * Date: 5/15/12
 * Time: 10:01 PM
 */

/**
 * LazyApplicationPoolExecutor runs submitted Runnable asynchronously in separate thread if at least
 * <strong>period</strong> milliseconds  passed since last submission
 */

public class LazyApplicationPoolExecutor implements Executor {
    Future<?> commandFuture;
    AtomicBoolean isExecutionRequested = new AtomicBoolean(false);
    StopWatch commandWatch = new StopWatch();
    int period;

    public LazyApplicationPoolExecutor() {
        this.period = 1000;
    }

    public LazyApplicationPoolExecutor(int period) {
        this.period = period;
    }

    /**
     * Executes submitted command on Idea's Application thread pool lazily, e.g.
     * no sooner than <strong>period</strong> milliseconds passed since previous execution finishes.
     * If multiple requests for execution arrives during period, only last one will be executed
     *
     * @param command - comman
     */

    public synchronized void execute(final Runnable command) {
        isExecutionRequested.set(true);
        if (commandFuture == null || commandFuture.isDone()) {
            commandFuture = ApplicationManager.getApplication().executeOnPooledThread(
                    new Runnable() {

                        public void run() {
                            do {
                                commandWatch.reset();
                                commandWatch.start();
                                command.run();
                                commandWatch.stop();
                                try {
                                    // sleep till the end of the period
                                    if (commandWatch.getTime() < period) {
                                        Thread.sleep(period - commandWatch.getTime());
                                    }
                                } catch (InterruptedException e) {
                                    // do nothing
                                    e.printStackTrace();
                                }
                            } while (isExecutionRequested.getAndSet(false));
                        }
                    }
            );
        } else {
            // swallow execution request
        }
    }
}
