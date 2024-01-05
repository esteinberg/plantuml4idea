package org.plantuml.idea.preview.image.links;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.grammar.navigation.PumlItemReference;
import org.plantuml.idea.rendering.ImageItem;
import org.plantuml.idea.rendering.RenderRequest;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

public class MyMouseAdapter extends MouseAdapter {
    private static final Logger LOG = Logger.getInstance(MyMouseAdapter.class);

    private final LinkNavigator navigator;
    private final ImageItem.LinkData linkData;
    private final RenderRequest renderRequest;

    public MyMouseAdapter(LinkNavigator navigator, ImageItem.LinkData linkData, RenderRequest renderRequest) {
        this.navigator = navigator;
        this.linkData = linkData;
        this.renderRequest = renderRequest;
    }

    public static boolean isWebReferenceUrl(String url) {
        return url.startsWith("www.") || url.startsWith("http://") || url.startsWith("https://") || url.startsWith("about:") || url.startsWith("mailto:");
    }

    @Override
    public void mousePressed(MouseEvent e) {
        long start = System.currentTimeMillis();
        String text = linkData.getText();
        try {
            if (linkData.isLink()) {
                if (isWebReferenceUrl(text)) {
                    BrowserUtil.browse(text);
                    return;
                } else {
                    if (openFile(text)) return;
                }
            }
            navigator.findNextSourceAndNavigate(text);
        } catch (Exception ex) {
            LOG.error(ex);
        }
        LOG.debug("mousePressed ", (System.currentTimeMillis() - start), "ms");
    }

    /**
     * just like {@link PumlItemReference#resolveFile())}
     */
    @Nullable
    private boolean openFile(String text) {
        String sourceFilePath = renderRequest.getSourceFilePath();
        Project project = renderRequest.getProject();
        File baseDir = renderRequest.getBaseDir();

        LinkNavigator.Coordinates coordinates = LinkNavigator.getCoordinates(text);

        if (navigator.openFile(new File(baseDir, coordinates.file()), coordinates)) return true;
        if (navigator.openFile(new File(coordinates.file()), coordinates)) return true;

        VirtualFile sourceFile = LocalFileSystem.getInstance().findFileByPath(sourceFilePath);
        if (sourceFile != null) {
            Module module = ModuleUtilCore.findModuleForFile(sourceFile, project);
            if (module != null) {
                VirtualFile[] contentRoots = ModuleRootManager.getInstance(module).getContentRoots();
                for (VirtualFile contentRoot : contentRoots) {
                    if (navigator.openFile(new File(contentRoot.getPath(), coordinates.file()), coordinates))
                        return true;
                }
            }
        }
        return true;
    }

}
