package org.plantuml.idea.intentions;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ArrowUtilsTest {

    @Test
    public void testReverse() throws Exception {
        assertEquals("<-", new String(ArrowUtils.cutArrowAndReverse(" -> ".toCharArray(), 1, 2)));
    }

}