package org.plantuml.idea.toolwindow;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.util.messages.MessageBus;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.plantuml.PlantUmlResult;
import org.plantuml.idea.util.LazyApplicationPoolExecutor;
import org.plantuml.idea.util.UIUtils;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import static com.intellij.codeInsight.completion.CompletionInitializationContext.DUMMY_IDENTIFIER;

/**
 * @author Eugene Steinberg
 */
public class PlantUmlToolWindow extends JPanel {
    private Project myProject;
    private ToolWindow toolWindow;
    private int zoom = 100;

    Logger logger = Logger.getInstance(PlantUmlToolWindow.class);
    private JLabel imageLabel;

    private FileEditorManagerListener plantUmlVirtualFileListener = new PlantUmlFileManagerListener();
    private DocumentListener plantUmlDocumentListener = new PlantUmlDocumentListener();
    private CaretListener plantUmlCaretListener = new PlantUmlCaretListener();
    private AncestorListener plantUmlAncestorListener = new PlantUmlAncestorListener();
    private ProjectManagerListener plantUmlProjectManagerListener = new PlantUmlProjectManagerListener();

    private LazyApplicationPoolExecutor lazyExecutor = new LazyApplicationPoolExecutor();

    public PlantUmlToolWindow(Project myProject, ToolWindow toolWindow) {
        super(new BorderLayout());

        this.myProject = myProject;
        this.toolWindow = toolWindow;
        this.zoom = 100;

        setupUI();

        registerListeners();

        renderLater();
    }

    private void setupUI() {
        ActionGroup group = (ActionGroup) ActionManager.getInstance().getAction("PlantUML.Toolbar");
        final ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, group, true);
        actionToolbar.setTargetComponent(this);
        add(actionToolbar.getComponent(), BorderLayout.PAGE_START);

        imageLabel = new JLabel();

        JScrollPane scrollPane = new JScrollPane(imageLabel);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void registerListeners() {
        logger.debug("Registering listeners");
        MessageBus messageBus = myProject.getMessageBus();
        messageBus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, plantUmlVirtualFileListener);

        EditorFactory.getInstance().getEventMulticaster().addDocumentListener(plantUmlDocumentListener);
        EditorFactory.getInstance().getEventMulticaster().addCaretListener(plantUmlCaretListener);

        toolWindow.getComponent().addAncestorListener(plantUmlAncestorListener);

        ProjectManager.getInstance().addProjectManagerListener(plantUmlProjectManagerListener);
    }

    private void renderLater() {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                if (!isProjectValid())
                    return;
                final String source = UIUtils.getSelectedSourceWithCaret(myProject);
                if (source.isEmpty())
                    return;
                final File selectedDir = UIUtils.getSelectedDir(myProject);
                lazyExecutor.execute(
                        new Runnable() {
                            @Override
                            public void run() {
                                renderWithBaseDir(source, selectedDir);
                            }
                        }
                );
            }
        });
    }

    private void renderWithBaseDir(String source, File baseDir) {
        if (source.isEmpty())
            return;
        PlantUmlResult result = PlantUml.render(source, baseDir);
        try {
            final BufferedImage image = UIUtils.getBufferedImage(result.getDiagramBytes());
            if (image != null) {
                ApplicationManager.getApplication().invokeLater(new Runnable() {
                    public void run() {
                        UIUtils.setImage(image, imageLabel, zoom);
                    }
                });
            }
        } catch (Exception e) {
            logger.warn("Exception occurred rendering source = " + source + ": " + e);
        }
    }

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
        renderLater();
    }

    private class PlantUmlFileManagerListener implements FileEditorManagerListener {
        public void fileOpened(FileEditorManager source, VirtualFile file) {
            logger.debug("file opened " + file);
        }

        public void fileClosed(FileEditorManager source, VirtualFile file) {
            logger.debug("file closed = " + file);
        }

        public void selectionChanged(FileEditorManagerEvent event) {
            logger.debug("selection changed" + event);
            renderLater();
        }
    }

    private class PlantUmlDocumentListener implements DocumentListener {

        public void beforeDocumentChange(DocumentEvent event) {
        }

        public void documentChanged(DocumentEvent event) {
            logger.debug("document changed " + event);
            //#18 Strange "IntellijIdeaRulezzz" - filter code completion event.
            if (!DUMMY_IDENTIFIER.equals(event.getNewFragment().toString())) {
                renderLater();
            }
        }
    }

    private boolean isProjectValid() {
        return myProject != null && !myProject.isDisposed();
    }

    private class PlantUmlCaretListener implements CaretListener {
        @Override
        public void caretPositionChanged(final CaretEvent e) {
            renderLater();
        }
    }

    private class PlantUmlAncestorListener implements AncestorListener {
        @Override
        public void ancestorAdded(AncestorEvent ancestorEvent) {
            renderLater();
        }

        @Override
        public void ancestorRemoved(AncestorEvent ancestorEvent) {
            // do nothing
        }

        @Override
        public void ancestorMoved(AncestorEvent ancestorEvent) {
            // do nothing

        }
    }

    private class PlantUmlProjectManagerListener implements ProjectManagerListener {
        @Override
        public void projectOpened(Project project) {
            logger.debug("opened project " + project);
            myProject = project;
        }

        @Override
        public boolean canCloseProject(Project project) {
            return true;
        }

        @Override
        public void projectClosed(Project project) {
            logger.debug("closed project " + project);
            myProject = null;
        }

        @Override
        public void projectClosing(Project project) {
        }
    }
}

