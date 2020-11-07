package org.plantuml.idea.rendering;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.ex.VirtualFileManagerEx;
import com.intellij.util.ObjectUtils;
import com.intellij.util.io.URLUtil;
import net.sourceforge.plantuml.BlockUml;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.Diagram;
import net.sourceforge.plantuml.core.DiagramDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.util.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static org.plantuml.idea.rendering.PlantUmlRendererUtil.checkCancel;
import static org.plantuml.idea.rendering.PlantUmlRendererUtil.zoomDiagram;


public class PlantUmlNormalRenderer {
    protected static final Logger logger = Logger.getInstance(PlantUmlNormalRenderer.class);
    protected static final FileFormatOption SVG = new FileFormatOption(FileFormat.SVG);
    public static final String TITLE_ONLY = "TITLE ONLY";

    /**
     * Renders source code and saves diagram images to files according to provided naming scheme
     * and image format.
     *
     * @param source              source code to be rendered
     * @param sourceFile
     * @param format              image format
     * @param path                path to use with first file
     * @param fileNameFormat      file naming scheme for further files
     * @param requestedPageNumber -1 for all pages
     * @throws IOException in case of rendering or saving fails
     */
    protected void renderAndSave(String source, File sourceFile, PlantUml.ImageFormat format, String path, String fileNameFormat, int zoom, int requestedPageNumber)
            throws IOException {
        FileOutputStream outputStream = null;
        try {

            SourceStringReader reader = Utils.newSourceStringReader(source, true, sourceFile);

            zoomDiagram(reader, zoom);

            VirtualFileManager vfm = VirtualFileManagerEx.getInstance();
            if (requestedPageNumber >= 0) {
                outputStream = new FileOutputStream(path);
                reader.outputImage(outputStream, requestedPageNumber, new FileFormatOption(format.getFormat()));
                outputStream.close();
                vfm.refreshAndFindFileByUrl(VirtualFileManager.constructUrl(URLUtil.FILE_PROTOCOL, path));
            } else {
                List<BlockUml> blocks = reader.getBlocks();
                int imageСounter = 0;

                for (BlockUml block : blocks) {
                    Diagram diagram = block.getDiagram();
                    int pages = diagram.getNbImages();
                    for (int page = 0; page < pages; ++page) {
                        String fName = imageСounter == 0 ? path : String.format(fileNameFormat, imageСounter);
                        outputStream = new FileOutputStream(fName);
                        try {
                            reader.outputImage(outputStream, imageСounter++, new FileFormatOption(format.getFormat()));
                        } finally {
                            outputStream.close();
                        }
                        vfm.refreshAndFindFileByUrl(VirtualFileManager.constructUrl(URLUtil.FILE_PROTOCOL, fName));
                    }
                    break;
                }
            }
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }

    }

    protected RenderResult doRender(RenderRequest renderRequest, RenderCacheItem cachedItem, String[] sourceSplit) {
        String documentSource = renderRequest.getSource();
        try {
            // image generation.                     
            SourceStringReader reader = Utils.newSourceStringReader(documentSource, renderRequest.isUseSettings(), renderRequest.getSourceFile());

            DiagramInfo info = zoomDiagram(reader, renderRequest.getZoom());
            Integer totalPages = info.getTotalPages();
            
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
                    incrementalRendering(renderRequest, cachedItem, sourceSplit, documentSource, reader, info, renderResult, formatOption, i, pageRequested);
                } else {
                    normalRendering(renderRequest, sourceSplit, documentSource, reader, info, renderResult, formatOption, containsIncludedNewPage, i, pageRequested);
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

    private void incrementalRendering(RenderRequest renderRequest, RenderCacheItem cachedItem, String[] sourceSplit, String documentSource, SourceStringReader reader, DiagramInfo info, RenderResult renderResult, FileFormatOption formatOption, int i, boolean pageRequested) throws IOException {
        boolean obsolete = renderRequest.requestedRefreshOrIncludesChanged()
                || renderRequest.getZoom() != cachedItem.getZoom()
                || !sourceSplit[i].equals(cachedItem.getImagesItemPageSource(i))
                || cachedItem.titleChaged(info.getTitle(i), i);

        boolean shouldRender = pageRequested && (obsolete || !cachedItem.hasImage(i));

        if (shouldRender) {
            ImageItem imageItem = generateImageItem(renderRequest, documentSource, sourceSplit[i], reader, formatOption, i, i, RenderingType.NORMAL, info.getTitle(i), info.getFilename());
            renderResult.addRenderedImage(imageItem);
        } else if (obsolete) {
            logger.debug("page ", i, "  title only");
            renderResult.addUpdatedTitle(new ImageItem(renderRequest.getBaseDir(), renderRequest.getFormat(), documentSource, sourceSplit[i], i, TITLE_ONLY, null, null, RenderingType.NORMAL, info.getTitle(i), info.getFilename()));
        } else {
            logger.debug("page ", i, " cached");
            renderResult.addCachedImage(cachedItem.getImageItem(i));
        }
    }

    private void normalRendering(RenderRequest renderRequest, String[] sourceSplit, String documentSource, SourceStringReader reader, DiagramInfo info, RenderResult renderResult, FileFormatOption formatOption, boolean containsIncludedNewPage, int i, boolean pageRequested) throws IOException {
        String pageSource = pageSource(sourceSplit, containsIncludedNewPage, i);
        if (pageRequested) {
            ImageItem imageItem = generateImageItem(renderRequest, documentSource, pageSource, reader, formatOption, i, i, RenderingType.NORMAL, info.getTitle(i), info.getFilename());
            renderResult.addRenderedImage(imageItem);
        } else {
            logger.debug("page ", i, "  title only");
            ImageItem imageItem = new ImageItem(renderRequest.getBaseDir(), renderRequest.getFormat(), documentSource, pageSource, i, TITLE_ONLY, null, null, RenderingType.NORMAL, info.getTitle(i), info.getFilename());
            renderResult.addUpdatedTitle(imageItem);
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


    @NotNull
    protected ImageItem generateImageItem(RenderRequest renderRequest,
                                          String documentSource,
                                          @Nullable String pageSource,
                                          SourceStringReader reader,
                                          FileFormatOption formatOption,
                                          int page,
                                          int logPage,
                                          RenderingType renderingType,
                                          String title,
                                          String filename) throws IOException {
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
        byte[] bytes = imageStream.toByteArray();

        logger.debug("generated ", formatOption.getFileFormat(), " for page ", logPage, " in ", System.currentTimeMillis() - start, "ms");

        byte[] svgBytes = new byte[0];
        if (renderRequest.getFormat() == PlantUml.ImageFormat.SVG) {
            svgBytes = bytes;
        } else if (renderRequest.isRenderUrlLinks()) {
            svgBytes = generateSvg(reader, page);
        }


        ObjectUtils.assertNotNull(diagramDescription);
        String description = diagramDescription.getDescription();
        if (description != null && description.contains("entities")) {
            description = "ok";
        }

        return new ImageItem(renderRequest.getBaseDir(), renderRequest.getFormat(), documentSource, pageSource, page, description, bytes, svgBytes, renderingType, title, filename);
    }

    protected byte[] generateSvg(SourceStringReader reader, int i) throws IOException {
        long start = System.currentTimeMillis();
        ByteArrayOutputStream svgStream = new ByteArrayOutputStream();
        reader.outputImage(svgStream, i, SVG);
        byte[] svgBytes = svgStream.toByteArray();
        logger.debug("generated ", SVG.getFileFormat(), " for page ", i, " in ", System.currentTimeMillis() - start, "ms");
        return svgBytes;
    }


}
