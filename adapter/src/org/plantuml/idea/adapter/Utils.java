package org.plantuml.idea.adapter;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.svg.MyTranscoder;
import com.intellij.util.ImageLoader;
import net.sourceforge.plantuml.FileSystem;
import net.sourceforge.plantuml.code.Transcoder;
import net.sourceforge.plantuml.code.TranscoderUtil;
import net.sourceforge.plantuml.cucadiagram.dot.GraphvizUtils;
import net.sourceforge.plantuml.security.SFile;
import net.sourceforge.plantuml.version.Version;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.lang.settings.PlantUmlSettings;
import org.w3c.dom.Document;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

public class Utils {
    private static final Logger LOG = Logger.getInstance(Utils.class);

    public static BufferedImage loadWithoutCache(@Nullable URL url, @NotNull InputStream stream, double scale, @Nullable ImageLoader.Dimension2DDouble docSize /*OUT*/) {
        try {
            MyTranscoder transcoder = MyTranscoder.createImage(scale, createTranscodeInput(url, stream));
            if (docSize != null) {
                docSize.setSize(transcoder.getOrigDocWidth(), transcoder.getOrigDocHeight());
            }
            return transcoder.getImage();
        } catch (Exception ex) {
            if (docSize != null) {
                docSize.setSize(0, 0);
            }
            throw new RuntimeException(ex);
        }
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
    public static Map<File, Long> prepareEnvironment(File baseDir, String source) {
        if (baseDir != null) {
            setPlantUmlDir(baseDir);
        } else {
            resetPlantUmlDir();
        }

        final Map<File, Long> includedFiles = PlantUmlIncludes.commitIncludes(source, baseDir);
        applyPlantumlOptions(PlantUmlSettings.getInstance());
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
}
