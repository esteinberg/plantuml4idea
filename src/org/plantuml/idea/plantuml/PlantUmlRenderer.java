package org.plantuml.idea.plantuml;

import com.intellij.openapi.diagnostic.Logger;
import net.sourceforge.plantuml.*;
import net.sourceforge.plantuml.core.Diagram;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class PlantUmlRenderer {
    private static final Logger logger = Logger.getInstance(PlantUmlRenderer.class);


    /**
     * Renders source code and saves diagram images to files according to provided naming scheme
     * and image format.
     *
     * @param source         source code to be rendered
     * @param baseDir        base dir to set for "include" functionality
     * @param format         image format
     * @param fileName       fileName to use with first file
     * @param fileNameFormat file naming scheme for further files
     * @throws IOException in case of rendering or saving fails
     */
    public static void renderAndSave(String source, @Nullable File baseDir, PlantUml.ImageFormat format, String fileName, String fileNameFormat, int zoom)
            throws IOException {
        FileOutputStream outputStream = null;
        try {
            if (baseDir != null) {
                FileSystem.getInstance().setCurrentDir(baseDir);
            }
            PlantUmlIncludes.commitIncludes(source, baseDir);
            SourceStringReader reader = new SourceStringReader(source);

            List<BlockUml> blocks = reader.getBlocks();
            int imageСounter = 0;
            for (BlockUml block : blocks) {
                Diagram diagram = block.getDiagram();
                int pages = diagram.getNbImages();
                zoomDiagram(diagram, zoom);
                for (int page = 0; page < pages; ++page) {
                    String fName = imageСounter == 0 ? fileName : String.format(fileNameFormat, imageСounter);
                    outputStream = new FileOutputStream(fName);
                    reader.generateImage(outputStream, imageСounter++, new FileFormatOption(format.getFormat()));
                    outputStream.close();
                }
            }
        } finally {
            FileSystem.getInstance().reset();
            if (outputStream != null) {
                outputStream.close();
            }
        }

    }

    /**
     * Renders file with support of plantUML include ange paging features, setting base dir and page for plantUML
     * to provided values
     *
     * @param renderRequest
     * @return rendering result
     */
    public static PlantUmlResult render(RenderRequest renderRequest) {
        try {
            File baseDir = renderRequest.getBaseDir();
            if (baseDir != null) {
                FileSystem.getInstance().setCurrentDir(baseDir);
            }

            long start = System.currentTimeMillis();
            PlantUmlResult plantUmlResult = doRender(renderRequest);
            long total = System.currentTimeMillis() - start;
            logger.debug("rendered ", renderRequest.getFormat(), " in ", total, "ms");
            return plantUmlResult;
        } finally {
            FileSystem.getInstance().reset();
        }
    }

    /**
     * Renders given source code into diagram
     *
     * @param renderRequest
     * @return rendering result
     */
    private static PlantUmlResult doRender(RenderRequest renderRequest) {
        String desc = null;
        int totalPages = 0;

        try {
            // image generation.
            SourceStringReader reader = new SourceStringReader(renderRequest.getSource());

            List<BlockUml> blocks = reader.getBlocks();

            for (int i = 0; i < blocks.size(); i++) {
                BlockUml block = blocks.get(i);

                Diagram diagram = block.getDiagram();
                zoomDiagram(diagram, renderRequest.getZoom());

                totalPages = totalPages + diagram.getNbImages();
            }

            //image/error is not rendered when page >= totalPages
            if (renderRequest.getPage() >= totalPages) {
                renderRequest.setPage(-1);
            }

            PlantUmlResult.Diagram[] diagrams;
            FileFormatOption formatOption = new FileFormatOption(renderRequest.getFormat().getFormat());
            if (renderRequest.getPage() == -1) {//render all images
                diagrams = new PlantUmlResult.Diagram[totalPages];
                for (int i = 0; i < totalPages; i++) {
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    desc = reader.generateImage(os, i, formatOption);
                    diagrams[i] = new PlantUmlResult.Diagram(os.toByteArray());
                }
            } else {//render single image
                diagrams = new PlantUmlResult.Diagram[1];
                // Write the image to "os"
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                desc = reader.generateImage(os, renderRequest.getPage(), formatOption);
                diagrams[0] = new PlantUmlResult.Diagram(os.toByteArray());
            }

            return new PlantUmlResult(diagrams, desc, totalPages, renderRequest);
        } catch (Throwable e) {
            logger.error("Failed to render image " + renderRequest.getSource(), e);
            return new PlantUmlResult(desc, e.getMessage(), totalPages);
        }
    }

    static void zoomDiagram(Diagram diagram, int zoom) {
        if (diagram instanceof UmlDiagram) {
            UmlDiagram umlDiagram = (UmlDiagram) diagram;
            umlDiagram.setScale(new ScaleSimple(zoom / 100f));
        }
    }

}
