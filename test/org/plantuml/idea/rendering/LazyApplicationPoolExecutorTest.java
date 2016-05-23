package org.plantuml.idea.rendering;

import org.junit.Assert;
import org.junit.Test;
import org.plantuml.idea.toolwindow.ExucutionTimeLabel;

import static org.plantuml.idea.rendering.LazyApplicationPoolExecutor.MILLION;

public class LazyApplicationPoolExecutorTest {
    @Test
    public void test() throws Exception {
        LazyApplicationPoolExecutor executor = new LazyApplicationPoolExecutor(100, new ExucutionTimeLabel());
        Assert.assertEquals(100, executor.delayNanos / MILLION);
        executor.setDelay(5);
        Assert.assertEquals(5, executor.delayNanos / MILLION);
    }
}