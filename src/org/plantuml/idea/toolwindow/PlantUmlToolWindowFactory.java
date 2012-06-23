package org.plantuml.idea.toolwindow;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.messages.MessageBus;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.plantuml.PlantUmlResult;
import org.plantuml.idea.util.LazyApplicationPoolExecutor;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Eugene Steinberg
 */

public class PlantUmlToolWindowFactory implements ToolWindowFactory {
    Logger logger = Logger.getInstance(PlantUmlToolWindowFactory.class);
    private Project myProject;
    private ToolWindow toolWindow;
    private JPanel mainPanel;
    private JLabel imageLabel;
    private BufferedImage diagram;
    private Document currentDocument;
    private JButton copyToClipboard;
    private JButton saveToFile;
    private FileEditorManagerListener plantUmlVirtualFileListener = new PlantUmlFileManagerListener();
    private DocumentListener plantUmlDocumentListener = new PlantUmlDocumentListener();

    private LazyApplicationPoolExecutor lazyExecutor = new LazyApplicationPoolExecutor();

    public PlantUmlToolWindowFactory() {

    }

    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        myProject = project;
        this.toolWindow = toolWindow;

        createUI();

        registerListeners();

        renderSelectedDocument();

    }

    private void createUI() {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(mainPanel, "", false);
        toolWindow.getContentManager().addContent(content);


        copyToClipboard.addActionListener(new copyToClipboardActionListener());
        saveToFile.addActionListener(new saveToFileActionListener());
    }

    private void renderSelectedDocument() {
        Editor selectedTextEditor = FileEditorManager.getInstance(myProject).getSelectedTextEditor();
        if (selectedTextEditor != null) {
            lazyRender(selectedTextEditor.getDocument());
        }
    }


    private void registerListeners() {
        logger.debug("Registering listeners");
        MessageBus messageBus = myProject.getMessageBus();
        messageBus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, plantUmlVirtualFileListener);

        EditorFactory.getInstance().getEventMulticaster().addDocumentListener(plantUmlDocumentListener);
    }

    private void setDiagram(BufferedImage image) {
        if (image != null) {
            diagram = image;
            imageLabel.setIcon(new ImageIcon(diagram));
            imageLabel.setPreferredSize(new Dimension(diagram.getWidth(), diagram.getHeight()));
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

            VirtualFile newFile = event.getNewFile();
            if (newFile != null) {
                Document document = FileDocumentManager.getInstance().getDocument(newFile);
                if (document != null)
                    lazyRender(document);
            }

        }
    }

    private class PlantUmlDocumentListener implements DocumentListener {
        public void beforeDocumentChange(DocumentEvent event) {
            // nothing

        }

        public void documentChanged(DocumentEvent event) {
            logger.debug("document changed " + event);
            lazyRender(event.getDocument());
        }
    }

    private void lazyRender(final Document document) {
        currentDocument = document;
        lazyExecutor.execute(new Runnable() {
            public void run() {
                render(document.getText());
            }
        });
    }

    private void render(String source) {
        PlantUmlResult result = PlantUml.render(source);
        try {
            final BufferedImage image = getBufferedImage(result.getDiagramBytes());
            ApplicationManager.getApplication().invokeLater(new Runnable() {
                public void run() {
                    setDiagram(image);
                }
            });
        } catch (IOException e) {
            logger.warn("Exception occurred rendering source = " + source + ": " + e);
        }

    }

    private static BufferedImage getBufferedImage(byte[] imageBytes) throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(imageBytes);
        BufferedImage bufferedImage;
        try {
            bufferedImage = ImageIO.read(input);
        } finally {
            input.close();
        }
        return bufferedImage;
    }

    private class copyToClipboardActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            CopyPasteManager.getInstance().setContents(new Transferable() {
                public DataFlavor[] getTransferDataFlavors() {
                    return new DataFlavor[]{
                            DataFlavor.imageFlavor
                    };
                }

                public boolean isDataFlavorSupported(DataFlavor flavor) {
                    return flavor.equals(DataFlavor.imageFlavor);
                }

                public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                    if (!flavor.equals(DataFlavor.imageFlavor)) {
                        throw new UnsupportedFlavorException(flavor);
                    }
                    return diagram;
                }
            });

        }
    }

    public static final String[] extensions;

    static {
        PlantUml.ImageFormat[] values = PlantUml.ImageFormat.values();
        extensions = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            extensions[i] = values[i].toString().toLowerCase();
        }

    }

    private class saveToFileActionListener implements ActionListener {

        public static final String FILENAME = "diagram";

        public void actionPerformed(ActionEvent e) {
            FileOutputStream os = null;
            FileSaverDescriptor fsd = new FileSaverDescriptor("Save diagram", "Please choose where to save diagram", extensions);
            final VirtualFileWrapper wrapper = FileChooserFactory.getInstance().createSaveFileDialog(
                    fsd, myProject).save(null, FILENAME);
            if (wrapper != null) {
                try {
                    File file = wrapper.getFile();
                    String name = file.getName();
                    String extension = name.substring(name.lastIndexOf('.') + 1);
                    PlantUml.ImageFormat imageFormat;
                    try {
                        imageFormat = PlantUml.ImageFormat.valueOf(extension.toUpperCase());
                    } catch (Exception e3) {
                        throw new IOException("Extension '" + extension + "' is not supported");
                    }
                    PlantUmlResult result = PlantUml.render(currentDocument.getText(), imageFormat);
                    os = new FileOutputStream(file);
                    os.write(result.getDiagramBytes());
                    os.flush();
                } catch (Throwable e1) {
                    String title = "Error writing diagram";
                    String message = title + " to file:" + wrapper.getFile() + " : " + e1.toString();
                    logger.warn(message);
                    Messages.showErrorDialog(message, title);
                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e1) {
                            // do nothing
                        }
                    }
                }
            }

        }

    }
}
