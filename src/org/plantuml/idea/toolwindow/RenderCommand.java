package org.plantuml.idea.toolwindow;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.plantuml.*;
import org.plantuml.idea.util.ImageWithUrlData;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;


abstract class RenderCommand implements Runnable {
    public static final Logger logger = Logger.getInstance(RenderCommand.class);

    private String sourceFilePath;
    private final String source;
    private final File selectedDir;
    private final int page;
    private int zoom;
    private RenderCache.RenderCacheItem cachedItem;
    private int version;

    public RenderCommand(String sourceFilePath, String source, File selectedDir, int page, int zoom, RenderCache.RenderCacheItem cachedItem, int version) {
        this.sourceFilePath = sourceFilePath;
        this.source = source;
        this.selectedDir = selectedDir;
        this.page = page;
        this.zoom = zoom;
        this.cachedItem = cachedItem;
        this.version = version;
    }

    @Override
    public void run() {
        if (source.isEmpty()) {
            logger.debug("source is empty");
            return;
        }

        try {
            Map<File, Long> includedFiles = PlantUmlIncludes.commitIncludes(source, selectedDir);
            logger.debug("includedFiles=", includedFiles);

            PlantUmlResult imageResult = PlantUmlRenderer.render(new RenderRequest(selectedDir, source, PlantUml.ImageFormat.PNG, page, zoom, version), cachedItem);
            PlantUmlResult svgResult = PlantUmlRenderer.render(new RenderRequest(selectedDir, source, PlantUml.ImageFormat.SVG, page, zoom, version), cachedItem);
            final ImageWithUrlData[] imagesWithData = toImagesWithUrlData(imageResult, svgResult, selectedDir);


            if (hasImages(imagesWithData)) {
                Runnable postRender = postRender(sourceFilePath, imageResult, imagesWithData, includedFiles);
                ApplicationManager.getApplication().invokeLater(postRender);

            } else {
                logger.debug("no images rendered");
            }

        } catch (Exception e) {
            logger.warn("Exception occurred rendering source = " + source + ": " + e, e);
        }
    }

    private boolean hasImages(ImageWithUrlData[] imagesWithUrlData) {
        for (ImageWithUrlData imageWithUrlData : imagesWithUrlData) {
            if (imageWithUrlData != null && imageWithUrlData.getImage() != null) {
                return true;
            }
        }
        return false;
    }

    private ImageWithUrlData[] toImagesWithUrlData(PlantUmlResult imageResult, PlantUmlResult svgResult, File baseDir) throws IOException {
        List<PlantUmlResult.Diagram> imageDiagrams = imageResult.getDiagrams();
        List<PlantUmlResult.Diagram> svgDiagrams = svgResult.getDiagrams();
        int pages = imageResult.getPages();
        //noinspection UndesirableClassUsage
        ImageWithUrlData[] imagesWithUrlData;
        if (cachedItem != null) {
            imagesWithUrlData = clone(cachedItem.getImagesWithData());
            if (imagesWithUrlData.length != pages) {
                imagesWithUrlData = new ImageWithUrlData[pages];
            }
        } else {
            imagesWithUrlData = new ImageWithUrlData[pages];
        }

        for (int i = 0; i < imageDiagrams.size(); i++) {
            PlantUmlResult.Diagram imageDiagram = imageDiagrams.get(i);
            PlantUmlResult.Diagram svgDiagram = svgDiagrams.get(i);

            if (imageDiagram != null) {
                imagesWithUrlData[imageDiagram.getPage()] = new ImageWithUrlData(imageDiagram.getDiagramBytes(), svgDiagram.getDiagramBytes(), baseDir);
            }
        }
        return imagesWithUrlData;
    }

    private ImageWithUrlData[] clone(ImageWithUrlData[] imagesWithData) {
        ImageWithUrlData[] result = new ImageWithUrlData[imagesWithData.length];
        for (int i = 0; i < imagesWithData.length; i++) {
            result[i] = ImageWithUrlData.deepClone(imagesWithData[i]);

        }
        return result;
    }

    @NotNull
    protected abstract Runnable postRender(String sourceFilePath, PlantUmlResult imageResult, final ImageWithUrlData[] imagesWithData, Map<File, Long> includedFiles);

}
