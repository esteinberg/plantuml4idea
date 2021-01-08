package org.plantuml.idea.util;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.lang.PlantIUmlFileType;
import org.plantuml.idea.lang.PlantUmlFileType;

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
        return fileType.equals(PlantUmlFileType.PLANTUML_FILE_TYPE) || fileType.equals(PlantIUmlFileType.PLANTUML_FILE_TYPE);
    }

}
