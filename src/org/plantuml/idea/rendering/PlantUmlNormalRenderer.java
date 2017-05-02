package org.plantuml.idea.rendering;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Pair;
import com.intellij.util.ObjectUtils;
import net.sourceforge.plantuml.*;
import net.sourceforge.plantuml.core.Diagram;
import net.sourceforge.plantuml.core.DiagramDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.plantuml.PlantUmlIncludes;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static org.plantuml.idea.rendering.PlantUmlRenderer.zoomDiagram;


public class PlantUmlNormalRenderer {
    protected static final Logger logger = Logger.getInstance(PlantUmlNormalRenderer.class);
    protected static final FileFormatOption SVG = new FileFormatOption(FileFormat.SVG);
    public static final String TITLE_ONLY = "TITLE ONLY";

    /**
     * Renders source code and saves diagram images to files according to provided naming scheme
     * and image format.
     *
     * @param source               source code to be rendered
     * @param baseDir              base dir to set for "include" functionality
     * @param format               image format
     * @param fileName             fileName to use with first file
     * @param fileNameFormat       file naming scheme for further files
     * @param requestedPageNumber  -1 for all pages   
     * @throws IOException in case of rendering or saving fails
     */
    public void renderAndSave(String source, @Nullable File baseDir, PlantUml.ImageFormat format, String fileName, String fileNameFormat, int zoom, int requestedPageNumber)
            throws IOException {
        FileOutputStream outputStream = null;
        try {
            if (baseDir != null) {
                FileSystem.getInstance().setCurrentDir(baseDir);
            }
            PlantUmlIncludes.commitIncludes(source, baseDir);
            SourceStringReader reader = new SourceStringReader(source);

            zoomDiagram(reader, zoom);

            if (requestedPageNumber >= 0) {
                outputStream = new FileOutputStream(fileName);
                reader.generateImage(outputStream, requestedPageNumber, new FileFormatOption(format.getFormat()));
                outputStream.close();
            } else {
                List<BlockUml> blocks = reader.getBlocks();
                int imageСounter = 0;

                for (BlockUml block : blocks) {
                    Diagram diagram = block.getDiagram();
                    int pages = diagram.getNbImages();
                    for (int page = 0; page < pages; ++page) {
                        String fName = imageСounter == 0 ? fileName : String.format(fileNameFormat, imageСounter);
                        outputStream = new FileOutputStream(fName);
                        try {
                            reader.generateImage(outputStream, imageСounter++, new FileFormatOption(format.getFormat()));
                        } finally {
                            outputStream.close();
                        }
                    }
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

            Pair<Integer, Titles> pages = zoomDiagram(reader, renderRequest.getZoom());
            Integer totalPages = pages.first;
            Titles titles = pages.second;

            if (totalPages == 0) {
                return new RenderResult(RenderingType.NORMAL, 0);
            }

            //image/error is not rendered when page >= totalPages
            int renderRequestPage = renderRequest.getPage();
            if (renderRequestPage >= totalPages) {
                renderRequestPage = -1;
            }
            RenderResult renderResult = new RenderResult(RenderingType.NORMAL, totalPages);

            FileFormatOption formatOption = new FileFormatOption(renderRequest.getFormat().getFormat());

            boolean containsIncludedNewPage = sourceSplit.length != totalPages;

            logger.debug("splitByNewPage.length=", sourceSplit.length, ", totalPages=", totalPages, ", cachedPages=", cachedItem != null ? cachedItem.getImageItems().length : null);
            boolean incrementalRendering =
                    cachedItem != null
                            && !RenderingType.NORMAL.renderingTypeChanged(cachedItem)
                            && !containsIncludedNewPage
                            && !cachedPageCountChanged(cachedItem, totalPages);

            logger.debug("incremental rendering=", incrementalRendering, ", totalPages=", totalPages);


            for (int i = 0; i < totalPages; i++) {
                boolean pageRequested = renderRequestPage == -1 || renderRequestPage == i;
                if (incrementalRendering) {
                    incrementalRendering(renderRequest, cachedItem, sourceSplit, documentSource, reader, titles, renderResult, formatOption, i, pageRequested);
                } else {
                    normalRendering(renderRequest, sourceSplit, documentSource, reader, titles, renderResult, formatOption, containsIncludedNewPage, i, pageRequested);
                } 
            }


            logger.debug("RenderResult totalPages=", totalPages);
            return renderResult;
        } catch (UnsupportedOperationException e) {
            throw e;
        } catch (RenderingCancelledException e) {
            throw e;
        } catch (Throwable e) {
            logger.error("Failed to render image " + documentSource, e);
            return new RenderResult(RenderingType.NORMAL, 0);
        }
    }

    private void incrementalRendering(RenderRequest renderRequest, RenderCacheItem cachedItem, String[] sourceSplit, String documentSource, SourceStringReader reader, Titles titles, RenderResult renderResult, FileFormatOption formatOption, int i, boolean pageRequested) throws IOException {
        boolean obsolete = renderRequest.requestedRefreshOrIncludesChanged()
                || renderRequest.getZoom() != cachedItem.getZoom()
                || !sourceSplit[i].equals(cachedItem.getImagesItemPageSource(i))
                || cachedItem.titleChaged(titles.get(i), i);

        boolean shouldRender = pageRequested && (obsolete || !cachedItem.hasImage(i));

        if (shouldRender) {
            renderResult.addRenderedImage(generateImageItem(renderRequest, documentSource, sourceSplit[i], reader, formatOption, i, i, RenderingType.NORMAL, titles.get(i)));
        } else if (obsolete) {
            logger.debug("page ", i, "  title only");
            renderResult.addUpdatedTitle(new ImageItem(renderRequest.getBaseDir(), documentSource, sourceSplit[i], i, TITLE_ONLY, null, null, RenderingType.NORMAL, titles.get(i)));
        } else {
            logger.debug("page ", i, " cached");
            renderResult.addCachedImage(cachedItem.getImageItem(i));
        }
    }

    private void normalRendering(RenderRequest renderRequest, String[] sourceSplit, String documentSource, SourceStringReader reader, Titles titles, RenderResult renderResult, FileFormatOption formatOption, boolean containsIncludedNewPage, int i, boolean pageRequested) throws IOException {
        String pageSource = pageSource(sourceSplit, containsIncludedNewPage, i);
        if (pageRequested) {
            renderResult.addRenderedImage(generateImageItem(renderRequest, documentSource, pageSource, reader, formatOption, i, i, RenderingType.NORMAL, titles.get(i)));
        } else {
            logger.debug("page ", i, "  title only");
            renderResult.addUpdatedTitle(new ImageItem(renderRequest.getBaseDir(), documentSource, pageSource, i, TITLE_ONLY, null, null, RenderingType.NORMAL, titles.get(i)));
        }
    }

    @Nullable
    private String pageSource(String[] sourceSplit, boolean containsIncludedNewPage, int i) {
        String pageSource = null;
        if (!containsIncludedNewPage) {
            pageSource = sourceSplit[i];
        }
        return pageSource;
    }


    protected boolean cachedPageCountChanged(RenderCacheItem cachedItem, int pagesCount) {
        return cachedItem != null && pagesCount != cachedItem.getImageItems().length;
    }

    protected void checkCancel() {
        if (Thread.currentThread().isInterrupted()) {
            throw new RenderingCancelledException();
        }
    }

    @NotNull
    protected ImageItem generateImageItem(RenderRequest renderRequest,
                                          String documentSource,
                                          @Nullable String pageSource,
                                          SourceStringReader reader,
                                          FileFormatOption formatOption,
                                          int page,
                                          int logPage,
                                          RenderingType renderingType,
                                          String title) throws IOException {
        checkCancel();
        long start = System.currentTimeMillis();

        ByteArrayOutputStream imageStream = new ByteArrayOutputStream();

        DiagramDescription diagramDescription;
        try {
            diagramDescription = reader.outputImage(imageStream, page, formatOption);
        } catch (UnsupportedOperationException e) {
            throw e;
        } catch (Exception e) {
            throw new RenderingCancelledException(e);
        }

        logger.debug("generated ", formatOption.getFileFormat(), " for page ", logPage, " in ", System.currentTimeMillis() - start, "ms");

        byte[] svgBytes = new byte[0];
        if (renderRequest.isRenderUrlLinks()) {
            svgBytes = generateSvg(reader, page);
        }


        ObjectUtils.assertNotNull(diagramDescription);
        String description = diagramDescription.getDescription();
        if (description != null && description.contains("entities")) {
            description = "ok";
        }

        return new ImageItem(renderRequest.getBaseDir(), documentSource, pageSource, page, description, imageStream.toByteArray(), svgBytes, renderingType, title);
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
