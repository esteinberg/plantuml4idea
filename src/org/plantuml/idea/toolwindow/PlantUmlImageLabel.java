package org.plantuml.idea.toolwindow;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ColoredSideBorder;
import com.intellij.ui.JBColor;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.scale.ScaleContext;
import com.intellij.ui.scale.ScaleType;
import com.intellij.util.ui.ImageUtil;
import com.intellij.util.ui.JBImageIcon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.action.context.*;
import org.plantuml.idea.lang.settings.PlantUmlSettings;
import org.plantuml.idea.rendering.ImageItem;
import org.plantuml.idea.rendering.RenderRequest;
import org.plantuml.idea.rendering.RenderResult;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URI;
import java.util.Map;

public class PlantUmlImageLabel extends JLabel {
    private static final AnAction[] AN_ACTIONS = {
            new SaveDiagramToFileContextAction(),
            new CopyDiagramToClipboardContextAction(),
            Separator.getInstance(),
            new CopyDiagramAsTxtToClipboardContextAction(),
            new CopyDiagramAsUnicodeTxtToClipboardContextAction(),
            Separator.getInstance(),
            new CopyDiagramAsLatexToClipboardContextAction(),
            new CopyDiagramAsTikzCodeToClipboardContextAction(),
            Separator.getInstance(),
            new ExternalOpenDiagramAsPNGAction(),
            new ExternalOpenDiagramAsSVGAction(),
            Separator.getInstance(),
            new CopyPlantUmlServerLinkContextAction()
    };
    private static final ActionPopupMenu ACTION_POPUP_MENU = ActionManager.getInstance().createActionPopupMenu(ActionPlaces.UNKNOWN, new ActionGroup() {

        @NotNull
        @Override
        public AnAction[] getChildren(@Nullable AnActionEvent e) {
            return AN_ACTIONS;
        }
    });

    private static Logger logger = Logger.getInstance(PlantUmlImageLabel.class);
    private final Project project;
    private final RenderResult renderResult;
    private RenderRequest renderRequest;
    private ImageItem imageWithData;
    private Image originalImage;

    private FileEditorManager fileEditorManager;
    private LocalFileSystem localFileSystem;

    public PlantUmlImageLabel(Project project, JPanel parent, ImageItem imageWithData, int i, RenderRequest renderRequest, RenderResult renderResult, FileEditorManager fileEditorManager, LocalFileSystem localFileSystem) {
        this.imageWithData = imageWithData;
        this.project = project;
        this.renderResult = renderResult;
        setup(parent, this.imageWithData, i, renderRequest);
        this.fileEditorManager = fileEditorManager;
        this.localFileSystem = localFileSystem;
    }

    public ImageItem getImageWithData() {
        return imageWithData;
    }

    public int getPage() {
        return imageWithData.getPage();
    }

    public RenderRequest getRenderRequest() {
        return renderRequest;
    }

    public void setup(JPanel parent, @NotNull ImageItem imageWithData, int i, RenderRequest renderRequest) {
        setOpaque(true);
        setBackground(JBColor.WHITE);
        if (imageWithData.hasImage()) {
            setDiagram(parent, imageWithData, renderRequest, this);
        } else {
            setText("page not rendered, probably plugin error, please report it and try to hit reload");
        }
        this.renderRequest = renderRequest;
    }

    private void setDiagram(JPanel parent, @NotNull final ImageItem imageItem, RenderRequest renderRequest, final JLabel label) {
        originalImage = imageItem.getImage();
        Image scaledImage;

        ScaleContext ctx = ScaleContext.create(parent);
        scaledImage = ImageUtil.ensureHiDPI(originalImage, ctx);
//        scaledImage = ImageLoader.scaleImage(scaledImage, ctx.getScale(JBUI.ScaleType.SYS_SCALE));

        label.setIcon(new JBImageIcon(scaledImage));
        label.addMouseListener(new PopupHandler() {

            @Override
            public void invokePopup(Component comp, int x, int y) {
                ACTION_POPUP_MENU.getComponent().show(comp, x, y);
            }
        });

        //Removing all children from image label and creating transparent buttons for each item with url

        label.removeAll();
        boolean showUrlLinksBorder = PlantUmlSettings.getInstance().isShowUrlLinksBorder();

        for (ImageItem.LinkData linkData : imageItem.getLinks()) {
            JLabel button = new JLabel();
            if (showUrlLinksBorder) {
                button.setBorder(new ColoredSideBorder(Color.RED, Color.RED, Color.RED, Color.RED, 1));
            }
            Rectangle area = linkData.getClickArea();

            int tolerance = 2;
            double scale = ctx.getScale(ScaleType.SYS_SCALE);
            int x = (int) ((double) area.x / scale) - 2 * tolerance;
            int width = (int) ((area.width) / scale) + 4 * tolerance;

            int y = (int) (area.y / scale) - tolerance;
            int height = (int) ((area.height) / scale) + 5 * tolerance;

            area = new Rectangle(x, y, width, height);

            button.setLocation(area.getLocation());
            button.setSize(area.getSize());

            button.setCursor(new Cursor(Cursor.HAND_CURSOR));

            //When user clicks on item, url is opened in default system browser
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    String text = linkData.getText();
                    try {
                        if (linkData.isLink()) {
                            if (isWebReferenceUrl(text)) {
                                Desktop.getDesktop().browse(URI.create(text));
                            } else {

                                if (openFile(new File(renderRequest.getBaseDir(), text))) return;
                                navigateToSource(text);
                            }
                        } else {
                            navigateToSource(text);
                        }
                    } catch (
                            Exception ex) {
                        logger.warn(ex);
                    }
                }

                private void navigateToSource(String text) {
                    File sourceFile = renderRequest.getSourceFile();
                    VirtualFile virtualFile = localFileSystem.findFileByPath(sourceFile.getAbsolutePath());
                    if (virtualFile != null) {
                        Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
                        if (document != null) {
                            String documentText = document.getText();
                            int i = documentText.indexOf(text);
                            if (i >= 0) {
                                navigateToEditor(virtualFile, i);
                            } else {
                                navigateToIncludedFile(text);
                            }
                        }
                    }
                }

                private void navigateToIncludedFile(String text) {
                    Map<File, Long> includedFiles = renderResult.getIncludedFiles();
                    for (File file : includedFiles.keySet()) {
                        VirtualFile virtualFile = localFileSystem.findFileByPath(file.getAbsolutePath());
                        if (virtualFile != null) {
                            Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
                            if (document != null) {
                                String documentText = document.getText();
                                int i = documentText.indexOf(text);
                                if (i >= 0) {
                                    navigateToEditor(virtualFile, i);
                                    return;
                                }
                            }
                        }
                    }
                }

                private void navigateToEditor(VirtualFile virtualFile, int i) {
                    FileEditor[] fileEditors = fileEditorManager.openFile(virtualFile, true, true);
                    if (fileEditors.length != 0) {
                        FileEditor fileEditor = fileEditors[0];
                        if (fileEditor instanceof TextEditor) {
                            Editor editor = ((TextEditor) fileEditor).getEditor();
                            editor.getCaretModel().moveToOffset(i);
                        }
                    }
                }

                private boolean openFile(File file) {
                    if (file.exists()) {
                        VirtualFile virtualFile = localFileSystem.findFileByPath(file.getAbsolutePath());
                        if (virtualFile == null) {
                            return false;
                        }
                        FileEditor[] fileEditors = fileEditorManager.openFile(virtualFile, true, true);
                        return fileEditors.length > 0;
                    }
                    return false;
                }
            });


            label.add(button);
        }

    }

    public static boolean isWebReferenceUrl(String url) {
        return url.startsWith("www.") || url.startsWith("http://") || url.startsWith("https://") || url.startsWith("about:") || url.startsWith("mailto:");
    }

    public Image getOriginalImage() {
        return originalImage;
    }
}
