package org.plantuml.idea.util;

import net.sourceforge.plantuml.FileSystem;
import org.jetbrains.annotations.NotNull;

import java.io.File;

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

    public static void setPlantUmlDir(@NotNull File baseDir) {
        FileSystem.getInstance().setCurrentDir(baseDir);
        System.setProperty("plantuml.include.path", baseDir.getAbsolutePath());
    }
}
