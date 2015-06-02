package org.plantuml.idea.intentions;

public class Arrow {
    private int caretOffset;
    private char[] chars;
    private int end1 = -1;
    private int end2 = -1;

    public Arrow(int caretOffset, char... chars) {
        this.caretOffset = caretOffset;
        this.chars = chars;
    }

    public Arrow invoke() {
        if (caretWithinCharArray() && arrowCharAtCaret() || previousIsArrowChar()) {
            for (int i = caretOffset; i < chars.length; i++) {
                if (!checkPosition(i)) break;
            }
            if (arrowEndsNotFound()) {
                for (int i = caretOffset - 1; i >= 0; i--) {//must step one char left, for cases when we are on far right edge 
                    if (!checkPosition(i)) break;
                }
            }
        }
        return this;
    }

    private boolean checkPosition(int currentPosition) {
        char currentChar = chars[currentPosition];
        boolean currentCharIsPartOfArrow = isArrowChar(currentChar);
        boolean continueTraversing = currentCharIsPartOfArrow;

        if (currentCharIsArrowEdge(currentCharIsPartOfArrow, currentPosition)) {
            if (end1 == -1) {
                end1 = currentPosition;
                continueTraversing = true;
            } else {
                end2 = currentPosition;
                continueTraversing = false;
            }
        }
        return continueTraversing;
    }

    private boolean currentCharIsArrowEdge(boolean currentCharIsPartOfArrow, int currentPosition) {
        return currentCharIsPartOfArrow && notInMiddleOfArrow(currentPosition);
    }

    private boolean notInMiddleOfArrow(int currentPosition) {
        char charRight = chars.length > currentPosition + 1 ? chars[currentPosition + 1] : ' ';
        char charLeft = currentPosition - 1 >= 0 ? chars[currentPosition - 1] : ' ';
        return !(isArrowChar(charLeft) && isArrowChar(charRight));
    }

    private boolean arrowEndsNotFound() {
        return end1 == -1 || end2 == -1;
    }

    private boolean arrowCharAtCaret() {
        return isArrowChar(chars[caretOffset]);
    }

    private boolean previousIsArrowChar() {
        return caretOffset > 0 && isArrowChar(chars[caretOffset - 1]);
    }

    private boolean caretWithinCharArray() {
        return chars.length > caretOffset;
    }

    private boolean isArrowChar(char aChar) {
        return isArrowEndpoint(aChar) || isMiddleOfArrow(aChar);
    }

    private boolean isArrowEndpoint(char c) {
        return c == '<' || c == '>';
    }

    private boolean isMiddleOfArrow(char c) {
        return c == '-' || c == '.';
    }

    public int getEnd1() {
        return end1;
    }

    public int getEnd2() {
        return end2;
    }

    public boolean isValidArrow() {
        if (arrowEndsNotFound()) {
            return false;
        }
        return isArrowEndpoint(chars[end1]) || isArrowEndpoint(chars[end2]);
    }
}
