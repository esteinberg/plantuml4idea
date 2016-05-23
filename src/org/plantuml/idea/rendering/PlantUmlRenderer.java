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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static org.plantuml.idea.lang.annotator.LanguageDescriptor.IDEA_PARTIAL_RENDER;

public class PlantUmlRenderer {
    private static final Logger logger = Logger.getInstance(PlantUmlRenderer.class);
    private static final Pattern PATTERN = Pattern.compile("\\n\\s*@?newpage(\\p{Blank}+[^\\n]+|\\p{Blank}*)(?=\\n)");


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
            RenderResult renderResult;
            boolean partialRender = sourceSplit[0].contains(IDEA_PARTIAL_RENDER);
            logger.debug("partialRender ", partialRender);
            if (partialRender) {
                renderResult = partialRender(renderRequest, cachedItem, start, sourceSplit);
            } else {
                renderResult = doRender(renderRequest, cachedItem, source);
            }
            logger.debug("rendered ", renderRequest.getFormat(), " in ", System.currentTimeMillis() - start, "ms");
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
        List<RenderResult.Diagram> allDiagrams = new ArrayList<RenderResult.Diagram>();
        for (int i = 0; i < renderResults.size(); i++) {
            RenderResult x = renderResults.get(i);
            List<RenderResult.Diagram> diagrams = x.getDiagrams();
            for (int i1 = 0; i1 < diagrams.size(); i1++) {
                RenderResult.Diagram diagram = diagrams.get(i1);
                allDiagrams.add(diagram);
            }
        }
        renderResult = new RenderResult(Strategy.PARTIAL, allDiagrams, sourceSplit.length);
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
                RenderResult.Diagram diagram = new RenderResult.Diagram(page, generateImage(renderRequest.getSource(), partialSource, reader, formatOption, 0));
                renderResults.add(new RenderResult(Strategy.PARTIAL, Arrays.asList(diagram), 1));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        } else {
            logger.debug("page ", page, " not changed");
        }
        logger.debug("partial page processing done for page ", page, " in ", System.currentTimeMillis() - partialPageProcessingStart, "ms");
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


            List<RenderResult.Diagram> result = new ArrayList<RenderResult.Diagram>();
            FileFormatOption formatOption = new FileFormatOption(renderRequest.getFormat().getFormat());
            boolean renderAll = false;

            String[] renderRequestSplit = new String[0];
            boolean numberOfPagesChanged = cachedItem != null && cachedItem.getImagesWithData().length != totalPages;
            if (!numberOfPagesChanged) {
                renderRequestSplit = splitNewPage(documentSource);
            }
            if (totalPages > 1 && cachedItem != null && !numberOfPagesChanged) {
                logger.debug("incremental rendering, totalPages=", totalPages);

                if (renderRequestSplit.length == totalPages) {
                    if (renderRequestPage == -1) {
                        for (int i = 0; i < totalPages; i++) {
                            generateImageIfNecessary(documentSource, cachedItem, reader, i, result, formatOption, renderRequestSplit);
                        }
                    } else {
                        generateImageIfNecessary(documentSource, cachedItem, reader, renderRequestPage, result, formatOption, renderRequestSplit);
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
                        String pageSource = null;
                        if (renderRequestSplit.length == totalPages) {
                            pageSource = renderRequestSplit[i];
                        }
                        result.add(generateImage(documentSource, pageSource, reader, formatOption, i));
                    }
                } else {//render single image
                    String pageSource = null;
                    if (renderRequestSplit.length == totalPages) {
                        pageSource = renderRequestSplit[renderRequestPage];
                    }
                    result.add(generateImage(documentSource, pageSource, reader, formatOption, renderRequestPage));
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

    private static void generateImageIfNecessary(String documentSource, RenderCacheItem cachedItem, SourceStringReader reader, int renderRequestPage, List<RenderResult.Diagram> result, FileFormatOption formatOption, String[] renderRequestSplit) throws IOException {
        if (shouldGenerate(documentSource, cachedItem, renderRequestSplit, renderRequestPage)) {
            result.add(generateImage(documentSource, renderRequestSplit[renderRequestPage], reader, formatOption, renderRequestPage));
        } else {
            logger.debug("page ", renderRequestPage, " no change");
        }
    }

    private static boolean shouldGenerate(String documentSource, RenderCacheItem cachedItem, String[] renderRequestSplit, int i) {
        ImageWithUrlData imageWithUrlData = cachedItem.getImagesWithData()[i];
        if (imageWithUrlData == null) return true;

        String pageSource = imageWithUrlData.getPageSource();
        if (pageSource == null && documentSource.equals(imageWithUrlData.getDocumentSource())) {
            imageWithUrlData.setPageSource(pageSource);
            return false;
        }

        String renderRequestPiece = renderRequestSplit[i];
        if (!renderRequestPiece.equals(imageWithUrlData.getPageSource())) return true;

        return false;
    }

    @NotNull
    private static RenderResult.Diagram generateImage(String documentSource, String pageSource, SourceStringReader reader, FileFormatOption formatOption, int i) throws IOException {
        checkCancel();
        long start = System.currentTimeMillis();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String description = null;
        try {
            description = reader.generateImage(os, i, formatOption);
        } catch (NullPointerException e) {
            throw new RenderingCancelledException(e);//todo  http://plantuml.sourceforge.net/qa/?qa=4552/npe-while-generating-image-when-interrupted
        }

        if (description.contains("entities")) {
            description = "ok";
        }
        long total = System.currentTimeMillis() - start;
        RenderResult.Diagram diagram = new RenderResult.Diagram(documentSource, pageSource, i, description, os.toByteArray());
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
