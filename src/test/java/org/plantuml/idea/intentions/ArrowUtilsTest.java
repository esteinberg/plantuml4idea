package org.plantuml.idea.intentions;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.plantuml.idea.intentions.ArrowUtils.cutArrowAndReverse;

public class ArrowUtilsTest {

    @Test
    public void properArrowReversing() throws Exception {
        assertEquals("<-", new String(cutArrowAndReverse(" -> ".toCharArray(), 1, 2)));
        assertEquals("<-", new String(cutArrowAndReverse("->".toCharArray(), 0, 1)));
    }

}