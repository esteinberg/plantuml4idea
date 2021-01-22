package org.plantuml.idea.adapter;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
//import com.intellij.ui.svg.MyTranscoder;
import com.intellij.util.ImageLoader;
import net.sourceforge.plantuml.BlockUml;
import net.sourceforge.plantuml.FileSystem;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.code.Transcoder;
import net.sourceforge.plantuml.code.TranscoderUtil;
import net.sourceforge.plantuml.cucadiagram.dot.GraphvizUtils;
import net.sourceforge.plantuml.preproc.FileWithSuffix;
import net.sourceforge.plantuml.security.SFile;
import net.sourceforge.plantuml.version.Version;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.lang.settings.PlantUmlSettings;
import org.plantuml.idea.rendering.RenderRequest;
import org.plantuml.idea.rendering.RenderingCancelledException;
import org.w3c.dom.Document;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class Utils {
    private static final Logger LOG = Logger.getInstance(Utils.class);

    public static BufferedImage loadWithoutCache(@Nullable URL url, @NotNull InputStream stream, double scale, @Nullable ImageLoader.Dimension2DDouble docSize /*OUT*/) {
//        try {
//            MyTranscoder transcoder = MyTranscoder.createImage(scale, createTranscodeInput(url, stream));
//            if (docSize != null) {
//                docSize.setSize(transcoder.getOrigDocWidth(), transcoder.getOrigDocHeight());
//            }
//            return transcoder.getImage();
//        } catch (Exception ex) {
//            if (docSize != null) {
//                docSize.setSize(0, 0);
//            }
//            throw new RuntimeException(ex);
//        }
        return null;
    }

    @NotNull
    private static TranscoderInput createTranscodeInput(@Nullable URL url, @NotNull InputStream stream) throws IOException {
        TranscoderInput myTranscoderInput;
        String uri = null;
        try {
            if (url != null && "jar".equals(url.getProtocol())) {
                // workaround for BATIK-1217
                url = new URL(url.getPath());
            }
            uri = url != null ? url.toURI().toString() : null;
        } catch (URISyntaxException ignore) {
        }

        Document document = new SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName()).createDocument(uri, stream);
        //          patchColors(url, document);
        myTranscoderInput = new TranscoderInput(document);
        return myTranscoderInput;
    }

    @NotNull
    public static void prepareEnvironment(RenderRequest renderRequest) {
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

    @NotNull
    public static LinkedHashMap<File, Long> getIncludedFiles(SourceStringReader reader) {
        long start = System.currentTimeMillis();
        LinkedHashMap<File, Long> includedFiles = new LinkedHashMap<>();
        List<BlockUml> blocks = reader.getBlocks();
        for (BlockUml block : blocks) {
            try {
                Set<File> convert = FileWithSuffix.convert(block.getIncluded());
                ArrayList<File> files = new ArrayList<>(convert);
                files.sort(File::compareTo);
                for (File file : files) {
                    includedFiles.put(file, file.lastModified());
                }
            } catch (FileNotFoundException e) {
                LOG.warn(e);
            }
        }
        LOG.debug("getIncludedFiles ", (System.currentTimeMillis() - start), "ms");
        return includedFiles;
    }

    public static void saveAllDocuments(@Nullable String sourceFilePath) {
        try {
            long start = System.currentTimeMillis();
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
        if (unsavedDocuments.length == 1 && sourceFile!=null) {
            com.intellij.openapi.editor.Document unsavedDocument = unsavedDocuments[0];
            VirtualFile file = documentManager.getFile(unsavedDocument);
            if (file != null) {
                return file.getPath().equals(sourceFile);
            }
        }
        return false;
        
    }
}
