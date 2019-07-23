package org.plantuml.idea.rendering;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.ui.JBUI;
import net.sourceforge.plantuml.*;
import net.sourceforge.plantuml.core.Diagram;
import net.sourceforge.plantuml.cucadiagram.Display;
import net.sourceforge.plantuml.cucadiagram.DisplayPositionned;
import net.sourceforge.plantuml.error.PSystemError;
import net.sourceforge.plantuml.sequencediagram.Event;
import net.sourceforge.plantuml.sequencediagram.Newpage;
import net.sourceforge.plantuml.sequencediagram.SequenceDiagram;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.plantuml.idea.lang.annotator.LanguageDescriptor.IDEA_PARTIAL_RENDER;

public class PlantUmlRendererUtil {
    private static final Logger logger = Logger.getInstance(PlantUmlRendererUtil.class);

    public static final Pattern NEW_PAGE_PATTERN = Pattern.compile("\\n\\s*@?(?i)(newpage)(\\p{Blank}+[^\\n]+|\\p{Blank}*)(?=\\n)");

    private static final PlantUmlPartialRenderer PARTIAL_RENDERER = new PlantUmlPartialRenderer();
    private static final PlantUmlNormalRenderer NORMAL_RENDERER = new PlantUmlNormalRenderer();

    /**
     * Renders source code and saves diagram images to files according to provided naming scheme
     * and image format.
     *
     * @param source         source code to be rendered
     * @param baseDir        base dir to set for "include" functionality
     * @param format         image format
     * @param path       path to use with first file
     * @param fileNameFormat file naming scheme for further files
     * @param pageNumber     -1 for all pages   
     * @throws IOException in case of rendering or saving fails
     */
    public static void renderAndSave(String source, @Nullable File baseDir, PlantUml.ImageFormat format, String path, String fileNameFormat, int zoom, int pageNumber)
            throws IOException {
        NORMAL_RENDERER.renderAndSave(source, baseDir, format, path, fileNameFormat, zoom, pageNumber);
    }

    /**
     * Renders file with support of plantUML include ange paging features, setting base dir and page for plantUML
     * to provided values
     */
    public static RenderResult render(RenderRequest renderRequest, RenderCacheItem cachedItem) {
            File baseDir = renderRequest.getBaseDir();
            if (baseDir != null) {
                Utils.setPlantUmlDir(baseDir);
            }
            long start = System.currentTimeMillis();

            String source = renderRequest.getSource();
            String[] sourceSplit = NEW_PAGE_PATTERN.split(source);
            logger.debug("split done ", System.currentTimeMillis() - start, "ms");

            boolean partialRender = sourceSplit[0].contains(IDEA_PARTIAL_RENDER);
            logger.debug("partialRender ", partialRender);

            RenderResult renderResult;
            if (partialRender) {
                renderResult = PARTIAL_RENDERER.partialRender(renderRequest, cachedItem, start, sourceSplit);
            } else {
                renderResult = NORMAL_RENDERER.doRender(renderRequest, cachedItem, sourceSplit);
            }
            return renderResult;
    }

    public static DiagramInfo zoomDiagram(SourceStringReader reader, int zoom) {
        logger.debug("zooming diagram");
        int totalPages = 0;
        List<BlockUml> blocks = reader.getBlocks();
        String fileOrDirname = null;

        if (blocks.size() > 1) {
//            logger.error("more than 1 block"); //TODO
            //happens when the source is incorrectly extracted and contains multiple diagrams
        }
        
        for (int i = 0; i < blocks.size(); i++) {
            BlockUml block = blocks.get(i);

            long start = System.currentTimeMillis();
            checkCancel();
            Diagram diagram = block.getDiagram();
            logger.debug("getDiagram done in  ", System.currentTimeMillis() - start, " ms");

            start = System.currentTimeMillis();
            zoomDiagram(diagram, zoom);
            logger.debug("zoom diagram done in  ", System.currentTimeMillis() - start, " ms");
            fileOrDirname = block.getFileOrDirname();
            totalPages = totalPages + diagram.getNbImages();

            break;
        }
        DiagramInfo.Titles titles = getTitles(totalPages, blocks);
        return new DiagramInfo(totalPages, titles, fileOrDirname);
    }

    private static void zoomDiagram(Diagram diagram, int zoom) {
        if (diagram instanceof NewpagedDiagram) {
            NewpagedDiagram newpagedDiagram = (NewpagedDiagram) diagram;
            for (Diagram page : newpagedDiagram.getDiagrams()) {
                if (page instanceof AbstractPSystem) {
                    AbstractPSystem descriptionDiagram = (AbstractPSystem) page;
                    Scale scale = descriptionDiagram.getScale();

                    if (scale == null || scale instanceof ScaleSimple || zoom != 100) {
                        descriptionDiagram.setScale(calculateScale(zoom, scale));
                    }
                }
            }
        } else if (diagram instanceof AbstractPSystem) { //gantt, salt wireframe - but has no effect
            AbstractPSystem d = (AbstractPSystem) diagram;
            Scale scale = d.getScale();

            if (scale == null || scale instanceof ScaleSimple || zoom != 100) {
                d.setScale(calculateScale(zoom, scale));
            }
        }
    }

    @NotNull
    private static ScaleSimple calculateScale(int zoom, Scale scale) {
        return new ScaleSimple(getPlantUmlScale(scale) * getSystemScale() * zoom / 100f);
    }

    private static double getPlantUmlScale(Scale scale) {
        double plantUmlScale = 1.0;
        if (scale instanceof ScaleSimple) {
            plantUmlScale = scale.getScale(1, 1);
        }
        return plantUmlScale;
    }

    private static double getSystemScale() {
        try {
            return JBUI.ScaleContext.create().getScale(JBUI.ScaleType.SYS_SCALE);  //TODO API change 2019/03/05
        } catch (Throwable e) {
            return 1;
        }
    }

    @NotNull
    protected static DiagramInfo.Titles getTitles(int totalPages, List<BlockUml> blocks) {
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
            } else if (diagram instanceof UmlDiagram) {
                DisplayPositionned title = ((UmlDiagram) diagram).getTitle();
                addTitle(titles, title.getDisplay());
            } else if (diagram instanceof PSystemError) {
                DisplayPositionned title = ((PSystemError) diagram).getTitle();
                if (title == null) {
                    titles.add(null);
                } else {
                    addTitle(titles, title.getDisplay());
                }
            }
            break;
        }
        return new DiagramInfo.Titles(titles);
    }


    protected static void addTitle(List<String> titles, Display display) {
        if (display.size() > 0) {
            titles.add(display.toString());
        } else {
            titles.add(null);
        }
    }

    public static void checkCancel() {
        if (Thread.currentThread().isInterrupted()) {
            throw new RenderingCancelledException();
        }
    }

}
