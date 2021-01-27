package org.plantuml.idea.adapter;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.adapter.rendering.PlantUmlRendererUtil;
import org.plantuml.idea.external.Classloaders;
import org.plantuml.idea.external.PlantUmlFacade;
import org.plantuml.idea.lang.annotator.SourceAnnotation;
import org.plantuml.idea.plantuml.ImageFormat;
import org.plantuml.idea.rendering.RenderCacheItem;
import org.plantuml.idea.rendering.RenderRequest;
import org.plantuml.idea.rendering.RenderResult;
import org.plantuml.idea.toolwindow.Zoom;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * @see Classloaders#getFacade(java.lang.ClassLoader)
 */
public class FacadeImpl implements PlantUmlFacade {
    private static final Logger LOG = Logger.getInstance(FacadeImpl.class);

    @Nullable
    @Override
    public Collection<SourceAnnotation> annotateSyntaxErrors(String source, VirtualFile virtualFile) {
        return Annotator.annotateSyntaxErrors(source, virtualFile);
    }

    @Override
    public void renderAndSave(String source, File sourceFile, ImageFormat format, String path, String pathPrefix, Zoom scaledZoom, int pageNumber) throws IOException {
        RenderRequest renderRequest = new RenderRequest(sourceFile.getAbsolutePath(), source, format, pageNumber, scaledZoom, -1, false, null);
        PlantUmlRendererUtil.renderAndSave(renderRequest, path, pathPrefix);
    }

    @Override
    public RenderResult render(RenderRequest renderRequest, RenderCacheItem cachedItem) {
        return PlantUmlRendererUtil.render(renderRequest, cachedItem);
    }

    @Override
    public String version() {
        return Utils.version();
    }


    @Override
    public String encode(String source) throws IOException {
        return Utils.encode(source);
    }
}
