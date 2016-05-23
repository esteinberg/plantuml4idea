package org.plantuml.idea.rendering;

import com.intellij.openapi.diagnostic.Logger;
import net.sourceforge.plantuml.*;
import net.sourceforge.plantuml.core.Diagram;
import net.sourceforge.plantuml.descdiagram.DescriptionDiagram;
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
    private static final Pattern PATTERN = Pattern.compile("\\n\\s*@?newpage(\\p{Blank}+[^\\n]+|\\p{Blank}*)(?=\\n)");
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
            String[] sourceSplit = splitNewPage(source);
            logger.debug("split done ", System.currentTimeMillis() - start, "ms");

            boolean partialRender = sourceSplit[0].contains(IDEA_PARTIAL_RENDER);
            logger.debug("partialRender ", partialRender);

            RenderResult renderResult;
            if (partialRender) {
                renderResult = partialRender(renderRequest, cachedItem, start, sourceSplit);
            } else {
                renderResult = doRender(renderRequest, cachedItem, source);
            }
            return renderResult;
        } finally {
            FileSystem.getInstance().reset();
        }
    }

    enum Strategy {
        PARTIAL,
        NORMAL
    }
    @NotNull
    public static RenderResult partialRender(RenderRequest renderRequest, RenderCacheItem cachedItem, long start, String[] sourceSplit) {
        List<RenderResult> renderResults = new ArrayList<RenderResult>();
//        if (renderRequest.getPage() == -1) {
        for (int page = 0; page < sourceSplit.length; page++) {
            renderPartial(renderRequest, cachedItem, renderResults, page, sourceSplit);
        }
//        } else {
//            renderPartial(renderRequest, cachedItem, renderResults, renderRequest.getPage(), sourceSplit);
//        }

        logger.debug("partial rendering done ", System.currentTimeMillis() - start, "ms");

        RenderResult renderResult = joinResults(sourceSplit, renderResults);
        logger.debug("result prepared ", System.currentTimeMillis() - start, "ms");
        return renderResult;
    }

    @NotNull
    public static RenderResult joinResults(String[] sourceSplit, List<RenderResult> renderResults) {
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
        renderResult = new RenderResult(Strategy.PARTIAL, allImageItems, sourceSplit.length);
        return renderResult;
    }

    public static void renderPartial(RenderRequest renderRequest, RenderCacheItem cachedItem, List<RenderResult> renderResults, int page, String[] sources) {
        long partialPageProcessingStart = System.currentTimeMillis();
        String partialSource = "@startuml\n" + sources[page] + "\n@enduml";
        if (cachedItem == null || !partialSource.equals(cachedItem.getImagesWithDataPageSource(page))) {
            logger.debug("rendering partially, page ", page);
            SourceStringReader reader = new SourceStringReader(partialSource);
            int totalPages = zoomDiagram(renderRequest, reader);
            if (totalPages > 1) {
                //todo multi image diagram for included pages with newpage
                throw new RuntimeException("partial rendering not supported with @newpage in included file");
            }
            FileFormatOption formatOption = new FileFormatOption(renderRequest.getFormat().getFormat());
            try {
                ImageItem imageItem = new ImageItem(page, generateImage(renderRequest, renderRequest.getSource(), partialSource, reader, formatOption, 0, page));
                renderResults.add(new RenderResult(Strategy.PARTIAL, Arrays.asList(imageItem), 1));
            } catch (RenderingCancelledException e) {
                throw e;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        } else {
            logger.debug("page ", page, " not changed");
        }
        logger.debug("partial page ", page, " rendering done in ", System.currentTimeMillis() - partialPageProcessingStart, "ms");
    }

    /**
     * Renders given source code into diagram
     *
     * @param renderRequest
     * @param cachedItem
     * @param documentSource
     * @return rendering result
     */
    private static RenderResult doRender(RenderRequest renderRequest, RenderCacheItem cachedItem, String documentSource) {

        try {
            // image generation.
            SourceStringReader reader = new SourceStringReader(documentSource);

            int totalPages = zoomDiagram(renderRequest, reader);

            //image/error is not rendered when page >= totalPages
            int renderRequestPage = renderRequest.getPage();
            if (renderRequestPage >= totalPages) {
                renderRequestPage = -1;
            }


            List<ImageItem> result = new ArrayList<ImageItem>();
            FileFormatOption formatOption = new FileFormatOption(renderRequest.getFormat().getFormat());
            boolean renderAll = false;

            String[] renderRequestSplit = new String[0];
            boolean numberOfPagesChanged = cachedItem != null && cachedItem.getImageItems().length != totalPages;
            if (!numberOfPagesChanged) {
                renderRequestSplit = splitNewPage(documentSource);
            }
            if (totalPages > 1 && cachedItem != null && !numberOfPagesChanged) {
                logger.debug("incremental rendering, totalPages=", totalPages);

                if (renderRequestSplit.length == totalPages) {
                    if (renderRequestPage == -1) {
                        for (int i = 0; i < totalPages; i++) {
                            generateImageIfNecessary(renderRequest, documentSource, cachedItem, reader, i, result, formatOption, renderRequestSplit);
                        }
                    } else {
                        generateImageIfNecessary(renderRequest, documentSource, cachedItem, reader, renderRequestPage, result, formatOption, renderRequestSplit);
                    }
                } else {
                    logger.debug("number of pages changed, or included file contains a newpage");
                    renderAll = true;
                }
            } else {
                logger.debug("no incremental rendering.  totalPages=", totalPages, ", cachedPages=", cachedItem != null ? cachedItem.getImageItems().length : null);
                renderAll = true;
            }


            if (renderAll) {
                logger.debug("render all");
                if (renderRequestPage == -1) {//render all images
                    for (int i = 0; i < totalPages; i++) {
                        String pageSource = null;
                        if (renderRequestSplit.length == totalPages) {
                            pageSource = renderRequestSplit[i];
                        }
                        result.add(generateImage(renderRequest, documentSource, pageSource, reader, formatOption, i, i));
                    }
                } else {//render single image
                    String pageSource = null;
                    if (renderRequestSplit.length == totalPages) {
                        pageSource = renderRequestSplit[renderRequestPage];
                    }
                    result.add(generateImage(renderRequest, documentSource, pageSource, reader, formatOption, renderRequestPage, renderRequestPage));
                }
            }
            logger.debug("RenderResult totalPages=", totalPages);
            return new RenderResult(Strategy.NORMAL, result, totalPages);
        } catch (RenderingCancelledException e) {
            throw e;
        } catch (Throwable e) {
            logger.error("Failed to render image " + documentSource, e);
            return new RenderResult(Strategy.NORMAL, Collections.EMPTY_LIST, 0);
        }
    }

    @NotNull
    public static String[] splitNewPage(String source) {
        return PATTERN.split(source);
    }

    private static int zoomDiagram(RenderRequest renderRequest, SourceStringReader reader) {
        logger.debug("zoooming diagram");
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

        return totalPages;
    }

    private static void checkCancel() {
        if (Thread.currentThread().isInterrupted()) {
            throw new RenderingCancelledException();
        }
    }

    private static void generateImageIfNecessary(RenderRequest renderRequest, String documentSource, RenderCacheItem cachedItem, SourceStringReader reader, int renderRequestPage, List<ImageItem> result, FileFormatOption formatOption, String[] renderRequestSplit) throws IOException {
        if (shouldGenerate(documentSource, cachedItem, renderRequestSplit, renderRequestPage)) {
            result.add(generateImage(renderRequest, documentSource, renderRequestSplit[renderRequestPage], reader, formatOption, renderRequestPage, renderRequestPage));
        } else {
            logger.debug("page ", renderRequestPage, " no change");
        }
    }

    private static boolean shouldGenerate(String documentSource, RenderCacheItem cachedItem, String[] renderRequestSplit, int i) {
        ImageItem diagram = cachedItem.getImageItems()[i];
        if (diagram == null) return true;

        String renderRequestPiece = renderRequestSplit[i];
        if (!renderRequestPiece.equals(diagram.getPageSource())) return true;

        String pageSource = diagram.getPageSource();
        if (pageSource == null && documentSource.equals(diagram.getDocumentSource())) {
            diagram.setPageSource(pageSource);
            return false;
        }

        return false;
    }

    @NotNull
    private static ImageItem generateImage(RenderRequest renderRequest, String documentSource, String pageSource, SourceStringReader reader, FileFormatOption formatOption, int i, int logPage) throws IOException {
        checkCancel();

        ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
        String description = null;
        byte[] svgBytes = new byte[0];

        try {
            long start = System.currentTimeMillis();
            description = reader.generateImage(imageStream, i, formatOption);
            logger.debug("generated ", formatOption.getFileFormat(), " for page ", logPage, " in ", System.currentTimeMillis() - start, "ms");
            if (renderRequest.isRenderUrlLinks()) {
                svgBytes = generateSvg(reader, i);
            }
        } catch (NullPointerException e) {
            throw new RenderingCancelledException(e);//todo  http://plantuml.sourceforge.net/qa/?qa=4552/npe-while-generating-image-when-interrupted
        }

        if (description.contains("entities")) {
            description = "ok";
        }


        return new ImageItem(renderRequest.getBaseDir(), documentSource, pageSource, i, description, imageStream.toByteArray(), svgBytes);
    }

    private static byte[] generateSvg(SourceStringReader reader, int i) throws IOException {
        long start = System.currentTimeMillis();
        byte[] svgBytes;
        ByteArrayOutputStream svgStream = new ByteArrayOutputStream();
        reader.generateImage(svgStream, i, SVG);
        svgBytes = svgStream.toByteArray();
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

}
