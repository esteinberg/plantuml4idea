package org.plantuml.idea.intentions;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class ArrowTest {

    private static final int[] INVALID_POSITION = new int[]{-1, -1};

    @Test
    public void arrowFinding() throws Exception {
        assertThat("->        ").withCaretOnPosition(0).hasEndpointsOn(0, 1);
        assertThat(" ->       ").withCaretOnPosition(1).hasEndpointsOn(1, 2);
        assertThat("->>>>     ").withCaretOnPosition(2).hasEndpointsOn(0, 4);
        assertThat("  ->      ").withCaretOnPosition(2).hasEndpointsOn(2, 3);
        assertThat("->        ").withCaretOnPosition(2).hasEndpointsOn(0, 1);
        assertThat(" <-       ").withCaretOnPosition(3).hasEndpointsOn(1, 2);
        assertThat("  ->      ").withCaretOnPosition(4).hasEndpointsOn(2, 3);
        assertThat("  --      ").withCaretOnPosition(3).isNotValidArrow();
        assertThat("  ->      ").withCaretOnPosition(1).isNotValidArrow();
        assertThat("Alice --> ").withCaretOnPosition(6).hasEndpointsOn(6, 8);

        //sequence 
        assertThat("->x       ").hasEndpointsOn(0, 2);
        assertThat("->>       ").withCaretOnPosition(0).hasEndpointsOn(0, 2);
        assertThat("-\\       ").withCaretOnPosition(0).hasEndpointsOn(0, 1);
        assertThat("\\\\-     ").withCaretOnPosition(0).hasEndpointsOn(0, 2);
        assertThat("//--      ").withCaretOnPosition(0).hasEndpointsOn(0, 3);
        assertThat("->o       ").withCaretOnPosition(0).hasEndpointsOn(0, 2);
        assertThat("o\\\\--   ").withCaretOnPosition(0).hasEndpointsOn(0, 4);
        assertThat("<->       ").withCaretOnPosition(0).hasEndpointsOn(0, 2);
        assertThat("<->o      ").withCaretOnPosition(0).hasEndpointsOn(0, 3);
        assertThat("[->       ").withCaretOnPosition(0).hasEndpointsOn(0, 2);
        assertThat("[o->      ").withCaretOnPosition(0).hasEndpointsOn(0, 3);
        assertThat("[o->o     ").withCaretOnPosition(0).hasEndpointsOn(0, 4);
        assertThat("[x->      ").withCaretOnPosition(0).hasEndpointsOn(0, 3);
        assertThat("[<-       ").withCaretOnPosition(0).hasEndpointsOn(0, 2);
        assertThat("[x<-      ").withCaretOnPosition(0).hasEndpointsOn(0, 3);
        assertThat(" ->]      ").withCaretOnPosition(1).hasEndpointsOn(1, 3);
        assertThat(" ->o]     ").withCaretOnPosition(1).hasEndpointsOn(1, 4);
        assertThat(" o->o]    ").withCaretOnPosition(1).hasEndpointsOn(1, 5);
        assertThat(" ->x]     ").withCaretOnPosition(1).hasEndpointsOn(1, 4);
        assertThat(" <-]      ").withCaretOnPosition(1).hasEndpointsOn(1, 3);
        assertThat(" x<-]     ").withCaretOnPosition(1).hasEndpointsOn(1, 4);
        //class   
        assertThat("<|--      ").withCaretOnPosition(1).hasEndpointsOn(0, 3);
        assertThat("*--       ").hasEndpointsOn(0, 2);
        assertThat("o--       ").withCaretOnPosition(0).hasEndpointsOn(0, 2);
        assertThat("..        ").withCaretOnPosition(0).isNotValidArrow();
        assertThat("--        ").withCaretOnPosition(0).isNotValidArrow();
        assertThat("<|..      ").withCaretOnPosition(0).hasEndpointsOn(0, 3);
        assertThat("-->       ").withCaretOnPosition(0).hasEndpointsOn(0, 2);
        assertThat("..>       ").withCaretOnPosition(0).hasEndpointsOn(0, 2);
        assertThat("..|>      ").withCaretOnPosition(0).hasEndpointsOn(0, 3);
        assertThat("<--*      ").withCaretOnPosition(0).hasEndpointsOn(0, 3);

        assertThat("  ->       ").hasEndpointsOn(2, 3);
        assertThat("  -->      ").hasEndpointsOn(2, 4);
        assertThat("  --->     ").hasEndpointsOn(2, 5);
        assertThat("  <-       ").hasEndpointsOn(2, 3);
        assertThat("  <--      ").hasEndpointsOn(2, 4);
        assertThat("  <---     ").hasEndpointsOn(2, 5);
        assertThat("  <->      ").hasEndpointsOn(2, 4);
        assertThat("  <-->     ").hasEndpointsOn(2, 5);
        assertThat("  <--->    ").hasEndpointsOn(2, 6);
        assertThat("  --       ").isNotValidArrow();
        assertThat("           ").isNotValidArrow();
        assertThat(" asdasdaas ").isNotValidArrow();
        assertThat(" <><>><as> ").isNotValidArrow();
    }

    @Test
    public void arrowFinding2() throws Exception {
        assertThat("Alice-->Bob").withCaretOnPosition(4).isNotValidArrow();
        assertThat("Alice-->Bob").withCaretOnPosition(5).hasEndpointsOn(5, 7);
        assertThat("Alice-->Bob").withCaretOnPosition(6).hasEndpointsOn(5, 7);
        assertThat("Alice-->Bob").withCaretOnPosition(7).hasEndpointsOn(5, 7);
        assertThat("Alice-->Bob").withCaretOnPosition(8).hasEndpointsOn(5, 7);
        assertThat("Alice-->Bob").withCaretOnPosition(9).isNotValidArrow();

        assertThat("Alice->oBob").withCaretOnPosition(5).hasEndpointsOn(5, 7);
        assertThat("Alice->oBob").withCaretOnPosition(6).hasEndpointsOn(5, 7);
        assertThat("Alice->xBob").withCaretOnPosition(7).hasEndpointsOn(5, 7);
        assertThat("Alice->xBob").withCaretOnPosition(8).hasEndpointsOn(5, 7);
    }

    private AssertStep assertThat(String s) {
        return new AssertStep(s);
    }

    private class AssertStep {
        private int caretPosition = -1;
        private String input;
        private boolean expectValidArrow;

        public AssertStep(String input) {
            this.input = input;
        }

        public AssertStep withCaretOnPosition(int caretPosition) {
            this.caretPosition = caretPosition;
            return this;
        }

        public AssertStep hasEndpointsOn(int arrowStart, int arrowEnd) {
            expectValidArrow = true;
            int[] expectedPosition = {arrowStart, arrowEnd};
            Arrays.sort(expectedPosition);
            execute(expectedPosition);
            return this;
        }

        public void isNotValidArrow() {
            expectValidArrow = false;
            execute(INVALID_POSITION);
        }

        private void execute(int[] expectedPosition) {
            if (fixedCaretPosition()) {
                Arrow arrow = getArrow(caretPosition);
                validate(expectedPosition, arrow);
            } else { //check every caret position
                for (int caretPosition1 = 0; caretPosition1 < input.length(); caretPosition1++) {
                    Arrow arrow = getArrow(caretPosition1);
                    if (isCaretOnArrow(caretPosition1, expectedPosition)) {
                        validate(expectedPosition, arrow);
                    } else {
                        assertFalse(arrow.isValid());
                    }
                }
            }
        }

        private void validate(int[] expectedPosition, Arrow arrow) {
            try {
                assertEquals(arrow.toString(), expectValidArrow, arrow.isValid());
                if (expectValidArrow) {
                    int[] actuals = {arrow.getStart(), arrow.getEnd()};
                    assertArrayEquals(Arrays.toString(expectedPosition) + " != " + Arrays.toString(actuals), expectedPosition, actuals);
                }
            } catch (AssertionError e) {
                //for debugging
                throw e;
            }
        }

        private boolean fixedCaretPosition() {
            return caretPosition != -1;
        }

        private Arrow getArrow(int caretPosition) {
            return Arrow.from(caretPosition, input.toCharArray());
        }

    }

    private static boolean isCaretOnArrow(int caret, int[] expectedPosition) {
        return expectedPosition[0] <= caret && caret <= expectedPosition[1] + 1;
    }

}