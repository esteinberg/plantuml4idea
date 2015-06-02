package org.plantuml.idea.intention;

import org.junit.Test;
import org.plantuml.idea.intentions.Arrow;

import java.util.Arrays;

import static org.junit.Assert.*;

public class ArrowTest {

    @Test
    public void arrowEndpoints() throws Exception {
        caretOnPosition(0).of("->        ").isArrow().hasEndpointsOn(0, 1);
        caretOnPosition(1).of(" ->       ").isArrow().hasEndpointsOn(1, 2);
        caretOnPosition(2).of("  ->      ").isArrow().hasEndpointsOn(2, 3);
        caretOnPosition(2).of("->        ").isArrow(true).hasEndpointsOn(0, 1);
        caretOnPosition(3).of("  ->      ").isArrow(true).hasEndpointsOn(2, 3);
        caretOnPosition(3).of("  --      ").isArrow(false).hasEndpointsOn(2, 3);
        caretOnPosition(1).of("  ->      ").isArrow(false).hasEndpointsOn(-1, -1);
        caretOnPosition(6).of("Alice --> ").isArrow().hasEndpointsOn(6, 8);


        assertThat("  ->       ").hasEndpointsOn(2, 3);
        assertThat("  -->      ").hasEndpointsOn(2, 4);
        assertThat("  --->     ").hasEndpointsOn(2, 5);
        assertThat("  <-       ").hasEndpointsOn(2, 3);
        assertThat("  <--      ").hasEndpointsOn(2, 4);
        assertThat("  <---     ").hasEndpointsOn(2, 5);
        assertThat("  <->      ").hasEndpointsOn(2, 4);
        assertThat("  <-->     ").hasEndpointsOn(2, 5);
        assertThat("  <--->    ").hasEndpointsOn(2, 6);
        assertThat("  --       ").isArrow(false).hasEndpointsOn(2, 3);
        assertThat("           ").isArrow(false).hasEndpointsOn(-1, -1);


        //sequence todo
        caretOnPosition(0).of("->x    ").isArrow().hasEndpointsOn(0, 2);
        caretOnPosition(0).of("->>    ").isArrow().hasEndpointsOn(0, 3);
        caretOnPosition(0).of("-\\    ").isArrow().hasEndpointsOn(0, 1);
        caretOnPosition(0).of("\\\\-  ").isArrow().hasEndpointsOn(0, 2);
        caretOnPosition(0).of("//--   ").isArrow().hasEndpointsOn(0, 1);
        caretOnPosition(0).of("->o    ").isArrow().hasEndpointsOn(0, 1);
        caretOnPosition(0).of("o\\\\--").isArrow().hasEndpointsOn(0, 1);
        caretOnPosition(0).of("<->    ").isArrow().hasEndpointsOn(0, 1);
        caretOnPosition(0).of("<->o   ").isArrow().hasEndpointsOn(0, 1);
        caretOnPosition(0).of("[->    ").isArrow().hasEndpointsOn(0, 1);
        caretOnPosition(0).of("[o->   ").isArrow().hasEndpointsOn(0, 1);
        caretOnPosition(0).of("[o->o  ").isArrow().hasEndpointsOn(0, 1);
        caretOnPosition(0).of("[x->   ").isArrow().hasEndpointsOn(0, 1);
        caretOnPosition(0).of("[<-    ").isArrow().hasEndpointsOn(0, 1);
        caretOnPosition(0).of("[x<-   ").isArrow().hasEndpointsOn(0, 1);
        caretOnPosition(0).of(" ->]   ").isArrow().hasEndpointsOn(0, 1);
        caretOnPosition(0).of(" ->o]  ").isArrow().hasEndpointsOn(0, 1);
        caretOnPosition(0).of(" o->o] ").isArrow().hasEndpointsOn(0, 1);
        caretOnPosition(0).of(" ->x]  ").isArrow().hasEndpointsOn(0, 1);
        caretOnPosition(0).of(" <-]   ").isArrow().hasEndpointsOn(0, 1);
        caretOnPosition(0).of(" x<-]  ").isArrow().hasEndpointsOn(0, 1);
        //class
        caretOnPosition(0).of("<|--   ").isArrow().hasEndpointsOn(0, 1);
        caretOnPosition(0).of("*--    ").isArrow().hasEndpointsOn(0, 1);
        caretOnPosition(0).of("o--    ").isArrow().hasEndpointsOn(0, 1);
        caretOnPosition(0).of("..     ").isArrow().hasEndpointsOn(0, 1);
        caretOnPosition(0).of("--     ").isArrow().hasEndpointsOn(0, 1);
        caretOnPosition(0).of("<|..   ").isArrow().hasEndpointsOn(0, 1);
        caretOnPosition(0).of("-->    ").isArrow().hasEndpointsOn(0, 1);
        caretOnPosition(0).of("..>    ").isArrow().hasEndpointsOn(0, 1);
        caretOnPosition(0).of("..|>   ").isArrow().hasEndpointsOn(0, 1);
        caretOnPosition(0).of("<--*   ").isArrow().hasEndpointsOn(0, 1);
    }

    private AssertStep assertThat(String s) {
        return new AssertStep(s);
    }

    private AssertStep caretOnPosition(int caretPosition) {
        return new AssertStep(caretPosition);
    }

    private class AssertStep {
        private int caretPosition = -1;
        private String string;
        private Boolean isArrow;

        public AssertStep(int caretPosition) {
            this.caretPosition = caretPosition;
        }

        public AssertStep(String string) {
            this.string = string;
        }

        public AssertStep of(String string) {
            this.string = string;
            return this;
        }

        public AssertStep hasEndpointsOn(int i, int i1) {
            if (caretPosition != -1) {
                check(i, i1, caretPosition, isArrow, string, false);
            } else {
                checkAllCaretPositions(i, i1);
            }
            return this;
        }

        private void checkAllCaretPositions(int i, int i1) {
            for (int j = 0; j < string.length(); j++) {
                check(i, i1, j, isArrow, string, true);
            }
        }

        public AssertStep isArrow() {
            isArrow = true;
            return this;
        }

        public AssertStep isArrow(boolean b) {
            isArrow = b;
            return this;
        }
    }

    static void check(int expected1, int expected2, int caret, Boolean isArrow, String string, boolean allCarets) {
        Arrow endpointPosition = new Arrow(caret, string.toCharArray());
        endpointPosition.invoke();


        int[] expecteds = {expected1, expected2};
        int[] actuals = {endpointPosition.getEnd1(), endpointPosition.getEnd2()};
        Arrays.sort(expecteds);
        Arrays.sort(actuals);

        System.err.println("caretOn(" + caret + ").of(\"" + string + "\").isArrow(" + isArrow + ").andHasEndpointsOn(" + expected1 + ", " + expected2 + ");");
        boolean caretOnArrow = isCaretOnArrow(caret, expecteds);//for all caret check
        try {
            if (allCarets) {
                if (!caretOnArrow) {
                    assertFalse(endpointPosition.isValidArrow());
                    assertEquals(-1, actuals[0]);
                    assertEquals(-1, actuals[1]);
                } else {
                    assertArrayEquals(Arrays.toString(expecteds) + " != " + Arrays.toString(actuals), expecteds, actuals);
                    if (isArrow != null) {
                        assertEquals(isArrow, endpointPosition.isValidArrow());
                    } else {
                        assertEquals(true, endpointPosition.isValidArrow());
                    }
                }
            } else {
                if (isArrow != null) {
                    assertEquals(isArrow, endpointPosition.isValidArrow());
                } else {
                    assertEquals(true, endpointPosition.isValidArrow());
                }
                assertArrayEquals(Arrays.toString(expecteds) + " != " + Arrays.toString(actuals), expecteds, actuals);
            }
        } catch (AssertionError e) {
            //for debugging
            throw e;
        }
    }

    private static boolean isCaretOnArrow(int caret, int[] expecteds) {
        return expecteds[0] <= caret && caret <= expecteds[1] + 1;
    }

}