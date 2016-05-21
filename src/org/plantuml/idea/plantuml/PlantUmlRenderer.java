package org.plantuml.idea.plantuml;

import com.intellij.openapi.diagnostic.Logger;
import net.sourceforge.plantuml.*;
import net.sourceforge.plantuml.core.Diagram;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.toolwindow.RenderCache;
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
    public static PlantUmlResult render(RenderRequest renderRequest, RenderCache.RenderCacheItem cachedItem) {
        try {
            File baseDir = renderRequest.getBaseDir();
            if (baseDir != null) {
                FileSystem.getInstance().setCurrentDir(baseDir);
            }

            long start = System.currentTimeMillis();
            PlantUmlResult plantUmlResult = doRender(renderRequest, cachedItem);
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
     * @param cachedItem
     * @return rendering result
     */
    private static PlantUmlResult doRender(RenderRequest renderRequest, RenderCache.RenderCacheItem cachedItem) {

        try {
            // image generation.
            SourceStringReader reader = new SourceStringReader(renderRequest.getSource());

            int totalPages = zoomDiagram(renderRequest, reader);

            //image/error is not rendered when page >= totalPages
            int renderRequestPage = renderRequest.getPage();
            if (renderRequestPage >= totalPages) {
                renderRequestPage = -1;
            }


            List<PlantUmlResult.Diagram> result = new ArrayList<PlantUmlResult.Diagram>();
            FileFormatOption formatOption = new FileFormatOption(renderRequest.getFormat().getFormat());


            if (cachedItem != null && totalPages > 1 && cachedItem.getImagesWithData().length == totalPages) {
                logger.debug("incremental rendering, totalPages=", totalPages);
                String cachedSource = cachedItem.getSource();
                String[] cachedSourceSplit = cachedSource.split("@newpage");
                String[] renderRequestSplit = renderRequest.getSource().split("@newpage");

                if (cachedSourceSplit.length == renderRequestSplit.length && renderRequestSplit.length == totalPages) {
                    if (renderRequestPage == -1) {
                        for (int i = 0; i < totalPages; i++) {
                            generateImageIfNecessary(cachedItem, reader, i, result, formatOption, cachedSourceSplit, renderRequestSplit);
                        }
                    } else {
                        generateImageIfNecessary(cachedItem, reader, renderRequestPage, result, formatOption, cachedSourceSplit, renderRequestSplit);
                    }
                }
            } else {
                if (renderRequestPage == -1) {//render all images
                    for (int i = 0; i < totalPages; i++) {
                        result.add(generateImage(reader, formatOption, i));
                    }
                } else {//render single image
                    result.add(generateImage(reader, formatOption, renderRequestPage));
                }
            }


            return new PlantUmlResult(result, totalPages, renderRequest);
        } catch (Throwable e) {
            logger.error("Failed to render image " + renderRequest.getSource(), e);
            return new PlantUmlResult(Collections.EMPTY_LIST, 0, renderRequest);
        }
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

    private static void generateImageIfNecessary(RenderCache.RenderCacheItem cachedItem, SourceStringReader reader, int renderRequestPage, List<PlantUmlResult.Diagram> result, FileFormatOption formatOption, String[] cachedSourceSplit, String[] renderRequestSplit) throws IOException {
        if (shouldGenerate(cachedItem, cachedSourceSplit, renderRequestSplit, renderRequestPage)) {
            result.add(generateImage(reader, formatOption, renderRequestPage));
        } else {
            logger.debug("page ", renderRequestPage, " no change");
        }
    }

    private static boolean shouldGenerate(RenderCache.RenderCacheItem cachedItem, String[] cachedSourceSplit, String[] renderRequestSplit, int i) {
        String renderRequestPiece = renderRequestSplit[i];
        String cachedSourcePiece = cachedSourceSplit[i];
        ImageWithUrlData imageWithUrlData = cachedItem.getImagesWithData()[i];
        return imageWithUrlData == null || !renderRequestPiece.equals(cachedSourcePiece);
    }

    @NotNull
    private static PlantUmlResult.Diagram generateImage(SourceStringReader reader, FileFormatOption formatOption, int i) throws IOException {
        logger.debug("generating ", formatOption.getFileFormat(), " for page ", i);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        reader.generateImage(os, i, formatOption);
        return new PlantUmlResult.Diagram(i, os.toByteArray());
    }

    static void zoomDiagram(Diagram diagram, int zoom) {
        if (diagram instanceof UmlDiagram) {
            UmlDiagram umlDiagram = (UmlDiagram) diagram;
            umlDiagram.setScale(new ScaleSimple(zoom / 100f));
        }
    }

}
