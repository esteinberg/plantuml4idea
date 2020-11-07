package org.plantuml.idea.util;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.PsiFile;
import net.sourceforge.plantuml.FileSystem;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.preproc.Defines;
import net.sourceforge.plantuml.security.SFile;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.lang.PlantIUmlFileType;
import org.plantuml.idea.lang.PlantUmlFileType;
import org.plantuml.idea.lang.settings.PlantUmlSettings;
import org.plantuml.idea.plantuml.PlantUmlIncludes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Utils {
    private static final Logger LOG = Logger.getInstance(Utils.class);

    public static int asInt(String renderDelay, int defaultValue) {
        int i = defaultValue;
        //noinspection EmptyCatchBlock
        try {
            i = Integer.parseInt(renderDelay);
        } catch (NumberFormatException e) {
        }
        return i;
    }

    @NotNull
    public static Map<File, Long> prepareEnvironment(File baseDir, String source) {
        if (baseDir != null) {
            setPlantUmlDir(baseDir);
        } else {
            resetPlantUmlDir();
        }

        final Map<File, Long> includedFiles = PlantUmlIncludes.commitIncludes(source, baseDir);
        PlantUmlSettings.getInstance().applyPlantumlOptions();
        return includedFiles;
    }

    public static void setPlantUmlDir(@NotNull File baseDir) {
        FileSystem.getInstance().setCurrentDir(new SFile(baseDir.toURI()));

        String includedPaths = PlantUmlSettings.getInstance().getIncludedPaths();
        String separator = System.getProperty("path.separator");

        StringBuilder sb = new StringBuilder();
        sb.append(baseDir.getAbsolutePath());

        if (StringUtils.isNotBlank(includedPaths)) {
            String[] split = includedPaths.split("\n");
            for (String s : split) {
                if (StringUtils.isNotBlank(s)) {
                    sb.append(separator);
                    sb.append(s);
                }
            }
        }

        System.setProperty("plantuml.include.path", sb.toString());
    }

    public static void resetPlantUmlDir() {
        FileSystem.getInstance().reset();
        System.clearProperty("plantuml.include.path");
    }


    @NotNull
    public static SourceStringReader newSourceStringReader(String source, boolean useSettings, File file) {
        List<String> configAsList;
        String encoding;
        if (useSettings) {
            PlantUmlSettings settings = PlantUmlSettings.getInstance();
            encoding = settings.getEncoding();
            configAsList = settings.getConfigAsList();
        } else {
            encoding = "UTF-8";
            configAsList = new ArrayList<>();

        }

        Defines defines;
        if (file != null) {
            defines = Defines.createWithFileName(file);
        } else {
            defines = Defines.createEmpty();
        }
        return new SourceStringReader(defines, source, encoding, configAsList);
    }

    public static boolean isPlantUmlFileType(@NotNull PsiFile file) {
        FileType fileType = file.getFileType();
        return fileType.equals(PlantUmlFileType.PLANTUML_FILE_TYPE) || fileType.equals(PlantIUmlFileType.PLANTUML_FILE_TYPE);
    }

}
