package org.plantuml.idea.rendering;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Pair;
import net.sourceforge.plantuml.*;
import net.sourceforge.plantuml.core.Diagram;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.plantuml.PlantUmlIncludes;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.plantuml.idea.rendering.PlantUmlRenderer.zoomDiagram;


public class PlantUmlNormalRenderer {
    protected static final Logger logger = Logger.getInstance(PlantUmlNormalRenderer.class);
    protected static final FileFormatOption SVG = new FileFormatOption(FileFormat.SVG);

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
    public void renderAndSave(String source, @Nullable File baseDir, PlantUml.ImageFormat format, String fileName, String fileNameFormat, int zoom)
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

            zoomDiagram(reader, zoom);

            for (BlockUml block : blocks) {
                Diagram diagram = block.getDiagram();
                int pages = diagram.getNbImages();
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

    protected RenderResult doRender(RenderRequest renderRequest, RenderCacheItem cachedItem, String[] sourceSplit) {
        String documentSource = renderRequest.getSource();
        try {
            // image generation.
            SourceStringReader reader = new SourceStringReader(documentSource);

            Pair<Integer, List<String>> pages = zoomDiagram(reader, renderRequest.getZoom());
            Integer totalPages = pages.first;
            List<String> titles = pages.second;

            if (totalPages == 0) {
                return new RenderResult(RenderingType.NORMAL, Collections.EMPTY_LIST, 0, titles);
            }

            //image/error is not rendered when page >= totalPages
            int renderRequestPage = renderRequest.getPage();
            if (renderRequestPage >= totalPages) {
                renderRequestPage = -1;
            }


            List<ImageItem> result = new ArrayList<ImageItem>();
            FileFormatOption formatOption = new FileFormatOption(renderRequest.getFormat().getFormat());

            boolean containsIncludedNewPage = sourceSplit.length != totalPages;

            logger.debug("splitByNewPage.length=", sourceSplit.length, ", totalPages=", totalPages, ", cachedPages=", cachedItem != null ? cachedItem.getImageItems().length : null);
            boolean incremenalRendering =
                    cachedItem != null
                            && !RenderingType.NORMAL.renderingTypeChanged(cachedItem)
                            && !containsIncludedNewPage
                            && !cachedPageCountChanged(cachedItem, totalPages);


            if (incremenalRendering) {
                logger.debug("incremental rendering, totalPages=", totalPages);
                if (renderRequestPage == -1) {
                    for (int i = 0; i < totalPages; i++) {
                        generateImageIfNecessary(renderRequest, documentSource, cachedItem, reader, i, result, formatOption, sourceSplit);
                    }
                } else {
                    generateImageIfNecessary(renderRequest, documentSource, cachedItem, reader, renderRequestPage, result, formatOption, sourceSplit);
                }
            }


            if (!incremenalRendering) {
                logger.debug("render all");
                if (renderRequestPage == -1) {//render all images
                    for (int i = 0; i < totalPages; i++) {
                        String pageSource = null;
                        if (!containsIncludedNewPage) {
                            pageSource = sourceSplit[i];
                        }
                        result.add(generateImageItem(renderRequest, documentSource, pageSource, reader, formatOption, i, i, RenderingType.NORMAL));
                    }
                } else {//render single image
                    String pageSource = null;
                    if (!containsIncludedNewPage) {
                        pageSource = sourceSplit[renderRequestPage];
                    }
                    result.add(generateImageItem(renderRequest, documentSource, pageSource, reader, formatOption, renderRequestPage, renderRequestPage, RenderingType.NORMAL));
                }
            }
            logger.debug("RenderResult totalPages=", totalPages);
            return new RenderResult(RenderingType.NORMAL, result, totalPages, titles);
        } catch (RenderingCancelledException e) {
            throw e;
        } catch (Throwable e) {
            logger.error("Failed to render image " + documentSource, e);
            return new RenderResult(RenderingType.NORMAL, Collections.EMPTY_LIST, 0, Collections.EMPTY_LIST);
        }
    }


    protected boolean cachedPageCountChanged(RenderCacheItem cachedItem, int pagesCount) {
        return cachedItem != null && pagesCount != cachedItem.getImageItems().length;
    }

    protected void checkCancel() {
        if (Thread.currentThread().isInterrupted()) {
            throw new RenderingCancelledException();
        }
    }

    protected void generateImageIfNecessary(RenderRequest renderRequest, String documentSource, RenderCacheItem cachedItem, SourceStringReader reader, int i, List<ImageItem> result, FileFormatOption formatOption, String[] renderRequestSplit) throws IOException {
        if (shouldGenerate(renderRequest, cachedItem, renderRequestSplit, i)) {
            result.add(generateImageItem(renderRequest, documentSource, renderRequestSplit[i], reader, formatOption, i, i, RenderingType.NORMAL));
        } else {
            logger.debug("page ", i, " no change, updating source");
            cachedItem.getImageItems()[i].setDocumentSource(documentSource); //TODO needed?
        }
    }

    protected boolean shouldGenerate(RenderRequest renderRequest, RenderCacheItem cachedItem, String[] renderRequestSplit, int i) {
        ImageItem cacheImage = cachedItem.getImageItems()[i];
        if (cacheImage == null) return true;

        if (renderRequest.requestedRefreshOrIncludesChanged()) {
            return true;
        }

        String renderRequestPiece = renderRequestSplit[i];
        if (!renderRequestPiece.equals(cacheImage.getPageSource())) return true;

        return false;
    }

    @NotNull
    protected ImageItem generateImageItem(RenderRequest renderRequest, String documentSource, String pageSource, SourceStringReader reader, FileFormatOption formatOption, int i, int logPage, RenderingType renderingType) throws IOException {
        checkCancel();
        long start = System.currentTimeMillis();

        ByteArrayOutputStream imageStream = new ByteArrayOutputStream();

        String description = null;
        try {
            description = reader.generateImage(imageStream, i, formatOption);
        } catch (Exception e) {
            throw new RenderingCancelledException(e);
        }

        logger.debug("generated ", formatOption.getFileFormat(), " for page ", logPage, " in ", System.currentTimeMillis() - start, "ms");

        byte[] svgBytes = new byte[0];
        if (renderRequest.isRenderUrlLinks()) {
            svgBytes = generateSvg(reader, i);
        }


        if (description.contains("entities")) {
            description = "ok";
        }

        return new ImageItem(renderRequest.getBaseDir(), documentSource, pageSource, i, description, imageStream.toByteArray(), svgBytes, renderingType);
    }

    protected byte[] generateSvg(SourceStringReader reader, int i) throws IOException {
        long start = System.currentTimeMillis();
        ByteArrayOutputStream svgStream = new ByteArrayOutputStream();
        reader.generateImage(svgStream, i, SVG);
        byte[] svgBytes = svgStream.toByteArray();
        logger.debug("generated ", SVG.getFileFormat(), " for page ", i, " in ", System.currentTimeMillis() - start, "ms");
        return svgBytes;
    }


}
