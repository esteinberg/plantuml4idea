package org.plantuml.idea.adapter.rendering;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.io.URLUtil;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import org.apache.commons.io.IOUtils;
import org.plantuml.idea.adapter.Format;
import org.plantuml.idea.rendering.RenderRequest;
import org.plantuml.idea.settings.PlantUmlSettings;

import java.io.FileOutputStream;
import java.io.IOException;


public class PlantUmlExporter {
    protected static final Logger logger = Logger.getInstance(PlantUmlExporter.class);

    public void renderAndSave(RenderRequest renderRequest, String path, String pathPrefix)
            throws IOException {
        PlantUmlSettings settings = PlantUmlSettings.getInstance();
        FileFormat pFormat = Format.from(renderRequest.getFormat());
        String fileSuffix = pFormat.getFileSuffix();
        int requestedPageNumber = renderRequest.getPage();
        DiagramFactory diagramFactory = DiagramFactory.create(renderRequest, renderRequest.getSource());

        VirtualFileManager vfm = VirtualFileManager.getInstance();
        if (requestedPageNumber >= 0) {
            try (FileOutputStream outputStream = new FileOutputStream(path)) {
                diagramFactory.outputImage(outputStream, requestedPageNumber, new FileFormatOption(pFormat, settings.isGenerateMetadata()));
            }
            vfm.refreshAndFindFileByUrl(VirtualFileManager.constructUrl(URLUtil.FILE_PROTOCOL, path));
        } else {
            if (pathPrefix == null) {
                throw new IllegalArgumentException("pathPrefix is null");
            }
            int totalPages = diagramFactory.getTotalPages();
            for (int page = 0; page < totalPages; page++) {
                String resultPath;
                if (settings.isUsePageTitles()) {
                    String titleOrPageNumber = diagramFactory.getTitleOrPageNumber(page);
                    String pageTitleSuffix = "-" + titleOrPageNumber;
                    if (page == 0 && pathPrefix.endsWith(pageTitleSuffix)) {
                        pathPrefix = pathPrefix.substring(0, pathPrefix.length() - pageTitleSuffix.length());
                    }
                    resultPath = pathPrefix + "-" + titleOrPageNumber + fileSuffix;
                } else {
                    if (page == 0) {
                        resultPath = pathPrefix + fileSuffix;
                    } else {
                        resultPath = pathPrefix + "-" + page + fileSuffix;
                    }
                }
                try (FileOutputStream outputStream = new FileOutputStream(resultPath)) {
                    diagramFactory.outputImage(outputStream, page, new FileFormatOption(pFormat, settings.isGenerateMetadata()));
                }
                vfm.refreshAndFindFileByUrl(VirtualFileManager.constructUrl(URLUtil.FILE_PROTOCOL, resultPath));
            }
        }
    }

    public static void save(String path, byte[] imageBytes) {
        try {
            logger.debug("saving ", path);
            try (FileOutputStream outputStream = new FileOutputStream(path)) {
                IOUtils.write(imageBytes, outputStream);
            }
            VirtualFileManager vfm = VirtualFileManager.getInstance();
            vfm.refreshAndFindFileByUrl(VirtualFileManager.constructUrl(URLUtil.FILE_PROTOCOL, path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
