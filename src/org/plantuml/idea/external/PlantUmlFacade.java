package org.plantuml.idea.external;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.lang.annotator.SourceAnnotation;
import org.plantuml.idea.plantuml.ImageFormat;
import org.plantuml.idea.preview.Zoom;
import org.plantuml.idea.rendering.RenderCacheItem;
import org.plantuml.idea.rendering.RenderRequest;
import org.plantuml.idea.rendering.RenderResult;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public interface PlantUmlFacade {

    static PlantUmlFacade get() {
        return Classloaders.getFacade();
    }

    @Nullable
    Collection<SourceAnnotation> annotateSyntaxErrors(String source, VirtualFile virtualFile);

    void renderAndSave(String source, File sourceFile, ImageFormat format, String path, String pathPrefix, Zoom zoom, int pageNumber)
            throws IOException;

    RenderResult render(RenderRequest renderRequest, RenderCacheItem cachedItem);

    String version();

    String encode(String source) throws IOException;

    void save(String path, byte[] imageBytes);

    String extractEmbeddedSourceFromImage(VirtualFile file);
}
