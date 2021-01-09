package org.plantuml.idea.adapter;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiFile;
import com.intellij.util.ImageLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.adapter.rendering.PlantUmlRendererUtil;
import org.plantuml.idea.external.Classloaders;
import org.plantuml.idea.external.PlantUmlFacade;
import org.plantuml.idea.lang.annotator.SourceAnnotation;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.rendering.RenderCacheItem;
import org.plantuml.idea.rendering.RenderRequest;
import org.plantuml.idea.rendering.RenderResult;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;

/**
 * @see Classloaders#getFacade(java.lang.ClassLoader)
 */
public class FacadeImpl implements PlantUmlFacade {
    private static final Logger LOG = Logger.getInstance(FacadeImpl.class);

    @Nullable
    @Override
    public Collection<SourceAnnotation> annotateSyntaxErrors(PsiFile file, String source) {
        return Annotator.annotateSyntaxErrors(file, source);
    }

    @Override
    public void renderAndSave(String source, File sourceFile, PlantUml.ImageFormat format, String path, String pathPrefix, int zoom, int pageNumber) throws IOException {
        PlantUmlRendererUtil.renderAndSave(source, sourceFile, format, path, pathPrefix, zoom, pageNumber);
    }

    @Override
    public RenderResult render(RenderRequest renderRequest, RenderCacheItem cachedItem) {
        return PlantUmlRendererUtil.render(renderRequest, cachedItem);
    }

    @Override
    public BufferedImage loadWithoutCache(@Nullable URL url, @NotNull InputStream stream, double scale, @Nullable ImageLoader.Dimension2DDouble docSize) {
        return Utils.loadWithoutCache(url, stream, scale, docSize);
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
