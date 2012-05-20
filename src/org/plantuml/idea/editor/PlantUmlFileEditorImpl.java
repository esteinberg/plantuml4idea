package org.plantuml.idea.editor;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.messages.MessageBus;
import org.intellij.images.ui.ImageComponent;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.messaging.RenderingNotifier;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.plantuml.PlantUmlResult;
import org.plantuml.idea.util.LazyApplicationPoolExecutor;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;

/**
 * User: eugene
 * Date: 5/7/12
 * Time: 12:54 AM
 */
public class PlantUmlFileEditorImpl extends UserDataHolderBase implements FileEditor {
    private static final String NAME = "PlantUMLFileEditor";
    public static final int RENDERING_PERIOD = 2000;

    private ImageComponent imageComponent = new ImageComponent();
    private JLabel description = new JLabel("", null, SwingConstants.LEFT);
    JPanel mainPanel = new JPanel();

    private SourceDocumentListener sourceDocumentListener = new SourceDocumentListener();
    private SourceVirtualFileListener sourceVirtualFileListener = new SourceVirtualFileListener();
    Document sourceDocument;

    private LazyApplicationPoolExecutor lazyExecutor = new LazyApplicationPoolExecutor(RENDERING_PERIOD);

    public PlantUmlFileEditorImpl(Project project, VirtualFile file) {

        sourceDocument = FileDocumentManager.getInstance().getDocument(file);

        if (sourceDocument != null) {
            sourceDocument.addDocumentListener(sourceDocumentListener);
        }

        VirtualFileManager.getInstance().addVirtualFileListener(sourceVirtualFileListener);
        mainPanel.add(imageComponent);
        mainPanel.add(description);

        MessageBus messageBus = ApplicationManager.getApplication().getMessageBus();

        messageBus.connect().subscribe(RenderingNotifier.RENDERING_TOPIC, new RenderingNotifier() {
            public void afterRendering(PlantUmlResult result) {
                BufferedImage diagram = result.getDiagram();
                description.setText(result.getDescription());
                if (diagram != null) {
                    imageComponent.getDocument().setValue(diagram);
                    imageComponent.setPreferredSize(new Dimension(diagram.getWidth(), diagram.getHeight()));
                }
            }
        });


        lazyRender();
    }

    @NotNull
    public JComponent getComponent() {
        return mainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return mainPanel;
    }

    @NotNull
    public String getName() {
        return NAME;
    }

    @NotNull
    public FileEditorState getState(@NotNull FileEditorStateLevel level) {
        return new FileEditorState() {
            public boolean canBeMergedWith(FileEditorState otherState, FileEditorStateLevel level) {
                return false;
            }
        };
    }

    public void setState(@NotNull FileEditorState state) {
        // do nothing;
    }

    public boolean isModified() {
        return false;
    }

    public boolean isValid() {
        return true;
    }

    public void selectNotify() {
        // do nothing
    }

    public void deselectNotify() {
        // do nothing
    }

    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
        // do nothing
    }

    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
        // do nothing
    }

    public BackgroundEditorHighlighter getBackgroundHighlighter() {
        return null;
    }

    public FileEditorLocation getCurrentLocation() {
        return null;
    }

    public StructureViewBuilder getStructureViewBuilder() {
        return null;
    }

    public void dispose() {
        if (sourceDocument != null) {
            sourceDocument.removeDocumentListener(sourceDocumentListener);
        }
        VirtualFileManager.getInstance().removeVirtualFileListener(sourceVirtualFileListener);
    }

    public ImageComponent getImageComponent() {
        return imageComponent;
    }

    private void lazyRender() {
        lazyExecutor.execute(new Runnable() {
            public void run() {
                render();
            }
        });
    }


    private void render() {
        if (sourceDocument != null) {
            final PlantUmlResult result = PlantUml.render(sourceDocument.getText());
            ApplicationManager.getApplication().invokeLater(new Runnable() {
                public void run() {
                    MessageBus messageBus = ApplicationManager.getApplication().getMessageBus();
                    RenderingNotifier renderingNotifier = messageBus.syncPublisher(RenderingNotifier.RENDERING_TOPIC);
                    renderingNotifier.afterRendering(result);

                }
            });
        }
    }

    private class SourceDocumentListener implements DocumentListener {
        public void beforeDocumentChange(DocumentEvent event) {
            // nothing
        }

        public void documentChanged(DocumentEvent event) {
            lazyRender();
        }
    }

    private class SourceVirtualFileListener extends VirtualFileAdapter {
        public void contentsChanged(VirtualFileEvent event) {
            super.contentsChanged(event);
            lazyRender();
        }

    }

}
