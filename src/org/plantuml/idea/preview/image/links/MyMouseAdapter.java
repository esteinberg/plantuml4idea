package org.plantuml.idea.preview.image.links;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
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
                } else {
                    String[] split = text.split("#");
                    text = split[0];
                    String method = split.length == 2 ? split[1] : null;

                    if (navigator.openFile(new File(renderRequest.getBaseDir(), text), method)) return;
                    if (navigator.openFile(new File(text), method)) return;

                    VirtualFile sourceFile = LocalFileSystem.getInstance().findFileByPath(renderRequest.getSourceFilePath());
                    if (sourceFile != null) {
                        Module module = ModuleUtilCore.findModuleForFile(sourceFile, renderRequest.getProject());
                        if (module != null) {
                            VirtualFile[] contentRoots = ModuleRootManager.getInstance(module).getContentRoots();
                            for (VirtualFile contentRoot : contentRoots) {
                                if (navigator.openFile(new File(contentRoot.getPath(), text), method)) return;
                            }
                        }
                    }
                    navigator.findNextSourceAndNavigate(text);
                }
            } else {
                navigator.findNextSourceAndNavigate(text);
            }
        } catch (
                Exception ex) {
            LOG.warn(ex);
        }
        LOG.debug("mousePressed ", (System.currentTimeMillis() - start), "ms");
    }

}
