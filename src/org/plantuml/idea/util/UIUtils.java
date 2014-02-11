package org.plantuml.idea.util;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.UIUtil;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.plantuml.PlantUml;
import org.plantuml.idea.toolwindow.PlantUmlToolWindow;
import org.w3c.dom.svg.SVGDocument;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Eugene Steinberg
 */
public class UIUtils {
    private static Map<Project, PlantUmlToolWindow> windowMap = new ConcurrentHashMap<Project, PlantUmlToolWindow>();

    public static BufferedImage getBufferedImage(byte[] imageBytes, int zoom) throws IOException {

        String xmlParser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory documentFactory = new SAXSVGDocumentFactory(xmlParser);
        InputStream inputStream = new ByteArrayInputStream(imageBytes);

        SVGDocument document = documentFactory.createSVGDocument("http://www.w3.org/2000/svg",
                new InputStreamReader(inputStream, "utf8"));

        UserAgentAdapter userAgent = new UserAgentAdapter();
        DocumentLoader documentLoader = new DocumentLoader(userAgent);
        BridgeContext bridgeContext = new BridgeContext(userAgent, documentLoader);
        bridgeContext.setDynamicState(BridgeContext.DYNAMIC);
        GVTBuilder builder = new GVTBuilder();
        GraphicsNode graphicsNode = builder.build(bridgeContext, document);
        Rectangle2D rect = graphicsNode.getGeometryBounds();
        int width = (int) Math.round(rect.getWidth() * 1.1d * zoom / 100d);
        int height = (int) Math.round(rect.getHeight() * 1.1d * zoom / 100d);
        BufferedImage bi = UIUtil.createImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = bi.createGraphics();
        graphics.scale(zoom / 100d, zoom / 100d);
        graphicsNode.paint(graphics);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(bi, "PNG", os);
        ByteArrayInputStream input = new ByteArrayInputStream(os.toByteArray());
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
            if (file != null) {
                VirtualFile parent = file.getParent();
                if (parent != null && parent.isDirectory()) {
                    baseDir= new File(parent.getPath());
                }
            }
        }
        return baseDir;
    }

    @Nullable
    public static PlantUmlToolWindow getToolWindow(Project project) {
        return project != null ? windowMap.get(project) : null;
    }

    public static void addProject(Project project, PlantUmlToolWindow toolWindow) {
        windowMap.put(project, toolWindow);
    }

    public static void removeProject(Project project) {
        windowMap.remove(project);
    }
}
