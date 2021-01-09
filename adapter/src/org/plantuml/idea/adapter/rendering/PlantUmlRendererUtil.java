package org.plantuml.idea.adapter.rendering;

import com.intellij.openapi.diagnostic.Logger;
import net.sourceforge.plantuml.*;
import net.sourceforge.plantuml.core.Diagram;
import net.sourceforge.plantuml.cucadiagram.Display;
import net.sourceforge.plantuml.cucadiagram.DisplayPositionned;
import net.sourceforge.plantuml.error.PSystemError;
import net.sourceforge.plantuml.preproc.Defines;
import net.sourceforge.plantuml.sequencediagram.Event;
import net.sourceforge.plantuml.sequencediagram.Newpage;
import net.sourceforge.plantuml.sequencediagram.SequenceDiagram;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.adapter.Utils;
import org.plantuml.idea.lang.annotator.LanguageDescriptor;
import org.plantuml.idea.lang.settings.PlantUmlSettings;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.rendering.*;
import org.plantuml.idea.util.UIUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class PlantUmlRendererUtil {
    private static final Logger logger = Logger.getInstance(PlantUmlRendererUtil.class);

    public static final Pattern NEW_PAGE_PATTERN = Pattern.compile("\\n\\s*@?(?i)(newpage)(\\p{Blank}+[^\\n]+|\\p{Blank}*)(?=\\n)");

    private static final PlantUmlPartialRenderer PARTIAL_RENDERER = new PlantUmlPartialRenderer();
    private static final PlantUmlNormalRenderer NORMAL_RENDERER = new PlantUmlNormalRenderer();

    public static void renderAndSave(String source, File sourceFile, PlantUml.ImageFormat format, String path, String pathPrefix, int zoom, int pageNumber)
            throws IOException {
        Utils.prepareEnvironment(UIUtils.getParent(sourceFile), source);

        NORMAL_RENDERER.renderAndSave(source, sourceFile, format, path, pathPrefix, zoom, pageNumber);
    }

    public static RenderResult render(RenderRequest renderRequest, RenderCacheItem cachedItem) {
        long start = System.currentTimeMillis();
        final Map<File, Long> includedFiles = Utils.prepareEnvironment(renderRequest.getBaseDir(), renderRequest.getSource());

        String source = renderRequest.getSource();
        String[] sourceSplit = NEW_PAGE_PATTERN.split(source);
        logger.debug("split done ", System.currentTimeMillis() - start, "ms");

        boolean partialRender = sourceSplit[0].contains(LanguageDescriptor.IDEA_PARTIAL_RENDER);
        logger.debug("partialRender ", partialRender);

        RenderResult renderResult;
        if (partialRender) {
            renderResult = PARTIAL_RENDERER.partialRender(renderRequest, cachedItem, start, sourceSplit);
        } else {
            renderResult = NORMAL_RENDERER.doRender(renderRequest, cachedItem, sourceSplit);
        }

        renderResult.setIncludedFiles(includedFiles);
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
        DiagramInfo.Titles titles = getTitles(blocks);
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
        return new ScaleSimple(getPlantUmlScale(scale) * zoom / 100f);
    }

    private static double getPlantUmlScale(Scale scale) {
        double plantUmlScale = 1.0;
        if (scale instanceof ScaleSimple) {
            plantUmlScale = scale.getScale(1, 1);
        }
        return plantUmlScale;
    }

    @NotNull
    protected static DiagramInfo.Titles getTitles(List<BlockUml> blocks) {
        List<String> titles = new ArrayList<String>();
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
            } else if (diagram instanceof TitledDiagram) {
                addTitle(titles, ((TitledDiagram) diagram).getTitle().getDisplay());
            } else {
                titles.add(null);
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

    public static SourceStringReader newSourceStringReader(String source, boolean useSettings, File file) {
        List<String> configAsList;
        String encoding;
        if (useSettings) {
            PlantUmlSettings settings = PlantUmlSettings.getInstance();
            encoding = settings.getEncoding();
            configAsList = settings.getConfigAsList();
        } else {
            encoding = "UTF-8";
            configAsList = new ArrayList<>();

        }

        Defines defines;
        if (file != null) {
            defines = Defines.createWithFileName(file);
        } else {
            defines = Defines.createEmpty();
        }
        return new SourceStringReader(defines, source, encoding, configAsList);
    }


}
