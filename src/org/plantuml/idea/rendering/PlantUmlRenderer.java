package org.plantuml.idea.rendering;

import com.intellij.openapi.diagnostic.Logger;
import net.sourceforge.plantuml.*;
import net.sourceforge.plantuml.core.Diagram;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.plantuml.PlantUmlIncludes;
import org.plantuml.idea.util.ImageWithUrlData;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
     * @param cachedItem
     * @return rendering result
     */
    public static RenderResult render(RenderRequest renderRequest, RenderCacheItem cachedItem) {
        try {
            File baseDir = renderRequest.getBaseDir();
            if (baseDir != null) {
                FileSystem.getInstance().setCurrentDir(baseDir);
            }
            long start = System.currentTimeMillis();
            RenderResult renderResult = doRender(renderRequest, cachedItem);
            long total = System.currentTimeMillis() - start;
            logger.debug("rendered ", renderRequest.getFormat(), " in ", total, "ms");
            return renderResult;
        } finally {
            FileSystem.getInstance().reset();
        }
    }

    /**
     * Renders given source code into diagram
     *
     * @param renderRequest
     * @param cachedItem
     * @return rendering result
     */
    private static RenderResult doRender(RenderRequest renderRequest, RenderCacheItem cachedItem) {

        try {
            // image generation.
            SourceStringReader reader = new SourceStringReader(renderRequest.getSource());

            int totalPages = zoomDiagram(renderRequest, reader);

            //image/error is not rendered when page >= totalPages
            int renderRequestPage = renderRequest.getPage();
            if (renderRequestPage >= totalPages) {
                renderRequestPage = -1;
            }


            List<RenderResult.Diagram> result = new ArrayList<RenderResult.Diagram>();
            FileFormatOption formatOption = new FileFormatOption(renderRequest.getFormat().getFormat());
            boolean renderAll = false;

            if (cachedItem != null && totalPages > 1 && cachedItem.getImagesWithData().length == totalPages) {
                logger.debug("incremental rendering, totalPages=", totalPages);
                String[] renderRequestSplit = splitNewPage(renderRequest.getSource());

                if (renderRequestSplit.length == totalPages) {
                    if (renderRequestPage == -1) {
                        for (int i = 0; i < totalPages; i++) {
                            generateImageIfNecessary(cachedItem, reader, i, result, formatOption, renderRequestSplit);
                        }
                    } else {
                        generateImageIfNecessary(cachedItem, reader, renderRequestPage, result, formatOption, renderRequestSplit);
                    }
                } else {
                    logger.debug("number of pages changed, or included file contains a newpage");
                    renderAll = true;
                }
            } else {
                logger.debug("no incremental rendering.  totalPages=", totalPages, ", cachedPages=", cachedItem != null ? cachedItem.getImagesWithData().length : null);
                renderAll = true;
            }


            if (renderAll) {
                logger.debug("render all");
                if (renderRequestPage == -1) {//render all images
                    for (int i = 0; i < totalPages; i++) {
                        result.add(generateImage(reader, formatOption, i));
                    }
                } else {//render single image
                    result.add(generateImage(reader, formatOption, renderRequestPage));
                }
            }
            logger.debug("RenderResult totalPages=", totalPages);
            return new RenderResult(result, totalPages);
        } catch (Throwable e) {
            logger.error("Failed to render image " + renderRequest.getSource(), e);
            return new RenderResult(Collections.EMPTY_LIST, 0);
        }
    }

    @NotNull
    public static String[] splitNewPage(String source) {
        return source.split("\\n\\s*@?newpage\\s*\\n");
    }

    private static int zoomDiagram(RenderRequest renderRequest, SourceStringReader reader) {
        int totalPages = 0;
        List<BlockUml> blocks = reader.getBlocks();

        for (int i = 0; i < blocks.size(); i++) {
            BlockUml block = blocks.get(i);

            Diagram diagram = block.getDiagram();
            zoomDiagram(diagram, renderRequest.getZoom());

            totalPages = totalPages + diagram.getNbImages();
        }
        return totalPages;
    }

    private static void generateImageIfNecessary(RenderCacheItem cachedItem, SourceStringReader reader, int renderRequestPage, List<RenderResult.Diagram> result, FileFormatOption formatOption, String[] renderRequestSplit) throws IOException {
        if (shouldGenerate(cachedItem, renderRequestSplit, renderRequestPage)) {
            result.add(generateImage(reader, formatOption, renderRequestPage));
        } else {
            logger.debug("page ", renderRequestPage, " no change");
        }
    }

    private static boolean shouldGenerate(RenderCacheItem cachedItem, String[] renderRequestSplit, int i) {
        ImageWithUrlData imageWithUrlData = cachedItem.getImagesWithData()[i];
        if (imageWithUrlData == null) return true;

        String source = imageWithUrlData.getSource();
        if (source == null) return true;

        String[] split = splitNewPage(source);
        if (split.length != renderRequestSplit.length) return true;

        String renderRequestPiece = renderRequestSplit[i];
        String cachedSourcePiece = split[i];
        if (!renderRequestPiece.equals(cachedSourcePiece)) return true;

        return false;
    }

    @NotNull
    private static RenderResult.Diagram generateImage(SourceStringReader reader, FileFormatOption formatOption, int i) throws IOException {
        long start = System.currentTimeMillis();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String description = reader.generateImage(os, i, formatOption);
        long total = System.currentTimeMillis() - start;
        RenderResult.Diagram diagram = new RenderResult.Diagram(i, description, os.toByteArray());
        logger.debug("generated ", formatOption.getFileFormat(), " for page ", i, " in ", total, "ms");
        return diagram;
    }

    static void zoomDiagram(Diagram diagram, int zoom) {
        if (diagram instanceof UmlDiagram) {
            UmlDiagram umlDiagram = (UmlDiagram) diagram;
            umlDiagram.setScale(new ScaleSimple(zoom / 100f));
        }
    }

}
