package org.plantuml.idea.adapter.rendering;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.ex.VirtualFileManagerEx;
import com.intellij.util.io.URLUtil;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.adapter.Format;
import org.plantuml.idea.lang.settings.PlantUmlSettings;
import org.plantuml.idea.rendering.*;

import java.io.FileOutputStream;
import java.io.IOException;


public class PlantUmlExporter {
    protected static final Logger logger = Logger.getInstance(PlantUmlExporter.class);

    public void renderAndSave(RenderRequest renderRequest, String path, String pathPrefix)
            throws IOException {
        FileOutputStream outputStream = null;
        FileFormat pFormat = Format.from(renderRequest.getFormat());
        String fileSuffix = pFormat.getFileSuffix();
        int requestedPageNumber = renderRequest.getPage();
        try {
            DiagramFactory diagramFactory = DiagramFactory.create(renderRequest, renderRequest.getSource());

            VirtualFileManager vfm = VirtualFileManagerEx.getInstance();
            if (requestedPageNumber >= 0) {
                outputStream = new FileOutputStream(path);
                diagramFactory.outputImage(outputStream, requestedPageNumber, new FileFormatOption(pFormat));
                outputStream.close();
                vfm.refreshAndFindFileByUrl(VirtualFileManager.constructUrl(URLUtil.FILE_PROTOCOL, path));
            } else {
                if (pathPrefix == null) {
                    throw new IllegalArgumentException("pathPrefix is null");
                }
                PlantUmlSettings settings = PlantUmlSettings.getInstance();
                boolean usePageTitles = settings.isUsePageTitles();

                for (MyBlock block : diagramFactory.getBlockInfos()) {
                    net.sourceforge.plantuml.core.Diagram diagram = block.getDiagram();
                    int pages = block.getNbImages();
                    for (int page = 0; page < pages; ++page) {
                        String resultPath;
                        if (usePageTitles) {
                            String titleOrPageNumber = block.getTitles().getTitleOrPageNumber(page);
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
                        outputStream = new FileOutputStream(resultPath);
                        try {
//                            reader.outputImage(outputStream, imageСounter++, new FileFormatOption(pFormat));
                            diagram.exportDiagram(outputStream, page, new FileFormatOption(pFormat));
                        } finally {
                            outputStream.close();
                        }
                        vfm.refreshAndFindFileByUrl(VirtualFileManager.constructUrl(URLUtil.FILE_PROTOCOL, resultPath));
                    }
                    break;
                }
            }
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }

    }


}
