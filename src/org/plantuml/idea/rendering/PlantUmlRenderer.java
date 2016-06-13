package org.plantuml.idea.rendering;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Pair;
import net.sourceforge.plantuml.*;
import net.sourceforge.plantuml.core.Diagram;
import net.sourceforge.plantuml.cucadiagram.Display;
import net.sourceforge.plantuml.cucadiagram.DisplayPositionned;
import net.sourceforge.plantuml.descdiagram.DescriptionDiagram;
import net.sourceforge.plantuml.sequencediagram.Event;
import net.sourceforge.plantuml.sequencediagram.Newpage;
import net.sourceforge.plantuml.sequencediagram.SequenceDiagram;
import org.apache.commons.lang.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.plantuml.PlantUmlIncludes;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static org.plantuml.idea.lang.annotator.LanguageDescriptor.IDEA_PARTIAL_RENDER;

public class PlantUmlRenderer {
    private static final Logger logger = Logger.getInstance(PlantUmlRenderer.class);
    private static final Pattern PATTERN = Pattern.compile("\\n\\s*@?(?i)(newpage)(\\p{Blank}+[^\\n]+|\\p{Blank}*)(?=\\n)");
    private static final FileFormatOption SVG = new FileFormatOption(FileFormat.SVG);

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

            String source = renderRequest.getSource();
            String[] sourceSplit = splitByNewPage(source);
            logger.debug("split done ", System.currentTimeMillis() - start, "ms");

            boolean partialRender = sourceSplit[0].contains(IDEA_PARTIAL_RENDER);
            logger.debug("partialRender ", partialRender);

            RenderResult renderResult;
            if (partialRender) {
                renderResult = partialRender(renderRequest, cachedItem, start, sourceSplit);
            } else {
                renderResult = doRender(renderRequest, cachedItem, sourceSplit);
            }
            return renderResult;
        } finally {
            FileSystem.getInstance().reset();
        }
    }

    enum Strategy {
        PARTIAL,
        NORMAL;

        public boolean renderingTypeChanged(RenderCacheItem cachedItem) {
            return cachedItem != null && cachedItem.getRenderResult().getStrategy() != this;
        }
    }

    @NotNull
    public static RenderResult partialRender(RenderRequest renderRequest, RenderCacheItem cachedItem, long start, String[] sourceSplit) {
        List<RenderResult> renderResults = new ArrayList<RenderResult>();
        FileFormatOption formatOption = new FileFormatOption(renderRequest.getFormat().getFormat());
        boolean renderAll = cachedPageCountChanged(cachedItem, sourceSplit.length)
                || Strategy.PARTIAL.renderingTypeChanged(cachedItem)
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

    public static boolean cachedPageCountChanged(RenderCacheItem cachedItem, int pagesCount) {
        return cachedItem != null && pagesCount != cachedItem.getImageItems().length;
    }

    public static void renderPartial(RenderRequest renderRequest, RenderCacheItem cachedItem, List<RenderResult> renderResults, int page, String[] sources, FileFormatOption formatOption) {
        long partialPageProcessingStart = System.currentTimeMillis();
        String partialSource = "@startuml\n" + sources[page] + "\n@enduml";
        if (cachedItem == null
                || renderRequest.requestedRefreshOrIncludesChanged()
                || Strategy.PARTIAL.renderingTypeChanged(cachedItem)
                || !partialSource.equals(cachedItem.getImagesItemPageSource(page))) {
            generateImage(renderRequest, renderResults, page, formatOption, partialSource);
        } else {
            logger.debug("page ", page, " not changed");
        }
        logger.debug("partial page ", page, " rendering done in ", System.currentTimeMillis() - partialPageProcessingStart, "ms");
    }

    @NotNull
    public static RenderResult joinResults(String[] sourceSplit, List<RenderResult> renderResults, RenderCacheItem cachedItem) {
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
        renderResult = new RenderResult(Strategy.PARTIAL, allImageItems, sourceSplit.length, allTitles);
        return renderResult;
    }

    public static void generateImage(RenderRequest renderRequest, List<RenderResult> renderResults, int page, FileFormatOption formatOption, String partialSource) {
        logger.debug("rendering partially, page ", page);
        SourceStringReader reader = new SourceStringReader(partialSource);
        Pair<Integer, List<String>> pages = zoomDiagram(renderRequest, reader);
        Integer totalPages = pages.first;
        List<String> titles = pages.second;
        
        if (totalPages > 1) {
            throw new NotImplementedException("partial rendering not supported with @newpage in included file, and it won't be");
        }
        if (titles.size() > 1) {
            logger.warn("too many titles " + Arrays.toString(titles.toArray()) + ", partialSource=" + partialSource);
        }
        try {
            ImageItem imageItem = new ImageItem(page, generateImage(renderRequest, renderRequest.getSource(), partialSource, reader, formatOption, 0, page, RenderingType.PARTIAL));
            renderResults.add(new RenderResult(Strategy.PARTIAL, Arrays.asList(imageItem), 1, titles));
        } catch (RenderingCancelledException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Renders given source code into diagram
     *
     * @param renderRequest
     * @param cachedItem
     * @param sourceSplit
     * @return rendering result
     */
    private static RenderResult doRender(RenderRequest renderRequest, RenderCacheItem cachedItem, String[] sourceSplit) {
        String documentSource = renderRequest.getSource();
        try {
            // image generation.
            SourceStringReader reader = new SourceStringReader(documentSource);

            Pair<Integer, List<String>> pages = zoomDiagram(renderRequest, reader);
            Integer totalPages = pages.first;
            List<String> titles = pages.second;
            
            if (totalPages == 0) {
                return new RenderResult(Strategy.NORMAL, Collections.EMPTY_LIST, 0, titles);
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
                            && !Strategy.NORMAL.renderingTypeChanged(cachedItem)
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
                        result.add(generateImage(renderRequest, documentSource, pageSource, reader, formatOption, i, i, RenderingType.NORMAL));
                    }
                } else {//render single image
                    String pageSource = null;
                    if (!containsIncludedNewPage) {
                        pageSource = sourceSplit[renderRequestPage];
                    }
                    result.add(generateImage(renderRequest, documentSource, pageSource, reader, formatOption, renderRequestPage, renderRequestPage, RenderingType.NORMAL));
                }
            }
            logger.debug("RenderResult totalPages=", totalPages);
            return new RenderResult(Strategy.NORMAL, result, totalPages, titles);
        } catch (RenderingCancelledException e) {
            throw e;
        } catch (Throwable e) {
            logger.error("Failed to render image " + documentSource, e);
            return new RenderResult(Strategy.NORMAL, Collections.EMPTY_LIST, 0, Collections.EMPTY_LIST);
        }
    }

    @NotNull
    public static String[] splitByNewPage(String source) {
        return PATTERN.split(source);
    }

    private static Pair<Integer, List<String>> zoomDiagram(RenderRequest renderRequest, SourceStringReader reader) {
        logger.debug("zooming diagram");
        int totalPages = 0;
        List<BlockUml> blocks = reader.getBlocks();

        for (int i = 0; i < blocks.size(); i++) {
            BlockUml block = blocks.get(i);

            long start = System.currentTimeMillis();
            checkCancel();
            Diagram diagram = block.getDiagram();
            logger.debug("getDiagram done in  ", System.currentTimeMillis() - start, " ms");

            start = System.currentTimeMillis();
            zoomDiagram(diagram, renderRequest.getZoom());
            logger.debug("zoom diagram done in  ", System.currentTimeMillis() - start, " ms");

            totalPages = totalPages + diagram.getNbImages();
        }
        List<String> titles = getTitles(totalPages, blocks);
        return new Pair<Integer, List<String>>(totalPages, titles);
    }

    @NotNull
    private static List<String> getTitles(int totalPages, List<BlockUml> blocks) {
        List<String> titles = new ArrayList<String>(totalPages);
        for (BlockUml block : blocks) {
            Diagram diagram = block.getDiagram();
            if (diagram instanceof SequenceDiagram) {
                SequenceDiagram sequenceDiagram = (SequenceDiagram) diagram;
                addTitle(titles, sequenceDiagram.getTitle().getDisplay());
                List<Event> events = sequenceDiagram.events();
                for (Event event : events) {
                    if (event instanceof Newpage) {
                        Display title = ((Newpage) event).getTitle();
                        addTitle(titles, title);
                    }
                }
            } else if (diagram instanceof NewpagedDiagram) {
                NewpagedDiagram newpagedDiagram = (NewpagedDiagram) diagram;
                List<Diagram> diagrams = newpagedDiagram.getDiagrams();
                for (Diagram diagram1 : diagrams) {
                    if (diagram1 instanceof UmlDiagram) {
                        DisplayPositionned title = ((UmlDiagram) diagram1).getTitle();
                        addTitle(titles, title.getDisplay());
                    }
                }
            }
        }
        return titles;
    }

    private static void addTitle(List<String> titles, Display display) {
        if (display.size() > 0) {
            titles.add(display.asStringWithHiddenNewLine());
        } else {
            titles.add(null);
        }
    }

    private static void checkCancel() {
        if (Thread.currentThread().isInterrupted()) {
            throw new RenderingCancelledException();
        }
    }

    private static void generateImageIfNecessary(RenderRequest renderRequest, String documentSource, RenderCacheItem cachedItem, SourceStringReader reader, int i, List<ImageItem> result, FileFormatOption formatOption, String[] renderRequestSplit) throws IOException {
        if (shouldGenerate(renderRequest, cachedItem, renderRequestSplit, i)) {
            result.add(generateImage(renderRequest, documentSource, renderRequestSplit[i], reader, formatOption, i, i, RenderingType.NORMAL));
        } else {
            logger.debug("page ", i, " no change, updating source");
            cachedItem.getImageItems()[i].setDocumentSource(documentSource); //TODO needed?
        }
    }

    private static boolean shouldGenerate(RenderRequest renderRequest, RenderCacheItem cachedItem, String[] renderRequestSplit, int i) {
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
    private static ImageItem generateImage(RenderRequest renderRequest, String documentSource, String pageSource, SourceStringReader reader, FileFormatOption formatOption, int i, int logPage, RenderingType renderingType) throws IOException {
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

    private static byte[] generateSvg(SourceStringReader reader, int i) throws IOException {
        long start = System.currentTimeMillis();
        ByteArrayOutputStream svgStream = new ByteArrayOutputStream();
        reader.generateImage(svgStream, i, SVG);
        byte[] svgBytes = svgStream.toByteArray();
        logger.debug("generated ", SVG.getFileFormat(), " for page ", i, " in ", System.currentTimeMillis() - start, "ms");
        return svgBytes;
    }

    static void zoomDiagram(Diagram diagram, int zoom) {
        if (diagram instanceof UmlDiagram) {
            UmlDiagram umlDiagram = (UmlDiagram) diagram;
            Scale scale = umlDiagram.getScale();
            if (scale == null) {
                umlDiagram.setScale(new ScaleSimple(zoom / 100f));
            }
        } else if (diagram instanceof NewpagedDiagram) {
            NewpagedDiagram newpagedDiagram = (NewpagedDiagram) diagram;
            for (Diagram page : newpagedDiagram.getDiagrams()) {
                if (page instanceof DescriptionDiagram) {
                    DescriptionDiagram descriptionDiagram = (DescriptionDiagram) page;
                    Scale scale = descriptionDiagram.getScale();
                    if (scale == null) {
                        descriptionDiagram.setScale(new ScaleSimple(zoom / 100f));
                    }
                }
            }
        }
    }

    public enum RenderingType {
        PARTIAL,
        NORMAL
    }
}
