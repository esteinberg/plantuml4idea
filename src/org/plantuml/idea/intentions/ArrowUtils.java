package org.plantuml.idea.intentions;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.apache.commons.lang.ArrayUtils;

public class ArrowUtils {

    private static HashBiMap<Character, Character> characterCharacterBiMap;
    private static BiMap<Character, Character> inverse;

    static {
        characterCharacterBiMap = HashBiMap.create();
        inverse = characterCharacterBiMap.inverse();
        characterCharacterBiMap.put('<', '>');
        characterCharacterBiMap.put('\\', '/');
        characterCharacterBiMap.put('[', ']');
    }

    /**
     * return only the arrow
     */
    public static char[] cutArrowAndReverse(char[] chars, int end1, int end2) {
        char[] subarray = ArrayUtils.subarray(chars, end1, Math.min(end2 + 1, chars.length));
        ArrayUtils.reverse(subarray);
        for (int i = 0; i < subarray.length; i++) {
            subarray[i] = reverseArrowChar(subarray[i]);
        }
        return subarray;
    }

    public static char reverseArrowChar(char aChar) {
        Character character = characterCharacterBiMap.get(aChar);
        if (character == null) {
            character = inverse.get(aChar);
        }
        if (character == null) {
            character = aChar;
        }
        return character;
    }
}
