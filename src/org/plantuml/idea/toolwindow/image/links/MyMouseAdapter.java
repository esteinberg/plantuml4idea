package org.plantuml.idea.toolwindow.image.links;

import com.intellij.openapi.diagnostic.Logger;
import org.plantuml.idea.rendering.ImageItem;
import org.plantuml.idea.rendering.RenderRequest;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URI;

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
                    Desktop.getDesktop().browse(URI.create(text));
                } else {
                    if (navigator.openFile(new File(renderRequest.getBaseDir(), text))) return;
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
