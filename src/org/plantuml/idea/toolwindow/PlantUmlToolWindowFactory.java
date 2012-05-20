package org.plantuml.idea.toolwindow;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.messages.MessageBus;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.plantuml.PlantUmlResult;
import org.plantuml.idea.util.LazyApplicationPoolExecutor;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author Eugene Steinberg
 */

public class PlantUmlToolWindowFactory implements ToolWindowFactory {
    Logger logger = Logger.getInstance(PlantUmlToolWindowFactory.class);
    private Project myProject;
    private ToolWindow toolWindow;
    private JPanel mainPanel;
    private JLabel imageLabel;
    private FileEditorManagerListener plantUmlVirtualFileListener = new PlantUmlFileManagerListener();
    private DocumentListener plantUmlDocumentListener = new PlantUmlDocumentListener();

    private LazyApplicationPoolExecutor lazyExecutor = new LazyApplicationPoolExecutor();

    public PlantUmlToolWindowFactory() {

    }

    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        myProject = project;
        this.toolWindow = toolWindow;

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(mainPanel, "", false);
        toolWindow.getContentManager().addContent(content);

        registerListeners();

    }

    private void registerListeners() {
        logger.debug("Registering listeners");
        MessageBus messageBus = myProject.getMessageBus();
        messageBus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, plantUmlVirtualFileListener);

        EditorFactory.getInstance().getEventMulticaster().addDocumentListener(plantUmlDocumentListener);
    }

    private void setDiagram(BufferedImage image) {
        if (image != null) {
            imageLabel.setIcon(new ImageIcon(image));
            imageLabel.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        }
    }

    private class PlantUmlFileManagerListener implements FileEditorManagerListener {
        public void fileOpened(FileEditorManager source, VirtualFile file) {
            logger.debug("file opened " + file);
        }

        public void fileClosed(FileEditorManager source, VirtualFile file) {
            System.out.println("file closed = " + file);
        }

        public void selectionChanged(FileEditorManagerEvent event) {
            logger.debug("selection changed" + event);

            VirtualFile newFile = event.getNewFile();
            if (newFile != null) {
                Document document = FileDocumentManager.getInstance().getDocument(newFile);
                if (document != null)
                    lazyRender(document.getText());
            }

        }
    }

    private class PlantUmlDocumentListener implements DocumentListener {
        public void beforeDocumentChange(DocumentEvent event) {
            // TODO: implement me

        }

        public void documentChanged(DocumentEvent event) {
            logger.debug("document changed " + event);
            lazyRender(event.getDocument().getText());
        }
    }

    private void lazyRender(final String source) {
        lazyExecutor.execute(new Runnable() {
            public void run() {
                render(source);
            }
        });
    }

    private void render(String source) {
        final PlantUmlResult result = PlantUml.render(source);
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
                setDiagram(result.getDiagram());
            }
        });
    }
}

