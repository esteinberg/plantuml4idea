package org.plantuml.idea.adapter.rendering;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.ex.VirtualFileManagerEx;
import com.intellij.util.io.URLUtil;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.adapter.Format;
import org.plantuml.idea.lang.settings.PlantUmlSettings;
import org.plantuml.idea.rendering.*;

import java.io.FileOutputStream;
import java.io.IOException;


public class PlantUmlNormalRenderer {
    protected static final Logger logger = Logger.getInstance(PlantUmlNormalRenderer.class);
    protected static final FileFormatOption SVG = new FileFormatOption(FileFormat.SVG);

    /**
     * Renders source code and saves diagram images to files according to provided naming scheme
     * and image format.
     *
     * @param renderRequest
     * @param source        source code to be rendered
     * @param path          path to use with first file
     * @throws IOException in case of rendering or saving fails
     */
    public void renderAndSave(RenderRequest renderRequest, String path, String pathPrefix)
            throws IOException {
        FileOutputStream outputStream = null;
        FileFormat pFormat = Format.from(renderRequest.getFormat());
        String fileSuffix = pFormat.getFileSuffix();
        int requestedPageNumber = renderRequest.getPage();
        try {
            DiagramFactory diagramFactory = DiagramFactory.create(renderRequest, renderRequest.getSource());

            VirtualFileManager vfm = VirtualFileManagerEx.getInstance();
            if (requestedPageNumber >= 0) {
                outputStream = new FileOutputStream(path);
                diagramFactory.outputImage(outputStream, requestedPageNumber, new FileFormatOption(pFormat));
                outputStream.close();
                vfm.refreshAndFindFileByUrl(VirtualFileManager.constructUrl(URLUtil.FILE_PROTOCOL, path));
            } else {
                if (pathPrefix == null) {
                    throw new IllegalArgumentException("pathPrefix is null");
                }
                PlantUmlSettings settings = PlantUmlSettings.getInstance();
                boolean usePageTitles = settings.isUsePageTitles();

                for (MyBlock block : diagramFactory.getBlockInfos()) {
                    net.sourceforge.plantuml.core.Diagram diagram = block.getDiagram();
                    int pages = block.getNbImages();
                    for (int page = 0; page < pages; ++page) {
                        String fPath;
                        if (usePageTitles) {
                            String titleOrPageNumber = block.getTitles().getTitleOrPageNumber(page);
                            if (page == 0 && pathPrefix.endsWith("-" + titleOrPageNumber)) {
                                pathPrefix = pathPrefix.substring(0, pathPrefix.length() - ("-" + titleOrPageNumber).length());
                            }
                            fPath = pathPrefix + "-" + titleOrPageNumber + fileSuffix;
                        } else {
                            if (page == 0) {
                                fPath = pathPrefix + fileSuffix;
                            } else {
                                fPath = pathPrefix + "-" + page + fileSuffix;
                            }
                        }
                        outputStream = new FileOutputStream(fPath);
                        try {
//                            reader.outputImage(outputStream, imageСounter++, new FileFormatOption(pFormat));
                            diagram.exportDiagram(outputStream, page++, new FileFormatOption(pFormat));
                        } finally {
                            outputStream.close();
                        }
                        vfm.refreshAndFindFileByUrl(VirtualFileManager.constructUrl(URLUtil.FILE_PROTOCOL, fPath));
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

    public RenderResult doRender(RenderRequest renderRequest, RenderCacheItem cachedItem, String[] sourceSplit) {
        try {
            long start = System.currentTimeMillis();
            DiagramFactory diagramFactory = DiagramFactory.create(renderRequest, renderRequest.getSource());

            int totalPages = diagramFactory.getTotalPages();

            if (totalPages == 0) {
                return new RenderResult(RenderingType.NORMAL, 0);
            }

            //image/error is not rendered when page >= totalPages
            int renderRequestPage = renderRequest.getPage();
            if (renderRequestPage >= totalPages) {
                renderRequestPage = -1;
            }
            RenderResult renderResult = new RenderResult(RenderingType.NORMAL, totalPages);

            FileFormatOption formatOption = new FileFormatOption(Format.from(renderRequest));

            boolean containsIncludedNewPage = sourceSplit.length != totalPages;


            logger.debug("splitByNewPage.length=", sourceSplit.length, ", totalPages=", totalPages, ", cachedPages=", cachedItem != null ? cachedItem.getImageItems().length : null);
            boolean incrementalRendering =
                    cachedItem != null
                            && !RenderingType.NORMAL.renderingTypeChanged(cachedItem)
                            && !containsIncludedNewPage
                            && !cachedPageCountChanged(cachedItem, totalPages);

            logger.debug("incremental rendering=", incrementalRendering, ", totalPages=", totalPages);
            logger.debug("sum of all before render ", System.currentTimeMillis() - start, "ms");


            for (int page = 0; page < totalPages; page++) {
                boolean pageRequested = renderRequestPage == -1 || renderRequestPage == page;
                if (incrementalRendering) {
                    incrementalRendering(renderRequest, cachedItem, sourceSplit, renderRequest.getSource(), diagramFactory, renderResult, formatOption, page, pageRequested);
                } else {
                    normalRendering(renderRequest, sourceSplit, renderRequest.getSource(), diagramFactory, renderResult, formatOption, containsIncludedNewPage, page, pageRequested);
                }
            }
            renderResult.setIncludedFiles(diagramFactory.getIncludedFiles());
            return renderResult;
        } catch (UnsupportedOperationException e) {
            throw e;
        } catch (RenderingCancelledException e) {
            throw e;
        } catch (Throwable e) {
            logger.error("Failed to render image " + renderRequest.getSource(), e);
            return new RenderResult(RenderingType.NORMAL, 0);
        }
    }

    private void incrementalRendering(RenderRequest renderRequest, RenderCacheItem cachedItem, String[] sourceSplit, String documentSource, DiagramFactory info, RenderResult renderResult, FileFormatOption formatOption, int page, boolean pageRequested) throws IOException {
        boolean obsolete = renderRequest.requestedRefreshOrIncludesChanged()
                || cachedItem.zoomChanged(renderRequest)
                || cachedItem.sourceChanged(sourceSplit, page)
                || cachedItem.titleChanged(page, info.getTitle(page));

        boolean shouldRender = pageRequested && (obsolete || cachedItem.imageMissing(page));

        if (shouldRender) {
            ImageItem imageItem = info.generateImageItem(renderRequest, documentSource, sourceSplit[page], formatOption, page, page, RenderingType.NORMAL);
            renderResult.addRenderedImage(imageItem);
        } else if (obsolete) {
            logger.debug("page ", page, "  title only");
            renderResult.addUpdatedTitle(new ImageItem(renderRequest.getBaseDir(), renderRequest.getFormat(), documentSource, sourceSplit[page], page, RenderResult.TITLE_ONLY, null, null, RenderingType.NORMAL, info.getTitle(page), info.getFilename(page)));
        } else {
            logger.debug("page ", page, " cached");
            renderResult.addCachedImage(cachedItem.getImageItem(page));
        }
    }

    private void normalRendering(RenderRequest renderRequest, String[] sourceSplit, String documentSource, DiagramFactory info, RenderResult renderResult, FileFormatOption formatOption, boolean containsIncludedNewPage, int page, boolean pageRequested) {
        String pageSource = pageSource(sourceSplit, containsIncludedNewPage, page);
        if (pageRequested) {
            ImageItem imageItem = info.generateImageItem(renderRequest, documentSource, pageSource, formatOption, page, page, RenderingType.NORMAL);
            renderResult.addRenderedImage(imageItem);
        } else {
            logger.debug("page ", page, "  title only");
            ImageItem imageItem = new ImageItem(renderRequest.getBaseDir(), renderRequest.getFormat(), documentSource, pageSource, page, RenderResult.TITLE_ONLY, null, null, RenderingType.NORMAL, info.getTitle(page), info.getFilename(page));
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


}
