package org.plantuml.idea.adapter.rendering;

import com.intellij.openapi.diagnostic.Logger;
import net.sourceforge.plantuml.BlockUml;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.DiagramDescription;
import net.sourceforge.plantuml.preproc.FileWithSuffix;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.plantuml.ImageFormat;
import org.plantuml.idea.rendering.ImageItem;
import org.plantuml.idea.rendering.RenderRequest;
import org.plantuml.idea.rendering.RenderingCancelledException;
import org.plantuml.idea.rendering.RenderingType;
import org.plantuml.idea.util.Utils;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
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

    public static DiagramFactory create(SourceStringReader reader, @Nullable RenderRequest renderRequest) {
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

        LOG.error("numImage is too big = " + numImage);
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

        LOG.error("numImage is too big = " + numImage);
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

        LOG.error("numImage is too big = " + numImage);
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
        LOG.error("numImage is too big = " + numImage);
        return null;
    }

    protected byte[] generateSvgLinks(int i) {
        long start = System.currentTimeMillis();
        ByteArrayOutputStream svgStream = new ByteArrayOutputStream();
        outputImage(svgStream, i, PlantUmlNormalRenderer.SVG);
        byte[] svgBytes = svgStream.toByteArray();
        boolean isPng = Utils.isPng(svgBytes);
        LOG.debug("generated ", PlantUmlNormalRenderer.SVG.getFileFormat(), " for page ", i, " in ", System.currentTimeMillis() - start, "ms, png=", isPng);
        if (isPng) {
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
        boolean isPng = Utils.isPng(bytes);
        boolean wrongResultFormat = format == ImageFormat.SVG && isPng;

        LOG.debug("generated ", formatOption.getFileFormat(), " for page ", logPage, " in ", System.currentTimeMillis() - start, "ms, png=", isPng, ", wrongResultFormat=", wrongResultFormat);

        byte[] svgBytes = new byte[0];
        if (!wrongResultFormat) {
            if (format == ImageFormat.SVG) {
                svgBytes = bytes;
            } else if (format == ImageFormat.PNG && renderRequest.isRenderUrlLinks()) { //todo  do not do that if exporting
                svgBytes = generateSvgLinks(page);
            }
        }
        debugInfo(documentSource, svgBytes);

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

    private void debugInfo(String documentSource, byte[] svgBytes) {
        if (LOG.isDebugEnabled()) {
            try {
                LOG.debug("documentSource=", java.util.Base64.getEncoder().encodeToString(documentSource.getBytes(StandardCharsets.UTF_8)));
                if (svgBytes.length > 0) {
                    TransformerFactory factory = TransformerFactory.newInstance();
                    LOG.debug("TransformerFactory=" + factory.getClass());
                    LOG.debug("TransformerFactoryClassLoader=" + factory.getClass().getClassLoader());
                    Transformer transformer = factory.newTransformer();
                    LOG.debug("Transformer=" + transformer.getClass());
                    LOG.debug("TransformerClassLoader=" + transformer.getClass().getClassLoader());
                    LOG.debug("TransformerOutputProperties=" + transformer.getOutputProperties());
                    LOG.debug("svgBytes=", java.util.Base64.getEncoder().encodeToString(svgBytes));
                }
            } catch (Throwable e) {
                LOG.error(e);
            }
        }
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
