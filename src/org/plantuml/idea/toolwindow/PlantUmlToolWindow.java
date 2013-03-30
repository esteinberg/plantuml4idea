package org.plantuml.idea.toolwindow;

import com.intellij.openapi.actionSystem.*;
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
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.messages.MessageBus;
import org.plantuml.idea.action.SelectPageAction;
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
    private int page = 0;
    private int numPages = 1;
    private String cachedSource = "";
    private int cachedPage = page;

    Logger logger = Logger.getInstance(PlantUmlToolWindow.class);
    private JLabel imageLabel;

    private FileEditorManagerListener plantUmlVirtualFileListener = new PlantUmlFileManagerListener();
    private DocumentListener plantUmlDocumentListener = new PlantUmlDocumentListener();
    private CaretListener plantUmlCaretListener = new PlantUmlCaretListener();
    private AncestorListener plantUmlAncestorListener = new PlantUmlAncestorListener();
    private ProjectManagerListener plantUmlProjectManagerListener = new PlantUmlProjectManagerListener();

    private LazyApplicationPoolExecutor lazyExecutor = new LazyApplicationPoolExecutor();

    private SelectPageAction selectPageAction;

    public PlantUmlToolWindow(Project myProject, ToolWindow toolWindow) {
        super(new BorderLayout());

        this.myProject = myProject;
        this.toolWindow = toolWindow;

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

        JScrollPane scrollPane = new JBScrollPane(imageLabel);
        add(scrollPane, BorderLayout.CENTER);

        selectPageAction = (SelectPageAction) ActionManager.getInstance().getAction("PlantUML.SelectPage");
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

    private boolean renderRequired(String newSource) {
        if (newSource.isEmpty())
            return false;
        if (!newSource.equals(cachedSource) || page != cachedPage) {
            cachedSource = newSource;
            cachedPage = page;
            return true;
        }
        return false;
    }

    private void renderLater() {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                if (!isProjectValid())
                    return;
                final String source = UIUtils.getSelectedSourceWithCaret(myProject);
                if (!renderRequired(source))
                    return;
                final File selectedDir = UIUtils.getSelectedDir(myProject);
                lazyExecutor.execute(
                        new Runnable() {
                            @Override
                            public void run() {
                                renderWithBaseDir(source, selectedDir, page);
                            }
                        }
                );
            }
        });
    }

    public void renderWithBaseDir(String source, File baseDir, int pageNum) {
        if (source.isEmpty())
            return;
        PlantUmlResult result = PlantUml.render(source, baseDir, pageNum);
        try {
            final BufferedImage image = UIUtils.getBufferedImage(result.getDiagramBytes());
            if (image != null) {
                ApplicationManager.getApplication().invokeLater(new Runnable() {
                    public void run() {
                        UIUtils.setImage(image, imageLabel, zoom);
                    }
                });
            }
            setNumPages(result.getPages());
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

    public void setPage(int page) {
        if (page >= 0 && page < numPages) {
            this.page = page;
            selectPageAction.setPage(page);
            renderLater();
        }
    }

    public void nextPage() {
        setPage(this.page + 1);
    }

    public void prevPage() {
        setPage(this.page - 1);
    }

    public void setNumPages(int numPages) {
        this.numPages = numPages;
        if (page >= numPages)
            setPage(numPages - 1);
        selectPageAction.setNumPages(numPages);
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

