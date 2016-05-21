package org.plantuml.idea.util;

import org.junit.Assert;
import org.junit.Test;

import static org.plantuml.idea.util.LazyApplicationPoolExecutor.MILLION;

public class LazyApplicationPoolExecutorTest {
    @Test
    public void test() throws Exception {
        LazyApplicationPoolExecutor LazyApplicationPoolExecutor = new LazyApplicationPoolExecutor(100);
        Assert.assertEquals(100, LazyApplicationPoolExecutor.delayNanos / MILLION);
        LazyApplicationPoolExecutor.setDelay(5);
        Assert.assertEquals(5, LazyApplicationPoolExecutor.delayNanos / MILLION);
    }
}