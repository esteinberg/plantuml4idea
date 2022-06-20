package org.plantuml.idea.adapter.rendering;

import com.intellij.openapi.diagnostic.Logger;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.adapter.Format;
import org.plantuml.idea.rendering.*;
import org.plantuml.idea.settings.PlantUmlSettings;

import java.io.IOException;


public class PlantUmlNormalRenderer {
    protected static final Logger logger = Logger.getInstance(PlantUmlNormalRenderer.class);
    protected static final FileFormatOption SVG = new FileFormatOption(FileFormat.SVG);

    public RenderResult doRender(RenderRequest renderRequest, RenderCacheItem cachedItem, String[] sourceSplit) {
        try {
            long start = System.currentTimeMillis();
            DiagramFactory diagramFactory = DiagramFactory.create(renderRequest, renderRequest.getSource());
            PlantUmlSettings settings = PlantUmlSettings.getInstance();

            int totalPages = diagramFactory.getTotalPages();

            if (totalPages == 0) {
                return new RenderResult(RenderingType.NORMAL, 0);
            }

            //image/error is not rendered when page >= totalPages
            int renderRequestPage = renderRequest.getPage();
            if (renderRequestPage >= totalPages) {
                renderRequestPage = -1;
            }

            FileFormatOption formatOption = new FileFormatOption(Format.from(renderRequest.getFormat()), settings.isGenerateMetadata());

            boolean containsIncludedNewPage = sourceSplit.length != totalPages;


            logger.debug("splitByNewPage.length=", sourceSplit.length, ", totalPages=", totalPages, ", cachedPages=", cachedItem != null ? cachedItem.getImageItems().length : null);
            boolean incrementalRendering =
                    cachedItem != null
                            && !RenderingType.NORMAL.renderingTypeChanged(cachedItem)
                            && !containsIncludedNewPage
                            && !cachedPageCountChanged(cachedItem, totalPages);

            logger.debug("incremental rendering=", incrementalRendering, ", totalPages=", totalPages);
            logger.debug("sum of all before render ", System.currentTimeMillis() - start, "ms");


            RenderResult renderResult = new RenderResult(RenderingType.NORMAL, totalPages);
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
        } catch (UnsupportedOperationException | RenderingCancelledException e) {
            throw e;
        } catch (Throwable e) {
            logger.error("Failed to render image " + renderRequest.getSource(), e);
            return new RenderResult(RenderingType.NORMAL, 0);
        }
    }

    private void incrementalRendering(RenderRequest renderRequest, RenderCacheItem cachedItem, String[] sourceSplit, String documentSource, DiagramFactory factory, RenderResult renderResult, FileFormatOption formatOption, int page, boolean pageRequested) throws IOException {
        boolean obsolete = renderRequest.requestedRefreshOrIncludesChanged()
                || cachedItem.zoomChanged(renderRequest)
                || cachedItem.sourceChanged(sourceSplit, page)
                || cachedItem.differentFormat(renderRequest)
                || cachedItem.titleChanged(page, factory.getTitle(page));

        boolean shouldRender = pageRequested && (obsolete || cachedItem.imageMissing(page));

        if (shouldRender) {
            ImageItem imageItem = factory.generateImageItem(renderRequest, documentSource, sourceSplit[page], formatOption, page, page, RenderingType.NORMAL);
            renderResult.addRenderedImage(imageItem);
        } else if (obsolete) {
            logger.debug("page ", page, "  title only");
            renderResult.addUpdatedTitle(new ImageItem(renderRequest.getBaseDir(), renderRequest.getFormat(), documentSource, sourceSplit[page], page, RenderResult.TITLE_ONLY, null, null, RenderingType.NORMAL, factory.getTitle(page), factory.getFilename(page), null));
        } else {
            logger.debug("page ", page, " cached");
            renderResult.addCachedImage(cachedItem.getImageItem(page));
        }
    }

    private void normalRendering(RenderRequest renderRequest, String[] sourceSplit, String documentSource, DiagramFactory factory, RenderResult renderResult, FileFormatOption formatOption, boolean containsIncludedNewPage, int page, boolean pageRequested) {
        String pageSource = pageSource(sourceSplit, containsIncludedNewPage, page);
        if (pageRequested) {
            ImageItem imageItem = factory.generateImageItem(renderRequest, documentSource, pageSource, formatOption, page, page, RenderingType.NORMAL);
            renderResult.addRenderedImage(imageItem);
        } else {
            logger.debug("page ", page, "  title only");
            ImageItem imageItem = new ImageItem(renderRequest.getBaseDir(), renderRequest.getFormat(), documentSource, pageSource, page, RenderResult.TITLE_ONLY, null, null, RenderingType.NORMAL, factory.getTitle(page), factory.getFilename(page), null);
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
