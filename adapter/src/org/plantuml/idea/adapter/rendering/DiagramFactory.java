package org.plantuml.idea.adapter.rendering;

import com.intellij.openapi.diagnostic.Logger;
import net.sourceforge.plantuml.BlockUml;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.Log;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.DiagramDescription;
import net.sourceforge.plantuml.preproc.FileWithSuffix;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.select.NodeFilter;
import org.plantuml.idea.plantuml.ImageFormat;
import org.plantuml.idea.rendering.ImageItem;
import org.plantuml.idea.rendering.RenderRequest;
import org.plantuml.idea.rendering.RenderingCancelledException;
import org.plantuml.idea.rendering.RenderingType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class DiagramFactory {
    private static final Logger LOG = com.intellij.openapi.diagnostic.Logger.getInstance(DiagramFactory.class);
    private final List<MyBlock> myBlocks;
    private final int totalPages;

    public DiagramFactory(List<MyBlock> myBlocks, int totalPages) {
        this.myBlocks = myBlocks;
        this.totalPages = totalPages;

        if (myBlocks.size() > 1) {
            LOG.debug("more than 1 block ", this);
            //happens when the source is incorrectly extracted and contains multiple diagrams
        }
    }

    public static DiagramFactory create(RenderRequest renderRequest, String documentSource) {
        SourceStringReader reader = PlantUmlRendererUtil.newSourceStringReader(documentSource, renderRequest);
        return create(reader, renderRequest);
    }

    public static DiagramFactory create(SourceStringReader reader, RenderRequest renderRequest) {
        long start1 = System.currentTimeMillis();
        int totalPages = 0;
        List<MyBlock> myBlocks = new ArrayList<>();

        for (BlockUml blockUml : reader.getBlocks()) {
            checkCancel();
            long start = System.currentTimeMillis();

            MyBlock myBlockInfo = new MyBlock(blockUml);
            if (renderRequest != null) {
                myBlockInfo.zoomDiagram(renderRequest);
            }
            myBlocks.add(myBlockInfo);
            totalPages = totalPages + myBlockInfo.getNbImages();
            LOG.debug("myBlockInfo done in  ", System.currentTimeMillis() - start, " ms");

            break;
        }


        DiagramFactory diagramFactory = new DiagramFactory(myBlocks, totalPages);
        LOG.debug("diagramFactory done in ", System.currentTimeMillis() - start1, "ms");
        return diagramFactory;
    }

    public static void checkCancel() {
        if (Thread.currentThread().isInterrupted()) {
            throw new RenderingCancelledException();
        }
    }

    public int getTotalPages() {
        return totalPages;
    }

    public String getTitle(int numImage) {
        for (MyBlock myBlock : myBlocks) {
            final int nbInSystem = myBlock.getNbImages();
            if (numImage < nbInSystem) {
                return myBlock.getTitles().getTitle(numImage);
            }
            numImage = numImage - nbInSystem;
        }

        Log.error("numImage is too big = " + numImage);
        return null;
    }

    public String getTitleOrPageNumber(int numImage) {
        for (MyBlock myBlock : myBlocks) {
            final int nbInSystem = myBlock.getNbImages();
            if (numImage < nbInSystem) {
                return myBlock.getTitles().getTitleOrPageNumber(numImage);
            }
            numImage = numImage - nbInSystem;
        }

        Log.error("numImage is too big = " + numImage);
        return null;
    }

    public String getFilename(int numImage) {
        for (MyBlock myBlock : myBlocks) {
            final int nbInSystem = myBlock.getNbImages();
            if (numImage < nbInSystem) {
                return myBlock.getFilename();
            }
            numImage = numImage - nbInSystem;
        }

        Log.error("numImage is too big = " + numImage);
        return null;
    }

    public DiagramDescription outputImage(OutputStream imageStream, int numImage, FileFormatOption formatOption) {
        try {
            for (MyBlock myBlock : myBlocks) {
                final int nbInSystem = myBlock.getNbImages();
                if (numImage < nbInSystem) {
                    myBlock.getDiagram().exportDiagram(imageStream, numImage, formatOption);
                    return myBlock.getDiagram().getDescription();
                }
                numImage = numImage - nbInSystem;
            }
        } catch (UnsupportedOperationException e) {
            throw e;
        } catch (Exception e) {
            throw new RenderingCancelledException(e);
        }
        Log.error("numImage is too big = " + numImage);
        return null;
    }

    // see: http://stackoverflow.com/questions/7541843/how-to-search-for-comments-using-jsoup
    private void removeComments(Element article) {
        article.filter(new NodeFilter() {
            @Override
            public FilterResult tail(Node node, int depth) {
                if (node instanceof Comment) {
                    return FilterResult.REMOVE;
                }
                return FilterResult.CONTINUE;
            }

            @Override
            public FilterResult head(Node node, int depth) {
                if (node instanceof Comment) {
                    return FilterResult.REMOVE;
                }
                return FilterResult.CONTINUE;
            }
        });
    }

    protected byte[] generateSvgLinks(int i) {
        long start = System.currentTimeMillis();
        ByteArrayOutputStream svgStream = new ByteArrayOutputStream();
        outputImage(svgStream, i, PlantUmlNormalRenderer.SVG);
        byte[] svgBytes = svgStream.toByteArray();
        boolean png = isPng(svgBytes);
        LOG.debug("generated ", PlantUmlNormalRenderer.SVG.getFileFormat(), " for page ", i, " in ", System.currentTimeMillis() - start, "ms, png=", png);
        if (png) {
            LOG.debug("generated png instead of svg, no links possible");
            return new byte[0];
        }
        return svgBytes;
    }

    @NotNull
    protected ImageItem generateImageItem(RenderRequest renderRequest,
                                          String documentSource,
                                          @Nullable String pageSource,
                                          FileFormatOption formatOption,
                                          int page,
                                          int logPage,
                                          RenderingType renderingType) {
        checkCancel();
        long start = System.currentTimeMillis();

        ImageFormat format = renderRequest.getFormat();
        ByteArrayOutputStream imageStream = new ByteArrayOutputStream();

        DiagramDescription diagramDescription = outputImage(imageStream, page, formatOption);

        byte[] bytes = imageStream.toByteArray();
        bytes = sanitizeSvg(bytes, format);
        boolean png = isPng(bytes);
        boolean wrongResultFormat = format == ImageFormat.SVG && png;

        LOG.debug("generated ", formatOption.getFileFormat(), " for page ", logPage, " in ", System.currentTimeMillis() - start, "ms, png=", png);

        byte[] svgBytes = new byte[0];
        if (!wrongResultFormat) {
            if (format == ImageFormat.SVG) {
                svgBytes = bytes;
            } else if (format == ImageFormat.PNG && renderRequest.isRenderUrlLinks()) { //todo  do not do that if exporting
                svgBytes = generateSvgLinks(page);
            }
        }


        Objects.requireNonNull(diagramDescription);
        String description = diagramDescription.getDescription();
        if (description != null && description.contains("entities")) {
            description = "ok";
        }

        ImageFormat resultFormat = format;
        if (wrongResultFormat) {
            resultFormat = ImageFormat.PNG;
        }
        return new ImageItem(renderRequest.getBaseDir(), resultFormat, documentSource, pageSource, page, description, bytes, svgBytes, renderingType, getTitle(page), getFilename(page), null);
    }

    private byte[] sanitizeSvg(byte[] bytes, ImageFormat format) {
        if (format == ImageFormat.SVG) {
            String html = new String(bytes, StandardCharsets.UTF_8);
            Parser parser = Parser.xmlParser();
//                parser.settings(new ParseSettings(true, true)); // tag, attribute preserve case
            org.jsoup.nodes.Document document = parser.parseInput(html, "");
            removeComments(document);
            String s = document.toString();
            return s.getBytes(StandardCharsets.UTF_8);
        }
        return bytes;
    }

    private boolean isPng(byte[] bytes) {
        boolean isPng = false;
        if (bytes.length > 4) {
            isPng = "‰PNG".equals(new String(bytes, 0, 4));
        }
        return isPng;
    }

    @NotNull
    public LinkedHashMap<File, Long> getIncludedFiles() {
        long start = System.currentTimeMillis();
        LinkedHashMap<File, Long> includedFiles = new LinkedHashMap<>();
        for (MyBlock block : myBlocks) {
            try {
                Set<File> convert = FileWithSuffix.convert(block.getBlockUml().getIncluded());
                ArrayList<File> files = new ArrayList<>(convert);
                files.sort(File::compareTo);
                for (File file : files) {
                    includedFiles.put(file, file.lastModified());
                }
            } catch (FileNotFoundException e) {
                LOG.warn(e);
            }
        }
        LOG.debug("getIncludedFiles ", (System.currentTimeMillis() - start), "ms");
        return includedFiles;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("myBlocks", myBlocks)
                .append("totalPages", totalPages)
                .toString();
    }
}
