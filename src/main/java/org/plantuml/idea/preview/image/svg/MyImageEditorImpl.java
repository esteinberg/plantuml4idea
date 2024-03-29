/*
 * Copyright 2004-2005 Alexey Efimov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.plantuml.idea.preview.image.svg;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.scale.ScaleContext;
import com.intellij.ui.scale.ScaleType;
import org.intellij.images.editor.ImageDocument;
import org.intellij.images.editor.ImageZoomModel;
import org.intellij.images.thumbnail.actionSystem.ThumbnailViewActions;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.preview.PlantUmlPreviewPanel;
import org.plantuml.idea.preview.Zoom;
import org.plantuml.idea.preview.image.svg.batik.Dimension2DDouble;
import org.plantuml.idea.preview.image.svg.batik.MySvgDocumentFactoryKt;
import org.plantuml.idea.preview.image.svg.batik.MySvgTranscoder;
import org.plantuml.idea.settings.PlantUmlSettings;
import org.w3c.dom.Document;

import javax.swing.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Image viewer implementation.
 *
 * @author <a href="mailto:aefimov.box@gmail.com">Alexey Efimov</a>
 */
public final class MyImageEditorImpl implements MyImageEditor {
    private static final Logger LOG = Logger.getInstance(MyImageEditorImpl.class);

    private final Project project;
    private final VirtualFile file;
    private final MyImageEditorUI editorUI;

//  public MyImageEditorImpl(@NotNull Project project, @NotNull VirtualFile file) {
//    this(project, file, false);
//  }

    /**
     * @param previewPanel
     * @param isEmbedded   if it's true the toolbar and the image info are disabled and an image is left-side aligned
     * @param zoomModel
     */
    public MyImageEditorImpl(PlantUmlPreviewPanel previewPanel, @NotNull Project project, @NotNull VirtualFile file, boolean isEmbedded, Zoom zoomModel) {
        this.project = project;
        this.file = file;

        editorUI = new MyImageEditorUI(previewPanel, this, isEmbedded, zoomModel);
//        Disposer.register(this, editorUI);

//        VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileListener() {
//            @Override
//            public void propertyChanged(@NotNull VirtualFilePropertyEvent event) {
//                MyImageEditorImpl.this.propertyChanged(event);
//            }
//
//            @Override
//            public void contentsChanged(@NotNull VirtualFileEvent event) {
//                MyImageEditorImpl.this.contentsChanged(event);
//            }
//        }, this);

        setValue(previewPanel, file);
    }

    private void setValue(PlantUmlPreviewPanel previewPanel, VirtualFile file) {
        try {
            //CUSTOM
            editorUI.setImageProvider(new MyScaledImageProvider(previewPanel, file), "svg");

//            editorUI.setImageProvider(IfsUtil.getImageProvider(file), IfsUtil.getFormat(file));

        } catch (Throwable e) {
            LOG.error(e);
            //     Error loading image file
            editorUI.setImageProvider(null, null);
        }
    }

    @Override
    public boolean isValid() {
        ImageDocument document = editorUI.getImageComponent().getDocument();
        return document.getValue() != null;
    }

    @Override
    public MyImageEditorUI getComponent() {
        return editorUI;
    }

    @Override
    public JComponent getContentComponent() {
        return editorUI.getImageComponent();
    }

    @Override
    @NotNull
    public VirtualFile getFile() {
        return file;
    }

    @Override
    @NotNull
    public Project getProject() {
        return project;
    }

    @Override
    public ImageDocument getDocument() {
        return editorUI.getImageComponent().getDocument();
    }

    @Override
    public void setTransparencyChessboardVisible(boolean visible) {
        //CUSTOM
        PlantUmlSettings.getInstance().setShowChessboard(visible);

        editorUI.getImageComponent().setTransparencyChessboardVisible(visible);
        editorUI.repaint();
    }

    @Override
    public boolean isTransparencyChessboardVisible() {
        return editorUI.getImageComponent().isTransparencyChessboardVisible();
    }

    @Override
    public boolean isEnabledForActionPlace(String place) {
        // Disable for thumbnails action
        return !ThumbnailViewActions.ACTION_PLACE.equals(place);
    }

    @Override
    public void setGridVisible(boolean visible) {
        editorUI.getImageComponent().setGridVisible(visible);
        editorUI.repaint();
    }

    @Override
    public void setEditorBackground(Color color) {
        editorUI.getImageComponent().getParent().setBackground(color);
    }

    @Override
    public void setBorderVisible(boolean visible) {
        editorUI.getImageComponent().setBorderVisible(visible);
    }

    @Override
    public boolean isGridVisible() {
        return editorUI.getImageComponent().isGridVisible();
    }


    @Override
    public ImageZoomModel getZoomModel() {
        return editorUI.getZoomModel();
    }

    public void dispose() {
        Disposer.dispose(editorUI);
    }


    public class MyScaledImageProvider implements ImageDocument.ScaledImageProvider {

        private final PlantUmlPreviewPanel previewPanel;
        private final VirtualFile file;

        private volatile boolean renderingInProgress;

        private volatile MyImageEditorImpl.Holder holder = new MyImageEditorImpl.Holder();

        public MyScaledImageProvider(PlantUmlPreviewPanel previewPanel, VirtualFile file) {
            this.previewPanel = previewPanel;
            this.file = file;
        }

        public MyImageEditorImpl.Holder getHolder() {
            return holder;
        }

        @Override
        public BufferedImage apply(Double bullshitScale, Component component) {
            double zoom = getZoomModel().getZoomFactor();
            MyImageEditorImpl.Holder holder = this.holder;
            if (holder.isChanged(zoom) && !renderingInProgress) {
                createImage(component, zoom);
            }
            return holder.image;
        }

        public synchronized void createImage(Component component, double zoom) {
            MyImageEditorImpl.Holder holder = this.holder;
            if (holder.image != null && holder.zoom == zoom) {
                return;
            }
            byte[] buf = null;
            double scaledZoom = 0;
            try {
                renderingInProgress = true;
                long start = System.currentTimeMillis();
                buf = file.contentsToByteArray();
                if (buf.length == 0) {
                    throw new RuntimeException("Empty file");
                }

                Document svgDocument = MySvgDocumentFactoryKt.createSvgDocument(null, buf);
                logDocument(svgDocument);

                //it shows what is in png document - unZOOMED values, not limited by px limit
                Dimension2DDouble outSize = new Dimension2DDouble(0.0D, 0.0D);

                ScaleContext scaleContext = ScaleContext.create(previewPanel);

                double scale = scaleContext.getScale(ScaleType.SYS_SCALE);
                if (PlantUmlSettings.getInstance().isSvgPreviewScaling()) {
                    scaledZoom = zoom * scale;
                } else {
                    scaledZoom = zoom;
                }

                BufferedImage image = MySvgTranscoder.createImage((float) scaledZoom, svgDocument, outSize);

                MyImageEditorImpl.Holder newHolder = new MyImageEditorImpl.Holder(image, outSize, zoom, null, null);
                this.holder = newHolder;
                LOG.debug("image created in ", System.currentTimeMillis() - start, "ms", " zoom=", zoom, " scale=", scale, " width=", newHolder.image.getWidth(), " hight=", newHolder.image.getHeight(), " docWidth=", newHolder.outSize.getWidth(), " docHight=", newHolder.outSize.getHeight());
            } catch (Throwable e) {
                String source = null;
                if (buf != null) {
                    source = new String(buf, StandardCharsets.UTF_8);
                }
                LOG.error(e.getMessage() + " - scaledZoom="+scaledZoom+" imageSource: " + source, e);
                this.holder = new Holder(null, null, -1.0, source, e);
            } finally {
                renderingInProgress = false;
            }
        }

        private void logDocument(Document svgDocument) {
            if (LOG.isDebugEnabled()) {
                try {
                    DOMSource domSource = new DOMSource(svgDocument);
                    StringWriter writer = new StringWriter();
                    StreamResult result = new StreamResult(writer);
                    TransformerFactory tf = TransformerFactory.newInstance();
                    Transformer transformer = tf.newTransformer();
                    transformer.transform(domSource, result);

                    String stringFromDocument = writer.toString();
                    if (stringFromDocument != null) {
                        byte[] bytes = stringFromDocument.getBytes(StandardCharsets.UTF_8);
                        String s = Base64.getEncoder().encodeToString(bytes);
                        LOG.debug("svgDocument=" + s);
                    }
                } catch (Throwable ex) {
                    LOG.error(ex);
                }
            }
        }

        public void dispose() {
            holder = new Holder();
        }
    }

    static class Holder {
        private final Dimension2DDouble outSize;
        private final double zoom;
        private final BufferedImage image;
        private String source;
        private Throwable exception;

        public Holder(BufferedImage image, Dimension2DDouble outSize, double zoom, String source, Throwable exception) {
            this.outSize = outSize;
            this.zoom = zoom;
            this.image = image;
            this.source = source;
            this.exception = exception;
        }

        public Holder() {
            this(null, null, -1.0, null, null);
        }

        private boolean isChanged(double zoom) {
            return image == null || this.zoom != zoom;
        }

        public Dimension2DDouble getOutSize() {
            return outSize;
        }

        public double getZoom() {
            return zoom;
        }

        public BufferedImage getImage() {
            return image;
        }

        @Override
        public String toString() {
            return "Holder{" +
                    "outSize=" + outSize +
                    ", zoom=" + zoom +
                    ", image=" + image +
                    ", exception=" + exception +
                    '}';
        }


        public Throwable getException() {
            return exception;
        }

        public String getSource() {
            return source;
        }

        public void setException(Throwable exception) {
            this.exception = exception;
        }
    }
}
