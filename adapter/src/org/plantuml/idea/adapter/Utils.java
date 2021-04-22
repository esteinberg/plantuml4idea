package org.plantuml.idea.adapter;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import net.sourceforge.plantuml.FileSystem;
import net.sourceforge.plantuml.OptionFlags;
import net.sourceforge.plantuml.code.Transcoder;
import net.sourceforge.plantuml.code.TranscoderUtil;
import net.sourceforge.plantuml.cucadiagram.dot.GraphvizUtils;
import net.sourceforge.plantuml.security.SFile;
import net.sourceforge.plantuml.version.Version;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.settings.PlantUmlSettings;
import org.plantuml.idea.rendering.RenderRequest;
import org.plantuml.idea.rendering.RenderingCancelledException;

import java.io.File;
import java.io.IOException;

public class Utils {
    private static final Logger LOG = Logger.getInstance(Utils.class);

    @NotNull
    public static void prepareEnvironment(RenderRequest renderRequest) {
        OptionFlags.getInstance().setVerbose(LOG.isDebugEnabled());
        
        long start = System.currentTimeMillis();
        File baseDir = renderRequest.getBaseDir();
        if (baseDir != null) {
            setPlantUmlDir(baseDir);
        } else {
            resetPlantUmlDir();
        }

        saveAllDocuments(renderRequest.getSourceFilePath());
        applyPlantumlOptions(PlantUmlSettings.getInstance());
        LOG.debug("prepareEnvironment done ", System.currentTimeMillis() - start, "ms");
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

    public static String version() {
        return Version.versionString();
    }

    public static void applyPlantumlOptions(PlantUmlSettings plantUmlSettings) {
        boolean blank = StringUtils.isBlank(System.getProperty("GRAPHVIZ_DOT"));
        boolean blank1 = StringUtils.isBlank(System.getenv("GRAPHVIZ_DOT"));
        boolean propertyNotSet = blank && blank1;
        boolean propertySet = !blank || !blank1;

        if (propertyNotSet || (propertySet && !plantUmlSettings.isUsePreferentiallyGRAPHIZ_DOT())) {
            if (String.valueOf(plantUmlSettings.getDotExecutable()).isEmpty()) {
                GraphvizUtils.setDotExecutable(null);
            } else {
                GraphvizUtils.setDotExecutable(plantUmlSettings.getDotExecutable());
            }
        }

        if (StringUtils.isNotBlank(plantUmlSettings.getPLANTUML_LIMIT_SIZE())) {
            try {
                Integer.parseInt(plantUmlSettings.getPLANTUML_LIMIT_SIZE());
                System.setProperty("PLANTUML_LIMIT_SIZE", plantUmlSettings.getPLANTUML_LIMIT_SIZE());
            } catch (NumberFormatException e) {
                LOG.error("invalid PLANTUML_LIMIT_SIZE", e);
            }
        }
    }

    public static String encode(String source) throws IOException {
        Transcoder defaultTranscoder = TranscoderUtil.getDefaultTranscoder();
        return defaultTranscoder.encode(source);
    }

    public static void saveAllDocuments(@Nullable String sourceFilePath) {
        try {
            long start = System.currentTimeMillis();
            if (org.plantuml.idea.util.Utils.isUnitTest()) {
                return;
            }
            FileDocumentManager documentManager = FileDocumentManager.getInstance();
            com.intellij.openapi.editor.Document[] unsavedDocuments = documentManager.getUnsavedDocuments();
            if (unsavedDocuments.length > 0 && !onlyCurrentlyDisplayed(documentManager, unsavedDocuments, sourceFilePath)) {
                ApplicationManager.getApplication().invokeAndWait(documentManager::saveAllDocuments);
            }

            LOG.debug("saveAllDocuments ", (System.currentTimeMillis() - start), "ms");
        } catch (Throwable e) {
            Throwable cause = e.getCause();
            if (cause instanceof InterruptedException) {
                throw new RenderingCancelledException((InterruptedException) cause);
            }
            throw e;
        }
    }

    private static boolean onlyCurrentlyDisplayed(FileDocumentManager documentManager, com.intellij.openapi.editor.Document[] unsavedDocuments, @Nullable String sourceFile) {
        if (unsavedDocuments.length == 1 && sourceFile != null) {
            com.intellij.openapi.editor.Document unsavedDocument = unsavedDocuments[0];
            VirtualFile file = documentManager.getFile(unsavedDocument);
            if (file != null) {
                return file.getPath().equals(sourceFile);
            }
        }
        return false;

    }

}
