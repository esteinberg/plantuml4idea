package org.plantuml.idea.util;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.rendering.LazyApplicationPoolExecutor;
import org.plantuml.idea.toolwindow.PlantUmlToolWindow;
import org.plantuml.idea.toolwindow.PlantUmlToolWindowFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

/**
 * @author Eugene Steinberg
 */
public class UIUtils {
    private static Logger logger = Logger.getInstance(UIUtils.class);

    public static BufferedImage getBufferedImage(byte[] imageBytes) throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(imageBytes);
        return ImageIO.read(input);
    }

    public static String getSelectedSourceWithCaret(Project myProject) {
        String source = getSelectedSource(myProject);

        Editor selectedTextEditor = FileEditorManager.getInstance(myProject).getSelectedTextEditor();
        if (selectedTextEditor != null) {
            final Document document = selectedTextEditor.getDocument();
            int offset = selectedTextEditor.getCaretModel().getOffset();
            source = PlantUml.extractSource(document.getText(), offset);
        }
        return source;
    }

    public static String getSelectedSource(Project myProject) {
        String source = "";
        Editor selectedTextEditor = FileEditorManager.getInstance(myProject).getSelectedTextEditor();
        if (selectedTextEditor != null) {
            final Document document = selectedTextEditor.getDocument();
            source = document.getText();
        }
        return source;
    }


    public static VirtualFile getSelectedFile(Project myProject) {
        Editor selectedTextEditor = FileEditorManager.getInstance(myProject).getSelectedTextEditor();
        VirtualFile file = null;
        if (selectedTextEditor != null) {
            final Document document = selectedTextEditor.getDocument();
            file = FileDocumentManager.getInstance().getFile(document);
        }
        return file;
    }

    public static File getSelectedDir(Project myProject) {
        Editor selectedTextEditor = FileEditorManager.getInstance(myProject).getSelectedTextEditor();
        File baseDir = null;
        if (selectedTextEditor != null) {

            final Document document = selectedTextEditor.getDocument();
            final VirtualFile file = FileDocumentManager.getInstance().getFile(document);
            baseDir = getParent(file);
        }
        return baseDir;
    }

    public static File getParent(VirtualFile file) {
        File baseDir = null;
        if (file != null) {
            VirtualFile parent = file.getParent();
            if (parent != null && parent.isDirectory()) {
                baseDir = new File(parent.getPath());
            }
        }
        return baseDir;
    }

    @Nullable
    public static PlantUmlToolWindow getPlantUmlToolWindow(@NotNull Project project) {
        PlantUmlToolWindow result = null;
        ToolWindow toolWindow = getToolWindow(project);
        if (toolWindow != null) {
            result = getPlantUmlToolWindow(toolWindow);
        }
        return result;
    }

    @Nullable
    public static PlantUmlToolWindow getPlantUmlToolWindow(@NotNull ToolWindow toolWindow) {
        PlantUmlToolWindow result = null;
        Content[] contents = toolWindow.getContentManager().getContents();
        if (contents.length > 0) {
            JComponent component = contents[0].getComponent();
            //it can be JLabel "Initializing..."
            if (component instanceof PlantUmlToolWindow) {
                result = (PlantUmlToolWindow) component;
            }
        }
        return result;
    }


    @Nullable
    public static ToolWindow getToolWindow(@NotNull Project project) {
        ToolWindowManager instance = ToolWindowManager.getInstance(project);
        if (instance == null) {
            return null;
        }
        return instance.getToolWindow(PlantUmlToolWindowFactory.ID);
    }

    public static void renderPlantUmlToolWindowLater(@Nullable Project project, LazyApplicationPoolExecutor.Delay delay) {
        if (project == null) return;

        ToolWindow toolWindow = getToolWindow(project);
        if (toolWindow == null || !toolWindow.isVisible()) {
            return;
        }

        PlantUmlToolWindow plantUmlToolWindow = getPlantUmlToolWindow(toolWindow);
        if (plantUmlToolWindow != null) {
            plantUmlToolWindow.renderLater(delay);
        }
    }


}
