package org.plantuml.idea.util;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.PsiFile;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.IntRange;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.lang.PlantIUmlFileType;
import org.plantuml.idea.lang.PlantUmlFileType;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Character.isLetter;

public class Utils {

    public static int asInt(String renderDelay, int defaultValue) {
        int i = defaultValue;
        //noinspection EmptyCatchBlock
        try {
            i = Integer.parseInt(renderDelay);
        } catch (NumberFormatException e) {
        }
        return i;
    }

    public static boolean isPlantUmlFileType(@NotNull PsiFile file) {
        FileType fileType = file.getFileType();
        return fileType.equals(PlantUmlFileType.INSTANCE) || fileType.equals(PlantIUmlFileType.PLANTUML_FILE_TYPE);
    }

    public static boolean containsLetters(CharSequence s) {
        for (int i = 0; i < s.length(); i++) {
            if (isLetter(s.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public static List<IntRange> rangesInside(String str, String open, String close) {
        if (str != null && !StringUtils.isEmpty(open) && !StringUtils.isEmpty(close)) {
            int strLen = str.length();
            if (strLen == 0) {
                return null;
            } else {
                int closeLen = close.length();
                int openLen = open.length();
                List<IntRange> ranges = null;

                int end;
                for (int pos = 0; pos < strLen - closeLen; pos = end + closeLen) {
                    int start = str.indexOf(open, pos);
                    if (start < 0) {
                        break;
                    }

                    start += openLen;
                    end = str.indexOf(close, start);
                    if (end < 0) {
                        break;
                    }
                    if (ranges == null) {
                        ranges = new ArrayList<>();
                    }
                    ranges.add(new IntRange(start, end));
                }

                return ranges;
            }
        } else {
            return null;
        }
    }

    public static List<IntRange> join(List<IntRange> r1, List<IntRange> r2) {
        if (r1 == null && r2 == null) {
            return null;
        } else if (r1 == null) {
            return r2;
        } else if (r2 == null) {
            return r1;
        } else {
            r1.addAll(r2);
            return r1;
        }
    }
}
