package org.plantuml.idea.rendering;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Pair;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import org.apache.commons.lang.NotImplementedException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.plantuml.idea.rendering.PlantUmlRenderer.zoomDiagram;

public class PlantUmlPartialRenderer extends PlantUmlNormalRenderer {
    private static final Logger logger = Logger.getInstance(PlantUmlPartialRenderer.class);


    @NotNull
    public RenderResult partialRender(RenderRequest renderRequest, RenderCacheItem cachedItem, long start, String[] sourceSplit) {
        List<RenderResult> renderResults = new ArrayList<RenderResult>();
        FileFormatOption formatOption = new FileFormatOption(renderRequest.getFormat().getFormat());
        boolean renderAll = cachedPageCountChanged(cachedItem, sourceSplit.length)
                || RenderingType.PARTIAL.renderingTypeChanged(cachedItem)
                || renderRequest.getPage() == -1
                || renderRequest.getPage() >= sourceSplit.length;

        if (renderAll) {
            logger.debug("render all pages ");
            for (int page = 0; page < sourceSplit.length; page++) {
                renderPartial(renderRequest, cachedItem, renderResults, page, sourceSplit, formatOption);
            }
        } else {
            logger.debug("render single page");
            renderPartial(renderRequest, cachedItem, renderResults, renderRequest.getPage(), sourceSplit, formatOption);
        }

        logger.debug("partial rendering done ", System.currentTimeMillis() - start, "ms");

        RenderResult renderResult = joinResults(sourceSplit, renderResults, cachedItem);
        logger.debug("result prepared ", System.currentTimeMillis() - start, "ms");
        return renderResult;
    }

    private void renderPartial(RenderRequest renderRequest, RenderCacheItem cachedItem, List<RenderResult> renderResults, int page, String[] sources, FileFormatOption formatOption) {
        long partialPageProcessingStart = System.currentTimeMillis();
        String partialSource = "@startuml\n" + sources[page] + "\n@enduml";
        if (cachedItem == null
                || renderRequest.requestedRefreshOrIncludesChanged()
                || RenderingType.PARTIAL.renderingTypeChanged(cachedItem)
                || !partialSource.equals(cachedItem.getImagesItemPageSource(page))) {
            renderImage(renderRequest, renderResults, page, formatOption, partialSource);
        } else {
            logger.debug("page ", page, " not changed");
        }
        logger.debug("partial page ", page, " rendering done in ", System.currentTimeMillis() - partialPageProcessingStart, "ms");
    }

    private void renderImage(RenderRequest renderRequest, List<RenderResult> renderResults, int page, FileFormatOption formatOption, String partialSource) {
        logger.debug("rendering partially, page ", page);
        SourceStringReader reader = new SourceStringReader(partialSource);
        Pair<Integer, List<String>> pages = zoomDiagram(reader, renderRequest.getZoom());
        Integer totalPages = pages.first;
        List<String> titles = pages.second;

        if (totalPages > 1) {
            throw new NotImplementedException("partial rendering not supported with @newpage in included file, and it won't be");
        }
        if (titles.size() > 1) {
            logger.warn("too many titles " + Arrays.toString(titles.toArray()) + ", partialSource=" + partialSource);
        }
        try {
            ImageItem imageItem = new ImageItem(page, generateImageItem(renderRequest, renderRequest.getSource(), partialSource, reader, formatOption, 0, page, RenderingType.PARTIAL));
            renderResults.add(new RenderResult(RenderingType.PARTIAL, Collections.singletonList(imageItem), 1, titles));
        } catch (RenderingCancelledException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private RenderResult joinResults(String[] sourceSplit, List<RenderResult> renderResults, RenderCacheItem cachedItem) {
        RenderResult renderResult;
        List<ImageItem> allImageItems = new ArrayList<ImageItem>();
        for (int i = 0; i < renderResults.size(); i++) {
            RenderResult x = renderResults.get(i);
            List<ImageItem> imageItems = x.getImageItems();
            for (int i1 = 0; i1 < imageItems.size(); i1++) {
                ImageItem imageItem = imageItems.get(i1);
                allImageItems.add(imageItem);
            }
        }
        List<String> allTitles = Arrays.asList(new String[sourceSplit.length]);
        if (cachedItem != null) {
            List<String> titles = cachedItem.getTitles();
            for (int i = 0; i < titles.size(); i++) {
                String s = titles.get(i);
                if (allTitles.size() > i) {
                    allTitles.set(i, s);
                }
            }
        }
        for (int i = 0; i < renderResults.size(); i++) {
            RenderResult result = renderResults.get(i);
            List<ImageItem> imageItems = result.getImageItems();
            ImageItem imageItem = imageItems.get(0);
            int page = imageItem.getPage();
            if (!result.getTitles().isEmpty()) {
                String element = result.getTitles().get(0);
                allTitles.set(page, element);
            }
        }
        renderResult = new RenderResult(RenderingType.PARTIAL, allImageItems, sourceSplit.length, allTitles);
        return renderResult;
    }


}
