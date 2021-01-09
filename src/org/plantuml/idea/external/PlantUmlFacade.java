package org.plantuml.idea.external;

import com.intellij.psi.PsiFile;
import com.intellij.util.ImageLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

public interface PlantUmlFacade {
    static PlantUmlFacade get() {
        return Classloaders.getFacade();
    }

    static PlantUmlFacade getBundled() {
        return Classloaders.getFacade(Classloaders.getBundled());
    }

    @Nullable
    Collection<SourceAnnotation> annotateSyntaxErrors(PsiFile file, String source);

    void renderAndSave(String source, File sourceFile, PlantUml.ImageFormat format, String path, String pathPrefix, int zoom, int pageNumber)
            throws IOException;

    RenderResult render(RenderRequest renderRequest, RenderCacheItem cachedItem);

    BufferedImage loadWithoutCache(@Nullable URL url, @NotNull InputStream stream, double scale, @Nullable ImageLoader.Dimension2DDouble docSize /*OUT*/);

    String version();

    String encode(String source) throws IOException;
}
