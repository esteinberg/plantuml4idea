package org.plantuml.idea.toolwindow;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
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
import java.io.IOException;

import static com.intellij.codeInsight.completion.CompletionInitializationContext.DUMMY_IDENTIFIER;

/**
 * @author Eugene Steinberg
 */
public class PlantUmlToolWindow extends JPanel {
    private Project myProject;
    private ToolWindow toolWindow;

    Logger logger = Logger.getInstance(PlantUmlToolWindow.class);
    private JLabel imageLabel;

    private FileEditorManagerListener plantUmlVirtualFileListener = new PlantUmlFileManagerListener();
    private DocumentListener plantUmlDocumentListener = new PlantUmlDocumentListener();
    private CaretListener plantUmlCaretListener = new PlantUmlCaretListener();

    private LazyApplicationPoolExecutor lazyExecutor = new LazyApplicationPoolExecutor();


    public PlantUmlToolWindow(Project myProject, ToolWindow toolWindow) {
        super(new BorderLayout());

        this.myProject = myProject;
        this.toolWindow = toolWindow;

        setupUI();

        registerListeners();

        renderSelectedDocument();
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

        toolWindow.getComponent().addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent ancestorEvent) {
                renderSelectedDocument();
            }

            @Override
            public void ancestorRemoved(AncestorEvent ancestorEvent) {
                // do nothing
            }

            @Override
            public void ancestorMoved(AncestorEvent ancestorEvent) {
                // do nothing

            }
        });
    }

    private void renderSelectedDocument() {
        Editor selectedTextEditor = FileEditorManager.getInstance(myProject).getSelectedTextEditor();
        if (selectedTextEditor != null) {
            extractAndRender(selectedTextEditor.getDocument().getText(), 0);
        }
    }

    private void lazyRender(final String source) {
        Runnable command = new Runnable() {
            public void run() {
                render(source);
            }
        };
        lazyExecutor.execute(command);
    }

    private void extractAndRender(final String fullSource, int offset) {
        final String source = PlantUml.extractSource(fullSource, offset);
        if (!source.isEmpty()) {
            lazyRender(source);
        }
    }

    private void render(String source) {
        PlantUmlResult result = PlantUml.render(source);
        try {
            final BufferedImage image = UIUtils.getBufferedImage(result.getDiagramBytes());
            if (image != null) {
                ApplicationManager.getApplication().invokeLater(new Runnable() {
                    public void run() {
                        UIUtils.setImage(image, imageLabel);
                    }
                });
            }
        } catch (IOException e) {
            logger.warn("Exception occurred rendering source = " + source + ": " + e);
        }
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
            Editor selectedTextEditor = FileEditorManager.getInstance(myProject).getSelectedTextEditor();
            if (selectedTextEditor != null) {
                extractAndRender(selectedTextEditor.getDocument().getText(), selectedTextEditor.getCaretModel().getOffset());
            }
        }
    }

    private class PlantUmlDocumentListener implements DocumentListener {

        public void beforeDocumentChange(DocumentEvent event) {
        }

        public void documentChanged(DocumentEvent event) {
            logger.debug("document changed " + event);
            //#18 Strange "IntellijIdeaRulezzz" - filter code completion event.
            if (!DUMMY_IDENTIFIER.equals(event.getNewFragment().toString())) {
                extractAndRender(event.getDocument().getText(), event.getOffset());
            }
        }
    }

    private class PlantUmlCaretListener implements CaretListener {
        @Override
        public void caretPositionChanged(final CaretEvent e) {
            ApplicationManager.getApplication().runReadAction(new Runnable() {
                @Override
                public void run() {
                    String text = e.getEditor().getDocument().getText();
                    int offset = e.getEditor().logicalPositionToOffset(e.getNewPosition());
                    final String source = PlantUml.extractSource(text, offset);
                    if (!source.isEmpty()) {
                        Runnable command = new Runnable() {
                            public void run() {
                                render(source);
                            }
                        };
                        lazyExecutor.execute(command);
                    }
                }
            });
        }
    }
}

